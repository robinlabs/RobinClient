package com.magnifis.parking.geo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.w3c.dom.Document;

import com.magnifis.parking.Log;
import com.magnifis.parking.Xml;
import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.GcAddressComponent;
import com.magnifis.parking.model.GcGeometry;
import com.magnifis.parking.model.GcResponse;
import com.magnifis.parking.model.GcResult;
import com.magnifis.parking.utils.Http;
import com.magnifis.parking.utils.Utils;

import static com.magnifis.parking.utils.Utils.*;
import android.location.Address;

public class GoogleGeocoder {
	
  static final String TAG=GoogleGeocoder.class.getSimpleName();
  
  public static GcResult getFromLatlonRefined(DoublePoint latlon) 
		  throws IOException 
  {
	  GcResult rss[]=getFromLatlon(latlon);
	  double dst=Double.MAX_VALUE; GcResult best=null;
	  if (!isEmpty(rss)) for (GcResult gr:rss) {
		  DoublePoint loc=gr.getGeometry().getLocation();
		  if (loc!=null) {
			 Log.d(TAG,loc.toString());
			 double d=loc.distanceInNauticalMiles(latlon);
			 if (d<dst) {
				 Log.d(TAG,"set");
				 dst=d;
				 best=gr;
			 }
		  }
	  }
	  return best;
  }
  
  private static boolean isCountry(String [] list) {
	  if (!Utils.isEmpty(list)) for(String s:list)
		  if ("country".equals(s))
			  return true;
	  return false;
  }
  
  public static String getFromLatlonCountry(DoublePoint latlon) 
		  throws IOException 
  {
	  GcResult rss[]=getFromLatlon(latlon);
	 
	  if (isEmpty(rss))
		  return null;
	  
	  for (GcResult gr:rss) {
		  if (!isCountry(gr.getTypes()))
			  continue;
		  
		  GcAddressComponent [] addrList = gr.getAddressComponents();
		  for (GcAddressComponent addr:addrList) {
			  if (!isCountry(addr.getTypes()))
				  continue;
			  
			  String s = addr.getShortName();
			  if (Utils.isEmpty(s))
				  return null;
			  
			  return s.toLowerCase();
		  }
	  }
	  return null;
  }
  
  public static GcResult []getFromLatlon(DoublePoint latlon) 
		  throws IOException 
  {
		 StringBuilder rq=new StringBuilder(
				 "https://maps.googleapis.com/maps/api/geocode/xml?sensor=true&language=en&latlng="
				 );
		 
		 rq.append(latlon);
		
		 Log.d(TAG,rq.toString());
		 try {
			 HttpURLConnection uc=Http.httpRq(new URL(rq.toString()), null, null);
			 if (uc!=null) {
				 uc.connect();
				 InputStream is=uc.getInputStream();
				 if (is!=null) try {
					 Document doc=Xml.loadXmlFile(is);
					 if (doc!=null) {
						 GcResponse rsp=Xml.setPropertiesFrom(doc.getDocumentElement(), GcResponse.class);
						 Log.d(TAG,Xml.domToText(doc.getDocumentElement()).toString());
						 if (rsp.isSuccessful()&&!isEmpty(rsp.getResults())) {
                            return rsp.getResults();
						 }
					 }
				 } finally {
					 is.close();				
				 }
			 }
		 } catch (MalformedURLException e) {
			 // TODO Auto-generated catch block
			 e.printStackTrace();
		 }
	  return null;
  }

  public static GcResult []getFromLocationName(String locName)  throws IOException {
	 return getFromLocationName(locName, null);
  }  
	
  public static GcResult []getFromLocationName(
     String locName, DoublePoint box[]
  ) throws IOException {
	 StringBuilder rq=new StringBuilder(
		"https://maps.googleapis.com/maps/api/geocode/xml?sensor=true&language=en&address="
	);
	try {
		rq.append(URLEncoder.encode(locName,"UTF-8"));
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}
	if (box!=null) {
	   // bounds=34.172684,-118.604794|34.236144,-118.500938
	   rq.append("&bounds=");
	   rq.append(box[0].toString());
	   rq.append('|');
	   rq.append(box[1].toString());
	}
	Log.d(TAG,rq.toString());
	
	int nTrials = 1; 
	for (int i = 0; i <nTrials; i++) { 
		try {
			HttpURLConnection uc=Http.httpRq(new URL(rq.toString()), null, null);
			if (uc!=null) {
				uc.connect();
				InputStream is=uc.getInputStream();
				if (is!=null) try {
				  Document doc=Xml.loadXmlFile(is);
				  if (doc!=null) {
					  GcResponse rsp=Xml.setPropertiesFrom(doc.getDocumentElement(), GcResponse.class);
					  //Log.d(TAG,Xml.domToText(doc.getDocumentElement()).toString());
					  if (rsp.isSuccessful()&&!isEmpty(rsp.getResults())) {
						  return rsp.getResults();
						  /*
						GcGeometry geo=rsp.getResult().getGeometry();
						if (geo!=null) return rsp.getResult();//geo.getLocation();
						*/
					  }
				  }
				} finally {
				  is.close();				
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
			
			if (nTrials < 3)
				nTrials++; // one more shot 
		}
	}
	return null;
  }
}
