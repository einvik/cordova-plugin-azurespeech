
#import "AzureSpeech.h"

@implementation AzureSpeech

- (void)hasPermission:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* echo = @"Hei, du har min tillatelse.";
    NSLog(@"%@",echo);
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (BOOL)HasMicPermission
  {
    BOOL hasPermission = FALSE;
    if ([[AVAudioSession sharedInstance] recordPermission] == AVAudioSessionRecordPermissionGranted) {
      hasPermission = TRUE;
    }
    CDVPluginResult* result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:hasPermission];
    [result setKeepCallbackAsBool:NO];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
  }

@end