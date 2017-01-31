package com.magnifis.parking.phonebook;

import com.magnifis.parking.model.ContactRecord;
import com.magnifis.parking.model.ContactRecordBase;
import com.robinlabs.utils.BaseUtils;

public class Candidate extends CandidateBase {
	
	protected String bestMatcher=null;
	
	
	@Override
	public double calculateFinalRank(ContactRecordBase contact,double total) {
		double r=super.calculateFinalRank(contact, total);
		if (exactHit) r+=0.5;
		return r;
	}
	
	
	public String getBestMatcher() {
		return bestMatcher;
	}

	public void setBestMatcher(String bestMatcher) {
		this.bestMatcher = bestMatcher;
	}

	public ContactRecord contact;

	double finalRank;
	boolean exactHit=false;
	
	public boolean isVipOrExactHit() {
		return exactHit||contact.isVip();
	}
	
	public boolean eqByRanks(Candidate c) {
		return c.getFinalRank()==getFinalRank();
	}
	
	Candidate(ContactRecord contact, String bestMatcher, double finalRank) {
	  this.setFinalRank(finalRank);
	  this.contact=contact;
	}
	
	public Candidate(ContactRecord contact, CharSequence bestMatcher, boolean exactHit, double totalRank) {
		  this.exactHit=exactHit;
		  this.contact=contact;
		  this.bestMatcher=BaseUtils.toString(bestMatcher);
		  setFinalRank(calculateFinalRank(contact,totalRank));
		}
	
	@Override
	public String toString() {
	  return "("+contact.getName()+','/*+getWorstRank()+','+getBestRank()+','*/+getFinalRank()+(contact.isFavorite()?",*":"")+")";
	}

	public double getFinalRank() {
		return finalRank;
	}

	public void setFinalRank(double finalRank) {
		this.finalRank = finalRank;
	}
}