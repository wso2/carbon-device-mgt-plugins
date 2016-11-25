/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Checks if provided input is valid against RegEx input.
 *
 * @param regExp Regular expression
 * @param inputString Input string to check
 * @returns {boolean} Returns true if input matches RegEx
 */
function isPositiveInteger(str) {
    return /^\+?(0|[1-9]\d*)$/.test(str);
}

var notifierTypeConstants = {
    "LOCAL": "1",
    "GCM": "2"
};
// Constants to define platform types available
var platformTypeConstants = {
    "ANDROID": "android",
    "IOS": "ios",
    "WINDOWS": "windows"
};

var responseCodes = {
    "CREATED": "Created",
    "SUCCESS": "201",
    "INTERNAL_SERVER_ERROR": "Internal Server Error"
};

var configParams = {
    "NOTIFIER_TYPE": "notifierType",
    "NOTIFIER_FREQUENCY": "notifierFrequency",
    "GCM_API_KEY": "gcmAPIKey",
    "GCM_SENDER_ID": "gcmSenderId",
    "ANDROID_EULA": "androidEula",
    "IOS_EULA": "iosEula",
    "CONFIG_COUNTRY": "configCountry",
    "CONFIG_STATE": "configState",
    "CONFIG_LOCALITY": "configLocality",
    "CONFIG_ORGANIZATION": "configOrganization",
    "CONFIG_ORGANIZATION_UNIT": "configOrganizationUnit",
    "MDM_CERT_PASSWORD": "MDMCertPassword",
    "MDM_CERT_TOPIC_ID": "MDMCertTopicID",
    "APNS_CERT_PASSWORD": "APNSCertPassword",
    "MDM_CERT": "MDMCert",
    "MDM_CERT_NAME": "MDMCertName",
    "APNS_CERT": "APNSCert",
    "APNS_CERT_NAME": "APNSCertName",
    "ORG_DISPLAY_NAME": "organizationDisplayName",
    "GENERAL_EMAIL_HOST": "emailHost",
    "GENERAL_EMAIL_PORT": "emailPort",
    "GENERAL_EMAIL_USERNAME": "emailUsername",
    "GENERAL_EMAIL_PASSWORD": "emailPassword",
    "GENERAL_EMAIL_SENDER_ADDRESS": "emailSender",
    "GENERAL_EMAIL_TEMPLATE": "emailTemplate",
    "COMMON_NAME": "commonName",
    "KEYSTORE_PASSWORD": "keystorePassword",
    "PRIVATE_KEY_PASSWORD": "privateKeyPassword",
    "BEFORE_EXPIRE": "beforeExpire",
    "AFTER_EXPIRE": "afterExpire",
    "WINDOWS_EULA": "windowsLicense",
    "IOS_CONFIG_MDM_MODE": "iOSConfigMDMMode",
    "IOS_CONFIG_APNS_MODE": "iOSConfigAPNSMode"
};

