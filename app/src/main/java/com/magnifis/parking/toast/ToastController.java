package com.magnifis.parking.toast;

import com.magnifis.parking.App;
import com.magnifis.parking.Config;
import com.magnifis.parking.Log;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.R;
import com.magnifis.parking.suzie.SuziePopup;
import com.magnifis.parking.utils.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import static com.magnifis.parking.utils.Utils.*;

public class ToastController {
	final static String TAG=ToastController.class.getCanonicalName();
	
	Toast tt=null; ToastBase magTt=null;
	
	boolean fAbort=false;
	
	public void abort() {
		fAbort=true;
		if (tt!=null) { nTimesToShow = 0; /*tt.cancel();*/ } else
		if (magTt!=null) {
			magTt.hide();
			if (onHide!=null) onHide.run();
		}
	}
	
	private Runnable onHide=null;
	
    public Runnable getOnHide() {
		return onHide;
	}

	public void setOnHide(Runnable onHide) {
		this.onHide = onHide;
	}
	
	int nTimesToShow=0;
	
	public static void showSimpleToast(int txt, int duration) {
		showSimpleToast(App.self.getString(txt),duration);
	}
	
	public static void showSimpleToast(String txt, int duration) {

		if (Config.bubles && MainActivity.isActive())
			return;
		
		if (SuziePopup.isVisible())
			return;
			
		Toast t=Toast.makeText(App.self, txt, duration);
  	  ToastController.setGravity(t);
  	  t.show();		
	}
	

	public static void setGravity(Toast t) {
		t.setGravity(Gravity.TOP, 0, 0);
	}

	class MyToast extends Toast {

		public MyToast(final Context ctx,final String s, final String query) {
			super(ctx);

			  Toast tmp= Toast.makeText(ctx, s, Toast.LENGTH_LONG);
			  FrameLayout fl=new FrameLayout(ctx) {
				@Override
				protected void onWindowVisibilityChanged(
						int visibility) {
					super.onWindowVisibilityChanged(visibility);
					if (visibility!=View.VISIBLE) {
					    if (!fAbort&&(nTimesToShow!=0)) {
					    	(tt=new MyToast(ctx,s,query)).show(); 
					    	--nTimesToShow;
					    	return;
					    }
					    if (onHide!=null) onHide.run();
					}
				}
				  
			  };
			  /*
			  if (App.self.isVwVersion) {
				  setView(fl);
		          LayoutInflater infl=LayoutInflater.from(ctx);
		          ViewGroup vwTosat=(ViewGroup)infl.inflate(R.layout.vw_toast, fl, true);
		          TextView spt=(TextView) vwTosat.findViewById(R.id.vwSpeachPopupText);
		          spt.setText(s);
				  //fl.addView(vwTosat);
			  } else*/ {
				  if (!Config.double_toast) {
				     View tv = tmp.getView();
				  
				     fl.addView(tv);
				     setView(fl);
				      setText(s);
				  } else {
					  setView(fl);
					  LayoutInflater infl=LayoutInflater.from(ctx);
					  ViewGroup vwTosat=(ViewGroup)infl.inflate(R.layout.double_toast, fl, true);
					  TextView queryText=(TextView) vwTosat.findViewById(R.id.queryText);
					  if (isEmpty(query)) {
						  queryText.setVisibility(queryText.GONE);
					  } else {
						  queryText.setVisibility(queryText.VISIBLE);
						  queryText.setText(App.self.getString(R.string.toast_prefix_you) + " " + query);
					  }
					  TextView spt=(TextView) vwTosat.findViewById(R.id.queryInterpretationText);
					  if (isEmpty(s)) {
						  spt.setVisibility(queryText.INVISIBLE);
						  spt.setText("Just some text, cause without a text previos TextView move to the center.");
					  } else {
						  spt.setVisibility(queryText.VISIBLE);
						  spt.setText(App.self.getString(R.string.toast_prefix_Robin) + " " + s);
					  }
				  }
			  }
			  ToastController.setGravity(this);

			  setDuration(Toast.LENGTH_SHORT);
		}
		
	}
	
	
	public ToastController(final Context ctx, final String s) {
		this(ctx,s,false);
	}

	public ToastController(final Context ctx, final String s, final boolean forever) {

		if (Config.bubles && MainActivity.isActive() ) {
			Log.d(TAG, "don't show toast");
			return;
		}
			
		if (SuziePopup.isVisible())
			return;
			
		if (!isEmpty(s)) Utils.runInMainUiThread(
		  ctx,
		  new Runnable() {
			public void run() {
					if (forever&&Config.use_mag_toasts) {
					  magTt=new MagToast(s,null);
					  magTt.show();
					} else {
					  nTimesToShow=forever?-1:((countWords(s)/14)+1);
					  tt=new MyToast(ctx,s,"");
					  tt.show();
					} 
			}
		});
    }
	
	public ToastController(View content,  ToastBase.LayoutOptions lo, Drawable bg) {
	   this(content,lo,bg,true);
	}
	
	public ToastController(final View content, final ToastBase.LayoutOptions lo, final Drawable bg, final boolean killPreviousIfExists) {
	  if (content!=null) {
		  Utils.runInMainUiThread(
				  new Runnable() {
					public void run() {
						magTt=new MagToast(content,lo,bg, killPreviousIfExists);
					    magTt.show();
					}
				});		  
	  } 	
	}
	
	public ToastController(final Context ctx, final String s, final String query, final boolean forever) {

		if (Config.bubles && MainActivity.isActive())
			return;
			
		if (SuziePopup.isVisible())
			return;
			
		if (!isEmpty(s)) Utils.runInMainUiThread(
		  ctx,
		  new Runnable() {
			public void run() {
				if (forever&&Config.use_mag_toasts) {
					  magTt=new MagToast(s,null);
					  magTt.show();
				} else {
					nTimesToShow=forever?-1:((countWords(s)/14)+1);
				    tt=new MyToast(ctx,s,query);
				    tt.show();
				}
			}
		});
		
    }	
}
