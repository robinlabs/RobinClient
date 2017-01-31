package com.magnifis.parking.voice;

import com.magnifis.parking.model.Understanding;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by oded on 7/3/14.
 */


public class SpeechContext {

    JSONObject voiceContextObject;


    Map<Integer, String> getMap() {

        Map<Integer, String> map = new HashMap<Integer, String>();

        map.put(Understanding.CMD_DICTATE_NEW, ContextType.TEXT_MESSAGE);
        map.put(Understanding.CMD_CALL, ContextType.PERSON_NAME);
        map.put(Understanding.CMD_DICTATE_NEW, ContextType.PERSON_NAME);
        map.put(Understanding.CMD_NO, ContextType.YES_NO);
        map.put(Understanding.CMD_YES, ContextType.YES_NO);
        map.put(Understanding.CMD_SEND, ContextType.TEXT_MESSAGE);
        map.put(Understanding.CMD_REMINDER, ContextType.TEXT_MESSAGE);
        map.put(Understanding.CMD_PARKING, ContextType.STREET_ADDRESS);
        map.put(Understanding.CMD_NAVIGATE, ContextType.STREET_ADDRESS);
        map.put(Understanding.CMD_RETWEET, ContextType.TEXT_MESSAGE);
        map.put(Understanding.CMD_CONTACTS, ContextType.PERSON_NAME);

        //new suggestions
        map.put(Understanding.CMD_HELLO, ContextType.GREETING);
        map.put(Understanding.CMD_HOW_ARE_YOU, ContextType.GREETING);
        map.put(Understanding.CMD_MY_BIRTHDAY, ContextType.DATE);
        map.put(Understanding.CMD_CALENDAR, ContextType.DATE);
        map.put(Understanding.CMD_ALARM, ContextType.DATE);

        return map;
    }

    class ContextType {
        public final static String
                YES_NO = "YesNo",
                PERSON_NAME = "PersonName",
                STREET_ADDRESS = "Address",
                TEXT_MESSAGE = "TextMessage",
                OTHER = "Other",//(default)

                //new suggestions
                GREETING = "Greeting",
                DATE = "Date";
    }
}


