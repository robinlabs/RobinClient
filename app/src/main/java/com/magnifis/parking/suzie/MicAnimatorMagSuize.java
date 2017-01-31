package com.magnifis.parking.suzie;

import com.magnifis.parking.toast.ToastController;
import com.magnifis.parking.VR;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.util.Log;
import android.view.View;

public class MicAnimatorMagSuize implements VR.IAnimator {
	
	final static String TAG="Speech";

	public View micAnimationView = null;

	public View micAnimationViewL = null;

	public View micAnimationBgView1 = null;

	public View micAnimationViewError = null;
	
	public View micAnimationBgViewL = null;

	public View micBusy = null;
	
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
	private void showLevel(int i/*0-21*/) {
       if (micAnimationView != null) showLevel(micAnimationView,i);
       if (micAnimationViewL != null) showLevel(micAnimationViewL,i);
       SuziePopup sp = SuziePopup.get();
       if (sp != null)
    	   sp.returnToScreen();
	}

	@Override
	public void showListening(float rmsdB) {
		int ix=rmsdB>21?21:(Math.round(rmsdB));
        showLevel(ix);
	}
	
	@Override
	public void showThinking() {
		Log.d(VR.TAG_SPEECH,"an|showThinking");
		hidePrompt();

		if (micAnimationView != null) micAnimationView.setVisibility(View.GONE);
		if (micAnimationViewL != null) micAnimationViewL.setVisibility(View.GONE);
		if (micAnimationBgView1 != null) micAnimationBgView1.setVisibility(View.GONE);
		if (micAnimationBgViewL != null) micAnimationBgViewL.setVisibility(View.GONE);
		if (micBusy != null) micBusy.setVisibility(View.VISIBLE);
	}

	@Override
	public void showReadyToBegin() {
		Log.d(VR.TAG_SPEECH,"an|showReadyToBegin");
		hideBusyness(); 
		showPrompt();
		if (micAnimationView!=null) micAnimationView.setVisibility(View.VISIBLE);
		if (micAnimationViewL!=null) micAnimationViewL.setVisibility(View.VISIBLE);
		if (micAnimationBgView1!=null) micAnimationBgView1.setVisibility(View.VISIBLE);
		if (micAnimationBgViewL!=null) micAnimationBgViewL.setVisibility(View.VISIBLE);
		if (micBusy != null) micBusy.setVisibility(View.GONE);
		showListening(0);		
	}

	@Override
	public void showDone() {
		Log.d(VR.TAG_SPEECH,"an|showDone");
		hidePrompt();
		if (micAnimationView != null) micAnimationView.setVisibility(View.GONE);
		if (micAnimationViewL != null) micAnimationViewL.setVisibility(View.GONE);
		if (micAnimationBgView1 != null) micAnimationBgView1.setVisibility(View.GONE);
		if (micAnimationBgViewL != null) micAnimationBgViewL.setVisibility(View.GONE);
		if (micBusy != null) micBusy.setVisibility(View.GONE);
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
		if (tc==null) {
		}
	}
	
	private void hideBusyness() {
	}
	
	@Override
	public void showBegin() {
		Log.d(TAG,"showBegin");
	}
	@Override
	public void showError() {
		Log.d(VR.TAG_SPEECH,"an|showError");
		hidePrompt();
		if (micAnimationViewError!=null) micAnimationViewError.setVisibility(View.VISIBLE);
		hideBusyness();
	}
	@Override
	public void hideError() {
		Log.d(VR.TAG_SPEECH,"an|hideError");
		hidePrompt();
		if (micAnimationViewError!=null) micAnimationViewError.setVisibility(View.GONE);
		hideBusyness();
	}
}