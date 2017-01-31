package com.magnifis.parking.traffic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import android.graphics.Color;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.routing.RouteInfo;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Overlay;
import com.magnifis.parking.App;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.Phrases;
import com.magnifis.parking.R;
import static com.magnifis.parking.VoiceIO.*;
import com.magnifis.parking.model.GooDirectionsScraperParams;
import com.magnifis.parking.utils.Utils;

import compat.org.json.JSONArray;
import compat.org.json.JSONException;
import compat.org.json.JSONObject;

import android.location.Location;
import android.media.MediaPlayer;
import android.util.Log;

public class EtaMonitor {
	static final String TAG=EtaMonitor.class.getSimpleName();
	static final Pattern durationPattern =  Pattern.compile("(\\d hour(s)*)?\\s*(\\d min(s)*)?");
	static final int MIN_ABS_DELAY_2REPORT = 5; // minutes
	static final float MIN_REL_DELAY_2REPORT = 0.15f; // 15%
	
//	MediaPlayer tunePlayer;
//	static boolean isReportReady = false; 
	
	static GooDirectionsScraperParams params = new GooDirectionsScraperParams(); 
	static {
		params.setBaseURL("https://maps.google.com/maps?ie=UTF-8"); 
		params.setSourceParamName("saddr"); 
		params.setDestParamName("daddr"); 
		params.setCurEtaXpath(".dir-altroute div[class=altroute-rcol altroute-aux] span"); 
		params.setNormalEtaXpath(".dir-altroute div[class=altroute-rcol altroute-info] span:eq(1)"); 
		params.setRouteLengthXpath(".dir-altroute div[class=altroute-rcol altroute-info] span:eq(0)"); 
		params.setRouteNameXpath(".dir-altroute .dir-altroute-inner div:eq(2)"); 
	}
	
	public static void setParams(GooDirectionsScraperParams params) {
		EtaMonitor.params = params; 
	}
	
