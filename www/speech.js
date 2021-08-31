function AzureSpeech() {}

// The function that passes work along to native shells
// Message is a string, duration may be 'long' or 'short'
AzureSpeech.prototype.test = function(message, successCallback, errorCallback) {
  var options = {};
  options.message = message;
  cordova.exec(successCallback, errorCallback, 'AzureSpeech', 'test', [options]);
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