package com.magnifis.parking;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Build;
import android.view.View;

import com.magnifis.parking.suzie.SuzieService;
import com.magnifis.parking.toast.ToastController;

class MicAnimatorMag implements VR.IAnimator {
	
	final static String TAG="Speech";//MicAnimatorMag.class.getSimpleName();

	MainActivity ma=MainActivity.get();

	private void showLevel(View av, int i/*0-21*/) {
	   Drawable b=av.getBackground();
	   if (b instanceof LevelListDrawable) {
	     LevelListDrawable lld=(LevelListDrawable )b;
	     if (i>0) {
	       lld.setLevel(i-1);
	       av.setVisibility(View.VISIBLE);
	     } else
	       av.setVisibility(View.GONE);
	   }
	}


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void showLevel(View v,float f) {
        if (ma == null)return;

        Log.d("voice", "level:" + f);

        if (Build.VERSION.SDK_INT < 11) return;

        float factor = f * 0.3f;
        v.setScaleX(factor);
        v.setScaleY(factor);

    }


	private void showLevel(int i/*0-21*/) {
		if (ma == null)
			return;
		
		if (ma.micAnimationView != null) showLevel(ma.micAnimationView,i);
		if (ma.micAnimationViewL != null) showLevel(ma.micAnimationViewL,i);
	}
    private void showLevel(float i) {
		if (ma == null)
			return;

		if (ma.micAnimationView != null) showLevel(ma.micAnimationView,i);
		if (ma.micAnimationViewL != null) showLevel(ma.micAnimationViewL,i);
	}

    @Override
    public void showListening(float rmsdB) {
        showLevel(rmsdB);
    }

    MultipleEventHandler.EventSource es=null;

	@Override
	public void showThinking() {
		if (ma == null)
			return;
		
		Log.d(VR.TAG_SPEECH,"an|showThinking");
		hidePrompt();
		if (es==null) {
			es=ma.showProgress();
			Log.d(TAG,"an|es="+es+" animator="+this);
		}

		if (ma.micAnimationView != null) ma.micAnimationView.setVisibility(View.GONE);
		if (ma.micAnimationViewL != null) ma.micAnimationViewL.setVisibility(View.GONE);
		if (ma.micAnimationBgView != null) ma.micAnimationBgView.setVisibility(View.GONE);
		if (ma.micAnimationBgViewL != null) ma.micAnimationBgViewL.setVisibility(View.GONE);
	}

	@Override
	public void showReadyToBegin() {
		if (ma == null)
			return;

		Log.d(VR.TAG_SPEECH,"an|showReadyToBegin");
		hideBusyness(); 
		showPrompt();
		if (ma.micAnimationView!=null) ma.micAnimationView.setVisibility(View.VISIBLE);
		if (ma.micAnimationViewL!=null) ma.micAnimationViewL.setVisibility(View.VISIBLE);
		if (ma.micAnimationBgView!=null) ma.micAnimationBgView.setVisibility(View.VISIBLE);
		if (ma.micAnimationBgViewL!=null) ma.micAnimationBgViewL.setVisibility(View.VISIBLE);
		showListening(0);		
	}

	@Override
	public void showDone() {
		if (ma == null)
			return;

		Log.d(VR.TAG_SPEECH,"an|showDone");
		hidePrompt();
		if (ma.micAnimationView!=null) ma.micAnimationView.setVisibility(View.GONE);
		if (ma.micAnimationViewL!=null)  ma.micAnimationViewL.setVisibility(View.GONE);
		if (ma.micAnimationBgView!=null)  ma.micAnimationBgView.setVisibility(View.GONE);
		if (ma.micAnimationBgViewL!=null)  ma.micAnimationBgViewL.setVisibility(View.GONE);
		hideBusyness();
	}
	
	ToastController tc=null;
	
	private void hidePrompt() {
	  if (tc!=null) {
		  tc.abort();
		  tc=null;
	  }
	}
	
	private void showPrompt() {
		if (ma == null)
			return;

		if (tc==null) {
			  String prompt=ma.getMicPrompt();
			  if (prompt!=null) {
					if (Config.bubles && MainActivity.isActive())
					  ma.bubleAnswer(prompt);
				  else {
					  SuzieService.answerBubble(prompt);
					  /*
					  SuziePopup s = SuziePopup.get();
					  if (s != null && s.isVisible())
							s.bubleAnswer(s.toString());
					  else
						  tc=new ToastController(ma, prompt, true);
						  */
				  }
			  }
		}
	}
	
	private void hideBusyness() {
		if (es!=null) {
			Log.d(VR.TAG_SPEECH,"an|hideBusyness");
			es.fireEvent();
			es=null;
		}			
		else
			Log.d(VR.TAG_SPEECH,"an|no Busyness "+this);
	}
	
	@Override
	public void showBegin() {
		if (ma == null)
			return;

		Log.d(TAG,"an|showBegin");
		if (es==null) {
			es=ma.showProgress();
			Log.d(TAG,"an|es="+es+" animator="+this);
		}
	}

	@Override
	public void hideError() {
		if (ma == null)
			return;

	}
	
	@Override
	public void showError() {
		if (ma == null)
			return;
	}
}