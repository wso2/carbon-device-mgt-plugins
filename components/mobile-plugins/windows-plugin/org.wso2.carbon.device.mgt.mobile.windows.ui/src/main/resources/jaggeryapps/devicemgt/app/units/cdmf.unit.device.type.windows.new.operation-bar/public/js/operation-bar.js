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

/*
 * On operation click function.
 * @param selection: Selected operation
 */
function operationSelect(selection) {
    $(modalPopupContent).addClass("operation-data");
    $(modalPopupContent).html($(" .operation[data-operation-code=" + selection + "]").html());
    $(modalPopupContent).data("operation-code", selection);
    showPopup();
}

function submitForm(formId) {
    var form = $("#" + formId);
    var uri = form.attr("action");
    var deviceId = form.data("device-id");
    var contentType = form.data("content-type");
    var operationCode = form.data("operation-code");
    var uriencodedQueryStr = "";
    var uriencodedFormStr = "";
    var payload = {};
    form.find("input").each(function () {
        var input = $(this);
        if (input.data("param-type") == "path") {
            uri = uri.replace("{" + input.attr("id") + "}", input.val());
        } else if (input.data("param-type") == "query") {
            var prefix = (uriencodedQueryStr == "") ? "?" : "&";
            uriencodedQueryStr += prefix + input.attr("id") + "=" + input.val();
        } else if (input.data("param-type") == "form") {
            var prefix = (uriencodedFormStr == "") ? "" : "&";
            uriencodedFormStr += prefix + input.attr("id") + "=" + input.val();
            if(input.attr("type") == "text"){
                payload[input.attr("id")] = input.val();
            } else if(input.attr("type") == "checkbox"){
                payload[input.attr("id")] = input.is(":checked");
            }
        }
    });
    uri += uriencodedQueryStr;
    var httpMethod = form.attr("method").toUpperCase();
    //var contentType = form.attr("enctype");

    if (contentType == undefined || contentType == "") {
        contentType = "application/x-www-form-urlencoded";
        payload = uriencodedFormStr;
    }

    //setting responses callbacks
    var defaultStatusClasses = "fw fw-stack-1x";
    var content = $("#operation-response-template").find(".content");
    var title = content.find("#title");
    var statusIcon = content.find("#status-icon");
    var description = content.find("#description");
    description.html("");
    var successCallBack = function (response) {
        var res = response;
        try {
            res = JSON.parse(response).messageFromServer;
        } catch (err) {
            //do nothing
        }
        title.html("Operation Triggered!");
        statusIcon.attr("class", defaultStatusClasses + " fw-check");
        description.html(res);
        console.log("success!");
        $(modalPopupContent).html(content.html());
    };
    var errorCallBack = function (response) {
        console.log(response);
        title.html("An Error Occurred!");
        statusIcon.attr("class", defaultStatusClasses + " fw-error");
        var reason = (response.responseText == "null")?response.statusText:response.responseText;
        try {
            reason = JSON.parse(reason).message;
        } catch (err) {
            //do nothing
        }
        description.html(reason);
        console.log("Error!");
        $(modalPopupContent).html(content.html());
    };
    //executing http request
    if (httpMethod == "GET") {
        invokerUtil.get(uri, successCallBack, errorCallBack, contentType);
    } else if (httpMethod == "POST") {
        var deviceList = [deviceId];
        payload = generatePayload(operationCode, payload, deviceList);
        invokerUtil.post(uri, payload, successCallBack, errorCallBack, contentType);
    } else if (httpMethod == "PUT") {
        invokerUtil.put(uri, payload, successCallBack, errorCallBack, contentType);
    } else if (httpMethod == "DELETE") {
        invokerUtil.delete(uri, successCallBack, errorCallBack, contentType);
    } else {
        title.html("An Error Occurred!");
        statusIcon.attr("class", defaultStatusClasses + " fw-error");
        description.html("This operation requires http method: " + httpMethod + " which is not supported yet!");
        $(modalPopupContent).html(content.html());
    }
}

