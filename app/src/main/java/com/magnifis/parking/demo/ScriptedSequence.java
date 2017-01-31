package com.magnifis.parking.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ScriptedSequence {
	
	public class Outcome {
		public String firstResponse = null; 
		public String mgQuery = null; 
		
		public Outcome(String startSaying, String sendQuery) {
			firstResponse = startSaying; 
			mgQuery = sendQuery; 
		}
	}
	
	ArrayList<Outcome> outcomes = new ArrayList<Outcome>();
	int iCur = 0; 
	
	public void add(String startSaying, String sendQuery) {
		outcomes.add(new Outcome(startSaying, sendQuery)); 
	}
	
	public Outcome getNext() {
		if (outcomes.size() < 1)
			return null; 
		
		Outcome next = outcomes.get(iCur); 
		iCur =(++iCur) % outcomes.size() ; 
		
		return next; 
	}
	
	
	void reset() {
		iCur = 0;  
	}

	public static ScriptedSequence newScriptedSequence() {
		ScriptedSequence scriptedSeq = new ScriptedSequence(); 
		scriptedSeq.add("Looks like you have a choice of two routes: the fastest one is via Franklin Avenue... Would you like directions?", 
						""); 		
		scriptedSeq.add("Well, let me look up bike rentals nearby...", "Find bike rentals nearby"); 
		scriptedSeq.add("You should be there in 57 minutes. Hopefully...", ""); 
		scriptedSeq.add("Well, let me see what I can do...", ""); 
		scriptedSeq.add("You are about to reach you destination. Share your location perhaps?", ""); 
		scriptedSeq.add("Got it, let me repeat that...", ""); 
		
		return scriptedSeq; 
	}
	
	public static ScriptedSequence newScriptedSequence2() {
		ScriptedSequence scriptedSeq = new ScriptedSequence(); 
		scriptedSeq.add("", "Find french food in San Jose"); 	
		scriptedSeq.add("", "How about Thai"); 
		scriptedSeq.add("", "what else");
		scriptedSeq.add("", "does it have good reviews");
		scriptedSeq.add("", "is there parking there");
		scriptedSeq.add("", "go there");
		scriptedSeq.add("", "read my facebook feed");
		scriptedSeq.add("", "post to facebook");
		scriptedSeq.add("", "Thai food anyone");
		scriptedSeq.add("", "Yes");
	
		
		return scriptedSeq; 
	}
	
	public static ScriptedSequence newScriptedSequence3() {
		ScriptedSequence scriptedSeq = new ScriptedSequence(); 
		scriptedSeq.add("", "moviebot Any good comedies to watch"); 	
		scriptedSeq.add("", "moviebot Preferably sophisticated"); 
		scriptedSeq.add("", "moviebot Who is in it");
		scriptedSeq.add("", "moviebot Any other oscar contenders");
		scriptedSeq.add("", "moviebot Michael Keaton wasn't he in Batman");
		scriptedSeq.add("", "moviebot classic I'd love to see it");
		
		return scriptedSeq; 
	}
	

}

