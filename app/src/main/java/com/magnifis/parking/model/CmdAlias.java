package com.magnifis.parking.model;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.utils.nlp.StopWordsPredicate;

public class CmdAlias implements Serializable {
 
   @ML(attr="alias")
   protected String alias=null;  
   @ML(attr="command")
   protected String command=null;
   @ML(attr="userConfirmation")
   protected String userConfirmation=null;

   protected String canonicalKey=null;  
   
   
   public String getAlias() {
	   return alias;
   }
   
   public String getKey() {
	   return canonicalKey;
   }
   
   public void setAlias(String alias) {
	   this.alias = alias;
	   this.canonicalKey = StopWordsPredicate.getInstance().dropStopWords(alias); 
   }
   
   public String getCommand() {
	   return command;
   }
   
   public void setCommand(String command) {
	   this.command = command;
   }
   
   
   public String getUserConfirmation() {
	   return userConfirmation;
   }

   public void setUserConfirmation(String userConfirmation) {
	   this.userConfirmation = userConfirmation;
   }

}
