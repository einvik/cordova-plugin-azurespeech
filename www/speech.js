function AzureSpeech() {}

AzureSpeech.prototype.HasPermission = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'AzureSpeech', 'hasPermission', []);
}
AzureSpeech.prototype.GetPermission = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'AzureSpeech', 'getPermission', []);
}

AzureSpeech.prototype.Synthesize = function(SubscriptionKey, ServiceRegion, Message, successCallback, errorCallback) {
  var options = {};
  options.SubscriptionKey = SubscriptionKey;
  options.ServiceRegion = ServiceRegion;
  options.Message = Message;
  cordova.exec(successCallback, errorCallback, 'AzureSpeech', 'synthesize', [options]);
}

AzureSpeech.prototype.Recognize = function(SubscriptionKey, ServiceRegion, successCallback, errorCallback) {
  var options = {};
  options.SubscriptionKey = SubscriptionKey;
  options.ServiceRegion = ServiceRegion;
  
  cordova.exec(successCallback, errorCallback, 'AzureSpeech', 'recognize', [options]);
}
AzureSpeech.prototype.StopRecognize = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'AzureSpeech', 'stoprecognize', []);
}
AzureSpeech.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.azureSpeech = new AzureSpeech();
  return window.plugins.azureSpeech;
};
cordova.addConstructor(AzureSpeech.install);