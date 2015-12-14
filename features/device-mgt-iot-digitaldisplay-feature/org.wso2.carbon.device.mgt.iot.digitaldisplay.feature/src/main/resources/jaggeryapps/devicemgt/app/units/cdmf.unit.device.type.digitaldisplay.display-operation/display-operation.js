function onRequest (context) {

    var operationModule = require("../modules/operation.js").operationModule;
    var control_operations = operationModule.getControlOperations("digital_display");
    var monitor_operations = JSON.stringify(operationModule.getMonitorOperations("digital_display"));

    context.control_operations = control_operations;
    context.monitor_operations = monitor_operations;

    return context;
}