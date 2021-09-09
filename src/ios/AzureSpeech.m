
#import "AzureSpeech.h"

@implementation AzureSpeech

- (void)hasPermission:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:[HasMicPermission]];
    [pluginResult setKeepCallbackAsBool:NO];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (BOOL)HasMicPermission
  {
    BOOL hasPermission = FALSE;
    if ([[AVAudioSession sharedInstance] recordPermission] == AVAudioSessionRecordPermissionGranted) {
      hasPermission = TRUE;
    }
    return hasPermission;
  }

@end