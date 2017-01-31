package com.magnifis.parking.suzie;

import static com.magnifis.parking.Launchers.startNestedActivity;
import static com.magnifis.parking.VoiceIO.listenAfterTheSpeech;
import static com.magnifis.parking.VoiceIO.sayAndShow;

import java.util.List;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.PendingIntent.CanceledException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;

import com.magnifis.parking.App;
import com.magnifis.parking.Launchers;
import com.magnifis.parking.UnderstandingProcessorBase;
import com.magnifis.parking.UnderstandingProcessor;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.MultipleEventHandler;
import com.magnifis.parking.VoiceIO;
import com.magnifis.parking.Output;
import com.magnifis.parking.Props;
import com.magnifis.parking.R;
import com.magnifis.parking.VR;
import com.magnifis.parking.cmd.GoogleTranslateFetcher;
import com.magnifis.parking.cmd.i.LocalCommandHandler;
import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.MagReply;
import com.magnifis.parking.model.QueryInterpretation;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;

public class SuzieUnderstandigProcessor extends UnderstandingProcessor {
	
	final static String TAG=SuzieUnderstandigProcessor.class.getSimpleName();
	
	final protected SuzieService service;
	final protected SuziePopup suziePopup;
	
	public SuzieUnderstandigProcessor(SuzieService context, MultipleEventHandler.EventSource es) {
       super(context,es);
       service=context;
       suziePopup=service.getSuziePopup();
		Log.d(TAG, "SuzieUnderstandigProcessor.create");
	}
	
	DoublePoint navigateTo=null;

	@Override
	public MagReply continueUnderstanding(final MagReply reply,Understanding u, int cmd) {
	   Log.d(TAG, "SuzieUnderstandigProcessor.consumeUnderstanding");
		 
	    topActivity=Utils.getTopActivity();


		Log.d(TAG, "SUZIE ON ACTIVITY: "+topActivity.getPackageName()+" cmd: "+cmd);
		
		// navigation: waze, google 
	    if (topActivity.getPackageName().startsWith("com.waze")
	    		|| topActivity.getPackageName().contains(".maps")) 
	    	switch(cmd) {
	    	case Understanding.CMD_SEARCH:
	    	case Understanding.CMD_MAP: 
	    	case Understanding.CMD_PARKING: 
	    	case Understanding.CMD_ROUTE:
			if (u.calculateDestinationLocation((DoublePoint)null)) 
			  navigateTo=u.getDestinationLocation();
			else {
			  u.calculateOriginLocation((DoublePoint)null);
			  navigateTo=u.getOriginLocation();
			}
			if (navigateTo==null)
			   Log.d(TAG, "SUZIE: POINT EMPTY"); 
			else
				return reply;
	    }
	        
	    if (canBeHandleOutOfMainActivity(cmd)) return super.continueUnderstanding(reply, u, cmd); else switch(cmd) {
	    case Understanding.CMD_NO:
	    case Understanding.CMD_YES:
	    case Understanding.CMD_DO_IT:
	    	fallBack=true;
	    	return reply;
	    }

	    
		Log.d(TAG, "SEND RESULT TO MAIN ACTIVITY");
		reply.setProcessedByHandlerInFg(true);
		// prepare intent for main packageName
		Intent it=new Intent(MainActivity.INTERPRET_UNDERSTANDING);
		it.setClass(App.self, MainActivity.class);
		it.putExtra(MainActivity.EXTRA_UNDERSTANDING, u);
		
		Utils.startActivityFromNowhere(it);
		
	    return reply;
	}
	
	boolean fallBack=false;

	@Override
	protected void onPostExecute(MagReply reply) {
		if (!(reply==null||reply.noUnderstanding()||reply.isProcessedByHandlerInFg())) {
			Understanding u=reply.getUnderstanding();

            //is this a user who does not no what he wants? and just saying hello?
            if(Understanding.CMD_HELLO==u.getCommandCode()){
                sayAndShow(App.self.robin().suggestSomething());
                listenAfterTheSpeech();
                //super.onPostExecute(reply);
                return;
            }

            //is robin waiting for a response?
            if(App.self.robin().expectedResponse!=null){
                boolean success = App.self.robin().handleResponse(u.getCommandCode());
                if (success) {
                    //super.onPostExecute(reply);
                    return;
                }
            }

            if (fallBack) {
				QueryInterpretation qi=u.getQueryInterpretation();
				if (qi!=null) qi.sayAndShow(context);
				VoiceIO.condListenAfterTheSpeech();
				reply.setProcessedByHandlerInFg(true);				
			} else
			if (navigateTo!=null) {
				Log.d(TAG, "SUZIE: LAUNCHING NAVIGATION TO "+navigateTo);

				Intent it=new Intent(Intent.ACTION_VIEW);
				it.setPackage(topActivity.getPackageName());
				if (topActivity.getPackageName().contains(".maps"))
					it.setData(Uri.parse("geo:0,0?q="+navigateTo));
				else
					it.setData(Uri.parse("geo:"+navigateTo));			   

				VoiceIO.sayAndShow(u.getQueryInterpretation()); 
				Utils.startActivityFromNowhere(it);
				VoiceIO.condListenAfterTheSpeech();
				reply.setProcessedByHandlerInFg(true);
			} else switch (u.getCommandCode()) {
		    case Understanding.CMD_FUCK:
			    suziePopup.fuckAnimation();
			    break;
			case Understanding.CMD_JOKE:
			case Understanding.CMD_JOKE_SEXY:
				suziePopup.jokeAnimation();
				break;	
			case Understanding.CMD_HELLO:
				suziePopup.helloAnimation();
				break;	
			case Understanding.CMD_HOW_ARE_YOU:
				suziePopup.howareyouAnimation();
				break;	
			}
		}	
		
	    super.onPostExecute(reply);
  }
	
	
}