	public EtaMonitor() {
		//tunePlayer = MediaPlayer.create(MainActivity.get(), R.raw.mission_theme1);
	}
	
	
	public void getRoutesAndEtas(final String from, final String to, boolean isFromCurrentLocation, 
			
			final boolean addVisuals) {
	
		if (Utils.isEmpty(to) || Utils.isEmpty(from)) {
			return;
		}
		
		
//		synchronized (EtaMonitor.class) {
//			isReportReady = false; 
//		}
		
		
		Runnable routeRetriever = new Runnable() {
			
			@Override
			public void run() {
				
				

				RouteSummary[] routes = retrieveRouteOptions(from, to); 
		    	if (routes != null && routes.length > 0) { 
		    		int nRoutes = routes.length; 
		    		
		    		String msg = "", 
		    			   preamble = Phrases.pickDonePhrase() + " " + App.self.getString(R.string.etamonitor_preamble_for) + " "  + to + ", "; 
		    		if (nRoutes > 1) { 
		    			preamble += App.self.getString(R.string.etamonitor_preamble_have_a_choice) + " " + nRoutes + " " + App.self.getString(R.string.etamonitor_preamble_routes_take) + " "; 
		    		} else {
		    			preamble += App.self.getString(R.string.etamonitor_preamble_route_take) + " "; 
		    		}

		    		int iRoute = 0; 
		    		for (RouteSummary route : routes) {
		    			msg += route.getRouteName() 
		    					//+ ", " + route.getRouteLength() + "miles long"
		    					;
		    			int trafficDuration = route.getTrafficDuration(), 
		    				normalDuration = route.getDurationMinutes();
		    			if (trafficDuration > 0) {
		    				boolean doMentionTraffic = 
		    						((nRoutes-1 == iRoute) || (nRoutes > 2 && 0 == iRoute)); 
		    				msg += App.self.getString(R.string.etamonitor_about) + " " + formatDuration(trafficDuration, doMentionTraffic); 
		    			} else if (normalDuration > 0) {
		    				msg += App.self.getString(R.string.etamonitor_about) + " " + formatDuration(normalDuration, false); 
		    				msg += " " + App.self.getString(R.string.etamonitor_things_go_well);
		    			}
		    			if ( iRoute < 1 && iRoute++ < nRoutes-1) {
		    				msg += App.self.getString(R.string.etamonitor_you_can_take) + " "; 
		    			} else if (iRoute++ < nRoutes-1) {
		    				msg += App.self.getString(R.string.etamonitor_also_you_can_take) + " ";
		    			}
		    			
		    		} 
		    		
		    		sayFromGui(preamble + msg);
		    		if (nRoutes > 1) { // repeat 
		    			sayFromGui(App.self.getString(R.string.etamonitor_again) + " " + msg);
		    		} 
		    		
		    		
//		    		synchronized (EtaMonitor.class) {
//						isReportReady = false;
//					}
		    	} else { // no result s
		    		sayFromGui(App.self.getString(R.string.etamonitor_not_find_any_traffic)); 
	    		}	
		    	sayFromGui(App.self.getString(R.string.etamonitor_ask_me_to_navigate) + " " + to); 
			}

			

			private String formatDuration(int trafficDuration, boolean isInTraffic) {
				
				int hours = trafficDuration / 60; 
				int minutes = trafficDuration % 60; 
				String str = "";
				
				Log.d(TAG, "Formatting route duration: " + trafficDuration); 
				
				if (hours > 1) 
					str += Integer.toString(hours) + " " + App.self.getString(R.string.etamonitor_hours) + " "; 
				else if (1 == hours)
					str += Integer.toString(hours) + " " + App.self.getString(R.string.etamonitor_hour) + " ";
				
				if (minutes > 1 ||
						(hours < 1 && 1 == minutes)) {
					str += Integer.toString(minutes) + " " + App.self.getString(R.string.etamonitor_minutes) + " ";
				}
					
				if (isInTraffic) {
					str += Phrases.pickCurrentTrafficCondPhrase(); 
				} 
 				
				return str;
			}
		};
		
		String fromLoc = (isFromCurrentLocation) ? App.self.getString(R.string.etamonitor_your_location)
												 : from; 
		sayFromGui(Phrases.pickConfirmationPrefix() +  
				" " + App.self.getString(R.string.etamonitor_check_traffic) + " " + fromLoc + " " + App.self.getString(R.string.etamonitor_and) + " " + to + " ... "
				+ Phrases.pickTrafficIntelligencePhrase());
		
		routeRetriever.run(); 
		

		
		
		// TODO: remove
//		MainActivity.runGuiAfterTheSpeach(new Runnable() {
//			
//        	public void run() { 
//        	   try {
//				Thread.sleep(100);
//			   } catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			   }
//               
//        	   tunePlayer.start();
//               synchronized (EtaMonitor.class) {
//					while(!isReportReady) {
//						try {
//							EtaMonitor.class.wait(2000);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						} 
//					}
//               }
//               tunePlayer.stop();
//               tunePlayer.release();
//        	}
//         });
//         
	
		
		if (addVisuals) {
			Runnable routePolyinesRetriever = new Runnable() {

				@Override
				public void run() {
					createRoutePolyline(from, to); // TODO: separate thread?? 

				}	
			};
			
			routePolyinesRetriever.run(); 
		}
	}

	
	public RouteSummary[] retrieveRouteOptions(String from, String to) {
		
		RouteSummary[] routes = {};
		StringBuffer urlBuf = new StringBuffer(params.getBaseURL()); 
		String strURL = null; 
		try {
			urlBuf.append("&").append(params.getDestParamName()).append("=").append(URLEncoder.encode(to,"UTF-8"));
			urlBuf.append("&").append(params.getSourceParamName()).append("=").append(URLEncoder.encode(from,"UTF-8"));
			strURL = urlBuf.toString(); 
			Log.i(TAG, "G-directions request: " + strURL); 
		} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
		}
		
