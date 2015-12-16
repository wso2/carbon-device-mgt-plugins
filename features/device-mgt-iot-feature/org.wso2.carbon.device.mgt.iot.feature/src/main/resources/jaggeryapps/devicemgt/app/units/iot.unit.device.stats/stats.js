function onRequest(context) {
    var log = new Log("stats.js");
    var operationModule = require("/app/modules/operation.js").operationModule;
    var device = context.unit.params.device;
    log.info(device);
    var monitor_operations = JSON.stringify(operationModule.getMonitorOperations(device.type));
    return {"monitor_operations": monitor_operations, "device": device};
}