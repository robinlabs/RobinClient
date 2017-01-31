package com.magnifis.parking.tts;

import java.util.HashMap;
import java.util.Locale;

import com.magnifis.parking.Log;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.VR;
import com.magnifis.parking.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;


public class AnTTS extends TTS implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
	
	final static String TAG=MyTTS.TAG+":"+AnTTS.class.getSimpleName();
	
	protected volatile TextToSpeech tts=null;
		
	public AnTTS(Context context) {
		Log.d(TAG,"AnTTS() ");
		tts=new TextToSpeech(context,this);
	}
	
	@Override
	public void speak(String s) {
		Log.d(TAG,"speak "+s);
		if (tts == null)
			return;
		stopped=false;

        HashMap<String, String> params = new HashMap<String, String>() {
            {
                put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "something");
                put(TextToSpeech.Engine.KEY_PARAM_STREAM, Integer.toString(VR.TtsAudioStream));
                put(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS, "true");
            }
        };

        tts.speak(s, TextToSpeech.QUEUE_ADD, params);
	}
	
	private boolean stopped=false;

	@Override
	public void stop() {
		Log.d(TAG,"stop");
		if (tts!=null) {
			boolean sp=!stopped&&tts.isSpeaking();
			stopped=true;
			tts.stop();
			if (sp) onAllCompleted();
		}
	}

	@Override
	public void shutdown() {
		Log.d(TAG,"shutdown");
		if (tts!=null) {
	      tts.shutdown();
	      tts=null;
		}
	}

	public void onInit(int status) {
		Log.d(TAG,"onInit");		
		if (tts == null)
			return;
		if  (status==TextToSpeech.SUCCESS) {
			Log.d(TAG,"onInit TextToSpeech.SUCCESS");
			//tts.setLanguage(Locale.ENGLISH);
            if (Utils.isAndroid5orAbove)
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {

                    }

                    @Override
                    public void onDone(String utteranceId) {
                        onAllCompleted();
                    }

                    @Override
                    public void onError(String utteranceId) {

                    }
                });
            else
                tts.setOnUtteranceCompletedListener(this);
			onInit();
		} else {
			onInitError();
		}
	}

	public void onUtteranceCompleted(String utteranceId) {
	  	  Log.d(TAG,"onUtteranceCompleted");
		 onAllCompleted();
	}

	@Override
	public String getLanguage() {
		if (tts != null)
			return tts.getLanguage().getLanguage();
		else
			return "";
	}
	
	public static boolean fineSystemRussian=false;

	@Override
	public boolean setLanguage(String lang) {
		boolean ok=super.setLanguage(lang);
		if ("ru".endsWith(lang)) fineSystemRussian=ok;
		if (!ok&&"en".equals(lang)) {
			Log.d(TAG, ".. forced english tts ability ..");
			return true;
		}
		return ok;
	}

	@Override
	public boolean setLanguage(Locale loc) {
		return tts==null?false:tts.setLanguage(loc)== TextToSpeech.LANG_AVAILABLE;
	}

}
