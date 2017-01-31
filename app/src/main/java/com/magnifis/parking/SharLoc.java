package com.magnifis.parking;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.net.Uri;



import com.magnifis.parking.UserLocationProvider.LocationInfo;
import com.magnifis.parking.geo.GoogleGeocoder;
import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.GcResult;

import static com.magnifis.parking.utils.Utils.*;
import static com.magnifis.parking.VoiceIO.*;

public class SharLoc {
   final static int NO_SHARING=0, TWITTER_SHARING=1, SMS_SHARING=2, EMAIL_SHARING=3; 
	
   final static String TAG=SharLoc.class.getSimpleName();
   
   public void abort() {
	   if (!worker.isInterrupted()) worker.interrupt();
   }
	
   protected Thread worker=new Thread("SharLoc") {

	MultipleEventHandler.EventSource es=null;	   
	   
	@Override
	public void run() {
		
	   for (;;) try {
		   synchronized(SharLoc.this) {
			   try {
			     SharLoc.this.wait();
			   } catch (InterruptedException ex) {
				  return;
			   }
		   }
		   try {
			   MainActivity.get().runOnUiThread(
				 new Runnable() {
					@Override
					public void run() {
					  es=MainActivity.get().showProgress();
					}
				 }
			   );
			   String strLoc=location.toString();
			   GcResult gr=GoogleGeocoder.getFromLatlonRefined(
					   location
					   //new DoublePoint(42.18992,-87.847311)
					   );
			   
			   boolean exact=locationInfo.isExact();
			   
			   sayFromGui(
				 exact?R.string.P_YOUR_LOCATION_IS:R.string.P_YOUR_APPROXIMATE_LOCATION_IS
			   );
			   
			   if (gr!=null) {
				   strLoc=gr.getFormattedAddress(false);
			   }
		   
			   sayAndShowFromGui(strLoc);

               if (sharing != NO_SHARING) {
                   Intent sendIntent = new Intent();
                   sendIntent.setAction(Intent.ACTION_SEND);
                   sendIntent.putExtra(Intent.EXTRA_TEXT, strLoc);
                   sendIntent.setType("text/plain");
                   startActivityFromNowhere(sendIntent);
               }

		   } finally {
			   MainActivity.get().runOnUiThread(
				 new Runnable() {
					@Override
					public void run() {
					  es.fireEvent();
					}
				 }
			   );
			   
		   }
	   } catch (Throwable t) {
		  t.printStackTrace(); 
	   }
	}
	   
   };
   
   final static int EMAIL_SUBJ=R.string.M_LOCATION_EMAIL_SUBJ;
   
   public SharLoc() {
	   worker.start();
   }
   
   private LocationInfo locationInfo;
   private DoublePoint location;
   private int sharing;
   private boolean fListen=false;
   private WeakReference<Context> contextRef=new WeakReference<Context>(App.self);
   
   public void sayLocation(Context ctx,LocationInfo repLocRes) {
	   shareLocation(ctx, repLocRes,NO_SHARING, false); 
   }
   
   public void shareLocation(Context ctx,LocationInfo repLocRes, String sharing, boolean fListen) {
	   int sh=SMS_SHARING;
	   if ("twitter".equals(sharing)) sh=TWITTER_SHARING; else
	   if ("mail".equals(sharing)) sh=EMAIL_SHARING;
	   shareLocation(ctx, repLocRes,sh, fListen);
   }
	
   public void shareLocation(Context ctx, LocationInfo repLocRes, int sharing, boolean fListen) {
	   locationInfo=repLocRes;
	   location=locationInfo.getLocationDP();
	   contextRef=new WeakReference<Context>(ctx);
	   this.sharing=sharing;
	   this.fListen=fListen;
	   synchronized(this) {
	     notify();
	   }
   }
}
