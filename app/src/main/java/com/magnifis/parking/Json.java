package com.magnifis.parking;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import compat.org.json.JSONArray;
import compat.org.json.JSONException;
import compat.org.json.JSONObject;

public class Json {
	static private final String TAG="Json";
    public static Element convertToDom(JSONObject jso) {
    	try {
		  DocumentBuilder builder=DocumentBuilderFactory.newInstance().newDocumentBuilder();
		  Document doc=builder.newDocument();
		  Element root=doc.createElement("jsonRoot");
		  doc.appendChild(root);
		  convertToDom(jso,root);
		  return root;
		} catch (Exception e) {
           e.printStackTrace();
		}
    	return null;
    }  
    
    public static void convertToDom(JSONArray jsa, Element parent,String tag) {
    	Document doc=parent.getOwnerDocument();
    	for (int i=0;i<jsa.length();i++) {
       	   Element el=doc.createElement(tag);
       	   parent.appendChild(el);   		
       	   JSONArray sa=jsa.optJSONArray(i);
       	   if (sa!=null) {
       		   // handle sub-array
       		  convertToDom(sa,el,Integer.toString(i)); // should look ugly, but nothing to do
       	   } else {
       		 JSONObject ob=jsa.optJSONObject(i);
    		 if (ob==null) {
    			String s=jsa.optString(i);
    			if (s!=null) {
    				el.appendChild(doc.createTextNode(s));
    			}
    		 } else {
    			 convertToDom(ob,el);
    		 }       		 
       		 
       	   }
    	}
    }
    
    public static void convertToDom(JSONObject jso, Element parent) {
    	Document doc=parent.getOwnerDocument();
    	Iterator it=jso.keys();
    	while (it.hasNext()) {
    	  String key=(String)it.next();
    	  JSONArray jsa=jso.optJSONArray(key);
    	  if (jsa!=null) {
    		  convertToDom(jsa,parent,key);
    	  } else {
        	 Element el=doc.createElement(key);
        	 parent.appendChild(el);
    		 JSONObject ob=jso.optJSONObject(key);
    		 if (ob==null) {
    			String s=jso.optString(key);
    			if (s!=null) {
    				el.appendChild(doc.createTextNode(s));
    			}
    		 } else {
    			 convertToDom(ob,el);
    		 }
    	  }
    	  //Log.d(TAG+"key: ", key.toString());
    	}
    }  
}
