package com.magnifis.parking.cmd;

import android.content.Context;

import com.magnifis.parking.App;
import com.magnifis.parking.MultipleEventHandler;
import com.magnifis.parking.MultipleEventHandler.EventSource;
import com.magnifis.parking.Output;
import com.magnifis.parking.ProgressIndicatorHolder;
import com.magnifis.parking.R;
import com.magnifis.parking.VR;
import com.magnifis.parking.VoiceIO;
import com.magnifis.parking.XMLFetcher;
import com.magnifis.parking.Xml;
import com.magnifis.parking.cmd.etc.CmdHandlerHolder;
import com.magnifis.parking.cmd.i.ClientStateInformer;
import com.magnifis.parking.cmd.i.LocalCommandHandler;
import com.magnifis.parking.cmd.i.MagReplyHandler;
import com.magnifis.parking.model.LearnedAnswer;
import com.magnifis.parking.model.MagReply;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.model.UnderstandingStatus;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;

import org.w3c.dom.Element;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.magnifis.parking.RequestFormers.createMagnifisUnderstandingRqUrl;
import static com.magnifis.parking.VoiceIO.fireOpes;
import static com.magnifis.parking.VoiceIO.listenAfterTheSpeech;
import static com.magnifis.parking.VoiceIO.sayAndShow;
import static com.magnifis.parking.model.Understanding.CMD_DO_IT;
import static com.magnifis.parking.model.Understanding.CMD_FUCK;
import static com.magnifis.parking.model.Understanding.CMD_HELLO;
import static com.magnifis.parking.model.Understanding.CMD_HOW_ARE_YOU;
import static com.magnifis.parking.model.Understanding.CMD_NOP;
import static com.magnifis.parking.model.Understanding.CMD_TEACH;
import static com.magnifis.parking.model.Understanding.CMD_UNKNOWN;
import static com.magnifis.parking.model.Understanding.CMD_YES;
import static com.magnifis.parking.tts.MyTTS.speakText;
import static com.magnifis.parking.utils.Utils.isEmpty;

public class Teaching extends CmdHandlerBase implements LocalCommandHandler, MagReplyHandler, ClientStateInformer {
	
	public Teaching(Context ma) {
		super(ma);
	}

	private static final int TEACH_NONE = 0;
	private static final int TEACH_QUESTION = 10;
	private static final int TEACH_QUESTION_CONFIRM = 11;
	private static final int TEACH_QUESTION_TYPE = 12;
	private static final int TEACH_ANSWER = 20;
	private static final int TEACH_ANSWER_CONFIRM = 21;
	private static final int TEACH_ANSWER_TYPE = 22;
	private static final int TEACH_TEST_QUERY = 30;

	static class State {
		int teachState = TEACH_QUESTION;
		String teachQuestion = null;
		String teachAnswer = null;		
		int teachAddSay = 1;
	}
	
	State state = new State();
	
	@Override
	public void abort(int fAbort) {
       cancelTeaching();		
	}


	public void _abort() {
		state.teachState = TEACH_NONE;
		CmdHandlerHolder.removeCommandHandler(this);	    	
	}
	
	private String getEmail() {
		String s = App.self.getGmailAccountName();
		if (isEmpty(s))
			return "";
		else
			return s;
	}

