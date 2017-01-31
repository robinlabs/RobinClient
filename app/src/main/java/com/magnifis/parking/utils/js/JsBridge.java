package com.magnifis.parking.utils.js;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;

public class JsBridge {
	
	Context mContext;
	WebView theWebView; 
	
	static final Pattern jsFunctionPattern = Pattern.compile("(?i)function\\s+([A-Z_][0-9A-Z_]*)\\(([A-Z_][0-9A-Z_]*[[ ]*,[ ]*[A-Z_][0-9A-Z_]*]*)\\).*", 
			Pattern.DOTALL);  

    /** Instantiate the interface and set the packageName */
	public JsBridge(Context c, WebView hostView) {
        mContext = c;
        theWebView = hostView; 
    }

  
    public void toggleWebView(boolean on) {
    	if (theWebView != null) {
    		if (on)
    			theWebView.setVisibility(View.VISIBLE); 
    		else
    			theWebView.setVisibility(View.GONE); 	
    	}	
    }
    
    public void showWebView() {
    	if (theWebView != null) {
    		theWebView.setVisibility(View.VISIBLE); 
    	}
    }
    
    
    /**
     * Given a JS function code string and a (partial) list of parameter name/value pairs, 
     * returns a list of parameter values in the right orde, in order to call the function
     * Example: 
     *  
     * @param jsFuncCode - function code in the form of "...function foo(param_0, param_1, param_2, ..., param_n)"
     * @param givenParamsNVP - a possibly partial list of parameter name/value pairs, and possibly out of order, 
     * 				e.g.  {param_2 : v2, param_3 : v3, param_0 : v0} 
     * @return string-encoded comma-separated list of parameter values in the right order, e.g., (v0, "", v2, v3).
     * Note that missing parameter values are assigned empty strings
     */
    public static String getFunctionParamValues2Substitute(String jsFuncCode, Map<String,String> givenParamsNVP) {
    	if (null == givenParamsNVP || givenParamsNVP.isEmpty())
    		return ""; 
    	
    	StringBuffer paramValuesBuf  = new StringBuffer(givenParamsNVP.size()*16); 
    	String jsCodeOneLine = jsFuncCode.replaceAll("\n", " ");
    	jsCodeOneLine = jsCodeOneLine.replaceAll("\t", " ");
    	String paramSequence = jsFunctionPattern.matcher(jsCodeOneLine).replaceAll("$2");
    	StringTokenizer strTok = new StringTokenizer(paramSequence, ","); 
    	int i = 0; 
    	while ( strTok.hasMoreElements()) {
    		String token = strTok.nextToken();
    		String paramName = token.trim(); 
    		String paramVal = givenParamsNVP.get(paramName); 
    
    		if (null == paramVal)
    			paramVal = ""; 
    
    		if (i++ > 0) {
    			paramValuesBuf.append(","); 
    		}
    		paramValuesBuf.append("'").append(paramVal).append("'"); // wrap in q-marks 
    	}
    	
    	return paramValuesBuf.toString(); 
    }
}
