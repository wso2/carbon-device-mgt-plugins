function onRequest(context) {
    var log = new Log("operation.js");
    var operationModule = require("/app/modules/operation.js").operationModule;
    var device = context.unit.params.device;
    log.info(device);
    var control_operations = operationModule.getControlOperations(device.type);
    return {"control_operations": control_operations, "device": device};
}