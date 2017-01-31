package com.magnifis.parking.phonebook;

import static com.magnifis.parking.utils.Langutils.setize;
import static com.magnifis.parking.utils.Utils.dump;
import static com.magnifis.parking.utils.Utils.isEmpty;
import static com.robinlabs.utils.BaseUtils.dump;
import static com.robinlabs.utils.BaseUtils.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.telephony.PhoneNumberUtils;
import android.util.Pair;

import com.magnifis.parking.Config;
import com.magnifis.parking.Log;
import com.magnifis.parking.model.ContactRecord;
import com.magnifis.parking.model.ContactRecord;
import com.magnifis.parking.model.ContactRecordBase;
import com.magnifis.parking.utils.Langutils;
import com.magnifis.parking.utils.Langutils.Likenesses;
import com.magnifis.parking.utils.Translit;
import com.magnifis.parking.utils.Utils;
import com.robinlabs.utils.BaseUtils;

public class NeoPhonebookMatcher implements IPhonebookMatcher {
	final static String TAG= NeoPhonebookMatcher.class.getSimpleName();
	
	
	
	@Override
	public List<ContactRecord> getMatches(
			String[] queryCandidates,
			Collection<ContactRecord> phones, 
			boolean uncond,
			Set<Integer> onlyType
	) {
		return getMatches(
				queryCandidates,
				phones, 
				onlyType,
				true,
				MAX_SET_SIZE
		);
	}
	
	public SearchResult getCandidates(
			String[] queryCandidates,
			Collection<ContactRecord> phones, 
			Set<Integer> onlyType
	) {
		return getCandidates(
				queryCandidates,
				phones, 
				onlyType,
				true,
				MAX_SET_SIZE
		);
	}
	
	private List<Candidate> getByPhoneType(
		Collection<ContactRecord> phones, 
		Set<Integer> onlyType
	) {
		List<Candidate> res=new ArrayList <Candidate>();
		for (ContactRecord p:phones) 
		  if (!isEmpty(p.getPhone())&&isEmpty(onlyType)|| BaseUtils.anyIntersection(onlyType,p.getTypes()) ) {
			 res.add(new Candidate(p,null,false,1)); 
		  }
		return res;
	}
	
	
	public List<ContactRecord> getMatches(
			String[] queryCandidates,
			Collection<ContactRecord> phones, 
			Set<Integer> onlyType,
			boolean withPhoneOnly,
			int maxSetSize
	) {

	    return getCandidates(
	    		queryCandidates,
				phones, 
				onlyType,
				withPhoneOnly,
				maxSetSize
		).getContacts();
	}
	
	
	private Translit translit=Translit.getHebRus();
	
	public SearchResult getCandidates(
			String[] queryCandidates,
			Collection<ContactRecord> phones, 
			Set<Integer> onlyType,
			boolean withPhoneOnly,
			int maxSetSize
	) {
    	Collection<String> mat=new LinkedHashSet<String>();
    	for (String s:queryCandidates) {
    	   s=s.trim().toLowerCase();
    	   if (!isEmpty(s.trim())) mat.add(translit.process(s).toString());
    	}
	    return 	getCandidates(
	    		mat,
				phones, 
				onlyType,
				withPhoneOnly,
				maxSetSize
		);
	}
	
	public List<ContactRecord> getMatches(
			Collection<String> mat,
			Collection<ContactRecord> phones, 
			Set<Integer> onlyType,
			boolean withPhoneOnly,
			int maxSetSize
	) {
		//dump("pa",Langutils.phonetic_variants(setize(mat)));
		return getCandidates(
				mat,
				phones, 
				onlyType,
				withPhoneOnly,
				maxSetSize).getContacts();
	}
	
	public SearchResult getCandidates(
			Collection<String> mat,
			Collection<ContactRecord> phones, 
			Set<Integer> onlyType,
			boolean withPhoneOnly,
			int maxSetSize
	) {
		//dump("pa",Langutils.phonetic_variants(setize(mat)));
		return getCandidates(
				Langutils.improve_phonetics(setize(mat)),
				phones, 
				onlyType,
				withPhoneOnly,
				maxSetSize);
	}	
	
