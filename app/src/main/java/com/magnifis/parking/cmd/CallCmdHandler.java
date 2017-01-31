package com.magnifis.parking.cmd;

import static com.magnifis.parking.utils.Utils.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.ImageSpan;
import android.widget.TextView;

import com.magnifis.parking.App;
import com.magnifis.parking.Launchers;
import com.magnifis.parking.Log;
import com.magnifis.parking.Output;
import com.magnifis.parking.PhoneStatusReceiver;
import com.magnifis.parking.ProximityWakeUp;
import com.magnifis.parking.R;
import com.magnifis.parking.bubbles.SpannedTextBubbleContent;
import com.magnifis.parking.cmd.i.OnListeningAbortedHandler;
import com.magnifis.parking.model.ContactRecord;
import com.magnifis.parking.phonebook.CalleeAssocEngine;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;


public class CallCmdHandler 
  extends CalleeSelectionHandler implements OnListeningAbortedHandler

{
	final static String TAG=CallCmdHandler.class.getSimpleName();
	
	
	public CallCmdHandler(Context ctx) {
		super(ctx);	
	}
	
	
	@Override
	public boolean performFinalAction(ContactRecord rec, String[] cnames) {
		Log.d(TAG,"performFinalAction");
		if (areAssociationsRemembered()&&!isEmpty(cnames)) {
		   Log.d(TAG,"performFinalAction --  PhoneStatusReceiver.setAssociationToClear ");
    	   PhoneStatusReceiver.setAssociationToClear(new CalleeAssocEngine.Association(super.cae.getInitialPhoneType(),  rec,cnames));
		}
    	Launchers.directdial(context, rec.getPhone());
		return true; // callFireOpes there
	}

	@Override	
	public boolean performFinalAction(String phoneNumber) {
		Launchers.directdial(context, phoneNumber);
		return true; // callFireOpes there
	}
    
    @Override
	public Object getPerformByNumberString(String number) {		
		SpannableStringBuilder ssb=new SpannableStringBuilder(App.self.getString(R.string.mainactivity_onpostexecute_calling));

		ssb.append(' ');
		ssb.append( Utils.phoneNumberToSpeech(number) );
		
		return withHangUp(ssb);
	}

	@Override
	public String getActionName() {
		return App.self.getString(R.string.P_call_hint);
	}
	
	final static String ins="\n\n.";
	
	private Object withHangUp(SpannableStringBuilder ssb) {
		return _withHangUp(ssb,ssb.toString());
	}
	
	private Object _withHangUp(SpannableStringBuilder ssb, CharSequence toSay) {
		 
		ssb.append(ins);

		Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.end_call_icon);

		int sz=App.self.scaler.scaleItShort(80);

		bm=Bitmap.createScaledBitmap(bm, sz, sz , false);

		ImageSpan is=new ImageSpan(context,bm);   		
		ssb.setSpan(is, ssb.length()-1, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.setSpan(
				new AlignmentSpan.Standard(Alignment.ALIGN_CENTER),
				ssb.length()-1,
				ssb.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
		);

		return  
		    Output.Arg.andShow(
			  new MyTTS.Wrapper(
			    new SpannedTextBubbleContent(ssb) {
				  @Override
				  public boolean isMenuRestricted() {
					  return true;
				  }
			    }
			 ) {
				  
			    

				@Override
				public void onToSpeak() {
					super.onToSpeak();
					Log.d(TAG, "andShow:onToSpeak");
					ProximityWakeUp.disableHandWaving();
				}

				@Override
				public void onSaid(boolean fAborted) {
					Log.d(TAG,"<><><onSaid");
					ProximityWakeUp.enableHandWaving();
					if (mainActivity!=null) {
						final TextView tv=mainActivity.getLastBubbleAnswerTextView();
						if (tv!=null) 
						  tv.postDelayed(
							 new Runnable() {

								@Override
								public void run() {
									Spanned sp=(Spanned) tv.getText();
									if (sp!=null) {
									  ImageSpan isps[]= sp.getSpans(sp.length()-1, sp.length(), ImageSpan.class);
									  if (!isEmpty(isps))
										tv.setText(sp.subSequence(0, sp.length()-ins.length()));	
									}
								}
								 
							 },
							 10
						  );
					}
					super.onSaid(fAborted);
				}
				
			  }.setShowInASeparateBubble().setTimeToSleep(1000), // 1 sec pause after the speech
			  toSay
			)

		;		
	}

	@Override
	public Object[] getPerformByContactRecordOutput(ContactRecord r) {
		SpannableStringBuilder ssb=new SpannableStringBuilder(App.self.getString(R.string.mainactivity_onpostexecute_calling));
		
		ssb.append(' ');
		
		String nts=getContactNameToSay(r);
			
		ssb.append(Utils.isPhoneNumber(nts)?Utils.phoneNumberToSpeech(nts):condTranslit(nts));
		ssb.append(' ');
		ssb.append(r.getFormattedPhoneType());
		
		return new Object [] { withHangUp(ssb) };
	}


	@Override
	public void onListeningAbortedByBackKeyPressed() {
		MyTTS.speakText(R.string.P_press_the_back_key__dialog);
	}

}
