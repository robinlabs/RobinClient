/*
 ******************************************************************************
 * Parts of this code sample are licensed under Apache License, Version 2.0   *
 * Copyright (c) 2009, Android Open Handset Alliance. All rights reserved.    *
 *                                                                            *                                                                         *
 * Except as noted, this code sample is offered under a modified BSD license. *
 * Copyright (C) 2010, Motorola Mobility, Inc. All rights reserved.           *
 *                                                                            *
 * For more details, see MOTODEV_Studio_for_Android_LicenseNotices.pdf        * 
 * in your installation folder.                                               *
 ******************************************************************************
 */

package com.magnifis.parking;

import static com.magnifis.parking.tts.MyTTS.speakText;

import java.util.Random;

import org.w3c.dom.Document;

import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.magnifis.parking.R;
import com.magnifis.parking.model.HelpFile;
import com.magnifis.parking.model.HelpTopic;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;
import com.robinlabs.utils.BaseUtils;

import android.view.ViewGroup;

public class HelpActivity extends ListActivity {
	
	public static final String OPEN_HELP = "com.magnifis.parking.OPEN_HELP";

	final static String TAG=HelpActivity.class.getSimpleName();
	
	final static int
	  Level1BG=Color.rgb(46, 46, 47),
	  Level2BG=Color.rgb(46, 46, 47),
	  Level1FG=Color.WHITE,
	  Level2FG=Color.WHITE
	  ;
	
	private void setViewBackground(ListView listView, Resources resources) {
		//Drawable shape = resources. getDrawable(R.drawable.help_background);
		//listView.setBackgroundDrawable(shape);
		listView.setBackgroundColor(Level1BG);
		listView.setCacheColorHint(Color.TRANSPARENT);
	}
	
