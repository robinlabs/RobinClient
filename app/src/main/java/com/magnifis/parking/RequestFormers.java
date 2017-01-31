package com.magnifis.parking;

import android.content.ComponentName;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.magnifis.parking.cmd.SendCmdHandler;
import com.magnifis.parking.cmd.etc.CmdHandlerHolder;
import com.magnifis.parking.cmd.i.ClientStateInformer;
import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.GeoObject;
import com.magnifis.parking.model.UnderstandingStatus;
import com.magnifis.parking.suzie.SuzieService;
import com.magnifis.parking.utils.StringConstants;
import com.magnifis.parking.utils.Utils;
import com.robinlabs.persona.AppNames;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONException;

import static com.magnifis.parking.utils.Utils.isEmpty;
import static com.magnifis.parking.utils.Utils.urlencode;

public class RequestFormers {
	static final String TAG="RequestFormers";
	
	private static StringBuilder formCommonPart(String baseUrl, String ...params) {
		StringBuilder sb=new StringBuilder();
		sb.append(baseUrl.trim());
		sb.append("?clientID=");
		sb.append(App.self.android_id);
		
		Locale loc=App.self.getResLocale();
		if (loc!=null) {
		   String lang=loc.getLanguage();
		   if (!isEmpty(lang)) {
		     sb.append("&lang=");
		     sb.append(lang);	
		   }
		}
		
		if (App.self.robin().isDebugMode() || App.self.robin().isTestingMode())
			sb.append("&api_key=robindev"); 
		else
			sb.append("&api_key=robintest");
		
		sb.append("&version=");
		sb.append(App.self.getPackageInfo().versionName);
		sb.append("&app_id=");
		sb.append(Utils.urlencode(App.self.getString(R.string.app_name)));
		
		String appStore = App.self.getString(R.string.appstore);
		if (!Utils.isEmpty(appStore)) {
			sb.append("&appstore=").append(appStore);
		}
		
		if (params!=null) for (int i=0;i<params.length;i++) {
			if ((i&1)==0) {
			  sb.append('&');
			  sb.append(params[i]);
			  sb.append('=');
			} else
			  sb.append(urlencode(params[i]));
		}
		
		return sb;
	}
	
	/*
	static URL createPoiRequest(String q, DoublePoint location, boolean category, String addr) {
		return createPoiRequest(q,location, category, addr, null);
	}
	*/
	
    static URL createPoiRequest(String q, DoublePoint location, boolean category, String addr, Integer radius,String orderBy) {
    	StringBuilder sb=formCommonPart(StringConstants.poi_url());
    	try {
    		sb.append("&lat=");
    		sb.append(location.getLat());
    		sb.append("&lon=");
    		sb.append(location.getLon());
    		sb.append("&query=");
    		sb.append(urlencode(q));
    		sb.append("&category=");
    		sb.append(category);
    		sb.append("&orderby=");
    		sb.append(orderBy);
    		
    		DoublePoint userloc=UserLocationProvider.readLocationPoint();
    		if (userloc!=null) {
    		  sb.append("&userlat=");
    		  sb.append(userloc.getLat());
    		  sb.append("&userlon=");
    		  sb.append(userloc.getLon());
    		}
    		
    		if (addr!=null) {
    		  sb.append("&address=");
    		  sb.append(URLEncoder.encode(addr));
    		}
    		if (radius!=null) {
    		  sb.append("&rad_meters=");
    		  sb.append(radius);
    		}
    		Log.d(TAG+".createPoiRequest: ", sb.toString());
			return new URL(sb.toString());
       } catch (MalformedURLException e) {
			e.printStackTrace();
	   }	
       return null;
    }
	
    static URL createParkingRequest(DoublePoint location, int radius /*500*/) {
        // "yyyy-MM-dd'T'HH:mm"
        final SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");    	
    	StringBuilder sb=formCommonPart(StringConstants.parking_url());
    	try {
    		sb.append("&total_minutes=60");
           
            sb.append("&start_time=");
            String dt=sf.format(new Date());
            Log.d(TAG+".currentTime=",dt);
            sb.append(dt);
            
            
    		sb.append("&lat=");
    		sb.append(location.getLat());
    		sb.append("&lon=");
    		sb.append(location.getLon());
    		sb.append("&rad_meters=");
    		sb.append(radius);
    		Log.d(TAG+".createParkingRequest: ", sb.toString());
			return new URL(sb.toString());
       } catch (MalformedURLException e) {
			e.printStackTrace();
	   }	
       return null;
    }
    
