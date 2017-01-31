package com.magnifis.parking;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.SearchRecentSuggestions;
import android.speech.RecognizerIntent;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.impl.DownloaderService;
import com.magnifis.parking.MultipleEventHandler.EventSource;
import com.magnifis.parking.SmsActivity.Wrapper;
import com.magnifis.parking.UserLocationProvider.LocationInfo;
import com.magnifis.parking.bubbles.IBubbleContent;
import com.magnifis.parking.bubbles.IRepeat;
import com.magnifis.parking.cmd.etc.CmdHandlerHolder;
import com.magnifis.parking.cmd.i.ActivityResultHandler;
import com.magnifis.parking.cmd.i.IHandlesAbortInAnyCase;
import com.magnifis.parking.cmd.i.IIntentHandler;
import com.magnifis.parking.cmd.i.IOptionsListViewHolder;
import com.magnifis.parking.cmd.i.LocalCommandHandler;
import com.magnifis.parking.cmd.i.OnBeforeListeningHandler;
import com.magnifis.parking.cmd.i.OnListeningAbortedHandler;
import com.magnifis.parking.cmd.i.OnResumeHandler;
import com.magnifis.parking.demo.ScriptedSequence;
import com.magnifis.parking.fb.FbHelper;
import com.magnifis.parking.feed.IFeed;
import com.magnifis.parking.feed.MailFeedController;
import com.magnifis.parking.feed.MessageFeedController;
import com.magnifis.parking.feed.SmsFeedController;
import com.magnifis.parking.model.CmdAlias;
import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.GasReply;
import com.magnifis.parking.model.GasStation;
import com.magnifis.parking.model.GeoObject;
import com.magnifis.parking.model.GooWeather;
import com.magnifis.parking.model.LearnedAnswer;
import com.magnifis.parking.model.Origin;
import com.magnifis.parking.model.SaidPhrase;
import com.magnifis.parking.model.Script;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.model.UnderstandingStatus;
import com.magnifis.parking.phonebook.PhoneBook;
import com.magnifis.parking.suzie.SuziePopup;
import com.magnifis.parking.traffic.EtaMonitor;
import com.magnifis.parking.traffic.RoutePathOverlay;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.twitter.TwitterPlayer;
import com.magnifis.parking.utils.AndroidJsBridge;
import com.magnifis.parking.utils.ImageFetcher;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.utils.js.*;
import com.magnifis.parking.utils.nlp.StopWordsPredicate;
import com.magnifis.parking.views.DecoratedListView;
import com.magnifis.parking.views.Details;
import com.magnifis.parking.views.ProgressSpinner;
import com.magnifis.parking.views.TheMapView;
import com.magnifis.parking.views.TheWebView;
import com.magnifis.parking.views.WeatherView;
import com.magnifis.parking.widgets.WidgetProvider;
import com.robinlabs.persona.AppNames;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.magnifis.parking.Launchers._startNestedActivity;
import static com.magnifis.parking.Launchers.dial;
import static com.magnifis.parking.Launchers.isWazeInstalled;
import static com.magnifis.parking.Launchers.launchGpsNavigator;
import static com.magnifis.parking.Launchers.lookAtMarketFor;
import static com.magnifis.parking.Launchers.shareRobin;
import static com.magnifis.parking.Launchers.startNestedActivity;
import static com.magnifis.parking.Phrases.pickCannotCallPhrase;
import static com.magnifis.parking.Phrases.pickLaunchNavPhrase;
import static com.magnifis.parking.Phrases.pickNoParkingPhrase;
import static com.magnifis.parking.RequestFormers.createMagnifisUnderstandingRqUrl;
import static com.magnifis.parking.VoiceIO.condListenAfterTheSpeech;
import static com.magnifis.parking.VoiceIO.fireOpes;
import static com.magnifis.parking.VoiceIO.listenAfterTheSpeech;
import static com.magnifis.parking.VoiceIO.sayAndShowFromGui;
import static com.magnifis.parking.VoiceIO.sayFromGui;
import static com.magnifis.parking.tts.MyTTS.speakText;
import static com.magnifis.parking.utils.Utils.askYesOrContinue;
import static com.magnifis.parking.utils.Utils.contextTheme;
import static com.magnifis.parking.utils.Utils.firstUpper;
import static com.magnifis.parking.utils.Utils.hideSomething;
import static com.magnifis.parking.utils.Utils.isEmpty;
import static com.magnifis.parking.utils.Utils.isExternalStorageAvailable;
import static com.magnifis.parking.utils.Utils.showSomething;
import static com.magnifis.parking.utils.Utils.smart_droid_rm;
import static com.magnifis.parking.utils.Utils.trim;