	public static void onInfo(Context ctx, boolean dontSpeak) {
		Intent it = new Intent(OPEN_HELP);
		it.setClass(App.self, HelpActivity.class);
		it.putExtra("DONTSPEAK", dontSpeak);
		Launchers._startNestedActivity(ctx,it,PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ListView lv=getListView();
		Intent it=getIntent();
		Resources res = getResources();
		setViewBackground(lv, res);
		if (it.hasExtra(EXT_TOPIC)) {
			HelpTopic topic=(HelpTopic)it.getSerializableExtra(EXT_TOPIC);
			setTitle(topic.getName());
			setListAdapter(new ArrayAdapter<HelpTopic.Example>(this,
					android.R.layout.simple_list_item_1, topic.getExamples() ) {
		        @Override
		        public View getView(int position, View convertView,
		                ViewGroup parent) {
		            View view =super.getView(position, convertView, parent);

		            TextView textView=(TextView) view.findViewById(android.R.id.text1);

		            /*YOUR CHOICE OF COLOR*/
		            textView.setTextColor(Level2FG);
		            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0f);
		         
		            
		            return view;
		        }
				
			  }
			);
		} 
		else {
			App.self.hideNfyScreen();
			say();
			Document doc=Xml.loadXmlFile(res.openRawResource(R.raw.help));
			HelpFile help=Xml.setPropertiesFrom(doc.getDocumentElement(), HelpFile.class);
			setTitle(help.getTitle());
			setListAdapter(new ArrayAdapter<HelpTopic>(this,
					android.R.layout.simple_list_item_1, help.getTopics())
            {
		        @Override
		        public View getView(int position, View convertView,
		                ViewGroup parent) {
		            View view =super.getView(position, convertView, parent);

		            TextView textView=(TextView) view.findViewById(android.R.id.text1);
		            					
					HelpTopic topic=getItem(position);
					
					BitmapDrawable icon=topic.getIcon();
					
					if (icon==null) {
						icon=(BitmapDrawable)getResources().getDrawable(R.drawable.empty_help_icon);
					} 
					int	w=127,//icon.getIntrinsicWidth(),
						h=127;//icon.getIntrinsicHeight();
					
					
					double factor=getResources().getDisplayMetrics().density  / 0.5f;
	
				    icon.setBounds(0, 0, (int)Math.round(w*factor/5), (int)Math.round(h*factor/5));
					textView.setCompoundDrawables(icon, null, null, null);
			
		        	
		            /*YOUR CHOICE OF COLOR*/
		            textView.setTextColor(Level1FG);
		            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19.0f);

		            return view;
		        }
				
			  }					
			);
		}
	}
	
	public final static String EXT_TOPIC="EXT_TOPIC";

	@Override
	protected void onListItemClick(ListView lv, View v, int position, long id) {
		Object obj=lv.getItemAtPosition(position);
		if (obj instanceof HelpTopic) {
		   Intent it=new Intent();
		   HelpTopic ht=(HelpTopic)obj;
		   boolean shouldDie=false;
		   String url=ht.getOpenUrl();
		   if (!BaseUtils.isEmpty(url)) {
			 it.setClass(this, MainActivity.class);
			 it.setAction(MainActivity.INTERPRET_UNDERSTANDING);
			 Understanding u=new Understanding().setCommandByCode(Understanding.CMD_OPEN_URL);
			 u.setUrl(url);
			 u.setShowEmbedded(true);
			 it.putExtra(MainActivity.EXTRA_UNDERSTANDING, u);
			 shouldDie=true;
		   } else if (BaseUtils.isEmpty(ht.getLaunch())) {
		     it.setClass(this, HelpActivity.class);
		     it.putExtra(EXT_TOPIC, ht);
		   } else {
			 ComponentName cn=ComponentName.unflattenFromString(App.self.getPackageName()+"/"+ht.getLaunch());
			 it.setComponent(cn);
			 shouldDie=true;
		   }
		   Launchers._startNestedActivity(this,it);
		   if (shouldDie) finish();
		} else if (((HelpTopic.Example)obj).isExecutable())  {
		   String cmd=obj.toString().split("/")[0];
		   Log.d(TAG, "cmd="+cmd);
		   final Intent it=new Intent(Intent.ACTION_SEARCH);
		   it.setClass(this, MainActivity.class);
		   it.putExtra(SearchManager.QUERY, cmd);
		   //it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TOP);
		   MyTTS.abort();
		   MyTTS.speakText(cmd);
		   MyTTS.execAfterTheSpeech(
			 new Runnable() {

				@Override
				public void run() {
					startActivity(it);
				}
				 
			 }
		   );
		}
	}

	private void say() {
		Intent intent = getIntent();
		if (intent == null || !OPEN_HELP.equals(intent.getAction()))
			return;
		
		Props props=Props.getInstance(App.self);

		//MyTTS.abort();
		VR vr=VR.get();
		if (vr!=null) vr.abort();
		
		if (MyTTS.isSpeaking())
			return;
		
		if (!(intent.getBooleanExtra("DONTSPEAK", false) || "false".equalsIgnoreCase((String) props.get(MainActivity.FIRST_HELP)))) {
			speakText(
			   new MyTTS.BubblesInMainActivityOnly(
			     App.self.anyProximitySensor() ? R.string.P_info
					: R.string.P_info_nowave
			   )
			);
			props.setAndSave(MainActivity.FIRST_HELP, "false");
		} else {
			boolean sayHint = App.self.getBooleanPref("hint");
			if (sayHint) {
				int helpHintsLength = App.self.getResources().getStringArray(
						R.array.helpHints).length;
				Random randomGenerator = new Random();
				int randomInt = randomGenerator.nextInt(helpHintsLength);
				speakText(
				    new MyTTS.BubblesInMainActivityOnly(
				            App.self.getResources().getStringArray(
						   R.array.helpHints)[randomInt]
					)
				);
			}
		}
	}


	@Override
	protected void onResume() {
		super.onResume();
		App.self.setActiveActivity(this);
	}



	@Override
	protected void onPause() {
		super.onPause();
		App.self.removeActiveActivity(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		App.self.notifyStopActivity(this);
	}
	
	
	
}