package com.magnifis.parking.cmd;

import static com.magnifis.parking.utils.Langutils.setize;
import static com.magnifis.parking.utils.Utils.*;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.magnifis.parking.Abortable;
import com.magnifis.parking.Config;
import com.magnifis.parking.Launchers;
import com.magnifis.parking.Log;
import com.magnifis.parking.AborterHolder;
import com.magnifis.parking.CallSmsAttemptsHistory;
import com.magnifis.parking.Output;
import com.magnifis.parking.R;
import com.magnifis.parking.RunningInActivity;
import com.magnifis.parking.SuccessFailure;
import com.magnifis.parking.VR;
import com.magnifis.parking.VoiceIO;
import com.magnifis.parking.cmd.etc.CmdHandlerHolder;
import com.magnifis.parking.cmd.i.ActivityResultHandler;
import com.magnifis.parking.cmd.i.ClientStateInformer;
import com.magnifis.parking.cmd.i.IOptionsListViewHolder;
import com.magnifis.parking.cmd.i.MagReplyHandler;
import com.magnifis.parking.model.CallSmsAttempt;
import com.magnifis.parking.model.CalleeAssociation;
import com.magnifis.parking.model.ContactRecordBase;
import com.magnifis.parking.model.ContactRecord;
import com.magnifis.parking.model.MagReply;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.phonebook.CalleeAssocEngine;
import com.magnifis.parking.phonebook.Results;
import com.magnifis.parking.phonebook.SearchResult;
import com.magnifis.parking.phonebook.NeoPhonebookMatcher;
import com.magnifis.parking.phonebook.PhoneBook;
import com.magnifis.parking.suzie.SuziePopup;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Langutils;
import com.magnifis.parking.utils.CommonNames;
import com.magnifis.parking.utils.Translit;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.views.DecoratedListView;


public abstract class CalleeSelectionHandler 
  extends CmdHandlerBase
  implements Abortable, MagReplyHandler, ActivityResultHandler, ClientStateInformer 

