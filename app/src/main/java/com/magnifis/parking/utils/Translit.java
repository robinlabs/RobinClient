package com.magnifis.parking.utils;

public class Translit {
	
	private final static String
	alef="\u05D0",
	beth="\u05D1",
	gimel="\u05D2",
	daleth="\u05D3",
	hey="\u05D4",
	waw="\u05D5",
	zayn="\u05D6",
	heth= "\u05D7",
	teth= "\u05D8",
	yud="\u05D9",
	kaf="\u05DB",
	kaf_sofit="\u05Da",
	lamed="\u05DC",
	mem="\u05De",
	mem_sofit="\u05Dd",

	nun= "\u05e0",
	nun_sofit= "\u05df",

	samekh= "\u05E1",

	ayn="\u05E2",
	pey= "\u05E4",
	pey_sofit= "\u05E3",

	tsadi="\u05E6",
	tsadi_sofit="\u05E5",

	quf="\u05E7",
	reysh="\u05E8",
	shin="\u05E9",
	tab="\u05EA"
;
	
	final private static String table_hebrus[]={
		"а", "a",	"б", "b",	"в", "v", 	"г", "g", 	"д", "d", 	"е", "e",	"ё", "yo",
		"ж", "zh",	"з", "z",	"и", "i",	"й", "y",	"к", "k",	"л", "l",   "м", "m",
		"н", "n",	"о", "o",	"п", "p",	"р", "r",	"с", "s", 	"т",  "t",	"у", "u",
		"ф", "f",	"х", "kh",	"ц", "ts",	"ч", "ch",	"ш", "sh",	"щ", "sch",	"ъ", "",
		"ы", "i",	"ь", "",	"э", "e", 	"ю", "yu", 	"я", "ya",  "ё", "yo",

		alef, "e",
		beth, "b",
		gimel, "g",
		daleth, "d",
		hey,  "h",
		waw,   "w",
		zayn,  "z",
		heth,  "h",
		teth,  "t",
		yud,   "y",
		kaf,   "k",
		kaf_sofit, "k",
		lamed, "l",
		mem, "m",
		mem_sofit, "m",

		nun, "n",
		nun_sofit, "n",

		samekh, "s",

		ayn, "a",
		pey, "p",
		pey_sofit, "p",

		tsadi, "ts",
		tsadi_sofit, "ts",

		quf, "q",
		reysh, "r",
		shin, "sh",
		tab, "t"
	};
	
	private Translit() {}
	
	private String table[]=null;
	
	public static Translit getHebRus() {
		Translit t=new Translit();
		t.table=table_hebrus;
		return t;
	}
	
	public static Translit getHeb() {
		Translit t=new Translit();
		t.table=table_heb;
		return t;
	}
	
	
	final private static String table_heb[]={
		alef, "e",
		beth, "b",
		gimel, "g",
		daleth, "d",
		hey,  "h",
		waw,   "w",
		zayn,  "z",
		heth,  "h",
		teth,  "t",
		yud,   "y",
		kaf,   "k",
		kaf_sofit, "k",
		lamed, "l",
		mem, "m",
		mem_sofit, "m",

		nun, "n",
		nun_sofit, "n",

		samekh, "s",

		ayn, "a",
		pey, "p",
		pey_sofit, "p",

		tsadi, "ts",
		tsadi_sofit, "ts",

		quf, "q",
		reysh, "r",
		shin, "sh",
		tab, "t"
	};
	
 
    public  String process(char c) {
    	boolean uc=Character.isUpperCase(c);
    	for (int i=0;i<table.length;i+=2)
    	  if (table[i].charAt(0)==Character.toLowerCase(c)) {
    		  String r=table[i+1];
    		  if (uc) r=r.toUpperCase();
    		  return r;
    	  }
    	return String.valueOf(c);
    }
    
    public  CharSequence process(CharSequence s) {
    	if (!Utils.isEmpty(s)) {
    	   StringBuilder sb=new StringBuilder();  
    	   for (int i=0;i<s.length();i++) sb.append(process(s.charAt(i)));
    	   return sb;
    	}
    	return s;
    }

    public  String process(String s) {
    	if (!Utils.isEmpty(s)) {
     	   StringBuilder sb=new StringBuilder();  
     	   for (int i=0;i<s.length();i++) sb.append(process(s.charAt(i)));
     	   return sb.toString();
    	}
    	return s;
    }

}
