package com.magnifis.parking;

import java.io.IOException;

import com.magnifis.parking.VR.IAnimator;

import android.app.Activity;
import android.app.Application;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

public class PrIdMag extends ProgressIndicatorController {

	public PrIdMag() {
	}
	
	@Override
	public void show(String what, String who) {
		MainActivity ma=MainActivity.get();
		if (ma!=null&&ma.mSpinner!=null)  ma.mSpinner.show();
		
		if (VR.get() == null)
			return;
		
		VR.get().startSoundWaiting();
	}

	@Override
	public void hide() {
		MainActivity ma=MainActivity.get();
		if (ma!=null&&ma.mSpinner!=null)  ma.mSpinner.dismiss();
		
		VR.get().stopSoundWaiting();
	}

}
