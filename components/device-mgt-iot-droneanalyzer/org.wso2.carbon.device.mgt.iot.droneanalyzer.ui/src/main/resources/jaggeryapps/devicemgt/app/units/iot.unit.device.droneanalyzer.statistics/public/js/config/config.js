/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var config_api = function () {
    var api = this;
    var context_controller = "/drone_analyzer/controller/send_command";
    api.config_3dobject_holder = "#virtualDrone";
    api.realtime_plotting_update_interval = 30;
    api.realtime_plotting_totalPoints = 30;
    api.realtime_plotting_data_window = {};
    api.effectController = {uy: 70.0, uz: 15.0, ux: 10.0, fx: 2.0, fz: 15.0, Tmax: 1};
    api.drone_control = context_controller;
    api.drone_controlType = "POST";
    api.drone_controlDataType = "json";
    api.web_socket_endpoint = "/drone_analyzer/datastream/";
    api.modules_status = {
        "realtimePlotting": false,
        "sensorReadings": false,
        "angleOfRotation_2": false,
        "angleOfRotation_1": false
    };
};