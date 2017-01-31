package com.magnifis.parking.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;

import com.magnifis.parking.App;
import com.magnifis.parking.R;
import com.magnifis.parking.utils.Utils;
import com.robinlabs.utils.BaseUtils;

public class ContactRecord extends ContactRecordBase {
	
	public boolean isNamePhoneNumber() {
	  if (!(Utils.isEmpty(name)||Utils.isEmpty(phone))) {
		 return PhoneNumberUtils.compare(App.self, name, phone);
	  }
	  return false;
	}
	

	public ContactRecord() {
		super(); 
	}
	
	public ContactRecord(ContactRecord cr) {
		super(cr); 
		this.icon = cr.icon; 
	}

	public boolean isSamePhone(ContactRecordBase cj) {
	  if (cj==this) return true;
	  return cj==null
	   ?false
	   :(
		 getPhone()!=null&&
	 	 cj.getPhone()!=null&&
		 (PhoneNumberUtils.compare(getPhone(),cj.getPhone()))
       );
	}
	
	public static Set<Integer> getContactTypes(String onlyType) {
		HashSet<Integer> type=new HashSet<Integer>();
		if ("mobile".equalsIgnoreCase(onlyType)) {
			type.add(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE); 
			type.add(ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE); 
		} else
			if ("home".equalsIgnoreCase(onlyType)) type.add(ContactsContract.CommonDataKinds.Phone.TYPE_HOME); else
				if ("work".equalsIgnoreCase(onlyType)) {
					type.add(ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
					type.add(ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE);
				}
		return type;
	}
	
	@Override
	public String getFormattedPhoneType() {
	   if (!Utils.isEmpty(types)) {
		   String t0=getFormattedPhoneType(types[0]);
		   if (types.length==1) return t0;
		   StringBuilder sb=new StringBuilder(t0);
		   for (int i=1;i<types.length;i++) {
			   String t1=getFormattedPhoneType(types[i]);
			   if (!BaseUtils.isEmpty(t1)) {
			     sb.append(", ");
			     sb.append(t1);
			   }
		   }
		   return sb.toString();
	   }
	   return "";
	}
	
	public static String getFormattedPhoneType(int type) {
		   switch(type) {
		   case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
			   return App.self.getString(R.string.PT_MOBILE);
		   case	ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
			   return App.self.getString(R.string.PT_WORK_MOBILE);
		   case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
			   return App.self.getString(R.string.PT_WORK);
		   case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
			   return App.self.getString(R.string.PT_HOME);
		   }
		   return "";
		}
	
	

	public void initTypeLabel() {
		if (typeLabel==null&&!Utils.isEmpty(types)) typeLabel=getFormattedPhoneType();	
	} 
	

	public void release() {
		super.release(); 
		this.icon=null;
	}
	
	
	
	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	protected Drawable icon = null;


	public void loadIcon(Drawable defaultUserpic) {
	  if (icon==null) {
		if (photoId!=null&&photoId!=0) {
			ContentResolver cnr=App.self.getContentResolver();
			
	        Cursor cr = cnr.query(
	                ContactsContract.Data.CONTENT_URI,
	                new String [] {ContactsContract.CommonDataKinds.Photo.PHOTO},
	                ContactsContract.CommonDataKinds.Photo.PHOTO_ID + "=" + photoId, null,
	                null);
	        
	        try {
	          if (cr.moveToFirst()) do {
	        	 byte b[]=cr.getBlob(0);
	        	 if (b!=null) {
	        		    Bitmap bmp=BitmapFactory.decodeByteArray(b, 0, b.length);
	        		    setIcon(new BitmapDrawable(bmp));
						break;		
	        	 }
	          } while (cr.moveToNext());
	        } finally {
	          cr.close();
	        }
		} 
		if (icon==null)
			setIcon(defaultUserpic);
	  }
	}
	
	public boolean isMobile() {
		   return
				   Utils.anyIntersection(
						   types, 
						   ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE
				   );
	}
	
	public boolean isWork() {
		   return
				   Utils.anyIntersection(
						   types, 
						   ContactsContract.CommonDataKinds.Phone.TYPE_WORK,ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE
				   );
	}
	
	public boolean isHome() {
		   return Utils.anyIntersection(types,ContactsContract.CommonDataKinds.Phone.TYPE_HOME);
	}



	@Override
	public boolean equals(Object o) {
		if (o instanceof ContactRecordBase) {
		  ContactRecordBase r=(ContactRecordBase)o;
		  return BaseUtils.equals(r.name,name)&&PhoneNumberUtils.compare(r.phone,phone);
		}
		return false;
	}
	
	
}