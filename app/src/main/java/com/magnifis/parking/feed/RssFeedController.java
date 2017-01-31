//package com.magnifis.parking.feed;
//
//import java.lang.ref.WeakReference;
//import java.net.URL;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//
//import android.content.Context;
//import android.util.Log;
//
//import com.magnifis.parking.App;
//import com.magnifis.parking.MessageFeedContollerHolder;
//import com.magnifis.parking.messaging.Message;
//
//public class RssFeedController extends NewsFeedController {
//
//	private static final String TAG = RssFeedController.class.getSimpleName();
//
//	
//	private static WeakReference<RssFeedController> selfWr=null;
//	public static RssFeedController getInstance() {
//		synchronized(RssFeedController.class) {
//			RssFeedController fc = (selfWr==null?null:selfWr.get());
//			if (fc==null) 
//				fc=new RssFeedController(App.self);
//			return fc;
//		}
//	}
//
//	protected RssFeedController(Context activity) {
//		super(activity);
//		// TODO Auto-generated constructor stub
//	}
//	
//	
//	public void playFeed(URL rssUrl, boolean readTitles) {
//		
//		currentNewsBits.clear(); 
//		try {
//			// read response from the given news RSS url in the XML format
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//			Document doc = db.parse(rssUrl.openStream());
//			doc.getDocumentElement().normalize();
//			 
//			
//			NodeList nList = doc.getElementsByTagName("item"); 
//			
//			
//			// TODO: hack, restore now!
//			//int i = 0; 
//			int i = 3; 
//			
//			
//			for (; i < nList.getLength(); i++) {
//				Node nNode = nList.item(i);
//				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//					Message newsBit = new Message(); 
//		
//					Element eElement = (Element) nNode;
//					
//					if (readTitles)
//						newsBit.setSubject(getTagValue("title", eElement));
//					else 
//						newsBit.setSubject(". ");// needs something in the subject
//					
//					String body = getTagValue("description", eElement); 
//					// extract from html, if necessary
//					
//					newsBit.setBody(body);
//					newsBit.setId(String.valueOf(i));
//					currentNewsBits.add(newsBit); 
//				}
//			}
//			
//		}
//		catch (Exception e) {
//			Log.e(TAG, e.getMessage()); 
//		}
//		
//		if (this.context instanceof MessageFeedContollerHolder)
//			 ((MessageFeedContollerHolder)context).setMessageFeedController(this);
//		
//		this.readSome("", true, "Latest updates from Robin's newsroom: "); 
//		
//		//play(currentNewsBits, true, "Latest updates from Robin's newsroom: ");  
//	}
//
//	
//	/** Get XML tag value */
//	 private static String getTagValue(String sTag, Element eElement) {
//		 NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
//		 Node nValue = (Node) nlList.item(0);
//		 return nValue.getNodeValue();
//	 }
//	
//
//	
//	
//}
