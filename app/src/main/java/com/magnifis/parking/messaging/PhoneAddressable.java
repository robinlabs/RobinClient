package com.magnifis.parking.messaging;

import static com.robinlabs.utils.BaseUtils.isEmpty;

import com.magnifis.parking.utils.Utils;

public class PhoneAddressable extends Addressable {

	@Override
	public String getSynteticDisplayName(boolean isForSpeech) {
		return isEmpty(displayName)?(Utils.phoneNumberToSpeech(address).toString()):displayName;
	} 

}
