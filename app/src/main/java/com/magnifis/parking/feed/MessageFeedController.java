package com.magnifis.parking.feed;

import static com.magnifis.parking.tts.MyTTS.speakText;
import static com.magnifis.parking.utils.Utils.isEmpty;

import java.io.Serializable;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.magnifis.parking.App;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.IFeedContollerHolder;
import com.magnifis.parking.MultipleEventHandler;
import com.magnifis.parking.ProgressIndicatorHolder;
import com.magnifis.parking.R;
import com.magnifis.parking.SuccessFailure;
import com.magnifis.parking.VoiceIO;
import com.magnifis.parking.MultipleEventHandler.EventSource;
import com.magnifis.parking.R.string;
import com.magnifis.parking.messaging.Message;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.tts.MyTTS;

public class MessageFeedController implements IFeed {
	
	private static final String TAG = MessageFeedController.class.getSimpleName();
	protected Context context;
	
	protected MessageFeedController(Context activity) {
	   this.context=activity;
	}
	
	
	protected String lastReadId=null;

	public String getLastReadId() {
		return lastReadId;
	}
	
	public int getPageSize() {
	   return 5;
	}
	
	public String getThingName() {
	   return App.self.getString(R.string.messagefeedcontroller_getthingname);
	}
	
	public String getThingsName() {
	    return App.self.getString(R.string.messagefeedcontroller_getthingsname);
	}
	
   public void getN(int N, String sinceId, boolean fNew, boolean exclId, SuccessFailure<List<Message>> handler) { 
	   getN(N, sinceId, fNew, exclId, false, handler);	   
   }
   
   public void getN(int N, boolean fNew, SuccessFailure<List<Message>> handler) { 
	   getN(N, null, fNew, false, handler);
   }
   
   public void getSome( boolean fNew, SuccessFailure<List<Message>> handler) { 
	  getSome(null, fNew, handler);
   }  
   
   // should be called from bg
   public void replyBg(Understanding u) {}
   
   public void post(Message msg, SuccessFailure<Message> handler) {
	   
   }
   
   /////////////
   
   public void play(List<Message> ms) {
	   play(ms,false,null);
   };
   

   public void play(List<Message> ms, boolean markAsRead, String sayBefore) {}
	
   public void rewind() {
	   lastReadId=null;
   }
   /**
    * with the same service
    *
    */
    public void shareLastRead(SuccessFailure<Message> handler) {}
   
    public boolean canShare() { return false;  }
    public boolean canPost() { return false;  }
   
 
	public void getSome(String sinceId, boolean fNew, SuccessFailure<List<Message>> handler) {
		getN(getPageSize(), sinceId, fNew, true, handler);
	}
	
	protected boolean fAdvance=false, fNew=false;
	
	public void reset() {
		fAdvance=false; fNew=false;
		lastReadId=null;
	}

	public void coutinueReading() {
		fAdvance=true;
		readSome(lastReadId, false, App.self.getString(R.string.messagefeedcontroller_read_more)+" "+getThingsName()+" "); 
	}
	

	public void beginReading(Context ctx, boolean fNew, final String qi) {
		fAdvance=true;
		this.context=ctx; // TODO: make it content independent
		if (ctx instanceof IFeedContollerHolder)
			  ((IFeedContollerHolder)ctx).setFeedController(MessageFeedController.this);
		readSome(null, fNew, qi);
	}
	

	public void readPrevious() {
		fAdvance=false;
		read(-1, lastReadId, false, true, App.self.getString(R.string.messagefeedcontroller_read_previous)+" "+getThingName()+" "+App.self.getString(R.string.messagefeedcontroller_is));
	}
	

	public void readNext() {
		Log.i(TAG, "Skipping to next item..."); 
		read(1, lastReadId, false, true, App.self.getString(R.string.messagefeedcontroller_read_next)+" "+getThingName()+" "+App.self.getString(R.string.messagefeedcontroller_is));
	}
	
	protected boolean detailedMode=false;

	public void readAgain() {
		fAdvance=false;
		if (detailedMode)
		  this.readDetailed();
		else
		  read(1, lastReadId, false, false, App.self.getString(R.string.messagefeedcontroller_repeating));
	}
	

	public void readDetailed() {
		fAdvance=false;
		read(1, lastReadId, false, false, App.self.getString(R.string.messagefeedcontroller_the) + " " + getThingName() + " " + App.self.getString(R.string.messagefeedcontroller_is));
	}
	

	public void readSome(String sinceId, boolean fNew, final String qi) {
		read(getPageSize(), sinceId, fNew, true, qi);
	}
	

	public void read(final int N, String sinceId, final boolean fNew, boolean fExcl, final String qi) {
		final MultipleEventHandler.EventSource es=
			(context instanceof ProgressIndicatorHolder)
				  ?((ProgressIndicatorHolder)context).showProgress()
				  :null
			;
		detailedMode=false;
		getN(
				N,
				sinceId,
				fNew,
				fExcl,
				new SuccessFailure<List<Message>>() {

					@Override
					public void onSuccess(List<Message> ms) {
						if (es!=null) es.fireEvent();
						if (isEmpty(ms)) {
							VoiceIO.sayAndShow(
							   new MyTTS.Wrapper(
							   App.self.getString(R.string.messagefeedcontroller_there_are_no) + " "
									+ (fNew?"":App.self.getString(N>0?R.string.messagefeedcontroller_more:R.string.messagefeedcontroller_previous)) + " "
									+ App.self.getString(R.string.messagefeedcontroller_messages) + " "
									+ App.self.getString(R.string.messagefeedcontroller_to_read)
							   ).setShowInASeparateBubble()
							);
						} else {
							/*
						  if (context instanceof MessageFeedContollerHolder)
						     ((MessageFeedContollerHolder)context).setMessageFeedController(MessageFeedController.this);
						     */
						  boolean old=ms.get(0).isRead();
						  String s=null;
						  if (qi!=null) {
							  if (fNew && old) {
                                  VoiceIO.sayAndShow(
                                          new MyTTS.Wrapper(
								    App.self.getString(R.string.messagefeedcontroller_there_are_no)
									+ " "
									+ App.self.getString(R.string.messagefeedcontroller_new)
									+ " "
									+ App.self.getString(R.string.messagefeedcontroller_messages)));
									//+ App.self.getString(R.string.messagefeedcontroller_reading_older);
                                  return;
							  }
						  }
						  if (fNew) MessageFeedController.this.fNew=!old;
						  play(ms,!old,s);
						}
						VoiceIO.condListenAfterTheSpeech();
					}

					@Override
					public void onFailure() {
						if (es!=null) es.fireEvent();
						speakText(R.string.P_SOMETHING_WENT_WRONG);
						VoiceIO.condListenAfterTheSpeech();
					}

					@Override
					public void onCancel() {
						es.fireEvent();
						VoiceIO.condListenAfterTheSpeech();
					}
					
					
				}
		);		
	}
	
	public boolean markAsRead(Message m) {
		return markAsRead(m.getId());
	}

	public boolean markAsRead(String key) {
		return false;
	}

	public void getN(int N, String sinceId, boolean fNew, boolean exclId,
			boolean fetchBody, SuccessFailure<List<Message>> handler) 
	{
	
	}

   
}
