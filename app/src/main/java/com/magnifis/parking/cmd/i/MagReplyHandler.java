package com.magnifis.parking.cmd.i;

import com.magnifis.parking.Abortable;
import com.magnifis.parking.model.MagReply;

public interface MagReplyHandler  extends Abortable {
	/**
	 * 
	 * @param matches
	 * @return  true -- processed, false -- not processed
	 */
    boolean handleReplyInBg(MagReply reply);
    boolean handleReplyInUI(MagReply reply);
}