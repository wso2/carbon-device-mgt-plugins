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

var configuredOperations = [];

var windowsOperationConstants = {
    "PASSCODE_POLICY_OPERATION": "passcode-policy",
    "PASSCODE_POLICY_OPERATION_CODE": "PASSCODE_POLICY",
    "CAMERA_OPERATION": "camera",
    "CAMERA_OPERATION_CODE": "CAMERA",
    "ENCRYPT_STORAGE_OPERATION": "encrypt-storage",
    "ENCRYPT_STORAGE_OPERATION_CODE": "ENCRYPT_STORAGE"
};

/**
 * Method to update the visibility of grouped input.
 * @param domElement HTML grouped-input element with class name "grouped-input"
 */
var updateGroupedInputVisibility = function (domElement) {
    if ($(".parent-input:first", domElement).is(":checked")) {
        if ($(".grouped-child-input:first", domElement).hasClass("disabled")) {
            $(".grouped-child-input:first", domElement).removeClass("disabled");
        }
        $(".child-input", domElement).each(function () {
            $(this).prop('disabled', false);
        });
    } else {
        if (!$(".grouped-child-input:first", domElement).hasClass("disabled")) {
            $(".grouped-child-input:first", domElement).addClass("disabled");
        }
        $(".child-input", domElement).each(function () {
            $(this).prop('disabled', true);
        });
    }
};

/**
 * Populates policy configuration to the ui elements.
 *
 * This method will be invoked from the relevant cdmf unit when the edit page gets loaded.
 *
 * @param profileFeatureList saved feature list
 */
var polulateProfileOperations = function (profileFeatureList) {
    var selectedOperations = windowsOperationModule.populateProfile(profileFeatureList);
    $(".wr-advance-operations li.grouped-input").each(function () {
        updateGroupedInputVisibility(this);
    });
    for (var i = 0; i < selectedOperations.length; ++i) {
        var selectedOperation = selectedOperations[i];
        $(".operation-data").filterByData("operation-code", selectedOperation)
            .find(".panel-title .wr-input-control.switch input[type=checkbox]").each(function () {
            $(this).click();
        });
    }
};

/**
 * Generates policy profile feature list which will be saved with the profile.
 *
 * This function will be invoked from the relevant cdmf unit at the time of policy creation.
 *
 * @returns {Array} profile payloads
 */
var generateProfileFeaturesList = function () {
    var profilePayloads = [];
    // traverses key by key in policy["profile"]
    var key;
    for (key in policy["profile"]) {
        if (policy["profile"].hasOwnProperty(key)) {
            if (key == windowsOperationConstants["PASSCODE_POLICY_OPERATION_CODE"]) {
                policy["profile"][key].enablePassword = true;
            }
            profilePayloads.push({
                "featureCode": key,
                "deviceType": policy["platform"],
                "content": policy["profile"][key]
            });
        }
    }
    return profilePayloads;
};

/**
 * Generates policy profile object which will be saved with the profile.
 *
 * This function will be invoked from the relevant cdmf unit at the time of policy creation.
 *
 * @returns {object} generated profile.
 */
var generatePolicyProfile = function () {
    return windowsOperationModule.generateProfile(configuredOperations);
};

/**
 * Checks if provided number is valid against a range.
 *
 * @param numberInput Number Input
 * @param min Minimum Limit
 * @param max Maximum Limit
 * @returns {boolean} Returns true if input is within the specified range
 */
var inputIsValidAgainstRange = function (numberInput, min, max) {
    return (numberInput == min || (numberInput > min && numberInput < max) || numberInput == max);
};

/**
 * Validates policy profile operations for the windows platform.
 *
 * This function will be invoked from the relevant cdmf unit at the time of policy creation.
 *
 * @returns {boolean} whether validation is successful.
 */
