package com.magnifis.parking.phonebook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.impl.client.DefaultUserTokenHandler;



import com.magnifis.parking.App;
import com.magnifis.parking.Config;
import com.magnifis.parking.Launchers;
import com.magnifis.parking.Log;
import com.magnifis.parking.R;
import com.magnifis.parking.Launchers.PickAppListItem;
import com.magnifis.parking.R.drawable;
import com.magnifis.parking.VoiceIO;
import com.magnifis.parking.cmd.CallCmdHandler;
import com.magnifis.parking.model.ContactRecord;
import com.magnifis.parking.model.ContactRecord;
import com.magnifis.parking.model.ContactRecord;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.utils.Walker;

import compat.org.apache.commons.codec.language.DoubleMetaphone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import static com.magnifis.parking.utils.Utils.*;


public class PhoneBookExp {
	
	// TODO: move all static stuff 
	private volatile Boolean isPrecached = false; 
	private volatile Set<ContactRecord> contactCache = new HashSet<ContactRecord>(); 
	private Object contactCacheSO=new Object();
	private volatile Thread cachingThread = null;
	
	public static String TAG = PhoneBookExp.class.getSimpleName();
	public static Drawable defaultUserPic=App.self.getResources().getDrawable(R.drawable.ic_contact_picture);  
	public static SoftReference<PhoneBookExp> selfWr=null;
	
	public static synchronized PhoneBookExp getInstance() {
		synchronized(PhoneBookExp.class) {
			if (selfWr!=null) {
			  PhoneBookExp book=selfWr.get();
			  if (book!=null) return book;
			}
			return new PhoneBookExp();
		}
	}
	
	private Timer coTimer=new Timer();
	private TimerTask coTimerTask=null;
	
