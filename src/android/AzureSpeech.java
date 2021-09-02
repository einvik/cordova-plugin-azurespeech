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

import android.content.pm.PackageManager;
import org.apache.cordova.PermissionHelper;

public class AzureSpeech extends CordovaPlugin {
  CallbackContext callbackContext;
  CallbackContext getPermissionCallbackContext;

  SpeechConfig speechConfig;
  String speechRecognitionLanguage = "en-US";
  String speechSubscriptionKey = "";
  String serviceRegion = "";
  
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
        callbackContext.error(e.toString());
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
          pluginResult.setKeepCallBack(true);
          callbackContext.sendPluginResult(pluginResult);
          this.GetMicPermission(RECORD_AUDIO);
        }
      
      }
      catch (Exception e) 
      {
        callbackContext.error(e.toString());
      }
    }
    // if (action.equals("regognize")) 
    // {
    //   try {
    //     PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, Boolean.TRUE);
    //     callbackContext.sendPluginResult(pluginResult);
    //     return true;
    //   } 
    //   catch(Exception e) 
    //   {
    //     callbackContext.error(e.toString());
    //     return false;
    //   }
    // }

    if (action.equals("synthesize")) 
    {
      try {
        PluginResult pluginResult = this.Synthesize(args.getJSONObject(0));
        callbackContext.sendPluginResult(pluginResult);
        return true;
      } 
      catch(Exception e) 
      {
        callbackContext.error(e.toString());
        return false;
      }
    }
     

      return false;
  }
  // public void InitRecognizer() 
  // {
  //   if (this.speechConfig == null) {
  //     this.speechConfig = SpeechConfig.fromSubscription(options.getString("SubscriptionKey"),options.getString("ServiceRegion"));
  //   }
  // }
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

  
  
  private void SendUpdate(JSONObject info, boolean keepCallBack) 
  {
    if (this.callbackContext != null) 
    {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.Ok, info);
        pluginResult.setKeepCallBack(keepCallBack);
        this.callbackContext.sendPluginResult(pluginResult);
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
          this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.Error, PERMISSON_DENIED_ERROR));
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
