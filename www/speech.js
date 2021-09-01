function AzureSpeech() {}

// The function that passes work along to native shells
// Message is a string, duration may be 'long' or 'short'
AzureSpeech.prototype.Synthesize = function(SubscriptionKey, ServiceRegion, Message, successCallback, errorCallback) {
  var options = {};
  options.SubscriptionKey = SubscriptionKey;
  options.ServiceRegion = ServiceRegion;
  options.Message = Message;
  cordova.exec(successCallback, errorCallback, 'AzureSpeech', 'synthesize', [options]);
}

AzureSpeech.prototype.Recognize = function(SubscriptionKey, ServiceRegion, Message, successCallback, errorCallback) {
    var options = {};
    options.SubscriptionKey = SubscriptionKey;
    options.ServiceRegion = ServiceRegion;
    options.Message = Message;
    cordova.exec(successCallback, errorCallback, 'AzureSpeech', 'recognize', [options]);
  }

// Installation constructor that binds ToastyPlugin to window
AzureSpeech.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.azureSpeech = new AzureSpeech();
  return window.plugins.azureSpeech;
};
cordova.addConstructor(AzureSpeech.install);