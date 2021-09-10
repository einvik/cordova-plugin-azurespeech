
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
NSArray *a = @[@"a", @"b"];
NSLog(@"NSArray = %@", [a class]);
// NSArray = __NSArrayI

NSMutableArray *b = [[NSMutableArray alloc] initWithArray:a];
NSLog(@"NSMutableArray = %@", [b class]);
// NSMutableArray = __NSArrayM

NSString *c = @"imastring";
NSLog(@"NSString = %@", [c class]);
// NSString = __NSCFConstantString

NSNumber *d = [[NSNumber alloc] initWithInteger:5];
NSLog(@"NSNumber = %@", [d class]);

      CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:result];
    [pluginResult setKeepCallbackAsBool:NO];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end