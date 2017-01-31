package com.magnifis.parking.phonebook;

import com.magnifis.parking.Consts;
import com.magnifis.parking.model.ContactRecord;
import com.magnifis.parking.model.ContactRecordBase;

public class CandidateBase {
	
	public static double
	   STD_FAVORITE_COEFF=1.1, STD_3_DAYS_COEFF=1.3 , STD_10_DAYS_COEFF=1.1;
	
	public double calculateFinalRank(ContactRecordBase contact,double total) {

		double effectiveCoeff=1;
		
		if (contact.isContactedInLastNDays(Consts.FAVORITE_IS_VIP_IF_CALLED_IN_DAYS)) { 
			if (contact.isFavorite())  effectiveCoeff=STD_FAVORITE_COEFF;


			if (contact.getTimesContacted()>=3) {
				if (contact.isContactedInLastNDays(3)) effectiveCoeff+=STD_3_DAYS_COEFF-1; else
					if (contact.isContactedInLastNDays(10)) effectiveCoeff+=STD_10_DAYS_COEFF-1;
			}
		}
		
		return total*effectiveCoeff;
	}
}
