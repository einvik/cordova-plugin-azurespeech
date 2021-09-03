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

// For microphone stream
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import com.microsoft.cognitiveservices.speech.audio.PullAudioInputStreamCallback;
import com.microsoft.cognitiveservices.speech.audio.AudioStreamFormat;

import android.util.Log;
import android.media.AudioDeviceInfo;
public class AzureSpeech extends CordovaPlugin {

  private static final String LOG_TAG = "AzureSpeech";
  private static final boolean DEBUGMODE = Boolean.TRUE;
  CallbackContext callbackContext;
  CallbackContext getPermissionCallbackContext;
  CallbackContext recognizerCallbackContext;

  SpeechConfig speechConfig;
  SpeechRecognizer speechRecognition;
  MicrophoneStream microphoneStream = null;
  String speechRecognitionLanguage = "en-US";
  String speechSubscriptionKey = "";
  String serviceRegion = "";

  public static String[] permissions = { Manifest.permission.RECORD_AUDIO };
  public static int RECORD_AUDIO = 0;
  public static int PERMISSION_DENIED_ERROR = 400;


  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) 
  {
    Log.e(LOG_TAG,action);
    // if (action.equals("hasPermission")) 
    // {
    //   try 
    //   {
    //     PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,this.HasMicPermission());
    //     callbackContext.sendPluginResult(pluginResult);
    //     return true;
    //   }
    //   catch (Exception e) 
    //   {
    //     callbackContext.error("haspermission" + e.getMessage());
    //   }
    // }

    // if (action.equals("getPermission")) 
    // {
    //   try 
    //   {

    //     if (this.HasMicPermission()) 
    //     {
    //       PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,this.HasMicPermission());
    //       callbackContext.sendPluginResult(pluginResult);
    //       return true;
    //     } 
    //     else 
    //     {
    //       this.getPermissionCallbackContext = callbackContext;
    //       PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
    //       pluginResult.setKeepCallback(true);
    //       callbackContext.sendPluginResult(pluginResult);
    //       this.GetMicPermission(RECORD_AUDIO);
    //     }
      
    //   }
    //   catch (Exception e) 
    //   {
    //     callbackContext.error("getpermission" + e.getMessage());
    //   }
    // }
    if (action.equals("recognize")) 
    {
      try {
        if (this.microphoneStream != null) {
          this.speechRecognition.stopContinuousRecognitionAsync();
          this.microphoneStream.close();
          this.microphoneStream = null;
          this.SendTranscriptToClient("Stopping speechrecognition", "Event");
          return true;
        }
        
        if (this.speechConfig == null) {
          JSONObject options = args.getJSONObject(0);
          this.speechConfig = SpeechConfig.fromSubscription(options.getString("SubscriptionKey"),options.getString("ServiceRegion"));
        }
        this.recognizerCallbackContext = callbackContext;
        this.SendTranscriptToClient("Starting speechrecognition", "Event");

        AudioConfig audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
        // AudioConfig audioInput = AudioConfig.fromDefaultMicrophoneInput();
        this.speechRecognition = new SpeechRecognizer(speechConfig, audioInput);
        this.speechRecognition.speechEndDetected.addEventListener((o, speechRecognitionResultEventArgs) -> {
          Log.e(LOG_TAG,"speechStartDetected event");
          this.SendTranscriptToClient("", "speechStartDetected event");
        });    
        this.speechRecognition.speechEndDetected.addEventListener((o, speechRecognitionResultEventArgs) -> {
          Log.e(LOG_TAG,"speechEndDetected event");
          this.SendTranscriptToClient("", "speechEndDetected event");
        });
        this.speechRecognition.sessionStopped.addEventListener((o, speechRecognitionResultEventArgs) -> {
          Log.e(LOG_TAG,"sessionStopped event");
          this.SendTranscriptToClient("", "sessionStopped event");
        });
        this.speechRecognition.sessionStarted.addEventListener((o, speechRecognitionResultEventArgs) -> {
          Log.e(LOG_TAG,"sessionStarted event");
          this.SendTranscriptToClient("", "sessionStarted event");
        });
        this.speechRecognition.canceled.addEventListener((o, speechRecognitionResultEventArgs) -> {
          Log.e(LOG_TAG,"canceled event");
          this.SendTranscriptToClient("", "canceled event");
        });
        this.speechRecognition.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
          Log.e(LOG_TAG,"recognizing event");
          String Transcript = speechRecognitionResultEventArgs.getResult().getText();
          this.SendTranscriptToClient(Transcript, "recognizing event");

        });

        this.speechRecognition.recognized.addEventListener((o, speechRecognitionResultEventArgs) -> {
          String Transcript = speechRecognitionResultEventArgs.getResult().getText();
          Log.e(LOG_TAG,"recognized");

          SendTranscriptToClient(Transcript, "recognized");
        });
        
        Future<Void> task = this.speechRecognition.startContinuousRecognitionAsync();

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, Boolean.TRUE);
        pluginResult.setKeepCallback(Boolean.TRUE);
        callbackContext.sendPluginResult(pluginResult);
        return true;
      } 
      catch(Exception e) 
      {
        callbackContext.error("regognize" + e.toString());
        return false;
      }
    }

    // if (action.equals("synthesize")) 
    // {
    //   try {
    //     PluginResult pluginResult = this.Synthesize(args.getJSONObject(0));
    //     callbackContext.sendPluginResult(pluginResult);
    //     return true;
    //   } 
    //   catch(Exception e) 
    //   {
    //     callbackContext.error("synth" + e.getMessage());
    //   }
    // }
     

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
    try 
    {
      JSONObject info = new JSONObject();
      info.put(EventName,Transcript);
      this.SendRecognizerUpdate(info);
    } 
    catch (JSONException e) 
    {
      recognizerCallbackContext.error("SendTranscriptToClient, " + EventName + ":" + Transcript + e.getMessage());
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

    private class MicrophoneStream extends PullAudioInputStreamCallback {
      private final static int SAMPLE_RATE = 16000;
      private final AudioStreamFormat format;
      private AudioRecord recorder;
      

      public MicrophoneStream() {
          this.format = AudioStreamFormat.getWaveFormatPCM(SAMPLE_RATE, (short)16, (short)1);
          this.initMic();
      }

      public AudioStreamFormat getFormat() {
          return this.format;
      }

      @Override
      public int read(byte[] bytes) {
          long ret = this.recorder.read(bytes, 0, bytes.length);
          return (int)ret;
      }

      @Override
      public void close() {
          this.recorder.release();
          this.recorder = null;
      }

      private void initMic() {
          AudioFormat af = new AudioFormat.Builder()
                  .setSampleRate(SAMPLE_RATE)
                  .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                  .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                  .build();
          this.recorder = new AudioRecord.Builder()
                  .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                  .setAudioFormat(af)
                  .build();
          this.recorder.startRecording();
      }
  }

}
