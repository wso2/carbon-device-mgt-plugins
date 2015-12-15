var config_api= function(){
    var config_api_url = this;
    var domain="localhost";
    var port="9793";

    config_api_url.config_3dobject_holder="#canvasOne";
    config_api_url.config_3dobject_holder_width=325;
    config_api_url.config_3dobject_holder_height=220;
    config_api_url.realtime_plotting_update_interval = 30;
    config_api_url.realtime_plotting_totalPoints = 30;
    config_api_url.realtime_plotting_data_window ={};
    config_api_url.effectController={uy: 70.0, uz: 15.0, ux: 10.0, fx: 2.0, fz: 15.0, Tmax:1};
    var context_controller="org.wso2.devicemgt.drone.service/services/api/manager/device"
    config_api_url.drone_control="http://"+domain+":"+port+"/"+context_controller;
    config_api_url.drone_controlType="POST";
    config_api_url.drone_controlDataType = "json";
    config_api_url.maxEventLimit=10;
    config_api_url.calenderID="eventmanager47@gmail.com";
    config_api_url.xmpp_server="http://localhost:7070/http-bind/";
    config_api_url.web_socket_endpoint = "ws://localhost:9763/drone_analyzer/datastream/drone_status";
    config_api_url.modules_status={
        "realtimePlotting":false,
        "sensorReadings":false,
        "angleOfRotation_2":false,
        "angleOfRotation_1":false
    }
}