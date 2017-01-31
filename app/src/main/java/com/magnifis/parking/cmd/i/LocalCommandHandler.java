package com.magnifis.parking.cmd.i;

import java.util.List;

import com.magnifis.parking.Abortable;

public interface LocalCommandHandler extends Abortable {
	/**
	 * 
	 * @param matches
	 * @return  true -- processed, false -- not processed
	 */
	boolean onVoiceInput(List<String> matches, boolean partial);
}