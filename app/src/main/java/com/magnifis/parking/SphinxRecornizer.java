package com.magnifis.parking;

import java.io.File;

import edu.cmu.pocketsphinx.AssetsTask;
import edu.cmu.pocketsphinx.AssetsTaskCallback;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class SphinxRecornizer implements AssetsTaskCallback, RecognitionListener {

   // static public SpeechRecognizer recognizer;

    static String last = "";

    @Override
	public void onTaskCancelled() {
	}

    public static void open() {

        String p = App.self.getStringPref("voiceactivation");
        if (!"on".equals(p)){ return;}

        Log.d("Speech", "*** SPHINX OPEN ***");

        SphinxRecornizer t = new SphinxRecornizer();
        new AssetsTask(App.self, t).execute();
    }

    static public void close() {
        Log.d("Speech", "*** SPHINX CLOSE ***");
/*
        if (recognizer != null) {
            recognizer.cancel();
            recognizer = null;
        }
  */
    }

    static long timeStart = 0;

    public static void start() {
        Log.d("Speech", "*** SPHINX START ***");
/*
        if (recognizer == null)
            return;

        last = "";

        String p = App.self.getStringPref("voiceactivation");
        if (!"on".equals(p) && !"sensitive".equals(p))
            return;

        Boolean b = App.self.getBooleanPref("voiceactivationoncharging");
        if (b == null)
            b = true;

        if (b && !App.self.isCharging())
            return;

        recognizer.startListening("main");
        timeStart = System.currentTimeMillis();
  */
    }

    public static void stop() {
        Log.d("Speech", "*** SPHINX STOP ***");
/*
        if (recognizer == null)
            return;

        if (System.currentTimeMillis() - timeStart > 300)
            recognizer.cancel();

        Robin.changeNotification(false);
  */
    }

    @Override
	public void onTaskComplete(File assetsDir) {
    	/*
        Log.d("Speech", "*** SPHINX onTaskComplete ***");

        File modelsDir = new File(assetsDir, "models");
        recognizer = defaultSetup()
                .setAcousticModel(new File(modelsDir, "hmm/en-us-semi"))
                .setDictionary(new File(modelsDir, "lm/cmu07a.dic"))
                //.setRawLogDir(null)//assetsDir)
                .setKeywordThreshold(1e-5f)
                .getRecognizer();

        recognizer.addListener(this);

        File menuGrammar = null;
        if ("sensitive".equals(App.self.getStringPref("voiceactivation")))
            menuGrammar = new File(modelsDir, "grammar/robin2.gram");
        else
            menuGrammar = new File(modelsDir, "grammar/robin.gram");
        recognizer.addGrammarSearch("main", menuGrammar);
	*/
	}

	@Override
	public void onTaskError(Throwable arg0) {
        arg0.printStackTrace();
	}

	@Override
	public void onTaskProgress(File arg0) {
	}

	@Override
	public void onTaskStart(int arg0) {
	}

	@Override
	public void onBeginningOfSpeech() {
	}

	@Override
	public void onEndOfSpeech() {
	}

	@Override
	public void onPartialResult(Hypothesis hypothesis) {
        Robin.changeNotification(true);

        String text = hypothesis.getHypstr();
//        if (!text.contains("robin")) {
        if (text.equals(last))
            return;

        if (last.length() > text.length())
            Log.d("Speech", text);
        else
            Log.d("Speech", text.substring(last.length()));

        //stop();
        //start();

        last = text;

        if ("sensitive".equals(App.self.getStringPref("voiceactivation"))) {
            if (text.contains("google") || text.contains("robin"))
                App.activateApp();
            return;
        }

        if (text.contains("ok google")  || text.contains("hey google") || text.contains("hello google")
                || text.contains("ok robin") || text.contains("hey robin") || text.contains("hello robin"))
            App.activateApp();
	}

	@Override
	public void onResult(Hypothesis hypothesis) {
	}

}
