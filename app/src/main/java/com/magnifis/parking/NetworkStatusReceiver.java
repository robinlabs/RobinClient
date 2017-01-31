package com.magnifis.parking;

import com.magnifis.parking.suzie.SuziePopup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkStatusReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (SuziePopup.get() != null)
			if (SuziePopup.get().mag != null)
				SuziePopup.get().mag.hideError();
		if (MainActivity.get() != null)
			if (MainActivity.get().micAnimator != null)
				MainActivity.get().micAnimator.hideError();
	}

}