	public List<ContactRecord> getMatches(
			LinkedHashSet<LinkedHashSet<String>> mchs,
			Collection<ContactRecord> phones, 
			Set<Integer> onlyType,
			boolean withPhoneOnly,
			int maxSetSize			
	) {
		return getCandidates(
				mchs,
				phones, 
				onlyType,
				withPhoneOnly,
				maxSetSize
		).getContacts();
	}
	
	public SearchResult getCandidates(
			LinkedHashSet<LinkedHashSet<String>> mchs,
			Collection<ContactRecord> phones, 
			Set<Integer> onlyType,
			boolean withPhoneOnly,
			int maxSetSize			
	) {
		return getCandidates(
				mchs,
				phones, 
				onlyType,
				withPhoneOnly,
				maxSetSize,
				STD_BEST, STD_WORST,
				true
		);
	}
	
	public String [] findRelevantMatches(
	   String matches[],
	   ContactRecordBase r
	) {
		ArrayList<MatchCandidate> tab=new  ArrayList<MatchCandidate>();
		Collection<String> nc=setize(
 	           Langutils.normalize_phonetics(
      	             translit.process(r.getName().toString().toLowerCase()).toString()
      	           )
      	        );
		
		for (String nm:matches) {
			Collection<String>  
			        mc=setize(
        			    Langutils.normalize_phonetics(
        				  translit.process(nm.toLowerCase()).toString())
        			);
	        Likenesses lns=new Likenesses(nc, mc);
	        tab.add(
	           new MatchCandidate(
	        		   nm, r, lns.total
	           )
	        );			
		}
        Collections.sort(tab);
        int lastIndex=tab.size()-1;
        while (lastIndex>0&&tab.get(0).rank>=tab.get(lastIndex).rank*1.2) --lastIndex;
        String res[]=new String[lastIndex+1];
        for (int i=0;i<=lastIndex;i++) res[i]=tab.get(i).getMatch().toLowerCase();
		return res;
	}
	
	
	private List<Candidate> getRawMatches(
			LinkedHashSet<LinkedHashSet<String>> mchs,
			Collection<ContactRecord> phones, 
			Set<Integer> onlyType,
			boolean withPhoneOnly,
			double bestTrashold,
			double worstTrashold
	) {
		List<Candidate> rank=new ArrayList<Candidate>();
		double best = 0.;
		l_records:for (ContactRecord r : phones) if (
				(isEmpty(onlyType)||BaseUtils.anyIntersection(onlyType,r.getTypes()))&&
				(!(withPhoneOnly&&isEmpty(r.getPhone())))
				) {
			
			/////////////////////////////////////////////////
			

           /*
			    String nm=r.getName().toString();
			    
			    
			    String nmn = Langutils.normalize_phonetics(
    					translit.process(nm.toLowerCase()).toString()); 


            	LinkedHashSet<String> rs = setize(nmn);
      */
            	
            	Candidate found=null;
            	
            	double factor=1.2; // max factor
            	
            	for (Collection<String> mch : mchs) 
            		if (!isEmpty(mch)) {
            	//		Log.d(TAG,"encn="+r.getEncodedNames());
            			Langutils.Likenesses lkss=new  Langutils.Likenesses(/*rs*/r.getEncodedNames(),mch);
            			
    		//			Log.d(TAG, "XXX " + lkss.worst + ": " + dump(rs)
    		//					+ " " + dump(mch));
            			
            			if (lkss.exactHit||(lkss.worst > worstTrashold) || (lkss.total>=worstTrashold+best)) {
            				Candidate cnd=new Candidate(r,lkss.bestMathcher, lkss.exactHit, lkss.total*factor);
               				
            				if (found==null||found.finalRank<cnd.finalRank) {
            					//r.setName(nm);
            				   found=cnd;
            				} else
            				   found.exactHit|=lkss.exactHit;
            			}
            			if (factor>1) factor-=0.1;
            		}
            	
            	if (found!=null) {
            		if (rank.size()==0) rank.add(found); else {
            		   int ix=Collections.binarySearch(rank, found, candidateComparator);
            		   if (ix<0) {
            			 rank.add(-(ix+1), found);
            		   } else
            			 rank.add(ix,found);
            		}
            		if (rank.size()>18) rank.remove(18); // we don't want too many candidates
            	}
			
            
			/////////////////////////////////////////////////
		}
		return rank;
	}
	