	public static URL createMagnifisUnderstandingRqUrl(
			Context ma,
			Collection<String> matches
	    ) {
		return createMagnifisUnderstandingRqUrl(ma,matches,null);
	}
	
	public static URL createMagnifisUnderstandingRqUrl(
		Context context,
		Collection<String> matches,
		String clientState
    ) {
    	StringBuilder sb=formCommonPart(StringConstants.understanding_url());
    	try {
    		
    		MainActivity ma=null;
    		
    		if (context!=null) {
    		   ma=(context instanceof MainActivity)?(MainActivity)context:null;
       	       if (context instanceof SuzieService)
       	    	   sb.append("&button=1");
    		   if (ma!=null) try {
    			 String tab=ma.getActiveTabId();
    			 if (!isEmpty(tab)) sb.append("&channeltab=").append(URLEncoder.encode(tab,"UTF-8"));
    		   } catch (Throwable t) {}
    		}
    		
    		UnderstandingStatus us=UnderstandingStatus.get();
    		MAStatus status=((us==null)?null:us.status);
    		
    		TimeZone tz=TimeZone.getDefault();
    		
    		sb.append("&proximity_sensor=");
    		sb.append(App.self.anyProximitySensor()?1:0);
    		
    		
    		sb.append("&tzid=");
    		try {
				sb.append(URLEncoder.encode(tz.getID(),"UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		sb.append("&tzraw=");
    		sb.append(tz.getRawOffset());
    		
			if (SendCmdHandler.isActive()) {
				String text_message = SendCmdHandler.getText();
				if (!Utils.isEmpty(text_message)) {
					sb.append("&text_message=");
					try {
						sb.append(URLEncoder.encode(text_message,"UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
                String text_message_to = SendCmdHandler.getRecipient();
                if (!Utils.isEmpty(text_message_to)) {
                    sb.append("&text_message_to=");
                    try {
                        sb.append(URLEncoder.encode(text_message_to, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
			}

    		if (status!=null) {

    			String prevCommand=status.getLastCommand(),
    					prevDomain=status.getLastCommandDomain();

    			if (!isEmpty(prevCommand)) {
    				sb.append("&prev_command=");
    				try {
    					sb.append(URLEncoder.encode(prevCommand,"UTF-8"));
    				} catch (UnsupportedEncodingException e) {
    					e.printStackTrace();
    				}
    			}

    			if (!isEmpty(prevDomain)) {
    				sb.append("&prev_domain=");
    				try {
    					sb.append(URLEncoder.encode(prevDomain,"UTF-8"));
    				} catch (UnsupportedEncodingException e) {
    					e.printStackTrace();
    				}
    			}


    			DoublePoint lastLocation=status.getDestinationLocation();
    			if (lastLocation==null&&ma!=null) {
    				GeoObject obj=ma.getSelected();
    				if (obj!=null) lastLocation=obj.getPoint();
    			}

    			if (lastLocation!=null) {
    				sb.append("&lastLat=");
    				sb.append(lastLocation.getLat());
    				sb.append("&lastLon=");
    				sb.append(lastLocation.getLon());
    			}

    		} // status
    		
    		DoublePoint location=UserLocationProvider.readLocationPoint();
    		if (location!=null) {
    			sb.append("&lat=");
    			sb.append(location.getLat());
    			sb.append("&lon=");
    			sb.append(location.getLon());
    			
    			String s = UserLocationProvider.getCountry();
    			if (!Utils.isEmpty(s))
        			sb.append("&country="+s);
    		}

            try {
                boolean carMode = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("carMode", false);
                sb.append("&car=" + carMode);
            } catch (Exception e) {
                e.printStackTrace();
            }

    		for (String s:matches) {
    		  sb.append("&query=");
    		  try {
    			  sb.append(URLEncoder.encode(s,"UTF-8"));
    		  } catch (UnsupportedEncodingException e) {
    			  // TODO Auto-generated catch block
    			  e.printStackTrace();
    		  }
    		}
    		if (!isEmpty(clientState)) {
    			sb.append("&state=");
    			sb.append(clientState);
    		} else {
			  ClientStateInformer csi = CmdHandlerHolder.getClientStateInformer();
			  String state=(csi!=null)?csi.getClientStateName():CommandsStateHandler.getState();
			  if (!isEmpty(state)) {
				  sb.append("&state=");
				  sb.append(state);
			  }
    		}
    		
    		// get the top activity 
    		ComponentName topActivity = Utils.getTopActivity(); 
    		if (topActivity != null) {
    			String pkName = topActivity.getPackageName();
    			if (!(pkName.startsWith("com.magnifis.") ||
    				  pkName.startsWith("android."))) {
    				sb.append("&appcontext=").append(URLEncoder.encode(pkName));
    			}
    		}
    		
    		
    		
//    		// TODO: temp!!!
//    		if (App.self.robin().isDebugMode())
//				try {
//					sb.append("&user_object=").append(URLEncoder.encode("{\"user_id\":\"12345678\", \"contextlist\":[]}", "UTF-8"));
//				} catch (UnsupportedEncodingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} 
////    		catch (JSONException e) {
////					// TODO Auto-generated catch block
////					e.printStackTrace();
////				} 
////    		
    		
    		
    		
    		Log.d(TAG+".createMagnifisUnderstandingRqUrl: ", sb.toString());
			return new URL(
					sb.toString()
					//"http://173.255.249.124:8080/mageo/mageo?lat=31.8431758&lon=35.24332125&query=starbucks"
			);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
	
    static URL createTrafficRequest() {
	  DoublePoint dp=UserLocationProvider.readLocationPoint();
	  if (dp==null) return null;
      return createTrafficOrNewsRequest(dp,dp,true,null);
    }
	
    static URL createTrafficOrNewsRequest(
    	DoublePoint usrLoc,DoublePoint dstLoc,boolean dst_only,
    	String topic,
    	String ...params
    ) {
    	StringBuilder sb=formCommonPart(StringConstants.traffic_url(),params);
    	try {  		
    		if (usrLoc!=null) {
    		// user_lat/user_lon  -current user location
				sb.append("&user_lat=");
				sb.append(usrLoc.getLat());
				sb.append("&user_lon=");
				sb.append(usrLoc.getLon());
    		}
    		if (dstLoc!=null) {
    			// lat/lon - destination coordinates
    			// ("traffic in San Francisco" ==> San Francisco is the destination)
    			sb.append("&lat=");
    			sb.append(dstLoc.getLat());
    			sb.append("&lon=");
    			sb.append(dstLoc.getLon());
    			// dest_only (boolean) - provide traffic just for the destination point (default). 
    			// Otherwise the server will consider the geo-range between the source and destination. 
    			sb.append("&dest_only=");
    			sb.append(dst_only);
    		}
    		
    		if (topic!=null) {
        		sb.append("&news=");
        		sb.append(urlencode(topic)); 			
    		}
    		
    		Log.d(TAG+".createTrafficRequest: ", sb.toString());
    		return new URL(sb.toString());
    	} catch (MalformedURLException e) {
    		e.printStackTrace();
    	}	
    	return null;
    }
    
    static URL createWeatherRequest(DoublePoint dp) {

    	StringBuilder sb=new StringBuilder(
        		"http://api.openweathermap.org/data/2.5/forecast/daily?mode=xml&units=imperial&cnt=4&APPID="
        	);
        	try {
    			sb.append("e78e7da8cd6a8c32f401d4e9368daabe");
    			sb.append("&lat=").append(dp.getLat());
    			sb.append("&lon=").append(dp.getLon());
    			Log.d(TAG+".createWeatherRequest: ", sb.toString());
    			return new URL(sb.toString());
    		} catch (Throwable e) {
    			e.printStackTrace();
    		}
        	return null;
    	
    	
    	
    	//    	StringBuilder sb=new StringBuilder("http://www.google.com/ig/api?weather=");
//    	try {
//			sb.append(URLEncoder.encode(address,"UTF-8"));
//			Log.d(TAG+".createWeatherRequest: ", sb.toString());
//			return new URL(sb.toString());
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
//    	return null;
    } 
    
    public static URL createWorldWeatherOnlineRequest(DoublePoint dp) {
    	StringBuilder sb=new StringBuilder(
    		"http://api.worldweatheronline.com/free/v1/weather.ashx?num_of_days=4&format=xml&key="
    	);
    	try {
			sb.append("ffvj34nutxkram3n7ah7a7vh");
			
			// New key, may be limited to 1000 requests/day: 
			// sb.append("939d10ae282f1413251328f29d741");
			
			sb.append("&q=");
			sb.append(dp.toString());
			Log.d(TAG+".createWeatherRequest: ", sb.toString());
			return new URL(sb.toString());
		} catch (Throwable e) {
			e.printStackTrace();
		}
    	return null;
    } 

}
