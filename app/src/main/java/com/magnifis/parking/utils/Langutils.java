package com.magnifis.parking.utils;

import java.text.Normalizer;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.magnifis.parking.Log;
import com.robinlabs.utils.BaseUtils;

import android.annotation.SuppressLint;
import android.util.Pair;

//import com.magnifis.parking.Log;

import static com.magnifis.parking.utils.Langutils.setize;
import static com.robinlabs.utils.BaseUtils.*;

import compat.org.apache.commons.codec.language.DoubleMetaphone;
import compat.org.apache.commons.codec.language.Soundex;

public class Langutils {
	final static String TAG=Langutils.class.getCanonicalName();
	
	// from: http://stackoverflow.com/questions/1008802/converting-symbols-accent-letters-to-english-alphabet
	@SuppressLint("NewApi") public static CharSequence deAccent(CharSequence str) {
		if (isEmpty(str)) return str;
	    CharSequence nfdNormalizedString =
	    	Utils.isAndroid233orAbove
	    	    ?Normalizer.normalize(str, Normalizer.Form.NFD)
	    	    :str; 
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    return pattern.matcher(nfdNormalizedString).replaceAll("");
	}
		
	final public static String ordinals[]={
		"first", "second", "third", "fourth", "fifth", "sixth", "seventh" 
	};
	
	public static String combineTwoNames(String mainName,String second) {
		second=trim(second);
        if (isEmpty(mainName))
            return second;
		if (!isEmpty(second)) {
			 String nmLc=mainName.toLowerCase(); boolean anyChanges=false;
			 for (String t:second.split("\\W+")) if (!isEmpty(t)) {
			    String tLc=t.toLowerCase();
	            if (!containsTerm(nmLc,tLc)) {
	            	if (!anyChanges) {
	            		mainName+=" ;"; anyChanges=true;
	            	}
	            	mainName+=' '+t; 
	            }
			 }
			 if (anyChanges) mainName=statementCanonicalForm(mainName);
		}
		return mainName;
	}
	
	
	public static String statementCanonicalForm(String s) {
	  return statementCanonicalForm(s,false);
	}
	
	public static String statementCanonicalForm(String s, boolean lc) {
		if (!BaseUtils.isEmpty(s)) {
		   s=s.replaceAll("\\s+", " ").trim();
		   if (lc) s=s.toLowerCase();
		}
		return s;
	}
	
	public static int decodeOrdinal(Collection<String> c) {
	    for (String s:c) {
	      int ix=decodeOrdinal(s);
	      if (ix>=0) return ix;
	    }
		return -1;
	}
	
	public static int decodeOrdinal(String s) {
		if (s.length()==1) {
			  char c=s.charAt(0);
			  if ((c>='1')&&(c<='9')) return c-'1';
			}
		return indexOf(ordinals, s.toLowerCase());
	}
	
	public static Collection<String>  improve_phonetics(Collection<String> c) {
	  Collection<String> cc=BaseUtils.tryToCloneEmpty(c);
	  for (String s:c) cc.add(normalize_phonetics(s));
	  return cc;
	}
	
	public static LinkedHashSet<LinkedHashSet<String>> improve_phonetics(LinkedHashSet<LinkedHashSet<String>> c) {
		if (null == c)
			return c; 
		LinkedHashSet<LinkedHashSet<String>>  cc=BaseUtils.tryToCloneEmpty(c);
	  for (Collection<String> s:c) 
		  if (!isEmpty(s)) {
			  cc.add((LinkedHashSet<String>)improve_phonetics(s));
		  }
	  return cc;		
	}
	
	private final static char phonetic_var_list[]={
		   'b','v',
		   'v','f',
		   'w','v',
		   'w','u',
		   'v','b',
		   'w','v',
		   'f','v',
		   'u','w',
		   'u','o',
	       
		   'a','o',
		   'a','e',
		   'o', 'u',
		   'o', 'a',
		   'e', 'i',
//		   'e', 'a',
		   'y','i',
		   'i','y',
		   'i','e',
		   'z', 's',
		   'z', '!',
		   '!', 'z',
		   
		   /*
		   '\u0009', 'z',
		   '\u0009', 't',
		   '\u0009', 'd',
		   '\u0008', 'i',
		   'i', '\u0008',
		   '\u0008', 'e',
		   'e', '\u0008',
		   */
		   'z', 'd',
		   'd', 'z'
//		   's','z'
		   //'p','f',
		   //'f','p'
//		   '5','k',
//		   '5','h',
		//   '6','h'//,
		 //  'h','6'
		};
	
