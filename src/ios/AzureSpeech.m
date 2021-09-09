
#import "AzureSpeech.h"

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
  CDVPluginResult* pluginResult = nil;
  [[AVAudioSession sharedInstance] requestRecordPermission:^(BOOL hasPermission) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:hasPermission];
        [result setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }];
}

@end