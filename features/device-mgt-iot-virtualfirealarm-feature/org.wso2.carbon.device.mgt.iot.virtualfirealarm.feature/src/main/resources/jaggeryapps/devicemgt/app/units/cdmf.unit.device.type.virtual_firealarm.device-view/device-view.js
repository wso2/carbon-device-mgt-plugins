function onRequest(context) {
    var log = new Log("device-view.js");
    var deviceType = context.uriParams.deviceType;
    var deviceId = request.getParameter("id");

    if (deviceType != null && deviceType != undefined && deviceId != null && deviceId != undefined) {
        var deviceModule = require("/app/modules/device.js").deviceModule;
        var device = deviceModule.viewDevice(deviceType, deviceId);
        log.info(device);
        if (device && device.status != "error") {
            return {"device": device};
        }
    }
}