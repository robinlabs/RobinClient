package com.magnifis.parking.feed;

import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import android.app.Activity;

import com.google.code.samples.xoauth.XoauthAuthenticator;
import com.magnifis.parking.AThread;
import com.magnifis.parking.Advance;
import com.magnifis.parking.App;
import com.magnifis.parking.Log;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.MultipleEventHandler;
import com.magnifis.parking.Output;
import com.magnifis.parking.R;
import com.magnifis.parking.SuccessFailure;
import com.magnifis.parking.messaging.Message;

import com.magnifis.parking.messaging.Addressable;
import com.magnifis.parking.model.MailService;
import com.magnifis.parking.pref.PasswordPreference;
import com.magnifis.parking.toast.ToastController;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;
import static com.magnifis.parking.VoiceIO.*;

import static com.magnifis.parking.tts.MyTTS.speakText;
import static com.magnifis.parking.utils.Utils.*;

import com.sun.mail.imap.IMAPSSLStore;
import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.StringTerm;

import org.jsoup.Jsoup;


public class MailFeedController extends MessageFeedController {
	final static String TAG=MailFeedController.class.getSimpleName();
/*********
 [05/09/12 20:30:10] Ilya Eckstein: Client ID for installed applications
Client ID:    
922040378666-n0sqqu3k8lt1tvufnt12nnm98pmu3un2.apps.googleusercontent.com
Client secret:    
T3WjUomNOGtyr3UuI22UhFzw
Redirect URIs:    urn:ietf:wg:oauth:2.0:oob
 */
	
	final static String  
			MESSAGE_ID="Message-ID",
			TEXT_PLAIN="text/plain",
			TEXT_HTML="text/html";
	
	final static int MIC_LIVE_TIME=12000;

	public MailFeedController(MainActivity ma) {
		super(ma);
		XoauthAuthenticator.initialize();
	}
	
	@Override
	public void getN(
			int N, 
			String sinceId, 
			boolean fNew, 
			boolean exclId,
			boolean fetchBody,
			SuccessFailure<List<Message>> handler
	) {
		getN(N, sinceId, fNew, exclId, fetchBody, handler, false );		
	}

	

	@Override
	public boolean markAsRead(String key) {
		// TODO Auto-generated method stub
		return super.markAsRead(key);
	}
	
	Pattern enableImapPattern=Pattern.compile("\\[[^\\]]+\\]|\\([^\\)]+\\)");
	
