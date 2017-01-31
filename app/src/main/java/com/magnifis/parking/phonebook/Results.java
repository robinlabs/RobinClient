package com.magnifis.parking.phonebook;

import static com.robinlabs.utils.BaseUtils.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.magnifis.parking.Log;
import com.magnifis.parking.model.ContactRecord;
import com.magnifis.parking.model.ContactRecordBase;
import com.robinlabs.utils.BaseUtils;

public class Results extends SearchResult {
	final static String TAG=Results.class.getSimpleName();
	
	
	
	@Override
	public String getBestMatcher() {
		if (!isEmpty(results)) return results.get(0).bestMatcher;
		return null;
	}

	final int maxSetSize;
	final Set<Integer> onlyType;
	final LinkedHashSet<LinkedHashSet<String>> matches;
	
	public Results(int maxSetSize, Set<Integer> onlyType, LinkedHashSet<LinkedHashSet<String>> matches) { 
	   this.maxSetSize=maxSetSize;
	   this.onlyType=onlyType;
	   this.matches=matches;
	}
	
	// both the lists are ordered
	public List<Candidate> results=null;
	public List<Candidate> rawResults=null;
	volatile List<ContactRecord> contacts=null;
	
	@Override
	public synchronized List<ContactRecord> getContacts() {
		if (contacts==null) contacts=candidates2contacts(results);
		return contacts;
	}
	@Override
	public boolean anyOthers() {
		Candidate last=BaseUtils.getLast(results), lastRaw=BaseUtils.getLast(rawResults);
		return last!=null&&lastRaw!=null&&lastRaw!=last;			
	}
	
	@Override
	public double howMuchTheBestIsBetter() {
	   if (!isEmpty(rawResults)) {
		  if (rawResults.size()==1) return Double.MAX_VALUE;
		  double d=rawResults.get(0).finalRank-rawResults.get(1).finalRank;
		  Log.d(TAG,"howMuchTheBestIsBetter d="+d);
		  return d;
	   }
	   return 0.; 
	}
	
	@Override
	public boolean shrinkToSingleIfBetterThan(double x) {
		if (howMuchTheBestIsBetter()>=x) {
			while (results.size()>1) results.remove(results.size()-1);
			return true;
		}
		return false;
	}
	
	
	
	
	// return next maxSetSize (or less values) from the rawResults
	@Override
	public Results nextResults() {
		if (anyOthers()) {
			Results r=new Results(maxSetSize,onlyType,matches);
			r.stepNo=stepNo+1;
		    r.rawResults=rawResults;
			r.rawResults.removeAll(results);
			//if (isEmpty(matches)||rawResults.size()<=maxSetSize) {
			  int sz=Math.min(maxSetSize, r.rawResults.size());
			  r.results=new ArrayList<Candidate>();
			  for (Candidate c:r.rawResults.subList(0, sz))
				  r.results.add(c);  
			 // r.results.addAll(r.rawResults.subList(0, Math.min(maxSetSize, r.rawResults.size())));
		//    } else {
		//      r.results=shrinkToSize(matches,rawResults,maxSetSize);
		//    }
			
            return r;
		}
		return null;
	}
	@Override
	public int countResults() {
		// TODO Auto-generated method stub
		return isEmpty(results)?0:results.size();
	}
	
	public static List<ContactRecord> candidates2contacts(Collection<Candidate> candidates) {
		  if (isEmpty(candidates)) return null;
		  ContactRecord a[]=new ContactRecord[candidates.size()];
		  int i=0;
		  for (Candidate c:candidates) a[i++]=c.contact; 
		  return  Arrays.asList(a);
	}
	
	@Override
	public List<ContactRecord> getAllContacts() {
		return candidates2contacts(rawResults);
	}
};
