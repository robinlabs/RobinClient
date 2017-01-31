package com.magnifis.parking.voice;

import com.magnifis.parking.Log;
import com.magnifis.parking.VR;

/**
 * Created by oded on 7/3/14.
 */
public class CapioVR extends VR {

    public CapioVR(Object activity, IAnimator animator) {
        super(activity, animator);
        Log.d(VR.TAG_SPEECH, "Created Capio Voice recognizer");
    }


    SpeechContext getSpeechContext() {
        return null;
    }

    void setSpeechContext(SpeechContext speechContext) {
    }

}