	private static Comparator<Candidate> candidateComparator=
			 new Comparator<Candidate>() {

				@Override
				public int compare(Candidate lhs,Candidate rhs) {
					double d=lhs.getFinalRank()-rhs.getFinalRank();
					if (d==0) {
					  return lhs.contact.toString().compareTo(rhs.contact.toString());
					}
					return d<0?1:-1;
				}
			 };
	
	// order descending by final rank
	private static void orderCandidates(List<Candidate> lst) {
		  java.util.Collections.sort(lst,candidateComparator);		
	}
	
    public static List<Candidate> findRawVIPsOrExactHits(List<Candidate> lst) {
      if (!isEmpty(lst)) {
    	  List<Candidate> vips=new ArrayList<Candidate>();
    	  for (Candidate c:lst) if (c.isVipOrExactHit()) vips.add(c);
     	  return vips;
      }
      return null;	
    }
    
    public static List<Candidate> findExactHits(List<Candidate> lst) {
        if (!isEmpty(lst)) {
      	  List<Candidate> vips=new ArrayList<Candidate>();
      	  for (Candidate c:lst) if (c.exactHit) vips.add(c);
       	  return vips;
        }
        return null;	
      }
    
    public static List<Candidate> findRelevantVIPs(List<Candidate> lst, int N) {
    	List<Candidate> rawVips=findRawVIPsOrExactHits(lst);
    	if (rawVips==null||rawVips.size()<=N) return rawVips;
    	Log.d(TAG, "order VIPs by person ID");
    	LinkedHashMap<Long,List<Candidate>> vipmap=new LinkedHashMap<Long,List<Candidate>>();
    	for (Candidate c:rawVips) {
    		List<Candidate> lc=	vipmap.get(c.contact.getRawContactId());
    		if (lc==null) {
    			lc=new ArrayList<Candidate>();
    			vipmap.put(c.contact.getRawContactId(), lc);
    		}
    		lc.add(c);
    	}
	    if (vipmap.size()<rawVips.size()) {
	      Long idx[]=new Long[vipmap.size()];
	      int nRemoved=0, wantToRemove=vipmap.size()-N;
	      vipmap.keySet().toArray(idx);
	      //first path -- skip same persons
	      for (int i=vipmap.size();(wantToRemove>nRemoved)&&--i>=0;) {
	    	 long ix=idx[i];
	    	 List<Candidate> lc=vipmap.get(ix);
	    	 if (lc.size()>1) {
	    		Collections.sort(
	    			lc, 
	    			new Comparator<Candidate>() {
						@Override
						public int compare(Candidate lhs, Candidate rhs) {	
							if (lhs.exactHit!=rhs.exactHit) return lhs.exactHit?-1:1;
							double d=lhs.contact.getImportance()-rhs.contact.getImportance();
							if (d<0) return 1;
							if (d>0) return -1;
							return 0;
						}	
	    			}
	    		);
	    		int toRemove=wantToRemove-nRemoved;
	    		if (toRemove>0) {
	    		  if (toRemove>=lc.size()) toRemove=lc.size()-1;
	    		  while (toRemove-->0) { lc.remove(lc.size()-1); ++nRemoved;}
	    		}
	    		if (nRemoved==wantToRemove) break;
	    	 }
	      }
	      // second path -- truncate the list if it needs
	      List<Candidate> res=new ArrayList<Candidate>(N);
	      int i=0;
	      lp:for (List<Candidate> l0:vipmap.values()) for (Candidate c:l0) {
	    	 res.add(c);
	    	 if (++i>=N) break lp;	  
	      } 
	      return res;
	    } else
	      rawVips=rawVips.subList(0, N);
	    if (!isEmpty(rawVips)) Log.d(TAG, BaseUtils.dump(rawVips).toString());
    	return rawVips;
    }
    
    public <T extends Collection<String> > String findMostClose(T names, String matchers[]) {
    	return findMostClose(names,Langutils.prepareForRanking(matchers,translit));
    }
    
    public <T extends Collection<String>, T1 extends Collection<String>, Y extends Collection<T1> > String findMostClose(T names, Y mchs) {
    	String bestName=null;
    	if (!(isEmpty(names)||isEmpty(mchs))) {
    	  double bestRank=-1.;
    	  for (String nm:names) {
        	Set<String> rs = Langutils.prepareForRanking(nm,translit);
        	
        	for (Collection<String> ms:mchs) {
        	   Likenesses lk=new Likenesses(rs,ms);
        	   if (lk.total>bestRank) {
        		  bestRank=lk.total;
        		  bestName=nm; 
        	   }
        	}
    	  }

    	}
    	return bestName;
    }
    