$(document).on('submit', 'form', function (e) {
    cosole.log("darn!!");
    e.preventDefault();
    var postOperationRequest = $.ajax({
        url: $(this).attr("action") + '&' + $(this).serialize(),
        method: "post"
    });

    var btnSubmit = $('#btnSend', this);
    btnSubmit.addClass('hidden');

    var lblSending = $('#lblSending', this);
    lblSending.removeClass('hidden');

    var lblSent = $('#lblSent', this);
    postOperationRequest.done(function (data) {
        lblSending.addClass('hidden');
        lblSent.removeClass('hidden');
        setTimeout(function () {
            hidePopup();
        }, 3000);
    });

    postOperationRequest.fail(function (jqXHR, textStatus) {
        lblSending.addClass('hidden');
        lblSent.addClass('hidden');
    });
});

// Constants to define operation types available
var operationTypeConstants = {
    "PROFILE": "profile",
    "CONFIG": "config",
    "COMMAND": "command"
};


var generatePayload = function (operationCode, operationData, deviceList) {
    var payload;
    var operationType;
    switch (operationCode) {
        case windowsOperationConstants["CAMERA_OPERATION_CODE"]:
            operationType = operationTypeConstants["PROFILE"];
            payload = {
                "operation": {
                    "enabled": operationData["cameraEnabled"]
                }
            };
            break;
        case windowsOperationConstants["CHANGE_LOCK_CODE_OPERATION_CODE"]:
            operationType = operationTypeConstants["PROFILE"];
            payload = {
                "operation": {
                    "lockCode": operationData["lockCode"]
                }
            };
            break;
        case windowsOperationConstants["ENCRYPT_STORAGE_OPERATION_CODE"]:
            operationType = operationTypeConstants["PROFILE"];
            payload = {
                "operation": {
                    "encrypted": operationData["encryptStorageEnabled"]
                }
            };
            break;
        case windowsOperationConstants["NOTIFICATION_OPERATION_CODE"]:
            operationType = operationTypeConstants["PROFILE"];
            payload = {
                "operation": {
                    "message": operationData["message"]
                }
            };
            break;
        case windowsOperationConstants["PASSCODE_POLICY_OPERATION_CODE"]:
            operationType = operationTypeConstants["PROFILE"];
            payload = {
                "operation": {
                    "allowSimple": operationData["passcodePolicyAllowSimple"],
                    "requireAlphanumeric": operationData["passcodePolicyRequireAlphanumeric"],
                    "minLength": operationData["passcodePolicyMinLength"],
                    "minComplexChars": operationData["passcodePolicyMinComplexChars"],
                    "maxPINAgeInDays": operationData["passcodePolicyMaxPasscodeAgeInDays"],
                    "pinHistory": operationData["passcodePolicyPasscodeHistory"],
                    "maxFailedAttempts": operationData["passcodePolicyMaxFailedAttempts"]
                }
            };
            break;
        default:
            // If the operation is neither of above, it is a command operation
            operationType = operationTypeConstants["COMMAND"];
            // Operation payload of a command operation is simply an array of device IDs
            payload = deviceList;
    }

    if (operationType == operationTypeConstants["PROFILE"] && deviceList) {
        payload["deviceIDs"] = deviceList;
    }

    return payload;
};


// Constants to define Windows Operation Constants
var windowsOperationConstants = {
    "PASSCODE_POLICY_OPERATION_CODE": "PASSCODE_POLICY",
    "CAMERA_OPERATION_CODE": "CAMERA",
    "ENCRYPT_STORAGE_OPERATION_CODE": "ENCRYPT_STORAGE",
    "NOTIFICATION_OPERATION_CODE": "NOTIFICATION",
    "CHANGE_LOCK_CODE_OPERATION_CODE": "CHANGE_LOCK_CODE"
};