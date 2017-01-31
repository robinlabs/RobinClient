package com.magnifis.parking.phonebook;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.magnifis.parking.model.ContactRecord;
import com.magnifis.parking.model.ContactRecordBase;

public interface IPhonebookMatcher {
	 List<ContactRecord> getMatches(
			String[] queryCandidates,
			Collection<ContactRecord> contacts,
			boolean uncond,
			Set<Integer> onlyType
	 );
}
