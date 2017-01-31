package com.magnifis.parking;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.magnifis.parking.geo.GoogleGeocoder;
import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.views.TheMapView;

import static com.google.android.gms.common.GooglePlayServicesClient.*;
import static com.magnifis.parking.utils.Utils.*;
import static android.location.GpsStatus.*;


public class UserLocationProvider {
	final static String TAG=UserLocationProvider.class.getSimpleName();
	
	public final static int LOC_UNAVAILABE=0, LOC_GPS=1, LOC_COLD_GPS=2, LOC_NONE_GPS=3; 
	
	public static DoublePoint readLocationPoint() {
		LocationInfo li=queryLocation();
		if (li!=null) {
			DoublePoint dp = li.getLocationDP();

            /*
			if (dp!=null&&(lastLocationPointForCountry==null||dp.distance(lastLocationPointForCountry, 'K') > 100)) {
				lastLocationPointForCountry = dp;
				new Thread("get country") {
					@Override
					public void run() {
						try {
							lastCountry = GoogleGeocoder.getFromLatlonCountry(lastLocationPointForCountry);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
			*/
			return dp;
		} 
		return null;
	}
	
	private static String lastCountry = null;
	private static DoublePoint lastLocationPointForCountry = null;
	
	public static String getCountry() {
		return isEmpty(lastCountry)?App.self.getSimCountryIso():lastCountry;
	}
	
	public static class LocationInfo {
		protected int sensorStatus=LOC_NONE_GPS;
		protected Location location=null;
	
		public Long getAge() {
		   return (location==null)?null:(System.currentTimeMillis()-location.getTime());
		}
		
		final static double STD_CITY_SPEED=50.*1000./3600.; /*50 km/h in m/s*/
		
		public double getEffectiveAccuracy() {
		   if (location!=null) {
		     double speed=location.getSpeed(),accuracy=location.getAccuracy(), timeInMeters=getAge()*speed;
		     if (speed<STD_CITY_SPEED) speed=STD_CITY_SPEED;
		     return Math.sqrt(timeInMeters*timeInMeters+accuracy*accuracy);
		   }  
		   return 0.;
		}
		
		public boolean isExact() {
			return getEffectiveAccuracy()<=50;
		}
		public LocationInfo(int sensorStatus, Location location) {
			this.sensorStatus=sensorStatus;
			this.location=location;
		}
		
		public int getSensorStatus() {
			return sensorStatus;
		}
		
		public Location getLocation() {
			return location;
		}
		
		public DoublePoint getLocationDP() {
			return DoublePoint.from(location);
		}
		
		public void setLocation(Location location) {
			this.location = location;
		}
		
	};

	public static LocationInfo queryLocation() {
       if (lc == null || !lc.isConnected()) {
            start();
            return new LocationInfo(LOC_UNAVAILABE,null);
        }

		Location loc = lc.getLastLocation();
		if (loc==null)
            return new LocationInfo(LOC_UNAVAILABE,null);

        return new LocationInfo(LOC_COLD_GPS, loc);
   }
	
	public LocationInfo reportLocation() {

        if (lc == null || !lc.isConnected()) {
            start();
            return null;
        }

       int waitMillis = 0;

       final long STEP=200l;
	   for (;;) { 
		   Location loc=null;
		   if (overlay==null) {
               loc = lc.getLastLocation();
		   } else {
		        loc=overlay.getLastFix();
           }

		   boolean ok=loc!=null;
		   if (ok) {
               //lastLoc = loc;
			   if (overlay!=null) onLocationChanged(loc, true);
		   } else  {
			   if (waitMillis<=0)  return new LocationInfo(LOC_UNAVAILABE,null);
			   try {
				Thread.sleep(STEP);
			   } catch (InterruptedException e) {
				e.printStackTrace();
			   }
			   waitMillis-=STEP;
			   continue;
		   }
		   if (LocationManager.GPS_PROVIDER.equals(loc.getProvider())) 
			    return new LocationInfo(LOC_GPS,loc);
            else
		        return new  LocationInfo(LOC_COLD_GPS, loc); // TODO: too harsh
	   }
	 }

