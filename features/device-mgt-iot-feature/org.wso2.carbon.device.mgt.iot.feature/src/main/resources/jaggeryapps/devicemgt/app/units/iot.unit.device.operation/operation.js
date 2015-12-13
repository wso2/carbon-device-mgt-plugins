function onRequest(context) {
    var log = new Log("iot-operation.js");
    var operationModule = require("/modules/operation.js").operationModule;
    var device = context.unit.params.device;
    var control_operations = operationModule.getControlOperations(device.type);
    return {"control_operations": control_operations, "device": device};
}