		Log.i(TAG, "sending G-directions request: " + strURL); 
		Connection conn = Jsoup.connect(strURL); 
		Document dom;
		
		
		try {
			Log.i(TAG, "G-directions obtained ..."); 
			dom = conn.get();
			Log.i(TAG, "G-directions DOM built"); 
			
			// TODO: remove now!!!
//			Log.i(TAG, "G-directions DOM: \n" + dom.toString()); 
			
//			synchronized (EtaMonitor.class) {
//				isReportReady = true;
//				EtaMonitor.class.notify(); 
//			}

			
			//Element el = null; 
			Elements curEtaElements = dom.select(params.getCurEtaXpath());
			Elements normalEtaElements = dom.select(params.getNormalEtaXpath());
			Elements lengthElements = dom.select(params.getRouteLengthXpath());
			Elements nameElements = dom.select(params.getRouteNameXpath());
			
			
			routes = extractRouteSummaries(curEtaElements, normalEtaElements, 
											lengthElements, nameElements); 
			
			if (routes != null)
				Log.i(TAG, "# extracted routes: " + routes.length); 
						
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return routes; 
		
	}

	
	protected RouteSummary[] extractRouteSummaries(Elements curEtaElements,
			Elements normalEtaElements, Elements lengthElements,
			Elements nameElements) {
		
		int nRoutes = nameElements.size(); 
		RouteSummary[] routes = new RouteSummary[nRoutes]; 
		
		int iRoute = 0, nEmptyNames = 0; // name
		for (Element el : nameElements) {
			String routeName = el.text(); 
			if (Utils.isEmpty(routeName)) {
				routeName = App.self.getString(R.string.etamonitor_route_number) + " " + (iRoute + 1); 
				nEmptyNames++; 
			} else {
				routeName = routeName.replace(" " + App.self.getString(R.string.etamonitor_and) + " ", " " + App.self.getString(R.string.etamonitor_to) + " "); // "hwy1 and hwy2" ==> "hwy1 to hwy2
			}
			
			routes[iRoute] = new RouteSummary(routeName);		
			if (++iRoute >= nRoutes)
				break; 
		}
		
		iRoute = 0; // length
		for (Element el : lengthElements) {
			String strRouteLength = el.text(); 
			String[] valueAndUnit = strRouteLength.split("[ ]"); 
			
			if (valueAndUnit != null && 2 == valueAndUnit.length) { try {
				String sub[] = valueAndUnit[0].split(","); // over a thousand miles?
				float length = Float.parseFloat(sub[0]); 
				if (sub.length > 1) {
					length *= 1000; 
					length +=  Float.parseFloat(sub[1]); 
					routes[iRoute].routeLength = length; 
				}
				routes[iRoute].lengthUnits = valueAndUnit[1];
			}catch (NumberFormatException e) {
					e.printStackTrace(); 
			}}
			if (++iRoute >= nRoutes)
				break; 
		}
		
		iRoute = 0; // duration
		for (Element el : normalEtaElements) {
			String strDuration = el.text(); 
			routes[iRoute].durationMinutes = extractDurationInMinutes(strDuration); 
			if (++iRoute >= nRoutes)
				break; 
		}
		
		iRoute = 0; // duration in traffic 
		for (Element el : curEtaElements) {
			String strDuration = el.text(); 
			
			// This string should start with "In current traffic:"
			strDuration = strDuration.replaceAll("[(\\w|\\s)]+:", ":"); 
			routes[iRoute].trafficDuration = extractDurationInMinutes(strDuration); 
			if (++iRoute >= nRoutes)
				break; 
		}
		
		if (nEmptyNames >= nRoutes && curEtaElements.isEmpty()) { // no traffic info at all and/or something went wrong 
			return new RouteSummary[0]; 
		}
		
		
		// reorder to have the fastest route on top 
		//for (RouteSummary rou)
		
		
		return routes;
	}