	private final static char phonetic_var_list_new[]={
		   'b','v',
		   'b','p',
		   'v','f',
		   'v','w',
		   'v','b',
		   'w','v',
		   'w','u',
		   'w','v',
		   'f','v',
		   'f','p',
		   'p','f',
		   'p','b',
		   'u','w',
		   'u','o',
	       
		   'a','o',
		   'a','e',
		   'o', 'u',
		   'o', 'a',
		   'e', 'i',
//		   'e', 'a',
		   'y','i',
		   'i','y',
		   'i','e',
		   'z', 's',
		   'z', '!',
		   '!', 'z',
		   
		   /*
		   '\u0009', 'z',
		   '\u0009', 't',
		   '\u0009', 'd',
		   '\u0008', 'i',
		   'i', '\u0008',
		   '\u0008', 'e',
		   'e', '\u0008',
		   */
		   'z', 'd',
		   'd', 'z'
//		   's','z'
		   //'p','f',
		   //'f','p'
//		   '5','k',
//		   '5','h',
		//   '6','h'//,
		 //  'h','6'
		};
	
	private static char [] findVarSubstitutions(char c) {
		int cnt=0;
		for (int i=0;i<phonetic_var_list.length-1;i+=2) 
			if (phonetic_var_list[i]==c) ++cnt;
		if (cnt>0) {
		  char res[]=new char[cnt];
		  int k=0;
		  for (int i=0;i<phonetic_var_list.length-1;i+=2) 
			  if (phonetic_var_list[i]==c) res[k++]=phonetic_var_list[i+1];
		  return res;
		}
		return null;
	}
	
	private final static String phonetic_1x1_list[]={
		"+", "plus",
		"-", "minus",
		"ph", "f",
		"kn", "n",
		"q", "k",
		"sch","sk",
		"sc", "sk",
		"cl", "kl",
		"cr", "kr",
		"ca", "ka",
		"co", "ko",
		"cu", "ku",
		"ce", "se",
		"ci", "si",
		"ck", "k",
	    "cs", "x",
	    "ks", "x",
        "au", "o",
        "ou", "au",
        "mm", "m",
        "nn", "n",
        "ss", "s",
        "cc", "c",
        "ff", "f",
        "who", "ho",
        "wh", "w",
        "rr","r",
        "or", "Or", // TODO: ????
        "ar", "Ar",
        "th", "!"
      //  "th","\u0009",
      //  "ee","\u0008"
	};

	public static String normalize_phonetics(String s) {
		if (Utils.isEmpty(s))
			return "";
		
		if (s.endsWith("ie"))
			s=s.substring(0,s.length()-2)+"y"; 
		else
			if (s.endsWith("ing")) s=s.substring(0,s.length()-1);
	  
		s=(String) simpleReplaceAll(s,phonetic_1x1_list);	  
	  
		return s;
	}
	
	public static class Likenesses {
	   public double best=0, worst=0, total=0;
	   public boolean exactHit=false;
	   
	   public CharSequence bestMathcher=null;
	   
	   public Likenesses(String bestMatcher,double best, double worst) {
		   this.best=best;
		   this.worst=worst;
		   this.bestMathcher=bestMatcher;
	   }
	   
	   public Likenesses(String bestMatcher,double best, double worst, double total) {
		   this.best=best;
		   this.worst=worst;
		   this.total=total;
		   this.bestMathcher=bestMatcher;
	   }
	   
	   public <T extends Collection<? extends CharSequence>, Y  extends Collection<? extends CharSequence>> Likenesses(T aa,  Y matchers) {
		   if (isEmpty(aa)||isEmpty(matchers)) return;
		   
		   double bestFromWorst=Double.MAX_VALUE;
		   
		   exactHit=true;
		   
	       for (CharSequence m:matchers) {
	    	 double _bestfromWorst=0;
	    	 boolean _exactHit=false;
	    	 
	    	 for (CharSequence a:aa) {
	    		 
	    	   double lk=likeness(m,a);
	    	   
	    	   if (!_exactHit) _exactHit=m.equals(a);
	    	   
	    	   if (lk>_bestfromWorst) _bestfromWorst=lk;
	    	   if (lk>best) {
	    		   best=lk;
	    		   bestMathcher=m;
	    	   }
	 
	    	 }
	    	 
	    	 total+=_bestfromWorst;
	    	 
	    	 exactHit&=_exactHit;
	    	 
	    	 if (_bestfromWorst<bestFromWorst) bestFromWorst=_bestfromWorst;
	       }
	       
	       worst=bestFromWorst;
	       		   
	   }
	};
	
