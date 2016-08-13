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

// Constants to define platform types available
var platformTypeConstants = {
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

function promptErrorPolicyPlatform(errorMsg) {
    var mainErrorMsgWrapper = "#platform-config-main-error-msg";
    var mainErrorMsg = mainErrorMsgWrapper + " span";
    $(mainErrorMsg).text(errorMsg);
    $(mainErrorMsgWrapper).show();
}

$(document).ready(function () {
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

    var windowsConfigAPI = "/api/device-mgt/windows/v1.0/services/configuration";

    invokerUtil.get(
        windowsConfigAPI,
        function (data) {
            data = JSON.parse(data);
            if (data != null && data.configuration != null) {
                for (var i = 0; i < data.configuration.length; i++) {
                    var config = data.configuration[i];
                    if (config.name == configParams["NOTIFIER_FREQUENCY"]) {
                        $("input#windows-config-notifier-frequency").val(config.value / 1000);
                    } else if (config.name == configParams["WINDOWS_EULA"]) {
                        $("#windows-eula").val(config.value);
                    }
                }
            }
        }, function (data) {
            console.log(data);
        }
    );

    $("select.select2[multiple=multiple]").select2({
        tags: true
    });

    var errorMsgWrapperWindows = "#windows-config-error-msg";
    var errorMsgWindows = "#windows-config-error-msg span";
    var fileTypesWindows = ['jks'];
    var notSupportedError = false;

    var base64WindowsMDMCert = "";
    var fileInputWindowsMDMCert = $('#windows-config-mdm-certificate');
    var fileNameWindowsMDMCert = "";
    var invalidFormatWindowsMDMCert = false;

    $(fileInputWindowsMDMCert).change(function () {

        if (!window.File || !window.FileReader || !window.FileList || !window.Blob) {
            $(errorMsgWindows).text("The File APIs are not fully supported in this browser.");
            $(errorMsgWrapperWindows).removeClass("hidden");
            notSupportedError = true;
            return;
        }

        var file = fileInputWindowsMDMCert[0].files[0];
        fileNameWindowsMDMCert = file.name;
        var extension = file.name.split('.').pop().toLowerCase(),
            isSuccess = fileTypesWindows.indexOf(extension) > -1;

        if (isSuccess) {
            var fileReader = new FileReader();
            fileReader.onload = function (event) {
                base64WindowsMDMCert = event.target.result;
            };
            fileReader.readAsDataURL(file);
            invalidFormatWindowsMDMCert = false;
        } else {
            base64MDMCert = "";
            invalidFormatWindowsMDMCert = true;
        }
    });

    $("button#save-windows-btn").click(function () {

        var notifierFrequency = $("#windows-config-notifier-frequency").val();
        var windowsLicense = tinyMCE.activeEditor.getContent();

        if (!notifierFrequency) {
            $(errorMsgWindows).text("Polling Interval is a required field. It cannot be empty.");
            $(errorMsgWrapperWindows).removeClass("hidden");
        } else if (!windowsLicense) {
            $(errorMsgWindows).text("License is a required field. It cannot be empty.");
            $(errorMsgWrapperWindows).removeClass("hidden");
        } else if (!$.isNumeric(notifierFrequency)) {
            $(errorMsgWindows).text("Provided Notifier frequency is invalid. It must be a number.");
            $(errorMsgWrapperWindows).removeClass("hidden");
        } else {
            var addConfigFormData = {};
            var configList = new Array();

            var paramNotifierFrequency = {
                "name": configParams["NOTIFIER_FREQUENCY"],
                "value": String(notifierFrequency * 1000),
                "contentType": "text"
            };

            var windowsEula = {
                "name": configParams["WINDOWS_EULA"],
                "value": windowsLicense,
                "contentType": "text"
            };

            configList.push(paramNotifierFrequency);
            configList.push(windowsEula);

            addConfigFormData.type = platformTypeConstants["WINDOWS"];
            addConfigFormData.configuration = configList;

            var addConfigAPI = windowsConfigAPI;

            invokerUtil.put(
                addConfigAPI,
                addConfigFormData,
                function (data, textStatus, jqXHR) {
                    data = jqXHR.status;
                    if (data == 200) {
                        $("#config-save-form").addClass("hidden");
                        $("#record-created-msg").removeClass("hidden");
                    } else if (data == 500) {
                        $(errorMsg).text("Exception occurred at backend.");
                    } else if (data == 400) {
                        $(errorMsg).text("Configurations cannot be empty.");
                    } else {
                        $(errorMsg).text("An unexpected error occurred.");
                    }

                    $(errorMsgWrapperWindows).removeClass("hidden");
                }, function (data) {
                    data = data.status;
                    if (data == 500) {
                        $(errorMsg).text("Exception occurred at backend.");
                    } else if (data == 403) {
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