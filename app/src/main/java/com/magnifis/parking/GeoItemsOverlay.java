package com.magnifis.parking;

import java.net.MalformedURLException;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.GeoObject;
import com.magnifis.parking.model.PkFacility;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.ImageFetcher;
import com.magnifis.parking.views.TheMapView;

public class GeoItemsOverlay<DataItem extends GeoObject> extends ItemizedOverlay<OverlayItem> {
	protected final static String TAG=GeoItemsOverlay.class.getName();
	
	protected final Paint paint=new Paint() {
    	{
			setTextSize(16);
			setTextScaleX(0.8f);
			setColor(Color.WHITE);		
			setTypeface(Typeface.DEFAULT_BOLD);
			App.self.scaler.densityScaleIt(this);
    	}
    };
    
    protected final Paint paintBig=new Paint() {
    	{
			setTextSize(22);
			setColor(Color.WHITE);	
			setTypeface(Typeface.DEFAULT_BOLD);
			App.self.scaler.densityScaleIt(this);
    	}
    };
    
    protected final  Paint paintAddr=new Paint() {
    	{
			setTextSize(17);
			setTextScaleX(0.9f);
			setColor(Color.WHITE);	
			App.self.scaler.densityScaleIt(this);
    	}
    };
   
    public enum  HotspotPlace {
        NONE, CENTER, BOTTOM_CENTER, TOP_CENTER, RIGHT_CENTER, LEFT_CENTER, UPPER_RIGHT_CORNER, LOWER_RIGHT_CORNER, UPPER_LEFT_CORNER, LOWER_LEFT_CORNER
      }
      

  	/**
  	 * Adjusts a drawable's bounds so that (0,0) is a pixel in the location described by the hotspot
  	 * parameter. Useful for "pin"-like graphics. For convenience, returns the same drawable that
  	 * was passed in.
  	 *
  	 * @param marker
  	 *            the drawable to adjust
  	 * @param hotspot
  	 *            the hotspot for the drawable
  	 * @return the same drawable that was passed in.
  	 */
  	public static Drawable boundToHotspot(final Drawable marker, HotspotPlace hotspot) {
  		int mScale=1;
  		final int markerWidth = (int) (marker.getIntrinsicWidth() * mScale);
  		final int markerHeight = (int) (marker.getIntrinsicHeight() * mScale);
  		
  		Rect mRect = new Rect();

  		mRect.set(0, 0, 0 + markerWidth, 0 + markerHeight);

  		if (hotspot == null) {
  			hotspot = HotspotPlace.BOTTOM_CENTER;
  		}

  		switch (hotspot) {
  		default:
  		case NONE:
  			break;
  		case CENTER:
  			mRect.offset(-markerWidth / 2, -markerHeight / 2);
  			break;
  		case BOTTOM_CENTER:
  			mRect.offset(-markerWidth / 2, -markerHeight);
  			break;
  		case TOP_CENTER:
  			mRect.offset(-markerWidth / 2, 0);
  			break;
  		case RIGHT_CENTER:
  			mRect.offset(-markerWidth, -markerHeight / 2);
  			break;
  		case LEFT_CENTER:
  			mRect.offset(0, -markerHeight / 2);
  			break;
  		case UPPER_RIGHT_CORNER:
  			mRect.offset(-markerWidth, 0);
  			break;
  		case LOWER_RIGHT_CORNER:
  			mRect.offset(-markerWidth, -markerHeight);
  			break;
  		case UPPER_LEFT_CORNER:
  			mRect.offset(0, 0);
  			break;
  		case LOWER_LEFT_CORNER:
  			mRect.offset(0, -markerHeight);
  			break;
  		}
  		marker.setBounds(mRect);
  		return marker;
  	}
     
  	
  	public static Drawable boundCenterBottom(Drawable balloon) {
  	  return boundToHotspot(balloon,HotspotPlace.BOTTOM_CENTER);
  	 // return ItemizedOverlay.boundCenterBottom(balloon);
  	}
  	
  	public static Drawable boundRightBottom(Drawable balloon) {
  		return boundToHotspot(balloon,HotspotPlace.LOWER_RIGHT_CORNER);
      }


	
	public void focusSelected() {
		int ix=controller.indexOfTheLastSelected();
		if (ix>=0&&ix<size()) {
		   OverlayItem oi=getItem(ix);
		   if (oi!=null) {
			 // setLastFocusedIndex(ix);
			  setFocus(oi);
		   }
		}
	}

	public void clearOtherOverlaySelection() {
		for (Overlay ov:mv.getOverlays()) 
		      if (ov instanceof GeoItemsOverlay && ov!=this) {
		    	  GeoItemsOverlay gio=(GeoItemsOverlay)ov;
		    	  gio.controller.setSelected(null);
		      }		
	}
	