    private LinkedHashSet<LinkedHashSet<String>> lastMchs=null;
    
    public LinkedHashSet<LinkedHashSet<String>> getLastMatches() {
    	return lastMchs;
    }
    

    public static LinkedHashSet<LinkedHashSet<String>> excludeMatcher(
    	 LinkedHashSet<LinkedHashSet<String>> mchs1,
    	 String firstPassBestMatcher
    ) {
		LinkedHashSet<LinkedHashSet<String>> mchs2=null;//new LinkedHashSet<LinkedHashSet<String>>();
		double bestPossible=Langutils.likeness00(firstPassBestMatcher)/(double)firstPassBestMatcher.length();
		for (LinkedHashSet<String> mch:mchs1) {
			LinkedHashSet<String> refined=null;
			for (String s:mch) {
			   double lk=Langutils.likeness(firstPassBestMatcher, s);
			   if (bestPossible-lk>=1) {
				 // matches "s" is not close enough to the previous search
				 // best matcher and should be kept for the next search 
				 if (refined==null) refined=new LinkedHashSet<String>();
				 refined.add(s);
			   }
			}
			if (refined!=null) {
			   if (mchs2==null) mchs2=new LinkedHashSet<LinkedHashSet<String>>();
			   mchs2.add(refined);
			}
		}
		return mchs2;
	}    
    

