package com.magnifis.parking.views;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.magnifis.parking.App;
import com.magnifis.parking.R;
import com.magnifis.parking.messaging.Addressable;
import com.magnifis.parking.messaging.Message;
import com.magnifis.parking.model.DelegateAgentPhone;
import com.magnifis.parking.pref.PrefConsts;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;

public class SmsAlertView 
   extends ScalableLLShort 
 {
	
	protected TextView body, sender;
	protected CheckBox voiceModeCheckbox;
	private   View listenButton, replyButton, closeButton, listenButtonEnv, upperPart, buttonBox;

	public SmsAlertView(Context context) {
		this(context, null);
	}

	public SmsAlertView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		body=(TextView)findViewById(R.id.textBody);
		sender=(TextView)findViewById(R.id.textSender);
		listenButton=findViewById(R.id.listenButton);
		replyButton=findViewById(R.id.replyButton);
		closeButton=findViewById(R.id.user);
		voiceModeCheckbox=(CheckBox)findViewById(R.id.voiceModeCheckbox);
		listenButtonEnv=findViewById(R.id.listenButtonEnv);
		upperPart=findViewById(R.id.upperPart);
		buttonBox=findViewById(R.id.buttonBox);
	}
	
	public void setData(
	  Message m,
	  Runnable onListen,
	  Runnable onText,
	  Runnable onClose
	) { 
		Addressable sender=m.getSender();
		String n=sender.getDisplayNameAsIs();
		if (Utils.isEmpty(n)) n=DelegateAgentPhone.getAgentByPhone(sender.getAddress(), getContext());
		if (Utils.isEmpty(n)) n=sender.getDisplayName(false);
		if (Utils.isEmpty(n)) n=sender.getAddress();
		setData(
				   m.getBody(),
				   n,
				   onListen,
				   onText,
				   onClose
		);
	}
	
	public void setUpperPartVisibility(boolean visible) {
		upperPart.setVisibility(visible?View.VISIBLE:View.GONE);
		//buttonBox.setVisibility(visible?View.VISIBLE:View.GONE);
	}
	
	public void enableMicButton(boolean enabled) {
		listenButton.setEnabled(enabled);
		listenButton.setClickable(enabled);
	}
	
	public void setData(
	   Object bodyTextToShow,
	   CharSequence senderName,
	   final Runnable onListen,
	   final Runnable onText,
	   final Runnable onClose
	) {  
	  if (bodyTextToShow!=null) {
			if (bodyTextToShow instanceof Integer)
				body.setText((Integer)bodyTextToShow);
			else
				body.setText(bodyTextToShow.toString());
	  }
	  
	  if (senderName!=null) sender.setText(senderName);
	  
	  OnClickListener 
	     oLeftClick=new OnClickListener() {
	    	 @Override
	    	 public void onClick(View v) {
	    		 if (onListen!=null) onListen.run();
	    	 }
		 },
		 oRightClick=new OnClickListener() {
	    	 @Override
	    	 public void onClick(View v) {
	    		 if (onText!=null) onText.run();
	    	 }
		 },
		 onCloseClick=new OnClickListener() {
	    	 @Override
	    	 public void onClick(View v) {
	    		 if (onClose!=null) onClose.run();
	    	 }
		 };		 
		 
		 voiceModeCheckbox.setOnCheckedChangeListener(
		    new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				   try  {
				     Editor ed=App.self.getPrefs().edit();
				     ed.putBoolean(PrefConsts.PF_SMS_NOTIFY, isChecked);
				     ed.commit();
				   } catch(Throwable t) {
					   t.printStackTrace();
				   }
			       if (!isChecked) MyTTS.abort();
				}
		    	
		    }
		 );
		 
		 voiceModeCheckbox.setChecked(App.self.getBooleanPref(PrefConsts.PF_SMS_NOTIFY));
		 

	  listenButton.setOnClickListener(oLeftClick);
	  listenButtonEnv.setVisibility(onListen==null?View.GONE:View.VISIBLE);
	  
	  replyButton.setOnClickListener(oRightClick);
	  closeButton.setOnClickListener(onCloseClick);
	}
}