	@Override
	public boolean onTap(GeoPoint gp, MapView mapView) {
		
		Log.d(getClass().getName()," -- onTap");
		
		Projection prj=mv.getProjection();
		Point pt=prj.toPixels(gp, null);
		
		for (int i=size()-1;i>=0;i--) {
			GeoOverlayItem<DataItem> oi=(GeoOverlayItem<DataItem>)getItem(i);
			DataItem pkf=oi.getItem();
			Point fpt=prj.toPixels(pkf.getPoint().toGeoPoint(),null);
			if (controller.isTooltipAllowed()&&controller.isSelected(oi.getItem())) {
				if (getFocus()==null) focusSelected();
				int h=popupHeight, w=popupWidth;
				Log.d(TAG+".onTap b:",w+" "+h);
				Log.d(TAG+".onTap pt:",pt.toString());
				Log.d(TAG+".onTap fpt:",fpt.toString());
				if (oi.getClickableBox(fpt, true).contains(pt.x,pt.y)) {
					Log.d(TAG+".onTap contains(pt.x,pt.y):",fpt.toString());
					MyTTS.abort();
					mv.post(
							new Runnable() {
								public void run() {
									controller.showDetails();
								}
							}
					);
					return true;
				}
			}
		}
		for (int i=size()-1;i>=0;i--) {
			GeoOverlayItem<DataItem> oi=(GeoOverlayItem<DataItem>)getItem(i);
			DataItem pkf=oi.getItem();
			Point fpt=prj.toPixels(pkf.getPoint().toGeoPoint(),null);
			if (oi.getClickableBox(fpt, false).contains(pt.x,pt.y))  {
				MyTTS.abort();
				controller.setSelected(pkf);
				focusSelected();
				controller.showWholeSet(false);
				
				clearOtherOverlaySelection();
				return true;
			}
		}
		return false;
	}
	
	public void doPopulate() {
		//if (size()==0) {
		  // http://developmentality.wordpress.com/2009/10/19/android-itemizedoverlay-arrayindexoutofboundsexception-nullpointerexception-workarounds/
		  // Workaround for another issue with this class:
		  // <a href="http://groups.google.com/group/android-developers/browse_thread/thread/38b11314e34714c3">http://groups.google.com/group/android-developers/browse_thread/thread/38b11314e34714c3</a>
		  setLastFocusedIndex(-1);
		//}
		super.populate();
	}
	
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		// now shadows
		return super.draw(canvas, mapView, false, when);
	}
	
	private int popupHeight, popupWidth;
	
	public DoublePoint []getPopupGabarites(DoublePoint ptMarker) { // two gabarite points
	  Projection prj=mv.getProjection();
	  
	  int w=popupWidth*3/2,h=popupHeight;
	  
	  Point pt=prj.toPixels(ptMarker.toGeoPoint(), null);
	  
	  DoublePoint 
	    gp0=new DoublePoint(prj.fromPixels(pt.x-w/2, pt.y-h)),
	    gp1=new DoublePoint(prj.fromPixels(pt.x+w/2, pt.y));

	  
	  return new DoublePoint[] {gp0,gp1};
	}

	
	MapItemSetContoller<DataItem> controller;
	TheMapView mv;
	
	public TheMapView getMapView() {
	  return mv;
	}
	
	public void loadIconAndRepaint(String iUrl) {
		try {
			new ImageFetcher(iUrl, true, false) {
				protected void onPostExecute(Bitmap bmp) {
					if (bmp!=null) {
						getMapView().invalidate();
					}
				}
			};
		} catch (MalformedURLException e) {
		}
	}

	public GeoItemsOverlay(MapItemSetContoller<DataItem> controller, TheMapView mv) {
		super(controller.getDefaultMarker());
		this.controller=controller;
		this.mv=mv;
		
		Drawable dr=controller.getDefaultPopup();
		
		popupHeight=dr.getIntrinsicHeight(); 
		popupWidth=dr.getIntrinsicWidth();
		
		controller.setOverlay(this);
	}
	
	protected class TheOverlayItem extends GeoOverlayItem<DataItem> {
		
		protected DataItem fas;

		public TheOverlayItem(DataItem fas) {
			super(fas.getPoint().toGeoPoint(), fas.getName(), null);
			this.fas=fas;
		}
		
	    protected int ih=0,iw=0;
        
		@Override
		public Drawable getMarker(int stateBitset) {	    	
	    	Drawable mm=controller.isTooltipAllowed()&&controller.isSelected(fas)
	    	  ?controller.getPopupFor(fas)
	    	  :controller.getMarkerFor(fas)
	        ;
	    	
	    	int _ih=mm.getIntrinsicHeight();
	    	int _iw=mm.getIntrinsicWidth();
	    	
	    	ih=(int) Math.round(_ih*App.self.scaler.densityScaleFactor);
	    	iw=(int) Math.round(_iw*App.self.scaler.densityScaleFactor);
	    	
	    	if (ih!=_ih) {
	    	  mm=new ScaleDrawable(mm, 0, App.self.scaler.densityScaleFactor ,App.self.scaler.densityScaleFactor).getDrawable();
	    	  Log.d(TAG,"scale:"+App.self.scaler.densityScaleFactor);
	    	}
	    	
	        return mm;
	   }

		@Override
		public DataItem getItem() {
			return fas;
		}

		@Override
		public int getIntrinsicHeight() {
			return ih;
		}

		@Override
		public int getIntrinsicWidth() {
			// TODO Auto-generated method stub
			return iw;
		}

		@Override
		public Rect getClickableBox(Point fpt, boolean isSel) {
			if (isSel) {
				int h=popupHeight, w=popupWidth;
				Log.d(TAG+".onSelTap b:",w+" "+h);
				Log.d(TAG+".onSelTap fpt:",fpt.toString());
				return new Rect(fpt.x-w/2,fpt.y-h,fpt.x+w/2,fpt.y);				
			} else {
				int h=ih, w=iw;
				Log.d(TAG+".onTap b:",w+" "+h);
				Log.d(TAG+".onTap fpt:",fpt.toString());
				return new Rect(fpt.x-w/2,fpt.y-h,fpt.x+w/2,fpt.y);					
			}
		}
		
	}

	@Override
	protected OverlayItem createItem(int i) {
		return new TheOverlayItem(controller.get(i));
	}


	@Override
	public int size() {
		return controller.size();
	}

}
