//package com.magnifis.parking.feed;
//
//import static com.magnifis.parking.VoiceIO.sayAndShow;
//import static com.magnifis.parking.VoiceIO.sayShowFromGuiThenComplete;
//import static com.robinlabs.utils.BaseUtils.isEmpty;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Pattern;
//
//import org.apache.commons.lang.StringEscapeUtils;
//import org.w3c.dom.Element;
//
//import com.facebook.android.AsyncFacebookRunner;
//import com.magnifis.parking.App;
//import com.magnifis.parking.Json;
//import com.magnifis.parking.MainActivity;
//import com.magnifis.parking.Output;
//import com.magnifis.parking.R;
//import com.magnifis.parking.SuccessFailure;
//import com.magnifis.parking.Xml;
//import com.magnifis.parking.fb.BaseRequestListener;
//import com.magnifis.parking.fb.FbHelper.AuthAdapter;
//import com.magnifis.parking.messaging.Message;
//import com.magnifis.parking.model.FbFeedPost;
//import com.magnifis.parking.model.FbFeedResponse;
//import com.magnifis.parking.utils.Utils;
//
////import de.l3s.boilerpipe.BoilerpipeProcessingException;
////import de.l3s.boilerpipe.extractors.ArticleExtractor;
//
//import android.content.Context;
//import android.os.Bundle;
//import android.util.Log;
//
//public class NewsFeedController extends MessageFeedController {
//	
//	
//	private static final String TAG = NewsFeedController.class.getSimpleName();
//	private static Pattern ptrnIncompleteTail = Pattern.compile("[?!;.\u2014][\\s]*[A-Za-z0-9 :;,'-�]+[.]{3}[\\s]*$"); // incomplete sentences with "..." at the end
//
//	protected List<Message> currentNewsBits = new ArrayList<Message>(); // TODO: using Message for news bits is a hack
//	
//
//	
//	protected NewsFeedController(Context activity) {
//		super(activity);
//	}
//	
//	
//	@Override
//	public int getPageSize() {
//		return 100; // big number 
//	}
//	
//	@Override
//	public void getN(int N, String sinceId, boolean fNew, boolean exclId,
//			boolean fetchBody, SuccessFailure<List<Message>> handler) 
//	{
//		Log.i(TAG, "Fetching " + N + " news items starting with #" + sinceId); 
//		if (Utils.isEmpty(currentNewsBits)) {
//			Log.i(TAG, "Nothing to fetch!"); 
//			return; 
//		}
//		
//		int iStart = 0;
//		if (!Utils.isEmpty(sinceId)) {
//			if (sinceId.equals("null")) {
//				iStart = 1; // TODO: hack!!
//			} else {
//				iStart = -1; 
//				try {
//					int k = Integer.valueOf(sinceId); 
//					if (N < 0) {// e.g., -1
//						iStart = k + N; 
//						N = -N; // absolute value  
//					} else {
//						//iStart =  k+1; // start with next one
//						// TODO: ?????????????????????????????????
//						iStart =  k+2; // start with next one
//					}
//				} catch (NumberFormatException e) {
//					Log.i(TAG, "Non-numeric start item..."); 
//					e.printStackTrace();
//				} 
//			}
//		} 
//		
//		int i = 0; 
//		List<Message> msgs=new ArrayList<Message>();
//		for (Message msg : currentNewsBits) {
//			if (iStart < 0) {
//				if (msg.getId().equals(sinceId)) 
//					iStart = i;
//			}
//			
//			// find the starting point 
//			if (i++ < iStart || iStart < 0) {
//				continue; 
//			}
//			
//			msgs.add(msg); 
//			if (msgs.size() >= N)
//				break; 
//		}
//		
//		handler.onSuccess(msgs);
//		
//	}
//	
//	
//	
//	
//	@Override
//	/*
//	 * Plays back a subsequence prepopulated by the callee (typically starting from the current cursor position)
//	 */
//    public void play(List<Message> ms, final boolean markAsRead, final String sayBefore) {
//		if (!isEmpty(ms)) {
//			if (!isEmpty(sayBefore)) sayAndShow(sayBefore);
//			int i = 0; 
//			for (final Message m:ms) {
//
//				final StringBuilder sb=new StringBuilder();
//				String title = m.getSubject(); 
//				if (!isEmpty(title)) {
//					sb.append(title);
//					sb.append(".\n");
//				} else 
//					continue; 
//				
//				final String body = m.getBody().trim(); 
//				String spokenBody = null; 
//				boolean maybeHtml = (!isEmpty(body) && body.startsWith("<"));
//				if (maybeHtml) {
//					try {
//						spokenBody = StringEscapeUtils.unescapeHtml(
//								ArticleExtractor.getInstance().getText(body) // extract readable text
//							);
//					}catch (BoilerpipeProcessingException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					if (body.equals(spokenBody))
//						maybeHtml = false; 
//				} else 
//					spokenBody = body; 
//				
//				if (!isEmpty(spokenBody)) { 
//				
//					spokenBody = formatForSpeach(spokenBody, maybeHtml); // TODO: why do it twice??
//					sb.append(spokenBody);
//				}
//			
//				
//				if (0 == i) { // just the first one
//					renderHtml(body, maybeHtml); 
//				}
//				
//				final String forSpeach = formatForSpeach(sb.toString(), maybeHtml);
//				final Message nextM = (++i < ms.size()) ? ms.get(i) : null; 
//				Output.sayAndShow(
//				  MainActivity.get(),
//				  new Runnable() {
//					  
//					@Override
//					public String toString() {
//						return forSpeach; //sb.toString();
//					}
//
//					@Override
//					public void run() {
//						
//						lastReadId=m.getId();
//						Log.i(TAG, "Last read item id is " + lastReadId);
//						
//						// render HTML, if available
//						if (nextM != null) { 
//							final String nextMbody = nextM.getBody().trim(); 
//							final boolean maybeHtml = (!isEmpty(body) && body.startsWith("<"));
//							
//							renderHtml(nextMbody, maybeHtml); 
//						}
//						
//						if (markAsRead&&!m.isRead()) 
//							markAsRead(m.getId()); 
//					}
//					  
//				  },
//				  forSpeach,
//				  true
//				);
//				
//			}
//		}    	
//    }
//	
//	
//
//	public static String formatForSpeach(String st, boolean maybeHtml) {
//		String txt = st.trim();
//		
//		txt = ptrnIncompleteTail.matcher(txt).replaceAll("").trim();
//		
//		// TODO: temp (for techmeme), remove!
//		txt = txt.replaceAll("^[ A-Za-z\\/]+:", ""); 
//		txt = txt.replaceAll("(\\.\\.\\.)[ ]*$", ""); 
//		
//		
//		txt = txt.replaceAll("#", ""); // remove just the remaining "#" signs
//		txt = txt.replaceAll("\\|", ","); 
//		txt = txt.replaceAll("\\/", "-"); 
//		txt = txt.replaceAll("\\*{2,}", ""); // ***
//		
//		return txt;
//	}
//		
//	
//	public boolean canShare() {
//		return false; // @TODO!!
//	}
//	
//	void renderHtml(final String body, final boolean maybeHtml) {
//		if (maybeHtml) {// possibly HTML
//			// substitute the page content to new HTML using Javascript 
//			final String jsCode = new StringBuffer("document.body.innerHTML=\"")
//					.append(body.replaceAll("\"", "'").replaceAll("\n", " "))
//					.append(" \"").toString();
//
//			Log.i(TAG, "HTML newsbit, attempting to render: \n" + jsCode);
//			MainActivity.get().runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					MainActivity.get().openUrl(
//							"http://robingets.me/void2.html", false, jsCode, null);
//				}
//			});
//		}
//	}
//	
//	
//}