public class MainActivity extends MapActivity implements
		SharedPreferences.OnSharedPreferenceChangeListener,
		IFeedContollerHolder, ProgressIndicatorHolder,
		AborterHolder,
		IOptionsListViewHolder,
		HandWavingClient
{

	public void onNetworkCommunicationError() {
		if (mainView != null)
			mainView.post(new Runnable() {

				@Override
				public void run() {
					if (micAnimator != null)
						micAnimator.showError();
				}
			});
	}

	static String app_getString(int key) {
		return App.self.getString(key);
	}

	public String getMicPrompt() {
		Advance adv = App.self.voiceIO.getAdvance();
		return (adv == null) ? null : adv.getPrompt();
	}

	protected boolean bgOrSplashIsSet() {
		if (anyWelcome())
            return true;

        return Utils.anyBgBitmap(mainView);
	}

	protected void setMainViewBg() {
	   Log.d(TAG, "setMainViewBg");

	}

	protected void afterRotation(boolean actuallyChanged) {

		Log.d(TAG, "afterRotation()");

		if (bottomPanel == null || logoBar == null
				|| selfWr == null)
			return;
		/*
		 * RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(
		 * LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
		 * lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		 */
		if (actuallyChanged||!bgOrSplashIsSet())
            setMainViewBg();

        if (anyWelcome())
            mainView.setVisibility(View.GONE);
        else
            mainView.setVisibility(View.VISIBLE);

        bottomPanel.setVisibility(View.VISIBLE);

		bublesUpdateRotation();

		// logoBar.setLayoutParams(lp);

		if (mv != null)
			mv.recalculateBounds();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
		boolean f = App.self.isInLanscapeMode();
		if (f != inLandscape) {
			inLandscape = f;
			afterRotation(true);
		}
	}

	static final String TAG = MainActivity.class.getSimpleName();

	public static final String WAKE_UP = "com.magnifis.parking.WAKE_UP";
	public static final String OPEN_MAP = "com.magnifis.parking.OPEN_MAP";
    public static final String SHUT_DOWN = "com.magnifis.parking.SHUT_DOWN";
    public static final String TYPE = "com.magnifis.parking.TYPE";
    public static final String SHARE = "com.magnifis.parking.SHARE";
    public static final String START_TEACH = "com.magnifis.parking.START_TEACH";
	public static final String INTERPRET_UNDERSTANDING = "com.magnifis.parking.INTERPRET_UNDERSTANDING";
    public static final String START_PAYMENT = "com.magnifis.parking.START_PAYMENT";
    public static final String SHOW_TRAFFIC = "com.magnifis.parking.SHOW_TRAFFIC";

	public static final String EXTRA_UNDERSTANDING = "EXTRA_UNDERSTANDING";
	public static final String EXTRA_FOLLOWUP_REQUEST = "EXTRA_FOLLOWUP_REQUEST";
	static final String EXTRA_REQUEST_SEARCH = "EXTRA_REQUEST_SEARCH";

	public static final String VR_RESULTS = "com.magnifis.parking.VR_RESULTS";
	static final String FROM_CLEAN = "com.magnifis.parking.FROM_CLEAN";

	static final String SAY_SHOW_AND_LISTEN = "com.magnifis.parking.SAY_SHOW_AND_LISTEN";
	/**********
	 *  work as a slave for an external packageName
	 *  there are the extras:
	 *
	 *  "say"     -   string to say
	 *  "show"    -   string to show
	 *  "listen"  -   an intent to update and to send after the listening
	 *
	 */

	static final String
	   LISTENING_ABORTED="LISTENING_ABORTED",
	   LISTENING_RESULT="LISTENING_RESULT",
	   LISTENING_RESULT_CODE="LISTENING_RESULT_CODE",
	   HANDLE_VIA_MAIN_ACTIVITY="HANDLE_VIA_MAIN_ACTIVITY",
	   SAY="SAY", SHOW="SHOW", LISTEN="LISTEN", LISTEN_TIMEOUT="LISTEN_TIMEOUT",
	   LISTEN_LANG="LISTEN_LANG", LISTEN_USE_FREE_FORM="LISTEN_USE_FREE_FORM",
	   WORK_WITHOUT_QUE="WORK_WITHOUT_QUE", DONT_LISTEN="DONT_LISTEN" ,
	   STOP_LISTENING_FOR="STOP_LISTENING_FOR"
	 ;

	private Intent intentToForwardListeningResult=null;


	public static final String EXPANSION_DOWNLOADER_NOTIFICATION = "com.magnifis.parking.EXPANSION_DOWNLOADER_NOTIFICATION";
	static final String EXPANSION_DOWNLOADER_FAILURE = "com.magnifis.parking.EXPANSION_DOWNLOADER_FAILURE";
	static final String FIRST_EXECUTION = "firstExecution",
			FIRST_HELP = "firstHelp",
			FIRST_NAVIGATION_EXECUTION = "firstNavigationExecution";
	static final String COUNT_EXECUTION = "counttExecution_";
	static final String RATE = "review", SCMD_INIT = "initialization";
	static final String SHORTCUT = "askForShortcut";
	static final int secondsWaitToKillSendCommand = 60000;

	volatile TheMapView mv;
	volatile MapController mapController;
	volatile UserLocationProvider mlo = null;
	volatile PksOverlay pksOverlay;
	volatile PksController pksController;
    volatile PlacemarkOverlay markController;
    volatile PoisOverlay poisOverlay;
	volatile PoisController poisController;
	volatile GasOverlay gasOverlay;
	volatile GasController gasController;
	volatile RoutePathOverlay[] routeOverlays;
	volatile public SmsFeedController smsController;
	volatile MailFeedController mailController;
    AlertDialog alertClose = null;
	volatile MapItemSetContoller currentController;

	ProgressBar progressBar;

    View btnListen, btnListenL, bottomPanel = null,
            logoBar = null, vwScreen = null, micAnimationView = null,
            micAnimationViewL = null, micAnimationBgView = null,
            talkCircle =null,
            micAnimationBgViewL = null;

    ImageView locationLogo;
	ImageView parkingProviderLogo, yelpLogo, googleLogo;
	TheWebView webView = null;
	TheWebView jsContainerVW = null;

	private DecoratedListView optionsListView = null;

	public MainActivity() {
		selfWr = new WeakReference<MainActivity>(this);
		smsController = SmsFeedController.getInstance();
		mailController = new MailFeedController(this);
	}

	public void hideOptionsListView() {
		if (optionsListView != null) {
			optionsListView.setVisibility(View.GONE);
			ViewGroup vg = (ViewGroup) optionsListView.getParent();
			vg.removeView(optionsListView);
			optionsListView = null;
			vwScreen.setVisibility(View.GONE);
		}
	}

	public DecoratedListView getOptionsListView() {
		if (optionsListView == null) {
			DecoratedListView lv = new DecoratedListView(this);
			lv.setBackgroundColor(Color.WHITE);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			lp.addRule(RelativeLayout.ABOVE, R.id.BottomPanel);
			lv.setLayoutParams(lp);

			// ViewGroup vg=(ViewGroup)progressBar.getParent();

			int ix = /* mainView.indexOfChild(vwScreen)+1 */mainView
					.indexOfChild(progressBar);
			mainView.addView(lv, ix);
			optionsListView = lv;
		}
		vwScreen.setVisibility(View.VISIBLE);
		return optionsListView;
	}

	public PhoneBook phoneBook;
	public FbHelper fbHelper;

	public static void tweet(final String s, final DoublePoint loc,
			final Runnable afterThat, final Runnable geoDisabled) {
		get().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				get().twitterPlayer.tweet(s, loc, afterThat, geoDisabled);
			}
		});
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	class ProgressBarStopper extends MultipleEventHandler {
		public boolean isInactive() {
			synchronized (this) {
				if (inactiveSince != null
						&& Math.abs(inactiveSince - System.currentTimeMillis()) > 700)
					return true;
			}
			return false;
		}

		Long inactiveSince = null;

		@Override
		protected void onCompletion() {
			// progressBar.setVisibility(View.GONE);
			/*
			Throwable t = new Throwable();
			t.fillInStackTrace();
			Log.d("black", "FINISH", t);
			*/

			pic.hide();

			btnListen.setEnabled(true);
			btnListenL.setEnabled(true);

			synchronized (this) {
				inactiveSince = System.currentTimeMillis();
			}
		}

		public EventSource newEventSource() {
			synchronized (this) {
				inactiveSince = null;
				if (counter == 0) {
					// progressBar.setVisibility(View.VISIBLE);
					/*
					Throwable t = new Throwable();
					t.fillInStackTrace();
					Log.d("black", "START", t);
					Log.d("black", "MainActivity="+this+" pic="+pic+" counter="+counter+" btnListen="+btnListen);
					*/


					if (pic != null)
						pic.show();

					if (btnListen != null)
						btnListen.setEnabled(false);
					if (btnListenL != null)
						btnListenL.setEnabled(false);

				}
				++counter;
				return new EventSource();
			}
		}

	};

	protected ProgressBarStopper progressBarStopper = new ProgressBarStopper();

	ProgressSpinner mSpinner = null;

	public MultipleEventHandler.EventSource showProgress() {
		Log.d(TAG, "showProgress");
		return progressBarStopper.newEventSource();
	}

	public void setDefaultZoom() {
		initMap();
		mapController.setZoom(MapItemSetContoller.DEFAULT_ZOOM);
	}

	View shownView = null;

	public void showDetails(Details details, GeoObject fas) {
		if (shownView == null) {
			shownView = details;
			if (mv != null)
				mv.dontDiplayZoom = true;

			View beforeView = (View) mainView.findViewById(R.id.webPlace);
			mainView.addView(shownView, mainView.indexOfChild(beforeView));

			details.setData(fas);
			showSomething(details);
		}
	}

	public void showWeather(final GooWeather w) {

		final WeatherView layout = new WeatherView(this);

		layout.setData(w);

    	ViewGroup dialogs = getMainTabDialogView();
        dialogs.addView(layout);

        getMainTabAttr().bubleLastShown = 0;

    	showBubles();
	}

	LinearLayout bubleMessage = null;

	public void showMessage(final SuziePopup.BubleMessage bm) {

    	final ViewGroup dialogs = getMainTabDialogView(true);
        dialogs.post(new Runnable() {

			@Override
			public void run() {

		    	// ***************************************************************************
				// finish message
				if (bm == null) {
					if (bubleMessage == null)
						return;

					EditText te = (EditText) bubleMessage.findViewById(R.id.textEditText);
			    	te.clearFocus();
			    	te.setCursorVisible(false);
			    	te.setFocusableInTouchMode(false);
			    	te.setFocusable(false);
			    	te.setClickable(false);
			    	te.setEnabled(false);

			    	Button b = (Button) bubleMessage.findViewById(R.id.buttonSend);
			    	b.setOnClickListener(null);
			    	b.setBackgroundResource(R.drawable.sms_send_none);

					View c = bubleMessage.findViewById(R.id.buttonSmsCancel);
					c.setOnClickListener(null);
			    	c.setBackgroundResource(R.drawable.messagebubble_middle_fill);

					bubleMessage = null;
					return;
				}

		    	// ***************************************************************************
				// create new message
				if (bubleMessage == null) {
					bubleMessage = new LinearLayout(MainActivity.this);

					bubleMessage.setPadding(20, 20, 20, 20);

					LayoutInflater inflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
					inflater.inflate(R.layout.suzie_buble_message, bubleMessage);

					bubleMessage.findViewById(R.id.buttonSmsCancel).setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							abortOperation(0);
						}

					});

			        dialogs.addView(bubleMessage);
				}

		    	// ****************************************************************************
				// fill message
				TextView text_recipient = (TextView) bubleMessage.findViewById(R.id.textViewRecipientName);
		    	text_recipient.setText(bm.name);

				TextView text_addr = (TextView) bubleMessage.findViewById(R.id.textViewRecipientAddr);
		    	text_addr.setText(bm.addr);

		    	final EditText ta = (EditText) bubleMessage.findViewById(R.id.textAnswerText);
	    		ta.setText(bm.answer);
		    	final View v = bubleMessage.findViewById(R.id.acrollAnswer);
		    	if (bm.answer.length() > 0)
		    		v.setVisibility(View.VISIBLE);
		    	else
		    		v.setVisibility(View.GONE);

		    	final EditText te = (EditText) bubleMessage.findViewById(R.id.textEditText);
		    	te.setText(bm.text);
		    	te.setCursorVisible(true);
		    	te.setSelected(false);
		    	te.requestFocus();
		    	te.setOnClickListener(bm.onEditClick);
		    	te.setOnTouchListener(bm.onEditTouch);
		    	te.setLongClickable(false);
		    	te.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
					}

					@Override
					public void afterTextChanged(Editable s) {
						bm.onEditChanged.onClick(te);
					}
				});
		    	te.setSelection(bm.sel_start, bm.sel_end);

	    		ImageView image = (ImageView) bubleMessage.findViewById(R.id.user);
	    		if (bm.icon == null)
	    			image.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
	    		else
	    			image.setImageDrawable(bm.icon);

		    	//TextView text_hint = (TextView) bubleMessage.findViewById(R.id.textViewHint);
		    	//text_hint.setText(bm.hint);

		    	bubleMessage.findViewById(R.id.buttonSend).setOnClickListener(bm.onSendButtonClick);
		    	bubleMessage.findViewById(R.id.buttonKeyboard).setOnClickListener(bm.onKeyboardButtonClick);
		    	bubleMessage.findViewById(R.id.buttonSmsCancel).setOnClickListener(bm.onCancelButtonClick);

		    	// ****************************************************************************
		    	// scroll window
		        getMainTabAttr().bubleLastShown = 0;

		    	showBubles();
			}
		});
	}

	public void postDelayed(Runnable r, long timeout) {
		mainView.postDelayed(r, timeout);
	}

	boolean _somethingInHidding = false;

	public void hideShownView() {
		if (shownView == null)
			return;
		final View something = shownView;
		shownView = null;
		if (!_somethingInHidding && (something.getVisibility() == View.VISIBLE)) {
			_somethingInHidding = true;
			hideSomething(something, new Runnable() {
				public void run() {
					if (something.getTag(R.string.remove_on_hiding) != null) {
						mainView.postDelayed(new Runnable() {
							public void run() {
								ViewGroup vg = (ViewGroup) something
										.getParent();
								vg.removeView(something);
							}
						}, 5);
						// vg.removeView(something);
					}
					_somethingInHidding = false;
				}
			});
		}
		if (mv != null)
			mv.dontDiplayZoom = false;
	}

	private static WeakReference<MainActivity> selfWr = null;

	private static MainActivity _get() {
		return (selfWr == null) ? null : selfWr.get();
	}

	public static MainActivity get() {
		MainActivity ma = _get();
		return (ma == null || ma.killed) ? null : ma;
	}

	public static MainActivity getActive() {
		MainActivity ma = get();
		return (ma == null || ma.paused) ? null : ma;
	}

	Props props;
	SharedPreferences prefs;
	TwitterPlayer twitterPlayer = null;

	public TwitterPlayer getTwitterPlayer() {
		return twitterPlayer;
	}

	protected boolean inLandscape=App.self.isInLanscapeMode();

	void resetIntent() {
		lastIntent = new Intent(WAKE_UP);
		setIntent(lastIntent);
	}

	private WeakReference<Runnable> aborter = null;

	public void setAborter(Runnable abo) {
		aborter = new WeakReference<Runnable>(abo);
	}

	@Override
	public boolean abortOperation(int flags) {
		return abortOperation(false, flags, true);
	}

	boolean abortOperation(boolean fByMenuButton, int flags, boolean fAbortTTS) {
		boolean ok = false;
		App.self.voiceIO.setAdvance(null);
		System.gc();
		if (aborter != null) {
			Runnable abo = aborter.get();
			if (ok = (abo != null)) {
				abo.run();
				aborter = null;
			}
		}
		boolean ok2 = fAbortTTS?MyTTS.abort(new Runnable() {

			@Override
			public void run() {
				fireOpes();
			}

		}, fByMenuButton):false;
		VR vr=VR.get();
		if (vr!=null) abortVR();

		if (ok2) flags|=Abortable.TTS_ABORTED;

		if (((flags&Abortable.BY_MA_BUBBLE_CLICK)!=0)||!(ok||ok2)) {

			Abortable ab = CmdHandlerHolder.getAbortableCommandHandler();
			if (ab != null&&!fByMenuButton) {
				if (((flags&Abortable.BY_MA_BUBBLE_CLICK)!=0)||!(ok||ok2)||(ab instanceof IHandlesAbortInAnyCase)) {
				   ab.abort(flags);
				   return true;
				}
			}

		}



		return ok || ok2;
	}

	/***
	 *replaced with App.self.getStatusBarHeight();
	 */
	public int getTraySize() {
		Rect rectgle = new Rect();
		Window window = getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rectgle);

		return Math.abs(rectgle.top);
	}
	/*****/

	MicAnimatorMag micAnimator = null;
	
	// Storage Permissions variables
	private static final int REQUEST_EXTERNAL_STORAGE = 1;
	private static String[] PERMISSIONS_STORAGE = {
			"android.permission.READ_EXTERNAL_STORAGE",
	        "android.permission.WRITE_EXTERNAL_STORAGE"
	};

	//persmission method.
	private void verifyStoragePermissions() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	    // Check if we have read or write permission
		
		Method csp=getClass().getMethod("checkSelfPermission",  String.class),
			   rqp=getClass().getMethod("requestPermissions",  String [].class, int.class);
		 
	    int writePermission = (Integer)csp.invoke(this, "android.permission.WRITE_EXTERNAL_STORAGE"); 
	    		//activity.checkSelfPermission(  "android.permission.WRITE_EXTERNAL_STORAGE");
	    int readPermission =  (Integer)csp.invoke(this, "android.permission.READ_EXTERNAL_STORAGE"); 
	    		//activity.checkSelfPermission( "android.permission.READ_EXTERNAL_STORAGE");

	    if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
	        // We don't have permission so prompt the user
	    	rqp.invoke(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
	    	//activity.requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
	    }
	}

	/** Called when the packageName is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate ");
		if (Utils.isAndroid6orAbove)
			try {
				verifyStoragePermissions();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		App.self.setActiveActivity(this);

		// hideShowWidget(true);
		
		if (App.self.robin().isDebugMode()) {
			Log.d(TAG, "Debug mode: publishing app names ");
			AppNames.getInstance().publish(this);
		}
		
        inLandscape = App.self.isInLanscapeMode();


		if (FROM_CLEAN.equals(getIntent().getAction()))
			resetIntent();
		else
			lastIntent = getIntent();

		phoneBook = PhoneBook.getInstance();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);


		props = App.self.getProps();

		prefs = App.self.getPrefs();
		/*
		if (!Utils.isBooleanPrefSet(Consts.PR.speakCallerName)&&App.self.getCountExecution()==0) {
		  // first execution after installation
		  // if this is a first execution a/installation switch off incoming call notification
	      Editor ed=prefs.edit();
	      ed.putBoolean(Consts.PR.speakCallerName, false);
	      ed.commit();
		}*/

		mSpinner = new ProgressSpinner(this) {
			@Override
			public void onBackPressed() {
				if (MainActivity.this.voicesInstallerWorks
						|| App.self.expDownloadingInProgress()) {
					MainActivity.this.finish();
				} else
					abortOperation(0);
				super.onBackPressed();
			}
		};

		handlePreferences(prefs, false, null);

		// /////
		final View bg = new View(this);
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);

		if (anyWelcome() || App.self.expDownloadingInProgress()) {
			//bg.setBackgroundResource(inLandscape ? R.drawable.robin_splash_landscape
			//		: R.drawable.splash);

		} else
			bg.setBackgroundColor(getResources().getColor(R.color.SplashBgColor));

		bg.setLayoutParams(lp);

		micAnimator = new MicAnimatorMag();
		vr = VR.create(this, micAnimator, false, Intent.ACTION_VOICE_COMMAND.equals(lastIntent.getAction()));

		setContentView(bg);

		bg.postDelayed(
		  new Runnable() {
			  @Override
			 public void run() {
		       setActualMainView();
			 }
		  },
		  3
		);

		mlo = new UserLocationProvider(this);

		fbHelper = FbHelper.getInstance();

        final AlertDialog.Builder builder1 = new AlertDialog.Builder(contextTheme(this));
        builder1.setTitle(R.string.app_exit_confirm);
        builder1.setMessage(getString(R.string.shutdown_alert));
        builder1.setCancelable(true);
        builder1.setNeutralButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        alertClose.dismiss();
                    }
                }
        );
        builder1.setNegativeButton("Shut Down", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Robin.shutdown();
            }
        });
        builder1.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Robin.shutdown();
            }
        });
        alertClose = builder1.create();

		Log.d(TAG, "onCreate -- end");

        Communitainment.setCommunitainmentAlarm(this);
    }


	// subcommand aliases
	private ArrayList<CmdAlias> aliasList = new ArrayList<CmdAlias>();

	public void clearAliases() {

		if (aliasList != null)
			aliasList.clear();

		// TODO: clear DB as well
	}

	public void addAlias(CmdAlias alias) {

		if (aliasList != null)
			aliasList.add(alias);

		// @TODO: add DB saving
	}

	private VR vr;

	public VR getVR() {
		return vr;
	}

	public void setPic(ProgressIndicatorController pic) {
		this.pic = pic;
	}

    RelativeLayout mainView = null;

	ProgressIndicatorController pic = new PrIdMag();

	boolean initMap() {
		Log.d(TAG, "initMap()");
		if (mv == null) {
			TheMapView mapView = new TheMapView(
					this,
					getString(App.self.isReleaseBuild ? R.string.google_map_key_release
							: R.string.google_map_key));

			mapView.setClickable(true);
			mapView.setEnabled(true);
			// //
            mainView.setClipChildren(false);

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

            if ((android.os.Build.VERSION.SDK_INT >= 11) && Config.rotate_map) {
                int px=0, py=0;
                DisplayMetrics metrics = new DisplayMetrics();
                WindowManager wm = (WindowManager) App.self.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                wm.getDefaultDisplay().getMetrics(metrics);
                int w = metrics.widthPixels;
                int h = metrics.heightPixels;
                int m = (int)Math.round(Math.sqrt(w*w+h*h));
                lp = new RelativeLayout.LayoutParams(m, m);
                int marginLeft = (m-w)/2;
                int marginTop = (m-h)/2;
                //int margin = Math.max(marginLeft, marginTop);
                //lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                lp.setMargins(-marginLeft, -marginTop, -marginLeft, -marginTop);

                mapView.setMyPadding(marginLeft, marginTop);
                //mapView.setMyPadding(Math.abs(marginLeft-marginTop));
                /*
                if (marginLeft > marginTop)
                    mapView.setPadding(0, Math.abs(marginLeft-marginTop), 0, 0);
                else
                    mapView.setPadding(Math.abs(marginLeft-marginTop), 0, 0, 0);
                    */
            }

			mainView.addView(mapView, 1, lp);
            mapView.setLayoutParams(lp);

			afterRotation(false);
			mlo.bind(mapView);

            markController = new PlacemarkOverlay();

			pksController = new PksController();
			currentController = pksController;

			poisController = new PoisController();

			pksOverlay = new PksOverlay(pksController, mapView); // new
															// ParkingsOverlay();
			pksOverlay.doPopulate();

			poisOverlay = new PoisOverlay(poisController, mapView);
			poisOverlay.doPopulate();

			gasController = new GasController();
			gasOverlay = new GasOverlay(gasController, mapView);
			gasOverlay.doPopulate();

            mapView.getOverlays().add(markController);
			mapView.getOverlays().add(mlo.getOverlay());
			mapView.getOverlays().add(pksOverlay);
			mapView.getOverlays().add(poisOverlay);
			mapView.getOverlays().add(gasOverlay);

			mapController = mapView.getController();
			mapView.setBuiltInZoomControls(zoomcontrols);
			// mv.setTraffic(true);
			// mv.setStreetView(true);
			mapController.setZoom(17);

            if (mlo != null)
                mlo.enableMyLocation();

            this.mv=mapView;

            if (mv!=null)
                showMap();

			return true;
		}
		if (mv!=null)
            showMap();

		return false;
	}

	private void setActualMainView() {

		Log.d(TAG, "setActualMainView");

		try {
			System.gc();
			setContentView(R.layout.main);
		} catch (Throwable t) {
			t.printStackTrace();
			finish();
			return;
		}

		Log.d(TAG, "setActualMainView -- content view replaced");

		mainView = (RelativeLayout) findViewById(R.id.mainView);

		vwScreen = findViewById(R.id.wvScreen);

        LayoutInflater infl = LayoutInflater.from(this);

        bottomPanel = infl.inflate(R.layout.bottom_panel_universal, mainView, false);

        View beforeView = (View) mainView.findViewById(R.id.ProgressBar);
		mainView.addView(bottomPanel, mainView.indexOfChild(beforeView));

		// if (anyWelcome()) initMap(); never preload the map

		logoBar = findViewById(R.id.LogoBar);

		micAnimationView = findViewById(R.id.MicAnimation);
		micAnimationBgView = findViewById(R.id.MicAnimationBg);
		micAnimationViewL = null;//findViewById(R.id.MicAnimationL);
		micAnimationBgViewL = null;//findViewById(R.id.MicAnimationBgL);

		if (!checkDiskAvailability())
			return;

		btnListen = findViewById(R.id.BtnListen);
        btnListenL = btnListen;

		afterRotation(false);

		parkingProviderLogo = (ImageView) findViewById(R.id.ParkingProviderLogo);
		yelpLogo = (ImageView) findViewById(R.id.YelpLogo);
		googleLogo = (ImageView) findViewById(R.id.GoogleLogo);
        locationLogo = (ImageView) findViewById(R.id.BtnMyLocation);

		progressBar = (ProgressBar) findViewById(R.id.ProgressBar);

		prepareLogos();
		new SettingsFetcher();
		
        initTabs();

		whenAllVoicesInstalled();

	}
	
	private TabWidget tabWidget=null;
	private FrameLayout tabContent=null;
	private TabHost tabHost=null;
	
	public void  setCurrentTab(String tabId) {
		Integer ti=findTabIndexById(tabId);
		if (ti!=null) tabHost.setCurrentTab(ti);
	}
	
	public Integer findTabIndexById(String tabId) {
		if (tabContent!=null)
			for (int index = 0; index < tabContent.getChildCount(); index++) {
				View  contentView =  tabContent.getChildAt(index);
				if (tabId.equals(contentView.getTag())) return index;
			}
		return null;
	}
	
	public ScrollView getMainTab() {
		return findTabById("#main");
	}
	
	public ViewGroup getMainTabDialogView() {
		return getMainTabDialogView(false);
	}
	
	public ViewGroup getTabDialogView(String tabId, boolean fSelect) {
		try {
			return (ViewGroup)findTabById(tabId).findViewById(R.string.DialogView);
		} catch(Throwable t) {}
		return null;
	}
	
	public ViewGroup getMainTabDialogView(boolean fSelect) {
		return getTabDialogView("#main", fSelect);
	}
	
	public TabContent getMainTabAttr() {
		return getTabAttr("#main");
	}
	
	public TabContent getTabAttr(String tabId) {
		ScrollView sv=findTabById(tabId);	
		if (sv!=null) return (TabContent)sv.getTag(R.string.tag_tab_attr);
		return null;
	}
	
	public ScrollView findTabById(String tabId) {
		return findTabById(tabId, false);
	}
	
	public ScrollView findTabById(String tabId, boolean fSelect) {
		if (tabContent!=null)
			for (int index = 0; index < tabContent.getChildCount(); index++) {
				View  contentView =  tabContent.getChildAt(index);
				if (tabId.equals(contentView.getTag())) return (ScrollView)contentView;
			}		
		

		return null;
	}
	
	static class TabContent {
	   public int bubleLastShown = 0; // 1 - query, 2 - answer, 3 - partial
	   public TextView bubleLastQueryTextView = null;
	   public TextView bubleLastAnswerTextView = null;	
	};
		
	public Integer addTab(final String tabId, String caption) {
		if (tabHost==null) return null;
		Integer ti=findTabIndexById(tabId);
		if (ti==null) {

			TabSpec tabSpec = tabHost.newTabSpec(tabId);
			
			TabContentFactory tcf=new TabContentFactory() {
				@Override
				public View createTabContent(String tag) {
				   View  v=findTabById(tag);
				   if (v!=null) return v;
					
	               ScrollView sv=new ScrollView(getBaseContext());
	               sv.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT));
	               sv.setId(R.string.DialogScroll);
	               sv.setTag(tabId);
	               sv.setTag(R.string.tag_tab_attr, new TabContent());
	               
	               LinearLayout dv=new LinearLayout(getBaseContext());
	               dv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT) );
	               dv.setId(R.string.DialogView);
	               dv.setPadding(0, App.self.toPxSize(4), 0, 0);
	               dv.setScrollBarStyle(LinearLayout.SCROLLBARS_INSIDE_OVERLAY);
	               dv.setVerticalScrollBarEnabled(true);
	               dv.setHorizontalScrollBarEnabled(false);
	               dv.setOrientation(LinearLayout.VERTICAL);
	               /*
	                                            android:orientation="vertical"
                            android:overScrollMode="always"
                            android:paddingTop="4dp"
                            android:scrollbarStyle="insideOverlay"
                            android:scrollbars="vertical" >
	                */
	               
	               
	               sv.addView(dv);
					/*
	               TabAttribute ta=new TabAttribute();
	               ta.id=tabId;
	               sv.setTag(R.string.tag_tab_attr, ta);
	               */
	               return sv;
				}
				
				
			};	
			tabSpec.setContent(tcf);
		    tabSpec.setIndicator(caption);
			tabHost.addTab(tabSpec);	
		    
		    ti=findTabIndexById(tabId);
		    if (ti==null) {
		      View v=tcf.createTabContent(tabId);
		      tabContent.addView(v);
		    }

			ti=findTabIndexById(tabId);
			
			if (tabWidget.getTabCount()>1)  findViewById(android.R.id.tabs).setVisibility(View.VISIBLE);
		}
		tabHost.setCurrentTab(ti);
		return ti;
	}
	
	public String getActiveTabId() {
		if (tabHost!=null&&tabWidget.getTabCount()>0) try {
			return tabHost.getCurrentTabTag();
		} catch (Throwable t) {}
		return null;
	}
	
	void initTabs() {
		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();
		
/*		
		tabHost.setOnTabChangedListener(
			new OnTabChangeListener() {

				@Override
				public void onTabChanged(String tabId) {
					// TODO Auto-generated method stub
					
				}
				
			}
		);
	*/	
		tabWidget = tabHost.getTabWidget();
		tabContent = tabHost.getTabContentView();
		/*
		TextView[] originalTextViews = new TextView[tabWidget.getTabCount()];
		for (int index = 0; index < tabWidget.getTabCount(); index++) {
			originalTextViews[index] = (TextView) tabWidget.getChildTabViewAt(index);
		}
		tabWidget.removeAllViews();
		
		// Ensure that all tab content childs are not visible at startup.
		for (int index = 0; index < tabContent.getChildCount(); index++) {
			tabContent.getChildAt(index).setVisibility(View.GONE);
		}
		
		// Create the tabspec based on the textview childs in the xml file.
		// Or create simple tabspec instances in any other way...
		for (int index = 0; index < originalTextViews.length; index++) {
			final TextView tabWidgetTextView = originalTextViews[index];
			final View tabContentView = tabContent.getChildAt(index);
			TabSpec tabSpec = tabHost.newTabSpec(Integer.toString(index));
			
			TabContentFactory tcf=new TabContentFactory() {
				@Override
				public View createTabContent(String tag) {
					int ix=Integer.parseInt(tag);
					return tabContent.getChildAt(ix);
				}
				
			};
			
			tabSpec.setContent(tcf);
			
			if (tabWidgetTextView.getBackground() == null) {
				tabSpec.setIndicator(tabWidgetTextView.getText());
			} else {
				tabSpec.setIndicator(tabWidgetTextView.getText(), tabWidgetTextView.getBackground());
			}
			tabHost.addTab(tabSpec);
		}		
		tabHost.setCurrentTab(0);
		*/
		addTab("#main","#main");
	}

	boolean firstExecution() {
		return !"false".equalsIgnoreCase((String) props.get(FIRST_EXECUTION));
	}


	boolean shouldSayGreeting() {
		return (Intent.ACTION_MAIN.equals(lastIntent.getAction()) || byExpansionDownloading())
				&& (true/*!App.self.isGreetingDisabled() || App.self.getCountExecution() <= Consts.GREETING_SAY_RUN_TIMES*/);
	}

	boolean anyWelcome() {
		return firstExecution()/* ||shouldSayGreeting() */;
	}


	void whenAllVoicesInstalled() {
		Log.d(TAG, "whenAllVoicesinstalled");

		if (dontUseAcapella) {
			App.self.setCurrentVoice(MyTTS.SYSTEM_VOICE);
			MyTTS.setVoice(MyTTS.SYSTEM_VOICE);
		}

        Utils.updateUserDictionary();

        calcCountExecution();
		voicesInstallerWorks = false;
		if (firstExecution()) {
            Props props=App.self.getProps();
            props.setAndSave(MainActivity.FIRST_EXECUTION, "false");

			Log.d(TAG, "dnl:acapellaVoicesAndAfter -- firstExecution()");

			if (Config.use_itro) {
				IntroSlidesActivity.start(MainActivity.this, true);
				MainActivity.this.finish();
				return;
			}

			// play intro clip, first time only
			// playIntroClip();


			final TimerTask terminator = new TimerTask() {
				@Override
				public void run() {
					Log.d(TAG, "terminator");
					// Here we know that the installed voices are
					// broken.
					MyTTS.abort();

					installAcapellaVoices(
						new Runnable() {
						  public void run() {
							  whenAllVoicesInstalled();
						  }
						}
					);
				}
			};


			if (!dontUseAcapella) {
				Timer timer = new Timer();
				timer.schedule(terminator, 20000);
			}
			/*
			 * if (simulation) { simulation=false; return; // tesk voice
			 * reinstallation }
			 */

			speakText(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "cancel terminator");
					terminator.cancel();
				}

				@Override
				public String toString() {
					return getString(R.string.P_welcome_0);
				}
			});
			speakText(
				new MyTTS.Wrapper(R.string.P_welcome_1) {

					@Override
					public void onSaid(boolean fAborted) {
						super.onSaid(fAborted);
						doAfterWelcome(true);
					}

				}
			);
		} else {
			boolean ss = shouldSayGreeting();
			if (ss) {
				lastIntent = new Intent(Intent.ACTION_SEARCH);
				lastIntent.putExtra(SearchManager.QUERY, SCMD_INIT);
			}
			doAfterWelcome(ss);
		}
	};

	int countExecution;

	private void prepareLogos() {
		prepareLogo(R.string.parking_logo_url, parkingProviderLogo);
		prepareLogo(R.string.yelp_logo_url, yelpLogo);
	}

	private void prepareLogo(int id, final ImageView iv) {
		try {
			new ImageFetcher(getResources().getString(id)) {
				@Override
				protected void onPostExecute(Bitmap bmp) {
					if (bmp != null) {
						ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) iv
								.getLayoutParams();
						lp.width = (int) (bmp.getWidth() * App.self.scaler.densityScaleFactor);
						lp.height = (int) (bmp.getHeight() * App.self.scaler.densityScaleFactor);

						// iv.setLayoutParams(scaleIt(lp));
						iv.setImageBitmap(bmp);
					}
				}
			};
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	volatile boolean initialized = false;

	boolean isInactive() {
		return initialized && progressBarStopper.isInactive();
	}

	boolean isReady() {
		Log.d(TAG, "isReady");
		return initialized && (progressBarStopper.getCounter() == 0);
	}

	void doAfterWelcome(final boolean anyWelcome) {
		Log.d(TAG, "dnl:doAfterWelcome");
		mainView.postDelayed(
				new Runnable() {
					public void run() {
						hideSplash(anyWelcome);
					}
				},
		  3
		);

	}

	void hideSplash(boolean anyWelcome) {
		Log.d(TAG, "dnl:hideSplash");

		if (byExpansionDownloading()) {
			lastIntent.setAction(Intent.ACTION_MAIN);
		}

		if (!anyWelcome()) {
			initialized = true;
			handlePreferences(prefs, true, null);
			handleIntent(lastIntent);
			return;
		}

        handlePreferences(prefs, true, null);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                setMainViewBg();
            }
        };

        initialized = true;
        handleIntent(lastIntent);
	}

	int mapType = Integer.parseInt(app_getString(R.string.MStandart));

	/*
	private void saveVoicePref() {
		Editor ed = prefs.edit();
		ed.putString(getString(R.string.PfVoiceType),
				Integer.toString(voiceType));
		ed.commit();
	}
	*/

	int nVoiceDnlAttempt = 0;

	void installAcapellaVoicesIfNeed(final Runnable afterThat) {

		Log.d(TAG, "dnl:installAcapellaVoicesIfNeed");


		if (MyTTS.getVoice() == MyTTS.SYSTEM_VOICE) {
	//		MyTTS.setVoice(App.self.getCurrentVoice());
			afterThat.run();
			return;
		}

	}

	private void sayThatSystemVoiceWillBeUsed() {
		Output.sayAndShow(MainActivity.this,
		  new MyTTS.Wrapper(
			R.string.P_voice_download_troubles
		  ).setShowInNewBubble(true).setShowNewBubbleAfter(true)
		);
	}

	private volatile Intent lastIntent = null;

	boolean byExpansionDownloading() {
		if (lastIntent == null)
			return false;

		boolean edn=EXPANSION_DOWNLOADER_NOTIFICATION.equals(lastIntent.getAction());

		Log.d(TAG,"edn!");

		return edn;
	}

	volatile boolean expDownlaoding = false, voicesInstallerWorks = false;

	private EventSource acapellaVoicesES = null;

	public boolean isWaitingForVoiceDownload() {
		EventSource es=acapellaVoicesES;
		return es!=null&&!es.isFired();
	}

	private void takeAcapellaProgress() {
		if (acapellaVoicesES == null || acapellaVoicesES.isCompleted())
			acapellaVoicesES = showProgress();
	}

	public final static boolean dontUseAcapella=Utils.isAndroid5orAbove;

	final static String TAG_IAV = "IAV";

	void installAcapellaVoices(final Runnable afterThat) {
         afterThat.run();
    }

	boolean inForeground = true;

	int zlAfterAdjust;

	public void showLocation(DoublePoint pt) {
		showLocation(pt, null);
	}

    static String debug = "";

    static public void debug_car_mode(String s) {
        MainActivity ma = get();

        if (!App.self.getBooleanPref("car_mode_option_debug") || ma == null) {
            if (debug.length() > 1000)
                debug = "";
            debug += s+"\r\n";
            return;
        }

        if (debug.length() > 0) {
            ma.bubleAnswer(debug+s);
            debug = "";
        }
        else
            ma.bubleAnswer(s);
    }

    public void disableMyLocation() {
        if (mlo != null)
            mlo.disableMyLocation();
    }

	public void showLocation(final DoublePoint pt, final Runnable afterThat) {
		initMap();
		mv.postDelayed(new Runnable() {
			@Override
			public void run() {
                mv.moveTo(pt, true, false);
				if (afterThat != null)
					afterThat.run();
				// TODO: track actual animation finish
			}
		}, mv.timeToWaitMaturing());
	}
	
	public void onListen() {
		onListen(null);
	}

	public void onListen(View v) {
		if (micAnimator != null)
			micAnimator.hideError();
		if (initialized) doListen(v!=null);
	}

	public void doListen(boolean askToInstallRecognizer) {
		hideShownView();
		if (twitterPlayer != null)
			twitterPlayer.abort();
		if (optionalDialog != null)
			optionalDialog.dismiss();
		if (vr.isStarted()) {
			vr.stop();
			return;
		}

		//abortOperation(0);

		listen(askToInstallRecognizer);
	}

	static final int LAUNCH_REQUEST_CODE = 2;
	static final int TASK_TERMINATE_REQUEST_CODE = 3;
	public static final String VOID_PAGE_URL = "http://robingets.me/void.html";

	private void listen() {
		listen(false);
	}

	void playIntroClip() {
		Launchers.youtube(this,"pEIpBSDsOPM");
	}

	boolean shouldRunVRActivity = false;

	private void listen(boolean askToInstall) {
		if (!askToInstall) {
			OnBeforeListeningHandler h = CmdHandlerHolder.getOnBeforeListeningHandler();
			if (h != null)
				h.onBeforeListening();
		}
		App.self.voiceIO.listen(askToInstall);
	}

	boolean btIsOn() {
		BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
		if (bta != null) {
			return (bta.getState() == BluetoothAdapter.STATE_ON);
		}
		return false;
	}

	IFeed feedController = null;

	@Override
	public void setFeedController(IFeed c) {
		feedController = c;
	}

	public String getAdvancePrompt() {
		Advance adv = App.self.voiceIO.getAdvance();
		return adv == null ? null : adv.getPrompt();
	}

	public void handleRecognitionResult(int resultCode, Intent data) {
		ArrayList<String> matches = data == null ? null : data
				.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
		ArrayList<String> old_matches = data == null ? null : data
				.getStringArrayListExtra(VR.EXTRA_RESULT_NO_REPLACEMENT);

		handleRecognitionResult(resultCode, matches, old_matches);

	}

	public void handleRecognitionResult(int resultCode,
			ArrayList<String> matches, ArrayList<String> old_matches) {

        if (matches == null)
            return;

		if (intentToForwardListeningResult!=null) {
			Intent data=new Intent();
			data.putExtra(RecognizerIntent.EXTRA_RESULTS, matches);
			data.putExtra(VR.EXTRA_RESULT_NO_REPLACEMENT, old_matches);
			if (resultCode==0&&matches==null&&old_matches==null)
				data.putExtra(LISTENING_ABORTED, true);


			intentToForwardListeningResult.putExtra(LISTENING_RESULT, (Parcelable)data);
			intentToForwardListeningResult.putExtra(LISTENING_RESULT_CODE, (int)resultCode );


			if (Utils.isService(
			   intentToForwardListeningResult.getComponent()
			))
			   startService(intentToForwardListeningResult);
			else
			   startActivity(intentToForwardListeningResult);

			intentToForwardListeningResult=null;

			return;
		}

		LocalCommandHandler lch = CmdHandlerHolder.getLocalCommandHandler();

		if ((resultCode == RESULT_FIRST_USER) || (resultCode == VR.RESULT_RUN_INTENT) || (resultCode == RESULT_OK)) {

			if (!isEmpty(matches)) {
				String q = "";
				for (int i = 0; i < matches.size(); i++) {
					q = matches.get(i);
					Log.d(TAG + ".onActivityResult: ", q);
					break;
				}
				if (resultCode == RESULT_FIRST_USER)
					bubbleQueryPartial(q);
				else
					bubbleQuery(q, matches, old_matches);

				App.self.voiceIO.setAdvance(null);

				// TODO: for scripted scenarios only, keep in comments otherwise
//				 if (isScripted && scriptedSeq != null) {
//					 Outcome pair = scriptedSeq.getNext();
//					 matches = new ArrayList<String>();
//					 matches.add(pair.mgQuery);
//				 }

				if (lch != null && lch.onVoiceInput(matches, resultCode == RESULT_FIRST_USER)) // warning!!! for local handler using old_matches (without conversation)!!!
					return;

				if (resultCode == RESULT_FIRST_USER)
					return;

                if (resultCode == VR.RESULT_RUN_INTENT) {
                    bubleAnswer(getString(R.string.P_launching_intent));
                    return;
                }

                // experimental code
                if (App.self.getBooleanPref("qaTesting")) {
					if (!isEmpty(aliasList)) {
						Collection<String> newMatches = new HashSet<String>();
						for (String s : matches) {
							List<String> mm = genMoreSpeechMatchesFromAliases(
									s.toLowerCase(), aliasList);
							if (!Utils.isEmpty(mm))
								newMatches.addAll(mm);
						}
						if (!Utils.isEmpty(newMatches))
							matches.addAll(newMatches);
					}
				}

				if (App.self.isSamsungVersion) {
					if (SamsungNavigationCommandHandler.handle(this,matches)) {
						return;
					}
				}

				requestMagnifisUnderstanding(matches);
				return;
			}

		} else {
			if (lch != null && lch.onVoiceInput(null, resultCode == RESULT_FIRST_USER))
				return;
		}
		Log.d(TAG, "mic off");
		App.self.voiceIO.runAdvance();
	}

	private ArrayList<String> genMoreSpeechMatchesFromAliases(String utterance,
			ArrayList<CmdAlias> aliases) {
		ArrayList<String> moreMatches = new ArrayList<String>();

		StopWordsPredicate pred = StopWordsPredicate.getInstance();
		String ss = pred.dropStopWords(utterance);
		if (Utils.isEmpty(ss))
			ss = utterance;
		for (CmdAlias alias : aliases) {
			String src = alias.getAlias();
			String srcKey = alias.getKey(); // search key
			if (Utils.isEmpty(srcKey))
				srcKey = src; // in this case, SRC is the key
			if (ss.contains(srcKey) && (ss.length() < srcKey.length() * 2)) {// utterance
																				// contains
																				// the
																				// key
																				// as
																				// a
																				// non-negligible
																				// part

				String subst;
				if (utterance.contains(src)
						&& (utterance.length() < src.length() * 2)) {
					subst = utterance.replace(src, alias.getCommand()); // try
																		// full
																		// alias
																		// first
				} else {
					subst = ss.replace(srcKey, alias.getCommand()); // use
																	// canonical
																	// rep
				}
				moreMatches.add(subst); // TODO: hack, actually need to send
										// aliases to server
			}
		}

		return moreMatches;
	}

	/**
	 * Handle the results from the recognition packageName.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (App.self.mBilling != null && App.self.mBilling.isStarted() &&
                App.self.mBilling.handleActivityResult(requestCode, resultCode, data))
            return;

		if (!checkDiskAvailability())
			return;

		ActivityResultHandler arh = CmdHandlerHolder.getActivityResultHandler();
		if (arh == null || !arh.onActivityResult(requestCode, resultCode, data))
			switch (requestCode) {
			case LAUNCH_REQUEST_CODE:
				if (data != null)
					_startNestedActivity(this,data);
				break;
			case TASK_TERMINATE_REQUEST_CODE:
				break;
			case VR.VOICE_RECOGNITION_REQUEST_CODE:
				Log.d(TAG, "VR_LOOKUP_REQUEST_CODE");
				App.self.voiceIO.cancelListeningTimeout();
				handleRecognitionResult(resultCode, data);
				break;
			case FbHelper.AUTHORIZE_ACTIVITY_RESULT_CODE:
				fbHelper.facebook.authorizeCallback(requestCode, resultCode, data);
			}

		super.onActivityResult(requestCode, resultCode, data);
	}

	void sayAgain() {
		if (currentController.getSelected() != null) {
			currentController.showWholeSet(false);
			currentController.sayDetails(true);
		}
		fireOpes();
	}

	/*
	MAStatus status = new MAStatus(), prevStatus = new MAStatus();
	Understanding savedReply = null;
    */
	UnderstandingStatus us=UnderstandingStatus.getInstance();

	public static boolean isActive() {
		return MainActivity.getActive()!=null;
	}

	public static boolean isOnTop() {
		   return  isActive()||
			   Utils.isForegroundActivity(App.self, MainActivity.class.getCanonicalName());
	}

	public void performNavigationIfCan() {
		GeoObject obj = getSelected();
		if (obj == null || obj.getPoint() == null) {
			speakText(app_getString(R.string.mainactivity_performnavigationifcan_not_sure));
		} else
			navigateTo(obj.getPoint());
	}

	private boolean checkForFirstNavigationAndSuggestDefaultWaze(
			final DoublePoint dst) {
		boolean result = false;
		String firstNavExecution = (String) props
				.get(FIRST_NAVIGATION_EXECUTION);
		if (firstNavExecution == null
				|| !firstNavExecution.equalsIgnoreCase("false")) {
			if (isWazeInstalled(dst)) {
				result = true;
				props.setAndSave(FIRST_NAVIGATION_EXECUTION, "false");
				speakText(R.string.P_ASK_WAZE_AS_DEFAULT);
				final boolean ok[] = { false };
				AlertDialog ad = Utils.askConfirmation(this,
						R.string.default_navigator,
						R.string.P_ASK_WAZE_AS_DEFAULT,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								SharedPreferences.Editor prefsEditor = prefs
										.edit();
								prefsEditor
										.putString(
												app_getString(R.string.PfNavigatorType),
												app_getString(R.string.NtWaze));
								prefsEditor.commit();
								speakText(R.string.P_ASK_WAZE_OK);
								navigateTo(dst);
								ok[0] = true;
							}
						});
				ad.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						if (!ok[0]) {
							speakText(R.string.P_ASK_WAZE_CANCEL);
							navigateTo(dst);
						}
					}
				});
			}
		}
		return result;
	}

	public void navigateTo(final DoublePoint dst) {
		clearRouteOverlays();
		if (!checkForFirstNavigationAndSuggestDefaultWaze(dst)) {
			speakText(pickLaunchNavPhrase());
			launchGpsNavigator(this,dst);
			// new code : report delays on the way
			new Thread() {
				@Override
				public void run() {
					// the "other" nav system could be speaking now - let it
					// finish first
					try {
						Thread.sleep(15000); // TODO: ideally, should check if
												// system TTS is still speaking
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					EtaMonitor etaMonitor = new EtaMonitor();
					DoublePoint lc = UserLocationProvider.readLocationPoint();
					if (lc != null) {
						Map<String, Integer> delays = etaMonitor.getDelays(
								lc.toString(), dst.toString());
						if (delays != null && !delays.isEmpty()) {
							StringBuffer strAlert = new StringBuffer(
									app_getString(R.string.mainactivity_navigateto_attention));
							for (Map.Entry<String, Integer> delay : delays
									.entrySet()) {
								strAlert.append(delay.getKey())
										.append(" "
												+ app_getString(R.string.mainactivity_navigateto_delayed)
												+ " "); // route name
								strAlert.append(delay.getValue())
										.append(" "
												+ app_getString(R.string.mainactivity_navigateto_minutes)
												+ " ");
							}
							String alertText = strAlert.toString();
							sayAndShowFromGui(strAlert
									+ " "
									+ app_getString(R.string.mainactivity_navigateto_check_route_options));
							sayAndShowFromGui(app_getString(R.string.mainactivity_navigateto_again)
									+ " " + strAlert);
						}
					}
				}
			}.start();
		}
	}

	boolean isWebViewShown() {
		return webView!=null && (viewMode == viewWEB);
	}

	public static void openUrlFromOtherActivity(Activity ac, String url) {
		Intent it = new Intent();
		it.setClass(ac, MainActivity.class);
		it.setAction(MainActivity.INTERPRET_UNDERSTANDING);
		Understanding u = new Understanding()
				.setCommandByCode(Understanding.CMD_OPEN_URL);
		u.setUrl(url);
		u.setShowEmbedded(true);
		it.putExtra(MainActivity.EXTRA_UNDERSTANDING, u);
		Launchers._startNestedActivity(ac, it);
	}

	String lastWebViewUrl = null;

	public void openUrl(String url, boolean fClearHistory, final String jsFuncCode, final Map<String, String> jsParamPairs) {

		if (fClearHistory&&webView!=null) {
			LinearLayout inView = (LinearLayout) mainView.findViewById(R.id.webPlace);
			inView.removeAllViews();
			webView=null;
		}

		if (webView == null) {
			webView = new TheWebView(this);
			webView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
			LinearLayout inView = (LinearLayout) mainView.findViewById(R.id.webPlace);
			inView.addView(webView); // beforeView
		}




		TheWebView ww = webView;
	    boolean isJsContainerOnly = false;
		if (Utils.isEmpty(url) && !Utils.isEmpty(jsFuncCode)) {
			isJsContainerOnly = true;
			ww = jsContainerVW;
		}


		if (null == ww) {
			//LinearLayout v = (LinearLayout) mainView.findViewById(R.id.WebLayout);
			ww = new TheWebView(this);
			ww.setClickable(true);

			LinearLayout inView = (LinearLayout) mainView.findViewById(R.id.webPlace);
			inView.addView(ww); // beforeView


			if (isJsContainerOnly) {

				jsContainerVW = ww;

				// TODO:?????
				//ww.setVisibility(View.GONE); // hide by default



			}else {
				webView = ww;
				inView.bringChildToFront(ww); // TODO: needed??
			}
		}



		if (fClearHistory) {
			ww.clearHistory();
		}

		if (!Utils.isEmpty(jsFuncCode)) { // execute a JS function from the URL

			final String theUrl = url;
			ww.getSettings().setJavaScriptEnabled(true);
			// wwClient must be set BEFORE calling loadUrl!
			ww.setWebViewClient(new WebViewClient() {

				private boolean oneTimeActivationDone = false;

				@Override
			    public void onPageStarted(WebView view, String url, Bitmap favicon) {

					Log.i(TAG, "onPageStarted for: " + url);

					if (isEmpty(url) || url.equalsIgnoreCase(VOID_PAGE_URL))
						;//view.setVisibility(View.INVISIBLE);// pre-hide by default
					else
						view.setVisibility(View.VISIBLE);
					super.onPageStarted(view, url, favicon);
				}

				@Override
			    public boolean shouldOverrideUrlLoading(WebView view, String url) {
			        //view.loadUrl(url);
			        boolean answer = url.equals(theUrl);// overriding for specific URL only
			        if (false == answer) {
			        	view.setVisibility(View.VISIBLE); // restore things before losing control
			        }

			        return answer;
			    }


			    @Override
			    public void onPageFinished(WebView view, String url)
			    {

			    	if (this.oneTimeActivationDone ||
			    			(!isEmpty(url) && url.startsWith("javascript:("))) {

			    		super.onPageFinished(view, url);
			    		return; // prevent recursion
			    	}

			    	Log.i(TAG, "onPageFinished for:  " + url);

			    	String paramList = "";
			    	if (jsParamPairs != null)
			    		paramList = JsBridge.getFunctionParamValues2Substitute(jsFuncCode, jsParamPairs);
  			    	String jsCall = "javascript:("+jsFuncCode + ")(" + paramList + ")";
			    	Log.i(TAG, "Calling JS function: " + jsCall);

			    	if (isEmpty(url) || url.equalsIgnoreCase(VOID_PAGE_URL))
			    		;//view.setVisibility(View.GONE);// hide by default

			    	oneTimeActivationDone = true; // client valid for one time only
			    	view.loadUrl(jsCall);
			     }
			});
			ww.addJavascriptInterface(new AndroidJsBridge(this, ww), "android");
			// disabled for now  
	//		ww.addJavascriptInterface(new FeedsJsBridge(this, ww), "feeds");
		} else {
			//ww.setVisibility(View.VISIBLE);// back to default
		}



		showInternet();

		if (Utils.isEmpty(url)) {
			url = VOID_PAGE_URL;
		}

		Log.i(TAG, "Loading URL " + url);
		ww.loadUrl(url);

	}


	public void openUrl(Understanding u, String jsScriptCode) {

		String url = u.getUrl();
		if (u.isShowEmbedded()) {
			String jsCode = null,
					action = u.getAction();
			if (!Utils.isEmpty(action) && action.startsWith("script")) {
				Script scriptVal = u.getScript();
				if (scriptVal != null && !Utils.isEmpty(jsScriptCode)) {
					jsCode = jsScriptCode;
				}

				if (Utils.isEmpty(url)) {
					url = VOID_PAGE_URL;
				}
			}

			Map<String, String> paramPairs = u.getActionParamNVPairs();
			openUrl(url, false, jsCode, paramPairs);
		} else
			Launchers.launchBrowser(this,url);

	}


	String getWebViewUrl() {
		return (webView == null) ? lastWebViewUrl : webView.getUrl();
	}

	private Dialog optionalDialog = null;

	public void setOptionalDialog(Dialog dlg) {
		optionalDialog = dlg;
	}

	private boolean requiringHandWaving=true;

	@Override
	public boolean isRequiringHandWaving() {
		return requiringHandWaving;
	}

	void learnFromTextInput(Understanding u) {
		if (optionalDialog != null)
			optionalDialog.dismiss();
		Origin org = u.getOrigin();
		requiringHandWaving=false;

		// // TODO: remove
		// Log.i(TAG, "Teaching custom location");
		//
		// String fullAddress = null;
		// if (org != null) {
		// fullAddress = org.getFullAddress();
		// if (fullAddress != null &&
		// org.calculateLocation(getLastKnownLocation())) {
		// showLocation(org.getLocation());
		// }
		// }
		optionalDialog = u.getLearnAttribute().learnFromTextInput(this,
				(org == null ? null : org.getFullAddress()), new Runnable() {
					@Override
					public void run() {
						condListenAfterTheSpeech();
						requiringHandWaving=true;
					}
				});
	}
/*
	void doInGui(String mn) {
		try {
			final Method m = getClass().getMethod(mn);
			m.setAccessible(true);
			runOnUiThread(new Runnable() {
				public void run() {
					try {
						m.invoke(MainActivity.this);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/
	void setLogo() {
		parkingProviderLogo
				.setVisibility((pksController != null && pksController.size() > 0) ? View.VISIBLE
                        : View.GONE);
		if (poisController != null && poisController.size() > 0) {
			yelpLogo.setVisibility(poisController.anyYelpInfo() ? View.VISIBLE
					: View.GONE);
			/*
			 * googleLogo.setVisibility( poisController.anyGoogleInfo()
			 * ?View.VISIBLE :View.GONE );
			 */
		} else {
			yelpLogo.setVisibility(View.GONE);
			// googleLogo.setVisibility(View.GONE);
		}
        googleLogo.setVisibility(mv != null ? View.VISIBLE : View.GONE);
        locationLogo.setVisibility(mv != null ? View.VISIBLE : View.GONE);
	}

    class PlacemarkOverlay extends Overlay {

        private GeoPoint location = null;
        private boolean star = false;

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            super.draw(canvas, mapView, shadow);

            if (shadow || location == null)
                return;

            Point point = new Point();
            mapView.getProjection().toPixels(location, point);

            Bitmap bmp;
            if (star)
                bmp = BitmapFactory.decodeResource(getResources(), R.drawable.placemark_red);
            else
                bmp = BitmapFactory.decodeResource(getResources(), R.drawable.map_marker_greendot);

            int x = point.x - bmp.getWidth() / 2;
            int y = point.y - bmp.getHeight();

            canvas.drawBitmap(bmp, x, y, null);
        }
    }
