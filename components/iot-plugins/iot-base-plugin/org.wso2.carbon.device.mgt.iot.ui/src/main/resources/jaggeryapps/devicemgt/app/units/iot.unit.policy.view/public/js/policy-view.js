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

var skipStep = {};
var policy = {};


skipStep["policy-platform"] = function (policyPayloadObj) {
    console.log(policyPayloadObj);
    policy["name"] = policyPayloadObj["policyName"];
    policy["platform"] = policyPayloadObj["profile"]["deviceType"]["name"];
    policy["platformId"] = policyPayloadObj["profile"]["deviceType"]["id"];
    var userRoleInput = $("#user-roles-input");
    var ownershipInput = $("#ownership-input");
    var userInput = $("#users-select-field");
    var actionInput = $("#action-input");
    var policyNameInput = $("#policy-name-input");
    var policyDescriptionInput = $("#policy-description-input");
    userRoleInput.val(policyPayloadObj.roles);
    userInput.val(policyPayloadObj.users);
    ownershipInput.val(policyPayloadObj.ownershipType);
    actionInput.val(policyPayloadObj.compliance);
    policyNameInput.val(policyPayloadObj["policyName"]);
    policyDescriptionInput.val(policyPayloadObj["description"]);
    // updating next-page wizard title with selected platform
    $("#policy-heading").text(policy["platform"].toUpperCase() + " POLICY - " + policy["name"].toUpperCase());
    $("#policy-platform").text(policy["platform"].toUpperCase());
    $("#policy-assignment").text(policyPayloadObj.ownershipType);
    $("#policy-action").text(policyPayloadObj.compliance.toUpperCase());
    $("#policy-description").text(policyPayloadObj["description"]);
    var policyStatus = "Active";
    if(policyPayloadObj["active"] == true &&  policyPayloadObj["updated"] == true) {
        policyStatus = '<i class="fw fw-warning icon-success"></i> Active/Updated</span>';
    } else if(policyPayloadObj["active"] == true &&  policyPayloadObj["updated"] == false) {
        policyStatus = '<i class="fw fw-ok icon-success"></i> Active</span>';
    } else if(policyPayloadObj["active"] == false &&  policyPayloadObj["updated"] == true) {
        policyStatus = '<i class="fw fw-warning icon-warning"></i> Inactive/Updated</span>';
    } else if(policyPayloadObj["active"] == false &&  policyPayloadObj["updated"] == false) {
        policyStatus = '<i class="fw fw-error icon-danger"></i> Inactive</span>';
    }

    $("#policy-status").html(policyStatus);

    if(policyPayloadObj.users.length > 0) {
        $("#policy-users").text(policyPayloadObj.users.toString().split(",").join(", "));
    } else {
        $("#users-row").addClass("hidden");
    }

    if(policyPayloadObj.roles.length > 0) {
        $("#policy-roles").text(policyPayloadObj.roles.toString().split(",").join(", "));
    } else {
        $("#roles-row").addClass("hidden");
    }
    var profileFeaturesList = policyPayloadObj["profile"]["profileFeaturesList"];
    if (profileFeaturesList.length > 0){
        var content = profileFeaturesList[0]["content"];
        var policyDefinitionObj = JSON.parse(content);
        window.queryEditor.setValue(policyDefinitionObj["policyDefinition"]);
    }
};


// End of functions related to grid-input-view

/**
 * This method will return query parameter value given its name.
 * @param name Query parameter name
 * @returns {string} Query parameter value
 */
var getParameterByName = function (name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
};

$(document).ready(function () {

    window.queryEditor = CodeMirror.fromTextArea(document.getElementById('policy-definition-input'), {
        mode: MIME_TYPE_SIDDHI_QL,
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: true,
        matchBrackets: true,
        autofocus: true,
        readOnly: true,
        extraKeys: {
            "Shift-2": function (cm) {
                insertStr(cm, cm.getCursor(), '@');
                CodeMirror.showHint(cm, getAnnotationHints);
            },
            "Ctrl-Space": "autocomplete"
        }
    });

    var policyPayloadObj;
    invokerUtil.get(
        "/api/device-mgt/v1.0/policies/" + getParameterByName("id"),
        // on success
        function (data) {
            // console.log("success: " + JSON.stringify(data));
            data = JSON.parse(data);
            policyPayloadObj = data["responseContent"];
            skipStep["policy-platform"](policyPayloadObj);
        },
        // on error
        function () {
            // should be redirected to an error page
        }
    );

});