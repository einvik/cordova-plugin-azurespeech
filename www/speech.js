function AzureSpeech() {}
AzureSpeech.prototype.Synthesize = function(SubscriptionKey, ServiceRegion, Message, successCallback, errorCallback) {
  var options = {};
  options.SubscriptionKey = SubscriptionKey;
  options.ServiceRegion = ServiceRegion;
  options.Message = Message;
  cordova.exec(successCallback, errorCallback, 'AzureSpeech', 'synthesize', [options]);
}
AzureSpeech.prototype.HasPermission = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'AzureSpeech', 'hasPermission', []);
}
AzureSpeech.prototype.GetPermission = function(successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'AzureSpeech', 'getPermission', []);
}
AzureSpeech.prototype.Recognize = function(SubscriptionKey, ServiceRegion, Message, successCallback, errorCallback) {
    var options = {};
    options.SubscriptionKey = SubscriptionKey;
    options.ServiceRegion = ServiceRegion;
    options.Message = Message;
    cordova.exec(successCallback, errorCallback, 'AzureSpeech', 'recognize', [options]);
  }
AzureSpeech.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.azureSpeech = new AzureSpeech();
  return window.plugins.azureSpeech;
};
cordova.addConstructor(AzureSpeech.install);