function onRequest(context){
    var viewModel = {};
    var process = require("process");
    var serverIP = process.getProperty("carbon.local.ip");
    var serverPort = process.getProperty("carbon.https.port");
    viewModel.enrollmentURL = "https://"+serverIP+":"+serverPort+"/android_sense/manager/device/android_sense/download/";
    return viewModel;
}