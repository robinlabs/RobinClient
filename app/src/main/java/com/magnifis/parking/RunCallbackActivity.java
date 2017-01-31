package com.magnifis.parking;

import com.magnifis.parking.cmd.i.IOptionsListViewHolder;
import com.magnifis.parking.suzie.RequiresSuzie;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.views.DecoratedListView;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/***
 * 
 * @author zeev
 * This is a special activity intended to support "startActivityForResult" call from a service
 *  
 */
public class RunCallbackActivity extends Activity implements RequiresSuzie, IOptionsListViewHolder {
	final static String TAG=RunCallbackActivity.class.getSimpleName();
	
	RunningInActivity ria=null;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG,"onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
		if (ria!=null) ria.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"onCreate");
		super.onCreate(savedInstanceState);
		ria=App.self.getToRunInActivity(getIntent().getIntExtra(RunningInActivity.OBJECT_KEY, 0));
		if (ria==null) finish(); else {
			App.self.clearToRunInActivity(ria);
			ria.setActivity(this);
			ria.run();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
        ria.onBackPressed();
        finish();
        /*
		if (keyCode == KeyEvent.KEYCODE_BACK) {
		     if (ria.onBackPressed()) return true;
		}*/
		return super.onKeyDown(keyCode, event);
	}
/*
	@Override
	protected void onResume() {
		super.onResume();

		Log.d(TAG,"onResume");
		App.self.setActiveActivity(this);
		ria.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		Log.d(TAG,"onPause");
		App.self.removeActiveActivity(this);
		ria.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG,"onStop");
		App.self.notifyStopActivity(this);
		ria.onStop();
	}

	@Override
	protected void onStart() {
		Log.d(TAG,"onStart");
		super.onStart();
		ria.onStart();
	}*/

	@Override
	public boolean isRequiringSuzie() {
		return ria.isRequiringSuzie();
	}
	
	///////////////////////////////////////////
	
	protected DecoratedListView optionsListView=null;
	protected FrameLayout mainView=null;
	
	protected void showVwScreen() {
	   if (mainView==null) {
		  mainView=new FrameLayout(this);
		  mainView.setBackgroundColor(Color.WHITE);
		  setContentView(mainView,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
	   } else
		   mainView.setVisibility(View.VISIBLE); 
	}
	
	protected void hideVwScreen() {
		if (mainView!=null) mainView.setVisibility(View.GONE);
	}
	
	public void hideOptionsListView() {
		if (optionsListView != null) {
			optionsListView.setVisibility(View.GONE);
			ViewGroup vg = (ViewGroup) optionsListView.getParent();
			vg.removeView(optionsListView);
			optionsListView = null;
			hideVwScreen();
		}
	}

	public DecoratedListView getOptionsListView() {
		if (optionsListView == null) {
			showVwScreen();
			DecoratedListView lv = new DecoratedListView(this);
			lv.setBackgroundColor(Color.WHITE);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			lp.addRule(RelativeLayout.ABOVE, R.id.BottomPanel);
			lv.setLayoutParams(lp);

			// ViewGroup vg=(ViewGroup)progressBar.getParent();

			mainView.addView(lv);
			optionsListView = lv;
		}
		return optionsListView;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG,"onNewIntent");
		super.onNewIntent(intent);
	}
	
}
