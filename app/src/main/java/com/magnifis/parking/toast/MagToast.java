package com.magnifis.parking.toast;

import java.lang.ref.WeakReference;

import com.magnifis.parking.App;
import com.magnifis.parking.Config;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.R;
import com.magnifis.parking.VR;
import com.magnifis.parking.suzie.MicAnimatorMagSuize;
import com.magnifis.parking.toast.ToastBase.LayoutOptions;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.views.ScalableShort;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MagToast extends ToastBase {

	private static WeakReference<MagToast> selfWr=null;
	protected View view = null;
	
	@Override
	public <T extends View> T getContentView() {
		return (T)view;
	}
	
	private RelativeLayout toastView = null;
	
	public static MagToast get() {
		return selfWr==null?null:selfWr.get();
	}	
	
	WindowManager wm = (WindowManager) App.self.getSystemService(Context.WINDOW_SERVICE);
	
	public static View createStdToastContentView(String s) {
		TextView view = new TextView(App.self);
		view.setText(s);
		view.setTextColor(Color.WHITE);
		
		RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_IN_PARENT);
		view.setLayoutParams(lp);
		return view;
	}

	public  MagToast(String s, LayoutOptions lo) {
	   this(createStdToastContentView(s), lo, new ColorDrawable(Color.BLACK),true);
	}
	
	public  MagToast(View view, LayoutOptions lo, Drawable bg, boolean killPreviousIfExists) {
		if (killPreviousIfExists) {
			MagToast old = get();
			if (old != null) {
				old.hide();
				/*
			try {
				old.finalize();
			} catch (Throwable e) {
			}
				 */
				old = null;
			}
			selfWr = new WeakReference<MagToast>(this);
		}
		
		if (lo == null)
			lo = new LayoutOptions();
		
		this.view=view;
		
			
		int w=lo.width, h=lo.height, lm=lo.leftMargin,tm=lo.topMargin;
		Rect pd=lo.contentPadding;
		
		if (lo.dimensionsIsDP) {
			Point scaled_wh=App.self.scaler.densityScaleIt(new Point(w,h));
			switch (lo.width) {
			case ViewGroup.LayoutParams.MATCH_PARENT:
			case ViewGroup.LayoutParams.WRAP_CONTENT:	
				break;
		    default:
				w=scaled_wh.x;
			}
			switch (lo.height) {
			case ViewGroup.LayoutParams.MATCH_PARENT:
			case ViewGroup.LayoutParams.WRAP_CONTENT:	
				break;
		    default:
				h=scaled_wh.y;
			}
			Point lmtm=App.self.scaler.densityScaleIt(new Point(lm,tm));
			lm=lmtm.x; tm=lmtm.y;
			if (pd!=null) pd=App.self.scaler.densityScaleIt(pd);
		}
		
		WindowManager.LayoutParams toastParams 
		    = new WindowManager.LayoutParams(w, h,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				PixelFormat.TRANSLUCENT);

		toastParams.x = lm;
		toastParams.y = tm;
		
		toastView = new ScalableShort(App.self);
		
		switch (lo.vetricalPosition) {
		case 1:
			toastParams.gravity = Gravity.TOP;
			break;

		case 2:
			toastParams.gravity = Gravity.BOTTOM;
			break;

		default:
			toastParams.gravity = Gravity.CENTER_VERTICAL;
			break;
		}

		switch (lo.horizontalPosition) {
		case 1:
			toastParams.gravity = toastParams.gravity | Gravity.LEFT;
			break;

		case 2:
			toastParams.gravity = toastParams.gravity | Gravity.RIGHT;
			break;

		default:
			toastParams.gravity = toastParams.gravity | Gravity.CENTER_HORIZONTAL;
			break;
		}
			
		toastView.setVisibility(View.GONE);
		if (bg!=null) toastView.setBackgroundDrawable(bg);
		
		if (pd!=null)
			toastView.setPadding(pd.left, pd.top, pd.right, pd.bottom);
		
		toastView.addView(view);
		
		wm.addView(toastView, toastParams);
	}
	
	@Override
	public void hide() {
		if (toastView != null && wm != null) toastView.post(
			  new Runnable() {
				  @Override
				  public void run() {
					  try {
						  toastView.setVisibility(View.GONE);
						  wm.removeView(toastView);
					  } catch (Exception e) {} 
				  }
				}
		  );
	
	}

	@Override
	public void show() {
		if (toastView != null) {
			toastView.setVisibility(View.VISIBLE);
		}
	}
}
