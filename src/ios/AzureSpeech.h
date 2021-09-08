  
/*
The MIT License (MIT)
Copyright (c) 2017 Mtech Access Ltd.
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
#import <Cordova/CDVPlugin.h>
#import <Cordova/CDVInvokedUrlCommand.h>

@interface AzureSpeech : CDVPlugin {
}

- (void)setVisible:(CDVInvokedUrlCommand*)command;

@end
public class AzureSpeech extends CordovaPlugin {

  private static final String LOG_TAG = "AzureSpeech";
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
    Log.d(LOG_TAG,action);
    if (action.equals("stoprecognize")) 
    {
      if (this.microphoneStream != null) {
        this.speechRecognition.stopContinuousRecognitionAsync();
        this.microphoneStream.close();
        this.microphoneStream = null;
        this.SendTranscriptToClient("", "Stopping speechrecognition");
        return true;
      }
      
    }
    if (action.equals("recognize")) 
    {
      try {
        if (this.speechConfig == null) {
          JSONObject options = args.getJSONObject(0);
          this.speechConfig = SpeechConfig.fromSubscription(options.getString("SubscriptionKey"),options.getString("ServiceRegion"));
        }
        this.recognizerCallbackContext = callbackContext;
        this.SendTranscriptToClient("","Starting speechrecognition");

        AudioConfig audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
        // AudioConfig audioInput = AudioConfig.fromDefaultMicrophoneInput();
        this.speechRecognition = new SpeechRecognizer(speechConfig, audioInput);
        this.speechRecognition.speechEndDetected.addEventListener((o, speechRecognitionResultEventArgs) -> {
          Log.d(LOG_TAG,"speechStartDetected event");
          this.SendTranscriptToClient("speechEndDetected", "");
        });    
        this.speechRecognition.speechEndDetected.addEventListener((o, speechRecognitionResultEventArgs) -> {
          Log.d(LOG_TAG,"speechEndDetected event");
          this.SendTranscriptToClient("speechEndDetected", "");
        });
        this.speechRecognition.sessionStopped.addEventListener((o, speechRecognitionResultEventArgs) -> {
          Log.d(LOG_TAG,"sessionStopped event");
          this.SendTranscriptToClient("speechEndDetected", "");
        });
        this.speechRecognition.sessionStarted.addEventListener((o, speechRecognitionResultEventArgs) -> {
          Log.d(LOG_TAG,"sessionStarted event");
          this.SendTranscriptToClient("sessionStarted", "");
        });
        this.speechRecognition.canceled.addEventListener((o, speechRecognitionResultEventArgs) -> {
          Log.d(LOG_TAG,"canceled event");
          this.SendTranscriptToClient("canceled","");
        });
        this.speechRecognition.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
          Log.d(LOG_TAG,"recognizing event");
          String Transcript = speechRecognitionResultEventArgs.getResult().getText();
          this.SendTranscriptToClient("recognizing",Transcript);
        });

        this.speechRecognition.recognized.addEventListener((o, speechRecognitionResultEventArgs) -> {
          String Transcript = speechRecognitionResultEventArgs.getResult().getText();
          Log.d(LOG_TAG,"recognized");
          SendTranscriptToClient("recognized",Transcript);
        });
        
        Future<Void> task = this.speechRecognition.startContinuousRecognitionAsync();

        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, Boolean.TRUE);
        pluginResult.setKeepCallback(Boolean.TRUE);
        callbackContext.sendPluginResult(pluginResult);
        return true;
      } 
      catch(Exception e) 
      {
        Log.e(LOG_TAG,"Error: "+ e.toString());
        callbackContext.error("regognize" + e.toString());
        return false;
      }
    }
    if (action.equals("synthesize")) 
    {
      try {
        Log.d(LOG_TAG,"synthesize");
        PluginResult pluginResult = this.Synthesize(args.getJSONObject(0));
        callbackContext.sendPluginResult(pluginResult);
        return true;
      } 
      catch(Exception e) 
      {
        Log.e(LOG_TAG,"synth: "+ e.getMessage());
        callbackContext.error("synth" + e.getMessage());
      }
    }
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
        callbackContext.error("haspermission" + e.getMessage());
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
        } 
        else 
        {
          this.getPermissionCallbackContext = callbackContext;
          PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
          pluginResult.setKeepCallback(true);
          callbackContext.sendPluginResult(pluginResult);
          this.GetMicPermission(RECORD_AUDIO);
        }
        return true;
      
      }
      catch (Exception e) 
      {
        callbackContext.error("getpermission" + e.getMessage());
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

  private void SendTranscriptToClient(String EventName,String Transcript) {
    try 
    {
      JSONObject info = new JSONObject();
      if (!EventName.equals("")) {

        info.put("Event",EventName);
      }

      if (!Transcript.equals("")) {

        info.put("Data",Transcript);
      }
      
      this.SendRecognizerUpdate(info);
    } 
    catch (JSONException e) 
    {
      recognizerCallbackContext.error("SendTranscriptToClient, " + EventName + ":" + Transcript + ":" + e.getMessage());
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
