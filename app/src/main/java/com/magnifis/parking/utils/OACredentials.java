package com.magnifis.parking.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import com.magnifis.parking.App;
import com.magnifis.parking.Json;
import com.magnifis.parking.SuccessFailure;
import com.magnifis.parking.Xml;
import com.magnifis.parking.Xml.ML;
import compat.org.json.JSONObject;

public class OACredentials {
   @ML("access_token")
   protected String accessToken=null;
   @ML("token_type")
   protected String token_type=null;
   @ML("expires_in")
   protected Long  expires_in=null;
   @ML("refresh_token")
   protected String refresh_token=null;
   
   protected void cleanState() {
	   accessToken=null;  token_type=null; expires_in=null; refresh_token=null;
   }
     
   private long updated;
   
   public boolean isExpired() {
	  return expires_in!=null&&(((expires_in-10)*1000l+updated)<System.currentTimeMillis());
   };

   
   public boolean isTokenGood() {
	  return !(Utils.isEmpty(accessToken)||isExpired());
   }
   
   final private String sUrl, clientId, clientSecret, scope; 
   
   public OACredentials(
			  String sUrl,
			  String clientId,
			  String clientSecret,
			  String scope
   ) {
	 this.sUrl=sUrl;
	 this.clientId=clientId;
	 this.clientSecret=clientSecret;
	 this.scope=scope;
   }
   
   // improve diag later
   public static class Exception extends java.lang.Exception {} 
   
   
   private Future hotKeepr=null; 
   private SuccessFailure<String> todo=null;
   private boolean _failure=false;
   
   public boolean isFailure() {
	   return _failure;
   }
   
   public void keepItHot() {
	   synchronized(this) {
		  if (hotKeepr==null) {
			  _failure=false;
			  hotKeepr=App.self.tpx.submit(
				new Runnable() {
					@Override
					public void run() {
						 try {
							 URL u=new URL(sUrl);
							 HttpsURLConnection uc=(HttpsURLConnection)u.openConnection();
							 uc.setRequestMethod("POST");
							 uc.connect();
							 OutputStream os=uc.getOutputStream();
							 os.write(
									 ("client_id="+clientId+
											 "&client_secret="+clientSecret+
											 "&grant_type=client_credentials&scope="+scope
											 ).getBytes()
									 );
							 InputStream is=uc.getInputStream();
							 String s=Utils.convertStreamToString(is);
							 
							 Xml.setPropertiesFrom(Json.convertToDom(new JSONObject(s)), OACredentials.this); 
							 updated=System.currentTimeMillis();

							 synchronized(OACredentials.this) {
								 if (isTokenGood()) {
									 if (todo!=null) {
										 todo.onSuccess(accessToken);
										 todo=null;
									 }
									 hotKeepr=App.self.tpx.schedule(
									    this, 
									    expires_in-10, 
									    TimeUnit.SECONDS
									 );
									 return;
								 }
							 }

						 } catch(final Throwable t) {}					
						 synchronized(OACredentials.this) {
							 if (todo!=null) {
								 todo.onFailure(); 
								 todo=null;
								 _failure=true;
							 } 
							 hotKeepr=null;
						 }
					}
				}
			  );
		  }
	   }
   }
    
   public void doWithToken(final SuccessFailure<String> sf) {
	   if (isTokenGood()) {
		   sf.onSuccess(accessToken);
		   return;
	   }
	   todo=sf;
	   keepItHot();
   }

}