{
	final static String TAG=CalleeSelectionHandler.class.getSimpleName();
	
	final boolean beSilent;
	
	protected CalleeAssocEngine cae=null;
	
	protected  CallSmsAttemptsHistory callSmsAttemptsHistory=CallSmsAttemptsHistory.getInstance();
	
	public boolean isInSmsMode() {
	  return false;
	}
	
	public CalleeSelectionHandler(Context ctx) {
		this(ctx, false);
	}
	
	public CalleeSelectionHandler(Context ctx, boolean beSilent) {
		super(ctx);
		this.beSilent=beSilent;
	}
	
	@Override
	public void abort(int flags) {
	   abortTtsThenShutdown();
	   giveUp();
	}
	
	protected void giveUp() {
	  Log.d(TAG, "giveUp");
	  if (!isEmpty(initialMatches)) callSmsAttemptsHistory.remember(initialMatches,this.initialDesiredPhoneType, isInSmsMode());
	}
	
	private void abortTtsThenShutdown() {
		MyTTS.abort();
		shutdown();		
	}
	
	private boolean requires_inactivation=true;
	
	protected void inactivate() {
	  if (requires_inactivation) {
		requires_inactivation=false;
		    //ICmdHandlerHolder holder=CmdHandlerHolder.getCmdHandlerHolder(context);
		    CmdHandlerHolder.removeCommandHandler(CalleeSelectionHandler.this);
		    
			if (mainActivity==null) {
				  if (proxyActivity!=null) {
					 Activity ac=proxyActivity.get();
					 if (ac!=null) ac.finish();
				  }
			} else
			Utils.runInMainUiThread(
				context,
				new Runnable() {
				  @Override
				  public void run() {
			         mainActivity.hideOptionsListView();
				  }
				}
			);
			cleanTheContactListRepresentation(null);	
		
	  }
	}
	
	protected void shutdown() {
		inactivate();
		VR.get().killMicrophone();
	}
	
	private void cleanTheContactListRepresentation(Collection<ContactRecord> newlist) {
		if (cmdContext != null) {
			List<ContactRecord> phones = cmdContext.getContacts();
			if (!isEmpty(phones))

				for (ContactRecordBase r : phones) {
					if (newlist==null||!newlist.contains(r)) 
						   r.release();
				}
		}		
	}
	
	private volatile MagReply cmdContext=null;
	private volatile Results lastSearchResults=null;
	
	private boolean anyOtherResults() {
		return lastSearchResults!=null&&lastSearchResults.anyOthers();
	}
	
	private String  initialMatches[]=null;	
	
	
	private int           initialDesiredPhoneType=0;
	private Set<Integer>  initialPhoneTypes=null;
	private boolean firstPass=true;
	private Results otherSecondPassResults=null;
	private String firstPassBestMatcher=null;
	
	/*
	private boolean isAfterOther() {
	   return lastSearchResults!=null&&lastSearchResults.getStepNo()>0;	
	}
	*/
	
	@Override
	public boolean handleReplyInBg(MagReply reply) {
	  MagReply prevContext=cmdContext;
	  cmdContext=reply; otherSecondPassResults=null;
	  Understanding understanding=reply.getUnderstanding();
	  switch (understanding.getCommandCode()) {
	  case Understanding.CMD_OTHER:
		  Understanding prvU=prevContext.getUnderstanding();
		  understanding.setPhoneType(prvU.getPhoneType());
		  understanding.setContactNames(prvU.getContactNames());
		  if (lastSearchResults!=null) {
			  otherSecondPassResults=lastSearchResults.nextResults();
			  if (otherSecondPassResults!=null) {
				 cmdContext.setContacts(otherSecondPassResults.getContactsWithResources(true)); 
				 lastSearchResults=otherSecondPassResults;
			  }
			  return true;
		  }
		  break;
	  case Understanding.CMD_CALL:
		  //  Langutils.init();
		    firstPass=initialMatches==null||understanding.isPhoneNumberGiven();
		    foundContact=null;
		    
		    if (firstPass) {
		      firstPassBg();
		    } else {
		      if (prevContext!=null) cmdContext.setContacts(prevContext.getContacts());
		      secondPassBg();
		    }
		    
		  return true;
	  }
	  abort(0);
	  return false;
	}
	
	private void refine() {
		Log.d(TAG, "refining ...");
		firstPass=true;
		initialMatches=null;
		firstPassBg();
	}
	
	protected boolean silentlySelected=false;
	
	public void clearSilentlySelected() {
		silentlySelected=false;
	}
	
	
	public boolean isSilentlySelected() {
		return silentlySelected;
	}

	private void firstPassBg() {
	    Understanding understanding=cmdContext.getUnderstanding();
		if (understanding.isPhoneNumberGiven()) {
		   initialMatches=null;
		   initialDesiredPhoneType=CalleeAssociation.calculateDesiredPhoneType(understanding.getPhoneType());
		   ContactRecord cr=PhoneBook.getJoinedContactByPhone(understanding.getDescription());
		   if (cr!=null) {
			   cmdContext.setContacts(Arrays.asList(new ContactRecord[]{cr}));
			   silentlySelected=true;
			   understanding.setNumber(false);
			   Log.d(TAG, "using custom record "+cr.toString());		  
		   }
		} else if (!isEmpty(understanding.getContactNames())) {
			/*
				understanding.setContactNames(
				  new String[] {
					//	  "santa","center","saida",
					//	  "sarah","sovereign" 
					//	  "vansgsdgfgfgfgdjdfg", "vaXzRnce"
						  "alex","vaXzRnce"
				  }
				);
				*/
			
		
				initialMatches=understanding.getContactNames();
				initialDesiredPhoneType=CalleeAssociation.calculateDesiredPhoneType(understanding.getPhoneType());
				initialPhoneTypes=ContactRecord.getContactTypes(understanding.getPhoneType());
				cae=new CalleeAssocEngine(initialDesiredPhoneType);
				
				  
				Pair<List<ContactRecord>,Results> p=firstPathCalculations(
					initialMatches,
					initialPhoneTypes
				);
				if (p!=null) {
					cmdContext.setContacts(p.first);
					lastSearchResults=p.second;
				}
			
		}	
	}
	
	private Pair<List<ContactRecord>,Results> firstPathCalculations(String contactNames[], Set<Integer> phoneTypes) {
		return 
				
			Config.good_phonetic_search_result_has_more_priority_than_associations	
				?_firstPathCalculationsNew(contactNames, phoneTypes)
				:_firstPathCalculationsOld(contactNames, phoneTypes)
			  ;
	}
	
	private Pair<List<ContactRecord>,Results> _firstPathCalculationsNew(String contactNames[], Set<Integer> phoneTypes) {
		long millis = System.currentTimeMillis(); 
		Results lastSearchResults=(Results)pbMatcher.getCandidates(
				contactNames, 
				getPbook().getAllContacts(),
				phoneTypes
		);

		millis = System.currentTimeMillis() - millis; 
		
		if (!lastSearchResults.shrinkToSingleIfBetterThan(1.)) {
			firstPassBestMatcher=lastSearchResults.getBestMatcher();
		}
		
		// TODO: downgrade log level 
		Log.d(TAG, "Top-level contact search for [" + contactNames + "] millisec latency = " + millis); 
		
		int resultsCount=lastSearchResults.countResults();
		
		if (resultsCount>1||resultsCount==0) {
			String idx[]=getCalleeAssociations(initialMatches);
			if (!isEmpty(idx)) for (String ix:idx) {
				Log.d(TAG, "custom recors found: "+ix);
				ContactRecord cr=PhoneBook.getJoinedContactByPhone(ix);
				if (cr!=null) {
					cae.touch(ix, initialMatches);
					Log.d(TAG, "using custom record "+cr.toString());
					return 
						new Pair<List<ContactRecord>,Results>(	
							Arrays.asList(new ContactRecord[]{cr}),
							null
						);
				}
			}
		}
		
		if (lastSearchResults.countResults()>1) {
			lastSearchResults.getContactsWithResources();
			Log.d(TAG,"primary_search "+dump(lastSearchResults.getContacts()));
		}
		return 
		  new Pair<List<ContactRecord>,Results>(		
			 lastSearchResults.getContacts(),
		     lastSearchResults
		  );
	}
	
	private Pair<List<ContactRecord>,Results> _firstPathCalculationsOld(String contactNames[], Set<Integer> phoneTypes) {
		String idx[]=getCalleeAssociations(initialMatches);
		if (!isEmpty(idx)) for (String ix:idx) {
			Log.d(TAG, "custom recors found: "+ix);
			ContactRecord cr=PhoneBook.getJoinedContactByPhone(ix);
			if (cr!=null) {
				cae.touch(ix, initialMatches);
				Log.d(TAG, "using custom record "+cr.toString());
				return 
					new Pair<List<ContactRecord>,Results>(	
						Arrays.asList(new ContactRecord[]{cr}),
						null
					);
			}
		}
	
		long millis = System.currentTimeMillis(); 
		Results lastSearchResults=(Results)pbMatcher.getCandidates(
				contactNames, 
				getPbook().getAllContacts(),
				phoneTypes
		);

		millis = System.currentTimeMillis() - millis; 
		
		if (!lastSearchResults.shrinkToSingleIfBetterThan(1.)) {
			firstPassBestMatcher=lastSearchResults.getBestMatcher();
		}
		
		// TODO: downgrade log level 
		Log.d(TAG, "Top-level contact search for [" + contactNames + "] millisec latency = " + millis); 
		
		if (lastSearchResults.countResults()>1) {
			lastSearchResults.getContactsWithResources();
			Log.d(TAG,"primary_search "+dump(lastSearchResults.getContacts()));
		}
		return 
		  new Pair<List<ContactRecord>,Results>(		
			 lastSearchResults.getContacts(),
		     lastSearchResults
		  );
	}
	
	private  void updateContactList(List<ContactRecord> newlist) {
		cleanTheContactListRepresentation(newlist); // should be optimized later
		cmdContext.setContacts(newlist);
		showContactList(newlist);
	}
	
	final static int PICK_CONTACT=7677;
	
	private boolean moreSearchResultAvailable() {
	   return lastSearchResults!=null&&lastSearchResults.anyOthers();
	}
	
	private boolean areWeInTheFirstStage() {
	   return lastSearchResults==null||lastSearchResults.getStepNo()==0;
	}
	
	WeakReference<Activity> proxyActivity=null;
	
	@SuppressWarnings("unchecked")
	private  void showContactList(final List<ContactRecord> lst) {
	  final String selectAContactFromPhonebook=context.getString(R.string.M_select_from_phonebook);
	  
	  new RunningInActivity(context) {
	
		@Override
		protected void onActivityResult(int requestCode, int resultCode,Intent data) {
			CalleeSelectionHandler.this.onActivityResult(requestCode, resultCode, data);
			super.onActivityResult(requestCode, resultCode, data);
		}

		@Override
		public boolean onBackPressed() {
			Log.d(TAG,"onBackPressed");
		    Context ctx=CalleeSelectionHandler.this.context;
		    if (ctx instanceof AborterHolder)  {
		    	Log.d(TAG,"onBackPressed -- abortOperation ");
		       return ((AborterHolder)ctx).abortOperation(0);
		    }
			return super.onBackPressed();
		}

		@Override
		public boolean isRequiringSuzie() {
		   return true;
		}
			  
	    @Override
		public void run() {
	    	
	    	if (usingProxyActivity) {
	    		Activity oldAc=proxyActivity==null?null:proxyActivity.get();
	    		if (oldAc!=null) oldAc.finish();
	    		proxyActivity=new WeakReference<Activity>(activity);
	    	}
	    	
			DecoratedListView dlv=((IOptionsListViewHolder)activity).getOptionsListView();
			
			TextView footer=dlv.getFooter();
			dlv.setFooterText(selectAContactFromPhonebook);
			ListView lv=dlv.getListView();
			footer.setClickable(true);
			footer.setOnClickListener(
			  new OnClickListener() {
				@Override
				public void onClick(View v) {
					MyTTS.abort();
					MyTTS.speakText(R.string.P_select_contact_manually_i_will_learn);
					peekContactManually(activity);
				}
				  
			  }
			);

			/*
			boolean fOther=moreSearchResultAvailable();
			List<ContactRecord> lst0=cmdContext.getContacts();
			final List lst=fOther?new ArrayList(lst0):lst0;
			
			if (fOther) {
			  lst.add(
				new Runnable() {
					@Override
					public void run() {
						
					}
					
					@Override
					public String toString() {
						return "more ...";
					}
				}
			  );
			}
			*/
			lv.setAdapter(PhoneBook.prepareAdapter(activity,lst));
			lv.setOnItemClickListener(
			  new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					MyTTS.abort();
					Object o=position<lst.size()?lst.get(position):null;
					if (o!=null) { 
					  if (o instanceof ContactRecord)
					    callSingleAndRemember((ContactRecord)o,true);
					  else
					  if (o instanceof Runnable) ((Runnable)o).run();
					}
					
					/*else if (position==lst.size()||o==selectAContactFromPhonebook) {
						peekContactManually();
					}*/
				}
				  
			  }
			);
	    }
	  };
	}
	
	private void peekContactManually(Activity ctx) {
		Intent it = new Intent(
				Intent.ACTION_PICK,		
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI
				);
		//it.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
		Launchers.startNestedActivityWithCode(
		   ctx,
		   it, PICK_CONTACT,
		   new SuccessFailure() {
			@Override
			public void onCancel() {
				super.onCancel();
				CalleeSelectionHandler.this.abort(0);
			 }
			   
		   }
		);		
	}
	
	private void firstPassUI() {
		final Understanding understanding=cmdContext.getUnderstanding();
		boolean shouldFinish=true;
		
		
		final List<ContactRecord> phones = cmdContext.getContacts();
		
		if (
			isEmpty(understanding.getContactNames())&&
			isEmpty(phones)&&
			!understanding.isPhoneNumberGiven()
		) {
			Output.show(context, understanding
					.getQueryInterpretation().getToShow(),
					understanding.getQuery());
			if (mainActivity==null) 
			   Launchers.openDialer(context);
			else
			   mainActivity.callCurrentSelection(understanding);
			VoiceIO.fireOpes();
		} else {

			if (understanding.isPhoneNumberGiven()) {
				silentlySelected=true;
				Output.sayAndShow(context,
				   new MyTTS.Wrapper(getPerformByNumberString(understanding.getDescription())) {
					 @Override
					 public void onSaid(boolean fAborted) {
						super.onSaid(fAborted);
						if (!fAborted&&performFinalAction(understanding.getDescription()))
						   	   VoiceIO.fireOpes();
					 }
				   }
			   );

			} else {

				if (isEmpty(phones)) {
					shouldFinish=false;
					MyTTS.speakText(R.string.P_select_contact_manually);
					  new RunningInActivity(context) {
							
							@Override
							protected void onActivityResult(int requestCode, int resultCode,Intent data) {
								CalleeSelectionHandler.this.onActivityResult(requestCode, resultCode, data);
								super.onActivityResult(requestCode, resultCode, data);
								if (super.usingProxyActivity) activity.finish();
							}

							@Override
							public void run() {
								peekContactManually(activity);		
							}
					  };
					VoiceIO.fireOpes();
				} else {
					if (phones.size() > 1) {
						shouldFinish=false;
						showContactList(phones);
						askToSelectACallee(understanding.getQuery());
						VoiceIO.listenAfterTheSpeech();
					} else {
						shouldFinish=false;
						silentlySelected=true;
						ContactRecord r=phones.get(0);
						callSingle(
						  r,
						  understanding.getQuery(),
						  isEmpty(initialMatches)
						    ?null
						    :pbMatcher.findRelevantMatches(initialMatches, r)
						);
					}
				}
			}
		}

	    if (shouldFinish) shutdown();		
	}

	@Override
	public boolean handleReplyInUI(MagReply reply) {
	  Understanding understanding=reply.getUnderstanding();
	  switch (understanding.getCommandCode()) {
	    case Understanding.CMD_OTHER:
	    	if (otherSecondPassResults!=null) {
			     List<ContactRecord> phones=cmdContext.getContacts();
			     if (!isEmpty(phones)) {
			    	updateContactList(phones);
			    	askToSelectACallee(true);
			    	VoiceIO.listenAfterTheSpeech();
			     } else
			    	 VoiceIO.fireOpes();
	    		 return true;
	    	} else {
	    	   Output.sayAndShow(context, R.string.P_NO_MORE_MATCHING_CONTACTS);
	    	   VoiceIO.listenAfterTheSpeech();
  		     //  shutdown();
	    	}
	    	return true;
		case Understanding.CMD_CALL:
			if (firstPass)
	          firstPassUI();
			else 
			  secondPassUI();
			return true;
	  }
	  return false;
	}
	
	private void askToSelectACallee() {
		askToSelectACallee(null,false);
	}
	
	private void askToSelectACallee(boolean fMore) {
		askToSelectACallee(null,fMore);
	}
	
	private void askToSelectACallee(String query) {
		askToSelectACallee(query,false);
	}
	
	private void askToSelectACallee(String query, boolean fMore) {
		    if (mainActivity==null)
		    	SuziePopup.hideBubles(true, true);
		
		
		  //MyTTS.speakText("There are several matches in your contacts, please say ");
			
			List<ContactRecord> contacts = cmdContext.getContacts(); 
			boolean sayCommandExplicitly = true; 
			if (!Utils.isEmpty(query) && contacts != null) {
				String[] queryWords = query.split("\\s"); 
				// if query is not too long AND the names are all common U names
				// we can cope with natural user input
				if (queryWords != null && queryWords.length <= 4) {
					sayCommandExplicitly = false; 
					for (ContactRecord rec : contacts) {
						String[] nameParts = rec.getName().split("[\\s\\/\\+,:;&()-]+");
						if (!Utils.isEmpty(nameParts) && 
								!commonNames.isCommonName(nameParts[0], Locale.US)) {
							sayCommandExplicitly = true;
							break; 
						}
					}
				}
			}
				MyTTS.speakText(
				   new MyTTS.BubblesInMainActivityOnly(
				     Utils.getString(
						(sayCommandExplicitly
						   ?R.string.P_what_person_w_command
						   :R.string.P_what_person)
				     )
				   )
				);
	
			speakContactsList(contacts,  getActionName(), sayCommandExplicitly);
//		  boolean several=cmdContext.getContacts().size()>1;
//		  MyTTS.speakText("There are "+(fMore?"yet "+(several?"more":"one"):"several")+" "+(several?"matches":"match")+" in your contacts, please say ");
//		  sayContactsList(cmdContext.getContacts(),true);
//		  MyTTS.speakText(R.string.P_MAKE_YOUR_PICK);
			
	}
	
	protected CharSequence condTranslit(String s) {
	   return MyTTS.hasUnsupporedLetters(s)?translit.process(s):s;
	}
	
	private Translit translit=Translit.getHebRus();
	
	
	private boolean contactsWithSameName(List<ContactRecord> cc) {
		if (!isEmpty(cc)) for (ContactRecord c:cc) for (ContactRecord d:cc) 
			if (!c.isSamePhone(d)&&c.iSsameTypeAs(d)&&c.iSsameNameAs(d)) return true;
		return false;
	}
	
	private void speakContactsList(List<ContactRecord> cc, String action, boolean withIntrusion) {
		
		// bug - sometimes i see it !!
		if (cc == null)
			return;
		
		if (cc.size()>pbMatcher.MAX_BEST_CASES)
			cc=cc.subList(0, pbMatcher.MAX_BEST_CASES);
		_speakContactsList(cc, action, withIntrusion);
	}
	
	private void _speakContactsList(List<ContactRecord> cc, String action, boolean withIntrusion) {
		
		
		if (!cc.isEmpty()) { 
			Map<String, Integer> contactsPerName = new HashMap<String, Integer>();
			for (ContactRecordBase r : cc) {
				String name = r.getName().toString().toLowerCase(); 
				Integer cnt = contactsPerName.get(name); 
				if (null == cnt || cnt == 0) {
					contactsPerName.put(name,1); 
				} else {
					contactsPerName.put(name, cnt+1); 
				}
			
			}
			
			String ordinals[]=context.getResources().getStringArray(R.array.callee_selection_cl_ordinals);
			
			boolean sameNames=contactsWithSameName(cc);
			final int iLast = cc.size()-1; 
			for (int i=0;i<=iLast;i++) {
				ContactRecordBase r=cc.get(i);
				StringBuilder sb = new StringBuilder();
				String name = r.getName().toLowerCase(); 
				if (withIntrusion) {
					if (iLast == i) {
						// sb.append("; ");
						sb.append(context.getString(R.string.callee_selection_cl_or) );
						sb.append(' ');
					}
					sb.append(action).append(' ');
				}
				sb.append(condTranslit(name)); 
				
				// append phone type only if there is > 1
				Integer cnt = contactsPerName.get(name); 
				if (cnt != null && cnt > 1)
					sb.append(' ').append(((ContactRecord)r).getFormattedPhoneType());
				

				if (cc.size() > 1 && (/*withIntrusion||*/sameNames) && (i < ordinals.length)) {
					sb.append(' ');
					sb.append(context.getString(R.string.callee_selection_cl_or));
					sb.append(" \"");
					sb.append(ordinals[i]);
					sb.append("\" ");
					sb.append(context.getString(R.string.callee_selection_cl_one));
				}

				MyTTS.speakText(new MyTTS.BubblesInMainActivityOnly(sb));
			}
			if (anyOtherResults() 
					&& cc.size() < 4)  // sounds too long otherwise
				MyTTS.speakText(new MyTTS.BubblesInMainActivityOnly(context.getString(R.string.P_SAY_OTHER)));// to get more matching contacts");
		}
	}
	
	private boolean associationsRemembered=false;

	public boolean areAssociationsRemembered() {
		return associationsRemembered;
	}

	private void callSingleAndRemember(final ContactRecord r, boolean relevantMatchesOnly) {
		boolean anyMathces=!isEmpty(initialMatches);
		
		String matches[]=
				   /*relevantMatchesOnly&&anyMathces
				      ?pbMatcher.findRelevantMatches(initialMatches, r)
				      :*/initialMatches;
		
		
				   
		if (anyMathces) {
			associationsRemembered=true;
			saveCalleeAssociations(
			   r, 
			   matches
			);
		}
		callSingle(r,null,matches);

	}
	
	protected void onBeforePerformByContactRecord(ContactRecord r) {}
	
	private void doFinalAction(boolean fAborted, final String matches[], final ContactRecord r) {
		boolean fo=true;
		if (fAborted) {
			if (!(clearsCalleeAssociationsManually()||isEmpty(matches)))
				   Utils.runInBgThread(
							  new Runnable() {
								@Override
								public void run() {
									Log.d(TAG,"BG: clearCalleeAssociations(r,matches)");
									clearCalleeAssociations(r,matches);
								}  
							  }
				   );	
			giveUp();
		} else {
			fo=performFinalAction(r,matches);
		}
		shutdown();
		if (fo) VoiceIO.fireOpes();				
	}
	
    private void callSingle(final ContactRecord r,String query, final String matches[]) {
		inactivate();
		onBeforePerformByContactRecord(r);
		Pair<Object[],String> op=getPerformByContactRecordOutput(r,query);
		if (op == null)
			doFinalAction(false, matches, r);
		else
		Output.sayOnlyOrSayAndShow(
			context, 
			false, 
			op.second,
			new MyTTS.OnSaidListener() {

				@Override
				public void onSaid(boolean fAborted) {
					Log.d(TAG,"onSaid -- "+fAborted);
					doFinalAction(fAborted, matches, r);
				}
				
			}, 
			op.first
		);
	}
	
	private ContactRecord foundContact=null;
	private List<ContactRecord> secondPassCandidates=null;
	
	private void secondPassUI() {
		if (foundContact==null) {
			if (isEmpty(secondPassCandidates)) {
			  if (mainActivity==null) showContactList(cmdContext.getContacts());
		    } else
			  updateContactList(secondPassCandidates);
			askToSelectACallee();
			VoiceIO.listenAfterTheSpeech();
		} else {
			callSingleAndRemember(foundContact,true);
		}		
	}
	
	private List<ContactRecord> calculateSecondPathSearch(
			LinkedHashSet<LinkedHashSet<String>> mchs,
			List<ContactRecord> phones,
			Set<Integer> contactTypes
	) {
		SearchResult secondPassResult=pbMatcher.getCandidates(
		        mchs,
				phones, 
				contactTypes,
				true,
				1,
				0.8,//NeoPhonebookMatcher.STD_BEST, 
				0.35, //NeoPhonebookMatcher.STD_WORST
				true
		);

        secondPassResult.shrinkToSingleIfBetterThan(0.8);

        return secondPassResult.getContacts();
	}
	
	private void secondPassBg() {
	   Understanding understanding=cmdContext.getUnderstanding();
	   String mat[]=understanding.getContactNames();
	   
       LinkedHashSet<LinkedHashSet<String>> mchs0=setize(mat);
   	   LinkedHashSet<LinkedHashSet<String>> mchs= Langutils.improve_phonetics(mchs0);
 
    	  	
		List<ContactRecord> phones=cmdContext.getContacts();			
		
		
		if (!isEmpty(phones)) {
			int nPhones = phones.size(); 
			if (understanding.getOrdinal()!=null) {
			  int ix = understanding.getOrdinal()-1;
			  if (ix >= 0 && ix < nPhones) {
				  foundContact = phones.get(ix);
				  Log.d(TAG, "found by ordinal");
				  return;
			  }
			}
			
			Set<Integer> ctypes=ContactRecord.getContactTypes(understanding.getPhoneType());
			
			secondPassCandidates=null; 
			if (foundContact==null&&!(isEmpty(understanding.getPhoneType())&&isEmpty(understanding.getContactNames()))) {					

				secondPassCandidates=calculateSecondPathSearch(mchs,phones,ctypes);
				
				if (!isEmpty(secondPassCandidates)) { 
				   if (secondPassCandidates.size()==1) {
					   foundContact=secondPassCandidates.get(0);
					   Log.d(TAG,"found by basic second path");
					   return;
				   }
				   if (!isEmpty(mat)) {
					   // try to exclude best matcher
					   if (!isEmpty(firstPassBestMatcher)) {
						   LinkedHashSet<LinkedHashSet<String>> mchs2=pbMatcher.excludeMatcher(mchs, firstPassBestMatcher);
						   if (!isEmpty(mchs2)) {
							   secondPassCandidates=calculateSecondPathSearch(mchs2,phones,ctypes);
							   if (secondPassCandidates.size()==1) {
								   foundContact=secondPassCandidates.get(0);
								   Log.d(TAG,"found by second path w/o prev search best matcher");
								   return;
							   }
						   }
					   }
					
				   }
				   
				}
				   // do combine
				   {
					  boolean anyNew[]={false};
					  String refinedMatchers[]=mul(initialMatches,mat,true,true,anyNew);
					  if (anyNew[0]) {
						  Pair<List<ContactRecord>,Results> p=
								  firstPathCalculations(refinedMatchers, or(initialPhoneTypes,ctypes));

						  if (p!=null) {
							  Log.d(TAG,"refined with combined matchers");
							  cmdContext.setContacts(p.first);
							  lastSearchResults=p.second;
							  firstPass=true;
							  return;
						  }
					  }
					  
				   }		
				
			}
			
			if (foundContact==null)
				   refine();
		}
	}

	private NeoPhonebookMatcher pbMatcher=new NeoPhonebookMatcher();
	private CommonNames commonNames=new CommonNames();
	private volatile PhoneBook pBook=null;
	private Object pbSO=new Object();
	
	private PhoneBook getPbook() {
		synchronized(pbSO) {
		  if (pBook==null) pBook=PhoneBook.getInstance();
		}
		return pBook;
	}
	
	final static long TWO_WEEKS=14l*24l*60l*60l*1000l;
	
	private String [] getCalleeAssociations(String contactNames[]) {
       return cae.getCalleeAssociations(contactNames,System.currentTimeMillis()+TWO_WEEKS);
	}
	
	public boolean clearsCalleeAssociationsManually() {
		return false;
	}
	
	// may be called from a GUI thread
	public void clearCalleeAssociations(final ContactRecordBase rec, boolean clearOldOne) {
		if ((associationsRemembered||clearOldOne)&&!isEmpty(initialMatches)) 
		   Utils.runInBgThread(
			  new Runnable() {
				@Override
				public void run() {
					clearCalleeAssociations(rec,initialMatches);
					
				}  
			  }
		   );		   
	}
	
	// may NOT be called from a GUI thread
	private void clearCalleeAssociations(ContactRecordBase rec, String cnames[]) {
	   if (cae!=null) cae.clearCalleeAssociations(rec, cnames);
	}
	
	protected void saveCalleeAssociations(ContactRecordBase rec, String cnames[]) {
	   if (!(cae==null||isEmpty(cnames))) {
		  List<CallSmsAttempt> lst=callSmsAttemptsHistory.getRelevantAttempts(/*initialDesiredPhoneType*/-1, isInSmsMode());
	      if (isEmpty(lst)) {
		     cae.saveCalleeAssociations(rec, cnames);
	      } else {
	    	 Set<String> whole=new HashSet<String>();
	    	 for (String s:cnames) whole.add(s.toLowerCase());
	    	 for (CallSmsAttempt a:lst) {
	    		String acns[]=a.getContactNames();
	    		if (!isEmpty(acns)) for (String s:acns) whole.add(s.toLowerCase());
	    	 }
	    	 Log.d(TAG, "saveCalleeAssociations: extended set: "+Utils.dump(whole));
	    	 String wh[]=new String [whole.size()];
	    	 cae.saveCalleeAssociations(rec,  whole.toArray(wh));
	      }
	   }
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG,"onActivityResult");
		
		if (requestCode==PICK_CONTACT) {
			MyTTS.abort();
			if (resultCode==Activity.RESULT_OK) {
				Uri u=data.getData();
				if (u!=null) {
					Log.d(TAG, "poke contact: "+data.getDataString());
					ContactRecord rec=PhoneBook.getByUri(data.getData());
					if (rec!=null) {
						callSingleAndRemember(rec,false/*remember all matches*/);
						return true;
					}
				}
			} 
			abort(0);
			return true;
		}
		return false;
	}

	@Override
	public String getClientStateName() {
		if (isTimeoutTooLong()) {
			inactivate();
			Log.d(TAG, " deactivating by timeout ");
			giveUp();
			return null;
		}
		touchLastInteractionTime();
		return ClientStateInformer.SN_CALLEE_SELECTION;
	}
	
	public boolean performFinalAction(ContactRecord r, String cnames[]) {
		return performFinalAction(r.getPhone());
	}
	
	abstract public Object getPerformByNumberString(String number);
	
	
	public String getContactNameToSay(ContactRecord r) {
		String closest=pbMatcher.findMostClose(r.getNames(), initialMatches);
		if (isEmpty(closest)) {
			Collection<String> nms=r.getNames();
			if (!isEmpty(nms)) for (String n:nms) {
			   if (!isEmpty(n)&&((closest==null)||(closest.length()<n.length()&&!Utils.isPhoneNumber(n)))) 
				   closest=n;
			}
		}
		if (isEmpty(closest)) {
			closest=r.getName();
			Log.d(TAG,"this should never be");
		}
       return closest;
	}
	
	public Pair<Object [],String> getPerformByContactRecordOutput(ContactRecord r, String q) {
		return new  Pair<Object [],String>(getPerformByContactRecordOutput(r),q);
	}
	
	public Object [] getPerformByContactRecordOutput(ContactRecord r) { 
		return new Object[] { getPerformByContactRecordString(r) };
	}
	
	public String getPerformByContactRecordString(ContactRecord r) { return null; }
	
	abstract public String getActionName();
	
	abstract public boolean performFinalAction(String phoneNumber);

}
