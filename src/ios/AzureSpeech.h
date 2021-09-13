#import <Cordova/CDVPlugin.h>

#import <AVFoundation/AVFoundation.h>
#import <MicrosoftCognitiveServicesSpeech/SPXSpeechApi.h>

@interface AzureSpeech : CDVPlugin
@property (nonatomic, retain) SPXSpeechRecognizer *speechRecognizer;
@property (nonatomic, strong) AVAudioPlayer *player;

- (void)hasPermission:(CDVInvokedUrlCommand*)command;
- (void)getPermission:(CDVInvokedUrlCommand*)command;
- (void)synthesize:(CDVInvokedUrlCommand*)command;
- (void)recognize:(CDVInvokedUrlCommand*)command;
- (void)stoprecognize:(CDVInvokedUrlCommand*)command;


@end