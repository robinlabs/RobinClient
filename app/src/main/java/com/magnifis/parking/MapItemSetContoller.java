package com.magnifis.parking;

import static com.magnifis.parking.Phrases.pickCannotCallPhrase;

import java.io.Serializable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.GeoObject;
import com.magnifis.parking.model.GeoSpannable;
import com.magnifis.parking.model.PkFacility;
import com.magnifis.parking.model.QueryInterpretation;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.views.TheMapView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

public abstract class MapItemSetContoller<T extends GeoObject> {
	static final String TAG=MapItemSetContoller.class.getName();
	
	abstract public GeoSpannable<T> getSpannable();
	
	public boolean isCurrent() {
		return MainActivity.get().currentController==this;
	}
	
	public boolean isTooltipAllowed() {
		return true;
		//return isCurrent();
	}
	
	protected GeoItemsOverlay<T> overlay=null;
	
    public GeoItemsOverlay<T> getOverlay() {
		return overlay;
	}
	public void setOverlay(GeoItemsOverlay<T> overlay) {
		this.overlay = overlay;
	}
	
	public boolean isSelected(T item) {
	  return item==getSelected();
	}
	
	public int indexOfTheLastSelected() {
	   return (getSelected()==null)?-1:indexOf(getSelected());
	}
	 
    abstract public T getSelected();
    abstract public void setSelected(T item);
	
    abstract public MapItemIterator<T> getIterator();
	
    abstract public void setIterator(MapItemIterator<T> iterator);
    
	public int  indexOf(T item) {
    	return getIterator().indexOf(item);
    }
	public int size() {
		if (getIterator()==null) return 0;
		return getIterator().size();
	}
	
	public T get(int i) {
        return getIterator().get(i);
	}
	
    
    public void showDetails() {}
    
    public void sayDetails(boolean sayExtra) {}
    
    public Drawable getMarkerFor(T item) {
      return getDefaultMarker();
    }
    
    abstract public Drawable getDefaultMarker();
    
    public Drawable getPopupFor(T item) {
      return getDefaultPopup();
    }
    
    abstract public Drawable getDefaultPopup();
    
    public void goItem(T item) {}
    
	public DoublePoint[] getBoundBox() {
		GeoSpannable<T> gs=getSpannable();
		return gs==null?null:gs.getBoundBox();
	}
	
	public static int DEFAULT_ZOOM=17;
   
	public void showWholeSet(boolean adjustZoom) {
		DoublePoint dp[]=getBoundBox();

		TheMapView mv=(TheMapView)getOverlay().getMapView();
		MainActivity ma=MainActivity.get();

		if (dp!=null) {
			DoublePoint myloc=UserLocationProvider.readLocationPoint();
			if (myloc!=null) {
			   GeoObject nearest=getSpannable().getNearestTo(myloc);
			   DoublePoint pt=(nearest==null)?dp[2]:nearest.getPoint();
			   if (myloc.distanceInMeters(pt)<=20000l) {
				  // we are in a 20 km proximity to the target 
				  Log.d(TAG, " we are in a 20 km proximity to the target ");
				  Log.d(TAG, " let's update the focus and the zoom ");				  
 				  dp=DoublePoint.getUpdatedGabarites(dp, myloc);
			   }
			}
			
/*
			if (size()==1) {
				MainActivity.get().setDefaultZoom();
				ma.zlAfterAdjust=mv.getZoomLevel();
			} else */ {
				if (adjustZoom)  {
                    mv.getController().setZoom(DEFAULT_ZOOM);
                    /*
					GeoPoint gp=new DoublePoint(dp[1].getLat()-dp[0].getLat(),dp[1].getLon()-dp[0].getLon()).toGeoPoint();
					mv.getController().zoomToSpan(gp.getLatitudeE6(),gp.getLongitudeE6());
					ma.zlAfterAdjust=mv.getZoomLevel();*/
                    ma.zlAfterAdjust=mv.moveAndZoomToCover(dp[0], dp[1]);
				}
				// fix the box with the marker sizes
				dp=DoublePoint.getUpdatedGabarites(dp, getOverlay().getPopupGabarites(getSelected().getPoint()));
				//  gp=new DoublePoint(dp[1].getLat()-dp[0].getLat(),dp[1].getLon()-dp[0].getLon()).toGeoPoint();
				// mapController.zoomToSpan(gp.getLatitudeE6(),gp.getLongitudeE6());
			}

			if (ma.zlAfterAdjust==mv.getZoomLevel())
				ma.showLocation(dp[2]);
			else 
				ma.showLocation(
						new DoublePoint(
								new DoublePoint(mv.getMapCenter()).getLat(),getSelected().getPoint().getLon()
								)
						);

			/*
   	  showLocation(
   		//status.getSelectedParking().getPoint().toGeoPoint()
   			  (zlAfterAdjust==mv.getZoomLevel())
   			       ?dp[2]
   			       ://status.getSelectedParking().getPoint()
   			    	 new DoublePoint(dp[2].getLat(),status.getSelectedParking().getPoint().getLon())
   	  );
			 */

			//getOverlay().doPopulate();
			mv.invalidate();
			mv.postDelayed(
					new Runnable() {
						public void run() {
							Log.d(TAG,"dize: "+size());
							T sel=getSelected();
							if (sel!=null) Log.d(TAG,"sel: "+sel);
							getOverlay().focusSelected();  // here is a bug to fix
						}    			
					}, 
					200
					);

		}		
	}
	
    public void sayAndGoOtherIfCan(QueryInterpretation qi, boolean fBackward) {
    	
    	if (size()<=0
    	) {
    		MyTTS.speakText(App.self.getString(R.string.mapitemsetcontroller_say_and_go_other_no_more_options));
    		VoiceIO.condListenAfterTheSpeech();
    		return;
    	}
    	
    	T sel=getSelected();
 
    	T pkf=fBackward
    		?getIterator().prev(sel):getIterator().next(sel)
    	;
    	if (pkf!=null) {
    		if (sel==null) {
    		   // acception focus
    			getOverlay().clearOtherOverlaySelection();
    		}
    		setSelected(pkf);
    		Log.d(TAG+".sayAndGoOtherIfCan","distance="+getSelected().getDistance());
    		getOverlay().doPopulate();
    		showWholeSet(true);
    		MyTTS.speakText(qi);
    		
    		MyTTS.speakText(
    		 fBackward
    		    ?R.string.mapitemsetcontroller_say_and_go_other_previous_option
    			:R.string.mapitemsetcontroller_say_and_go_other_another_option
    		);
    		sayDescriptionAndListen();
    		return;
    	}
     
    	sayMoMoreOptions();
    }  	
    
    public void sayMoMoreOptions() {
    	MyTTS.speakText(R.string.mapitemsetcontroller_no_more_options);
    	VoiceIO.listenAfterTheSpeech();
    }
   
    public void sayDescriptionAndListen()  {
		sayDetails(
				//false
				true
				); 
		VoiceIO.condListenAfterTheSpeech();  	
    }
    
    public void select(T pkfs, boolean adjustZoom) {
        setSelected(pkfs);
        showWholeSet(adjustZoom);
        sayDescriptionAndListen();
    }
    
 }