	public static <T extends Collection<String>, Y extends Collection<T>> LinkedHashSet<LinkedHashSet<String>> 
	     exclude(Y mchs, String x, int ...pCounter) {
		if (pCounter!=null) pCounter[0]=0;
		if (!isEmpty(mchs)) {
			LinkedHashSet<LinkedHashSet<String>> rv=null;
			for (T mh:mchs) {
				LinkedHashSet<String> prv=null;
				
				for (String s:mh) if (!s.equals(x)) {
					if (prv==null) prv=new LinkedHashSet<String>();
					if (pCounter!=null) ++pCounter[0];
					prv.add(s);
				}
				
				if (!isEmpty(prv)) {
				  if (rv==null) rv=new LinkedHashSet<LinkedHashSet<String>>();
				  rv.add(prv);
				}
			}
			return rv;
		}
		return null;
	}
	
	public static <T extends Collection<String>, Y extends Collection<T>> double bestPossibleRank(Y mchs) {
		double best=0;
		for (T cs:mchs) {
			Likenesses lk=new Likenesses(cs,cs);
			if (best<lk.total) best=lk.total;
		}
		return best;
	}

	/*
	public static double overallLikeness(Collection<String> aa,  Collection<String> matchers) {
		   if (isEmpty(aa)||isEmpty(matchers)) return 0;
		   double best=0;
	       for (String m:matchers) {
	    	 // all the matchers must fit
	    	 double _best=0;
	    	 for (String a:aa) {
	    	   double lk=likeness(m,a);
	    	   if (lk>_best) _best=lk;
	    	 }
	    	 if (_best<0.01) return 0;
	    	 best+=_best;
	       }
	       return best/(double)matchers.size();
	} 
	*/
	
	// this is a function just like likeness but does not require
	// to generate phonetic variants
	private static double likeness00(CharSequence a, CharSequence b) {
	  return  likeness00(a,b,-1,-1,-1,-1,0);
	}
	
	private static double likeness00(CharSequence a, CharSequence b, int lastFound, int last_b_found, int iStart, int jStart , double prev_inc) {
		   double n=0; 		
		   for (int i=iStart+1;i<a.length();i++) {
			  for (int j=jStart+1;j<b.length();j++) {
				 char ac=a.charAt(i), bc=b.charAt(j);
	             boolean dsub=Character.isUpperCase(ac)||Character.isUpperCase(bc);
	             if (dsub) {
	        //    	 Log.d(TAG,"dsub! "+ac+" "+bc);
	            	ac=Character.toLowerCase(ac);
	            	bc=Character.toLowerCase(bc);
	             }
				 boolean ok=ac==bc, flag=false;
				 double correction=1;
				 
				 if (!(ok||dsub)) {
					char s1[]=findVarSubstitutions(a.charAt(i)); 
					if (!isEmpty(s1)) {
					   if (contains(s1, bc)) {
						   correction=0.9; flag=true;
					   } else {
					     char s2[]=findVarSubstitutions(bc);
					     if (!isEmpty(s2)) {
					    	 if (contains(s2, ac)) {
					    		 correction=0.9; flag=true;
					    	 } else if (intersects(s1,s2)) {
					    		 correction=0.81; flag=true;
					    	 }
					     }
					   }
					}
				 }
				 
				 if (ok||flag) {
				   double nTailWoSub=0;
				   
				   if (flag) {
					 double  nTailWoSub1=likeness00(a,b,lastFound,last_b_found,i-1,j,prev_inc); // skip j only
					 double  nTailWoSub2=likeness00(a,b,lastFound,last_b_found,i,last_b_found,prev_inc); // skip i
					 nTailWoSub=Math.max(nTailWoSub1, nTailWoSub2);
				   }
					
				   double inc=0;
				   if ((i==0)&&(j==0)) inc=flag?1:1.8;
				   else {
					 boolean lastChar=(i==a.length()-1)&&(j==b.length()-1);
					 if ((i==lastFound+1)&&(j==last_b_found+1)) { // seq
						inc=prev_inc+(flag?0.3:1);
						//if (lastChar&&!flag) inc+=0.3;
					 } else 
				    	//  if (lastChar) inc=flag?1:1.3; // std:1.1
				    	//  else
					          inc=1;
				   }
				   
				   inc*=correction;
				   
				   lastFound=i;
				   jStart=last_b_found=j;
				   prev_inc=inc;
				   
				   if (flag) {
				      double nTailWithSub=likeness00(a,b,lastFound,last_b_found,lastFound,last_b_found,prev_inc);
				      return n+Math.max(nTailWithSub+inc,nTailWoSub);
				   }
				   
				   n+=inc;
				   break;
		
				 } else
				   prev_inc=0;
			  }
		   }
		   return n;
	}	
	
	
	public static double likeness(CharSequence a, CharSequence b) {
		   double l1=likeness00(a,b), l2=likeness00(b,a);
		   return (l1+l2)/(double)(a.length()+b.length());
	}

	
	public static double likeness02(CharSequence a, CharSequence b) {
		return likeness00(a,b)+ likeness00(b,a);
	}
	
