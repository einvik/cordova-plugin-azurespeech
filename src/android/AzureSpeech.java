package com.einvik.cordova.plugin.azurespeech;

import com.microsoft.cognitiveservices.speech.AudioDataStream;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisOutputFormat;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// import android.Manifest;
import java.util.concurrent.TimeUnit;

public class AzureSpeech extends CordovaPlugin {
  PluginResult pluginResult;
  SpeechConfig speechConfig;
  
  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) 
  {
      switch(action) {
        case "recognize":
        try {
          for (Integer i = 0; i < 5; i++) {
            String msg = i.toString();
            callbackContext.success(msg);
            TimeUnit.MINUTES.sleep(1);
          }
        } 
        catch(Exception e) 
        {
          callbackContext.error(e.toString());
          return false;
        }
    
          break;
        case "synthesize":
          try {
            this.pluginResult = this.Synthesize(args.getJSONObject(0));
            callbackContext.sendPluginResult(this.pluginResult);
          } 
          catch(Exception e) 
          {
            callbackContext.error(e.toString());
            return false;
          }
          break;
          default: 
            callbackContext.error("\"" + action + "\" is not a recognized action.");
            return false;
      }
      return true;
  }
  public void InitRecognizer() 
  {
    if (this.speechConfig == null) {
      this.speechConfig = SpeechConfig.fromSubscription(options.getString("SubscriptionKey"),options.getString("ServiceRegion"));
    }
  }
  public PluginResult Synthesize(JSONObject options) 
  {
        try {
          if (this.speechConfig == null) {
            this.speechConfig = SpeechConfig.fromSubscription(options.getString("SubscriptionKey"),options.getString("ServiceRegion"));
          }
          AudioConfig audioConfig = AudioConfig.fromDefaultSpeakerOutput();

          SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, audioConfig);
          synthesizer.SpeakText(options.getString("Message"));
          return new PluginResult(PluginResult.Status.OK);
        }
        catch(Exception e) 
        {
          return new PluginResult(PluginResult.Status.ERROR);
        }
  }
}