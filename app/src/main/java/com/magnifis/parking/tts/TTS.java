package com.magnifis.parking.tts;

import java.util.Locale;

public abstract class TTS {
  
  public abstract void speak(String s);
  public abstract void stop();
  public abstract void shutdown();
  
  
  protected void onInit() {}
  protected void onInitError() {}
  protected void onAllCompleted() {}
  
  
  
  public void pushVoice(String newVoice) {}
  public void popVoice() {}
  public void switchVoice() {}
  
  public abstract String getLanguage();
  
  public boolean setLanguage(Locale loc) {  return true; }

  static String lastLang = null;

  public boolean setLanguage(String lang) {
      if (lastLang == null) {
          if (lang == null)
            return true;

          lastLang = lang;
          return setLanguage(new Locale(lang));
      }

      if (lastLang.equals(lang))
	    return true;

	  return setLanguage(new Locale(lang));
  }
  
}
