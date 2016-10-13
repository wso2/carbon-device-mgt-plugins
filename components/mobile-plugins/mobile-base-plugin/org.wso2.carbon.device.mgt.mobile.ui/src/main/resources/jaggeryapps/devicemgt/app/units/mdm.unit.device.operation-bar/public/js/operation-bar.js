/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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



/*
 * Setting-up global variables.
 */

var operations = '.wr-operations',
    modalPopup = '.modal',
    modalPopupContent = modalPopup + ' .modal-content',
    navHeight = $('#nav').height(),
    headerHeight = $('header').height(),
    offset = (headerHeight + navHeight),
    deviceSelection = '.device-select',
    platformTypeConstants = {
        "ANDROID": "android",
        "IOS": "ios",
        "WINDOWS": "windows"
    },
    ownershipTypeConstants = {
        "BYOD": "BYOD",
        "COPE": "COPE"
    },
    operationBarModeConstants = {
        "BULK": "BULK_OPERATION_MODE",
        "SINGLE": "SINGLE_OPERATION_MODE"
    };

var permittedOperations = [];

/*
 * Function to get selected devices ID's
 */
function getSelectedDeviceIds() {
    var deviceIdentifierList = [];
    $(deviceSelection).each(function (index) {
        var device = $(this);
        var deviceId = device.data('deviceid');
        var deviceType = device.data('type');
        deviceIdentifierList.push({
                                      "id": deviceId,
                                      "type": deviceType
                                  });
    });
    if (deviceIdentifierList.length == 0) {
        var thisTable = $(".DTTT_selected").closest('.dataTables_wrapper').find('.dataTable').dataTable();
        thisTable.api().rows().every(function () {
            if ($(this.node()).hasClass('DTTT_selected')) {
                var deviceId = $(thisTable.api().row(this).node()).data('deviceid');
                var deviceType = $(thisTable.api().row(this).node()).data('devicetype');
                deviceIdentifierList.push({
                                              "id": deviceId,
                                              "type": deviceType
                                          });
            }
        });
    }

    return deviceIdentifierList;
}

/*
 * On operation click function.
 * @param selection: Selected operation
 */
function operationSelect(selection) {
    var deviceIdList = getSelectedDeviceIds();
    if (deviceIdList == 0) {
        modalDialog.header("Operation cannot be performed !");
        modalDialog.content("Please select a device or a list of devices to perform an operation.");
        modalDialog.footer("<div class=\"buttons\"><a href=\"javascript:modalDialog.hide()\" class=\"btn-operations\">Ok</a> </div>");
        modalDialog.showAsError();
    } else {

        var selectedOperation;
        //Find the operation object
        for (var i=0; i<permittedOperations.length; i++) {
            if (permittedOperations[i].code === selection) {
                selectedOperation = permittedOperations[i];
                break;
            }
        }

        modalDialog.header(selectedOperation.name);

        modalDialog.icon(selectedOperation.icon);

        var commonContent = "<br><br>Do you want to perform this operation on selected device(s) ? <br>";

        //Customize content based on some special operation codes.
        if ((selectedOperation.code === "WIPE_DATA") &&
            (selectedOperation.deviceType === "android") &&
            (selectedOperation.ownership === "BYOD")) {

            modalDialog.content($("#BYODAndroidWipeData").html() + commonContent);

        } else if (selectedOperation.code === "NOTIFICATION" ) {
            modalDialog.content($("#Notification").html() + commonContent);

        } else if (selectedOperation.code === "CHANGE_LOCK_CODE") {
            modalDialog.content($("#ChangeLockCode").html() + commonContent);

        } else if ((selectedOperation.code === "DEVICE_LOCK") &&
            (selectedOperation.deviceType === "android")) {

            if (selectedOperation.ownership === "COPE") {
                modalDialog.content($("#COPEAndroidDeviceLock").html() + $("#AndroidDeviceLock").html() + commonContent);
            } else {
                modalDialog.content($("#AndroidDeviceLock").html() + commonContent);
            }

        } else if ( (selectedOperation.code === "UPGRADE_FIRMWARE") &&
            (selectedOperation.deviceType === "android" )) {

            modalDialog.content($("#AndroidUpgradeFirmware").html() + commonContent);

        } else {
            modalDialog.content(commonContent);
        }

        modalDialog.footer("<div class=\"buttons\"> <a href=\"javascript:runOperation('" +
                            selectedOperation.code + "')\" class=\"btn-operations\">Yes</a> " +
                            "<a href=\"javascript:modalDialog.hide()\" class=\"btn-operations btn-default\">No</a> </div>");

    }
    modalDialog.show();
}

function getDevicesByTypes(deviceList) {
    var deviceTypes = {};
    $.each(deviceList, function (index, item) {
        if (!deviceTypes[item.type]) {
            deviceTypes[item.type] = [];
        }
        if (item.type == platformTypeConstants.ANDROID ||
            item.type == platformTypeConstants.IOS || item.type == platformTypeConstants.WINDOWS) {
            deviceTypes[item.type].push(item.id);
        }
    });
    return deviceTypes;
}

