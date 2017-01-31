package com.magnifis.parking;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.w3c.dom.Element;

import com.magnifis.parking.cmd.SendCmdHandler;
import com.magnifis.parking.cmd.i.ClientStateInformer;
import com.magnifis.parking.cmd.i.UnderstandingHandler;
import com.magnifis.parking.db.AndroidContentProvider;
import com.magnifis.parking.feed.SmsFeedController;
import com.magnifis.parking.messaging.Addressable;
import com.magnifis.parking.messaging.Message;
import com.magnifis.parking.model.AndroidCalendar;
import com.magnifis.parking.model.QueryInterpretation;
import com.magnifis.parking.model.SmsRecord;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.phonebook.PhoneBook;
import com.magnifis.parking.pref.PrefConsts;
import com.magnifis.parking.suzie.RequiresSuzie;
import com.magnifis.parking.suzie.SuziePopup;
import com.magnifis.parking.suzie.SuzieService;
import com.magnifis.parking.toast.MagToast;
import com.magnifis.parking.toast.ToastBase;
import com.magnifis.parking.toast.ToastController;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Translit;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.views.ProgressSpinner;
import com.magnifis.parking.views.SmsAlertView;

import static com.magnifis.parking.tts.MyTTS.speakText;
import static com.magnifis.parking.utils.Utils.*;
import static com.robinlabs.utils.BaseUtils.isEmpty;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class SmsActivity extends Activity implements TextMessageQue,
		RequiresSuzie {
	final static String TAG = SmsActivity.class.getSimpleName();

	class Wrapper extends MyTTS.Wrapper {
		@Override
		public boolean isBubblesInMainActivityOnly() {
			return SmsActivity.this.handle_via_main_activity;
		}

		public Wrapper(Object obj) {
			super(obj);
		}
	}

	final static int MODE_NEW_SMS = 0, MODE_CONFIRM_READING = 1,
			MODE_READING_CONFIRMED = 2, MODE_CONFIRM_REPLY = 3,
			MODE_REPLY_CONFIRMED = 4, MODE_DICTATE_MESSAGE = 5,
			MODE_CONFIRM_THAT_MESSAGE_IS_RIGHT = 6;

	final static String ACTION_COMPOSER = "com.magnifis.parking.COMPOSER";

	final static int DICTATION_MAX_COUNTER = SendCmdHandler.DICTATION_MAX_COUNTER;

	private int mode = MODE_NEW_SMS;

	VR vr = null;
	ProgressSpinner mSpinner = null;

	private SmsFeedController feedController = null;
	private Object fcSO = new Object();

	private SmsFeedController getController() {
		synchronized (fcSO) {
			if (feedController == null)
				feedController = SmsFeedController.getInstance();
		}
		return feedController;
	}

	boolean instant, handle_via_main_activity;

	private Uri inbox_url = Uri.parse("content://mms-sms/inbox");

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		/*
		 * View v=new View(this);
		 * v.setBackgroundColor(getResources().getColor(R.color.SplashBgColor));
		 * setContentView(v);
		 */
		if (useContentObserver)
			getContentResolver().registerContentObserver(inbox_url, true,
					observer);
		instant = App.self.shouldReadSmsInstantly();
		Intent it = getIntent();
		handleIntent(it, true);
	}

	static boolean useContentObserver = false;

	private ContentObserver observer = new ContentObserver(null) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			Log.d(SmsActivity.TAG, "ContentObserver.onChange ");
			List<SmsRecord> msgs = AndroidContentProvider.get(
					Uri.parse("content://sms/inbox"), SmsRecord.class, null,
					"(read=0) and (seen=0)", "date desc");
			if (!isEmpty(msgs)) {
				Log.d(TAG, "ContentObserver.onChange# " + msgs.get(0).getBody());
			}
		}

	};

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart " + Utils.isForegroundPackage());
	}

	private Vector<Intent> smsIntents = new Vector<Intent>();

	private StringBuilder getSmsToSpeak() {
		return getSmsPartToSpeak(SmsFeedController.PLAY_MODE_NORMAL);
	}
