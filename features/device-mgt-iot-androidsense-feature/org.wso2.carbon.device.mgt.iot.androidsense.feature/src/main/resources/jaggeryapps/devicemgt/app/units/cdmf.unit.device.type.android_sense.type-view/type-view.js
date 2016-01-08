function onRequest(context){
    var viewModel = {};
    var process = require("process");
    var serverIP = process.getProperty("carbon.local.ip");
    var serverPort = process.getProperty("carbon.http.port");
    viewModel.enrollmentURL = "http://"+serverIP+":"+serverPort+"/android_sense_mgt/manager/device/android_sense/download/";
    return viewModel;
}