	public SearchResult getCandidates(
			LinkedHashSet<LinkedHashSet<String>> mchs,
			Collection<ContactRecord> phones, 
			Set<Integer> onlyType,
			boolean withPhoneOnly,
			int maxSetSize,
			double best,
			double worst,
			boolean useVIPs
	) {
		lastMchs=mchs;
		
		Results results=new Results(maxSetSize,onlyType,mchs);

		if (!isEmpty(phones)) {
			
			boolean typesOnly=isEmpty(mchs);
			
			if (!typesOnly) dump(TAG,mchs);
			
			List<Candidate> rank=
				typesOnly
				   ?getByPhoneType(phones, onlyType)	
				   :getRawMatches(mchs,phones,onlyType,withPhoneOnly,best,worst);	
				   
			if (!rank.isEmpty()) {
				 //////////////////////////////////////////////////////
				/*
				 if  (withPhoneOnly) {
					filterOutSamePhoneNumbers(rank);
				 }
				 */
			  	 if (typesOnly) orderCandidates(rank);
			   	 results.rawResults=rank;
			  	 ///////////////////////////////////////////////////////

				 dump(TAG
						 
						 
						 
						 
						 
						 
						 , rank);
		   }
		   if (!isEmpty(rank)) {
			 //  filterOutSameContacts(rank);
			   Log.d(TAG,"0 primary_search "+dump(rank));
			   
			   
			   ////////////////// advance VIP's if it's necessary
			   /*
			   double bestPossible=Langutils.bestPossibleRank(mchs);
			   boolean shouldAdvance= (!rank.get(0).contact.isVip())&&(rank.get(0).finalRank*1.1<bestPossible);
			   Log.d(TAG, "should "+(shouldAdvance?"":"not")+" advance the VIP's : "+bestPossible+" "+rank.get(0).finalRank+" "
					   +" isVIP="+rank.get(0).contact.isVip()
			   );
			   if (shouldAdvance) {
				  // should advance the VIP's if there are any
				  List<Candidate> vips= findRawVIPs(rank);
				  if (!isEmpty(vips)) {
					 double bestActualRank=rank.get(0).finalRank;//, bestVipRank=vips.get(0).finalRank;
					 
					 if (vips.size()>2) vips=vips.subList(0, 2);
					 
					 for (Candidate c:vips) {
						c.finalRank=bestActualRank+c.finalRank/100.;
						rank.remove(c);
					 }
					 
					 rank.addAll(0, vips);
					 Log.d(TAG, "advanced vips: "+dump(vips));
					 //orderCandidates(rank);
				  }
			   }
			   */			   
			   //////////////////////////////////////////////////
			   
			   int lastIndex=rank.size()-1;
			   if (lastIndex>=maxSetSize) lastIndex=maxSetSize-1;
			   while ((lastIndex<Math.min(MAX_BEST_CASES,rank.size())-1)&&
				  (rank.get(0).getFinalRank()-rank.get(lastIndex).getFinalRank()<0.001)) lastIndex++;
			   
			   
			   double bestPossible=Langutils.bestPossibleRank(mchs);
			   boolean bestMatchIsGoodEnough=(rank.size()==1)||(rank.get(0).finalRank*1.1>=bestPossible);

               {
				  List<Candidate> s=rank.subList(0, lastIndex+1);
				  rank=new ArrayList<Candidate>(s.size());
				  for (Candidate c:s) rank.add(c);
			   }
			   
			   Log.d(TAG,"1 primary_search "+dump(rank));
			   
			   results.results=rank;
			  
			   if (Config.new_style_vip_advancing&&useVIPs&&
					   !isEmpty(results.results)&&
					     maxSetSize>1&&results.rawResults.size()>results.results.size()) {
				   ////// advance VIP's if it's necessary
				   
				   if (bestMatchIsGoodEnough) {
					 List<Candidate> exactHits=findExactHits(results.rawResults);
				     if (!isEmpty(exactHits)) {
				    	 exactHits.removeAll(results.results);
				    	 bestMatchIsGoodEnough=isEmpty(exactHits);
				     }
				   }
				   
				   boolean shouldAdvance= !bestMatchIsGoodEnough;
				           
				   if (shouldAdvance) {
					   Log.d(TAG,"shouldAdvance");
					   int maxVipsAdvanced=Math.min(maxSetSize/2, MAX_VIPS_ADVANCED);
					   List<Candidate> releventVipsInResuls=findRelevantVIPs(results.results,maxVipsAdvanced);
					   int nRelevatVipsInResult=releventVipsInResuls==null?0:releventVipsInResuls.size();
					   if (nRelevatVipsInResult<maxVipsAdvanced) {
						   Log.d(TAG,"nRelevatVipsInResult<maxVipsAdvanced");
						   List<Candidate> relevantVips=findRelevantVIPs(results.rawResults,maxVipsAdvanced);
						   int nRelevantVips=relevantVips==null?0:relevantVips.size();
						   if (nRelevatVipsInResult<nRelevantVips) {;
						   List<Candidate> vipsToRemove=findRawVIPsOrExactHits(results.results);
						   if (!isEmpty(vipsToRemove)) {
							   Log.d(TAG,"!isEmpty(vipsToRemove #1)");
							   vipsToRemove.removeAll(releventVipsInResuls);
							   if (!isEmpty(vipsToRemove)) {
								   Log.d(TAG,"!isEmpty(vipsToRemove #2)");
								   results.results.removeAll(vipsToRemove);  
								   results.rawResults.addAll(0, vipsToRemove);
							   }
						   }
						   results.rawResults.removeAll(releventVipsInResuls);
						   relevantVips.removeAll(results.results); // leave vips to inject only.
						   if (relevantVips.size()>0) {
							   Log.d(TAG,"relevantVips.size()>0");
							   int nToMove=results.results.size()+relevantVips.size()-maxSetSize;
							   if (nToMove>0) {
								   // move the extra results to raw results.
								   int szBeforeInjection=results.results.size()-nToMove;
								   List<Candidate> newResults=new ArrayList<Candidate>(szBeforeInjection);
								   int j=results.results.size(),k=0;
								   while (--j>=0) {
									   Candidate c=results.results.get(j);
									   if (c.isVipOrExactHit()||k>=nToMove) 
										   newResults.add(0,c); 
									   else {
										   results.rawResults.add(0,c); k++;
									   }
								   } 
							   }
							   results.results.addAll(relevantVips); // injection
						   }
						   } 
					   }
				   }
				   ////////////////////////////////////////
			   }
		   }
		}
	//	filterOutSameContacts(results.rawResults);
		return results;
	}

	final public static int MAX_SET_SIZE=Config.debug?6:5, MAX_BEST_CASES=5, MAX_VIPS_ADVANCED=Config.debug?3:3;
	final public static double STD_BEST=1.7,STD_WORST=0.5;
}