	private ContentObserver contentObserver=new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
          //  Log.d(TAG,"contacts changed");
            if (coTimerTask!=null) coTimerTask.cancel();
            coTimer.purge();
            coTimerTask=new TimerTask() {

        		@Override
        		public void run() {
        			Log.d(TAG,"++contacts changed");
        			reloadCache();
        		}
        		
        	};
            coTimer.schedule(coTimerTask, 8000l);
        }
        
    };
    
    private void reloadCache() {
		synchronized (contactCacheSO) {
		   isPrecached = false; 
		   if (cachingThread!=null) {
			   if (cachingThread.isAlive())cachingThread.interrupt();
			   cachingThread=null;
		   }
		   startPrecaching();
		}
    }
	
	private PhoneBookExp() {
		selfWr=new SoftReference(this);
		startPrecaching(); 
	    App.self.getContentResolver().registerContentObserver (ContactsContract.Contacts.CONTENT_URI, true, contentObserver);
	}
	
	public static ContactRecord getById(long id) {
	   return getByUri(Uri.parse("content://com.android.contacts/data/"+id));
	}
	
	
	public static List<ContactRecord> getContactsByPhone(final String phone) {
		if (isEmpty(phone)) return null;
		String p=PhoneNumberUtils.stripSeparators(phone);
		if (isEmpty(p)) return null;
		
		if (p.length()>4)
			p=p.substring(p.length()-4,p.length());
		
		
		final List<ContactRecord> res=new ArrayList<ContactRecord>();
		
		walkOverContactsByUri(
				  ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				  new Walker<ContactRecord>() {
					@Override
					public boolean walk(ContactRecord r) {
						if (!isEmpty(r.getName())&&PhoneNumberUtils.compare(r.getPhone(),phone)) {
							res.add(r);
						}
						return true;
					}
				  },
				  ContactsContract.CommonDataKinds.Phone.NUMBER+" like ?",
				  '%'+p
	    );
	
		
		return  res;
	}

	
	public static List<CharSequence> getContactNamesByPhone(String phone) {
       List<ContactRecord> cs=getContactsByPhone(phone);
       if (!isEmpty(cs)) {
    	   List<CharSequence> res=new ArrayList<CharSequence>(cs.size());
    	   for (ContactRecord r:cs) {
    		   CharSequence n=r.getName();
    		  if (!isEmpty(n)) res.add(n);
    	   }
    	   return res;
       }
       return null;
	}
	
	public static ContactRecord getContactWithBestName(String phone) {
		   List<ContactRecord> cs=getContactsByPhone(phone);
		   if (!isEmpty(cs)) {
			       ContactRecord best=null;
		    	   for (ContactRecord r:cs) if (!r.isNamePhoneNumber()) {
		    		   CharSequence n=r.getName(), bn=best==null?null:best.getName();
		    		  if (bn==null||bn.length()<n.length()) {
		    			  best=r;
		    		  }
		    	   }
		    	   return best;
		    }
		    return null;
		}
	
	public static CharSequence getBestContactName(String phone) {
		ContactRecord cr=getContactWithBestName(phone);
	    return cr==null?null:cr.getName();
	}
	
	public static List<ContactRecord> getContctsByUri(Uri uri) {
	  final List<ContactRecord> res=new ArrayList<ContactRecord>();
	  walkOverContactsByUri(
			  uri,
			  new Walker<ContactRecord>() {

				@Override
				public boolean walk(ContactRecord rec) {
					res.add(rec);
					return true;
				}
				  
			  }
	  );
	  return res;	
	}
	
	public static ContactRecord getByUri(Uri uri) {
	   // content://com.android.contacts/data/10183

	   final ContactRecord found[]={null};
	   walkOverContactsByUri(
		  uri,
		  new Walker<ContactRecord>() {

			@Override
			public boolean walk(ContactRecord rec) {
				found[0]=rec;
				return false;
			}
			  
		  }
	   );
	   return found[0];	
	}
	
	public static void walkOverContactsByUri(
			Uri uri,
			Walker<ContactRecord> w
			) {
		walkOverContactsByUri(uri,w,null);
	}
	
	public static void walkOverContactsByUri(
			Uri uri,
			Walker<ContactRecord> w,
			String selection,
			String ...selectionArgs
			) {
		ContentResolver cr=App.self.getContentResolver();
		Cursor people = cr.query(
				uri,
				//ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] {
						ContactsContract.CommonDataKinds.Phone._ID,
						ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID,
						
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
						
						ContactsContract.CommonDataKinds.Phone.NUMBER,
						ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
						ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
						ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY,
						ContactsContract.CommonDataKinds.Phone.TYPE,
						ContactsContract.CommonDataKinds.Phone.STARRED,
						ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED,
						ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED
				},
				selection,
				selectionArgs,
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
				+ ", "
				+ ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY
				+ " DESC, "
				+ ContactsContract.CommonDataKinds.Phone.NUMBER
				);

		if (people!=null) try {


			if (people.moveToFirst()) {

				int nameFieldColumnIndex = people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
				int numberFieldColumnIndex = people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

				int photoIdFieldColumnIndex = people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID);

				int typeFieldColumnIndex = people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

				int starredFieldColumnIndex = people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED);

				int lastTimeFieldColumnIndex = people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LAST_TIME_CONTACTED);

				int timesConnectedFieldColumnIndex = people
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TIMES_CONTACTED);


				do {
					ContactRecord rec = new ContactRecord(); 
					rec.setId(people.getLong(0));
					rec.setRawContactId(people.getLong(1));
					rec.setContactId(people.getLong(2));
					// TODO: why 'getString' ??
					rec.setName(people.getString(nameFieldColumnIndex));
					rec.setPhone(people.getString(numberFieldColumnIndex));
					rec.setFavorite(!people.isNull(starredFieldColumnIndex)&&people.getInt(starredFieldColumnIndex)!=0);
					if (!people.isNull(lastTimeFieldColumnIndex)) {
						long lastTimeContacted=people.getLong(lastTimeFieldColumnIndex);
						//     if (lastTimeContacted!=0)
						//	     Log.d(TAG,people.getString(nameFieldColumnIndex)+":lastTimeConnected="+lastTimeContacted);	
						rec.setLastContactTime(lastTimeContacted);
						if (rec.getPhone()!=null&&rec.isContactedInLastNDays(10)) 
							Log.d(TAG,people.getString(nameFieldColumnIndex)+" "+rec.getPhone()+":contacted in last 10 days: "
									+new java.util.Date(lastTimeContacted)
									);
					}

					if (!people.isNull(timesConnectedFieldColumnIndex)) {
						int timesContacted=people.getInt(timesConnectedFieldColumnIndex);
						// 	if (timesContacted!=0)
						//    	    Log.d(TAG,people.getString(nameFieldColumnIndex)+":timesConnected="+timesContacted);	
						rec.setTimesContacted(timesContacted);
					}
					
					if (!people.isNull(photoIdFieldColumnIndex))  rec.setPhotoId( people.getLong( photoIdFieldColumnIndex) );
					if (!people.isNull(typeFieldColumnIndex)) rec.addType( people.getInt(typeFieldColumnIndex) );



                    if (!w.walk(rec)) break;

				} while (people.moveToNext());
			}
		} finally {
			people.close();
		}		
	}
	
	private void startPrecaching() {
		
		if (isPrecached||(cachingThread!=null)&&cachingThread.isAlive())
			return; 
		
		class CachingThread extends Thread {

			public CachingThread() {
				super("Phonebook CachingThread");
			}

			@Override
			public void run() {
				final Set<ContactRecord> _contactCache = new HashSet<ContactRecord>();
				try {

					walkOverContactsByUri(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							new Walker<ContactRecord>() {

								@Override
								public boolean walk(ContactRecord rec) {
									if (isInterrupted())
										return false;
									if (!_contactCache.contains(rec)) {
										if (!isEmpty(rec.getPhone())) {
											ContactRecord f = _byPhoneFirst(rec.getPhone(), false);
											if (f != null) {
												f.updateWith(rec);
												return true;
											}
										}
										_contactCache.add(rec);
									}
									return true;
								}

							});
				} finally {
					synchronized (contactCacheSO) {
						if (!isInterrupted()) {
							contactCache = _contactCache;
							isPrecached = true;
							contactCacheSO.notify();
						}
					}
				}
			}
		
		}
		
		cachingThread = new CachingThread(); 
		//cachingThread.setPriority(Thread.MIN_PRIORITY); 
		cachingThread.start(); 
	}
	
	
	// returns the path to CVS file
	/*
	public String exportAsCvsFile() {
		
		if (!isPrecached)
			return null; // assuming cachibg is done
		
		final String[] typeLabels = {"", "home", "mobile", "work", "fax"}; 
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath() + "/.MagnifisRobin");
		dir.mkdirs();
		File file = new File(dir, "userscontacts.csv");
		try {
			PrintStream outStream = new PrintStream(new FileOutputStream(file));

			outStream.println("Name,Type,Phone"); 
			for (ContactRecord r : contactCache) {
				int type = r.getType(); 
				String typeLabel = ""; 
				if (type > 0 && type <= 4)
					typeLabel = typeLabels[type]; 
				outStream.println(r.getName() + "," + typeLabel + ","+ r.getPhone()); 
			}
		
			outStream.close(); 	
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getLocalizedMessage()); 
			return null; 
		}
		
		return file.getAbsolutePath(); 
	}
	*/
	


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ListAdapter prepareAdapter(
	  Context ctx,
	  final List lst) {
/*
		Drawable dup=ctx.getResources().getDrawable(
			    R.drawable.ic_contact_picture
			  );
	*/	
		for (Object obj:lst) if (obj instanceof ContactRecord){
			ContactRecord cr = (ContactRecord)obj; 
			cr.loadIcon(defaultUserPic);
			cr.initTypeLabel();
		}
		
       return
    		   new ArrayAdapter(
   					ctx,
   					android.R.layout.select_dialog_item,
   					android.R.id.text1,
   					lst.toArray()) {
   				
   				
   				Resources res=App.self.getResources();

   				public View getView(int position, View convertView, ViewGroup parent) {
   					// User super class to create the View
   					View v = super.getView(position, convertView, parent);
   					
   					//v.getLayoutParams().height=LayoutParams.WRAP_CONTENT;
   					
   					TextView tv = (TextView)v.findViewById(android.R.id.text1);
   					//tv.getLayoutParams().height=LayoutParams.WRAP_CONTENT;
   					tv.setEllipsize(null);
   					//tv.setSingleLine(false);
   				

   					// Put the icon drawable on the TextView (support various screen densities)
   					
   					Object obj=lst.get(position);
   					
   					if (!(obj instanceof ContactRecord)) {
   						tv.setText(obj.toString());
   						tv.setCompoundDrawables(null, null, null, null);
   						tv.setGravity(Gravity.CENTER);
   						return v;
   					} else 
   						tv.setGravity(Gravity.LEFT);
   					
   					ContactRecord r=(ContactRecord)obj;
   					Drawable ic= ((ContactRecord)r).getIcon();
   					if (ic!=null) {
   						int dpS = (int) (32 * res.getDisplayMetrics().density  / 0.5f);
   						ic.setBounds(0, 0, dpS, dpS);
   						tv.setCompoundDrawables(ic, null, null, null);
   						if (Config.vip_contact_red) tv.setTextColor(r.isVip()?Color.RED:Color.BLACK);
   					} 
   					
   					// Add margin between image and name (support various screen densities)
   					int dp5 = (int) (5 * res.getDisplayMetrics().density   / 0.5f);
   					tv.setCompoundDrawablePadding(dp5);

   					return v;
   				}
   			};
	}

	public static void performCall(final Activity ctx,final List<ContactRecord> lst) {
		if (lst.size()==1) {
		    ContactRecord r=lst.get(0);
			Launchers.directdial(ctx, r.getPhone());
			r.release();	
			VoiceIO.fireOpes();
		} else {
			
			final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder
			    .setTitle("Select a phone to call")
			    .setAdapter(prepareAdapter(ctx,lst), new DialogInterface.OnClickListener() {
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   dialog.dismiss();
					   Launchers.directdial(ctx,lst.get(which).getPhone());
				   }
				  }
			);
			ctx.runOnUiThread(
			  new Runnable() {

				@Override
				public void run() {
					Dialog dlg=builder.create();
					dlg.setOnDismissListener(
					  new OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface arg0) {
							Log.d(TAG,"onDismiss");
							for (ContactRecord r:lst) r.release();	
							VoiceIO.fireOpes();
						}
						  
					  }
					);
					dlg.show();
				}
				 
			  }
			);
			
		} 
	}
	
	private void waitForCached() {
		synchronized (contactCacheSO) {
			startPrecaching();
		    if (!isPrecached) {
				cachingThread.setPriority(Thread.MAX_PRIORITY); 
				try {
					Log.i(TAG, "Waiting for contacts cache to fill up...");
					contactCacheSO.wait();
				} catch (InterruptedException e) {} 
			}
		}
	}
	
	public List<ContactRecord> byPhone(String wanted) {
	   return byPhone(wanted,false);
	}
	
	public List<ContactRecord> byPhone(String wanted, boolean anyNameRequired) {
	  if (isEmpty(wanted)) return null;
	  waitForCached();
	  return _byPhone(wanted,anyNameRequired);
	}
	
	private List<ContactRecord> _byPhone(String wanted, boolean anyNameRequired) {
		   this.waitForCached();
		   if (!isEmpty(contactCache)) {
			  List<ContactRecord> res=new ArrayList<ContactRecord>();
			  for (ContactRecord r:contactCache) 
				  if (!(anyNameRequired&&isEmpty(r.getName()))&&PhoneNumberUtils.compare(r.getPhone(),wanted)) {
				res.add(r);
		      }
			  return res; 
		   }
		   return null;
	}
	
	private ContactRecord _byPhoneFirst(String wanted, boolean anyNameRequired) {
		   this.waitForCached();
		   if (!isEmpty(contactCache)) {
			  for (ContactRecord r:contactCache) 
				  if (!(anyNameRequired&&isEmpty(r.getName()))&&PhoneNumberUtils.compare(r.getPhone(),wanted)) {
			      return r;
		      }
		   }
		   return null;
	}
	
	public List<ContactRecord> findContacts(String namesCsv) {
	   return findContacts(namesCsv,(Set<Integer>)null);
	}
	
	public  List<ContactRecord> findContacts(String namesCsv, String onlyType) {
	   return findContacts(namesCsv,ContactRecord.getContactTypes(onlyType));
    }
	
	public Collection<ContactRecord> getAllContacts() {
		waitForCached();	
		return contactCache;
	}
	
	public List<ContactRecord> findContacts(String namesCsv, Set<Integer> onlyType) {
	
		boolean uncond=isEmpty(namesCsv);
		
		waitForCached();
	    
		if (!isEmpty(contactCache)) { 
					
			String[] nameCandidates = Utils.simpleSplit(namesCsv,',');
			List<ContactRecord> res = getMatcher().getMatches(nameCandidates, contactCache, uncond, onlyType); 
			
			return res;
		} else {
			Log.i(TAG, "Contacts cache is empty, exiting...");
		}

		return null;
	}
	
	public List<ContactRecord> findContacts(
			String[] queryCandidates,
			String onlyType
		) {
		return findContacts(queryCandidates,ContactRecord.getContactTypes(onlyType));
	}
	
	public List<ContactRecord> findContacts(
			String[] queryCandidates,
			Set<Integer> onlyType
		) {
	  waitForCached();
	  return getMatcher().getMatches(queryCandidates,contactCache, isEmpty(queryCandidates), onlyType);

	}
	
	private static IPhonebookMatcher getMatcher() {
		return new NeoPhonebookMatcher();
	}

}
