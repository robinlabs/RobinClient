package com.magnifis.parking.model;

import static com.robinlabs.utils.BaseUtils.removeLeadingZeros;
import android.telephony.PhoneNumberUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.magnifis.parking.App;
import com.magnifis.parking.Log;
import com.robinlabs.utils.BaseUtils;

public class BrokenPhoneNumber {
   final static String TAG=BrokenPhoneNumber.class.getSimpleName();
	
   public int     countryCode=-1;
   public int     leadingZeros=0;
   public String  nationalNumber=null;
   public boolean emergencyNumber=false;

   @Override
   public String toString() {
	   return  "cc:"+countryCode+" nn:"+nationalNumber+" emergency:"+emergencyNumber;
   }
   
   public BrokenPhoneNumber() {}
   public BrokenPhoneNumber(PhoneNumberUtil phoneNumberUtils,String phone) {
	   breakPhoneNumber(this,phoneNumberUtils,phone);
   }
   
   public static BrokenPhoneNumber breakPhoneNumber(
	 BrokenPhoneNumber bp,
	 PhoneNumberUtil phoneNumberUtils,
	 String phone
   ) {
		   Log.d(TAG,"breakPhoneNumber: "+phone);
		   String pcc=App.self.getPhoneCountryCode();
		   
		   if (pcc!=null) pcc=pcc.toUpperCase();
		   
		   phone=PhoneNumberUtils.stripSeparators(phone);
		   boolean gbl=BaseUtils.isGlobalPhoneNumber(phone);
		   
		   if (PhoneNumberUtils.isEmergencyNumber(phone)) {
			   bp.emergencyNumber=true;
			   if (pcc!=null) try {
				   bp.countryCode=phoneNumberUtils.getCountryCodeForRegion(pcc);
			   } catch(Throwable t) {}
			   bp.nationalNumber=phone;
			   return bp;
		   } else
		   if (pcc!=null||gbl) try {
			  if (gbl) phone=BaseUtils.warrantyThatStartsWithPlus(phone);
			  PhoneNumber pn=phoneNumberUtils.parse(phone, pcc);
	          bp.countryCode=pn.getCountryCode();
	          bp.nationalNumber=phoneNumberUtils.getNationalSignificantNumber(pn);
	          Log.d(TAG, "global phone="+bp.countryCode+" "+bp.nationalNumber+" "+pn.getNationalNumber());
	          return bp;
		   } catch (NumberParseException e) {
			  e.printStackTrace();
		   }
		   
		   int zz[]={0};
		   bp.nationalNumber=removeLeadingZeros(phone,zz);
		   bp.leadingZeros=zz[0];
		
		   return bp;
		}
}