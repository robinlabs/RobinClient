/**
 * 
 */
package com.magnifis.parking;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.os.AsyncTask;

public class XMLFetcher<T> extends Fetcher<T> {
	
	@Override
	protected T consumeInputStream(InputStream is)  throws IOException {
		if (is!=null) {
			ByteArrayOutputStream bais=new ByteArrayOutputStream();
			IOException xx=null;
			
			stream_reading:for (;;) {
			   int k=0;
			   for (int i=0;i<N_READ_ATTEMPS;i++) try {
				   xx=null;
				   if (i>0) try {
					   Thread.sleep(100);
				   } catch(InterruptedException ix) {
					   return null;
				   }
			     k=is.read();
			     if (k<0) break stream_reading;
			     break;
			   } catch(IOException x) {
				 xx=x;
			   }
			   if (xx!=null) {
				   exactNcePlace("XMLFetcher#1",xx);
				   throw xx;
			   }
			   bais.write(k);
			}
			
			
			Document doc=Xml.loadXmlFile(new ByteArrayInputStream(bais.toByteArray()));
			if (doc!=null) return consumeXmlData(doc.getDocumentElement());
			   exactNcePlace("XMLFetcher#2");
		}
		exactNcePlace("XMLFetcher#3");
		return null;
	}
	
	protected T consumeXmlData(Element root) {
	   return (T)root;
	}
}