/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var windowsOperationModule = function () {
    var publicMethods = {};
    var privateMethods = {};

    // Constants to define operation types available
    var operationTypeConstants = {
        "PROFILE": "profile",
        "CONFIG": "config",
        "COMMAND": "command"
    };

    // Constants to define Windows Operation Constants
    var windowsOperationConstants = {
        "PASSCODE_POLICY_OPERATION_CODE": "PASSCODE_POLICY",
        "CAMERA_OPERATION_CODE": "CAMERA",
        "ENCRYPT_STORAGE_OPERATION_CODE": "ENCRYPT_STORAGE",
        "NOTIFICATION_OPERATION_CODE": "NOTIFICATION",
        "CHANGE_LOCK_CODE_OPERATION_CODE": "CHANGE_LOCK_CODE"
    };

    /**
     * Convert the windows platform specific code to the generic payload.
     * TODO: think of the possibility to follow a pattern to the key name (namespace?)
     * @param operationCode
     * @param operationPayload
     * @returns {{}}
     */
    privateMethods.generateGenericPayloadFromWindowsPayload = function (operationCode, operationPayload) {
        var payload = {};
        operationPayload = JSON.parse(operationPayload);
        switch (operationCode) {
            case windowsOperationConstants["PASSCODE_POLICY_OPERATION_CODE"]:
                payload = {
                    "passcodePolicyAllowSimple": operationPayload["allowSimple"],
                    "passcodePolicyRequireAlphanumeric": operationPayload["requireAlphanumeric"],
                    "passcodePolicyMinLength": operationPayload["minLength"],
                    "passcodePolicyMinComplexChars": operationPayload["minComplexChars"],
                    "passcodePolicyMaxPasscodeAgeInDays": operationPayload["maxPINAgeInDays"],
                    "passcodePolicyPasscodeHistory": operationPayload["pinHistory"],
                    "passcodePolicyMaxFailedAttempts": operationPayload["maxFailedAttempts"]
                };
                break;
            case windowsOperationConstants["CAMERA_OPERATION_CODE"]:
                payload = {
                    "cameraEnabled": operationPayload["enabled"]
                };
                break;
            case windowsOperationConstants["ENCRYPT_STORAGE_OPERATION_CODE"]:
                payload = {
                    "encryptStorageEnabled": operationPayload["encrypted"]
                };
                break;
        }
        return payload;
    };

    privateMethods.generateWindowsOperationPayload = function (operationCode, operationData, deviceList) {
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


    publicMethods.getWindowsServiceEndpoint = function (operationCode) {
        var featureMap = {
            "DEVICE_LOCK": "lock-devices",
            "DISENROLL": "disenroll",
            "DEVICE_RING": "ring-device",
            "LOCK_RESET": "lock-reset",
            "WIPE_DATA": "wipe-data"
        };
        //return "/mdm-windows-agent/services/windows/operation/" + featureMap[operationCode];
        return "/api/device-mgt/windows/v1.0/operation/admin/devices/" + featureMap[operationCode];
    };

    /**
     * Get the icon for the featureCode
     * @param operationCode
     * @returns icon class
     */
    publicMethods.getWindowsIconForFeature = function (operationCode) {
        var featureMap = {
            "DEVICE_LOCK": "fw-lock",
            "DEVICE_RING": "fw-dial-up",
            "DISENROLL": "fw-export",
            "LOCK_RESET": "fw-key",
            "WIPE_DATA": "fw-delete"
        };
        return featureMap[operationCode];
    };

    /**
     * Filter a list by a data attribute.
     * @param prop
     * @param val
     * @returns {Array}
     */
    $.fn.filterByData = function (prop, val) {
        return this.filter(
            function () {
                return $(this).data(prop) == val;
            }
        );
    };

    /**
     * Method to generate Platform specific operation payload.
     *
     * @param operationCode Operation Codes to generate the profile from
     * @param deviceList Optional device list to include in payload body for operations
     * @returns {*}
     */
    publicMethods.generatePayload = function (operationCode, deviceList) {
        var payload;
        var operationData = {};
        // capturing form input data designated by .operationDataKeys
        $(".operation-data").filterByData("operation-code", operationCode).find(".operationDataKeys").each(
            function () {
                var operationDataObj = $(this);
                var key = operationDataObj.data("key");
                var value;
                if (operationDataObj.is(":text") || operationDataObj.is("textarea") ||
                    operationDataObj.is(":password") || operationDataObj.is("input[type=hidden]")) {
                    value = operationDataObj.val();
                    operationData[key] = value;
                } else if (operationDataObj.is(":checkbox")) {
                    value = operationDataObj.is(":checked");
                    operationData[key] = value;
                } else if (operationDataObj.is(":radio") && operationDataObj.is(":checked")) {
                    value = operationDataObj.val();
                    operationData[key] = value;
                } else if (operationDataObj.is("select")) {
                    value = operationDataObj.find("option:selected").attr("value");
                    operationData[key] = value;
                } else if (operationDataObj.hasClass("grouped-array-input")) {
                    value = [];
                    var childInput;
                    var childInputValue;
                    if (operationDataObj.hasClass("one-column-input-array")) {
                        $(".child-input", this).each(function () {
                            childInput = $(this);
                            if (childInput.is(":text") || childInput.is("textarea") || childInput.is(":password")
                                || childInput.is("input[type=hidden]")) {
                                childInputValue = childInput.val();
                            } else if (childInput.is(":checkbox")) {
                                childInputValue = childInput.is(":checked");
                            } else if (childInput.is("select")) {
                                childInputValue = childInput.find("option:selected").attr("value");
                            }
                            // push to value
                            value.push(childInputValue);
                        });
                    } else if (operationDataObj.hasClass("valued-check-box-array")) {
                        $(".child-input", this).each(function () {
                            childInput = $(this);
                            if (childInput.is(":checked")) {
                                // get associated value with check-box
                                childInputValue = childInput.data("value");
                                // push to value
                                value.push(childInputValue);
                            }
                        });
                    } else if (operationDataObj.hasClass("multi-column-joined-input-array")) {
                        var columnCount = operationDataObj.data("column-count");
                        var inputCount = 0;
                        var joinedInput;
                        $(".child-input", this).each(function () {
                            childInput = $(this);
                            if (childInput.is(":text") || childInput.is("textarea") || childInput.is(":password")
                                || childInput.is("input[type=hidden]")) {
                                childInputValue = childInput.val();
                            } else if (childInput.is(":checkbox")) {
                                childInputValue = childInput.is(":checked");
                            } else if (childInput.is("select")) {
                                childInputValue = childInput.find("option:selected").attr("value");
                            }
                            inputCount++;
                            if (inputCount % columnCount == 1) {
                                // initialize joinedInput value
                                joinedInput = "";
                                // append childInputValue to joinedInput
                                joinedInput += childInputValue;
                            } else if ((inputCount % columnCount) >= 2) {
                                // append childInputValue to joinedInput
                                joinedInput += childInputValue;
                            } else {
                                // append childInputValue to joinedInput
                                joinedInput += childInputValue;
                                // push to value
                                value.push(joinedInput);
                            }
                        });
                    } else if (operationDataObj.hasClass("multi-column-key-value-pair-array")) {
                        columnCount = operationDataObj.data("column-count");
                        inputCount = 0;
                        var childInputKey;
                        var keyValuePairJson;
                        $(".child-input", this).each(function () {
                            childInput = $(this);
                            childInputKey = childInput.data("child-key");
                            if (childInput.is(":text") || childInput.is("textarea") || childInput.is(":password")
                                || childInput.is("input[type=hidden]")) {
                                childInputValue = childInput.val();
                            } else if (childInput.is(":checkbox")) {
                                childInputValue = childInput.is(":checked");
                            } else if (childInput.is("select")) {
                                childInputValue = childInput.find("option:selected").attr("value");
                            }
                            inputCount++;
                            if ((inputCount % columnCount) == 1) {
                                // initialize keyValuePairJson value
                                keyValuePairJson = {};
                                // set key-value-pair
                                keyValuePairJson[childInputKey] = childInputValue;
                            } else if ((inputCount % columnCount) >= 2) {
                                // set key-value-pair
                                keyValuePairJson[childInputKey] = childInputValue;
                            } else {
                                // set key-value-pair
                                keyValuePairJson[childInputKey] = childInputValue;
                                // push to value
                                value.push(keyValuePairJson);
                            }
                        });
                    }
                    operationData[key] = value;
                }
            }
        );
        payload = privateMethods.generateWindowsOperationPayload(operationCode, operationData, deviceList);
        return payload;
    };

    /**
     * Method to populate the Platform specific operation payload.
     *
     * @param operationCode Operation Codes to generate the profile from
     * @param operationPayload payload
     * @returns {*}
     */
    publicMethods.populateUI = function (operationCode, operationPayload) {
        var uiPayload = privateMethods.generateGenericPayloadFromWindowsPayload(operationCode, operationPayload);
        // capturing form input data designated by .operationDataKeys
        $(".operation-data").filterByData("operation-code", operationCode).find(".operationDataKeys").each(
            function () {
                var operationDataObj = $(this);
                //operationDataObj.prop('disabled', true)
                var key = operationDataObj.data("key");
                // retrieve corresponding input value associated with the key
                var value = uiPayload[key];
                // populating input value according to the type of input
                if (operationDataObj.is(":text") ||
                    operationDataObj.is("textarea") ||
                    operationDataObj.is(":password") ||
                    operationDataObj.is("input[type=hidden]")) {
                    operationDataObj.val(value);
                } else if (operationDataObj.is(":checkbox")) {
                    operationDataObj.prop("checked", value);
                } else if (operationDataObj.is(":radio")) {
                    if (operationDataObj.val() == uiPayload[key]) {
                        operationDataObj.attr("checked", true);
                        operationDataObj.trigger("click");
                    }
                } else if (operationDataObj.is("select")) {
                    operationDataObj.val(value);
                    /* trigger a change of value, so that if slidable panes exist,
                     make them slide-down or slide-up accordingly */
                    operationDataObj.trigger("change");
                } else if (operationDataObj.hasClass("grouped-array-input")) {
                    // then value is complex
                    var i, childInput;
                    var childInputIndex = 0;
                    // var childInputValue;
                    if (operationDataObj.hasClass("one-column-input-array")) {
                        // generating input fields to populate complex value
                        if (value) {
                            for (i = 0; i < value.length; ++i) {
                                operationDataObj.parent().find("a").filterByData("click-event", "add-form").click();
                            }
                            // traversing through each child input
                            $(".child-input", this).each(function () {
                                childInput = $(this);
                                var childInputValue = value[childInputIndex];
                                // populating extracted value in the UI according to the input type
                                if (childInput.is(":text") ||
                                    childInput.is("textarea") ||
                                    childInput.is(":password") ||
                                    childInput.is("input[type=hidden]") ||
                                    childInput.is("select")) {
                                    childInput.val(childInputValue);
                                } else if (childInput.is(":checkbox")) {
                                    operationDataObj.prop("checked", childInputValue);
                                }
                                // incrementing childInputIndex
                                childInputIndex++;
                            });
                        }
                    } else if (operationDataObj.hasClass("valued-check-box-array")) {
                        // traversing through each child input
                        $(".child-input", this).each(function () {
                            childInput = $(this);
                            // check if corresponding value of current checkbox exists in the array of values
                            if (value) {
                                if (value.indexOf(childInput.data("value")) != -1) {
                                    // if YES, set checkbox as checked
                                    childInput.prop("checked", true);
                                }
                            }
                        });
                    } else if (operationDataObj.hasClass("multi-column-joined-input-array")) {
                        // generating input fields to populate complex value
                        if (value) {
                            for (i = 0; i < value.length; ++i) {
                                operationDataObj.parent().find("a").filterByData("click-event", "add-form").click();
                            }
                            var columnCount = operationDataObj.data("column-count");
                            var multiColumnJoinedInputArrayIndex = 0;
                            // handling scenarios specifically
                            if (operationDataObj.attr("id") == "wifi-mcc-and-mncs") {
                                // traversing through each child input
                                $(".child-input", this).each(function () {
                                    childInput = $(this);
                                    var multiColumnJoinedInput = value[multiColumnJoinedInputArrayIndex];
                                    var childInputValue;
                                    if ((childInputIndex % columnCount) == 0) {
                                        childInputValue = multiColumnJoinedInput.substring(3, 0)
                                    } else {
                                        childInputValue = multiColumnJoinedInput.substring(3);
                                        // incrementing childInputIndex
                                        multiColumnJoinedInputArrayIndex++;
                                    }
                                    // populating extracted value in the UI according to the input type
                                    if (childInput.is(":text") ||
                                        childInput.is("textarea") ||
                                        childInput.is(":password") ||
                                        childInput.is("input[type=hidden]") ||
                                        childInput.is("select")) {
                                        childInput.val(childInputValue);
                                    } else if (childInput.is(":checkbox")) {
                                        operationDataObj.prop("checked", childInputValue);
                                    }
                                    // incrementing childInputIndex
                                    childInputIndex++;
                                });
                            }
                        }
                    } else if (operationDataObj.hasClass("multi-column-key-value-pair-array")) {
                        // generating input fields to populate complex value
                        if (value) {
                            for (i = 0; i < value.length; ++i) {
                                operationDataObj.parent().find("a").filterByData("click-event", "add-form").click();
                            }
                            columnCount = operationDataObj.data("column-count");
                            var multiColumnKeyValuePairArrayIndex = 0;
                            // traversing through each child input
                            $(".child-input", this).each(function () {
                                childInput = $(this);
                                var multiColumnKeyValuePair = value[multiColumnKeyValuePairArrayIndex];
                                var childInputKey = childInput.data("child-key");
                                var childInputValue = multiColumnKeyValuePair[childInputKey];
                                // populating extracted value in the UI according to the input type
                                if (childInput.is(":text") ||
                                    childInput.is("textarea") ||
                                    childInput.is(":password") ||
                                    childInput.is("input[type=hidden]") ||
                                    childInput.is("select")) {
                                    childInput.val(childInputValue);
                                } else if (childInput.is(":checkbox")) {
                                    operationDataObj.prop("checked", childInputValue);
                                }
                                // incrementing multiColumnKeyValuePairArrayIndex for the next row of inputs
                                if ((childInputIndex % columnCount) == (columnCount - 1)) {
                                    multiColumnKeyValuePairArrayIndex++;
                                }
                                // incrementing childInputIndex
                                childInputIndex++;
                            });
                        }
                    }
                }
            }
        );
    };

    /**
     * generateProfile method is only used for policy-creation UIs.
     *
     * @param operationCodes Operation codes to generate the profile from
     * @returns {{}}
     */
    publicMethods.generateProfile = function (operationCodes) {
        var generatedProfile = {};
        for (var i = 0; i < operationCodes.length; ++i) {
            var operationCode = operationCodes[i];
            var payload = publicMethods.generatePayload(operationCode, null);
            generatedProfile[operationCode] = payload["operation"];
        }
        return generatedProfile;
    };

    /**
     * populateProfile method is used to populate the html ui with saved payload.
     *
     * @param payload List of profileFeatures
     * @returns [] configuredOperations array
     */
    publicMethods.populateProfile = function (payload) {
        var i, configuredOperations = [];
        for (i = 0; i < payload.length; ++i) {
            var configuredFeature = payload[i];
            var featureCode = configuredFeature["featureCode"];
            var operationPayload = configuredFeature["content"];
            //push the feature-code to the configuration array
            configuredOperations.push(featureCode);
            publicMethods.populateUI(featureCode, operationPayload);
        }
        return configuredOperations;
    };

    return publicMethods;
}();