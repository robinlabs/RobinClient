package com.magnifis.parking.utils.nlp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.magnifis.parking.utils.Utils;


// @TODO: extract this class into a common library across the backend 

public class StopWordsPredicate {
	
	//@see http://www.textfixer.com/resources/common-english-words.php
	static String[] stopWordsConservative = 
		{"robin", "please", 
		"\'tis","\'twas","a","ain\'t","all",
		"am","an","and","any","are","aren\'t","as","at","be","been","but","by",
		"can","can\'t","cannot","could","could\'ve","couldn\'t","did","didn\'t","do","does",
		"doesn\'t","don\'t","either","else","for","from","get","got","had","has",
		"hasn\'t","have","he","he\'d","he\'ll","he\'s","her","hers","him","his","how","how\'d",
		"how\'ll","how\'s","however","i","i\'d","i\'ll","i\'m","i\'ve","if","in","into","is",
		"isn\'t","it","it\'s","its","just","at least","let","like","likely","may","me","might",
		"might\'ve","mightn\'t","most","must","must\'ve","mustn\'t","my","neither","no","nor",
		"not","of","off","often","on","only","or","our",
		"shan\'t","she","she\'d","she\'ll","she\'s","should","should\'ve","shouldn\'t",
		"since","so","than","that","that\'ll","that\'s","the","their","them","then",
		"there","there\'s","these","they","they\'d","they\'ll","they\'re","they\'ve","this",
		"tis","to","too","twas","us","wants","was","wasn\'t","we","we\'d","we\'ll","we\'re",
		"were","weren\'t","what","what\'d","what\'s","when","when","when\'d","when\'ll","when\'s",
		"where\'d","where\'ll","where\'s","which","while","who","who\'d","who\'ll"
		,"who\'s","whom","why","why\'d","why\'ll","why\'s","will","with","won\'t","would",
		"would\'ve","wouldn\'t","yet","you","you\'d","you\'ll","you\'re","you\'ve", "your"};
	
	
	Set<String> wordSet; 
	Stemmer stemmer = new Stemmer();
	static StopWordsPredicate instance = new StopWordsPredicate(); 
	
	StopWordsPredicate() {
		wordSet = new HashSet<String>(Arrays.asList(stopWordsConservative)); 
	}
	
	static public StopWordsPredicate getInstance() {
		return instance; 
	}
	
	public boolean isStopWord(String word) {
		return wordSet.contains(word.trim().toLowerCase()); 
	}

	public String dropStopWords(String phrase) {
		
		String[] words = phrase.trim().toLowerCase().split("[\\s]"); 
		
		if (Utils.isEmpty(words)) 
			return phrase; 
		
		StringBuffer strBuf = new StringBuffer(phrase.length()); 
		for (String word : words) {
			if (!isStopWord(word)) {
				stemmer.add(word.toCharArray(), word.length()); 
				stemmer.stem(true);// aggressive
				
				String stemmed = stemmer.toString();  
				strBuf.append(stemmed).append(' '); 
			}
		}
		
		return strBuf.toString().trim(); 
	}
}
