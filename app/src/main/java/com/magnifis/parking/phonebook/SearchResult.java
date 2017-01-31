package com.magnifis.parking.phonebook;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle.Control;

import com.magnifis.parking.model.ContactRecordBase;


import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;

import com.magnifis.parking.App;
import com.magnifis.parking.R;
import com.magnifis.parking.model.ContactRecord;
import com.robinlabs.utils.BaseUtils;


public abstract class SearchResult {
	public abstract String getBestMatcher();

	public abstract int countResults();
	public abstract boolean anyOthers();
	public abstract List<ContactRecord> getContacts();
	public abstract SearchResult nextResults();
		
	public abstract  List<ContactRecord> getAllContacts();
	
	public abstract boolean shrinkToSingleIfBetterThan(double x);
	
	protected int stepNo=0;
	
	public int getStepNo() {
		return stepNo;
	}
	
	
	public abstract double howMuchTheBestIsBetter();
	
	public  List<ContactRecord>  getContactsWithResources() {
		return getContactsWithResources(false);
	}
	
	public List<ContactRecord> getContactsWithResources(boolean forAllContacts) {
		List<ContactRecord> contacts=forAllContacts?getAllContacts():getContacts();
		if (!BaseUtils.isEmpty(contacts)) {
			Drawable dup=App.self.getResources().getDrawable(
				    R.drawable.ic_contact_picture
				  );
		   HashMap<Long,Drawable> knownIcons=new HashMap<Long,Drawable>();
		   int nICs=0;
		   StringBuilder sb=new StringBuilder(ContactsContract.CommonDataKinds.Photo.PHOTO_ID);
		   sb.append(" in (");
		   for (ContactRecord c:contacts) {
			   Long pid=c.getPhotoId();
			   if (pid!=null&&c.getIcon()==null) {
				 if (nICs++>0) sb.append(',');
				 sb.append(pid);

			   }
			   c.initTypeLabel();
		   }
		   if (nICs>0) {
			 sb.append(')');
				ContentResolver cnr=App.self.getContentResolver();
				
		        Cursor cr = cnr.query(
		                ContactsContract.Data.CONTENT_URI,
		                new String [] {ContactsContract.CommonDataKinds.Photo.PHOTO,ContactsContract.CommonDataKinds.Photo.PHOTO_ID},
		                sb.toString(), null,
		                null);
		        try {
		        	if (cr.moveToFirst()) do {
		        		byte b[]=cr.getBlob(0);
		        		if (b!=null) {
		        			Bitmap bmp=BitmapFactory.decodeByteArray(b, 0, b.length);
		        			knownIcons.put(cr.getLong(1), new BitmapDrawable(bmp));	
		        		}
		        	} while (cr.moveToNext());		        	
		        } finally {
		        	cr.close();
		        }
		   }
		   
		   for (ContactRecord c:contacts) if (c.getIcon()==null) {
			   
			   
			   //c.setIcon(PhoneBook.defaultUserPic); 
			   
			   
			   Long pid=c.getPhotoId();
			   if (pid!=null&&knownIcons.containsKey(pid)) {
				 c.setIcon(knownIcons.get(pid));
			   } else
				   c.setIcon(dup);
		   }
	
	  }
	  return contacts;
	}
}