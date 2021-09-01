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
public class AzureSpeech extends CordovaPlugin {
  @Override
  public boolean execute(String action, JSONArray args,
    final CallbackContext callbackContext) {
      switch(action) {
          case "synthesise":
            this.Synthesize(args);
          break;
          default: 
            callbackContext.error("\"" + action + "\" is not a recognized action.");
            return false;
      }

      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
      callbackContext.sendPluginResult(pluginResult);
      return true;
  }

  public boolean Synthesize(JSONArray args) {
        JSONObject options = args.getJSONObject(0);
        SpeechConfig speechConfig = SpeechConfig.fromSubscription(options.getString("SubscriptionKey"),options.getString("ServiceRegion") );
         AudioConfig audioConfig = AudioConfig.fromDefaultSpeakerOutput();

        SpeechSynthesizer synthesizer = new SpeechSynthesizer(speechConfig, audioConfig);
        synthesizer.SpeakText(options.getString("Message"));
  }
}