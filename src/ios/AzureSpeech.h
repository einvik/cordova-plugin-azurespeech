#import <Cordova/CDVPlugin.h>

#import <AVFoundation/AVFoundation.h>

@interface AzureSpeech : CDVPlugin

- (void)hasPermission:(CDVInvokedUrlCommand*)command;
- (void)getPermission:(CDVInvokedUrlCommand*)command;
- (void)synthesize:(CDVInvokedUrlCommand*)command;
- (void)recognize:(CDVInvokedUrlCommand*)command;
- (void)stoprecognize:(CDVInvokedUrlCommand*)command;


@end