var validatePolicyProfile = function () {
    var validationStatusArray = [];
    var validationStatus;
    var operation;

    // starting validation process and updating validationStatus
    if (configuredOperations.length == 0) {
        // updating validationStatus
        validationStatus = {
            "error": true,
            "mainErrorMsg": "You cannot continue. Zero configured features."
        };
        // updating validationStatusArray with validationStatus
        validationStatusArray.push(validationStatus);
    } else {
        // validating each and every configured Operation
        // Validating PASSCODE_POLICY
        if ($.inArray(windowsOperationConstants["PASSCODE_POLICY_OPERATION_CODE"], configuredOperations) != -1) {
            // if PASSCODE_POLICY is configured
            operation = windowsOperationConstants["PASSCODE_POLICY_OPERATION"];
            // initializing continueToCheckNextInputs to true
            var continueToCheckNextInputs = true;

            // validating first input: passcodePolicyMaxPasscodeAgeInDays
            var passcodePolicyMaxPasscodeAgeInDays = $("input#passcode-policy-max-passcode-age-in-days").val();
            if (passcodePolicyMaxPasscodeAgeInDays) {
                if (!$.isNumeric(passcodePolicyMaxPasscodeAgeInDays)) {
                    validationStatus = {
                        "error": true,
                        "subErrorMsg": "Provided passcode age is not a number.",
                        "erroneousFeature": operation
                    };
                    continueToCheckNextInputs = false;
                } else if (!inputIsValidAgainstRange(passcodePolicyMaxPasscodeAgeInDays, 1, 730)) {
                    validationStatus = {
                        "error": true,
                        "subErrorMsg": "Provided passcode age is not with in the range of 1-to-730.",
                        "erroneousFeature": operation
                    };
                    continueToCheckNextInputs = false;
                }
            }

            // validating second and last input: passcodePolicyPasscodeHistory
            if (continueToCheckNextInputs) {
                var passcodePolicyPasscodeHistory = $("input#passcode-policy-passcode-history").val();
                if (passcodePolicyPasscodeHistory) {
                    if (!$.isNumeric(passcodePolicyPasscodeHistory)) {
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Provided passcode history is not a number.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    } else if (!inputIsValidAgainstRange(passcodePolicyPasscodeHistory, 1, 50)) {
                        validationStatus = {
                            "error": true,
                            "subErrorMsg": "Provided passcode history is not with in the range of 1-to-50.",
                            "erroneousFeature": operation
                        };
                        continueToCheckNextInputs = false;
                    }
                }
            }

            // at-last, if the value of continueToCheckNextInputs is still true
            // this means that no error is found
            if (continueToCheckNextInputs) {
                validationStatus = {
                    "error": false,
                    "okFeature": operation
                };
            }

            // updating validationStatusArray with validationStatus
            validationStatusArray.push(validationStatus);
        }
        // Validating CAMERA
        if ($.inArray(windowsOperationConstants["CAMERA_OPERATION_CODE"], configuredOperations) != -1) {
            // if CAMERA is configured
            operation = windowsOperationConstants["CAMERA_OPERATION"];
            // updating validationStatus
            validationStatus = {
                "error": false,
                "okFeature": operation
            };
            // updating validationStatusArray with validationStatus
            validationStatusArray.push(validationStatus);
        }
        // Validating ENCRYPT_STORAGE
        if ($.inArray(windowsOperationConstants["ENCRYPT_STORAGE_OPERATION_CODE"], configuredOperations) != -1) {
            // if ENCRYPT_STORAGE is configured
            operation = windowsOperationConstants["ENCRYPT_STORAGE_OPERATION"];
            // updating validationStatus
            validationStatus = {
                "error": false,
                "okFeature": operation
            };
            // updating validationStatusArray with validationStatus
            validationStatusArray.push(validationStatus);
        }

    }
    // ending validation process

    // start taking specific notifying actions upon validation
    var wizardIsToBeContinued;
    var errorCount = 0;
    var mainErrorMsgWrapper, mainErrorMsg,
        subErrorMsgWrapper, subErrorMsg, subErrorIcon, subOkIcon, featureConfiguredIcon;
    var i;
    for (i = 0; i < validationStatusArray.length; i++) {
        validationStatus = validationStatusArray[i];
        if (validationStatus["error"]) {
            errorCount++;
            if (validationStatus["mainErrorMsg"]) {
                mainErrorMsgWrapper = "#policy-profile-main-error-msg";
                mainErrorMsg = mainErrorMsgWrapper + " span";
                $(mainErrorMsg).text(validationStatus["mainErrorMsg"]);
                $(mainErrorMsgWrapper).removeClass("hidden");
            } else if (validationStatus["subErrorMsg"]) {
                subErrorMsgWrapper = "#" + validationStatus["erroneousFeature"] + "-feature-error-msg";
                subErrorMsg = subErrorMsgWrapper + " span";
                subErrorIcon = "#" + validationStatus["erroneousFeature"] + "-error";
                subOkIcon = "#" + validationStatus["erroneousFeature"] + "-ok";
                featureConfiguredIcon = "#" + validationStatus["erroneousFeature"] + "-configured";
                // hiding featureConfiguredState as the first step
                if (!$(featureConfiguredIcon).hasClass("hidden")) {
                    $(featureConfiguredIcon).addClass("hidden");
                }
                // updating error state and corresponding messages
                $(subErrorMsg).text(validationStatus["subErrorMsg"]);
                if ($(subErrorMsgWrapper).hasClass("hidden")) {
                    $(subErrorMsgWrapper).removeClass("hidden");
                }
                if (!$(subOkIcon).hasClass("hidden")) {
                    $(subOkIcon).addClass("hidden");
                }
                if ($(subErrorIcon).hasClass("hidden")) {
                    $(subErrorIcon).removeClass("hidden");
                }
            }
        } else {
            if (validationStatus["okFeature"]) {
                subErrorMsgWrapper = "#" + validationStatus["okFeature"] + "-feature-error-msg";
                subErrorIcon = "#" + validationStatus["okFeature"] + "-error";
                subOkIcon = "#" + validationStatus["okFeature"] + "-ok";
                featureConfiguredIcon = "#" + validationStatus["okFeature"] + "-configured";
                // hiding featureConfiguredState as the first step
                if (!$(featureConfiguredIcon).hasClass("hidden")) {
                    $(featureConfiguredIcon).addClass("hidden");
                }
                // updating success state and corresponding messages
                if (!$(subErrorMsgWrapper).hasClass("hidden")) {
                    $(subErrorMsgWrapper).addClass("hidden");
                }
                if (!$(subErrorIcon).hasClass("hidden")) {
                    $(subErrorIcon).addClass("hidden");
                }
                if ($(subOkIcon).hasClass("hidden")) {
                    $(subOkIcon).removeClass("hidden");
                }
            }
        }
    }

    wizardIsToBeContinued = (errorCount == 0);
    return wizardIsToBeContinued;
};