/*
	private StringBuilder getSmsToShow() {
		return getSmsPartToShow(SmsFeedController.PLAY_MODE_NORMAL);
	}
*/
	private Message getCurrentSms() {
		if (!isEmpty(smsIntents)) {
			List<Message> lst = SmsFeedController.from(smsIntents.get(0));
			if (!isEmpty(lst))
				return lst.get(0);
		}
		return null;
	}

	private StringBuilder getSmsPartToSpeak(int part) {
		if (!isEmpty(smsIntents))
			return SmsFeedController.getSmsPartForSpeach(
					SmsFeedController.from(smsIntents.get(0)), part);
		return null;
	}

	private Addressable getSmsSender() {
		if (!isEmpty(smsIntents)) {
			List<Message> lst = SmsFeedController.from(smsIntents.get(0));
			if (!isEmpty(lst)) {
				return lst.get(0).getSender();
			}
		}
		return null;
	}

	private StringBuilder getSmsPartToShow(int part) {
		if (!isEmpty(smsIntents))
			return SmsFeedController.getSmsPartToShow(
					SmsFeedController.from(smsIntents.get(0)), part);
		return null;
	}

	private StringBuilder getSmsHeaderToSpeak() {
		return getSmsPartToSpeak(SmsFeedController.PLAY_MODE_HEADER_ONLY);
	}

	private StringBuilder getSmsHeaderToShow() {
		return getSmsPartToShow(SmsFeedController.PLAY_MODE_HEADER_ONLY);
	}

	private StringBuilder getSmsBodyToSpeak() {
		return getSmsPartToSpeak(SmsFeedController.PLAY_MODE_BODY_ONLY);
	}

	private StringBuilder getSmsBodyToShow() {
		return getSmsPartToShow(SmsFeedController.PLAY_MODE_BODY_ONLY);
	}

	void cancelWaitingForListeningResults() {
		if (vr != null) {
			if (listeningTimeoutTask != null) {
				listeningTimeoutTask.cancel();
				listeningTimeoutTask = null;
			}
			vr.killMicrophone();
			vr = null;
		}
	}

	void handleIntent(Intent it, final boolean fOnCreate) {
		Log.d(TAG, "handleIntent 0");

		boolean newSmsIntent = SMSReceiver.SMS_ARRIVED.equals(it.getAction());

		if (newSmsIntent)
			handle_via_main_activity = it.getBooleanExtra(
					MainActivity.HANDLE_VIA_MAIN_ACTIVITY, false);

		if (App.self.isInSilentModeOrConversation()) {
			if (fOnCreate)
				doFinish();
			return;
		}

		Log.d(TAG, "handleIntent 1");

		if (SuziePopup.get() != null)
			SuziePopup.get().disableBubles();
		
		Log.d(TAG, "handleIntent 2");

		if (MainActivity.VR_RESULTS.equals(it.getAction())) {
			onActivityResult(VR.VOICE_RECOGNITION_REQUEST_CODE,
					Activity.RESULT_OK, it);
			return;
		}

		Log.d(TAG, "handleIntent 3");

		/*
		 * if (ACTION_COMPOSER.equals(it.getAction())) {
		 * MyTTS.abortWithoutUnlock(); Message m=getCurrentSms()
		 * composeTextMessage(dictatedMessage,
		 * getCurrentSms().getSender().getAddress()); return; }
		 */

		Log.d(TAG, "handleIntent 4");

		if (RESULT.equals(it.getAction())) {
			Utils.dump(TAG, it);
			onActivityResult(VR.VOICE_RECOGNITION_REQUEST_CODE, it.getIntExtra(
					MainActivity.LISTENING_RESULT_CODE, 0),
					(Intent) it
							.getParcelableExtra(MainActivity.LISTENING_RESULT));
			return;
		}

		Log.d(TAG, "handleIntent 5");

		if (SmsFeedController.INTENT_SENT_OR_FAILED.equals(it.getAction())) {
			getController().handleSendingStatusIntent(this, this, it);
			return;
		}

		Log.d(TAG, "handleIntent 6");

		if (!newSmsIntent) {
			if (fOnCreate)
				doFinish();
			return;
		}

		Log.d(TAG, "handleIntent 7");

		cancelWaitingForListeningResults();

		Log.d(TAG, "handleIntent 8");

		synchronized (smsIntents) {
			Intent data = it.getParcelableExtra(Intent.EXTRA_INTENT);
			if (fOnCreate) { // may be instance reuse
				Log.d(TAG, "handleIntent 9");
				Log.d(TAG, "instance reuse");
				smsIntents.add(inSmsPocessing ? smsIntents.size() : 0, data);
			} else {
				Log.d(TAG, "handleIntent 10");
				smsIntents.add(smsIntents.isEmpty() ? 0 : 1, data);
				if (smsIntents.size() > 1)
					return;
			}
		}

		Log.d(TAG, "handleIntent 11");

		startNewSmsProcessing();
	}

	private Translit translit = Translit.getHeb();

	private void sendMessageAndAfter() {
		final Addressable sender = getSmsSender();
		Output.sayAndShow(this, new Wrapper(
				getString(R.string.mainactivity_onpostexecute_texting) + " "
						+ translit.process(sender.getSynteticDisplayName(true))) {

			@Override
			public void onSaid(boolean fAborted) {
				if (fAborted)
					handleNextOrDie();
				else {
					/*
					 * Intent nfyIntent=new
					 * Intent(SmsFeedController.INTENT_SENT_OR_FAILED);
					 * nfyIntent.putExtra( SmsFeedController.COMPONENT, new
					 * ComponentName(SmsActivity.this,SmsActivity.class) );
					 */
					getController().sendSms(sender.getAddress(),
							dictatedMessage, /* nfyIntent */null);
					handleNextOrDie(); // don't wait for sending result
				}
			}

		});

	}

	private void replyConfirmed() {

		fireOpes();
		doFinish();

		Addressable sender = getSmsSender();
		if (sender != null)
			SendCmdHandler.startReplyTo(sender.getAddress());

		/*
		 * String lang = null; if (!isEmpty(smsIntents)) { List<Message>
		 * lst=SmsFeedController.from(smsIntents.get(0)); if (!isEmpty(lst))
		 * lang = SmsFeedController.getSmsLanguage(lst.get(0)); }
		 * 
		 * if (Utils.isEmpty(lang)) Output.sayAndShow(this, new
		 * Wrapper(R.string.P_SAY_YOUR_MSG)); else Output.sayAndShow(this, new
		 * Wrapper(App.self.getString(R.string.P_SAY_YOUR_MSG_LANG) + " " +
		 * lang));
		 * 
		 * final String lang2 = lang;
		 * 
		 * mode=MODE_DICTATE_MESSAGE; MyTTS.execAfterTheSpeech( new Runnable() {
		 * 
		 * @Override public void run() { listenForDictation(lang2); } } );
		 */
	}

	private void playConfirmed() {
		playSingleSmsAndAfter(false);
	}

	private void confirmReadingOrFinishSingleSmsProcessing(
			boolean shouldConfirmReading) {
		Log.d(TAG, "confirmReadingOrFinishSingleSmsProcessing");

		if (smsIntents.isEmpty()) {
			fireOpes();
			doFinish();
			return;
		}

		if (instant) {
			if (smsIntents.size() > 1) {
				startNewSmsProcessing();
				return;
			}
			fireOpes();
			doFinish();
		} else {
			mode = MODE_CONFIRM_READING;
			if (!shouldConfirmReading && !instant) {
				// confirm reading of a next message
				Output.sayAndShow(SmsActivity.this, new Wrapper(
						R.string.mainactivity_new_message));
				MyTTS.execAfterTheSpeech(new Runnable() {
					@Override
					public void run() {
						listenForConfirmation();
					}
				});
			} else {
				// confirm reading of a first message
				listenForConfirmation();
			}
		}
	}

	private void confirmDesireToReplyAndAfter() {
		Log.d(TAG, "confirmDesireToReplyAndAfter");
		mode = MODE_CONFIRM_REPLY;
		listenForConfirmation();
	}

	private void playSingleSmsAndAfter(final boolean shouldConfirmReading) {
		Log.d(TAG, "playSingleSmsAndAfter 0");
		hideSmsAlert();

		boolean voiced_mode = !App.self.isInSilentMode();

		final boolean locked = App.self.isPhoneLocked() || !App.self.isScreenOn()
				|| App.self.isInPhoneConversation(), lockedNotMA = locked
				&& !handle_via_main_activity;
		final boolean can_read = App.self.shouldSpeakIncomingSms();

		StringBuilder toSay = new StringBuilder(), toShow = new StringBuilder();
/*
		if (shouldConfirmReading) {
			toSay.append((!instant && !lockedNotMA && !read_header && can_read) ? getString(R.string.mainactivity_new_message)
					: "");
			// toSay.append(" . ");
		}
		*/

		final boolean fReadMessage = instant || !shouldConfirmReading;

		if (fReadMessage) {
			if (instant) {
                StringBuilder s = getSmsToSpeak();
                toShow.append(s);
				toSay.append(" . ");
				toSay.append(s);
			} else {
				toShow.append(getSmsBodyToShow());
				toSay.append(translit.process(getSmsBodyToSpeak()));
			}
			if (!lockedNotMA) {
				toSay.append(" . ");
				toSay.append(getString(R.string.P_want_to_reply));
			}
		} else if (!instant) {
			toShow.append(getSmsHeaderToShow());
			toSay.append(translit.process(getSmsHeaderToSpeak()));
			if (!lockedNotMA && !can_read) {
				toSay.append(" . ");
				toSay.append(App.self.getString(R.string.shell_i_read_it));
			}
		}

		if (isEmpty(toShow))
			toShow = toSay;

		Wrapper x = new Wrapper(toSay) {
			@Override
			public void onSaid(boolean fAborted) {
				super.onSaid(fAborted);
				Log.d(SmsActivity.TAG, "mark as read");
				if (!fAborted && (fReadMessage || (!instant)))
					App.self.setLastMessageRead(getCurrentSms());
			}
		};

		showSmsAlert(getCurrentSms());

		if (voiced_mode) {
			MyTTS.speakText(new MyTTS.BubblesInMainActivityOnly(x)
					.setShowInNewBubble(true));
			MyTTS.execAfterTheSpeech(new Runnable() {
				@Override
				public void run() {
					if (App.self.isPhoneLocked() || !App.self.isScreenOn()) {
						/*
						 * if (handle_via_main_activity) {
						 * MainActivity.wakeUp(true); }
						 */
						if (!handle_via_main_activity) {
                            doFinish();
							return;
                        }

						// return;
					}
					if (fReadMessage) {
						confirmDesireToReplyAndAfter();
					} else
						confirmReadingOrFinishSingleSmsProcessing(shouldConfirmReading);
				}
			});
		}
	}

	private Timer timer = new Timer();
	private TimerTask listeningTimeoutTask = null;

	boolean in_listening = false;

	private void listen(long timeout, boolean useFreeForm, String lang) {
		if (!handle_via_main_activity) {
			vr = VR.get();
			if (vr == null)
				return;
			vr.open(true);
			in_listening = true;
			VR.useFreeForm = useFreeForm;
			VR.useLanguage = lang;
			vr.start(true);
			if (timeout > 0)
				timer.schedule(listeningTimeoutTask = new TimerTask() {
					@Override
					public void run() {
                        vr = VR.get();
                        if (vr != null)
    						vr.killMicrophone();
						listeningTimeoutTask = null;
						in_listening = false;
					}
				}, timeout);
		} else {
			Intent it = new Intent(MainActivity.SAY_SHOW_AND_LISTEN), rs = new Intent(
					RESULT);
			it.setClass(this, MainActivity.class);
			rs.setClass(this, this.getClass());
			rs.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			it.putExtra(MainActivity.LISTEN, rs);
			it.putExtra(MainActivity.WORK_WITHOUT_QUE, workWithQue);
			if (timeout > 0)
				it.putExtra(MainActivity.LISTEN_TIMEOUT, timeout);
			it.putExtra(MainActivity.LISTEN_USE_FREE_FORM, useFreeForm);
			it.putExtra(MainActivity.LISTEN_LANG, lang);
			startActivity(it);
			in_listening = true;
		}

	}

	private void listenForDictationConfirmation() {
		listen(0, false, null);
	}

	private void listenForConfirmation() {
		listen(TIMEOUT_FOR_CONFIRMATION, false, null);
	}

	private void listenForDictation(String lang) {
		listen(TIMEOUT_FOR_DICTATION, true, lang);
	}

	private long TIMEOUT_FOR_CONFIRMATION = 15000l, TIMEOUT_FOR_DICTATION = 0;

	static final String RESULT = "com.magnifis.parking.RecogtitionResult";

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent it) {
		Log.d(TAG, "onActivityResult");
		switch (requestCode) {
		case MainActivity.TASK_TERMINATE_REQUEST_CODE:
			handleNextOrDie();
			break;
		case VR.VOICE_RECOGNITION_REQUEST_CODE:
			// ignore partial results!
            if (resultCode == Activity.RESULT_FIRST_USER)
                return;
            if (resultCode == VR.RESULT_RUN_INTENT)
                return;

			in_listening = false;
			//hideSmsAlert();
			cancelWaitingForListeningResults();
			Log.d(TAG, "VR_LOOKUP_REQUEST_CODE");
			boolean lAborted = false;

			if (it != null) {
				lAborted = it.getBooleanExtra(MainActivity.LISTENING_ABORTED,
						false);
				if (resultCode == Activity.RESULT_OK) {
					if (!lAborted) {
						
						
						List<String> matches = it
								.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
						if (!isEmpty(matches)) {
							
							mSpinner = new ProgressSpinner(SmsActivity.this);
							mSpinner.show();
							try {	
								switch (mode) {
	                            case MODE_NEW_SMS:
	                            case MODE_CONFIRM_REPLY:
								case MODE_CONFIRM_READING:
								case MODE_CONFIRM_THAT_MESSAGE_IS_RIGHT:
									askForMagnifisYesNo(matches);
									break;
								case MODE_DICTATE_MESSAGE:
									askForMagnifisDictation(matches);
									break;
								} 
							} catch (Throwable e) {
									hideProgress();
									e.printStackTrace();
							}
						
							Log.d(TAG, "We have a voice command");
							return;
						}
					}
				}
			}

			if (mode == MODE_DICTATE_MESSAGE || lAborted)
				handleNextOrDieVerbose();
			else
				handleNextOrDie();

			return;
		}
		super.onActivityResult(requestCode, resultCode, it);
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume " + Utils.isForegroundPackage());
		super.onResume();
		App.self.setActiveActivity(this);
        SuziePopup.hideBubles(true, true);
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause " + Utils.isForegroundPackage());
		super.onPause();
		App.self.removeActiveActivity(this);
	}

	private boolean stopped = false;

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop " + Utils.isForegroundPackage());
		super.onStop();
        if (App.self.isPhoneLocked() || !App.self.isScreenOn())
            return;
		hideSmsAlert();
		synchronized (smsIntents) {
			if (inSmsPocessing) {
				if (smsIntents.size() > 0)
					smsIntents.remove(0);
				if (handle_via_main_activity)
					fireOpes();
				inSmsPocessing = false;
			}
		}
		System.gc();
		App.self.notifyStopActivity(this);
		stopped = true;
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestory " + Utils.isForegroundPackage());
		if (useContentObserver)
			getContentResolver().unregisterContentObserver(observer);
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent " + intent.toString());
		handleIntent(intent, stopped);
		stopped = false;
	}

	private XMLFetcher<Understanding> fetcher = null;

	private void hideProgress() {
		if (mSpinner != null) {
            try {
			    mSpinner.dismiss();
            } catch (Exception e) {}
			mSpinner = null;
		}
	}

	private String dictatedMessage = null;

	private void confirmThatDicationIsRight() {
		MyTTS.speakText(new MyTTS.BubblesInMainActivityOnly(R.string.P_YOU_SAID));
		Output.sayAndShow(this,
				new Wrapper(SendCmdHandler.formatMessageBody(dictatedMessage))
						.setShowInNewBubble(true).setShouldHideBubbles(false));
		MyTTS.speakText(new MyTTS.BubblesInMainActivityOnly(
				R.string.P_is_that_right).setShouldHideBubbles(false));
		MyTTS.execAfterTheSpeech(new Runnable() {
			@Override
			public void run() {
				mode = MODE_CONFIRM_THAT_MESSAGE_IS_RIGHT;
				listenForDictationConfirmation();
			}
		});
	}

	private void askForMagnifisDictation(List<String> ss) {
		askForMagnifisUnderstanding(ss, ClientStateInformer.SN_DICTATE_MSG,
				new UnderstandingHandler() {
					@Override
					public boolean handleUnderstanding(Understanding u) {
						if (u.getCommandCode() == Understanding.CMD_DICTATE) {
							dictatedMessage = u.getMessage();
							if (isEmpty(dictatedMessage)) {
								handleNextOrDie();
							} else
								confirmThatDicationIsRight();
						} else {
							if (handle_via_main_activity) {
								// TODO: forward the understanding
								// to the main packageName

							}
							handleNextOrDieVerbose();
						}
						return true;
					}
				});
	}

	private void askForMagnifisYesNo(List<String> ss) {
		askForMagnifisUnderstanding(ss, ClientStateInformer.SN_YES_NO,
				new UnderstandingHandler() {

					@Override
					public boolean handleUnderstanding(final Understanding u) {
						if (u != null) {
							int cmd = u.getCommandCode();
							if (cmd == Understanding.CMD_READ
									&& Utils.isOneFrom(mode,
                                            MODE_NEW_SMS,
											MODE_CONFIRM_READING,
											MODE_CONFIRM_REPLY)) {
								playConfirmed();
								return true;
							} else if (cmd == Understanding.CMD_REPLY
									&& Utils.isOneFrom(mode,
                                            MODE_NEW_SMS,
											MODE_CONFIRM_READING,
											MODE_CONFIRM_REPLY)) {
								replyConfirmed();
								return true;
							} else if (cmd == Understanding.CMD_YES
									|| cmd == Understanding.CMD_DO_IT) {
								Log.d(TAG, "we have a magnifis response");
								switch (mode) {
                                case MODE_NEW_SMS:
								case MODE_CONFIRM_READING:
									playConfirmed();
									return true;
								case MODE_CONFIRM_REPLY:
									replyConfirmed();
									return true;
								case MODE_CONFIRM_THAT_MESSAGE_IS_RIGHT:
									sendMessageAndAfter();
									return true;
								}
								return true;
							} else if (mode == MODE_CONFIRM_THAT_MESSAGE_IS_RIGHT
									&& cmd == Understanding.CMD_NO) {
								if (u.isCancel())
									handleNextOrDieVerbose();
								else {
									if (++dictation_counter < DICTATION_MAX_COUNTER)
										replyConfirmed();
									else {
										Addressable sender = getCurrentSms()
												.getSender();
										speakText(new MyTTS.BubblesInMainActivityOnly(
												R.string.P_YOU_CAN_SEND_MESSAGE));
										composeTextMessage(dictatedMessage,
												sender.getAddress());
									}
								}
								return true;
							} else {
								boolean cmdNo = cmd == Understanding.CMD_NO;
								if (cmdNo
										|| Utils.isOneFrom(mode,
												MODE_CONFIRM_READING,
												MODE_CONFIRM_REPLY)) {
									boolean verbose = mode != MODE_CONFIRM_READING;
									Runnable b4death = new Runnable() {
										@Override
										public void run() {
											Intent it = new Intent(
													MainActivity.INTERPRET_UNDERSTANDING);
											it.putExtra(
													MainActivity.EXTRA_UNDERSTANDING,
													u);

											if (SmsActivity.this.handle_via_main_activity
													|| !App.self
															.shouldUseSuzie()) {
												it.setClass(App.self,
														MainActivity.class);
												SmsActivity.this
														.startActivity(it);
											} else {
												it.setClass(App.self,
														SuzieService.class);
												SmsActivity.this
														.startService(it);
											}
										}
									};
									handleNextOrDieVerbose(verbose,
											(verbose || cmdNo) ? null : b4death);
								} else {
									MyTTS.speakText(new MyTTS.BubblesInMainActivityOnly(
											R.string.P_YES_OR_NO));
									MyTTS.execAfterTheSpeech(new Runnable() {
										@Override
										public void run() {
											if (mode == MODE_CONFIRM_THAT_MESSAGE_IS_RIGHT)
												listenForDictationConfirmation();
											else
												listenForConfirmation();

										}
									});
								}
							}
						} else
							handleNextOrDie();
						return true;
					}

				});
	}

	private void askForMagnifisUnderstanding(final List<String> ss,
			final String clientState, final UnderstandingHandler uh) {
		
		fetcher = new XMLFetcher<Understanding>() {

				@Override
				protected InputStream invokeRequest(URL u, String pd, String ref, String userAgent)
						throws IOException {
					return super.invokeRequest(RequestFormers
							.createMagnifisUnderstandingRqUrl(null, ss,
									clientState), pd, ref, userAgent);
				}

				@SuppressLint("NewApi")
				protected Understanding consumeXmlData(Element root) {
					if (root != null)
						try {
							return Xml.setPropertiesFrom(root,
									Understanding.class);
						} catch (Throwable e) {
							e.printStackTrace();
							fireOpes();
							doFinish();
						}
					return null;
				}

				@Override
				protected void onCancelled() {
					hideProgress();
					super.onCancelled();
					fireOpes();
					doFinish();
				}

				@Override
				protected void onPostExecute(Understanding u) {
					super.onPostExecute(u);
					hideProgress();
					if (u == null) {
						speakText(
						// "The understanding server has not returned any  XML."
						// "I'm sorry, what was that?"
						R.string.mainactivity_onpostexecute_network_not_connected);
						return;
					}
					uh.handleUnderstanding(u);
					// handleNextOrDie();
				}

			};
	}

	int dictation_counter = 0;

	private void handleNextOrDieVerbose(boolean verbose) {
		handleNextOrDieVerbose(verbose, null);
	}

	private void handleNextOrDieVerbose(boolean verbose, Runnable b4death) {
		if (verbose)
			handleNextOrDieVerbose(b4death);
		else
			handleNextOrDie(b4death);
	}

	private void handleNextOrDieVerbose() {
		handleNextOrDieVerbose(null);
	}

	private void handleNextOrDieVerbose(final Runnable b4death) {
		MyTTS.speakText(new MyTTS.BubblesInMainActivityOnly(
				R.string.P_CANCELLING));
		MyTTS.execAfterTheSpeech(new Runnable() {

			@Override
			public void run() {
				handleNextOrDie(b4death);
			}

		});
	}

	private void composeTextMessage(String smsBody, String phone) {
		Intent it = new Intent();
		it.setAction(Intent.ACTION_SENDTO);
		it.setType("text/plain");

		String advert = App.self.getString(R.string.adv_for_sms);
		if (!App.self.shouldAdvertInSms())
			advert = "";
		if (smsBody != null) {
			if (smsBody.length() + advert.length() < 140)
				smsBody += "\n" + advert;
		} else {
			smsBody = "\n" + advert;
		}
		it.putExtra("sms_body", smsBody);

		StringBuilder sb = new StringBuilder("sms:");
		if (phone != null)
			sb.append(phone);
		it.setData(Uri.parse(sb.toString()));
		startActivityForResult(it, MainActivity.TASK_TERMINATE_REQUEST_CODE);
	}

	private boolean handleNextOrDie() {
		return handleNextOrDie(null);
	}

	private boolean handleNextOrDie(final Runnable b4death) {
		Log.d(TAG, "handleNextOrDie");
		inSmsPocessing = false;
		fireOpes();
		mode = MODE_NEW_SMS;
		synchronized (smsIntents) {
			if (smsIntents.size() > 1) {
				smsIntents.remove(0);
				SmsActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.d(TAG, "handleNextOrDie -- GUI T ");
						dictation_counter = 0;
						dictatedMessage = null;
						startNewSmsProcessing();
					}
				});
				return true;
			} else {
				if (b4death != null)
					b4death.run();
				doFinish();
			}
		}
		return false;
	}

	boolean workWithQue = true;

	void fireOpes() {
		if (lk1) {
			VoiceIO.fireOpes();
		}
		lk1 = false;
		lk0 = false;

	}

	boolean lk0 = false, lk1 = false;

	void runWhenInactive(Runnable r, boolean fromGui) {
		if (handle_via_main_activity && workWithQue) {

			Log.d(TAG, "waiting for lock");

			lk0 = true;
			App.self.voiceIO.getOperationTracker().queOperation(r, fromGui);
			return;
		}
		r.run();
	}

	private boolean inSmsPocessing = false;

	void startNewSmsProcessing() {
		Log.d(TAG, "startNewSmsProcessing");

		runWhenInactive(new Runnable() {
			@Override
			public void run() {
				lk1 = lk0;
				inSmsPocessing = true;
				playSingleSmsAndAfter(true);
			}
		}, true);
	}

	@Override
	public void queTextMessage(Context ctx, final Object o) {
		Output.sayAndShow(ctx, new Wrapper(o) {

			@Override
			public void onSaid(boolean fAborted) {
				super.onSaid(fAborted);
				handleNextOrDie();
			}

		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			MyTTS.abortWithoutUnlock();
			return handleNextOrDie();
		}
		return super.onKeyDown(keyCode, event);
	}

	MagToast currentAlert = null;

	void hideSmsAlert() {
		MagToast mt = currentAlert;
		if (mt != null) {
			mt.hide();
			currentAlert = null;
		}
	}

	MagToast showSmsAlert(final Message m) {
		Log.d(TAG, "showSmsAlert 0");
		final MagToast tc[] = { null };
        if (m == null)
            return tc[0];
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "showSmsAlert 1");
				SmsAlertView pv = App.self.createFromLayout(R.layout.sms_alert);
				if (pv != null)
				pv.setData(m,
				// on mic
				// shouldHandleInSilentMode()
				// ?
						new Runnable() {
							@Override
							public void run() {
								hideSmsAlert();
								MyTTS.abortWithoutUnlock();
								replyConfirmed();
							}
						}
						// :null
						,
						// on Text
						new Runnable() {
							@Override
							public void run() {
								hideSmsAlert();

								MyTTS.abortWithoutUnlock();
								Addressable ad = m.getSender();
								composeTextMessage(dictatedMessage,
										ad == null ? null : ad.getAddress());

								/*
								 * try { Intent it=new Intent(ACTION_COMPOSER);
								 * it.setClass(App.self, SmsActivity.class);
								 * startActivity(it); } catch(Throwable t) {}
								 */
							}
						},
						// on close
						new Runnable() {
							@Override
							public void run() {
								hideSmsAlert();
								MyTTS.abortWithoutUnlock();
								handleNextOrDie();
							}
						});
				ToastBase.LayoutOptions lo = new ToastBase.LayoutOptions();
				lo.contentPadding = new Rect(0, 40, 0, 0);
				tc[0] = new MagToast(pv, lo, null, false);
				tc[0].show();
			}
		});
		return (currentAlert = tc[0]);
	}

	private void doFinish() {
		Log.d(TAG, "doFinish");
		inSmsPocessing = false;
		if (SuziePopup.get() != null)
			SuziePopup.get().enableBubles();
		if (handle_via_main_activity)
			MainActivity.cancelListeningFor();
		finish();
	}

	@Override
	public boolean isRequiringSuzie() {
		return !handle_via_main_activity;
	}
}
