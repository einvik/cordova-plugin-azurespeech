
#import "AzureSpeech.h"
#import <MicrosoftCognitiveServicesSpeech/SPXSpeechApi.h>

@implementation AzureSpeech
@synthesize speechRecognizer;

  - (void)hasPermission:(CDVInvokedUrlCommand*)command
  {
        BOOL hasPermission = FALSE;
      if ([[AVAudioSession sharedInstance] recordPermission] == AVAudioSessionRecordPermissionGranted) {
        hasPermission = TRUE;
      }
      CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:hasPermission];
      [pluginResult setKeepCallbackAsBool:NO];
      [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }

  - (void)getPermission:(CDVInvokedUrlCommand*)command 
  {
    [[AVAudioSession sharedInstance] requestRecordPermission:^(BOOL hasPermission) {
          CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:hasPermission];
          [pluginResult setKeepCallbackAsBool:NO];
          [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
  }

  - (void)synthesize:(CDVInvokedUrlCommand*)command 
  {
    NSString *result = [command.arguments componentsJoinedByString:@","];
    NSDictionary* options = command.arguments[0];
    NSString *Message = options[@"Message"];
    NSString *ServiceRegion = options[@"ServiceRegion"];
    NSString *SubscriptionKey = options[@"SubscriptionKey"];

    SPXSpeechConfiguration *speechConfig = [[SPXSpeechConfiguration alloc] initWithSubscription:SubscriptionKey region:ServiceRegion];
    [speechConfig setSpeechSynthesisOutputFormat:SPXSpeechSynthesisOutputFormat_Audio16Khz32KBitRateMonoMp3];
    SPXSpeechSynthesizer *speechSynthesizer = [[SPXSpeechSynthesizer alloc] initWithSpeechConfiguration:speechConfig audioConfiguration:nil];
    SPXSpeechSynthesisResult *speechResult = [speechSynthesizer speakText:Message];
    CDVPluginResult* pluginResult = nil;
    if (SPXResultReason_Canceled == speechResult.reason) 
    {
      SPXSpeechSynthesisCancellationDetails *details = [[SPXSpeechSynthesisCancellationDetails alloc] initFromCanceledSynthesisResult:speechResult];
      NSLog(@"Speech synthesis was canceled: %@. Did you pass the correct key/region combination?", details.errorDetails);
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:NO];

    } 
    else if (SPXResultReason_SynthesizingAudioCompleted == speechResult.reason) 
    {
        NSLog(@"Speech synthesis was completed");
        self.player = [[AVAudioPlayer alloc] initWithData:[speechResult audioData] error:nil];
        [self.player prepareToPlay];
        [self.player play];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:YES];
    } 
    else 
    {
        NSLog(@"There was an error.");
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:NO];
    }
    [pluginResult setKeepCallbackAsBool:NO];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }

  - (void)recognize:(CDVInvokedUrlCommand*)command 
  {

     __block CDVPluginResult* pluginResult = nil;

    NSDictionary* options = command.arguments[0];
    // NSString *Message = options[@"Message"];
    NSString *ServiceRegion = options[@"ServiceRegion"];
    NSString *SubscriptionKey = options[@"SubscriptionKey"];
    SPXSpeechConfiguration *speechConfig = [[SPXSpeechConfiguration alloc] initWithSubscription:SubscriptionKey region:ServiceRegion];
    if (!speechConfig) {
        NSLog(@"Could not load speech config");
        // [self updateRecognitionErrorText:(@"Speech Config Error")];
        return;
    }

    speechRecognizer = [[SPXSpeechRecognizer alloc] init:speechConfig];
    if (!speechRecognizer) {
      NSLog(@"Could not create speech recognizer");
        return;
    }
    // connect callbacks
    [speechRecognizer addRecognizingEventHandler: ^ (SPXSpeechRecognizer *recognizer, SPXSpeechRecognitionEventArgs *eventArgs) {
      NSLog(@"Received intermediate result event. SessionId: %@, recognition result:%@. Status %ld. offset %llu duration %llu resultid:%@", eventArgs.sessionId, eventArgs.result.text, (long)eventArgs.result.reason, eventArgs.result.offset, eventArgs.result.duration, eventArgs.result.resultId);
      NSDictionary *Return = [[NSDictionary alloc] initWithObjectsAndKeys:@"recognizing",@"Event", eventArgs.result.text,@"Data", nil];
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:Return];
      [pluginResult setKeepCallbackAsBool:YES];
      [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];

    [speechRecognizer addRecognizedEventHandler: ^ (SPXSpeechRecognizer *recognizer, SPXSpeechRecognitionEventArgs *eventArgs) {
      NSLog(@"Received final result event. SessionId: %@, recognition result:%@. Status %ld. offset %llu duration %llu resultid:%@", eventArgs.sessionId, eventArgs.result.text, (long)eventArgs.result.reason, eventArgs.result.offset, eventArgs.result.duration, eventArgs.result.resultId);
      NSDictionary *Return = [[NSDictionary alloc] initWithObjectsAndKeys:@"recognized",@"Event", eventArgs.result.text,@"Data", nil];
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:Return];
      [pluginResult setKeepCallbackAsBool:YES];
      [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];

    __block bool end = false;
    [speechRecognizer addSessionStoppedEventHandler: ^ (SPXRecognizer *recognizer, SPXSessionEventArgs *eventArgs) {
        NSLog(@"Received session stopped event. SessionId: %@", eventArgs.sessionId);
        [speechRecognizer stopContinuousRecognition];
    }];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:TRUE];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    [speechRecognizer startContinuousRecognition];
}


  - (void)stoprecognize:(CDVInvokedUrlCommand*)command 
  {
    NSLog(@"Stopping recognition");
    [speechRecognizer stopContinuousRecognition];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:TRUE];
    NSDictionary *Return = [[NSDictionary alloc] initWithObjectsAndKeys:@"",@"Event", @"Stopping recognition",@"Data", nil];

    [pluginResult setKeepCallbackAsBool:NO];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end