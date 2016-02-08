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

(function () {
    var deviceId = $(".device-id");
    var deviceIdentifier = deviceId.data("deviceid");
    var deviceType = deviceId.data("type");
    var payload = [deviceIdentifier];
    var operationTable;
    var serviceUrl = "/mdm-android-agent/operation/device-info";
    invokerUtil.post(serviceUrl, payload,
                     function (message) {
                         console.log(message);
                     }, function (message) {
                console.log(message);
            });
    $(document).ready(function () {
        $(".panel-body").removeClass("hidden");
        $("#loading-content").remove();
        loadApplicationsList();

        $("#refresh-apps").click(function () {
            $('#apps-spinner').removeClass('hidden');
            loadApplicationsList();
        });
        loadOperationBar(deviceType);
    });

    function loadApplicationsList() {
        var applicationsList = $("#applications-list");
        var deviceListingSrc = applicationsList.attr("src");
        var deviceId = applicationsList.data("device-id");
        var deviceType = applicationsList.data("device-type");

        $.template("application-list", deviceListingSrc, function (template) {
            var serviceURL = "/devicemgt_admin/operations/" + deviceType + "/" + deviceId + "/apps";

            var successCallback = function (data) {
                data = JSON.parse(data);
                $('#apps-spinner').addClass('hidden');
                var viewModel = {};
                if (data != null && data.length > 0) {
                    for (var i = 0; i < data.length; i++) {
                        data[i].name = data[i].name.replace(/[^\w\s]/gi, ' ');
                        data[i].name = data[i].name.replace(/[0-9]/g, ' ');
                    }
                }
                viewModel.applications = data;
                viewModel.deviceType = deviceType;
                if (data.length > 0) {
                    var content = template(viewModel);
                    $("#applications-list-container").html(content);
                }

            };
            invokerUtil.get(serviceURL,
                            successCallback, function (message) {
                        console.log(message);
                    });
        });
    }

}());
