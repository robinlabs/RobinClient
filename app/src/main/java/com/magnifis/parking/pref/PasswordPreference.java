package com.magnifis.parking.pref;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.PreferenceObfuscator;
import com.magnifis.parking.App;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.SuccessFailure;
import com.magnifis.parking.TheDownloader;
import com.magnifis.parking.model.LearnAttribute;
import com.magnifis.parking.utils.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.EditTextPreference;
import android.provider.Settings.Secure;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

public class PasswordPreference extends EditTextPreference {
	
	public static String getDecoded(String key) {
		return getCodec().getString(key, "");
	}
	
	public static PreferenceObfuscator getCodec() {
		  SharedPreferences prefs=App.self.getPrefs();
		  
		  String deviceId = Secure.getString(App.self.getContentResolver(),
	              Secure.ANDROID_ID);
		  
		  AESObfuscator ao=
			new AESObfuscator(
				TheDownloader.SALT,
				MainActivity.class.getPackage().getName(), 
				deviceId
			);
		  
		  return new PreferenceObfuscator(prefs, ao);		
	}
	
	public static void showPasswordDialog(
		    Activity ctx,
			final String key,
			Object title
	) {
		showPasswordDialog(ctx, key, title, null);
	}
	
	public static void showPasswordDialog(
	    Activity ctx,
		final String key,
		Object title,
		final SuccessFailure sf
	) {		
	  //final boolean showAgain[]={false};
	  
	  final PreferenceObfuscator obf=getCodec();
	  
  	  AlertDialog.Builder adb=Utils.prepareConfirmation(
  		  ctx,
  		  title,
		  null
	  );
  	  
  	  final EditText ev=new EditText(ctx);
  	  ev.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
  	  String pwd=obf.getString(key, "");
  	  ev.setText(pwd);
      final Button okBtn[]={null};

      PasswordTransformationMethod pwtm=new PasswordTransformationMethod() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			super.onTextChanged(s, start, before, count);
			okBtn[0].setEnabled(!Utils.isEmpty(ev.getText()));
		}
    	  
      };
      
      ev.setTransformationMethod(pwtm);  
      
      final boolean ok[]={false};
      
  	  adb.setPositiveButton(
      	  android.R.string.yes,
      	  new DialogInterface.OnClickListener() {
  			@Override
  			public void onClick(final DialogInterface dialog, int which) {
  			   ok[0]=true;
  			   obf.putString(key, ev.getText().toString());
  			   obf.commit();
  			   if (sf!=null) sf.onSuccess();
  			}
      	  }
	  );
   	  adb.setView(ev);
	
   	  final AlertDialog optionalDialog=adb.create();
   	
   	  optionalDialog.setOnDismissListener(
   	    new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface arg0) {
				if (!ok[0]&&sf!=null) sf.onCancel();
				/*
				if (showAgain[0]) {
					showAgain[0]=false;
					optionalDialog.show();
				} ;
				*/
			}
   		  
   	     }
   	  );
   	  
   	  optionalDialog.show();
   	  
	  okBtn[0]=optionalDialog.getButton(AlertDialog.BUTTON_POSITIVE);
      if (Utils.isEmpty(pwd)) okBtn[0].setEnabled(false);  	  
   	  
	}
	
	
	@Override
	protected void onClick() {
	    showPasswordDialog(
	      (Activity)getContext(), getKey(), getTitle() );
	}

	public PasswordPreference(Context context) {
		super(context);
	}

	public PasswordPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PasswordPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

}
