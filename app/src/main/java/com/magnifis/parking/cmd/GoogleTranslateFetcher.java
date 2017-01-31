package com.magnifis.parking.cmd;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.util.Log;

import com.magnifis.parking.App;
import com.magnifis.parking.JSONFetcher;
import com.magnifis.parking.Json;
import com.magnifis.parking.R;
import com.magnifis.parking.tts.MyTTS;

import compat.org.json.JSONObject;

public class GoogleTranslateFetcher extends JSONFetcher<String> {

	private static final String TAG = GoogleTranslateFetcher.class.getSimpleName();
	public String fromLanguageCode;
	public String toLanguageCode; 
	public String srcText; 
	
	static public String getSystemLangCode() {
		return App.self.getString(R.string.P_shortlang);
	}
	
	static public String getSystemLang() {
		return App.self.getString(R.string.P_lang);
	}
	
	public GoogleTranslateFetcher(String fromLanguageCode, String toLanguageCode, String srcText) {

		super();
		
		Log.d(TAG, "GoogleTransleFetcher: create"); 

		this.fromLanguageCode = fromLanguageCode;
		this.toLanguageCode = toLanguageCode;
		this.srcText = srcText;
		
		execute("http://translate.google.com/translate_a/t?client=robin&text="+
				URLEncoder.encode(srcText)+"&sl="+fromLanguageCode+"&tl="+toLanguageCode+"&pc=0&oc=1", null, null);
	}


	@Override
	protected  String doInBackground(Object... params) {
	
		Log.d(TAG, "GoogleTransleFetcher: doInBackground"); 

		try{
			URL u= new URL((String)params[0]);
			InputStream is=invokeRequest(u, null, null, "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_3 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8J2 Safari/6533.18.5");
			
			if (is!=null) try {
				return consumeInputStream(is);
			} finally {
			  is.close();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return null; 

	}
	
	
	@SuppressLint("NewApi")
	protected String consumeJsonData(JSONObject root) {

		Log.d(TAG, "GoogleTransleFetcher: consumeJsonData"); 

		Element el = Json.convertToDom(root);
		NodeList nl = el.getElementsByTagName("sentences");
		if (nl == null || nl.getLength() < 1)
			return null;
		
		nl = nl.item(0).getChildNodes();
		if (nl == null || nl.getLength() < 1)
			return null;

		for(int i = 0; i < nl.getLength(); i++)
			if ("trans".equals(nl.item(i).getNodeName()))
				return nl.item(i).getTextContent();
		
		return null; 
	}
	
	
	@Override
	protected void onPostExecute(String text) {
		
		Log.d(TAG, "GoogleTransleFetcher: on post execute"); 
		
		if (text == null) {
			MyTTS.speakText(App.self.getString(R.string.P_translate_message));
			return;
		}
		
		// say
		MyTTS.speakText(new MyTTS.Wrapper(new MyTTS.TextInLang(toLanguageCode, text)).setShowInASeparateBubble());
/*
		// open google translate app
		Intent i = new Intent();
		i.setAction(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_TEXT, srcText);
		i.putExtra("key_text_input", srcText);
		i.putExtra("key_text_output", text);
		i.putExtra("key_language_from", fromLanguageCode);
		i.putExtra("key_language_to", toLanguageCode);
		i.putExtra("key_suggest_translation", "");
		i.putExtra("key_from_floating_window", false);
		i.setComponent(
			    new ComponentName(
			        "com.google.android.apps.translate",
			        "com.google.android.apps.translate.HomeActivity"));
		PendingIntent pi = PendingIntent.getActivity(App.self, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		try {
		  pi.send();
		} catch (CanceledException e) {
		}
		*/	
	}

}
