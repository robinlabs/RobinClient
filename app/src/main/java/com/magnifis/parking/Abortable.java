package com.magnifis.parking;

public interface Abortable {
   final static int BY_FB_CLICK=1, BY_FB_BUBBLE_CLICK=2, BY_WEATHER_CLICK=4, BY_MENU_CLICK=8, BY_MA_BUBBLE_CLICK=16, TTS_ABORTED=32;
   void abort(int flags);
}
