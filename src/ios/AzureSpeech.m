
#import "AzureSpeech.h"
#import <Cordova/CDVPlugin.h>

@implementation Echo

- (void)hasPermission:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* echo = @"Hei, du har min tillatelse.";
    NSLog(@"%@",echo);
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end