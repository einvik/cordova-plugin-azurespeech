#import <Cordova/CDVPlugin.h>

@interface AzureSpeech : CDVPlugin

- (void)hasPermission:(CDVInvokedUrlCommand*)command;

@end