package com.magnifis.parking.views;


import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;
import com.magnifis.parking.App;
import com.magnifis.parking.Config;
import com.magnifis.parking.Log;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.R;
import com.magnifis.parking.model.DoublePoint;

public class TheMapView extends MapView {
	static final String TAG="TheMapView";
	
	private Runnable onScrollListener=null;
	
	
	@Override
	public boolean onTouchEvent(android.view.MotionEvent ev) {
		if (gd.onTouchEvent(ev)) return true;
        switch(ev.getAction()&MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_MOVE:
            MainActivity.get().disableMyLocation();
        	if (onScrollListener!=null) onScrollListener.run();
        	break;
        case MotionEvent.ACTION_DOWN:
        	break;
        case MotionEvent.ACTION_UP:	

        }
        return super.onTouchEvent (ev);
	}

	public Runnable getOnScrollListener() {
		return onScrollListener;
	}

	public void setOnScrollListener(Runnable onScrollListener) {
		this.onScrollListener = onScrollListener;
	}
	
	GestureDetector gd;
	
	private void init(Context ctx) {
	  gd=new GestureDetector(
		 ctx, 
		 new  GestureDetector.OnGestureListener() {

			@Override
			public boolean onDown(MotionEvent arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onFling(MotionEvent arg0, MotionEvent arg1,
					float arg2, float arg3) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onLongPress(MotionEvent arg0) {
				if (App.self.isVwVersion) {
					MainActivity.get().onInfo(null);
				} else {
					MainActivity.get().swapMapView();
				}
			}

			@Override
			public boolean onScroll(MotionEvent arg0, MotionEvent arg1,
					float arg2, float arg3) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onShowPress(MotionEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean onSingleTapUp(MotionEvent arg0) {
				// TODO Auto-generated method stub
				return false;
			}
			 
		 }
	  );
	  gd.setIsLongpressEnabled(true);
      gd.setOnDoubleTapListener(
    	new OnDoubleTapListener() {

			@Override
			public boolean onDoubleTap(MotionEvent e) {
				int zl=getZoomLevel();
				if (zl<getMaxZoomLevel()) {
					getController().setZoom(zl+1);
					return true;
				}
				return false;
			}

			@Override
			public boolean onDoubleTapEvent(MotionEvent e) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				// TODO Auto-generated method stub
				return false;
			}
    		
    	}
      );
	}

	public TheMapView(Context context, String apiKey) {
		super(context, apiKey);
		init(context);
	}

	public TheMapView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	final private long MATURING_TIME=1500;
	
	private long creationTime=System.currentTimeMillis();
	
	public long timeToWaitMaturing() {
	   long w=System.currentTimeMillis()-creationTime;
	   return (w>=MATURING_TIME)?0:(MATURING_TIME-w);
	}
	
	public boolean isRecentlyCreated() {
	   return System.currentTimeMillis()-creationTime<MATURING_TIME;
	}

	public boolean waitForMaturing() {
	   long w=System.currentTimeMillis()-creationTime;
	   if (w<MATURING_TIME)
		try {
			Thread.sleep(MATURING_TIME-w);
		} catch (InterruptedException e) {
            return false;
		}
	   return true;
	}

	public TheMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	/* (non-Javadoc)
	 * @see com.google.android.maps.MapView#displayZoomControls(boolean)
	 */
	@Override
	public void displayZoomControls(final boolean takeFocus) {
	//	Log.d(TAG,"displayZoomControls");
		postDelayed(
		  new Runnable() {
			public void run() {
			  if (!dontDiplayZoom) TheMapView.super.displayZoomControls(takeFocus);
			}
		  },
		  500
		);
	}
	
	public boolean dontDiplayZoom=false;
	
	private int lOff=0, tOff=0, rOff=0, dOff=0;
	private int myPaddingLeft = 0, myPaddingTop = 0;

	public void setMyPadding(int paddingLeft, int paddingTop) {
        myPaddingLeft = paddingLeft;
        myPaddingTop = paddingTop;
    }

	public void recalculateBounds() {

        if ((android.os.Build.VERSION.SDK_INT < 11) || !Config.rotate_map)
            return;
/*
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) App.self.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)getLayoutParams();
        if (metrics.widthPixels > metrics.heightPixels)
            lp.setMargins(-myPaddingTop, -myPaddingLeft, -myPaddingTop, -myPaddingLeft);
            //setPadding(0, myPadding, 0, myPadding);
        else
            lp.setMargins(-myPaddingLeft, -myPaddingTop, -myPaddingLeft, -myPaddingTop);
            //setPadding(myPadding, 0, myPadding, 0);
        setLayoutParams(lp);
*/
            /*
            mapView.setLayoutParams(new RelativeLayout.LayoutParams(m,m));
            //mapView.setPadding(300, 300, 300, 300);
            mapView.setLeft(-300);
            mapView.setX(-300);*/
/*
            RelativeLayout mapContainer = new RelativeLayout(this);
            mapContainer.setLayoutParams(lp);

            mapContainer.addView(mapView);
            */
        // //
        //mapView.setLeft(px);
        //mapView.setTop(py);

        /*
	  final DoublePoint mc=getActualMapCenter();
	  postDelayed(
		 new Runnable() {

			@Override
			public void run() {

				  MainActivity ma=MainActivity.get();	//(ViewGroup)getRootView();
				  if (ma != null) { 
					  View leftPanel=ma.findViewById(R.id.LeftPanel);
					  View bottomPanel=ma.findViewById(R.id.BottomPanel);
					  if (leftPanel!=null&&leftPanel.isShown()) lOff=leftPanel.getWidth(); else lOff=0;
					  if (bottomPanel!=null&&bottomPanel.isShown()) dOff=bottomPanel.getHeight(); else dOff=0;
					  moveTo(mc,false,false);
				  }
			}
			 
		 }
		 , 
		 100
	  );*/

	}
	
	public DoublePoint getActualMapCenter() {
	   DoublePoint mc=new DoublePoint(getMapCenter());
		Projection p=getProjection();
		DoublePoint lt=new DoublePoint(p.fromPixels(lOff, tOff)),
				    rd=new DoublePoint(p.fromPixels(getWidth()-rOff, getHeight()-dOff));
		
	    return DoublePoint.center(lt, rd);
	}
	
	public DoublePoint getSpan() {
		return new DoublePoint(getLatitudeSpan()/1e6,getLongitudeSpan()/1e6);
	}

	public void moveTo(DoublePoint dp, boolean fAnimate, boolean isCurrentLocation) {

        if (!isCurrentLocation)
            MainActivity.get().disableMyLocation();

        Projection p=getProjection();
		DoublePoint lt=new DoublePoint(p.fromPixels(0, 0)),
				    rd=new DoublePoint(p.fromPixels(getWidth()-1, getHeight()-1));
		
		DoublePoint spn=DoublePoint.span(lt, rd);
		
		Log.d(TAG, "span="+spn.toString());
		Log.d(TAG, "latspan="+this.getLatitudeSpan());
		Log.d(TAG, "lonspan="+this.getLongitudeSpan());
		
		double xRes=spn.getLon()/getWidth(), yRes=spn.getLat()/getHeight();
		
		DoublePoint center=dp.clone()
				.incLon((rOff-lOff)*xRes/2)
				.incLat((tOff-dOff)*yRes/2)
			;
		if (fAnimate)
		  getController().animateTo( center.toGeoPoint() );		
		else
		   getController().setCenter( center.toGeoPoint() );	
	}
	
		
	public int moveAndZoomToCover(DoublePoint orgLoc, DoublePoint dstLoc) {
		
		DoublePoint spn=DoublePoint.span(orgLoc, dstLoc);//.mulLat(1.2).mulLon(1.2);
		
		double xRes=spn.getLon()/(getWidth()-lOff-rOff), yRes=spn.getLat()/(getHeight()-tOff-dOff);
        
		DoublePoint center=DoublePoint.center(orgLoc, dstLoc)
			.incLon((rOff-lOff)*xRes/2)
			.incLat((tOff-dOff)*yRes/2)
		;
		
		getController().animateTo( center.toGeoPoint() );
		
		GeoPoint gp=DoublePoint.span(orgLoc, dstLoc)
				 .incLon((lOff+rOff)*xRes)
				 .incLat((lOff+dOff)*yRes)
			//	 .mulLat(1.2)
			//	 .mulLon(1.2)
				 .toGeoPoint();
		
		getController().zoomToSpan(gp.getLatitudeE6(),gp.getLongitudeE6());
		return getZoomLevel();
	}

}