	private void _consume(final MailService ms , final SuccessFailure<IMAPSSLStore> storeHandler) {
		try {	
			//boolean fSASL=false;
			IMAPSSLStore store=null;
			
			/*if (fSASL) {
			

				String token=App.self.peekGoogleToken(ac);
				if (token==null) {
					token=App.self.updateGoogleToken(ac, MainActivity.get(), false);
					if (token==null) {
						//cancel=true; 
						return;
					}
				}

				store=XoauthAuthenticator.connectToImap(
						"imap.googlemail.com",
						993,
						ac.name,
						token,
						null,//"", //oauthTokenSecret,
						//new OAuthConsumer(null,null,null,null),

						new OAuthConsumer(null,
								"1041048675389.apps.googleusercontent.com",
								"SzbcmimbKx0fYgonS1iQvU3p",null),
						//XoauthAuthenticator.getAnonymousConsumer(),   //consumer,
								 
								!App.self.isReleaseBuild //debug
						);
				Log.d(TAG, store!=null?"sto!=null":"hmm");
			
			} else*/ {
				Properties props = new Properties();
				Session session = Session.getInstance(props);
				session.setDebug(!App.self.isReleaseBuild);
				final URLName unusedUrlName = null;
				store = new IMAPSSLStore(session, unusedUrlName);
				
				String password=PasswordPreference.getDecoded(ms.getPasswordPrefKey());
				
				final boolean emptyPassword=isEmpty(password);
				
				try {
				   if (emptyPassword) throw new AuthenticationFailedException();
					
				   store.connect(
						ms.getImapServer(),
						993,
						ms.getMailAddress(),
						password
						);
				   
				} catch (AuthenticationFailedException afe) {
				   String msg=afe.getMessage();
				   Log.d(TAG, isEmpty(msg)?"":msg);
				   final MainActivity act=MainActivity.get();
				   
				   if (ms.isGmail()&&!isEmpty(msg)&&msg.toLowerCase().contains("settings page")) {					   
					   Output.sayAndShowFromGui(
						 act, enableImapPattern.matcher(msg).replaceAll(""), false
					   );
					   storeHandler.onCancel();
					   return;
				   }
					
                   act.runOnUiThread(
                	  new Runnable() {

						@Override
						public void run() {
							MyTTS.speakText(
							 emptyPassword
							   ?R.string.P_PASSWORD_NOT_SPECIFIED
							   :R.string.P_WRONG_PASSWORD
							);
							PasswordPreference.showPasswordDialog(
								act,
								ms.getPasswordPrefKey(),
								ms.getPasswordDialogTitle(),
								new SuccessFailure() {

									@Override
									public void onSuccess() {
									  Log.d(TAG, "onSuccess");
									  consume(storeHandler);
									}

									@Override
									public void onCancel() {
									  Log.d(TAG, "onCancel");
									  storeHandler.onCancel();
									}
									
								}
							);
						}
                		  
                	  }
                   );
                   return;
				} catch(Throwable t) {
					Log.d(TAG, "probably connection problem");
					final MainActivity act=MainActivity.get();
	                if (act!=null) act.runOnUiThread(
	                   new Runnable() {
	                	   @Override
	                	   public void run() {
	                		  storeHandler.onCancel();
	                	   }
	                    }
	                );
				}
				
			}
			if (store!=null&&store.isConnected()) {
				storeHandler.onSuccess(store);
			} else {
				storeHandler.onFailure();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private MailService mailService=MailService.fromPreferences();
	
	public void consume(final SuccessFailure<IMAPSSLStore> storeHandler) {
		mailService=MailService.fromPreferences();
		if (mailService==null) {
			storeHandler.onFailure();
			return;		  
		}

        new AThread() {
			@Override
			public void run() {
			   setAborter();
               _consume(mailService, storeHandler);
			}
        	
        }.start();
		
	}
	
	static class Cache {
	    SoftReference<List<Message>> cache=null;
	   
		public synchronized List<Message> get(
				int N, 
				String sinceId, 
				boolean fNew, 
				boolean exclId
		) {
		  if (sinceId!=null&&cache!=null) {
			  List<Message> ceds=cache.get();
			  if (ceds!=null) {
				  Message found=null;
				  int i=0;
				  for (;i<ceds.size();i++) {
					 Message ced=ceds.get(i);
					 if (sinceId.equals(ced.getId())) {
						 found=ced; break;
					 } 
				  }
				  if (found!=null) {
					 boolean pos=N>0;
				     int start=exclId?(pos?i+N:i-N):i, end=start+N+(pos?-1:1);
				     if (start>=0&&end<ceds.size()) {
				    	 if (pos&&start==0&&end==(ceds.size()-1)) return ceds;
				    	 ArrayList<Message> r=new ArrayList<Message>(Math.abs(N));
				    	 if (pos)
				    	   for (i=start;i<=end;i++) r.add(ceds.get(i));
				    	 else {
				    	   for (i=start;i<=end;i--) r.add(ceds.get(i));
				    	 }
				    	 return r;
				     }
				  }
			  }
		  }
		  return null;
		}
		
		public synchronized void put(List<Message> ms) {
			cache=new SoftReference<List<Message>>(ms);
		}
	
	}
	
	private Cache cache=new Cache();

	final static int N_PRELOAD=10;
	
	public void getN(
			final int N, 
			final String sinceId, 
			final boolean fNew, 
			final boolean exclId,
			final boolean fetchBody,
			final SuccessFailure<List<Message>> handler,
			final boolean markAsRead
	)  {
	  final boolean useCache=!((sinceId==null)||fetchBody||markAsRead);
      if (useCache) {
    	List<Message> c=cache.get(N, sinceId, fNew, exclId);
    	if (!isEmpty(c)) {
    		handler.onSuccess(c); return;
    	}
      }
		
	  MyTTS.speakText(R.string.P_COMMUNICATION_WMS);
	  consume(
		new SuccessFailure<IMAPSSLStore>() {

			@Override
			public void onSuccess(IMAPSSLStore store) {
				//final SuccessFailure<List<Message>> actualHandler=handler;
				if (useCache&&fAdvance) {
					 final boolean pos=N>0;
					 _getN(
							    store,
							    N,//pos?N_PRELOAD:-N_PRELOAD, 
							    sinceId, 
							    false, 
							    exclId,
							    false,
							    new SuccessFailure<List<Message>> () {
									@Override
									public void onSuccess(List<Message> lst) {
										handler.onSuccess(lst);
									}

									@Override
									public void onCancel() {
										handler.onCancel();
									}

									@Override
									public void onFailure() {
										handler.onFailure();
									}    	
							    },
							    false
							);					
				} else
				 _getN(
				    store,
				    N, 
				    sinceId, 
				    fNew, 
				    exclId,
				    fetchBody,
				    handler,
				    markAsRead
				);
			}

			@Override
			public void onCancel() {
	              MainActivity.get().runOnUiThread(
	            		  new Runnable() {

	            			  @Override
	            			  public void run() {
	            				  handler.onCancel();
	            			  }

	            		  }
	               );
			}

			@Override
			public void onFailure() {
              MainActivity.get().runOnUiThread(
            		  new Runnable() {

            			  @Override
            			  public void run() {
            				  handler.onFailure();
            			  }

            		  }
               );
			}
			
		}
	  );
	}
	

	private void _getN(
			final IMAPSSLStore store,
			final int N, 
			final String sinceId, 
			final boolean fNew, 
			final boolean exclId,
			final boolean fetchBody,
			final SuccessFailure<List<Message>> handler,
			final boolean markAsRead
	)  {
				
              final AThread th=(AThread)Thread.currentThread();
	    	  
	    	  try {
	    		  
	    		boolean cancel=false;
                 th.condAbort();
                 boolean success=false;
                 final ArrayList<Message> msgs=new ArrayList<Message>();
                 if (!cancel&&store!=null&&store.isConnected()) try {
                	th.condAbort();
                	Folder f=store.getFolder("Inbox");
                	f.open(markAsRead?Folder.READ_WRITE:Folder.READ_ONLY);
                	if (f!=null) {
                	  Log.d(TAG,"f!=null");
                	  int cnt=0;
                	  //boolean anyNew=f.hasNewMessages();
                	  
                	  boolean pos=N>0;
                	  
                	  int mc=f.getMessageCount();
                	  
                	  int from=pos?mc:1,
                		  to=pos?1:mc,
                		  inc=pos?-1:1;
                	   
                	  if (sinceId!=null) {
                		 if (mailService.isSuppportsSearchById()) {
                		   StringTerm st=new MessageIDTerm(sinceId);///HeaderTerm(MESSAGE_ID,sinceId);
                		   javax.mail.Message ms[]=f.search(st);
                		   
                		   if (isEmpty(ms)||!sinceId.equals(getId(ms[0]))) {
                			   success=true; return;
                		   } else {
                			   from=ms[0].getMessageNumber();
                			   if (exclId) {
                				   if (pos) --from; else ++from;
                			   } else if (N==1) {
                				   msgs.add(convert(ms[0],fetchBody));
                				   if (markAsRead&&!ms[0].isSet(Flag.SEEN)) {
                					   ms[0].setFlag(Flag.SEEN, true);
                				   }
                				   success=true;
                				   return;
                			   }
                		   }                 		   
                		   
                		 } else {
                			from=Integer.parseInt(sinceId);
             				if (exclId) {
            					if (pos) --from; else ++from;
            				}
                		 }
                	  }
                	  
                	  boolean firstIsNew=false;
                	  
                	  if (from>0&&from<=mc) for (int i=from;pos?i>=to:i<=to ;i+=inc) {
                		  th.condAbort();
                		  
                		  
                		 if (++cnt>Math.abs(N)) break;
                		 
                		 javax.mail.Message m=f.getMessage(i);
                		 
                		 boolean fRead=m.isSet(Flag.SEEN);
                		 
                		 if (cnt==1) firstIsNew=!fRead;
                		 
                		 if (fNew&&firstIsNew&&cnt>1&&fRead) break;
                		 
                		 
                		 th.condAbort();

                		 msgs.add(convert(m,fetchBody));
                	  }
                	}
                	success=true;
                 } finally {
                   if (success||cancel) {
                	  final boolean s=success;
                   	  MainActivity.get().runOnUiThread(
                     	   new Runnable() {

     						@Override
     						public void run() {
     							if (s&&!th.fAbort) handler.onSuccess(msgs); else handler.onCancel();
     						}
                     		   
                     	   }
                      );
                      return;                   
                    }
                 }
	    		  
			} catch (Throwable e) {
				if (e!=null) e.printStackTrace();
			}
	        MainActivity.get().runOnUiThread(
	          new Runnable() {
					@Override
					public void run() {
						if (th.fAbort) handler.onCancel(); else handler.onFailure();
					}
	           }
	         );

	}
	
	private static String getHeaderFirst(javax.mail.Message m, String name) {
	  try {
		String t[]=m.getHeader(name);
		if (!isEmpty(t)) return t[0];
	  } catch (MessagingException e) {}
	  return null;
	}
	
	private static String getId(javax.mail.Message m) {
		return getHeaderFirst(m,MESSAGE_ID);
	}
	
	private Message convert(javax.mail.Message m, boolean fetchBody) throws MessagingException, IOException {
		Message msg=new Message();
		msg.setType(Message.TYPE_EMAIL);
		msg.setSubject(m.getSubject());
		msg.setSent(m.getSentDate());
		msg.setReceived(m.getReceivedDate());
		
		if (false) {
			Enumeration<Header> en=m.getAllHeaders();
			if (en!=null) while (en.hasMoreElements()) {
				Header h=en.nextElement();
/****
 hdr=X-YMAIL-UMID --> 2_0_0_1_22_AOXsHkgAAT7rUQFcBgeuUkDBvNQ
 hdr=Received --> from [2.55.126.244] by web162306.mail.bf1.yahoo.com via HTTP; Thu, 24 Jan 2013 08:06:30 PST
 hdr=Date --> Thu, 24 Jan 2013 08:06:30 -0800 (PST)
 hdr=From --> Yahoo! Mail <mailbot@yahoo.com>
 hdr=Subject --> Welcome to Yahoo!
 hdr=To --> zeev.belkin@yahoo.com
 hdr=MIME-Version --> 1.0
 hdr=Content-Type --> text/html; charset=us-ascii
 hdr=Content-Length --> 12181
 */
				Log.d(TAG, "hdr="+h.getName()+" --> "+h.getValue());
			}
		} 
		
        msg.setId(
          mailService.isSuppportsSearchById()
            ?getId(m)
            :Integer.toString(m.getMessageNumber())
        );
        Log.d(TAG,"id="+msg.getId());
        msg.setRead(m.isSet(Flag.SEEN));
        Address as[]=m.getFrom();
        if (!isEmpty(as)) {
       	 // InternetAddress
       	   msg.setSender( Addressable.fromEmail(as[0]));
        }
        
        if (fetchBody||isEmpty(msg.getSubject())) {
        	String fullBody = extractBodyText(m.getContent(),m.getContentType()); 
        	String body = filterMsgHistory(fullBody);
       	  	msg.setBody(body);
        }
		return msg;
	}
	
	// @see http://regexlib.com/REDetails.aspx?regexp_id=404 
	Pattern filterHistoryPattern = Pattern.compile(
			"On ((Mon|Tue|Wed|Thu|Fri|Sat|Sun),)? " 
					+ "((((Jan(uary)?|Ma(r(ch)?|y)|Jul(y)?|Aug(ust)?|Oct(ober)?|Dec(ember)?)\\ 31)|" +
					"((Jan(uary)?|Ma(r(ch)?|y)|Apr(il)?|Ju((ly?)|(ne?))|Aug(ust)?|Oct(ober)?|" +
					"(Sep(t)?|Nov|Dec)(ember)?)\\ (0?[1-9]|([12]\\d)|30))|(Feb(ruary)?\\ " +
					"(0?[1-9]|1\\d|2[0-8]|(29(?=,\\ " +
					"((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|" +
					"((16|[2468][048]|[3579][26])00)))))))\\,\\ ((1[6-9]|[2-9]\\d)\\d{2}))" + 
					" at ([0-9]{1,2}):([0-5][0-9])([ ])?([aApP][mM]), .*wrote", 
		Pattern.CASE_INSENSITIVE
	);
	
	private String filterMsgHistory(String fullBody) {
		
		if (Utils.isEmpty(fullBody)) return fullBody; 

		
		String[] parts = filterHistoryPattern.split(fullBody); 
		if (!Utils.isEmpty(parts))
			return parts[0]; 
		
		return null;
	}
	
	

	@Override
	public void readDetailed() {
	  if (lastReadId==null) {
		 MyTTS.speakText(R.string.P_no_messages_to_read);
		 listenAfterTheSpeech();
	  }
	  final MultipleEventHandler.EventSource es=MainActivity.get().showProgress();
	  detailedMode=true;
	  getN(
		1,
		lastReadId,
		false,
		false,
		true,
		new SuccessFailure<List<Message>>() {

			@Override
			public void onSuccess(List<Message> ms) {
				es.fireEvent();
				if (isEmpty(ms)) {
				  onFailure();
				} else {
				  es.fireEvent();
				  sayAndShow(ms.get(0).getBody());
				  condListenAfterTheSpeech();
				}
 			}

			@Override
			public void onFailure() {
				es.fireEvent();
				speakText(R.string.P_SOMETHING_WENT_WRONG);
				condListenAfterTheSpeech();
			}

			@Override
			public void onCancel() {
				es.fireEvent();
				condListenAfterTheSpeech();
			}
			
			
		},
		true // mark as read
      );
	}

	static String extractCharset(String type) {
	   if (!isEmpty(type)) {
		 for (String s:type.split(";")) {   
		   s=s.trim().toLowerCase();
		   if (s.startsWith("charset=")) {
			  int i=s.indexOf('"'),j=s.lastIndexOf('"');
			  if (i>0&&j>i) return s.substring(i+i,j);
		   }
		 }
	   }
	   return null;
	}
	
	static String extractBodyText(Object raw, String ctype) throws MessagingException {
    	if (raw!=null) {
    	  if (raw instanceof Multipart) {
    		  Multipart mp=(Multipart)raw;
    		  for (int k=0;k<mp.getCount();k++) {
   			      BodyPart bp=mp.getBodyPart(k);
   			      String t=bp.getContentType().toLowerCase();
   			      if (t.startsWith(TEXT_HTML)) {
       			      String s=extractBodyText(bp,t);
       			      if (s!=null) return s;   			    	  
   			      }
    		  }
    		  for (int k=0;k<mp.getCount();k++) {
    			   BodyPart bp=mp.getBodyPart(k);
    			   if (bp!=null) {
       			      String s=extractBodyText(bp,bp.getContentType());
       			      if (s!=null) return s;
    			   }
    		  }
    	  } else if (raw instanceof Part) {
    		  Part p=(Part)raw;
    		  ctype=ctype.toLowerCase();
    		  boolean tp=ctype.startsWith("text/"),th=ctype.startsWith(TEXT_HTML);
    		  if (!(tp||th)) return null;
    		 
     	      String r=null;
    	      
    	      try {
    	    	  InputStream is=p.getInputStream();
    	    	  InputStreamReader isr=null;

    	    	  try {
    	    		  String cs=extractCharset(ctype);
    	    		  if (!isEmpty(cs)) isr=new InputStreamReader(is, cs);
    	    	  } catch(Throwable t) {
    	    		  t.printStackTrace();
    	    	  }
    	    	  if (isr==null) isr=new InputStreamReader(is, "UTF-8");
    	    	  try {
    	    		  StringWriter sw=new StringWriter();
    	    		  char buf[]=new char [1024];
    	    		  for (;;) {
    	    			  int i=isr.read(buf);
    	    			  if (i<=0) break;
    	    			  sw.write(buf, 0, i);
    	    		  }
    	    		  r=sw.getBuffer().toString();
    	    	  } finally {
    	    		  is.close();
    	    	  }
    	      } catch (IOException e) {
    	    	  e.printStackTrace();
    	      }
    		 
    	      if (th&&r!=null) {
    			  Log.d(TAG,TEXT_HTML);
    			  return Jsoup.parse(r).text();   	    	  
    	      } else {
    	    	  Log.d(TAG,"other: "+ctype); 
    	      }
    	      
    	      return r;
    	  } else {
      		Log.d(TAG,"raw.class="+raw.getClass().getCanonicalName());
      		return raw.toString();
      	  }
    	}
    	return null;
	}
	
	public  void playOne(final Activity act, final Message m) {
		
		final StringBuilder sb=new StringBuilder(App.self.getString(R.string.sms_message_from) + " ");
		
		Addressable ab=m.getSender();
		if (!isEmpty(ab.getDisplayName(true))) {
			sb.append(ab.getDisplayName(true));
			sb.append(' ');
		} else
		  sb.append(ab.getAddress());
		sb.append(' ');
		Date dt=m.getReceived();
		if (dt!=null) {
		  sb.append(App.self.getString(R.string.sms_message_received) + " ");
		  sb.append(formatMessageDate(dt));
		  sb.append(' ');
		}
		if (!isEmpty(m.getSubject())) {
			sb.append(App.self.getString(R.string.message_subject) + " " + m.getSubject());
			sb.append(' ');
		}
		if (!isEmpty(m.getBody())) sb.append(m.getBody());
		
		MyTTS.OnStringSpeakListener sw=new MyTTS.OnStringSpeakListener() {
			ToastController tc=null;
			@Override
			public String toString() {
			  return sb.toString();
			}

			@Override
			public void onSaid(boolean fAborted) {
				tc.abort();
				//lastReadId=m.getId();
				if (fAborted) 
				  fAdvance=false;
				else
				  if (fAdvance) App.self.voiceIO.setAdvance(advance);
			}

			@Override
			public void onToSpeak() {
				tc=new ToastController(act, sb.toString() ,true);
				lastReadId=m.getId();
			}
         };
         speakText(sw);
		/*
		MyTTS.OnStringSpeakListener sw=new MyTTS.Wrapper(sb) {
			@Override
			public void onSaid(boolean fAborted) {
				lastReadId=m.getId();
				if (fAborted) 
				  fAdvance=false;
				else
				  if (fAdvance) App.self.voiceIO.setAdvance(advance);
			}
			@Override
			public void onToSpeak() {
			}
         }.setShowInASeparateBubble();
         Output.sayAndShow(context, sw);
         */
	}	
	
	@Override
    public void play(List<Message> ms, final boolean _fNew, String sayBefore) {
		if (!isEmpty(ms)) {
			boolean first=true;
			for (final Message m:ms) {
				if (fAdvance&&fNew&&m.isRead()) {
					fAdvance=false; fNew=false;
					sayAndShow(R.string.P_NO_NEW_MESSAGES);
					break;
				}
				if (first) {
					first=false;
					if (!isEmpty(sayBefore)) sayAndShow(sayBefore);
				}
				playOne(MainActivity.get(), m);
			}
		}    	
    }
	
	private Advance advance=new Advance(App.self.getString(R.string.mailfeedcontroller_advance_promt), MIC_LIVE_TIME) {

		@Override
		public void run() {
            MailFeedController.this.readNext();
		}
		
	};

	@Override
	public int getPageSize() {
		return 1;
	}
	
}