$(document).ready(function () {
    $("#gcm-inputs").hide();
    tinymce.init({
        selector: "textarea",
        height:500,
        theme: "modern",
        plugins: [
            "autoresize",
            "advlist autolink lists link image charmap print preview anchor",
            "searchreplace visualblocks code fullscreen",
            "insertdatetime image table contextmenu paste"
        ],
        toolbar: "undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image"
    });

    var androidConfigAPI = "/api/device-mgt/android/v1.0/configuration";

    /**
     * Following requests would execute
     * on page load event of platform configuration page in WSO2 EMM Console.
     * Upon receiving the response, the parameters will be set to the fields,
     * in case those configurations are already set.
     */

    invokerUtil.get(
        androidConfigAPI,
        function (data) {
            data = JSON.parse(data);
            if (data != null && data.configuration != null) {
                for (var i = 0; i < data.configuration.length; i++) {
                    var config = data.configuration[i];
                    if (config.name == configParams["NOTIFIER_TYPE"]) {
                        $("#android-config-notifier").val(config.value);
                        if (config.value != notifierTypeConstants["GCM"]) {
                            $("#gcm-inputs").hide();
                            $("#local-inputs").show();
                        } else {
                            $("#gcm-inputs").show();
                            $("#local-inputs").hide();
                        }
                    } else if (config.name == configParams["NOTIFIER_FREQUENCY"]) {
                        $("input#android-config-notifier-frequency").val(config.value / 1000);
                    } else if (config.name == configParams["GCM_API_KEY"]) {
                        $("input#android-config-gcm-api-key").val(config.value);
                    } else if (config.name == configParams["ANDROID_EULA"]) {
                        $("#android-eula").val(config.value);
                    }
                }
            }
        }, function (data) {
            console.log(data);
        });

    $("select.select2[multiple=multiple]").select2({
        tags: true
    });

    $("#android-config-notifier").change(function () {
        var notifierType = $("#android-config-notifier").find("option:selected").attr("value");
        if (notifierType != notifierTypeConstants["GCM"]) {
            $("#gcm-inputs").hide();
            $("#local-inputs").show();
        } else {
            $("#local-inputs").hide();
            $("#gcm-inputs").show();
        }
    });

    /**
     * Following click function would execute
     * when a user clicks on "Save" button
     * on Android platform configuration page in WSO2 EMM Console.
     */
    $("button#save-android-btn").click(function () {
        var notifierType = $("#android-config-notifier").find("option:selected").attr("value");
        var notifierFrequency = $("input#android-config-notifier-frequency").val();
        var gcmAPIKey = $("input#android-config-gcm-api-key").val();
        var gcmSenderId = "sender_id";
        var androidLicense = tinyMCE.activeEditor.getContent();
        var errorMsgWrapper = "#android-config-error-msg";
        var errorMsg = "#android-config-error-msg span";
        if (notifierType == notifierTypeConstants["LOCAL"] && !notifierFrequency) {
            $(errorMsg).text("Notifier frequency is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (notifierType == notifierTypeConstants["LOCAL"] && !isPositiveInteger(notifierFrequency)) {
            $(errorMsg).text("Provided notifier frequency is invalid. ");
            $(errorMsgWrapper).removeClass("hidden");
        } else if (notifierType == notifierTypeConstants["GCM"] && !gcmAPIKey) {
            $(errorMsg).text("FCM API Key is a required field. It cannot be empty.");
            $(errorMsgWrapper).removeClass("hidden");
        } else {

            var addConfigFormData = {};
            var configList = new Array();

            var type = {
                "name": configParams["NOTIFIER_TYPE"],
                "value": notifierType,
                "contentType": "text"
            };

            var frequency = {
                "name": configParams["NOTIFIER_FREQUENCY"],
                "value": String(notifierFrequency * 1000),
                "contentType": "text"
            };

            var gcmKey = {
                "name": configParams["GCM_API_KEY"],
                "value": gcmAPIKey,
                "contentType": "text"
            };

            var gcmId = {
                "name": configParams["GCM_SENDER_ID"],
                "value": gcmSenderId,
                "contentType": "text"
            };

            var androidEula = {
                "name": configParams["ANDROID_EULA"],
                "value": androidLicense,
                "contentType": "text"
            };

            configList.push(type);
            configList.push(frequency);
            configList.push(androidEula);
            if (notifierType == notifierTypeConstants["GCM"]) {
                configList.push(gcmKey);
                configList.push(gcmId);
            }

            addConfigFormData.type = platformTypeConstants["ANDROID"];
            addConfigFormData.configuration = configList;

            var addConfigAPI = androidConfigAPI;

            invokerUtil.put(
                addConfigAPI,
                addConfigFormData,
                function (data, textStatus, jqXHR) {
                    $("#config-save-form").addClass("hidden");
                    $("#record-created-msg").removeClass("hidden");

                }, function (data) {
                    if (data.status == 500) {
                        $(errorMsg).text("Exception occurred at backend.");
                    } else if (data.status == 403) {
                        $(errorMsg).text("Action was not permitted.");
                    } else {
                        $(errorMsg).text("An unexpected error occurred.");
                    }
                    $(errorMsgWrapper).removeClass("hidden");
                }
            );
        }
    });
});