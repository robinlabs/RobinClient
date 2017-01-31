package com.magnifis.parking.cmd.i;

import java.util.List;

import com.magnifis.parking.Abortable;

public interface IPartialVRResultHandler extends Abortable {
	/**
	 * 
	 * @param matches
	 * @return true -- processed, false -- not processed
	 */
	boolean onPartialVRInput(List<String> matches);
}