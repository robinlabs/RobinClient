package com.magnifis.parking.feed;

import com.magnifis.parking.App;
import com.magnifis.parking.Output;
import com.magnifis.parking.model.audioburst.ABFeed;
import com.magnifis.parking.model.audioburst.Burst;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;

import android.content.Context;

public class AudioburstFeedController implements IFeed {
	
	final private ABFeed abFeed;
	final private Burst bursts[] ;

	public AudioburstFeedController(Context activity, ABFeed feed) {
		abFeed=feed;
		bursts=abFeed.getBursts();
	}
	
	int currentBurstNo=-1;
	
	void playOne(Burst b) {
		playOne(b, false);
	}
	
	void playOne(Burst b, final boolean fContinue) {
		String title=b.getTitle();
		boolean anyTitle=!Utils.isEmptyOrBlank(title);
		if (anyTitle) {
			Output.sayAndShow(
  			  App.self,
  			  new MyTTS.Wrapper(title+'\n').setShowInNewBubble(true), 
  			  false
  		    );	            					
		}	
		String alert=b.getDetails().getText();
		if (Utils.isEmptyOrBlank(alert)) return;
		alert= "\n" +  alert;              				    
		Output.sayAndShow(
		  App.self,
		  new MyTTS.PlayableUrl(alert, b.getAudioURL()) {
			@Override
			public void onSaid(boolean fAborted, boolean fByMenu) {
				super.onSaid(fAborted,fByMenu);
				if (fContinue&&!fAborted&&hasNext()) playOne(bursts[++currentBurstNo], true);
			}
		  }.setShowInNewBubble(!anyTitle), 
		  false
		);		
	}

	@Override
	public void readAgain() {
	  if (currentBurstNo>=0) playOne(bursts[currentBurstNo]);		
	}

	@Override
	public void readDetailed() {
	   if (currentBurstNo>=0) playOne(bursts[currentBurstNo]);	
	}

	@Override
	public void readPrevious() {
      if (currentBurstNo>0) playOne(bursts[--currentBurstNo]); 
	}

	public boolean hasNext() {
	  return (bursts!=null) && (currentBurstNo<bursts.length-1);		
	}
	
	@Override
	public void readNext() {
      if (hasNext()) {
    	 playOne(bursts[++currentBurstNo]); 
      } else 
  		Output.sayAndShow(
  		   App.self,
  		   "There are not more audio bursts",
  		   false
  	    );
	}

	@Override
	public void coutinueReading() {
	   while (hasNext()) readNext();
	}
	
	public void readAll() {
      if (hasNext()) {
    	  playOne(bursts[++currentBurstNo], true); 
      }  else	
    	Output.sayAndShow(
    	   App.self,
    	   "There are not any audio bursts",
    	   false
    	 );
	}
	
}
