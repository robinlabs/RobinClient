package com.magnifis.parking.phonebook;

import com.magnifis.parking.model.ContactRecord;
import com.magnifis.parking.model.ContactRecordBase;

public class MatchCandidate extends CandidateBase implements Comparable<MatchCandidate> {
	public double rank = 0;
	public String match = null;

	public double getRank() {
		return rank;
	}

	public void setRank(double rank) {
		this.rank = rank;
	}

	public String getMatch() {
		return match;
	}

	public void setMatch(String match) {
		this.match = match;
	}

	@Override
	public int compareTo(MatchCandidate another) {
		double d=rank-another.rank; 
		if (d==0) return 0;
		return d<0?1:-1;
	}
	
	public MatchCandidate() {}
	
	
	public MatchCandidate(String match, ContactRecordBase contact, double totalRank) {
	   this.match=match;
	   this.rank=calculateFinalRank(contact,totalRank);
	}
	
	public MatchCandidate(String match, double rank) {
	  this.match=match;
	  this.rank=rank;
	}

}