/*
    void showMark2(DoublePoint dstLocation) {
        initMap();
        us.status.setPrevMapCenter(DoublePoint.from(mv.getMapCenter()));
        us.status.setPrevLocation(pmo.getLocation());
        pmo.doPopulate(dstLocation);
    }*/

    void showMark(DoublePoint dstLocation) {

        Abortable ab = CmdHandlerHolder.getAbortableCommandHandler();
        if (ab != null) ab.abort(0);

        if (mlo != null)
            mlo.myLocationEnabled = true;

        initMap();

        markController.star = false;
        markController.location = new GeoPoint((int)(dstLocation.getLat() * 1E6), (int)(dstLocation.getLon() * 1E6));

        MapController mc = mv.getController();
        mc.setZoom(16);
        //mc.zoomToSpan(markController.location.getLatitudeE6(), markController.location.getLongitudeE6());
        //mc.animateTo(markController.location);

        mv.invalidate();
	}

    void showMark2(DoublePoint dstLocation) {

        Abortable ab = CmdHandlerHolder.getAbortableCommandHandler();
        if (ab != null) ab.abort(0);

        initMap();

        markController.star = true;
        markController.location = new GeoPoint((int)(dstLocation.getLat() * 1E6), (int)(dstLocation.getLon() * 1E6));

        MapController mc = mv.getController();
        mc.setZoom(16);
        //mc.zoomToSpan(markController.location.getLatitudeE6(), markController.location.getLongitudeE6());
        //mc.animateTo(markController.location);

        mv.invalidate();
    }

    void handleNoParking(Understanding understanding) {
		handleNoOption(understanding, false);
	}

	private void handleNoOption(Understanding understanding) {
		handleNoOption(understanding, true);
	}

	void handleNoOption(Understanding understanding, boolean usePoiName) {

		if (!usePoiName && poisController!=null && (poisController.size() > 0)) {
			String poiAddr = poisController.get(0).getStreetAddress();
			if (!isEmpty(poiAddr))
				speakText(app_getString(R.string.mainactivity_handlenooption_nearest_one)
						+ " "
						+ poiAddr
						+ app_getString(R.string.mainactivity_handlenooption_but));
		}

		String phrase = pickNoParkingPhrase();

		if (usePoiName && understanding != null) {
			String poiName = understanding.getOrigin().getPoiName();
			phrase.replace("parking", poiName);
		}

		speakText(phrase);
		listenAfterTheSpeech();

	}

	private boolean anyParking() {
		return us.status.getNumberOfAvailableParkings() > 0;
	}

	boolean silentParking = false;

	boolean indirectParkingSearch = false;


	public MultipleEventHandler.EventSource opes = null;

	boolean ifIsNeccessaryReadMessages = false;

	boolean loadMapFromBg() {
		return loadMapFromBg(null);
	}

	boolean loadMapFromBg(final Runnable onSuccessInGui) {
		Throwable t=Utils.runInGuiAndWait(mainView, new Runnable() {
			@Override
			public void run() {
				initMap();
				if (onSuccessInGui != null)
					onSuccessInGui.run();
			}
		});
		if (t!=null) {
		  Log.e(TAG,"loadMapFromBg failed");
		  t.printStackTrace();
		}
		return t == null;
	}

	public void requestMagnifisUnderstanding(Collection<String> matches) {

        if(App.self.robin().canHandleResponseOffline(matches)){
            return;
        }

		MultipleEventHandler.EventSource es = showProgress();
		try {
			UnderstandingProcessor mFetcher = new UnderstandingProcessor(this,
					es);
			VoiceIO.setCurrentUP(mFetcher);
			
			String postData = null; 
			
			// TODO: temp experiment!!! this changes the request to POST! 
			if (App.self.robin().isDebugMode()) 		
				postData = AppNames.getInstance().getApps(this).toString(); 
			
			
			mFetcher.execute(createMagnifisUnderstandingRqUrl(this, matches),
							postData, null);
		} catch (Throwable e) {
			es.fireEvent();
			e.printStackTrace();
		}
	}

	public void interpretMagnifisUnderstanding(Understanding u) {
		Log.d(TAG, "interpretMagnifisUnderstanding");
		MultipleEventHandler.EventSource es = showProgress();
		try {
			UnderstandingProcessor mFetcher = new UnderstandingProcessor(this,
					es);
			VoiceIO.setCurrentUP(mFetcher);
			mFetcher.execute(u);
		} catch (Throwable e) {
			es.fireEvent();
			e.printStackTrace();
		}
	}

	boolean isSpecCommandRecent() {
		if (specCommandTime != null)
			return (System.currentTimeMillis() - specCommandTime) < 60l * 1000l; // one
																					// minute
		return false;
	}

	void updateSpecCommandTime() {
		specCommandTime = System.currentTimeMillis();
	}

	private Long specCommandTime = null;

	private SharLoc sharloc = null;// new SharLoc();

	public void shareCurrentLocation(String method) {
		mlo.reportLocation();
		LocationInfo linf=UserLocationProvider.queryLocation();
		if ((linf == null)||(linf.getLocation()==null)||(linf.getSensorStatus()==UserLocationProvider.LOC_UNAVAILABE)) {
			speakText(R.string.P_NO_CURRENT_LOCATION);
			listenAfterTheSpeech();
		} else {
			int k=linf.getSensorStatus();
			DoublePoint lc=linf.getLocationDP();
			if (k==UserLocationProvider.LOC_NONE_GPS)
				speakText(R.string.P_EXACT_LOCATION_GPS);
			sharloc.shareLocation(this, linf, method, true);
			/*
			switch (k) {
			case UserLocationProvider.LOC_GPS:
				sharloc.shareLocation(lc, method, true);

				break;
			default:
				speakText((k == UserLocationProvider.LOC_COLD_GPS) ? R.string.P_EXACT_LOCATION_COLD_GPS
						: R.string.P_EXACT_LOCATION_GPS);
				listenAfterTheSpeech();
			}
			*/
		}
	}

	public void onNavigate(View v) {
		performNavigationIfCan();
	}

	public void onCall(View v) {
		callCurrentSelection();
	}

	public void onBrowse(View v) {
		GeoObject sel = currentController.getSelected();
		if (sel != null) {
			String url = currentController.getSelected().getUrl();
			if (!isEmpty(url)) {
				openUrl(url, true, null, null);
			}
		}
	}

	public void onMyLocation(View v) {

		Log.d(TAG, "On my location...");

		//abortOperation(0);
		//abortVR();

        Abortable ab = CmdHandlerHolder.getAbortableCommandHandler();
        if (ab != null) ab.abort(0);

        if (mlo != null)
            mlo.myLocationEnabled = true;

        initMap();
        mapController.setZoom(17);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                bubbleQuery(getString(R.string.ButtonMyLocation), null, null);
            }
        }, 0);

        postDelayed(new Runnable() {
            @Override
            public void run() {
                LocationInfo repLocRes = UserLocationProvider.queryLocation();

                if (repLocRes == null) {
                    speakText(R.string.P_NO_CURRENT_LOCATION);
                    return;
                }

                int k = repLocRes.getSensorStatus();
                DoublePoint lc = repLocRes.getLocationDP();
                if (lc == null) {
                    speakText(R.string.P_NO_CURRENT_LOCATION);
                    return;
                }
                if (!(repLocRes.isExact() || k == UserLocationProvider.LOC_GPS)) {
                    if (k == UserLocationProvider.LOC_NONE_GPS)
                        speakText(R.string.P_EXACT_LOCATION_GPS);
                }
                if (sharloc != null)
                    sharloc.sayLocation(MainActivity.this, repLocRes);
            }
        }, 3000);
	}

	public void onInfo(View v) {

//		IntroSlidesActivity.start(this, false);

        //App.self.robin().showNotification();

		bubbleQuery(getString(R.string.ButtonHelp), null, null);
		HelpActivity.onInfo(this, false);

	/*
		final ToastController tc[]={null};

		SmsAlertView pv=App.self.createFromLayout(R.layout.sms_alert);
		pv.setData(
				  "very very very long very very very long very very very long very very very long very very very long  an sms text ",
				  "John Appleseed",
          new Runnable() {
						@Override
						public void run() {

						}
		  },
		  null,
		  new Runnable() {
			@Override
			public void run() {
				Log.d("XX","YY");
				tc[0].abort();
			}
		  }
		);
		ToastBase.LayoutOptions lo= new ToastBase.LayoutOptions();
		lo.contentPadding=new Rect(0,40,0,0);
		tc[0]=new ToastController(pv,lo,null,true);
	*/
	}


	@Override
	protected boolean isRouteDisplayed() {
		return true;
	}

	public MapItemSetContoller getSelectionController() {
		if (mv != null)
			for (Overlay ov : mv.getOverlays())
				if (ov instanceof GeoItemsOverlay) {
					GeoItemsOverlay gio = (GeoItemsOverlay) ov;
					if (gio.controller.getSelected() != null)
						return gio.controller;
				}
		return currentController;
	}

	private Object _getSelectedSO = new Object();

	public GeoObject getSelected() {
		synchronized (_getSelectedSO) {
			MapItemSetContoller c = getSelectionController();
			return c == null ? null : c.getSelected();
		}
	}

	private void callCurrentSelection() {
		callCurrentSelection(null);
	}

	public void callCurrentSelection(Understanding understanding) {
		GeoObject obj = getSelected();
		if (obj != null) {
			String phone = obj.getPhone();
			if (phone != null) {

				if (understanding != null)
					speakText(understanding.getQueryInterpretation());

				Log.i(TAG, "Calling phone: " + phone);
				// Call phone number
				dial(this, phone);
			} else {
				sayFromGui(pickCannotCallPhrase());
			}

		} else
			Launchers.openDialer(this);
	}

	void enableTraffic() {
		if (mv != null) {
			mv.setTraffic(true);
			mv.refreshDrawableState();
		}
	}

	public void swapMapView() {
		switchMapView(!mv.isSatellite());
	}

	void switchMapView(boolean fSat) {
		if (mv != null && fSat != mv.isSatellite()) {
			mv.setSatellite(fSat);
			mv.refreshDrawableState();
			SharedPreferences.Editor prefsEditor = prefs.edit();
			prefsEditor.putString(getString(R.string.PfMapType),
					getString(fSat ? R.string.MSatellite : R.string.MStandart));
			prefsEditor.commit();
		}
	}

	boolean checkDiskAvailability() {
		boolean f = isExternalStorageAvailable();
		if (!f) {
			AlertDialog ad = Utils.showAlert(this, null,
					R.string.M_EXT_STORAGE_REQUIRED);
			ad.getButton(AlertDialog.BUTTON_NEUTRAL).setText("Exit");
			ad.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			});
		}
		return f;
	}

	private Intent newIntentToProcessing = null;

	private void processNewIntent(Intent intent) {
		Log.d(TAG, "processNewIntent");
		if (!voicesInstallerWorks) {
				if (initialized)
					handleIntent(intent);
	    }
	}

	@Override
	public void onNewIntent(Intent intent) {
		newIntentToProcessing = null;
		super.onNewIntent(intent);
		if (Intent.ACTION_VOICE_COMMAND.equals(intent.getAction())
				&& vr != null)
			vr.markCallByVoiceCommand();
		Log.d(TAG, "onNewIntent: " + intent.toString());
		if (!FROM_CLEAN.equals(intent.getAction()))
			lastIntent = intent;
		if (!checkDiskAvailability())
			return;
		if (paused && !SHOW_TRAFFIC.equals(intent.getAction()))
			newIntentToProcessing = intent;
		else
			processNewIntent(intent);

	}

	boolean voiceCommand = false;

	private boolean shouldPerformMainAction() {
		return askForReviewDlg();
	}

	public static void wakeUp() {
		wakeUp(false,false);
	}

	public static void cancelListeningFor() {
		wakeUp(true,true);
	}

	public static void wakeUp(boolean dontListen) {
		wakeUp(dontListen,false);
	}

	public static void newTask() {
		Intent it=new Intent(Intent.ACTION_MAIN);
		it.setClass(App.self, MainActivity.class);
		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Utils.startActivityFromNowhere(it);
	}

	public static void wakeUp(boolean dontListen, boolean stopListeningFor) {
		Intent it=new Intent(WAKE_UP);
		it.setClass(App.self, MainActivity.class);
		it.putExtra(DONT_LISTEN, dontListen);
		it.putExtra(STOP_LISTENING_FOR, stopListeningFor);
		Utils.startActivityFromNowhere(it);
	}

	void handleIntent(Intent intent) {
		if (!(FROM_CLEAN.equals(intent.getAction())
				|| WAKE_UP.equals(intent.getAction())
				|| Intent.ACTION_MAIN.equals(intent.getAction())
                || Intent.ACTION_ASSIST.equals(intent.getAction())
                || Intent.ACTION_VOICE_COMMAND
					.equals(intent.getAction()))) {
			Intent it = new Intent(FROM_CLEAN);
			it.setClass(this, MainActivity.class);
			startActivity(it);
			return;
		}
		intent = lastIntent;
		voiceCommand = false;
		String ac = intent.getAction();

		IIntentHandler inh = CmdHandlerHolder.getIntentHandler();
		if (inh == null || !inh.onNewIntent(intent)) {
			if (INTERPRET_UNDERSTANDING.equals(ac)) {
				Log.d(TAG, INTERPRET_UNDERSTANDING);
				if (isReady()) {
					Utils.dump(TAG, intent);
					Understanding u = (Understanding) intent
							.getSerializableExtra(EXTRA_UNDERSTANDING);
					if (u != null)
						interpretMagnifisUnderstanding(u);

					if (intent.getBooleanExtra(EXTRA_FOLLOWUP_REQUEST, false))
						WidgetProvider.infromServerOnTheTextClick();
				}
			} else if (MainActivity.SAY_SHOW_AND_LISTEN.equals(intent
					.getAction())) {
				if (intent.getBooleanExtra(WORK_WITHOUT_QUE, false))
					sayShowAndListen(intent, false);
				else
					queSayShowAndListen(intent);
			} else if (Intent.ACTION_MAIN.equals(ac)) {
				if (shouldPerformMainAction())
					doListen(false);
            } else if (Intent.ACTION_ASSIST.equals(ac)) {
                    doListen(false);
			} else if (OPEN_MAP.equals(ac)) {
				initMap();
            } else if (START_PAYMENT.equals(ac)) {
                if (App.self.mBilling != null && App.self.mBilling.isStarted())
                    App.self.mBilling.launchPurchaseFlow(this, "ALYONA_VOICE", "inapp", "");
            } else if (SHOW_TRAFFIC.equals(ac)) {
                interpretMagnifisUnderstanding(new Understanding()
                        .setCommandByCode(Understanding.CMD_TRAFFIC)
                        .setQueryInterpretation(R.string.P_showing_traffic));
			} else if (START_TEACH.equals(ac)) {
				App.self.hideNfyScreen();
				bubbleQuery(getString(R.string.P_cmd_teach), null, null);
				interpretMagnifisUnderstanding(new Understanding()
                        .setCommandByCode(Understanding.CMD_TEACH)
                        .setQuery(getString(R.string.P_cmd_teach))
                        .setQueryInterpretation(R.string.P_teach_q_say));
			} else if (Intent.ACTION_SEARCH_LONG_PRESS.equals(ac)) {
				doListen(false);
			} else if (VR_RESULTS.equals(ac)) {
				handleRecognitionResult(Activity.RESULT_OK, intent);
            } else if (TYPE.equals(ac)) {
                onSearchRequested();
            } else if (SHARE.equals(ac)) {
                shareRobin(this);
            } else if (SHUT_DOWN.equals(ac)) {
                Robin.shutdown();
			} else if (Intent.ACTION_SEARCH.equals(ac)) {
				Log.d(TAG + ".onNewIntent: ", intent.toString());
				String query = Utils.trim(intent.getStringExtra(SearchManager.QUERY));
				if (!isEmpty(query)) {
					boolean mainAction = true;

					if (SCMD_INIT.equalsIgnoreCase(query)) {
						// don't save init to the history
						mainAction = shouldPerformMainAction();
						if (mainAction) {
							if (App.self.getCountExecution()<3)
								listen();
							else
								Utils.runInBgThread(new Runnable() {

									@Override
									public void run() {

										String phrase = null;

										// say info phrase
										String [] sayOnStart = App.self.getResources().getStringArray(R.array.sayOnStart);
										if (sayOnStart != null) {
											for(int i = 0; i < sayOnStart.length; i++) {
												int code = sayOnStart[i].hashCode();
												if (!SaidPhrase.test(code)) {
													SaidPhrase.mark(code);
													phrase = sayOnStart[i];
													break;
												}
											}
										}

										if (phrase == null)
                                            if (!App.self.isGreetingDisabled())
											    phrase = (new Robin()).sayHello();

                                        if (phrase != null)
										    Output.sayAndShow(App.self, phrase);

										listenAfterTheSpeech();
									}
								}, null);
							mainAction = false;
						}
					} else {
						SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
								this, TheRecentSuggestionsProvider.AUTHORITY,
								TheRecentSuggestionsProvider.MODE);
						suggestions.saveRecentQuery(query, null);
					}
					if (mainAction) {
						ArrayList<String> matches = new ArrayList<String>();

						if (Config.split_search_result_by_semicolon) {
							for (String s:Utils.simpleSplit(query, ';')) {
								s=s.trim();
								if (!isEmpty(s)) matches.add(s);
							}
						} else
							matches.add(query);

						ArrayList<String> new_matches = vr == null ? matches : vr
								.teachReplaceTeaching(matches);
                        if (new_matches != null)
						    handleRecognitionResult(Activity.RESULT_OK, new_matches, matches);
                        else
                            handleRecognitionResult(VR.RESULT_RUN_INTENT, matches, matches);
					}
				}
			} else if (WAKE_UP.equals(ac)
					|| (voiceCommand = Intent.ACTION_VOICE_COMMAND.equals(ac))) {

				App.self.hideNfyScreen();// in case it is open
				if (intent.getBooleanExtra(EXTRA_REQUEST_SEARCH, false)) {
					onSearchRequested();
				} else if (isReady()) {
					if (intent.getBooleanExtra(STOP_LISTENING_FOR, false)) {
						this.doCancelListeningFor();
					} else if (!intent.getBooleanExtra(DONT_LISTEN, false)) {
						onListen();
						/*
						if (!vr.isListening())
                            doListen(false);
                            */
							//vr.killMicrophone();
						//else*/
					}
				}
            }
		}
		resetIntent();
	}

	void queSayShowAndListen(final Intent it) {
		App.self.voiceIO.getOperationTracker().queOperation(
		  new Runnable() {

			@Override
			public void run() {
				sayShowAndListen(it,true);
			}

		  },
		  true
		);
	}


	private void doCancelListeningFor() {
		if (intentToForwardListeningResult!=null) intentToForwardListeningResult=null;
		App.self.voiceIO.cancelListeningTimeout();
	}

	void sayShowAndListen(Intent it, boolean whenIdle) {
		String toSay=it.getStringExtra(SAY), toShow=it.getStringExtra(SHOW);

		if (!isEmpty(toSay)&&!isEmpty(toShow))
			Output.sayAndShow(MainActivity.this, toShow, toSay, false);
		else if (!isEmpty(toSay)) {
			MyTTS.speakText(toSay);
		}

		Intent toListen=it.getParcelableExtra(LISTEN);
		if (toListen==null) {
			if (whenIdle) fireOpes();
		} else {
			App.self.voiceIO.setListeningTimeout(it.getLongExtra(LISTEN_TIMEOUT, 0));
			vr.useFreeForm=it.getBooleanExtra(LISTEN_USE_FREE_FORM, false);
			String lng=it.getStringExtra(LISTEN_LANG);
			if (lng!=null)   vr.useLanguage=lng;
			MainActivity.this.intentToForwardListeningResult=toListen;

			if (App.self.isPhoneLocked()) {
				if (!whenIdle) fireOpes();
				return;
			}


			if (whenIdle || !MyTTS.isSpeaking()) {
			  listen();
			} else
			  listenAfterTheSpeech();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onSearchRequested()
	 */
	@Override
	public boolean onSearchRequested() {
		Log.d(TAG, "onSearchRequested");
		if (!isReady())
			return false;
		abortOperation(true,0, true);
		vr.abort();
		if (twitterPlayer != null) {
			twitterPlayer.abort();
		}
		hideShownView();
		if (App.self.voiceIO.getOperationTracker().hasQueuedThreads())
			return false;
		return super.onSearchRequested();
	}

	private boolean abortVR() {
		if (vr.isStarted()) {
			vr.abort();
			OnListeningAbortedHandler h = CmdHandlerHolder.getOnListeningAbortedHandler();
			if (h != null)
				h.onListeningAbortedByBackKeyPressed();

			////////////////////////////////////////////////////////////////
			if (intentToForwardListeningResult!=null)
				handleRecognitionResult(0,null, null);
			////////////////////////////////////////////////////////////////

			return true;
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (initialized) {
				if (abortVR()) {
					return true;
				}

				/////////////////////
				if (intentToForwardListeningResult!=null) {
					handleRecognitionResult(0,null, null);
					return true;
				}
				/////////////////////

				if (abortOperation(0)) {
					return true;
				}

				if (isWebViewShown()) {
					if (!webView.hanldeBack())
						switchMode(false);
					return true;
				}

				if (shownView != null) {
					hideShownView();
					return true;
				}

				if (viewMode == viewMAP && Config.bubles) {
					showBubles();
					return true;
				}
			}

            alertClose.show();

			return true;
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_HEADSETHOOK:
		case KeyEvent.KEYCODE_MEDIA_PAUSE:
		case KeyEvent.KEYCODE_MEDIA_PLAY:
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			Log.d("KEY", ":" + keyCode);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.d(TAG, "onPrepareOptionsMenu");
		menu.clear();
		if (isReady()) {
			vr.abort();
			abortOperation(true,Abortable.BY_MENU_CLICK,true);
			getMenuInflater().inflate(R.menu.main, menu);
			return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int cmd=item.getItemId();
		/////////////////////
		if (cmd!=R.id.menu_search&&intentToForwardListeningResult!=null)
			handleRecognitionResult(0,null, null);
		/////////////////////
		switch (cmd) {
		case R.id.menu_share_robin:
			shareRobin(this);
			return true;
		case R.id.menu_feedback:
			//composeFeedback();
			com.magnifis.parking.Log.collectLogThenComposeFeedback(this);
			return true;
		case R.id.menu_search:
			onSearchRequested();
			return true;
		case R.id.menu_preferences:
			openSettings();
			return true;
		case R.id.menu_teach:
			bubbleQuery(getString(R.string.P_cmd_teach), null, null);
			interpretMagnifisUnderstanding(new Understanding()
					.setCommandByCode(Understanding.CMD_TEACH)
					.setQuery(getString(R.string.P_cmd_teach))
					.setQueryInterpretation(R.string.P_teach_q_say));
			return true;
		}
		return false;
	}

	void openSettings() {
		Intent it = new Intent();
		it.setClass(this, PrefsActivity.class);
		startNestedActivity(this, it);
	}

	private String PfFuelTypeKey = app_getString(R.string.PfFuelType);

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d(TAG, "onSharedPreferenceChanged");
		handlePreferences(sharedPreferences, true, key);
	}

	void onFuelTypeChanged() {
		if (currentController == gasController && currentController != null) {
			GasReply gss = (GasReply) gasController.getSpannable();
			if (gss != null) {
				GasStation sel = gasController.getSelected();
				gss.recalculate();
				us.status.setGasStationIterator(gss.facilities());
				gasOverlay.doPopulate();

				if (gasController.size() > 0) {

					if (gasController.indexOf(sel) < 0)
						sel = gasController.getIterator().next();

					gasController.setSelected(sel);
					gasController.showWholeSet(true);

				}
			}
		}
	}

	private boolean keepScreenAwake = true, zoomcontrols = false;

	public boolean isScripted = false;
	public ScriptedSequence scriptedSeq = null; // new ScriptedSequence();

	private final String
			PfMapType = app_getString(R.string.PfMapType),
			//PfProximitySensor = app_getString(R.string.PfProximitySensor),
			PfKeepScreenAwake = app_getString(R.string.PfKeepScreenAwake)
			;

	private void handlePreferences(SharedPreferences sharedPreferences,
			boolean apply, final String key) {

		if (PfFuelTypeKey.equals(key))
			onFuelTypeChanged();

		keepScreenAwake = App.self.getBooleanPref(PfKeepScreenAwake);

		mapType = Integer.parseInt(App.self.getStringPref(PfMapType));

		if (apply)
			runOnUiThread(new Runnable() {
				public void run() {

					if (key == null || key.equals(PfMapType)) {
						switchMapView(mapType == Integer
								.parseInt(getString(R.string.MSatellite)));
					}

				}
			});

	}

	public void togggleScriptedSequence(boolean flag) {

		isScripted = flag;
		Log.w(TAG, "Setting script sequencing to " + flag);

		if (isScripted) {
			scriptedSeq = ScriptedSequence.newScriptedSequence3();
		} else {
			scriptedSeq = null;
		}
	}

	public void hideWebView() {
		if (isWebViewShown()) {
			switchMode(false);
		}
	}

	private boolean wlOn = false;

	void wakeLockAcquire() {
		if (wlOn)
			return;
		wlOn = true;
		wakeLock.acquire();
	}

	void wakeLockRelease() {
		if (wlOn) {
			wakeLock.release();
			wlOn = false;
		}
	}

	private boolean paused = true, killed = false;

	private boolean from_new_intent() {
		StackTraceElement st[] = Thread.currentThread().getStackTrace();
		for (StackTraceElement ste : st)
			if (ste.getClassName().indexOf("magnifis") < 0
					&& ste.getMethodName().toLowerCase().indexOf("newintent") >= 0) {
				Log.d(TAG, "fromNewIntent " + ste.getMethodName());
				return true;
			}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();

		if (!paused) {
			paused = true;

			if (sharloc != null) {
				sharloc.abort();
				sharloc = null;
			}
			// null;
			Log.d(TAG, "onPause");

			// if (mlo!=null) mlo.disableMyLocation();
			if (optionalDialog != null) {
				optionalDialog.dismiss();
				optionalDialog = null;
			}

			/*
			Abortable ab=getAbortableCommandHandler();
			if (ab != null) {
				if (ab instanceof SendCmdHandler)
					abortOperation(0);
			}*/

			// audioManager=null;
			// Launchers.releaseAppCache();
			wakeLockRelease();
			// resetIntent();
			App.self.removeActiveActivity(this);
		}
	}

	AudioManager getAM() {
		return (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.android.maps.MapActivity# if (vr != null) vr.close();
	 *
	 * if (vr != null) vr.close();
	 *
	 * if (!isOpened()) return;
	 *
	 * resume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

        Robin.start();

        com.facebook.Settings.publishInstallAsync(this, "571677029578494");

        if (vr != null && vr.activity != this) {
			vr = VR.create(this, micAnimator, false, Intent.ACTION_VOICE_COMMAND.equals(lastIntent.getAction()));
		}

		if (paused) {
			Log.d(TAG, "onResume");

			/*
			Intent serviceIntent = new Intent();
			serviceIntent.setClass(App.self, SuzieService.class);
			serviceIntent.setAction(ApplicationMonitorAction.ABORT.name());
			App.self.startService(serviceIntent);
			*/

			App.self.setActiveActivity(this);

			if (sharloc == null)
				sharloc = new SharLoc();
			if (App.self.isInCarMode()||App.self.isCharging())
				wakeLockAcquire();

			if (newIntentToProcessing != null) {
				processNewIntent(newIntentToProcessing);
				newIntentToProcessing = null;
			}

			handlePreferences(prefs, true, null);

            OnResumeHandler orh = CmdHandlerHolder.getOnResumeHandler();
			if (orh != null)
				orh.onResume();
			paused = false;
		}
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart ");
		prefs.registerOnSharedPreferenceChangeListener(this);
		//this.handlePreferences(prefs, true, null);
		Launchers.loadAppCache();
		twitterPlayer = new TwitterPlayer(this);

		if (vr != null) {
			if (Intent.ACTION_VOICE_COMMAND.equals(lastIntent.getAction()))
				vr.markCallByVoiceCommand();
		}

        if (viewMode == viewMAP)
            if (mlo != null)
                mlo.enableMyLocation();

        super.onStart();

        EasyTracker.getInstance(this).activityStart(this);  // google analytics tracking
    }

	private void calcCountExecution() {
		countExecution = App.self.getCountExecution() + 1;
		props.setAndSave(COUNT_EXECUTION, Integer.toString(countExecution));
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");

        prefs.unregisterOnSharedPreferenceChangeListener(this);

		if (twitterPlayer != null) {
			twitterPlayer.abort();
			twitterPlayer = null;
		}

        if (mlo != null)
            mlo.disableMyLocation();

        Launchers.releaseAppCache();
		wakeLockRelease();

		App.self.notifyStopActivity(this);

		super.onStop();

        EasyTracker.getInstance(this).activityStop(this);  // google analytics tracking

        System.gc();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.google.android.maps.MapActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		killed = true;
		/*
		 * if (mlo!=null) try { mlo.disableMyLocation(); } catch(Throwable t) {}
		 * if (sharloc!=null) { sharloc.abort(); sharloc=null; }
		 */
		abortOperation(false,0,false);
		if (mSpinner != null) {
			mSpinner.dismiss();
			mSpinner = null;
		}
		Launchers.releaseAppCache();
		super.onDestroy();
		System.gc();
	}

	private WakeLock wakeLock = null;

	/*
	 * public DoublePoint getLastKnownLocation() { return
	 * (mlo==null)?null:mlo.getMyLocation(); }
	 */
	/*
	private boolean askForShortcut() {
		boolean result = true;
		boolean asked = Boolean.parseBoolean(props.getProperty(SHORTCUT,
				"false"));
		if (!asked && !App.self.shouldPlaceActivationIcon() && (countExecution > 5)
				&& (countExecution != 50)) {
			props.setAndSave(SHORTCUT, "true");
			AlertDialog ad = Utils.askConfirmation(this,
					R.string.shortcut_for_me, R.string.P_ASK_SHORTCUT_TRAY,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							SharedPreferences.Editor prefsEditor = prefs.edit();
							prefsEditor.putBoolean(PrefConsts.PF_ACTIVATION_ICON, true);
							prefsEditor.commit();
						}
					});
		}
		return result;
	}
	*/

	private boolean askForReviewDlg() {
		if (App.self.getCountExecution() == 50) {
			if ("true".equals(props.getProperty(RATE)))
				return false;
			speakText(R.string.P_RATE_ME_ON_GOOGLE);
			final boolean ok[] = { false };
			AlertDialog ad = Utils.askConfirmation(this, R.string.recommend_me,
					R.string.P_RATE_ME_ON_GOOGLE,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ok[0] = true;
							props.setAndSave(RATE, "true");
							lookAtMarketFor(MainActivity.this,MainActivity.class.getPackage()
									.getName());
						}
					});

			ad.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (!ok[0])
						props.setAndSave(RATE, "false");
				}
			});
			return false;
		}
		return true;
	}

	public void clearRouteOverlays() {

		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				// first, clean all old route overlays
				if (routeOverlays != null) {
					for (RoutePathOverlay oldOverlay : routeOverlays) {
						oldOverlay.clear();
						mv.getOverlays().remove(oldOverlay);
					}
				}
			}
		});
	}

	public void setRouteOverlays(final List<RoutePathOverlay> newOverlays) {

		if (mv != null && !Utils.isEmpty(newOverlays)) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					// first, clean all old route overlays
					if (routeOverlays != null) {
						for (RoutePathOverlay oldOverlay : routeOverlays) {
							oldOverlay.clear();
							mv.getOverlays().remove(oldOverlay);
						}
					}

					// add new ones
					routeOverlays = new RoutePathOverlay[newOverlays.size()];
					int iRoute = 0;
					for (RoutePathOverlay overlay : newOverlays) {
						mv.getOverlays().add(overlay);
						routeOverlays[iRoute++] = overlay;
					}
				}

			});
		}

	}

	CalReminding calReminder = CalReminding.getInstance();

	@Override
	public void onUserInteraction() {
		super.onUserInteraction();
	}

	@Override
	protected void onUserLeaveHint() {
		// TODO Auto-generated method stub
		super.onUserLeaveHint();
	}

	public CallSmsAttemptsHistory callSmsAttemptsHistory = CallSmsAttemptsHistory.getInstance();

	/******************************************************************************************************************/
	/******************************************************************************************************************/

    private static final int viewBUBLES = 1;
    private static final int viewMAP = 2;
    private static final int viewWEB = 3;
    private static final int viewMAX = 3;
    //private static final int viewHELP = 3;
    //private static final int viewSETTINGS = 5;

    private int viewMode = 1;

    public void onSwitchMode(View v) {
    	switchMode(true);
    }

    public void switchMode(boolean forward) {
    	if (!Config.bubles)
    		return;

    	while (true) {
    		if (forward)
    			viewMode++;
    		else
    			viewMode--;

    		switch (viewMode) {
				case viewBUBLES:
					showBubles();
					return;
				case viewMAP:
					initMap();
					return;
				case viewWEB:
					if (webView != null) {
						showInternet();
						return;
					}
				default:
		    		if (forward)
		    			viewMode = 0;
		    		else
		    			viewMode = viewMAX;
    		}

    	}
    }

    public void showMap() {

//    	if (Config.roku_version)
//    		MainActivity.get().togggleScriptedSequence(App.self.getBooleanPref("qaTesting"));



    	if (!Config.bubles)
    		return;

    	viewMode = viewMAP;

        setLogo();

    	//View clickArea = (View) mainView.findViewById(R.id.clickArea);
    	//clickArea.setVisibility(View.VISIBLE);
    	if (mv != null)
        	mv.setVisibility(View.VISIBLE);
    	if (logoBar != null)
    		logoBar.setVisibility(View.VISIBLE);

    	if (webView != null)
    		webView.setVisibility(View.GONE);

        if (mlo != null)
            mlo.enableMyLocation();

        //View helpRoot = (View)findViewById(R.id.tableLayoutHelpRoot);
		//helpRoot.setVisibility(View.GONE);

    	//View helpScreen = (View) mainView.findViewById(R.id.helpScreen);
    	//helpScreen.setVisibility(View.GONE);
    }

    
    public void showBubles() {

    	if (!Config.bubles)
    		return;

    	viewMode = viewBUBLES;

		final View dialogs = (View) mainView.findViewById(R.id.Dialogs);
    	dialogs.setVisibility(View.VISIBLE);

    	if (webView != null) {
    		webView.setVisibility(View.GONE);
    	}

        if (mlo != null)
            mlo.disableMyLocation();

    	//View helpRoot = (View)findViewById(R.id.tableLayoutHelpRoot);
		//helpRoot.setVisibility(View.GONE);

    	//View helpScreen = (View) mainView.findViewById(R.id.helpScreen);
    	//helpScreen.setVisibility(View.GONE);

    	//View clickArea = (View) mainView.findViewById(R.id.clickArea);
    	//clickArea.setVisibility(View.VISIBLE);
    	if (mv != null)
        	mv.setVisibility(View.GONE);
    	if (logoBar != null)
    		logoBar.setVisibility(View.GONE);

    	scrollDownAllDialogs();
    }
    
    void scrollDownAllDialogs() {
    	final View dialogs = (View) mainView.findViewById(R.id.Dialogs);
       	dialogs.post(new Runnable() {
            @Override
            public void run() {
            	dialogs.setVisibility(View.VISIBLE);
            	for (int i=0; i<tabContent.getChildCount(); i++) try {
	            	ScrollView scroll = (ScrollView)tabContent.getChildAt(i);    //dialogs.findViewById(R.id.DialogScroll);
	                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            	} catch (Throwable t) {}
            }
        });   	
    }

    public void showInternet() {

    	if (!Config.bubles)
    		return;

    	viewMode = viewWEB;

		if (webView != null)
    		webView.setVisibility(View.VISIBLE);

        if (mlo != null)
            mlo.disableMyLocation();

        //View clickArea = (View) mainView.findViewById(R.id.clickArea);
    	//clickArea.setVisibility(View.VISIBLE);

    	if (mv != null)
        	mv.setVisibility(View.GONE);
    	if (logoBar != null)
    		logoBar.setVisibility(View.GONE);

    	//View helpRoot = (View)findViewById(R.id.tableLayoutHelpRoot);
		//helpRoot.setVisibility(View.GONE);

    	//View helpScreen = (View) mainView.findViewById(R.id.helpScreen);
    	//helpScreen.setVisibility(View.GONE);

    	// ?
    	feedController=null;
    	//vwScreen.setVisibility(View.VISIBLE);
    }

    public void onSplashClick(View v) {
    	MyTTS.abort();
   }

   /*******************************************************************************************************************/
    /*******************************************************************************************************************/

	private static final int MENU_ITEM_BUBLE_QUERY_REPEAT = 0;
	private static final int MENU_ITEM_BUBLE_QUERY_CORRRECT = 1;
	private static final int MENU_ITEM_BUBLE_QUERY_FORGET = 2;

	private static final int MENU_ITEM_BUBLE_ANSWER_REPEAT = 0;
	private static final int MENU_ITEM_BUBLE_ANSWER_SHARE = 1;
	private static final int MENU_ITEM_BUBLE_ANSWER_FEEDBACK = 2;

	ArrayList<View> bublesQuery = new ArrayList<View>();
	ArrayList<View> bublesAnswer = new ArrayList<View>();

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);

    	CharSequence s = null;
    	try {
        	TextView tv  = (TextView)v.findViewById(R.id.textViewBubleAnswer);
        	if (tv==null) tv = (TextView)v.findViewById(R.id.textViewBubleQuery);
    		s = tv.getText();
    	} catch (Exception e) {}

    	if (Utils.isEmpty(s))
    		return;

    	int group = bublesQuery.indexOf(v);
    	if (group >= 0) {
    		abortOperation(false, Abortable.BY_MA_BUBBLE_CLICK,true);
    		
    		Object opt=v.getTag(R.string.tag_bubble_options);
    		IBubbleContent bc=MyTTS.Wrapper.findInterface(opt, IBubbleContent.class);

    		boolean restrict=(bc!=null)&&bc.isMenuRestricted();

    		if (!restrict) {
    			menu.add(group, MENU_ITEM_BUBLE_QUERY_REPEAT, 0, getString(R.string.BUBLE_MENU_QUERY_REPEAT));
    			menu.add(group, MENU_ITEM_BUBLE_QUERY_CORRRECT, 0, getString(R.string.BUBLE_MENU_QUERY_CORRECT));
    		}

        	String[] q = s.toString().split("->");
       		if (q.length > 1 && !Utils.isEmpty(q[1]))
           		menu.add(group, MENU_ITEM_BUBLE_QUERY_FORGET, 0, getString(R.string.BUBLE_MENU_QUERY_FORGET));
    	}
    	group = bublesAnswer.indexOf(v);
    	if (group >= 0) {

    		abortOperation(false, Abortable.BY_MA_BUBBLE_CLICK,true);

    		Object opt=v.getTag(R.string.tag_bubble_options);
    		IBubbleContent bc=MyTTS.Wrapper.findInterface(opt, IBubbleContent.class);

    		boolean restrict=(bc!=null)&&bc.isMenuRestricted();

    		if (!restrict) {
    		  menu.add(100+group, MENU_ITEM_BUBLE_ANSWER_REPEAT, 0, getString(R.string.BUBLE_MENU_ANSWER_REPEAT));
    		  menu.add(100+group, MENU_ITEM_BUBLE_ANSWER_SHARE, 0, getString(R.string.BUBLE_MENU_ANSWER_SHARE));
    		}
    		menu.add(100+group, MENU_ITEM_BUBLE_ANSWER_FEEDBACK, 0, getString(R.string.BUBLE_MENU_ANSWER_FEEDBACK));
    		return;
    	}
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	if (item == null)
    		return false;

		int group = item.getGroupId();
		if (group < 100) {
	    	CharSequence s = null;
	    	Object   bc=null;
	    	IRepeat  rp=Wrapper.findInterface(bc, IRepeat.class);
	    	try {
				View v = bublesQuery.get(group);
				bc=v.getTag(R.string.tag_bubble_options);
                TextView tv;
                tv = (TextView)((LinearLayout)((LinearLayout)v).getChildAt(0)).getChildAt(0);
	    		s = tv.getText().toString();
	    	} catch (Exception e) {}

	    	if (s == null)
	    		return false;

        	String[] q = s.toString().split("->");
        	s = q[0];

	    	if (s == null)
	    		return false;

	    	s = trim(s);
	    	if (s == null)
	    		return false;

        	ArrayList<String> matches = new ArrayList<String>();
        	matches.add(s.toString());
        	ArrayList<String> new_matches = null;

	    	switch (item.getItemId()) {
	    	case MENU_ITEM_BUBLE_QUERY_REPEAT:
	    		if (rp!=null) {
	    			rp.onRepeat();
	    		} else {
	    			new_matches = vr==null?matches:vr.teachReplaceTeaching(matches);
	    			if (new_matches != null)
	    				handleRecognitionResult(RESULT_OK, new_matches, matches);
	    			else
	    				handleRecognitionResult(VR.RESULT_RUN_INTENT, matches, matches);
	    		}
	    		break;
	    	case MENU_ITEM_BUBLE_QUERY_CORRRECT:
	    		final String source = s.toString();
				//speakText(R.string.P_teach_a_type);
				Utils.showTextInputDialog(this, R.string.BUBLE_MENU_QUERY_CORRECT, source,
						new Utils.TextInputDialogResult() {

							@Override
							public void onDialogOK(String result) {
								if (vr != null) {
									LearnedAnswer qa = new LearnedAnswer();
						    		qa.setQuestion(source);
						    		qa.setAnswer(result);
						    		qa.setSay(0);
						    		vr.teachAdd(qa);
						        	ArrayList<String> matches = new ArrayList<String>();
						        	matches.add(source);
						        	ArrayList<String> new_matches = vr==null?matches:vr.teachReplaceTeaching(matches);
                                    if (new_matches != null)
    						    		handleRecognitionResult(RESULT_OK, new_matches, matches);
                                    else
                                        handleRecognitionResult(VR.RESULT_RUN_INTENT, matches, matches);
								}
							}

							@Override
							public void onDialogClose() {
							}
						}
					);

	    		break;
	    	case MENU_ITEM_BUBLE_QUERY_FORGET:
				if (vr != null)
		    		vr.teachDel(s.toString(), q[1]);
				new_matches = vr==null?matches:vr.teachReplaceTeaching(matches);
                if (new_matches != null)
    	    		handleRecognitionResult(RESULT_OK, new_matches, matches);
                else
                    handleRecognitionResult(VR.RESULT_RUN_INTENT, matches, matches);
	    		break;
			}
		}
		else {
	    	String s = null;
	    	String q = null;
	    	try {
				View v = bublesAnswer.get(group-100);
                TextView tv;
                tv = (TextView)((LinearLayout)((LinearLayout)v).getChildAt(0)).getChildAt(0);
	    		s = tv.getText().toString();

	    		Object o = v.getTag(R.string.tag_query);
	    		if (o != null) {
                    tv = (TextView)((LinearLayout)((LinearLayout)o).getChildAt(0)).getChildAt(0);
		    		q = tv.getText().toString();
	    		}

	    	} catch (Exception e) {}

	    	String x = "";
	    	if (!isEmpty(q))
	    		x = getString(R.string.P_my_query) + " " + q + "\r\n";

	    	if (s == null)
	    		return false;

	    	switch (item.getItemId()) {
	    	case MENU_ITEM_BUBLE_ANSWER_REPEAT:
	            bubblesBreak("#main"); //imp
	            String[] n = s.split("\n");
	        	for(int i = 0; i < n.length; i++) {
	            	MyTTS.speakText(n[i]);
	        	}
	    		break;

	    	case MENU_ITEM_BUBLE_ANSWER_SHARE:
	    		Launchers.shareTextPlain(this, getString(R.string.P_robin_promo), x + getString(R.string.P_robin_answer) + " " + s);
	    		break;

	    	case MENU_ITEM_BUBLE_ANSWER_FEEDBACK:
	    		Launchers.composeFeedback(this, x + getString(R.string.P_robin_answer) + " " + s, null);
	    		break;
	    	}
		}

    	return super.onContextItemSelected(item);
    }


    public TextView getLastBubbleAnswerTextView() {
    	return  getMainTabAttr().bubleLastAnswerTextView;
    }

    ArrayList<String> last_matches = null;
    ArrayList<String> last_old_matches = null;

    public void bubleQueryCorrect(String s) {
    	
    	final TabContent ta=getMainTabAttr();

    	if (!Config.bubles || !SuziePopup.bublesEnabled)
    		return;

    	if (ta.bubleLastQueryTextView == null)
    		return;

    	String buble_text = s;

    	if (last_matches != null && last_old_matches != null && last_old_matches.size() == last_matches.size())
    		for (int i = 0; i < last_matches.size(); i++) {
    			if (s.equalsIgnoreCase(last_matches.get(i))) {
    				if (!s.equalsIgnoreCase(last_old_matches.get(i)))
    					buble_text = last_old_matches.get(i) + " -> " + Utils.firstUpper(s, true);
    				break;
    			}
    		}

    	final String final_buble_text = buble_text;

		runOnUiThread(
				   new Runnable() {
					@Override
					public void run() {
						ta.bubleLastQueryTextView.setText(Utils.firstUpper(final_buble_text, false));
					}
				   }
				);
    }


    public void onBubbleAnswerClick(View v) {
    	boolean
    	  ok0=abortVR(),
		  ok1=abortOperation(false,Abortable.BY_MA_BUBBLE_CLICK,true);

		if (!ok1||ok0)
		   openContextMenu(v);
   }


   public void onBubbleQueryClick(View v) {
	   onBubbleAnswerClick(v);
   }
   
    public void bubbleQuery(Object s, ArrayList<String> matches, ArrayList<String> old_matches) {
	     bubbleQuery(s, "#main", matches, old_matches);
    }
    
    public void bubbleQuery(Object s, String tabId) {
    	 bubbleQuery(s, tabId, null, null);
    }

    private void bubbleQuery(final Object _s, final String tabId, ArrayList<String> matches, ArrayList<String> old_matches) {

    	if (!Config.bubles || !SuziePopup.bublesEnabled)
    		return;

        if (mainView == null)
        	return;
        
        CharSequence s=MyTTS.Wrapper.findInterface(_s, CharSequence.class);
        		
        if (isEmpty(s) || s.equals("initialization"))
                	return;

    	last_matches = matches;
    	last_old_matches = old_matches;

    	CharSequence buble_text = s;

    	if (last_matches != null && last_old_matches != null && last_old_matches.size() == last_matches.size())
	    	try {
	    		for (int i = 0; i < last_matches.size(); i++) {
	    			if (s.toString().equalsIgnoreCase(last_matches.get(i))) {
	    				if (!s.toString().equalsIgnoreCase(last_old_matches.get(i)))
	    					buble_text = last_old_matches.get(i) + " -> " + Utils.firstUpper(s, true);
	    				break;
	    			}
	    		}
	    	} catch (Exception e) {}

    	final CharSequence final_buble_text = buble_text;

		runOnUiThread(
				   new Runnable() {
					@Override
					public void run() {
						TabContent ta=MainActivity.this.getTabAttr(tabId);
						
						// if was partial...
						if (ta.bubleLastShown == 3) {
							ta.bubleLastQueryTextView.setText(Utils.firstUpper(final_buble_text, false));
						}
						else {
					    	LayoutInflater inflater =
					    		    (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );

					    	LinearLayout layout;
                            layout = (LinearLayout) inflater.inflate(R.layout.buble_query_flat, null);
                            ta.bubleLastQueryTextView = (TextView) layout.findViewById(R.id.textViewBubleQuery);
                            ta.bubleLastQueryTextView.setText(Utils.firstUpper(final_buble_text, false));

					    	ViewGroup dialogs = MainActivity.this.getTabDialogView(tabId, false);
					    	layout.setOnLongClickListener(new View.OnLongClickListener() {

								@Override
								public boolean onLongClick(View v) {
									onBubbleQueryClick(v);
									return true;
								}
							});
					    	
					    	
					    	layout.setTag(R.string.tag_bubble_options,_s);


					        dialogs.addView(layout);

					        bublesQuery.add(layout);
					        registerForContextMenu(layout);
						}

						ta.bubleLastShown = 1; // query!

				        scrollDownAllDialogs();
					}
				   }
				);
    }

    public void bubbleQueryPartial(final String buble_text) {

    	if (!Config.bubles || !SuziePopup.bublesEnabled)
    		return;

    	if (isEmpty(buble_text))
        	return;

        if (mainView == null)
        	return;

		runOnUiThread(
				   new Runnable() {
					@Override
					public void run() {
						TabContent ta=MainActivity.this.getMainTabAttr();
						
						// was partial ?
						if (ta.bubleLastShown == 3) {
					    	ta.bubleLastQueryTextView.setText(Utils.firstUpper(buble_text, false));
						}
						else {

					    	LayoutInflater inflater =
					    		    (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );

					    	LinearLayout layout;
				    		layout = (LinearLayout) inflater.inflate(R.layout.buble_query_flat, null);
					    	ta.bubleLastQueryTextView = (TextView) layout.findViewById(R.id.textViewBubleQuery);
					    	ta.bubleLastQueryTextView.setText(Utils.firstUpper(buble_text, false));

					    	ViewGroup dialogs = MainActivity.this.getMainTabDialogView();
					    	layout.setOnLongClickListener(new View.OnLongClickListener() {

								@Override
								public boolean onLongClick(View v) {
									onBubbleQueryClick(v);
									return true;
								}
							});

					        dialogs.addView(layout);

					        bublesQuery.add(layout);
					        registerForContextMenu(layout);

					        ta.bubleLastShown = 3; // partial !
						}

						scrollDownAllDialogs();
					}
				   }
				);
    }
    
    public void bubleAnswer(final Object s) {
    	bubleAnswer(s, "#main");
    }

    public void bubleAnswer(final Object s, final String tabId) {

    	if (!Config.bubles)
    		return;

        if (isEmpty(MyTTS.Wrapper.findInterface(s, CharSequence.class)))
        	return;

        if (mainView == null)
        	return;

        mainView.post(
          new Runnable() {

			@Override
			public void run() {
				_bubbleAnswer(s,s, tabId);
			}
          }
        );
    }

    private void _bubbleAnswer(Object _s, Object _root, String tabId) {
    	CharSequence s=MyTTS.Wrapper.findInterface(_s, CharSequence.class), 
    			  root=MyTTS.Wrapper.findInterface(_root, CharSequence.class);
    	
    	
    	
    	MyTTS.IShowInNewBubble nb=MyTTS.Wrapper.findInterface(_s, MyTTS.IShowInNewBubble.class);
    	
    	if (nb!=null&&nb.shouldShowInNewBubble()) bubblesBreak(tabId); 


    	TabContent ta=this.getTabAttr(tabId);

						CharSequence toAdd = firstUpper(trim(s), false);

						if (isEmpty(toAdd)) return;

						switch (toAdd.charAt(0)) {
						case '.': case ',':
							toAdd = toAdd.subSequence(1, toAdd.length());   //.substring(1);
							if (Utils.isEmptyOrBlank(toAdd))
								return;
							toAdd = trim(Utils.firstUpper(toAdd, false));
						}

						if (Utils.isEmptyOrBlank(toAdd))
							return;

						boolean append_buble = true;
						try {
							if (toAdd.subSequence(0, 3).toString().equals("###")) {
								append_buble = false;
								toAdd = toAdd.subSequence(3, toAdd.length());
							}
						} catch (Exception e) {
						}

						if (toAdd.charAt(toAdd.length()-1) == '\n') {
							toAdd = toAdd.subSequence(0, toAdd.length()-1);
							if (Utils.isEmptyOrBlank(toAdd))
								return;
							bubblesBreak(tabId);
						}

						// add to prev answer
						if (ta.bubleLastShown == 2) {
							SpannableStringBuilder sb=new SpannableStringBuilder();
							if (append_buble) {
								sb.append(ta.bubleLastAnswerTextView.getText());
								sb.append('\n');
							}
							sb.append(toAdd);
					    	ta.bubleLastAnswerTextView.setText(sb);
						}
						else {
							LayoutInflater inflater =
					    		    (LayoutInflater)getSystemService( Context.LAYOUT_INFLATER_SERVICE );

					    	LinearLayout layout;
				    		layout = (LinearLayout) inflater.inflate(R.layout.buble_answer_flat, null);
					    	ta.bubleLastAnswerTextView = (TextView) layout.findViewById(R.id.textViewBubleAnswer);
					    	ta.bubleLastAnswerTextView.setText(toAdd);
					    	layout.setTag(R.string.tag_bubble_options,(root instanceof IBubbleContent)?root:null);

					    	ViewGroup dialogs = this.getTabDialogView(tabId, false);

					    	Object last_query = null;
					    	if (bublesQuery.size() > 0)
					    		last_query = bublesQuery.get(bublesQuery.size()-1);
					    	layout.setTag(R.string.tag_query, last_query);

					        dialogs.addView(layout);
					        bublesAnswer.add(layout);
					        registerForContextMenu(layout);

					        ta.bubleLastShown = 2; // answer!
						}
						
						if (nb!=null&&nb.shouldShowNewBubbleAfter()) bubblesBreak(tabId);

						scrollDownAllDialogs();
    }

    // breaks bubles
    // Guarantees next buble will shown in other buble
    public void bubblesBreak(String tabId) {
    	getTabAttr(tabId).bubleLastShown = 0;
    }

	private void bublesUpdateRotation() {//imp
		if (!Config.bubles || mainView == null || bottomPanel == null)
			return;
        try {
		if (inLandscape) {
			View dialogView = getMainTabDialogView();
			dialogView.setPadding(0, 0, 0, 0);
		}
		else {
			View dialogView = getMainTabDialogView();
			if (dialogView!=null) {
				int h = 0;
				ViewGroup.LayoutParams bp = bottomPanel.getLayoutParams();
				h = bp.height;
				if (h < 10)
					h = 180;
				dialogView.setPadding(0, 0, 0, h);
			}
		}
        } catch (Throwable t) {
        	t.printStackTrace();
        }
	}

	public View getRootView() {
		return mainView;
	}

}