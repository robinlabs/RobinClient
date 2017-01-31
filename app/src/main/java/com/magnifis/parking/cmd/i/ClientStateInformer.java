package com.magnifis.parking.cmd.i;

public interface ClientStateInformer {
   final static String
     SN_YES_NO="yes_no",
     SN_DICTATE_NAME="dictate_name",
     SN_DICTATE_MSG="dictate_message",
     SN_DICTATE_MSG_NEW="dictate_message_new",
     SN_CALLEE_SELECTION="callee_selection"
   ;
   String getClientStateName();  
}
