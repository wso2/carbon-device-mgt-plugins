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

/**
 * Method to update the visibility (i.e. disabled or enabled view)
 * of grouped input according to the values
 * that they currently possess.
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
 * @param selectedConfigurations  selected configurations.
 */
var polulateProfileOperations = function (selectedConfigurations) {
    $(".wr-advance-operations li.grouped-input").each(function () {
        updateGroupedInputVisibility(this);
    });
    // enabling previously configured options of last update
    for (var i = 0; i < selectedConfigurations.length; ++i) {
        var selectedOperation = selectedConfigurations[i];
        $(".operation-data").filterByData("operation-code", selectedOperation)
            .find(".panel-title .wr-input-control.switch input[type=checkbox]").each(function () {
            $(this).click();
        });
    }
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
        /* now follows the code to reinitialize all inputs of the slidable pane.
         reinitializing input fields into the defaults.*/
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

var slideDownPaneAgainstValueSetForRadioButtons = function (selectElement, paneID, valueSet) {
    var selectedValueOnChange = selectElement.value;
    var slideDownVotes = 0;
    for (var i = 0; i < valueSet.length; i++) {
        if (selectedValueOnChange == valueSet[i]) {
            slideDownVotes++;
        }
    }
    var paneSelector = "#" + paneID;
    if (slideDownVotes > 0) {
        $(paneSelector).removeClass("hidden");
    } else {
        $(paneSelector).addClass("hidden");
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

/**
 * This method will display appropriate fields based on wifi type
 * @param {object} wifi type select object
 */
var changeAndroidWifiPolicy = function (select) {
    slideDownPaneAgainstValueSet(select, 'control-wifi-password', ['wep', 'wpa', '802eap']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-eap', ['802eap']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-phase2', ['802eap']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-identity', ['802eap']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-anoidentity', ['802eap']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-cacert', ['802eap']);
};

/**
 * This method will display appropriate fields based on wifi EAP type
 * @param {object} wifi eap select object
 * @param {object} wifi type select object
 */
var changeAndroidWifiPolicyEAP = function (select, superSelect) {
    slideDownPaneAgainstValueSet(select, 'control-wifi-password', ['peap', 'ttls', 'pwd', 'fast', 'leap']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-phase2', ['peap', 'ttls', 'fast']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-provisioning', ['fast']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-identity', ['peap', 'tls', 'ttls', 'pwd', 'fast', 'leap']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-anoidentity', ['peap', 'ttls']);
    slideDownPaneAgainstValueSet(select, 'control-wifi-cacert', ['peap', 'tls', 'ttls']);
    if (superSelect.value != '802eap') {
        changeAndroidWifiPolicy(superSelect);
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
        var clonedForm = $("[data-add-form=" + $(this).attr("href") + "]").clone().find("[data-add-form-element=clone]").attr("data-add-form-clone", $(this).attr("href"));

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