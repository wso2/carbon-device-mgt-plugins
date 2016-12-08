/*
 * Copyright (c)  2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var ov = ov || {};
ov.chart = null;
ov.polling_task = null;
ov.data = [];
ov.filter_context = null;
ov.filters_meta = {};
ov.filters = [];
ov.filter_prefix = "g_";
ov.selected_filter_groups = [];
ov.force_fetch = false;
ov.freeze = false;

ov.initialize = function () {
    ov.startPolling();
};


ov.startPolling = function () {
    setTimeout(function () {
        ov.fetch();
    }, 500);
    //noinspection JSUnusedGlobalSymbols
    this.polling_task = setInterval(function () {
        ov.fetch();
    }, gadgetConfig.polling_interval);
};

ov.fetch = function () {
    ov.data.length = 0;

    //noinspection JSUnresolvedVariable
    wso2.gadgets.XMLHttpRequest.get(
        gadgetConfig.source,
        function (response) {
            // console.log(JSON.stringify(response));
            if (Object.prototype.toString.call(response) === '[object Array]' && response.length === 2) {
                var totalDeviceCountData = response[0]["data"];
                if (totalDeviceCountData && totalDeviceCountData.length > 0) {
                    //noinspection JSUnresolvedVariable
                    var totalDeviceCount = totalDeviceCountData[0].deviceCount;
                    if (totalDeviceCount > 0) {
                        var totalDeviceCountDivElm = "#TOTAL";
                        var totalDeviceCountElm = "#deviceCount";
                        $(totalDeviceCountElm).html(totalDeviceCount.toString());
                        $(totalDeviceCountDivElm).attr("onclick", "ov.onclick('total')");
                        $(totalDeviceCountDivElm).css("cursor", "pointer");
                        var data = response[1]["data"];
                        if (data && data.length > 0) {
                            ov.filter_context = response[1]["groupingAttribute"];
                            var zeroDeviceCountDivElms = [
                                "#ACTIVE", "#UNREACHABLE",
                                "#INACTIVE", "#REMOVED"
                            ];
                            var zeroDeviceCountElms = [
                                "#activeDevices", "#unreachableDevices",
                                "#inactiveDevices", "#removedDevices"
                            ];
                            var zeroDeviceCountPercentageElms = [
                                "activeDevicesProgress", "unreachableDevicesProgress",
                                "inactiveDevicesProgress", "removedDevicesProgress"
                            ];
                            for (var i = 0; i < data.length; i++) {
                                var deviceCountElm, deviceCountDivElm, deviceCountPercentageElm;
                                if (data[i].group == "ACTIVE") {
                                    deviceCountElm = "#activeDevices";
                                    deviceCountDivElm = "#ACTIVE";
                                    deviceCountPercentageElm = "activeDevicesProgress";
                                } else if (data[i].group == "UNREACHABLE") {
                                    deviceCountElm = "#unreachableDevices";
                                    deviceCountDivElm = "#UNREACHABLE";
                                    deviceCountPercentageElm = "unreachableDevicesProgress";
                                } else if (data[i].group == "INACTIVE") {
                                    deviceCountElm = "#inactiveDevices";
                                    deviceCountDivElm = "#INACTIVE";
                                    deviceCountPercentageElm = "inactiveDevicesProgress";
                                } else if (data[i].group == "REMOVED") {
                                    deviceCountElm = "#removedDevices";
                                    deviceCountDivElm = "#REMOVED";
                                    deviceCountPercentageElm = "removedDevicesProgress";
                                }
                                //noinspection JSUnresolvedVariable
                                var deviceCount = data[i].deviceCount;
                                if (deviceCount > 0) {
                                    if (deviceCount > 999) {
                                        $(deviceCountElm).html("999<sup>+</sup>");
                                    } else {
                                        $(deviceCountElm).html(deviceCount.toString());
                                    }
                                    $(deviceCountDivElm).css("cursor", "pointer");
                                    $(deviceCountDivElm).attr("onclick", "ov.onclick('" + data[i].group + "')");
                                } else {
                                    $(deviceCountElm).html(deviceCount.toString());
                                    $(deviceCountDivElm).css("cursor", "default");
                                    $(deviceCountDivElm).removeAttr("onclick");
                                }
                                // updating count as a percentage
                                document.getElementById(deviceCountPercentageElm).style.width =
                                    (deviceCount * 100 / totalDeviceCount) + '%';
                                // removing connectivity-status-groups with non-zero device counts
                                zeroDeviceCountElms.
                                    splice(zeroDeviceCountElms.indexOf(deviceCountElm), 1);
                                zeroDeviceCountDivElms.
                                    splice(zeroDeviceCountDivElms.indexOf(deviceCountDivElm), 1);
                                zeroDeviceCountPercentageElms.
                                    splice(zeroDeviceCountPercentageElms.indexOf(deviceCountPercentageElm), 1);
                            }
                            // refreshing zero-device-count-connectivity-status-groups with zero values and zero percentages
                            for (var j = 0; j < zeroDeviceCountElms.length; j++) {
                                $(zeroDeviceCountElms[j]).html(0);
                                $(zeroDeviceCountDivElms[j]).css("cursor", "default");
                                document.getElementById(zeroDeviceCountPercentageElms[j]).style.width = '0%';
                            }
                        }
                    } else {
                        $("#TOTAL").css("cursor", "default");
                        $("#ACTIVE").css("cursor", "default");
                        $("#UNREACHABLE").css("cursor", "default");
                        $("#INACTIVE").css("cursor", "default");
                        $("#REMOVED").css("cursor", "default");
                    }
                }
            } else {
                console.error("Invalid response structure found: " + JSON.stringify(response));
            }
        }, function () {
            console.warn("Error accessing source for : " + gadgetConfig.id);
        });
};

ov.onclick = function (filterGroup) {
    var url;
    if(filterGroup != ""){
        url = getBaseURL() + "devices?g_" + ov.filter_context + "=" + filterGroup;
    } else {
        url = getBaseURL() + "devices";
    }
    window.open(url);
};

$(document).ready(function () {
    ov.initialize();
});
