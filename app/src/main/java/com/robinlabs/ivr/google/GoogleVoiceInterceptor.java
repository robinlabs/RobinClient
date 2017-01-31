package com.robinlabs.ivr.google;

import static com.robinlabs.utils.BaseUtils.isEmpty;
import static com.robinlabs.utils.AndroidUtils.isEmpty;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import android.os.Bundle;
import android.os.Environment;
import android.speech.SpeechRecognizer;
import android.util.Log;



import com.magnifis.parking.utils.Utils;
import com.robinlabs.utils.MultipartCreator;
import com.robinlabs.utils.StreamUtils;
import com.robinlabs.utils.wav.WavFile;
import com.robinlabs.utils.wav.WavFileException;

import org.json.JSONException;
import org.json.JSONObject;



public class GoogleVoiceInterceptor extends GoogleRecognitionAdapter {
	
	final private ScheduledThreadPoolExecutor tpx;
	final private String speech_audio_logger_url;
	
	public GoogleVoiceInterceptor(
	  ScheduledThreadPoolExecutor tpx,
	  String speech_audio_logger_url
	) {
	   this.tpx=tpx;
	   this.speech_audio_logger_url=speech_audio_logger_url;
	}
	
	final static String TAG=GoogleVoiceInterceptor.class.getSimpleName();
	
	ByteArrayOutputStream baos=new ByteArrayOutputStream();
	
	@Override
	public void onBeginningOfSpeech() {
       baos=new ByteArrayOutputStream();
	}
	
	@Override
	public void onBufferReceived(byte[] buffer) {
    	if (baos!=null)
			try {
				baos.write(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void onEndOfSpeech() {
	}
	
	private final static boolean
	  keepOnDisk=false,
	  keepMultipartOnDisk=false,
	  sendToServer=true; 
	
	private void commitVoiceCapure(final List<String> transcripts, final float[] scores) {
		if ((keepOnDisk||keepMultipartOnDisk||sendToServer)&&!isEmpty(baos)) {
			 final byte ba[]=baos.toByteArray();
			 tpx.execute(
			    new Runnable() {
					@Override
					public void run() {
					     try {
					    	 String last_record=
					    			 Environment.getExternalStorageDirectory()+
					    			 "/.MagnifisRobin/lastSpeech_"+System.currentTimeMillis();
					    	 
					    	 String waveFn=last_record+".wav", textFn=last_record+".txt";
					    	 
					    	 JSONObject jso=new JSONObject().put("result", transcripts);
					    	 if (!Utils.isEmpty(scores)) {
					    		 jso.put("scores", Arrays.toString(scores)); 
					    	 }

					    	 if (keepOnDisk) {
					    		 WavFile wavFile = WavFile.newWavFile(new File(waveFn), 1, (int)(baos.size()/2), 16, 8000);
					    		 for (int i=0;i<ba.length;i++) {
					    			 wavFile.writeByte(ba[i]);
					    		 }
					    		 wavFile.close();

					    		 OutputStreamWriter osw=
					    				 new OutputStreamWriter(
					    						 new BufferedOutputStream(new FileOutputStream(textFn)),
					    						 "UTF-8"
					    						 );
					    		 osw.write(jso.toString());
					    		 osw.flush();
                                 osw.close();
					    		
					    	 }
					    	 if (sendToServer||keepMultipartOnDisk) {	 
					    		 File wf=new File(waveFn);
					    		 ByteArrayOutputStream bos=new ByteArrayOutputStream();
					    		 WavFile wavFile = WavFile.newWavFile(bos, 1, (int)(baos.size()/2), 16, 8000);
					    		 for (int i=0;i<ba.length;i++) {
					    			 wavFile.writeByte(ba[i]);
					    		 }
					    		 wavFile.close();

					    		 MultipartCreator mpc=new MultipartCreator()
					    		    .addPlainText(jso.toString(), "transcript")
					    		    .addPart(bos.toByteArray(), "audio/wav", "speech", wf.getName())
					    		 ;
					    		 
					    		 
					    		 if (sendToServer) {
					    			 HttpURLConnection uc=
					    					 (HttpURLConnection)new URL(speech_audio_logger_url)
					    			 .openConnection();
					    			 uc.setAllowUserInteraction(false);
					    			 uc.setDoInput(true);
					    			 uc.setDoOutput(true);
					    			 uc.setRequestMethod("POST");
					    			 mpc.setContentTypeTo(uc);
					    			 uc.connect();
					    			 OutputStream os=uc.getOutputStream();
					    			 mpc.writeTo(os);
					    			 os.flush();
					    			 os.close();
					    			 InputStream is=uc.getInputStream();
					    			 CharSequence rText=StreamUtils.getTextFromStream(is);
					    			 if (!isEmpty(rText)) {
					    				 String r=rText.toString().trim();
					    				 if (!isEmpty(r)) {
					    					 Log.d(TAG,r);
					    				 }
					    			 }
					    			 is.close();
					    		 }
					    		 
					    		 if (keepMultipartOnDisk) {
					    			 FileOutputStream fos=new FileOutputStream(last_record+".dat.txt");
					    			 fos.write(("contentType: "+mpc.getBoundary()+" \n").getBytes());
					    			 mpc.writeTo(fos);
					    			 fos.close();
					    	     }
					    	 }
					     } catch (IOException e) {
					    	 // TODO Auto-generated catch block
					    	 e.printStackTrace();
					     } catch (WavFileException e) {
					    	 // TODO Auto-generated catch block
					    	 e.printStackTrace();
						 } catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						 }						
						
					}
			    }
			 );
		}
	    baos=new ByteArrayOutputStream();
	}
	
	@Override
	public void onResults(Bundle results) {
		if (!isEmpty(results)) {
		  List<String> transcripts = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		  if (!isEmpty(transcripts)) { 
			  float[] scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
			  commitVoiceCapure(transcripts, scores);
		  } 
		}
	}

}
