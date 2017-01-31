package com.magnifis.parking.twitter;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.AccountSettings;
import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import android.util.Log;
import android.widget.Toast;

import com.magnifis.parking.App;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.Output;
import com.magnifis.parking.MultipleEventHandler.EventSource;
import com.magnifis.parking.R;
import com.magnifis.parking.SuccessFailure;
import com.magnifis.parking.feed.MessageFeedController;
import com.magnifis.parking.messaging.Message;
import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.QueryInterpretation;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.toast.ToastController;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.SmartDateFormatter;
import static com.magnifis.parking.VoiceIO.*;

import static com.magnifis.parking.utils.Utils.*;

public class TwitterPlayer extends MessageFeedController {
  static final String TAG=TwitterPlayer.class.getSimpleName();
	
  private static final String 
  consumerKey="WZe8SYfO1yasE6VCw8zufg",
  consumerSecret="jYIfGRAxDUy5t7zpjldFoGRk2IS5AkchPDEZDIkjks";
  
  // controller begin
  
  @Override
  public String getLastReadId() {
	 Long id=getLastTweetID();
	 return id==null?null:id.toString();
  }

  @Override
  public boolean canShare() { return true; }

  @Override
  public boolean canPost() { return true; }

// controller end

TwitterWrapper tw=null;
  
  static WeakReference<TwitterPlayer> self=null;
  
  Status lastTweet=null;
  
  public Long getLastTweetID() {
	  return lastTweet==null?null:lastTweet.getId();
  }
  
  public static void resetConnection() {
	 if (self==null) return;
	 TwitterPlayer twp=self.get();
	 if (twp==null) {
		 TwitterWrapper.resetPreferences(App.self);
	 } else {
		twp.tw.resetPreferences();
		twp.tw.resetEngine();
	 }
  }
  
  public TwitterPlayer(MainActivity ma) {
	 super(ma);
	 tw=new TwitterWrapper(MainActivity.get(),consumerKey,consumerSecret);
	 self=new WeakReference<TwitterPlayer>(this);
  }
  
  boolean qAbort=false;
  
  public void abort() {
	tw.abortDialog();
	if (tc!=null) tc.abort();
	qAbort=true;
  }
 
  TwitterStatusFormatter tsf=new TwitterStatusFormatter();
  
  public void tweet(
    final String s, final DoublePoint loc, 
	final Runnable afterThat,
	final Runnable geoDisabled
  ) {
	  if (tw.isBusy()) return;
	  final EventSource es=MainActivity.get().showProgress();
	  
	  tw.consume(
		 new TwitterWrapper.Consumer() {
			@Override
			public void onReady(Twitter tw, TwitterWrapper tww) {
			    try {
			    	StatusUpdate up=new StatusUpdate(s);
			    	if (loc!=null) {
			    		up.setLocation(new GeoLocation(loc.getLat(),loc.getLon()));
			    		up.setDisplayCoordinates(true);
			    		if (geoDisabled!=null) try {
			    			AccountSettings as=tw.getAccountSettings();
			    			if (!as.isGeoEnabled())  geoDisabled.run();
			    		} catch(Throwable t) {}
			    	}
			    	tw.updateStatus(up);
			    	if (afterThat!=null) afterThat.run();
			    } catch (TwitterException e) {
			       sayFromGui( R.string.P_SOMETHING_WENT_WRONG);
				   e.printStackTrace();
			    }
			    es.fireEventFromGui(MainActivity.get());
		    }

			@Override
			public void onFailure() {
				es.fireEventFromGui(MainActivity.get());
			}
		        	  
		 }
	  );
	  
  }
  
  public void retweetLast(final Understanding u) {
	  if (tw.isBusy()) return;
	  if (lastTweet==null) {
         MyTTS.speakText(R.string.P_NOTHING_TO_RETWEET);
		 return;
	  }
	  
	  MyTTS.speakText(u.getQueryInterpretation());
	  QueryInterpretation qi=u.getQueryInterpretation();
	  if (qi!=null) qi.sayAndShow(MainActivity.get());
		  
	  final EventSource es=MainActivity.get().showProgress();
	  
	  
	  tw.consume(
		 new TwitterWrapper.Consumer() {
			@Override
			public void onReady(Twitter tw, TwitterWrapper tww) {
			    try {
     			   tw.retweetStatus(getLastTweetID());
				   sayFromGui("We have retweetted: ");
				   playOneTweet(lastTweet);
			    } catch (TwitterException e) {
			       sayFromGui( R.string.P_SOMETHING_WENT_WRONG);
				   e.printStackTrace();
			    }
			    es.fireEventFromGui(MainActivity.get());
		    }

			@Override
			public void onFailure() {
				es.fireEventFromGui(MainActivity.get());
			}
		        	  
		 }
	  );
  }
 
  public void play(Understanding u) {
	  if (tw.isBusy()) return;
	  lastTweet=null;
	  tweetList=null;
	  _playMore(u);
  }
  
