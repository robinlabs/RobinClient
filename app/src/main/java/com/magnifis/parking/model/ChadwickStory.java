package com.magnifis.parking.model;

import com.magnifis.parking.Xml.ML;

public class ChadwickStory {
	
	 @ML("score")
	 protected String score=null; // String of format "XX:YY"

	 @ML("text")
	 protected String text=null; // story text 
	 
	 public String getText() {
		 return text; 
	 } 


}