    static boolean myLocationEnabled = false;
	public synchronized void enableMyLocation() {
        if (overlay==null || !myLocationEnabled)
            return;

        myLocationEnabled = true;

        mv.post(
                new Runnable() {

                    @Override
                    public void run() {
                        if (overlay != null) {
                            overlay.enableMyLocation();
                            try {
                                if (lc != null)
                                    lc.requestLocationUpdates(lr, ll);
                            } catch (Exception e) {}
                            reportLocation();
                        }
                    }
                }
                );
	}

    public synchronized void disableMyLocation() {
        if (overlay==null)
            return;

        myLocationEnabled = false;
        mv.post(
                new Runnable() {

                    @Override
                    public void run() {
                        if (overlay != null) {
                            overlay.disableMyLocation();
                            try {
                                if (lc != null)
                                    lc.removeLocationUpdates(ll);
                            } catch (Exception e) {}
                        }
                    }
                }
        );
    }

    static private TheMapView mv=null;
    private static com.google.android.gms.location.LocationClient lc=null;
    private static LocationNotify ln=null;
    private static LocationListener ll=null;
    private static com.google.android.gms.location.LocationRequest lr;
	
	private class UserLocationOverlay extends MyLocationOverlay {

		public UserLocationOverlay(Context context, MapView mapView) {
			super(context, mapView);
		}

        public void super_onLocationChanged(Location location) {
            super.onLocationChanged(location);
        }

		@Override
		public void onLocationChanged(Location location) {
			UserLocationProvider.this.onLocationChanged(location, false);
		}
		
	};
	
	static private UserLocationOverlay overlay=null;
	
	public MyLocationOverlay getOverlay() {
		return overlay;
	}
	
	public MyLocationOverlay bind(TheMapView mapView) {

        start();

        if (overlay==null) {
		overlay=new  UserLocationOverlay(mapView.getContext(), mapView);
		mv=mapView;
	  }
	  return overlay;
	}

    static class LocationNotify implements
            ConnectionCallbacks,
            OnConnectionFailedListener {

        @Override
        public void onConnected(Bundle bundle) {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }
    }

    public static void start() {

        if (ln != null)
            return;

        ln = new LocationNotify();

        lc = new LocationClient(App.self.getApplicationContext(), ln, ln);
        lc.connect();

        // Create the LocationRequest object
        lr = com.google.android.gms.location.LocationRequest.create();
        // Use high accuracy
        lr.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 30 seconds
        lr.setInterval(30*1000);
        // Set the fastest update interval to 1 second
        lr.setFastestInterval(30*1000);

        ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                UserLocationProvider.onLocationChanged(location, true);
            }
        };

        // rotation
        if ((android.os.Build.VERSION.SDK_INT >= 11) && Config.rotate_map) {
            SensorEventListener sl = new SensorEventListener() {
                @Override
                public void onSensorChanged(final SensorEvent event) {
                    if (mv != null && event != null && event.values.length > 0) {
                        try {
                            mv.post(new Runnable() {
                                @Override
                                public void run() {
                                    //if (myLocationEnabled)
                                        if (App.self.isInLanscapeMode())
                                            mv.setRotation(90-event.values[0]);
                                        else
                                            mv.setRotation(-event.values[0]);
                                }
                            });
                        } catch (Exception e) {
                        }
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };

            SensorManager mSensorManager = (SensorManager) App.self.getSystemService(Context.SENSOR_SERVICE);
            mSensorManager.registerListener(sl, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
        }
    }

	public UserLocationProvider(Context ctx) {
        start();
    }

	private static void onLocationChanged(Location location, boolean fCenter) {
		if (location!=null) {
			final DoublePoint lc=DoublePoint.from(location);
            if (mv != null)
			    mv.moveTo(lc, true, true);

            if (overlay != null)
                overlay.super_onLocationChanged(location);
		}
	}

}
