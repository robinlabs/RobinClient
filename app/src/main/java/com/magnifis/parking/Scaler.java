package com.magnifis.parking;

import static com.magnifis.parking.utils.Utils.isEmpty;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.magnifis.parking.utils.Utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class Scaler {
	final static String TAG=Scaler.class.getSimpleName();

	// scale
	int dspX,dspY,bgX,bgY;
	double _xScale=1,_yScale=1, shortTbScale=1, ratio=1;
	float displayDensity=1.5f, densityScaleFactor;
	
	public void initScaling(int traySize) {
		Log.d(TAG, "init rescaler for "+App.self.getDspResolutionString());
		DisplayMetrics dm=App.self.getResources().getDisplayMetrics();
		dspX=dm.widthPixels;
		dspY=dm.heightPixels;
		displayDensity= dm.density;
		densityScaleFactor=displayDensity/1.5f;
		
		double r=1.;
		
		//Drawable bg=getBackground();
		if (dspX>dspY) {
		   bgX=800;//bg.getIntrinsicHeight();
		   bgY=480;//bg.getMinimumWidth();
		   
		   ratio=(double)dspY*dm.ydpi/((double)dspX*dm.xdpi);
		   shortTbScale=(double)(dspY-traySize)/(double)bgY;
		   
		   r=dm.xdpi/dm.ydpi;
		} else {
		   bgY=800;//bg.getIntrinsicHeight();
		   bgX=480;//bg.getMinimumWidth();
		   
		   ratio=(double)(dspX*dm.xdpi/(double)dspY*dm.ydpi);
		   shortTbScale=(double)(dspX-traySize)/(double)bgX;
		   r=dm.ydpi/dm.xdpi;
		}
		
		_xScale=(double)dspX/(double)bgX;
		_yScale=(double)dspY/(double)bgY;
		Log.d(TAG,"wh "+bgX+" "+bgY+" "+dspX+" "+dspY);		
		Log.d(TAG,_xScale+" "+_yScale);
	}	
	
	public static interface Interface {
		void scaleIt(android.view.ViewGroup.LayoutParams params);
	};
	
	
	private  android.view.ViewGroup.LayoutParams scaleIt(
	  android.view.ViewGroup.LayoutParams params,
	  double xScale, double yScale
	) {

		switch (params.width) {
		  default:
			  params.width=(int)(params.width*xScale); break;
		  case LayoutParams.FILL_PARENT: case LayoutParams.WRAP_CONTENT:
		}
		switch (params.height) {
		  default:
			  params.height=(int)(params.height*yScale);
		  case LayoutParams.FILL_PARENT: case LayoutParams.WRAP_CONTENT:
		}		

		if (params instanceof ViewGroup.MarginLayoutParams) {
			ViewGroup.MarginLayoutParams rlp=(ViewGroup.MarginLayoutParams)params;
			rlp.topMargin=(int)(rlp.topMargin*yScale); 
			rlp.bottomMargin=(int)(rlp.bottomMargin*yScale); 
			rlp.leftMargin=(int)(rlp.leftMargin*xScale); 
			rlp.rightMargin=(int)(rlp.rightMargin*xScale); 
	
		} 
		
		return params;
	}
	
	public  void densityScaleIt(Paint pt) {
		pt.setTextSize(
				(float) ((float)pt.getTextSize()*densityScaleFactor)
				);
	}	
	
	public  Point densityScaleIt(Point pt) {
	  return new Point(
		Math.round(pt.x*densityScaleFactor),
		Math.round(pt.y*densityScaleFactor)
	  );
	}	
	
	public  Rect densityScaleIt(Rect r) {
		return densityScaleIt(r.left,r.top,r.right,r.bottom);
	}
	
	public  Rect densityScaleIt(int left, int top, int right, int bottom) {
		Point pt1=densityScaleIt(new Point(left,top)), pt2=densityScaleIt(new Point(right,bottom));
		return new Rect(
		  pt1.x, pt1.y,
		  pt2.x, pt2.y
		);
	}	
	
	public  void scaleIt(Paint pt, double yScale) {
	   pt.setTextSize(
	      (float) ((double)pt.getTextSize()*yScale)
	   );
	}
	
	public  void scaleItXY(
	  View child,
	  android.view.ViewGroup.LayoutParams params
	) {
		scaleIt(child, params, _xScale, _yScale);
	}
	
	public  void scaleItX(
			View child,
			android.view.ViewGroup.LayoutParams params
	) {
		scaleIt(child, params, _xScale, _xScale);
	}
	public  void scaleItY(
			View child,
			android.view.ViewGroup.LayoutParams params
	) {
		scaleIt(child, params, _yScale, _yScale);
	}
	
	public int scaleItShort(double v) {
	   if (App.self.isInLanscapeMode())
		  return (int)Math.round(v*_yScale);
	   else
		  return (int)Math.round(v*_xScale);		
	}
			
	
	public  void scaleItShort(
			View child,
			android.view.ViewGroup.LayoutParams params
	) {
	   if (App.self.isInLanscapeMode())
		 scaleIt(child, params, _yScale, _yScale);
	   else
		 scaleIt(child, params, _xScale, _xScale);
	}
	
	public  void scaleItShortTray(
			View child,
			android.view.ViewGroup.LayoutParams params,
			boolean recursive
	) {
		 scaleIt(child, params, shortTbScale, shortTbScale, recursive);
	}
	
	public  void scaleIt(
			  View child,
			  android.view.ViewGroup.LayoutParams params,
			  double xScale, double yScale

	) {
	   scaleIt(child, params, xScale, yScale,true);
	}
	
	public  void scaleIt(
	  View child,
	  android.view.ViewGroup.LayoutParams params,
	  double xScale, double yScale,
	  boolean recursive
	) {
		   if (
		     child.isInEditMode()//||
		     //child.getTag(R.string.scaled)!=null
		   ) return; 
		   
		   
		   if (child instanceof Interface&&recursive) {
			   ((Interface)child).scaleIt(params);
			   return;
		   }
		   
		   Log.d(TAG,"do rescale "+child.toString());
		   
		   ScalingParams sp=(ScalingParams)child.getTag(R.string.scaled);
		   
		   if (sp==null) {
			   child.setTag(R.string.scaled, sp=new ScalingParams(child, params));
		   } else {
			   if (xScale==sp.getxScale()&&yScale==sp.getyScale()) return;
			   params=Utils.cloneLayoutParams(sp.getLayoutParams());
		   }
		   
		   
		   sp.setxScale(xScale);
		   sp.setyScale(yScale);
		   
		   if (params!=null) {
			  Log.d(TAG,params.width+"x"+params.height);
			  child.setLayoutParams( scaleIt(params, xScale, yScale));
			  Log.d(TAG,params.width+"x"+params.height);
		   } else 
			  Log.d(TAG, "no layout params");
		   
		   
		   if (child instanceof TextView) {
			   if (child.getTag(R.string.don_t_scale_font)==null) {
			     TextView tv=(TextView)child;
			     tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,(float) (sp.getTextSize()*yScale));
			   }
		   } //else // why ?
			   child.setPadding(
				 (int)Math.round(sp.getLeftPadding()*xScale),
				 (int)Math.round(sp.getTopPadding()*yScale),
				 (int)Math.round(sp.getRightPadding()*xScale),
				 (int)Math.round(sp.getBottomPading()*yScale)
			   );
		   
	}	

}