  public void playMore(final Understanding u) {
	  if (tw.isBusy()) return;
	  if (lastTweet==null) {
		  MyTTS.speakText(R.string.P_NO_TWITTER_CONTEXT);
		  return;
	  }
	  _playMore(u);
  }
  
  ResponseList<Status> tweetList=null;
 
  final static int PAGE_SIZE=10;
  
  ToastController tc=null;
  
  protected void _playMore(final Understanding u) {
	 if (tw.isBusy()) return;
	 qAbort=false;
	 final EventSource ess=MainActivity.get().showProgress();
	 tw.consume(
	          new TwitterWrapper.Consumer() {
	        	  
	  			@Override
	  			public void onReady(Twitter tw, TwitterWrapper tww) {
	  				Log.d(TAG, " consume");
	  				if (qAbort) return;
	  				
	  				long lastGotTwId=-1;
	  				
	  				if (!isEmpty(tweetList)&&(lastTweet!=null)) {
	  					lastGotTwId=tweetList.get(tweetList.size()-1).getId();
	  					while (!tweetList.isEmpty()) {
	  						if (tweetList.get(0).getId()<=getLastTweetID().longValue()) break;
	  						tweetList.remove(0);
	  					}
	  					if (tweetList.get(0).getId()==getLastTweetID().longValue()) tweetList.remove(0);	  				
	  			    }
	  				
	  				final boolean fromBegin=lastGotTwId==-1;
	  				
	  				MainActivity.get().runOnUiThread(
	  						new Runnable() {
	  							@Override
	  							public void run() {
	  								if (fromBegin||(tweetList.size()<PAGE_SIZE)) {
	  									Output.sayAndShow(
	  											MainActivity.get(),
	  											u.getQueryInterpretation().getToShow(), 
	  											u.getQueryInterpretation().getToSay(), 
	  											u.getQuery(), false
	  									);
	  								}
	  							}	  					        	  
	  						}
	  				);
	  				
	  				
	  				try {	  					
	  					if (fromBegin) {
	  					  tweetList=tw.getHomeTimeline(); 
	  					} else {
	  					  if (tweetList.size()<PAGE_SIZE) {
	  						Log.d(TAG,"onToSpeak  -- yet one page");
	  						Paging pg=new Paging();
	  						pg.setMaxId(lastGotTwId-1);
	  						ResponseList<Status> lst=tw.getHomeTimeline(pg);
	  						if (lst!=null) tweetList.addAll(lst);
	  					  }
	  					}
	  					if (isEmpty(tweetList)) {
	  						tweetList=null;
	  						MyTTS.speakText(R.string.P_NO_TWEETS);
	  						return;
	  					}

	  					ess.fireEventFromGui(MainActivity.get());
	  					if (tweetList!=null) {
	  						if (qAbort) return;
	  					    int count=0;
	  						for (final Status st:tweetList) {
	  				   	        if (qAbort) {
	  				   	           if (tc!=null) tc.abort();
	  				   	           return;
	  				   	        }
	  				   	        if (count>=PAGE_SIZE) break;
	  				   	        ++count;
	  				   	        /*
	  					        final String forSpeach=tsf.format(st,true),
	  					        	         forToast=tsf.format(st,false);
	  					        Log.d(TAG,forSpeach);*/
	  					        playOneTweet(st,true);
	  					        //MainActivity.speakText(oneTwit);
	  					    }
	  					}
	  					
	  				} catch (TwitterException e) {
	  					e.printStackTrace();
	  				}
	  			}

				@Override
				public void onFailure() {
					ess.fireEventFromGui(MainActivity.get());
				}
	  		  }

	 );
	 
  }
  
  private void playOneTweet(Status st) {
	  playOneTweet(st,false);	  
  }
  
  private void playOneTweet(final Status st, final boolean updateLastTweet) {
        final String forSpeach=tsf.format(st,true),
     	         forToast=tsf.format(st,false);
     Log.d(TAG,forSpeach);
     MainActivity.get().runOnUiThread(
       new Runnable() {

		@Override
		public void run() {
		  MyTTS.OnStringSpeakListener sw=new MyTTS.OnStringSpeakListener() {
			@Override
			public String toString() {
			  return forSpeach;
			}

			@Override
			public void onSaid(boolean fAborted) {
				tc.abort();
				MyTTS.switchVoicesIfCan(); 
			}

			@Override
			public void onToSpeak() {
				if (updateLastTweet) lastTweet=st;
				Log.d(TAG,"onToSpeak #"+getLastTweetID());
				tc=new ToastController(MainActivity.get(),forToast,true);
			}
     	
           };
			
		   MyTTS.speakText( sw);
           //MyTTS.speakText(forSpeach);
		   //MainActivity.speakText(oneTwit);
			
		}
     	  
       }
     );
		 
  }

}