// Start of HTML embedded invoke methods
var showAdvanceOperation = function (operation, button) {
    $(button).addClass('selected');
    $(button).siblings().removeClass('selected');
    var hiddenOperation = ".wr-hidden-operations-content > div";
    $(hiddenOperation + '[data-operation="' + operation + '"]').show();
    $(hiddenOperation + '[data-operation="' + operation + '"]').siblings().hide();
};

/**
 * Method to slide down a provided pane upon provided value set.
 *
 * @param selectElement Select HTML Element to consider
 * @param paneID HTML ID of div element to slide down
 * @param valueSet Applicable Value Set
 */
var slideDownPaneAgainstValueSet = function (selectElement, paneID, valueSet) {
    var selectedValueOnChange = $(selectElement).find("option:selected").val();
    if ($(selectElement).is("input:checkbox")) {
        selectedValueOnChange = $(selectElement).is(":checked").toString();
    }

    var i, slideDownVotes = 0;
    for (i = 0; i < valueSet.length; i++) {
        if (selectedValueOnChange == valueSet[i]) {
            slideDownVotes++;
        }
    }
    var paneSelector = "#" + paneID;
    if (slideDownVotes > 0) {
        if (!$(paneSelector).hasClass("expanded")) {
            $(paneSelector).addClass("expanded");
        }
        $(paneSelector).slideDown();
    } else {
        if ($(paneSelector).hasClass("expanded")) {
            $(paneSelector).removeClass("expanded");
        }
        $(paneSelector).slideUp();
        /* now follows the code to reinitialize all inputs of the slidable pane */
        // reinitializing input fields into the defaults
        $(paneSelector + " input").each(
            function () {
                if ($(this).is("input:text")) {
                    $(this).val($(this).data("default"));
                } else if ($(this).is("input:password")) {
                    $(this).val("");
                } else if ($(this).is("input:checkbox")) {
                    $(this).prop("checked", $(this).data("default"));
                    // if this checkbox is the parent input of a grouped-input
                    if ($(this).hasClass("parent-input")) {
                        var groupedInput = $(this).parent().parent().parent();
                        updateGroupedInputVisibility(groupedInput);
                    }
                }
            }
        );
        // reinitializing select fields into the defaults
        $(paneSelector + " select").each(
            function () {
                var defaultOption = $(this).data("default");
                $("option:eq(" + defaultOption + ")", this).prop("selected", "selected");
            }
        );
        // collapsing expanded-panes (upon the selection of html-select-options) if any
        $(paneSelector + " .expanded").each(
            function () {
                if ($(this).hasClass("expanded")) {
                    $(this).removeClass("expanded");
                }
                $(this).slideUp();
            }
        );
        // removing all entries of grid-input elements if exist
        $(paneSelector + " .grouped-array-input").each(
            function () {
                var gridInputs = $(this).find("[data-add-form-clone]");
                if (gridInputs.length > 0) {
                    gridInputs.remove();
                }
                var helpTexts = $(this).find("[data-help-text=add-form]");
                if (helpTexts.length > 0) {
                    helpTexts.show();
                }
            }
        );
    }
};
// End of HTML embedded invoke methods