//function unloadOperationBar() {
//    $("#showOperationsBtn").addClass("hidden");
//    $(".wr-operations").html("");
//}

function loadOperationBar(deviceType, ownership, mode) {
    var operationBar = $("#operations-bar");
    var operationBarSrc = operationBar.attr("src");

    $.template("operations-bar", operationBarSrc, function (template) {
        var serviceURL = "/api/device-mgt/v1.0/devices/" + deviceType + "/*/features";
        invokerUtil.get(
            serviceURL,
            // success callback
            function (data) {
                //var permittedOperations = [];
                var i;
                var permissionList = $("#operations-mod").data("permissions");
                var totalFeatures = JSON.parse(data);
                for (i = 0; i < permissionList[deviceType].length; i++) {
                    var j;
                    for (j = 0; j < totalFeatures.length; j++) {
                        if (permissionList[deviceType][i] == totalFeatures[j]["code"]) {
                            if (deviceType == platformTypeConstants.ANDROID) {
                                if (totalFeatures[j]["code"] == "DEVICE_UNLOCK") {
                                    if (ownership == ownershipTypeConstants.COPE) {
                                        permittedOperations.push(totalFeatures[j]);
                                    }
                                } else if (totalFeatures[j]["code"] == "WIPE_DATA") {
                                    if (mode == operationBarModeConstants.BULK) {
                                        if (ownership == ownershipTypeConstants.COPE) {
                                            permittedOperations.push(totalFeatures[j]);
                                        }
                                    } else {
                                        permittedOperations.push(totalFeatures[j]);
                                    }
                                } else {
                                    permittedOperations.push(totalFeatures[j]);
                                }
                            } else {
                                permittedOperations.push(totalFeatures[j]);
                            }
                        }
                    }
                }

                var viewModel = {};
                permittedOperations = permittedOperations.filter(function (current) {
                    var iconName;
                    switch (deviceType) {
                        case platformTypeConstants.ANDROID:
                            iconName = operationModule.getAndroidIconForFeature(current.code);
                            break;
                        case platformTypeConstants.WINDOWS:
                            iconName = operationModule.getWindowsIconForFeature(current.code);
                            break;
                        case platformTypeConstants.IOS:
                            iconName = operationModule.getIOSIconForFeature(current.code);
                            break;
                    }

                    /* adding ownership in addition to device-type
                     as it's vital in cases where UI for the same feature should change
                     according to ownership
                      */
                    if (ownership) {
                        current.ownership = ownership;
                    }

                    if (iconName) {
                        current.icon = iconName;
                    }

                    return current;
                });

                viewModel.features = permittedOperations;
                var content = template(viewModel);
                $(".wr-operations").html(content);
            },
            // error callback
            function (message) {
                $(".wr-operations").html(message);
            });
    });
}

function runOperation(operationName) {
    var deviceIdList = getSelectedDeviceIds();
    var list = getDevicesByTypes(deviceIdList);

    var successCallback = function (data) {
        if (operationName == "NOTIFICATION") {
            modalDialog.header("Message sent successfully !");
            modalDialog.content("Message has been queued to be sent to the device.");
        } else {
            modalDialog.header("Operation queued successfully !");
            modalDialog.content("Operation has been queued successfully to be sent to the device.");
        }
        modalDialog.show();
    };
    var errorCallback = function (data) {

        modalDialog.header("Operation cannot be performed !");
        modalDialog.content("Unexpected error occurred. Please Try again later.");
        modalDialog.showAsError();
    };

    var payload, serviceEndPoint;
    if (list[platformTypeConstants.IOS]) {
        payload =
            operationModule.generatePayload(platformTypeConstants.IOS, operationName, list[platformTypeConstants.IOS]);
        serviceEndPoint = operationModule.getIOSServiceEndpoint(operationName);
    } else if (list[platformTypeConstants.ANDROID]) {
        payload = operationModule
            .generatePayload(platformTypeConstants.ANDROID, operationName, list[platformTypeConstants.ANDROID]);
        serviceEndPoint = operationModule.getAndroidServiceEndpoint(operationName);
    } else if (list[platformTypeConstants.WINDOWS]) {
        payload = operationModule.generatePayload(platformTypeConstants.WINDOWS, operationName,
                                                  list[platformTypeConstants.WINDOWS]);
        serviceEndPoint = operationModule.getWindowsServiceEndpoint(operationName);
    }
    if (operationName == "NOTIFICATION") {
        var errorMsgWrapper = "#notification-error-msg";
        var errorMsg = "#notification-error-msg span";
        var messageTitle = $("#messageTitle").val();
        var messageText = $("#messageText").val();
        if (!(messageTitle && messageText)) {
            $(errorMsg).text("Enter a message. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else {
            invokerUtil.post(serviceEndPoint, payload, successCallback, errorCallback);
            //$(modalPopupContent).removeData();
            modalDialog.hide();
        }
    } else {
        invokerUtil.post(serviceEndPoint, payload, successCallback, errorCallback);
        //$(modalPopupContent).removeData();
        modalDialog.hide();
    }
}

/*
 * DOM ready functions.
 */
$(document).ready(function () {
    $(operations).show();
});