	void cancelTeaching() {
		_abort();
		speakText(R.string.P_teach_cancel);
		
		if (isEmpty(state.teachQuestion))
			return;
		
		StringBuilder url = new StringBuilder(App.self.getString(R.string.teaching_url));
		try {
			url.append("&t=");
			url.append(URLEncoder.encode(App.self.android_id));
			url.append("&q=");
			url.append(URLEncoder.encode(state.teachQuestion,"UTF-8"));
			url.append("&a=");
			// if teaching not finished, answer must be empty!
			// url.append(URLEncoder.encode(state.teachAnswer,"UTF-8"));
			url.append("&d=");
			url.append(URLEncoder.encode(getEmail(), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		final String goto_url = url.toString();

		new Thread("save teaching result") {
			@Override
			public void run() {
				try {
					HttpURLConnection  x = (HttpURLConnection)new URL(goto_url).openConnection();
				    x.getInputStream();
				    x.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
		VoiceIO.fireOpes();
	}
	
	void finishTeaching() {
		_abort();
		
		VR vr = VR.get();
		if (vr != null) {
			LearnedAnswer qa = new LearnedAnswer();
		    qa.setQuestion(state.teachQuestion);
		    qa.setAnswer(state.teachAnswer);
		    qa.setSay(state.teachAddSay);
			vr.teachAdd(qa);
		}
		speakText(R.string.P_teach_ok);

		VoiceIO.fireOpes();
	}
	
    @Override
	public boolean onVoiceInput(List<String> matches, boolean partial) {
		
    	if (partial)
    		return true;
    	
		// empty voice - stop teaching !
		if (matches == null) {
			cancelTeaching();
			return true;
		}
		
		// waiting for confirmation user - working through server !
		if (state.teachState == TEACH_QUESTION_CONFIRM)
			return false;
		
		// waiting for confirmation user - working through server !
		if (state.teachState == TEACH_ANSWER_CONFIRM)
			return false;
		
		// received voice - do something....
		String selectedFraze = null;
		for (String s : matches) {
			if (s.length() > 0)
			{
				selectedFraze = s;
				break;
			}
		}
		selectedFraze = selectedFraze.replaceAll("  ", " ");
		selectedFraze = selectedFraze.toLowerCase();
		
		if (selectedFraze.length() < 1)
		{
			state.teachState = TEACH_NONE;
			return true;
		}
		
		if (state.teachState == TEACH_QUESTION)
		{
			state.teachQuestion = selectedFraze;
			state.teachState = TEACH_QUESTION_CONFIRM;
			UnderstandingStatus.get().waitingForConfirmation=true;
			askForSomethingConfirmation(R.string.P_teach_q_confirm, selectedFraze);
			return true;
		}

		if (state.teachState == TEACH_QUESTION_CONFIRM)
		{
			if (selectedFraze.equals("yes")) {
				speakAndListen(App.self.getString(R.string.P_teach_a_say)+" "+
						state.teachQuestion+App.self.getString(R.string.P_teach_a_say2), TEACH_ANSWER);							
			}
			else {
				state.teachState = TEACH_QUESTION_TYPE;
				speakText(R.string.P_teach_q_type);
				
				Utils.showTextInputDialog(context, R.string.P_teach_q_type, state.teachQuestion, 
					new Utils.TextInputDialogResult() {
					
						boolean bOK = false;
						
						@Override
						public void onDialogOK(String result) {
		  					bOK = true;
							state.teachQuestion = result;
							speakAndListen(App.self.getString(R.string.P_teach_a_say)+" "+
									state.teachQuestion+App.self.getString(R.string.P_teach_a_say2), TEACH_ANSWER);
							
						}
						@Override
						public void onDialogClose() {
							if (!bOK)
								cancelTeaching();
						}
					}
				);
			}

			return true;
		}
		
		if (state.teachState == TEACH_ANSWER) {
			state.teachAnswer = selectedFraze;
			state.teachState = TEACH_ANSWER_CONFIRM;
			UnderstandingStatus.get().waitingForConfirmation=true;
			askForSomethingConfirmation(R.string.P_teach_a_confirm, selectedFraze);
			return true;
		}
		
		if (state.teachState == TEACH_ANSWER_CONFIRM)
		{
			if (selectedFraze.equals("yes")) {
				TestAnswer();
				return true;				
			}
			else {
				state.teachState = TEACH_ANSWER_TYPE;
				speakText(R.string.P_teach_a_type);
				Utils.showTextInputDialog(context, R.string.P_teach_a_type, state.teachAnswer, 
						new Utils.TextInputDialogResult() {
					
							boolean bOK = false;
							
							@Override
							public void onDialogOK(String result) {
								bOK = true;
								state.teachAnswer = result;
								TestAnswer();
							}
							@Override
							public void onDialogClose() {
								if (!bOK)
									cancelTeaching();
							}
						}
					);
			}
			return true;
		}
		
		return true;
	}

	void speakAndListen(String s, int newState) {

		//state.teachState = TEACH_NONE;
		
		MyTTS.abort();
		
		sayAndShow(s);
		
		state.teachState = newState;
		
		listenAfterTheSpeech();
	}

	@Override
	public boolean handleReplyInBg(MagReply reply) {
		
		switch(reply.getUnderstanding().getCommandCode()) {
		case Understanding.CMD_TEACH:
			return true;
		}
		
		// waiting for confirmation user - working through server !
		if (state.teachState == TEACH_QUESTION_CONFIRM)
			return true;

		// waiting for confirmation user - working through server !
		if (state.teachState == TEACH_ANSWER_CONFIRM)
			return true;
		
		// waiting for request test - working through server !
		if (state.teachState == TEACH_TEST_QUERY)
			return true;
		
		return false;		
	}

	@Override
	public boolean handleReplyInUI(MagReply reply) {
		
		// just start?
		if (state.teachState == TEACH_QUESTION) {
			if (reply.getCommandCode()==CMD_TEACH) {
				
				CmdHandlerHolder.setCommandHandler(this);
				VR.useReplaceTeaching = false;
				Output.sayAndShow(context, 
						App.self.getString(R.string.P_teach_q_say),
						App.self.getString(R.string.P_teach_q_say), false);
				listenAfterTheSpeech();
				return true;
			}
		}
		
		// waiting for confirmation user - working through server !
		if (state.teachState == TEACH_QUESTION_CONFIRM) {
			if ((reply.getUnderstanding().getCommandCode() == CMD_YES)
					|| (reply.getUnderstanding().getCommandCode() == CMD_DO_IT))
				
				speakAndListen(App.self.getString(R.string.P_teach_a_say)+" "+
						state.teachQuestion+App.self.getString(R.string.P_teach_a_say2), TEACH_ANSWER);							

			else {
				
				state.teachState = TEACH_QUESTION_TYPE;
				speakText(R.string.P_teach_q_type);
				
				Utils.showTextInputDialog(context, R.string.P_teach_q_type, state.teachQuestion, 
					new Utils.TextInputDialogResult() {
					
						boolean bOK = false;
						
						@Override
						public void onDialogOK(String result) {
		  					bOK = true;
							state.teachQuestion = result;
							speakAndListen(App.self.getString(R.string.P_teach_a_say)+" "+
									state.teachQuestion+App.self.getString(R.string.P_teach_a_say2), TEACH_ANSWER);							
						}
						@Override
						public void onDialogClose() {
							if (!bOK)
								cancelTeaching();
						}
				});
			}
			
			UnderstandingStatus.get().waitingForConfirmation=false;
			return true;
		}
		
		// waiting for confirmation user - working through server !
		if (state.teachState == TEACH_ANSWER_CONFIRM) {
			if ((reply.getUnderstanding().getCommandCode() == CMD_YES)
					|| (reply.getUnderstanding().getCommandCode() == CMD_DO_IT)) {

				TestAnswer();
			}			
			else {
				state.teachState = TEACH_ANSWER_TYPE;
				speakText(R.string.P_teach_a_type);
				Utils.showTextInputDialog(context, R.string.P_teach_a_type, state.teachAnswer, 
						new Utils.TextInputDialogResult() {
					
							boolean bOK = false;
							
							@Override
							public void onDialogOK(String result) {
								bOK = true;
								state.teachAnswer = result;
								TestAnswer();
							}
							@Override
							public void onDialogClose() {
								if (!bOK)
									cancelTeaching();
							}
						}
					);
			}

			UnderstandingStatus.get().waitingForConfirmation=false;
			return true;
		}
		
		// check command - working through server !
		if (state.teachState == TEACH_TEST_QUERY) {
			switch (reply.getUnderstanding().getCommandCode()) {
			//case CMD_PLAY: case CMD_ROUTE: case CMD_SEND: case CMD_MAP: case CMD_PARKING: case CMD_GAS: case CMD_TRANSLATE:
			//case CMD_CALL: case CMD_OPENAPP: case CMD_INFO: case CMD_CONTACTS: case CMD_READ: case CMD_WEATHER: case CMD_CLOSE_ALL:
            //case CMD_CLOSE_APP: case CMD_DAILY_UPDATE:
			//case CMD_SAY: case CMD_JOKE: case CMD_JOKE_SEXY: case CMD_QUOTE: case CMD_SWITCH_TO_RUSSIAN: case CMD_MY_PARKING:

            case CMD_FUCK: case CMD_HELLO: case CMD_HOW_ARE_YOU: case CMD_NOP: case CMD_UNKNOWN:
            {

                state.teachAddSay = 1;
                break;
            }

			default:
                state.teachAddSay = 0;
				break;
			}
					//, CMD_SAY, CMD_JOKE, CMD_
				//case CMD_SEARCH:
					//if (reply.getUnderstanding().get)
			
			finishTeaching();

			return true;
		}
		
		return false;
	}
	
	private void TestAnswer() {
		
		MyTTS.execAfterTheSpeech(
				  new Runnable() {
					@Override
					public void run() {
						state.teachState = TEACH_TEST_QUERY;
						
						ArrayList<String> matches = new ArrayList<String>();
						matches.add(state.teachAnswer);

						MultipleEventHandler.EventSource es = ((ProgressIndicatorHolder)context).showProgress();
						try {
							UnderstandingProcessor mFetcher = new UnderstandingProcessor(es);
							mFetcher.execute(createMagnifisUnderstandingRqUrl(context, matches), null, null);
						} catch (Throwable e) {
							es.fireEvent();
							e.printStackTrace();
						}
					}  
				  }		
			);
	}
	
	private void askForSomethingConfirmation(Object confirm, Object userText) {
		stateName=ClientStateInformer.SN_YES_NO;
		speakText(confirm);
		Output.sayAndShow(context, userText);
		speakText(R.string.P_is_that_right);
		listenAfterTheSpeech();
	}

	private String stateName=null;

	@Override
	public String getClientStateName() {
		return stateName;
	}
	
	public class UnderstandingProcessor extends XMLFetcher<MagReply>  {

		final protected MultipleEventHandler.EventSource es;
		
		public UnderstandingProcessor(EventSource es) {
			this.es = es;
		}		
		
		@Override
		protected void onCancelled() {
			fireOpes();
			Utils.runInMainUiThread(new Runnable() {

				@Override
				public void run() {
					es.fireEvent();
				}

			});
			super.onCancelled();
		}
		
		@Override
		protected MagReply consumeXmlData(Element root) {
			if (root!=null) try {
				return consumeUnderstanding(Xml.setPropertiesFrom(root, Understanding.class));
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected MagReply consumeData(Object o) {
			return consumeUnderstanding((Understanding) o);
		}
		
		public MagReply consumeUnderstanding(Understanding understanding) {
			if (understanding == null)
				return null;
			final MagReply reply = new MagReply();
			reply.setUnderstanding(understanding);
			handleReplyInBg(reply);
			return reply;
		}
		
		@Override
		protected void onPostExecute(MagReply reply) {
			handleReplyInUI(reply);
			es.fireEvent();
		}
		
	}
	
}
