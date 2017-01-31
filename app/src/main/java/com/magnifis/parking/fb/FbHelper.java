package com.magnifis.parking.fb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import android.os.Bundle;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.magnifis.parking.App;
import com.magnifis.parking.Json;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.MultipleEventHandler;
import com.magnifis.parking.Output;
import com.magnifis.parking.Phrases;
import com.magnifis.parking.R;
import com.magnifis.parking.SuccessFailure;
import com.magnifis.parking.VoiceIO;
import com.magnifis.parking.Xml;
import com.magnifis.parking.fb.SessionEvents.AuthListener;
import com.magnifis.parking.fb.SessionEvents.LogoutListener;
import com.magnifis.parking.feed.MessageFeedController;
import com.magnifis.parking.messaging.Addressable;
import com.magnifis.parking.messaging.Message;
import com.magnifis.parking.model.FbFQLFeedPost;
import com.magnifis.parking.model.FbFQLFeedPostPage;
import com.magnifis.parking.model.FbFQLFeedPostPagesResponse;
import com.magnifis.parking.model.FbFQLFeedPostUser;
import com.magnifis.parking.model.FbFQLFeedPostUsersResponse;
import com.magnifis.parking.model.FbFQLFeedResponse;
import com.magnifis.parking.model.FbFeedPost;
import com.magnifis.parking.model.FbFeedResponse;
import com.magnifis.parking.model.LearnAttribute;
import com.magnifis.parking.toast.ToastController;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;

import static com.magnifis.parking.tts.MyTTS.speakText;
import static com.magnifis.parking.utils.Utils.formatMessageDate;
import static com.magnifis.parking.utils.Utils.isEmpty;
import static com.magnifis.parking.VoiceIO.*;

public class FbHelper extends MessageFeedController  {
	
	private static WeakReference<FbHelper> selfWr=null;
	
	public static FbHelper getInstance() {
		synchronized(FbHelper.class) {
			FbHelper self=selfWr==null?null:selfWr.get();
			return self==null?new FbHelper():self;
		}
	}
	
	public FbHelper() {
		super(MainActivity.get());
		selfWr=new WeakReference<FbHelper>(this);
	}	
	
	final public static int AUTHORIZE_ACTIVITY_RESULT_CODE = 117;

	public static final String robinFbAppId = "310277872395686";
	public static final String robinFbAppName = App.self.getString(R.string.fbhelper_app_name);
//	public static final String robinGPlayUrl = "https://play.google.com/store/apps/details?id=com.magnifis.parking";
	public static final String robinGPlayUrl = "http://bit.ly/robinApp";
	public static final String robinFbAppDesc = App.self.getString(R.string.fbhelper_app_desc);
	public static final String[] permissions = {"user_posts", 
//												"publish_stream",
												"user_birthday"}; 
	final static String TAG=FbHelper.class.getSimpleName();
	
	private final static int POST = 0, STATUS = 1;
	private static int command; 
	
	public Facebook facebook = new Facebook(robinFbAppId);
	
	static String userId = null; // unknown yet
	
	Map<String,Message> gNewsBuffer = new ConcurrentHashMap<String,Message>(); 
	Integer iInvokeNews = 0; 
	
	
	public static class DialogAdapter implements DialogListener {

