package com.magnifis.parking;

import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Document;

import com.magnifis.parking.model.Intro;
import com.magnifis.parking.model.Intro.IntroItem;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.views.RealViewSwitcher;
import com.magnifis.parking.views.Scalable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import com.magnifis.parking.R;

public class IntroSlidesActivity extends Activity implements RealViewSwitcher.OnScreenSwitchListener {
	
	final static String TAG=IntroSlidesActivity.class.getSimpleName(),
			            TEST_VOICE="TEST_VOICE";
	
	private Intro.IntroItem intros[]=null;
	
	private RealViewSwitcher gal=null;
	
	private boolean testPassed=false;
	
	int prevPosition=-1;
	
	@Override
	public void onScreenSwitched(int screen) {
		if (prevPosition!=screen) {
			prevPosition=screen;
		     onPageShown(screen,false);
		}
	}

	protected void onPageShown(int ix, final boolean testVoice) {
		Log.d(TAG, "onPageShown: "+ix);
		if (!testVoice) testPassed=true;
		final String txt=Utils.trim(intros[ix].getText());

		MyTTS.abort();
		if (!Utils.isEmpty(txt)) gal.postDelayed(
		  new Runnable() {

			@Override
			public void run() {
		       if (testVoice) {
		    	 String a[]=txt.split("\\W+");
		    	 
		    	 final TimerTask terminator = new TimerTask() {
		    		 @Override
		    		 public void run() {
		    			 Log.d(TAG, "terminator");
		    			 // Here we know that the installed voices are
		    			 // broken.
		    			 MyTTS.abort();
                         MainActivity.wakeUp();
                         IntroSlidesActivity.this.finish();
		    		 }
		    	 };
		    	 
		    	 MyTTS.speakText(
		    	   new MyTTS.WithoutBubbles(a[0]) {

					@Override
					public void onSaid(boolean fAborted) {
						super.onSaid(fAborted);
						terminator.cancel();
						Props props=App.self.getProps();
						props.setAndSave(MainActivity.FIRST_EXECUTION, "false");
						testPassed=true;
					}
		    		   
		    	   }
		    	 );
		    	 
				 Timer timer = new Timer();

				 timer.schedule(terminator, 20000);
		    	 
		    	 if (a.length>1) MyTTS.speakText(
		    		new MyTTS.WithoutBubbles(
		    		  txt.substring(a[0].length())
		    		)
		    	 );
		    	 
		       } else 
		    	 MyTTS.speakText(new MyTTS.WithoutBubbles(txt));
			}
			 
		  },
		  10
		);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Resources res = getResources();
		Document doc=Xml.loadXmlFile(res.openRawResource(R.raw.intro));
		intros=Xml.setPropertiesFrom(doc.getDocumentElement(), Intro.class).getItems();
		
		super.onCreate(savedInstanceState);
		gal=new RealViewSwitcher(this) {

			boolean qFinish=true;
			
			@Override
			protected void onAttemptToScrollOut() {
				Log.d(TAG,"onAttemptToScrollOut");
				if (qFinish) {
					qFinish=false;
		    		Log.d(TAG,"finish!");
		    		MyTTS.abort();
		    		MainActivity.wakeUp();
		    	    IntroSlidesActivity.this.finish();
				}
			}
		 
		};
		
		for (IntroItem i:intros) {
			  ImageView iv=new ImageView(IntroSlidesActivity.this);
			  iv.setScaleType(ScaleType.CENTER_INSIDE);
			  Bitmap picture=i.getIcon();
			  iv.setImageBitmap(picture);
			  gal.addView(iv);
		}
		
		gal.setOnScreenSwitchListener(this);
		
//        gal.setBackgroundColor(/*Color.RED*/Color.argb( 255, 59, 69, 73));
		gal.setBackgroundColor(Color.argb( 255, 51, 204, 255));
		gal.setDrawingCacheEnabled(true);
		gal.setAnimationCacheEnabled(true);
		setContentView(gal);
		
		onPageShown(0,getIntent().getBooleanExtra(TEST_VOICE, false));
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (testPassed) {
			   MainActivity.newTask();
			   finish();
			   return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public static void start(Context parent,boolean fTestVoice) {
		Intent it=new Intent();
		it.setClass(App.self, IntroSlidesActivity.class);
		it.putExtra(TEST_VOICE, fTestVoice);
		it.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		Launchers.startNestedActivity(parent, it);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		App.self.setActiveActivity(this);
	}



	@Override
	protected void onPause() {
		super.onPause();
		App.self.removeActiveActivity(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		App.self.notifyStopActivity(this);
	}
}
