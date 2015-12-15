
var flight_dynamics = function(){
    var api = this;
    api.processingMessage = function(sender_message){
        if (sender_message.quatanium_val != undefined) {
            current_status = object_maker.get_heading_attitude_bank(sender_message.quatanium_val);
            //console.log(JSON.stringify(current_status));
            object_maker.set_heading_attitude_bank(current_status);
        }
        if (config_api.modules_status.angleOfRotation_2 || config_api.modules_status.angleOfRotation_1) {;
            object_maker.set_bank("#imageTop", current_status.bank);
            object_maker.set_heading("#imageBackSecond", current_status.heading);
        }
        if (config_api.modules_status.realtimePlotting) {

            if(current_status[$('#plotting_attribute').val()] != undefined){

                plotting.pushData(current_status[$('#plotting_attribute').val()]);
            }
        }
        if (config_api.modules_status.sensorReadings) {
            if (sender_message.accelerometer != undefined) {
                $("#z_acc").html(sender_message.accelerometer[0]);
                $("#pitch_acc").html(sender_message.accelerometer[1]);
                $("#yaw_acc").html(sender_message.accelerometer[2]);
            }
            if (sender_message.gyroscope != undefined) {
                $("#roll_gyo").html(sender_message.gyroscope[0]);
                $("#pitch_gyo").html(sender_message.gyroscope[1]);
                $("#yaw_gyo").html(sender_message.gyroscope[2]);
            }
            if (sender_message.magnetometer != undefined) {
                $("#roll_mag").html(sender_message.magnetometer[0]);
                $("#pitch_mag").html(sender_message.magnetometer[1]);
                $("#yaw_mag").html(sender_message.magnetometer[2]);
            }
            if (sender_message.basicPrm != undefined) {
                $("#altitude_basic").html(sender_message.basicParam[0]);
                $("#velocity_basic").html(sender_message.basicParam[1]);
            }
        }
    }

};




//$('#showBasicParam a:first').tab('show');
/* $('#showBasicParam a').click(function (e) {
        e.preventDefault();

        $(this).addClass("active");
        $("#Gyroscope").show();
 });*/

/*

 $("#module_control button").click(function(index) {
 console.log("Asking Server to send the "+$(this).attr('id')+" command to Ar Drone");
 ajax_handler.ajaxRequest(config_api.drone_control, config_api.drone_controlType, {action:$(this).attr('id'),speed:7,duration:7},
 config_api.drone_controlDataType, function(data, status){
 console.log(JSON.stringify(data));
 }
 );
 });*/