	private static double selfLikeness00[]=null;
	
	public static void init() {
		synchronized(Langutils.class) {
		  if (selfLikeness00==null) {
			  selfLikeness00=new double[10];
			  StringBuilder sb=new StringBuilder();
			  for (int i=0;i<selfLikeness00.length;i++) {
			    sb.append('a');
			    selfLikeness00[i]=likeness00(sb,sb);
			  }
		  }
		}
	}
	
	public static double likeness00(CharSequence a) {
		if (isEmpty(a)) return 0;
		init();
		if (a.length()<selfLikeness00.length) return selfLikeness00[a.length()-1];
		return likeness00(a,a);
	}
	
	public static double normalized_likeness(CharSequence a, CharSequence b) {
		  double x=likeness02(a,b), 
				 aa=likeness00(a), bb=likeness00(b);
				  
	      return x/(aa+bb);
    }
	
	public static double distance(CharSequence a, CharSequence b) {
	   double x=likeness02(a,b), aa=likeness00(a), bb=likeness00(b);
	   return (aa+bb-x)/(a.length()+b.length());
	}
	
	public static LinkedHashSet<LinkedHashSet<String>> toSingleWord(Collection<String> c) {
		if (!isEmpty(c)) {
		  LinkedHashSet<LinkedHashSet<String>> res=new LinkedHashSet<LinkedHashSet<String>>();
		  for (String s:c) if (!isEmpty(s)) res.add(toSet(toSingleWord(s)));
		  return res;
		}
		return null;
	}
	
	public static LinkedHashSet<LinkedHashSet<String>> toSingleWord(String c[]) {
		if (!isEmpty(c)) {
		  LinkedHashSet<LinkedHashSet<String>> res=new LinkedHashSet<LinkedHashSet<String>>();
		  for (String s:c) if (!isEmpty(s)) res.add(toSet(toSingleWord(s)));
		  return res;
		}
		return null;
	}
	
	public static String toSingleWord(String s) {
		return isEmpty(s)?s:s.replaceAll("\\W+", "");
	}
	
	public static LinkedHashSet<String> setize(String s) {
		if (!isEmpty(s)) {		
		  LinkedHashSet<String> res=new LinkedHashSet<String>();
		  String ar[]=s.trim().split(
				  "\\W+"
				 // "[^\\p{Alpha}]+"
				  );
		  
		  for (String t:ar)
			  if (t.length()>0) res.add(t.toLowerCase());
		  return res;
		}
		return null;
	}
	
	public static LinkedHashSet<LinkedHashSet<String>> setize(Collection<String> matches) {
		if (!isEmpty(matches)) {
		  LinkedHashSet<LinkedHashSet<String>> res=new LinkedHashSet<LinkedHashSet<String>> ();
		  for (String s:matches) {
			  LinkedHashSet<String> ss=setize(s);
			  res.add(ss);
			  /*
			  if (ss.size()>1) {
				  LinkedHashSet<String> fat_ass=new LinkedHashSet<String>();
				  fat_ass.add(toSingleWord(s));
				  res.add(fat_ass);
			  }*/
		  }
		  return res;
		}
		return null;
	}
	
	public static LinkedHashSet<LinkedHashSet<String>> setize(String matches[]) {
		if (!isEmpty(matches)) {
		  LinkedHashSet<LinkedHashSet<String>> res=new LinkedHashSet<LinkedHashSet<String>> ();
		  for (String s:matches) res.add(setize(s));
		  return res;
		}
		return null;
	}	
	
	public static LinkedHashSet<AbstractSet<String>> prepareForRanking(String nms[],Translit translit) {
	  if (isEmpty(nms)) return null; 
	  LinkedHashSet<AbstractSet<String>> res=new LinkedHashSet<AbstractSet<String>> ();
	  for (String nm:nms) res.add(prepareForRanking(nm,translit));
	  return res;
	}
	
	public static AbstractSet<String> prepareForRanking(String nm,Translit translit) {
       String nmn = Langutils.normalize_phonetics(
			translit.process(nm.toLowerCase()).toString()); 
	   return setize(nmn);
	}
	

}
