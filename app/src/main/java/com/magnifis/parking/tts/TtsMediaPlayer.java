package com.magnifis.parking.tts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.media.MediaPlayer;
import android.util.Log;

import com.magnifis.parking.Consts;
import com.magnifis.parking.Fetcher;
import com.magnifis.parking.tts.MyTTS.IBubblesInMainActivityOnly;
import com.magnifis.parking.tts.MyTTS.IControlsBubblesAlone;
import com.magnifis.parking.tts.MyTTS.OnSaidListener;
import com.magnifis.parking.tts.MyTTS.OnStringSpeakListener;
import com.magnifis.parking.tts.MyTTS.SelfSpeakable;
import com.magnifis.parking.tts.MyTTS.Wrapper;
import com.magnifis.parking.utils.DiskCache;
import com.magnifis.parking.utils.CachingFileFetcher;

public class TtsMediaPlayer extends MediaPlayer 
    implements OnStringSpeakListener, SelfSpeakable,  
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener,
    IBubblesInMainActivityOnly,
    IControlsBubblesAlone
{
	
	@Override
	public boolean isBubblesInMainActivityOnly() {
	  return wrapped!=null&&(wrapped instanceof Wrapper)&&((Wrapper)wrapped).isBubblesInMainActivityOnly(); 
	}
	
	final  String TAG=MyTTS.class.getSimpleName()+'.'+TtsMediaPlayer.class.getSimpleName();
	
	final Object wrapped;
	
	final MyTTS instance;
	
	public TtsMediaPlayer(
		MyTTS myTts,
		String ds,
		Object wrapped
	) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
	   this.instance=myTts;
	   this.wrapped=wrapped;
	   setOnPreparedListener(this);
	   setOnCompletionListener(this);
	   setOnErrorListener(this);
	   Log.d(TAG,"ds="+ds);
	   

	   new CachingFileFetcher(ds) {
		 {
			userAgent="Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5";
		 }

		@Override
		protected void onPostExecute(File f) {
			if (f==null) {
				TtsMediaPlayer.this.onCompletion(TtsMediaPlayer.this);
			}  else {
				try {
					TtsMediaPlayer.this.setDataSource(f.getAbsolutePath());
				} catch (Throwable e) {
					e.printStackTrace();
				}
				try {
					TtsMediaPlayer.this.prepareAsync();
				} catch (Exception e) {}
			}
		}
		   
	   };
	}
	
	boolean autoPlayAfterPrepare, prepared = false, aborted=false;
	
	@Override
	public void onSaid(boolean fAborted) {
		if (wrapped instanceof OnSaidListener)
			((OnSaidListener)wrapped).onSaid(fAborted);	  
		else
			if (!fAborted&&wrapped instanceof Runnable) ((Runnable)wrapped).run();
	}
	@Override
	public void onToSpeak() {
		instance.inProcess=this;
		if (wrapped instanceof OnStringSpeakListener)
			((OnStringSpeakListener)wrapped).onToSpeak();
	}
	@Override
	public void speak() {
		if (!prepared)
			autoPlayAfterPrepare = true;
		else
			try {
				start();
			} catch (Exception e) {}			
	}
	
	@Override
	public boolean abort() {
		if (aborted) return false;
		aborted=true;
		boolean f=isPlaying();
		try {
		  super.stop();
		  onCompletion(this);
		} catch(Throwable t) {}
		return f;
	}
	
	@Override
	public void onPrepared(MediaPlayer mp) {
		prepared = true;
		if (autoPlayAfterPrepare)
			start();
	}
	
	@Override
	public String toString() {
		return wrapped.toString();
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
		   instance.onCompletion();		
	}
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.d(TAG,"onError: "+what+" "+extra);
		return false;
	}
	@Override
	public boolean isControllingBubblesAlone() {
		return wrapped!=null&&(wrapped instanceof Wrapper)&&((Wrapper)wrapped).isControllingBubblesAlone();
	}
	
}