// Start of functions related to grid-input-view
/**
 * Method to set count id to cloned elements.
 * @param {object} addFormContainer
 */
var setId = function (addFormContainer) {
    $(addFormContainer).find("[data-add-form-clone]").each(function (i) {
        $(this).attr("id", $(this).attr("data-add-form-clone").slice(1) + "-" + (i + 1));
        if ($(this).find(".index").length > 0) {
            $(this).find(".index").html(i + 1);
        }
    });
};

/**
 * Method to set count id to cloned elements.
 * @param {object} addFormContainer
 */
var showHideHelpText = function (addFormContainer) {
    var helpText = "[data-help-text=add-form]";
    if ($(addFormContainer).find("[data-add-form-clone]").length > 0) {
        $(addFormContainer).find(helpText).hide();
    } else {
        $(addFormContainer).find(helpText).show();
    }
};

$(document).ready(function () {
    // Maintains an array of configured features of the profile
    var advanceOperations = ".wr-advance-operations";
    $(advanceOperations).on("click", ".wr-input-control.switch", function (event) {
        var operationCode = $(this).parents(".operation-data").data("operation-code");
        var operation = $(this).parents(".operation-data").data("operation");
        var operationDataWrapper = $(this).data("target");
        // prevents event bubbling by figuring out what element it's being called from.
        if (event.target.tagName == "INPUT") {
            var featureConfiguredIcon;
            if ($("input[type='checkbox']", this).is(":checked")) {
                configuredOperations.push(operationCode);
                // when a feature is enabled, if "zero-configured-features" msg is available, hide that.
                var zeroConfiguredOperationsErrorMsg = "#policy-profile-main-error-msg";
                if (!$(zeroConfiguredOperationsErrorMsg).hasClass("hidden")) {
                    $(zeroConfiguredOperationsErrorMsg).addClass("hidden");
                }
                // add configured-state-icon to the feature
                featureConfiguredIcon = "#" + operation + "-configured";
                if ($(featureConfiguredIcon).hasClass("hidden")) {
                    $(featureConfiguredIcon).removeClass("hidden");
                }
            } else {
                //splicing the array if operation is present.
                var index = $.inArray(operationCode, configuredOperations);
                if (index != -1) {
                    configuredOperations.splice(index, 1);
                }
                // when a feature is disabled, clearing all its current configured, error or success states
                var subErrorMsgWrapper = "#" + operation + "-feature-error-msg";
                var subErrorIcon = "#" + operation + "-error";
                var subOkIcon = "#" + operation + "-ok";
                featureConfiguredIcon = "#" + operation + "-configured";

                if (!$(subErrorMsgWrapper).hasClass("hidden")) {
                    $(subErrorMsgWrapper).addClass("hidden");
                }
                if (!$(subErrorIcon).hasClass("hidden")) {
                    $(subErrorIcon).addClass("hidden");
                }
                if (!$(subOkIcon).hasClass("hidden")) {
                    $(subOkIcon).addClass("hidden");
                }
                if (!$(featureConfiguredIcon).hasClass("hidden")) {
                    $(featureConfiguredIcon).addClass("hidden");
                }
                // reinitializing input fields into the defaults
                $(operationDataWrapper + " input").each(
                    function () {
                        if ($(this).is("input:text")) {
                            $(this).val($(this).data("default"));
                        } else if ($(this).is("input:password")) {
                            $(this).val("");
                        } else if ($(this).is("input:checkbox")) {
                            $(this).prop("checked", $(this).data("default"));
                            // if this checkbox is the parent input of a grouped-input
                            if ($(this).hasClass("parent-input")) {
                                var groupedInput = $(this).parent().parent().parent();
                                updateGroupedInputVisibility(groupedInput);
                            }
                        }
                    }
                );
                // reinitializing select fields into the defaults
                $(operationDataWrapper + " select").each(
                    function () {
                        var defaultOption = $(this).data("default");
                        $("option:eq(" + defaultOption + ")", this).prop("selected", "selected");
                    }
                );
                // collapsing expanded-panes (upon the selection of html-select-options) if any
                $(operationDataWrapper + " .expanded").each(
                    function () {
                        if ($(this).hasClass("expanded")) {
                            $(this).removeClass("expanded");
                        }
                        $(this).slideUp();
                    }
                );
                // removing all entries of grid-input elements if exist
                $(operationDataWrapper + " .grouped-array-input").each(
                    function () {
                        var gridInputs = $(this).find("[data-add-form-clone]");
                        if (gridInputs.length > 0) {
                            gridInputs.remove();
                        }
                        var helpTexts = $(this).find("[data-help-text=add-form]");
                        if (helpTexts.length > 0) {
                            helpTexts.show();
                        }
                    }
                );
            }
        }
    });

    // <start - fixing feature-configuring switch double-click issue>
    $(advanceOperations).on('hidden.bs.collapse', function (event) {
        var collapsedFeatureBody = event.target.id;
        var operation = collapsedFeatureBody.substr(0, collapsedFeatureBody.lastIndexOf("-"));
        var featureConfiguringSwitch = "#" + operation + "-heading input[type=checkbox]";
        var featureConfiguredIcon = "#" + operation + "-configured";
        if ($(featureConfiguringSwitch).prop("checked") == true) {
            $(featureConfiguringSwitch).prop("checked", false);
        }
        if (!$(featureConfiguredIcon).hasClass("hidden")) {
            $(featureConfiguredIcon).addClass("hidden");
        }
    });

    $(advanceOperations).on('shown.bs.collapse', function (event) {
        var expandedFeatureBody = event.target.id;
        var operation = expandedFeatureBody.substr(0, expandedFeatureBody.lastIndexOf("-"));
        var featureConfiguringSwitch = "#" + operation + "-heading input[type=checkbox]";
        var featureConfiguredIcon = "#" + operation + "-configured";
        if ($(featureConfiguringSwitch).prop("checked") == false) {
            $(featureConfiguringSwitch).prop("checked", true);
        }
        if ($(featureConfiguredIcon).hasClass("hidden")) {
            $(featureConfiguredIcon).removeClass("hidden");
        }
    });
    // <end - fixing feature-configuring switch double-click issue>

    // adding support for cloning multiple profiles per feature with cloneable class definitions
    $(advanceOperations).on("click", ".multi-view.add.enabled", function () {
        // get a copy of .cloneable and create new .cloned div element
        var cloned = "<div class='cloned'><hr>" + $(".cloneable", $(this).parent().parent()).html() + "</div>";
        // append newly created .cloned div element to panel-body
        $(this).parent().parent().append(cloned);
        // enable remove action of newly cloned div element
        $(".cloned", $(this).parent().parent()).each(
            function () {
                if ($(".multi-view.remove", this).hasClass("disabled")) {
                    $(".multi-view.remove", this).removeClass("disabled");
                }
                if (!$(".multi-view.remove", this).hasClass("enabled")) {
                    $(".multi-view.remove", this).addClass("enabled");
                }
            }
        );
    });

    $(advanceOperations).on("click", ".multi-view.remove.enabled", function () {
        $(this).parent().remove();
    });

    // enabling or disabling grouped-input based on the status of a parent check-box
    $(advanceOperations).on("click", ".grouped-input", function () {
        updateGroupedInputVisibility(this);
    });

    // add form entry click function for grid inputs
    $(advanceOperations).on("click", "[data-click-event=add-form]", function () {
        var addFormContainer = $("[data-add-form-container=" + $(this).attr("href") + "]");
        var clonedForm = $("[data-add-form=" + $(this).attr("href") + "]").clone().find("[data-add-form-element=clone]")
            .attr("data-add-form-clone", $(this).attr("href"));

        // adding class .child-input to capture text-input-array-values
        $("input, select", clonedForm).addClass("child-input");

        $(addFormContainer).append(clonedForm);
        setId(addFormContainer);
        showHideHelpText(addFormContainer);
    });

    // remove form entry click function for grid inputs
    $(advanceOperations).on("click", "[data-click-event=remove-form]", function () {
        var addFormContainer = $("[data-add-form-container=" + $(this).attr("href") + "]");

        $(this).closest("[data-add-form-element=clone]").remove();
        setId(addFormContainer);
        showHideHelpText(addFormContainer);
    });
});
