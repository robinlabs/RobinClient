package com.magnifis.parking;

import com.magnifis.parking.views.ScalableShort;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;

// flash light requires flashlight_layout.xml
public class FlashLight implements SurfaceHolder.Callback {
	
	private Camera mCamera = null;
	private WindowManager.LayoutParams _paramsBtn = null;
	private RelativeLayout _layoutBtnBox = null;
	private SurfaceView _surface = null; 

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (mCamera != null)
			try {
				mCamera.setPreviewDisplay(holder);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	public FlashLight() {
		_paramsBtn = new WindowManager.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				0,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
				,
				PixelFormat.TRANSLUCENT);

		_paramsBtn.width = LayoutParams.WRAP_CONTENT;
		_paramsBtn.height = LayoutParams.WRAP_CONTENT;
		_paramsBtn.format = PixelFormat.TRANSLUCENT;
		_paramsBtn.gravity = Gravity.TOP | Gravity.RIGHT;
		_paramsBtn.y = 0;
		_paramsBtn.x = 0;
		_paramsBtn.alpha = 0;

		_layoutBtnBox = new RelativeLayout(App.self.getApplicationContext());
		
		LayoutInflater inflater = (LayoutInflater) App.self.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		inflater.inflate(R.layout.flashlight_layout, _layoutBtnBox);
		_surface = (SurfaceView) _layoutBtnBox.findViewById(R.id.surface);

		WindowManager _windowManager = (WindowManager) App.self.getSystemService(Context.WINDOW_SERVICE);
		_windowManager.addView(_layoutBtnBox, _paramsBtn);
		
		_layoutBtnBox.setVisibility(View.VISIBLE);
		_windowManager.updateViewLayout(_layoutBtnBox, _paramsBtn);

		SurfaceHolder mHolder = _surface.getHolder();
		mHolder.addCallback(this);
		
		try {
			mCamera = Camera.open();
		} catch (Exception e) {
			
		}
	}
	
	public void turnOn() {
		if (mCamera == null)
			return;
		
        try {
		    Parameters p1 = mCamera.getParameters();
		    p1.setFlashMode(Parameters.FLASH_MODE_TORCH);
		    mCamera.setParameters(p1);
		    mCamera.startPreview();
        } catch (Exception e) {}
	}
		
	public void turnOff() {
		if (mCamera == null)
			return;

        try {
            Parameters p2 = mCamera.getParameters();
            p2.setFlashMode(Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(p2);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        } catch (Exception e) {}
	}
		
	public void close() {
		if (mCamera == null)
			return;
		
		WindowManager _windowManager = (WindowManager) App.self.getSystemService(Context.WINDOW_SERVICE);
		_windowManager.removeView(_layoutBtnBox);
	}
	
}
