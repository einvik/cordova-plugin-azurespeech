package com.einvik.cordova.plugin.azurespeech;

import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.pm.PackageManager;
import android.Manifest;
import org.apache.cordova.PermissionHelper;
import java.util.concurrent.Future;

public class AzureSpeech extends CordovaPlugin {

  private static final String LOG_TAG = "AzureSpeech";
  private static final boolean DEBUGMODE = Boolean.TRUE;
  CallbackContext callbackContext;
  CallbackContext getPermissionCallbackContext;
  CallbackContext recognizerCallbackContext;

  SpeechConfig speechConfig;
  SpeechRecognizer speechRecognition;
  String speechRecognitionLanguage = "en-US";
  String speechSubscriptionKey = "";
  String serviceRegion = "";

  public static String[] permissions = { Manifest.permission.RECORD_AUDIO };
  public static int RECORD_AUDIO = 0;
  public static int PERMISSION_DENIED_ERROR = 400;

  MicrophoneStream microphoneStream;

  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) 
  {
    if (action.equals("hasPermission")) 
    {
      try 
      {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,this.HasMicPermission());
        callbackContext.sendPluginResult(pluginResult);
        return true;
      }
      catch (Exception e) 
      {
        callbackContext.error(e.getMessage());
      }
    }

    if (action.equals("getPermission")) 
    {
      try 
      {

        if (this.HasMicPermission()) 
        {
          PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,this.HasMicPermission());
          callbackContext.sendPluginResult(pluginResult);
          return true;
        } 
        else 
        {
          this.getPermissionCallbackContext = callbackContext;
          PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
          pluginResult.setKeepCallback(true);
          callbackContext.sendPluginResult(pluginResult);
          this.GetMicPermission(RECORD_AUDIO);
        }
      
      }
      catch (Exception e) 
      {
        callbackContext.error(e.getMessage());
      }
    }
    if (action.equals("regognize")) 
    {
      try {
        if (this.speechConfig == null) {
          JSONObject options = args.getJSONObject(0);
          this.speechConfig = SpeechConfig.fromSubscription(options.getString("SubscriptionKey"),options.getString("ServiceRegion"));
        }
        this.recognizerCallbackContext = callbackContext;
        AudioConfig audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
        speechRecognition = new SpeechRecognizer(speechConfig, audioInput);
        
        speechRecognition.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
          String Transcript = speechRecognitionResultEventArgs.getResult().getText();
          SendTranscriptToClient(Transcript, "recognizing");

        });

        speechRecognition.recognized.addEventListener((o, speechRecognitionResultEventArgs) -> {
          String Transcript = speechRecognitionResultEventArgs.getResult().getText();
          SendTranscriptToClient(Transcript, "recognized");
      });
        Future<Void> task = speechRecognition.startContinuousRecognitionAsync();

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, Boolean.TRUE);
        callbackContext.sendPluginResult(pluginResult);
        return true;
      } 
      catch(Exception e) 
      {
        callbackContext.error(e.toString());
        return false;
      }
    }

    if (action.equals("synthesize")) 
    {
      try {
        PluginResult pluginResult = this.Synthesize(args.getJSONObject(0));
        callbackContext.sendPluginResult(pluginResult);
        return true;
      } 
      catch(Exception e) 
      {
        callbackContext.error(e.getMessage());
      }
    }
     

      return false;
  }

  private MicrophoneStream createMicrophoneStream() {
    if (this.microphoneStream != null) {
        this.microphoneStream.close();
        this.microphoneStream = null;
    }

    this.microphoneStream = new MicrophoneStream();
    return microphoneStream;
}

  private void SendTranscriptToClient(String Transcript,String EventName) {
    JSONObject info = new JSONObject();
    info.put(EventName,Transcript);
    this.SendRecognizerUpdate(info);
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
          return new PluginResult(PluginResult.Status.ERROR,e.getMessage());
        }
  }

  private void SendUpdate(JSONObject info, boolean keepCallBack) 
  {
    if (this.callbackContext != null) 
    {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, info);
        pluginResult.setKeepCallback(keepCallBack);
        this.callbackContext.sendPluginResult(pluginResult);
    }
  }
  
  private void SendRecognizerUpdate(JSONObject info) 
  {
    if (this.recognizerCallbackContext != null) 
    {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, info);
        pluginResult.setKeepCallback(Boolean.TRUE);
        this.recognizerCallbackContext.sendPluginResult(pluginResult);
    }
  }
  public boolean HasMicPermission() 
  {
    return PermissionHelper.hasPermission(this, permissions[RECORD_AUDIO]);
  }
  protected void GetMicPermission(int requestCode) 
  {
    PermissionHelper.requestPermission(this, requestCode, permissions[RECORD_AUDIO]);
  }

  public void onDestroy() {}
  public void onReset() {}
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException
  {
    for (int result:grantResults) 
    {
      if (result == PackageManager.PERMISSION_DENIED) 
      {
        if (this.getPermissionCallbackContext == null) 
        {
          this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
          return;
        } 
        else 
        {
          this.getPermissionCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, Boolean.FALSE));
        }
      } 
      else 
      {
        if (this.getPermissionCallbackContext == null) 
        {
          this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, Boolean.TRUE));
          return;
        } 
        else 
        {
          this.getPermissionCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, Boolean.TRUE));
        }
      }
    }
  }
}
