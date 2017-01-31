package com.magnifis.parking;

import java.lang.ref.WeakReference;

import com.magnifis.parking.cmd.SendCmdHandler;
import com.magnifis.parking.suzie.SuzieHints;
import com.magnifis.parking.suzie.SuziePopup;
import com.magnifis.parking.suzie.SuzieService;
import com.magnifis.parking.utils.Utils;

import android.os.Bundle;
import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SendSmsActivity extends Activity {

	public static final String SEND_SMS = "com.magnifis.parking.SEND_SMS";
	public boolean useBg = false; 
	SuziePopup.BubleMessage bm = null;
	
	void updateKeyboard() {
    	final View bubleMessage = (View) findViewById(R.id.sendSmsBlock);
    	com.magnifis.parking.views.NoImeEditText te = (com.magnifis.parking.views.NoImeEditText) bubleMessage.findViewById(R.id.textEditText);

    	InputMethodManager imm = (InputMethodManager) SendSmsActivity.this.getSystemService(Service.INPUT_METHOD_SERVICE);
    	
		//te.allowKeyboard = useKeyboard;
		//te.setCursorVisible(true);
		
		ImageView iv = (ImageView)findViewById(R.id.buttonKeyboard);
		if (bm != null && bm.keyboardVisible) {
			imm.showSoftInput(te, 0);
			iv.setImageResource(R.drawable.sms_keyboard2);
		}
		else {
			imm.hideSoftInputFromWindow(te.getWindowToken(), 0);
			iv.setImageResource(R.drawable.sms_keyboard);
		}
	}

	public boolean isActive = false;
	
	@Override
	protected void onResume() {
		super.onResume();
		isActive = true;
	}
	
	@Override
	protected void onPause() {
		if (bm != null && bm.onCancelButtonClick != null)
			bm.onKeyboardButtonClick.onClick(null);
		super.onPause();
		isActive = false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_sms);
		
		setbg();
		
		onNewIntent(getIntent());
	}
	
	private void setbg() {
	   int shortLong[]=App.self.getShortLongDspSizes();
	   
	   try {
		   final View main = (View) findViewById(R.id.sendSmsMain);
		   
		   if (useBg)
			   if (shortLong[0]<=800)
				   main.setBackgroundResource(App.self.isInLanscapeMode()?R.drawable.bg_800x480:R.drawable.bg_480x800);
			   else
				   main.setBackgroundResource(App.self.isInLanscapeMode()?R.drawable.bg_land:R.drawable.bg);
		   else
			   main.setBackgroundDrawable(null);
			   
	   } catch(Throwable t) {
		   Log.e("SendSmsActivity", "Error loading background image..."); 
	   }
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent == null)
			return;
		
		if (intent.getAction() == null)
			return;
		
		if (intent.getAction().equals(SEND_SMS)) {

			//setVisible(false);
	        
			useBg = true;//intent.getBooleanExtra("bg", false);
			setbg();
			
			if (SendCmdHandler.get() != null) {
				isActive = true;
				SendCmdHandler.get().showMessage(null, true);
			}
		}
		super.onNewIntent(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK && bm != null) {
			bm.onKeyboardButtonClick.onClick(null);
			return true;
		}
			
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

	    if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES)
	    	if (bm != null)
	    		bm.onKeyboardButtonClick.onClick(null);
	}
	
	public void showMessage(final SuziePopup.BubleMessage _bm) {

		if (bm == null && _bm == null)
			return;
		
		bm = _bm;
		
    	final View bubleMessage = (View) findViewById(R.id.sendSmsBlock);
    	bubleMessage.post(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					
		    	/****************************************************************************/
				// finish message
				if (bm == null) {
					try {
						// hide keyboard
						updateKeyboard();
						
						// show home screen
						Intent it = new Intent(Intent.ACTION_MAIN);
						it.addCategory(Intent.CATEGORY_HOME);
						Utils.startActivityFromNowhere(it);
						
						// hide activity
						setVisible(false);
					} catch (Throwable e) {
						e.printStackTrace();
					}
					return;
				}
				
				findViewById(R.id.buttonSend).setOnClickListener(bm.onSendButtonClick);
				findViewById(R.id.buttonKeyboard).setOnClickListener(bm.onKeyboardButtonClick);
				findViewById(R.id.buttonSmsCancel).setOnClickListener(bm.onCancelButtonClick);
					
				DisplayMetrics metrics = new DisplayMetrics();
				WindowManager wm = (WindowManager) App.self.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
				wm.getDefaultDisplay().getMetrics(metrics);
				
				LinearLayout viewCenter = (LinearLayout) bubleMessage.findViewById(R.id.layoutCenter);
				int size = Math.round((metrics.heightPixels - Utils.convertDpToPixel(124))/2);
				viewCenter.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, size));
		    	
		    	/****************************************************************************/
				// fill message
				TextView text_recipient = (TextView) bubleMessage.findViewById(R.id.textViewRecipientName);
		    	text_recipient.setText(bm.name);

				TextView text_addr = (TextView) bubleMessage.findViewById(R.id.textViewRecipientAddr);
		    	text_addr.setText(bm.addr);

		    	final EditText ta = (EditText) bubleMessage.findViewById(R.id.textAnswerText);
		    	
	    		ta.setText(bm.answer);
		    	final View v = bubleMessage.findViewById(R.id.acrollAnswer);	    		
		    	/*if (bm.answer.length() > 0)
		    		v.setVisibility(View.VISIBLE);
		    	else*/
		    		v.setVisibility(View.GONE);
		    	
		    	final EditText te = (EditText) bubleMessage.findViewById(R.id.textEditText);
		    	te.setText(bm.text);
		    	te.setCursorVisible(true);
		    	te.setSelected(false);
		    	te.requestFocus();
		    	te.setSelection(bm.sel_start, bm.sel_end);
		    	te.setOnClickListener(bm.onEditClick);
		    	te.setOnTouchListener(bm.onEditTouch);
		    	te.setLongClickable(false);
		    	te.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						bm.onEditChanged.onClick(te);
					}
				});
		    	
		    	//te.setDescendantFocusability(View.FOCUS_BLOCK_DESCENDANTS);
		    	//te.setActivated(false)

	    		ImageView image = (ImageView) bubleMessage.findViewById(R.id.user);
	    		if (bm.icon == null)
	    			image.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
	    		else
	    			image.setImageDrawable(bm.icon);
		    	
				TextView tv = (TextView) bubleMessage.findViewById(R.id.hints);
		    	tv.setText(bm.hint);
		    	
				setVisible(true);

				updateKeyboard();

		    	//TextView text_hint = (TextView) bubleMessage.findViewById(R.id.textViewHint);
		    	//text_hint.setText(bm.hint);

				// hide message in service
				SuzieService.messageBubble(null);
				
				} catch (Exception e) {e.printStackTrace();}
			}
		});
	}

	private static WeakReference<SendSmsActivity> selfWr=null;
	
	public static SendSmsActivity get() {
		return selfWr==null?null:selfWr.get();
	}
	
	public SendSmsActivity() {
		selfWr = new WeakReference<SendSmsActivity>(this);
	}
	
}