		@Override
		public void onComplete(Bundle values) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFacebookError(FacebookError e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onError(DialogError e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	synchronized void initFb(final AuthAdapter authListener, final DialogListener dl) {
				
        //FbUtility.mFacebook = facebook; 
        // Instantiate the asyncRunner object for asynchronous api calls.
        //FbUtility.mAsyncRunner = new AsyncFacebookRunner(FbUtility.mFacebook);

       
        // restore session if one exists
        SessionStore.restore(facebook, App.self);

        // lazy-authenticate 
        if (facebook.isSessionValid())  { 
			SessionEvents.onLoginSuccess();
			authListener.onAuthSucceed();
			dl.onComplete(null);
		} else {
			authListener.onBeforeAuthDialog();
			Log.d(TAG, "Fb Activity created, authorizing..."); 
			facebook.authorize(MainActivity.get(),  permissions,  
					App.self.isReleaseBuild
					  ?AUTHORIZE_ACTIVITY_RESULT_CODE
					  :Facebook.FORCE_DIALOG_AUTH,
					new DialogListener() {
				 
						@Override
						public void onComplete(Bundle values) {
							SessionEvents.onLoginSuccess();
							authListener.onAuthSucceed();
							dl.onComplete(values);
						}
	
						@Override
						public void onFacebookError(FacebookError error) {
							SessionEvents.onLoginError(error.getMessage());
							authListener.onAuthFail(error.getMessage());
							dl.onFacebookError(error);
						}
	
						@Override
						public void onError(DialogError error) {
							SessionEvents.onLoginError(error.getMessage());
							authListener.onAuthFail(error.getMessage());
							dl.onError(error);
						}
	
						@Override
						public void onCancel() {
							SessionEvents.onLoginError("Action Canceled");
							authListener.onCancel();
							dl.onCancel();
						}
			});
        }		
	}

	
	public void resetCredentials() {
		
		new Thread() {

			@Override
			public void run() {
				SessionStore.clear(App.self);
				try {
					facebook.logout(App.self.getCurrentActivity());
					SessionStore.clear(App.self);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage()); 
				} 
				
			}}.start();
		
		
	}
	
	public static class AuthAdapter implements AuthListener {
		
		private AuthAdapter delegate=null;
		
		public AuthAdapter() {}
		public AuthAdapter(AuthAdapter delegate) { this.delegate=delegate; }
		
		public void onBeforeAuthDialog() {
		  if (delegate!=null) delegate.onBeforeAuthDialog();
		}
		public void onFailure() {
			if (delegate!=null) delegate.onFailure();
		}
		
		public void onCancel() {
			if (delegate!=null) delegate.onCancel(); else onFailure();
		}

		@Override
		public void onAuthSucceed() {
			if (delegate!=null) delegate.onAuthSucceed();
			
		}

		@Override
		public void onAuthFail(String error) {
			if (delegate!=null) delegate.onAuthFail(error); else {
				Log.w(TAG, "Facebook login failed"); 
				onFailure();
			}
		}
		
	}
	
	public void consume(AuthAdapter al) {
		if (facebook.isSessionValid())
			al.onAuthSucceed();		
		else {
			initFb(new AuthAdapter(al) {
				@Override
				public void onAuthSucceed() {
					SessionStore.save(facebook,App.self);
					super.onAuthSucceed();	
				}
			  },
			  new DialogAdapter()
			); 			
		}
	}
	
	public void getUserBirthday(final SuccessFailure<Date> sf) {
		consume(new AuthAdapter() {
			
			@Override
			public void onCancel() {
				sf.onCancel();
			}

			@Override
			public void onFailure() {
				sf.onFailure();
			}

			@Override
			public void onBeforeAuthDialog() {
				sayShowFromGuiThenComplete(App.self.getString(R.string.fbhelper_need_to_access_your_account_to_produce_horoscope));
			}

			@Override
			public void onAuthSucceed() {
		        Bundle params = new Bundle();
		        params.putString("fields", "birthday,id");
		        new AsyncFacebookRunner(facebook).request("me", params, new BaseRequestListener() {

					@Override
					public void onComplete(String response, Object state) {
						Log.d(TAG, response);
						try {
							JSONObject js=new JSONObject(response);
							
							userId=js.getString( "id"); // get it opportunistically 
	
							String bd=js.getString( "birthday");
							if (!Utils.isEmpty(bd)) {
							  Log.d(TAG,"birthday="+bd);
							  SimpleDateFormat sdf=new SimpleDateFormat("MM/dd/yyyy");
							  sf.onSuccess(sdf.parse(bd));
							  return; // success
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						onFailure();
					}
		        	
		        });            
			}
		});	
	}
	
	public void shareAppToFeed(final String message) {
		shareAppToFeed(message, robinFbAppDesc);
	}
	
	public void shareAppToFeed(final String message, final String descripion) {
		consume(new AuthAdapter() {
			
			@Override
			public void onFailure() {
				super.onFailure();
				VoiceIO.fireOpes();
			}

			@Override
			public void onAuthSucceed() {
			   try {
				  postAppToFeed(message,descripion);
			   } catch(Throwable t) {
				   VoiceIO.fireOpes();
			   }
			}
		});
	}
	
	static final String ROBIN_ICON_URL = "http://robingets.me/images/sbicon.png";

	//protected static final int MIN_STORY_BODY_LENGTH = 100; // less then a 100 characters is not enough "meat" 
	
	
	void postAppToFeed(String message) {
		postAppToFeed(message, robinFbAppDesc);
	}
	
	void postAppToFeed(String message, String description) {
		command = POST;
		Bundle params = new Bundle();
		//params.putString("caption", getString(R.string.app_name));
		params.putString("description", description);
//		params.putString("picture", ROBIN_ICON_URL);
//		params.putString("link", robinGPlayUrl);
//      http://www.midwesternmac.com/blogs/jeff-geerling/facebook-oauthexception-when
		params.putString("name", robinFbAppName);
		params.putString("message", message+"\n"+robinGPlayUrl);
		params.putString("type", "link");
		/*
		  JSONObject privacy = new JSONObject();
	        try {
	            privacy.put("value", "EVERYONE");
	        } catch (JSONException e) {
	            Log.e(TAG, "Unknown error while preparing params", e);
	        }
	        
	        
	    Log.d(TAG,privacy.toString());

		*/
		//params.putString("privacy", /*"{value:'EVERYONE'}"*/privacy.toString());
	

		// TODO: restore:
		// Utility.mFacebook.dialog(Hackbook.this, "feed", params, new
		// UpdateStatusListener());

		new AsyncFacebookRunner(facebook).request("feed", params, "POST",
				new DirectPostToFeedListener(), null);

		String access_token = facebook.getAccessToken();
		if (null == access_token)
			System.out.println("access_token is NULL!!");
		else 
			System.out.println(access_token);
		
		
	}

	public void postToWall() {
		consume(new AuthAdapter() {
			
			@Override
			public void onFailure() {
				super.onFailure();
				VoiceIO.fireOpes();
			}
			
			@Override
			public void onCancel() {
				super.onCancel();
			}

			@Override
			public void onBeforeAuthDialog() {
				sayShowFromGuiThenComplete(App.self.getString(R.string.fbhelper_need_to_access_your_account_to_post));
			}

			@Override
			public void onAuthSucceed() {
				 speakText(R.string.P_DONT_UNDERSTAND_YOU);
				 facebook.dialog(MainActivity.get(), "feed", new DialogListener() {
					 
				        @Override
				        public void onFacebookError(FacebookError e) {
				        }
				 
				        @Override
				        public void onError(DialogError e) {
				        }
				 
				        @Override
				        public void onComplete(Bundle values) {
				        }
				 
				        @Override
				        public void onCancel() {
				        }
				    });	
			}
		});
		
		
	}

	
	public void setStatus(final String status) {
		consume(new AuthAdapter() {
			
			@Override
			public void onFailure() {
				super.onFailure();
				VoiceIO.fireOpes();
			}
			
			@Override
			public void onCancel() {
				super.onCancel();
			}

			@Override
			public void onBeforeAuthDialog() {
				sayShowFromGuiThenComplete(App.self.getString(R.string.fbhelper_need_to_access_your_account_to_set_status));
			}

			@Override
			public void onAuthSucceed() {
			   try {
					command = STATUS;
					Bundle params = new Bundle();
					params.putString("message", status);
					params.putString("type", "status");

					new AsyncFacebookRunner(facebook).request("feed", params, "POST",
							new DirectPostToFeedListener(), null);

					String access_token = facebook.getAccessToken();
					if (null == access_token)
						System.out.println("access_token is NULL!!");
					else 
						System.out.println(access_token);
			   } catch(Throwable t) {
			   }
			}
		});
	}
	
	/*
     * Request user name, and picture to show on the main screen.
     */
   void _requestUserData() {
       // mText.setText("Fetching user name, profile pic...");
        Bundle params = new Bundle();
        params.putString("fields", "name, picture");
        new AsyncFacebookRunner(facebook).request("me", params, new UserRequestListener());
    }

    /*
     * Callback for fetching current user's name, picture, uid.
     */
    public class UserRequestListener extends BaseRequestListener {

        @Override
        public void onComplete(final String response, final Object state) {
            JSONObject jsonObject;
            if (!Utils.isEmpty(response)) try {
            	Log.d(TAG,response);
            	
                jsonObject = new JSONObject(response);

                final String picURL = jsonObject.getString("picture");
                final String name = jsonObject.getString("name");
                final String user_birthday=jsonObject.getString("user_birthday");
               // FbUtility.userUID = jsonObject.getString("id");


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
    
	 public class FbAPIsAuthListener implements AuthListener {

	        @Override
	        public void onAuthSucceed() {
	        	SessionStore.save(facebook, App.self);
	        	//requestUserData();
	        }

	        @Override
	        public void onAuthFail(String error) {
	        	Log.w(TAG, "Facebook login failed"); 
	            //smText.setText("Login Failed: " + error);
	        }
	    }

	    /*
	     * The Callback for notifying the application when log out starts and
	     * finishes.
	     */
	    public class FbAPIsLogoutListener implements LogoutListener {
	        @Override
	        public void onLogoutBegin() {
	        	Log.i(TAG, "Facebook logging out"); 
	        	//mText.setText("Logging out...");
	        }

	        @Override
	        public void onLogoutFinish() {
	        	Log.i(TAG, "Facebook logout done"); 
//	            mText.setText("You have logged out! ");
//	            mUserPic.setImageBitmap(null);
	        }
	    }

	    
	public class DirectPostToFeedListener implements RequestListener {
		@Override
		public void onComplete(final String response, final Object state) {
			Log.i(TAG, "Facebook API Response: " + response);
			// showToast("API Response: " + response);
			switch (command) {
			case STATUS:
				sayShowFromGuiThenComplete(App.self.getString(R.string.P_FB_STATUS_UPDATED));
				break;
			case POST:
				sayShowFromGuiThenComplete(App.self.getString(R.string.P_YOU_SHARED_ME_ON_FB));
				break;
			default:
				break;
			}
			condListenAfterTheSpeech();
		}

		@Override
		public void onIOException(IOException e, Object state) {
			Log.e(TAG, "Facebook: IO exception " + e);

		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			Log.e(TAG, "Facebook:FileNotFoundException " + e);

		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {

			Log.e(TAG, "Facebook: MalformedURLException " + e);
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// showToast("Feed Post Error: " + e.getMessage() + " for state " +
			// state);
			Log.e(TAG, "Facebook error " + e);

		}

		public void onFacebookError(FacebookError error) {
			Log.e(TAG, "Facebook error " + error);
		}

	}

	@Override
	public void getN(int N, String sinceId, boolean fNew, boolean exclId,
			boolean fetchBody, SuccessFailure<List<Message>> handler
	) {
		readFeed(N, sinceId, fNew, exclId, fetchBody, handler);
	}
	
	private static Pattern ptrnLink=Pattern.compile("http(s)?\\:\\/\\/\\S+");
	private static Pattern ptrnIncompleteTail = Pattern.compile("[?!.][\\n]*[A-Za-z0-9 :;,'-]+[.]{3}$"); // incomplete sentences with "..." at the end
	//private static Pattern ptrnTagTail = Pattern.compile("[^(at|in)] #[A-Za-z0-9_]+$"); // at end of string, unless preceded by (at|in)
	//private static Pattern ptrnRetweet = Pattern.compile("(RT|cc|CC)[ ]*@[A-Za-z0-9_]+"); // retweet / CC	

	
	public static String formatForSpeach(String st) {
		String txt = st.trim();
		// remove links
		String newTxt = ptrnLink.matcher(txt).replaceAll("").trim();
		txt = ptrnIncompleteTail.matcher(newTxt).replaceAll("").trim();
		txt = txt.replaceAll("#", ""); // remove just the remaining "#" signs
		txt = txt.replaceAll("\\|", ","); 
		txt = txt.replaceAll("\\*{2,}", ""); // ***
		return txt;
	}
	
	
	private void readFeed(final int N, final String sinceId, final boolean fNew, final boolean exclId,
			final boolean fetchBody, final SuccessFailure<List<Message>> handler
	) {
		consume(new AuthAdapter() {
			
			@Override
			public void onCancel() {
				handler.onCancel();
			}

			@Override
			public void onFailure() {
				handler.onFailure();
			}

			@Override
			public void onBeforeAuthDialog() {
			  sayShowFromGuiThenComplete(App.self.getString(R.string.fbhelper_need_to_access_your_account_to_read_feed));
			}

			@Override
			public void onAuthSucceed() {
		        Bundle params = new Bundle();
		        //int N, final String sinceId, final boolean exclId,
		        params.putString("fields", "id,type,description,message,from,link,picture,created_time");
		        params.putString("limit", "25");
		        new AsyncFacebookRunner(facebook).request("me/home", params, new BaseRequestListener() {
		        //params.putString("q", "SELECT post_id,description,actor_id,target_id,message,type FROM stream WHERE filter_key in (SELECT filter_key FROM stream_filter WHERE uid=me() AND type='newsfeed') AND is_hidden=0");
		        //new AsyncFacebookRunner(facebook).request("me/fql", params, new BaseRequestListener() {
					@Override
					public void onComplete(String response, Object state) {
								try {
									compat.org.json.JSONObject jso = new compat.org.json.JSONObject(response);
									Element el = Json.convertToDom(jso);
									
									FbFeedResponse feedResponse = Xml.setPropertiesFrom(el,FbFeedResponse.class);
									final ArrayList<Message> msgs = new ArrayList<Message>();
									FbFeedPost[] posts = feedResponse.getPosts();
									if (!Utils.isEmpty(posts)) {

										for (FbFeedPost post : posts) {
											
											// TODO: quick and dirty filtering!
											boolean isInteresting = true; 
											String type = post.getType();  
											if (!Utils.isEmpty(type)) {
												if (type.equalsIgnoreCase("photo") ||
													type.equalsIgnoreCase("video"))
													isInteresting = false; 
											}
											
											String spokenBody = null; 
											if (isInteresting) {
												Message msg = post.toMessage();
												if (msg != null && !Utils.isEmpty(msg.getBody())) 
													spokenBody = formatForSpeach(msg.getBody()).trim(); 
												
												if (Utils.isEmpty(spokenBody)) 
													isInteresting = false; 
		
												if (isInteresting) {
													String[] words = spokenBody.split(" ");	
													int nWords = (null == words) ? 0 : words.length; 
														
													if (nWords < 2)
														isInteresting = false; 
													else if (nWords < 6 && 
															!Utils.isEmpty(type) && type.equalsIgnoreCase("link"))
														isInteresting = false; 
						
													if (isInteresting) 
														msgs.add(msg);
												}
											}
										}
								
										MainActivity.get().runOnUiThread(
										  new Runnable() {
											 @Override
											 public void run() {
												 handler.onSuccess(msgs);
											 }
										  }
										);
										

										return; // success
									}
								} catch (compat.org.json.JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								MainActivity.get().runOnUiThread(
										  new Runnable() {
											 @Override
											 public void run() {
												 onFailure();
											 }
										  }
										);
						
					}		        	
		        });            
			}
		});
		
		
	}
	
	@Override
    public void play(List<Message> ms, final boolean markAsRead, final String sayBefore) {
		if (!isEmpty(ms)) {
			if (!isEmpty(sayBefore)) sayAndShow(sayBefore);
			for (final Message m:ms) {

				final StringBuilder sb=new StringBuilder();
				Addressable ab=m.getSender();
				String displayName = ab.getDisplayName(true); 
				if (!isEmpty(displayName)) {
					sb.append(displayName);
					sb.append(' ');
				} else 
					continue; 
				
		
				
				sb.append(Phrases.pickRemarkPhrase());
				//sb.append(formatMessageDate(m.getSent())); // don't really need dates in facebook 
				sb.append('\n');
				if (!isEmpty(m.getBody())) sb.append(m.getBody());
				
				final String forSpeach = formatForSpeach(sb.toString());
				Output.sayAndShow(
				  MainActivity.get(),
				  new Runnable() {
					  
					@Override
					public String toString() {
						return sb.toString();
					}

					@Override
					public void run() {
						if (markAsRead&&!m.isRead()) markAsRead(m.getId());
						lastReadId=m.getId();
					}
					  
				  },
				  forSpeach,
				  true
				);
				
			}
		}    	
    }

	ToastController tc=null;
	
	@Override
	public String getThingName() {
		return "status";
	}


	@Override
	public String getThingsName() {
        return "statuses";
	}
	
	public void news() {
		final MultipleEventHandler.EventSource es=MainActivity.get().showProgress();
		
		if (Utils.isEmpty(userId)) {
			tryAndGetMyUserId(); 
		}
		
		long curTimeSec = System.currentTimeMillis() / 1000; 
		readNews(curTimeSec, new SuccessFailure<Collection<Message>>() {
			@Override
			public void onSuccess(Collection<Message> ms) {
				if (null == ms) { // special case
					es.fireEvent();
					return; 
				}
				if (ms.size() > 0) {
					getNewsActors(new SuccessFailure<Collection<Message>>() {
						@Override
						public void onSuccess(Collection<Message> ms) {
							getNewsPages(new SuccessFailure<Collection<Message>>() {
								@Override
								public void onSuccess(Collection<Message> ms) {
									es.fireEvent();
									
									if (ms.size() > 0) {
										String[] news = new String[ms.size()];
										int i = 0; 
										for (Message post : ms) {
											Addressable sender = post.getSender(); 
											String title = post.getSubject(), body = post.getBody(); 
											
											if (!isEmpty(sender.getDisplayName(true))) {
												news[i++] = title + Phrases.getRandomPhrase(R.array.facebookNewsPhrases).replace("${name}", 
																							post.getSender().getDisplayName(true)) 
																+ ".\n" + body;  
											} else {
												news[i++] = title + "\n" + body;
											}
										}
										playTextAlerts(news, App.self.getString(R.string.P_FB_NEWS_INTRO));
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
							}, ms);
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
					}, ms);
				} else {
					es.fireEvent();
					speakText(R.string.P_YOU_HAVENT_ANY);
				}
				condListenAfterTheSpeech();
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
		});
	}
		
	private void tryAndGetMyUserId() {
		
		if (facebook.isSessionValid()) {
			// BD request gets the user ID too
			getUserBirthday(new SuccessFailure<Date>() {
				@Override
				public void onSuccess(final Date d) {
					Log.d(TAG, "Got FB user ID");
				}

				@Override
				public void onCancel() {
				}

				@Override
				public void onFailure() {
				}
			});
		}
	}


	public void getNewsPages(final SuccessFailure<Collection<Message>> successFailure, final Collection<Message> ms) {
		consume(new AuthAdapter() {
			
			@Override
			public void onCancel() {
				successFailure.onCancel();
			}

			@Override
			public void onFailure() {
				successFailure.onFailure();
			}

			@Override
			public void onBeforeAuthDialog() {
				sayShowFromGuiThenComplete(App.self.getString(R.string.fbhelper_need_to_access_your_account_to_read_feed));
			}

			@Override
			public void onAuthSucceed() {
				String ids = "";
				for (Message m : ms) {
					if (isEmpty(ids)) {
						ids = m.getSender().getAddress();	
					} else {
						ids = ids + "," + m.getSender().getAddress();
					}
				}
		        Bundle params = new Bundle();
		        params.putString("q", "SELECT page_id,name FROM page WHERE page_id in (" + ids + ")");
		        new AsyncFacebookRunner(facebook).request("me/fql", params, new BaseRequestListener() {
					@Override
					public void onComplete(String response, Object state) {
								try {
									compat.org.json.JSONObject jso = new compat.org.json.JSONObject(response);
									Element el = Json.convertToDom(jso);
									FbFQLFeedPostPagesResponse pagesResponse = Xml.setPropertiesFrom(el,FbFQLFeedPostPagesResponse.class);
									FbFQLFeedPostPage[] pages = pagesResponse.getPages();
									if (!Utils.isEmpty(pages)) {
										for (FbFQLFeedPostPage page : pages) {
											for (Message m : ms) {
												if (m.getSender().getAddress().equals(page.getPage_id())) {
													m.getSender().setDisplayName(page.getName());
												}												
											}
										}
									}
									MainActivity.get().runOnUiThread(
											new Runnable() {
												@Override
												public void run() {
													successFailure.onSuccess(ms);
												}
											});
									return; // success
								} catch (compat.org.json.JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								MainActivity.get().runOnUiThread(
										new Runnable() {
											@Override
											public void run() {
												onFailure();
											}
										});
					}		        	
		        });
			}
		});
	}	
	
	public void getNewsActors(final SuccessFailure<Collection<Message>> successFailure, final Collection<Message> ms) {
		consume(new AuthAdapter() {
			
			@Override
			public void onCancel() {
				successFailure.onCancel();
			}

			@Override
			public void onFailure() {
				successFailure.onFailure();
			}

			@Override
			public void onBeforeAuthDialog() {
				sayShowFromGuiThenComplete(App.self.getString(R.string.fbhelper_need_to_access_your_account_to_read_feed));
			}

			@Override
			public void onAuthSucceed() {
				String ids = "";
				for (Message m : ms) { 
					if (isEmpty(ids)) {
						ids = m.getSender().getAddress();	
					} else {
						ids = ids + "," + m.getSender().getAddress();
					}
				}
		        Bundle params = new Bundle();
		        params.putString("q", "SELECT uid,first_name, last_name FROM user WHERE uid in (" + ids + ")");
		        new AsyncFacebookRunner(facebook).request("me/fql", params, new BaseRequestListener() {
					@Override
					public void onComplete(String response, Object state) {
								try {
									compat.org.json.JSONObject jso = new compat.org.json.JSONObject(response);
									Element el = Json.convertToDom(jso);
									
									FbFQLFeedPostUsersResponse usersResponse = Xml.setPropertiesFrom(el,FbFQLFeedPostUsersResponse.class);
									FbFQLFeedPostUser[] users = usersResponse.getUsers();
									if (!Utils.isEmpty(users)) {

										for (FbFQLFeedPostUser user : users) {
											for (Message m : ms) {
												if (m.getSender().getAddress().equals(user.getId())) {
													m.getSender().setDisplayName(user.getFirst_name());
												}												
											}
										}
								
										
									}
									MainActivity.get().runOnUiThread(
											new Runnable() {
												@Override
												public void run() {
													successFailure.onSuccess(ms);
												}
											});
									return; // success
								} catch (compat.org.json.JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								MainActivity.get().runOnUiThread(
										  new Runnable() {
											 @Override
											 public void run() {
												 onFailure();
											 }
										  }
										);
					}		        	
		        });
			}
		});
	}	
	
	public void readNews(final long endTime, final SuccessFailure<Collection<Message>> handler) {
		consume(new AuthAdapter() {
			
			@Override
			public void onCancel() {
				handler.onCancel();
			}

			@Override
			public void onFailure() {
				handler.onFailure();
			}

			@Override
			public void onBeforeAuthDialog() {
				sayShowFromGuiThenComplete(App.self.getString(R.string.fbhelper_need_to_access_your_account_to_read_feed));
			}

			@Override
			public void onAuthSucceed() {
		        Bundle params = new Bundle();
		        //get only subscriptions posted links
		        //params.putString("q", "SELECT attachment,post_id,description,actor_id,target_id,message,type,filter_key,created_time FROM stream WHERE filter_key in (SELECT filter_key FROM stream_filter WHERE uid=me() AND type='newsfeed') AND type=80 AND is_hidden=0 AND actor_id in (SELECT page_id FROM page_fan WHERE uid=me()) LIMIT 200");
		        //get all posted links
		        
		        // query news for last 6 days in 3 batches 
		        final ArrayList<Message> msgs = new ArrayList<Message>();
		        final  long TWO_DAYS = 48*60*60; // in sec
		        String query = "SELECT attachment,post_id,description,actor_id,target_id,message,type,filter_key,created_time FROM stream WHERE filter_key in (SELECT filter_key FROM stream_filter WHERE uid=me() AND type='newsfeed') AND type=80 AND is_hidden=0 "; 
		        
		        synchronized (iInvokeNews) {
		        	iInvokeNews = 0;
		        	gNewsBuffer.clear(); 
				}
		         
		        
		        long timestamp = endTime; 
		        final int nNewsPolls = 3; 
		        for (int i = 0; i < nNewsPolls; i++) {
		        	String sqrQuery = query 
		        			+ " AND created_time < " + timestamp 
							+ " AND created_time > " + (timestamp - TWO_DAYS)
							+ " ORDER BY created_time DESC LIMIT 50"; 
			        params.putString("q", sqrQuery); 
			        timestamp -= TWO_DAYS; 
			        
			        //params.putString("q", "SELECT attachment,post_id,description,actor_id,target_id,message,type,filter_key,created_time FROM stream WHERE filter_key in (SELECT filter_key FROM stream_filter WHERE uid=me() AND type='newsfeed') AND is_hidden=0 ORDER BY created_time DESC LIMIT 50");
			        new AsyncFacebookRunner(facebook).request("me/fql", params, new BaseRequestListener() {  	
						@Override
						public void onComplete(String response, Object state) {
									try {
										compat.org.json.JSONObject jso = new compat.org.json.JSONObject(response);							
										Element el = Json.convertToDom(jso);
										
										FbFQLFeedResponse feedResponse = Xml.setPropertiesFrom(el,FbFQLFeedResponse.class);
										
										FbFQLFeedPost[] posts = feedResponse.getPosts();
										if (!Utils.isEmpty(posts)) {
	
											
											Set<String> allTitles = new HashSet<String>(posts.length); 
											for (FbFQLFeedPost post : posts) {
												
												// TODO: quick and dirty filtering!
												boolean isInteresting = true; 
												if (isEmpty(post.getAttacmentType())) {
													isInteresting = false;													
												} else if (!post.getAttacmentType().equals("link")) {
													isInteresting = false;													
												}
												
												if (!Utils.isEmpty(userId) && userId.equals(post.getActorId()))
													isInteresting = false; // user's own post 
												
												if (Utils.isEmpty(post.getAttachmentCaption()))
													isInteresting = false; // not a news link
										
												
												String spokenTitle = null, spokenBody = null; 
												Message msg = post.toMessage();
												String title = msg.getSubject(), body = msg.getBody(); 
												if (msg != null 
														&& !Utils.isEmpty(title)
														&& !Utils.isEmpty(body) 
														&& isInteresting) {
													spokenTitle = formatForSpeach(msg.getSubject()).trim();
													spokenBody = formatForSpeach(msg.getBody()).trim();
												}
												
	
												if (Utils.isEmpty(spokenBody) || Utils.isEmpty(spokenTitle)) 
													isInteresting = false; 
												else if (allTitles.contains(title))  // dedup
													isInteresting = false; 
												else
													allTitles.add(title); 
											
	//											if (isInteresting) {
	//												isInteresting = spokenBody.matches("\\w+");
	//											}
	
												if (isInteresting) {
													Integer counter = 0;
													for (char ch: spokenTitle.toCharArray()) {
														counter++;
													
														// filter non-ascii 
														if (((int)ch) > 256) {
															isInteresting = false;
															break;
														}
														if (counter > 30) {
															break;
														}
													}
												}
	
												if (isInteresting) {
														String[] words = spokenBody.split(" ");	
														int nWords = (null == words) ? 0 : words.length; 
															
														if (nWords < 10)
															isInteresting = false; 
												}
	
												if (isInteresting) 
													gNewsBuffer.put(msg.getSubject(), msg);
											}
									
											
										}
										
										synchronized (iInvokeNews) { 
											iInvokeNews++; 
											Collection<Message> news = null; 
											Log.i(TAG, "FB personal news: iInvokeNews = " + iInvokeNews); 
											if (iInvokeNews >= nNewsPolls) {
												iInvokeNews = 0;
												news = new ArrayList<Message>(); 
												if (!gNewsBuffer.isEmpty()) { 
													for (Message m : gNewsBuffer.values()) {
														news.add(m); 
													} 
													gNewsBuffer.clear(); 
												}
											} 
							
											final Collection<Message> newsList = news; 
											MainActivity.get().runOnUiThread(
													new Runnable() {
													@Override
													public void run() {
														handler.onSuccess(newsList);
													}
											});
										
										}
	
										return; // success
									} catch (compat.org.json.JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									MainActivity.get().runOnUiThread(
											  new Runnable() {
												 @Override
												 public void run() {
													 onFailure();
												 }
											  }
											);
							
						}		        	
			        });
		        }
			}
		});
		
		
	}
	
   
}
