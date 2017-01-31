package com.magnifis.parking.model;

import static com.magnifis.parking.tts.MyTTS.speakText;
import static com.magnifis.parking.utils.Utils.isEmpty;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;

import com.magnifis.parking.App;
import com.magnifis.parking.DailyUpdate;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.Phrases;
import com.magnifis.parking.R;
import com.magnifis.parking.MultipleEventHandler.EventSource;
import com.magnifis.parking.Robin;
import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.views.ProgressSpinner;
import static com.magnifis.parking.utils.Utils.*;

public class LearnAttribute implements Serializable {
	final public static String LEARN_MACRO_PREFIX="learn:macro:";
	final public static String TAG=LearnAttribute.class.getName();
	
	@ML
	protected String name=null;
	@ML(attr="type")
	protected String type=null;
	@ML(attr="value")
	protected String value=null;
	@ML(attr="text_input_prompt")
	protected String prompt=null;
	@ML(attr="after_text_input")
	protected String afterTextInput=null;	
	@ML(attr="voice_input_prompt")
	protected String voiceInputPrompt=null;	
	
	public String getVoiceInputPrompt() {
		return voiceInputPrompt;
	}
	public void setVoiceInputPrompt(String voiceInputPrompt) {
		this.voiceInputPrompt = voiceInputPrompt;
	}
	public String getAfterTextInput() {
		return afterTextInput;
	}
	public void setAfterTextInput(String afterTextInput) {
		this.afterTextInput = afterTextInput;
	}
	public boolean isTextInputFallbackRequired() {
		return !isEmpty(prompt);
	}
	public String getPrompt() {
		return prompt;
	}
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}	
	
	@Override
	public String toString() {
		return name;
	}
	
	public boolean isCustomAddress() {
		return (type==null)||("location".equals(type));
	}
	
	public boolean isDate() {
		return (type==null)||("macro:date".equals(type));
	}
	
	public String getKey() {
		StringBuilder key=new StringBuilder("learn");
		if (!isCustomAddress()) {
			key.append(':');
			key.append(type);
		}
		key.append(':');
		key.append(name);
		return key.toString();
	}
	
	public LearnAttribute() {}
	
	public LearnAttribute(int key) {
	  this(App.self.getString(key));
	}
	
	public LearnAttribute(String key) {
	  if (!isEmpty(key)&&key.startsWith("learn:")) {
		 String a[]=key.split(":");
		 if (a.length>1) {
		    if (a.length>=3&&a[1].equals("macro")) {
		      setValue(getDefinition());
		      if (a.length==3) {
		    	setName(a[2]);
		        setType(a[1]);
		        if ("username".equals(name)) {
		    	   setAfterTextInput(App.self.getString(R.string.learnattribute_username_after_text_input));
		    	   setPrompt(App.self.getString(R.string.learnattribute_username_promt));
		        }
		      } else {
		    	setName(a[3]);
		    	setType(a[1]+':'+a[2]);
		      }
		    } else {
		      setName(a[1]);
		      setType("location");
		      setAfterTextInput(App.self.getString(R.string.learnattribute_address_after_text_input));
		      setPrompt(App.self.getString(R.string.learnattribute_address_promt));
		    }
		 }
	  }  
	}
	
	public String getDefinition() {
		return getDefinition(getKey());
	}
	
	private static String getDefinition(String key) {
		String result = null;
		if (!isEmpty(key)) {
			SharedPreferences prefs = App.self.getPrefs();
			result = prefs.getString(key, null);
			/*
			if (!isEmpty(result) && key.equals("learn:macro:birthday")) {
				result = Utils.formatDateToNiceString(result);	
			}
			*/
		
		}
		return result;		
	}
	
	public static boolean isCustomAddressKey(String key) {
		return !key.startsWith("learn:macro:");
	}
	
	public static String getDefVisiblePart(String key) {
		  String def=getDefinition(key);
		  if (!isEmpty(def)) {
			  if (isCustomAddressKey(key))
				 return urldecode(def.split(":")[0]);
		  }
		  return def;
		}
	
	public String getDefVisiblePart() {
	    return getDefVisiblePart(getKey());
	}
	
	public String getReadableDef() {
		String vp=getDefVisiblePart();
		if (isEmpty(vp)) return vp;
		return isDate()?Utils.formatDateToNiceString(vp):vp;
	}
	
	public boolean forget() {
		String key=getKey();
		if (!isEmpty(key)) {
			SharedPreferences prefs = App.self.getPrefs();
			if (prefs.contains(key)) {
				/*
			   if (!isEmpty(value)&&!value.equalsIgnoreCase(prefs.getString(key, null)))
				 return false;
				 */
			  Editor ed = prefs.edit();
			  ed.remove(getKey());
			  ed.commit();
			  return true;
			}
		}
		return false;
	}
	
	public String expandMacro(String s) {
		String rd=getReadableDef();
	    if (rd!=null) {
	    	Log.d(TAG, "expanding macro "+getName());
	    	s= Utils.simpleReplaceAll(s, "${"+getName()+"}", rd).toString();  //s.replaceAll("(?i)\\$\\{"+getName()+"\\}", rd );
	    }
		return s;
	}
	
	public static String expandMacros(String s) {
		if (isEmpty(s)) return s; 
		HashMap<String,String> extraMacros=new HashMap<String,String>();
		extraMacros.put("username", (new Robin()).getNameOfUser());
		extraMacros.put("partofday",Phrases.partOfDay());
		return expandMacros(s, extraMacros);
	}
	
    public static String expandMacros(String s, HashMap<String,String> extraMacros) {
        if (!isEmpty(s)) {
      	  SharedPreferences prefs=App.self.getPrefs();
      	  for (String key:prefs.getAll().keySet()) 
      		  if (key.startsWith(LearnAttribute.LEARN_MACRO_PREFIX)) {
      			  s=new LearnAttribute(key).expandMacro(s);
      		  }
      	  if (extraMacros!=null) for (String key:extraMacros.keySet()) {
      		s=Utils.simpleReplaceAll(s, "${"+key+"}", extraMacros.get(key)).toString();//s.replaceAll("(?i)\\$\\{"+key+"\\}", extraMacros.get(key));
      	  }
        }
        return s;
    }
    
    public void learn(String val) {
    	Editor ed = App.self.getPrefs().edit();
		ed.putString(getKey(), val);
		ed.commit();
	}
    
    public void learnDate(Date d) {
	    Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);								   
		String val = day+ "/" + month + "/" + year;
		learn(val);
    }
    
    public void learn(Understanding u) {
    	Origin org = u.getOrigin();
    	if (org==null) 
    	  learn(null, null);
    	else
    	  learn(org.getLocation(), org.getFullAddress());
    }

	public void learn(DoublePoint loc, String fullAddress) {
		Editor ed = App.self.getPrefs().edit();
		String key = getKey(), val;
		if (isCustomAddress()) {
			val = urlencode(fullAddress) + ':' + loc.toString();
		} else
			val = getValue();
		ed.putString(key, val);
		ed.commit();
	}	
	
    public AlertDialog learnFromTextInput(
      final Activity ctx,
      final String fullAddress,
      final Runnable afterThat
    ) {
    	
    	if (isDate()) {
    		learnDate(ctx);
    		return null;
    	}
    	
    	final boolean showAgain[]={false}, doCondListen[]={true};
    	   	
    	AlertDialog.Builder adb=Utils.prepareConfirmation(
    		  ctx,
    		  getPrompt(),
			  null
		   );
    	
    	final EditText tv=new EditText(ctx);
    	
	    String preV=getDefVisiblePart();
	    if (!isEmpty(preV)) tv.setText(preV);
    	
    	if (isCustomAddress()) {
    		if (!isEmpty(fullAddress)) {
    			tv.setText( fullAddress );
    		}
    	} else {
    	  if (!isEmpty(getValue())) {
    		tv.setText(getValue());
    	  }
    	}
    	
    	adb.setPositiveButton(
    	  android.R.string.yes,
    	  new DialogInterface.OnClickListener() {
    		  
    		void saveResult() {
    		  saveResult(null,null);
    		}
    		  
    		void saveResult(DoublePoint loc, String fullAddress) {
    			learn(loc,fullAddress);
    			String att=getAfterTextInput();
    			if (!isEmpty(att)) {
    				att=LearnAttribute.expandMacros(att);
    				speakText(att);
    			}    			
    		}
    		  
			@Override
			public void onClick(final DialogInterface dialog, int which) {
				String t=tv.getText().toString();
				if (!isEmpty(t)&&!isEmpty(t=t.trim())) {
				  if (isCustomAddress()) {
					  // verify the address
					  final String tt=t;
					  final ProgressSpinner mSpinner = new ProgressSpinner(ctx);
					  doCondListen[0]=false;
					  mSpinner.show();
					  new Thread() {
						 @Override
						 public void run() {
						   try {
								final GeocodingResult gr=GeocodingResult.fromAddress(
								  tt, 
								  App.self.getUserLocationDP(), null, null
								);
								ctx.runOnUiThread(
										new Runnable() {
											public void run() {
												if (gr==null||gr.isEmpty()) {
												  speakText(R.string.P_ENTER_ANOTHER_ADDRESS);
												  ((Dialog)dialog).show();
												} else {
													saveResult(gr,tt);	
													if (afterThat!=null) afterThat.run();
												}
											}
										}
								);  								
						   } finally {
							   ctx.runOnUiThread(
								  new Runnable() {
									  public void run() {
										  mSpinner.hide(); 
									  }
								  }
							   );  
						   }
						 }
					  }.start();
					  return;
				  }
				  setValue(t);
				  saveResult();
				} else {
			      speakText(R.string.P_TEXT_INPUT_REQUIRED);
			      showAgain[0]=true;
				}
			}
    	  }
    	);
    	adb.setView(tv);
    	
    	final AlertDialog optionalDialog=adb.create();
    	
    	optionalDialog.setOnDismissListener(
    	  new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface arg0) {
				if (showAgain[0]) {
					showAgain[0]=false;
					optionalDialog.show();
				} else if (doCondListen[0])
					if (afterThat!=null) afterThat.run();
			}
    		  
    	  }
    	);
    	
    	optionalDialog.show();
    	
    	return  optionalDialog;
    }

    /** Save user birthday */
	public void learnDate(final Activity ctx) {
		Integer year=null, month=null, day=null;
		
		SharedPreferences sp = App.self.getPrefs();
		String tmpVal = sp.getString(getKey(), null);
		if (tmpVal!=null) try {
		  String[] temp = tmpVal.split("/");
		  day = Integer.parseInt(temp[0]);
		  month = Integer.parseInt(temp[1])-1;
		  year = Integer.parseInt(temp[2]);
		} catch(Throwable t) {}
		if (year==null||month==null||day==null) {
			Calendar c = Calendar.getInstance();
			//default birthday is today
			year = c.get(Calendar.YEAR);
			month = c.get(Calendar.MONTH);
			day = c.get(Calendar.DAY_OF_MONTH);			
		}
		

		DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
			public void onDateSet(DatePicker view, int y, int m, int d) {
				// Dialog return month number less to 1
				String val = d + "/" + (m + 1) + "/" + y;
				learn(val);
			}
		};
		//Dialog get month number more to 1
		DatePickerDialog DPD = new DatePickerDialog(ctx, mDateSetListener,year, month , day);
		DPD.show();

	}    
}