	private int extractDurationInMinutes(String strDuration) {
		
		int durationMinutes = -1; 
		int iFirst = 0; 
		String[] valuesAndUnits = strDuration.split("[ ]"); 
		
		// find the first numeric value 
		for (; iFirst < valuesAndUnits.length && 
				(Utils.isEmpty(valuesAndUnits[iFirst]) || 
				 !Character.isDigit(valuesAndUnits[iFirst].charAt(0))); iFirst++); 
		
		if (iFirst < valuesAndUnits.length && 
				valuesAndUnits != null &&  valuesAndUnits.length >= 2) { try {
			int nHours = 0, nMinutes = 0; 
			
			for (int iVal = iFirst; iVal < valuesAndUnits.length-1; iVal += 2) {
				int curVal  = Integer.parseInt(valuesAndUnits[iVal]); 
				String units = valuesAndUnits[iVal+1].toLowerCase(); 
				if (units.startsWith("hour")) {
					nHours = curVal; 
				} else if (units.startsWith("min")) {
					nMinutes = curVal; 
				}
			}
			
			if (nHours >= 0 && nMinutes >= 0) {
				durationMinutes = nHours*60 + nMinutes;
			
				// and a small random number
				int maxPadding = (int)(Math.min((float)durationMinutes*0.15f, 5) + 0.5);  			
				if (maxPadding > 0)
					durationMinutes += new Random((int)(System.currentTimeMillis()%1000000)).nextInt(maxPadding);
			}
				
		}catch (NumberFormatException e) {
				e.printStackTrace(); 
		}catch (NullPointerException e) {
			e.printStackTrace();
		}}
		
		return durationMinutes; 
	}

	
	public Map<String, Integer> getDelays(String strSrc, String strDest) {
	
		Map<String, Integer> delays = new HashMap<String, Integer>(); 
		RouteSummary[] routes = retrieveRouteOptions(strSrc, strDest);
		if (!Utils.isEmpty(routes)) {
			for (RouteSummary route : routes) {
				int normalTime = route.getDurationMinutes(), 
					trafficTime = route.getTrafficDuration(); 
				
				if (trafficTime > 0 && normalTime > 0) {
					int delay = trafficTime - normalTime; 
					if (delay >= MIN_ABS_DELAY_2REPORT && 
							(float)delay/normalTime >= MIN_REL_DELAY_2REPORT) {
						delays.put(route.routeName, delay); 
						
					}
				}
						
			}
			
		}
		
		return delays; 
	}
	
	
	
	void createRoutePolyline(String from, String to) {
		
		if (null ==MainActivity.getActive())
			return; // no use, since there is no map activiy to draw this stuff
		
		String url = null; 
		StringBuffer urlBuf = new StringBuffer("http://maps.googleapis.com/maps/api/directions/json?sensor=true&alternatives=true"); 
		try {
			urlBuf.append("&origin=").append(URLEncoder.encode(from,"UTF-8"));
			urlBuf.append("&destination=").append(URLEncoder.encode(to,"UTF-8"));
			url = urlBuf.toString(); 
			Log.i(TAG, "Sending G-Maps V3 request: " + url); 
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (null == url)
			return;
		
	
		HttpPost httppost = new HttpPost(url);
		HttpClient httpClient = new DefaultHttpClient(); 
		HttpResponse response;
		try {
			response = httpClient.execute(httppost);
			
			HttpEntity entity = response.getEntity();
			InputStream is = null;
			is = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line = "0";
			while ((line = reader.readLine()) != null) {
			    sb.append(line + "\n");
			}
			is.close();
			reader.close();
			String result = sb.toString();
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(result);
				
				JSONArray routeArray = jsonObject.getJSONArray("routes");
				
				List<RoutePathOverlay> routeOverlays = new ArrayList<RoutePathOverlay>(); 
				int[] colors = {Color.MAGENTA,  Color.BLUE, Color.CYAN}; 
				int maxRoutes = Math.min(routeArray.length(), 3) ; 
				for (int iRoute = 0; iRoute < maxRoutes; iRoute++) { 
					JSONObject route = routeArray.getJSONObject(iRoute);
					if (null == route) 
						break; // no more 
					JSONObject overviewPolylines = route.getJSONObject("overview_polyline");
					String encodedString = overviewPolylines.getString("points");
					List<GeoPoint> pointsToDraw = decodePoly(encodedString);
					routeOverlays.add(new RoutePathOverlay(pointsToDraw, colors[iRoute])); 
				}

				// render in MainActivity
				if (MainActivity.getActive() != null) { 
						MainActivity.getActive().setRouteOverlays(routeOverlays);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}


	
	private List<GeoPoint> decodePoly(String encoded) {

	    List<GeoPoint> poly = new ArrayList<GeoPoint>();
	    int index = 0, len = encoded.length();
	    int lat = 0, lng = 0;

	    while (index < len) {
	        int b, shift = 0, result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lat += dlat;

	        shift = 0;
	        result = 0;
	        do {
	            b = encoded.charAt(index++) - 63;
	            result |= (b & 0x1f) << shift;
	            shift += 5;
	        } while (b >= 0x20);
	        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
	        lng += dlng;

	        GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6), (int) (((double) lng / 1E5) * 1E6));
	        poly.add(p);
	    }

	    return poly;
	}

}
