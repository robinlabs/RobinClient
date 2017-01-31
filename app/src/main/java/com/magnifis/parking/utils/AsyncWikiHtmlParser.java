package com.magnifis.parking.utils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.content.Context;
import android.os.AsyncTask;

import com.magnifis.parking.MultiAsyncTask;
import com.magnifis.parking.Output;

public class AsyncWikiHtmlParser extends MultiAsyncTask {

	private static final int MAX_SNIPPET_LENGTH = 512; 
	
	String theUrl; 
	Context theContext; 
	
	public AsyncWikiHtmlParser(Context context, String url) {
		super(); 
		theUrl = url; 
		theContext = context; 
	}

	@Override
	protected String doInBackground(Object[] params) {

		Connection conn = Jsoup.connect(theUrl);
		Document dom = null;

		try {
			Elements elems = null;
			if (conn != null)
				dom = conn.get();
			if (dom != null)
				elems = dom.select("div.content div p");

			if (!Utils.isEmpty(elems)) {
				String text = elems.get(0).text();
				String summary = null;

				if (text != null) {
					text = text.substring(0, MAX_SNIPPET_LENGTH); 
					while (!Utils.isEmpty(text)) { // clean text up
						// remove all brackets (with contents)
						summary = text.replaceAll("^(.*?)(\\[.*?\\]|\\(.*?\\))+(.*?)", "$1$3"); 
						if (summary.equals(text))
							break;
						text = summary;
					}
					
					// extract just the first sentence
					Pattern pattern = Pattern.compile("^(.*?\\.)[ ]+[A-Z]");
					Matcher matcher = pattern.matcher(text);
					if (matcher.find()) 
						summary = matcher.group(1);

					if (!Utils.isEmpty(summary)) 
						Output.sayAndShow(theContext, summary);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
