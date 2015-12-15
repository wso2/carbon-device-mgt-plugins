var config_api= function(){
    var config_api = this;
    var domain="localhost";
    var port="9793";
    var context_controller="/drone_analyzer/api/manager/device";
    config_api.config_3dobject_holder="#virtualDrone";
    config_api.realtime_plotting_update_interval = 30;
    config_api.realtime_plotting_totalPoints = 30;
    config_api.realtime_plotting_data_window ={};
    config_api.effectController={uy: 70.0, uz: 15.0, ux: 10.0, fx: 2.0, fz: 15.0, Tmax:1};
    config_api.drone_control="http://"+domain+":"+port+"/"+context_controller;
    config_api.drone_controlType="POST";
    config_api.drone_controlDataType = "json";
    config_api.xmpp_server="http://localhost:7070/http-bind/";
    config_api.web_socket_endpoint = "ws://localhost:9763/drone_analyzer/datastream/drone_status";
    config_api.modules_status={
                            "realtimePlotting":false,
                            "sensorReadings":false,
                            "angleOfRotation_2":false,
                            "angleOfRotation_1":false
    }
}