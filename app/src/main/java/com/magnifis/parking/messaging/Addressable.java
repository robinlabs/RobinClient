package com.magnifis.parking.messaging;

import java.io.Serializable;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import com.magnifis.parking.App;
import com.magnifis.parking.R;

import static com.magnifis.parking.utils.Utils.*;

public class Addressable implements Serializable {
	
	
	public Addressable() {}
	
	public static Addressable fromEmail(Address a) {
		if (a==null) return null;
		if (a instanceof InternetAddress) {
		   InternetAddress ia=(InternetAddress)a;
		   Addressable as=new Addressable();
		   as.setAddress(ia.getAddress());
		   as.setDisplayName(ia.getPersonal());
		   return as;
		}
		return fromEmail(a.toString());
	}
	
	public static Addressable fromEmail(String email) {
		Addressable a=new Addressable();
		if (!isEmpty(email)) {
		 int i=email.indexOf('<'), j=email.lastIndexOf('>');
		 if (i>0&&j>0&&j>i) {
			a.setDisplayName(email.substring(0,i).trim());
			a.setAddress(email.substring(i+1,j));
		 } else
			a.setAddress(email);
		}
		return a;
	}
	
	protected String address = null, // for sms phone numbe
			         displayName = null,
			         pictureUrl = null;

	
	
	public String getPictureUrl() {
		return pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public boolean hasDisplayName() {
		return !isEmpty(displayName);
	}
	
	public String getSynteticDisplayName(boolean isForSpeech) {
		return getDisplayName(isForSpeech); 
		//return isEmpty(displayName)?address:displayName;
	}
	
	public String getDisplayNameAsIs() {
	   return displayName;
	}

	public String getDisplayName(boolean isForSpeech) {
		if (isEmpty(displayName)) {
			if (isForSpeech)
				return App.self.getString(R.string.someone); 
			else 
				return address; 
		}
		
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

}
