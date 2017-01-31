package com.magnifis.parking;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.w3c.dom.Element;

import compat.org.json.JSONException;
import compat.org.json.JSONObject;
import compat.org.json.JSONTokener;

import com.att.android.speech.ATTSpeechActivity;
import com.magnifis.parking.utils.OACredentials;
import com.magnifis.parking.utils.Utils;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.os.Build;

public class AttTestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout rl=new RelativeLayout(this);
        setContentView(rl, 
           new RelativeLayout.LayoutParams(
        	 LayoutParams.MATCH_PARENT,
        	 LayoutParams.MATCH_PARENT
           )
        );
        Button b=new Button(this);
        b.setText("Start Recognition");
        b.setOnClickListener(
           new OnClickListener() {
			 @Override
			 public void onClick(View v) {
				 onStartRecognition();
			 }
           }
        );
        rl.addView(b);
    }
    
    void showAlert(final CharSequence txt) {
      runOnUiThread(
    	 new Runnable() {
    	   @Override
    	   public void run() {
    	        new AlertDialog
    	         .Builder(AttTestActivity.this)
    	         .setMessage(txt)
    	         .create()
    	         .show();  		   
    	   }
    	 }
      );
    }
    
    public static CharSequence dump( Bundle bl) {
    	StringBuilder sb=new StringBuilder();
        if (bl!=null) for (String key:bl.keySet()) {
      	   Object o=bl.get(key);
      	   sb.append(key);
      	   sb.append('=');
           sb.append(o==null?"null":o.toString());
        }
        return sb;
    }
    
    
    final static String
      CLIENT_ID="k7ac7vmolg4inqojt1xtevrgcdytha01",
      CLINET_SECRET="xy00zqzh71ipbohecidnqpbwm7ktfjcx"
    ;
    
    
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	showAlert(""+resultCode);
	    if (requestCode==777&&data!=null) {
	    	showAlert(dump(data.getExtras()));
	    }
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void onStartRecognition() {
    	new Thread("OAuth.T") {
			@Override
			public void run() {
			     OACredentials oat=new OACredentials("https://api.att.com/oauth/token", CLIENT_ID, CLINET_SECRET, "SPEECH,STTC");    			     
			     oat.keepItHot();
			     oat.doWithToken(
			       new SuccessFailure<String>() {
					  @Override
					  public void onSuccess(String token) {
						  /*
						  if (true) {
							  showAlert(token);
							  return;
						  }
						  */
						     Intent request = new Intent(AttTestActivity.this, ATTSpeechActivity.class);
					         request.putExtra(ATTSpeechActivity.EXTRA_RECOGNITION_URL, "https://api.att.com/speech/v3/speechToText"); 
						     request.putExtra(ATTSpeechActivity.EXTRA_SPEECH_CONTEXT, "Generic"); 
						     request.putExtra(ATTSpeechActivity.EXTRA_BEARER_AUTH_TOKEN, token); 
				//		     request.putExtra(ATTSpeechActivity.E, token); 
						     startActivityForResult(request, 777);           
					  }
			    	   
			       }		 
			     );
			}
    	}.start();
    }

}
