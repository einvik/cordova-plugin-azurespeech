
#import "AzureSpeech.h"
// #import <MicrosoftCognitiveServicesSpeech/SPXSpeechApi.h>

@implementation AzureSpeech

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
  // NSString *speechKey = @"asdfasdf";
  // NSString *serviceRegion = @"northeurope";
  // SPXSpeechConfiguration *speechConfig = [[SPXSpeechConfiguration alloc] initWithSubscription:speechKey region:serviceRegion];
  // NSString * result = [[command.arguments valueForKey:@"description"] componentsJoinedByString:@""];
NSString *result = [command.arguments componentsJoinedByString:@","];
NSLog(@"result = %@", result);
NSLog(@"result = %@", [result class]);
NSDictionary* options = command.arguments[0];
NSLog(@"Message = %@", options[@"Message"]);

NSString *Message = options[@"Message"];
NSString *ServiceRegion = options[@"ServiceRegion"];
NSString *SubscriptionKey = options[@"SubscriptionKey"];

// id json = [NSJSONSerialization JSONObjectWithData:result options:0 error:nil];
    SPXSpeechConfiguration *speechConfig = [[SPXSpeechConfiguration alloc] initWithSubscription:SubscriptionKey region:ServiceRegion];
    [speechConfig setSpeechSynthesisOutputFormat:SPXSpeechSynthesisOutputFormat_Audio16Khz32KBitRateMonoMp3];
    SPXSpeechSynthesizer *speechSynthesizer = [[SPXSpeechSynthesizer alloc] initWithSpeechConfiguration:speechConfig audioConfiguration:nil];
    SPXSpeechSynthesisResult *speechResult = [speechSynthesizer speakText:Message];
  if (SPXResultReason_Canceled == speechResult.reason) {
        SPXSpeechSynthesisCancellationDetails *details = [[SPXSpeechSynthesisCancellationDetails alloc] initFromCanceledSynthesisResult:speechResult];
        NSLog(@"Speech synthesis was canceled: %@. Did you pass the correct key/region combination?", details.errorDetails);
        // [self updateText:[NSString stringWithFormat:@"Speech synthesis was canceled: %@. Did you pass the correct key/region combination?", details.errorDetails] color:UIColor.redColor];
      CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:NO];

    } else if (SPXResultReason_SynthesizingAudioCompleted == speechResult.reason) {
        NSLog(@"Speech synthesis was completed");
        // Play audio.
        self.player = [[AVAudioPlayer alloc] initWithData:[speechResult audioData] error:nil];
        [self.player prepareToPlay];
        [self.player play];
              CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:YES;

    } else {
        NSLog(@"There was an error.");
              CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:no;

    }
    [pluginResult setKeepCallbackAsBool:NO];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end