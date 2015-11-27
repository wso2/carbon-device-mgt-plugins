var deviceId, deviceType;
$('select.select2').select2({
    placeholder: 'Select..'
});

$('select.select2[multiple=multiple]').select2({
    placeholder: 'Select..',
    tags: true
});

var stepperRegistry = {},
    hiddenOperation = '.wr-hidden-operations-content > div',
    advanceOperation = '.wr-advance-operations';

function initStepper(selector) {
    $(selector).click(function () {
        var nextStep = $(this).data("next");
        var currentStep = $(this).data("current");
        var isBack = $(this).data("back");
        if (!isBack) {
            var action = stepperRegistry[currentStep];
            if (action) {
                action(this);
            }
        }
        if (!nextStep) {
            var direct = $(this).data("direct");
            window.location.href = direct;
        }
        $(".itm-wiz").each(function () {
            var step = $(this).data("step");
            if (step == nextStep) {
                $(this).addClass("itm-wiz-current");
            } else {
                $(this).removeClass("itm-wiz-current");
            }
        });
        $(".wr-wizard").html($(".wr-steps").html());
        $("." + nextStep).removeClass("hidden");
        $("." + currentStep).addClass("hidden");

    });
}

function showAdvanceOperation(operation, button) {
    $(button).addClass('selected');
    $(button).siblings().removeClass('selected');
    $(hiddenOperation + '[data-operation="' + operation + '"]').show();
    $(hiddenOperation + '[data-operation="' + operation + '"]').siblings().hide();
}

var policy = {};
var configuredProfiles = [];

function savePolicy() {

    var payload = {
        policyName: policy.policyName,
        compliance: policy.selectedAction,
        ownershipType: policy.selectedOwnership,
        profile: {
            profileName: policy.policyName,
            deviceType: {
                id: policy.devicetypeId,
                name: policy.devicetype
            },
            policyDefinition: policy.policyDefinition,
            policyDescription: policy.policyDescription
        }
    };

    invokerUtil.post("../../apis/policies/add?deviceId="+deviceId, payload, function (data, txtStatus, jqxhr) {
        $(".policy-message").removeClass("hidden");
        $(".add-policy").addClass("hidden");
        setTimeout(function () {
            window.location = "../policies";
        }, 1500);
    }, function (err) {
        console.log(err);
    });
}

$(document).ready(function () {
    deviceId = getQueryParams('deviceId');
    deviceType = getQueryParams('deviceType');

    initStepper(".wizard-stepper");
    $(".wr-wizard").html($(".wr-steps").html());

    if (deviceId && deviceType){
        policy.devicetype = deviceType;
        policy.deviceId = deviceId;
        $('.policy-content').removeClass("hidden");
        $('.policy-devicetype').addClass("hidden");
    }

    $("input[type='radio'].user-select-radio").change(function () {
        $('.user-select').hide();
        $('#' + $(this).val()).show();
    });
    //Adds an event listener to swithc
    $(advanceOperation).on("click", ".wr-input-control.switch", function (evt) {
        var operation = $(this).parents(".operation-data").data("operation");
        //prevents event bubbling by figuring out what element it's being called from
        if (evt.target.tagName == "INPUT") {
            if (!$(this).hasClass('collapsed')) {
                configuredProfiles.push(operation);
            } else {
                //splicing the array if operation is present
                var index = jQuery.inArray(operation, configuredProfiles);
                if (index != -1) {
                    configuredProfiles.splice(index, 1);
                }
            }
            console.log(configuredProfiles);
        }

    });
    stepperRegistry['policy-devicetype'] = function (actionButton) {
        policy.devicetype = $(actionButton).data("devicetype");
        policy.devicetypeId = $(actionButton).data("devicetype-id");
    };
    stepperRegistry['policy-content'] = function (actionButton) {
        policy.policyName = $("#policy-name-input").val();
        policy.policyDescription = $("#policy-description-input").val();
    };
    stepperRegistry['policy-profile'] = function (actionButton) {
        policy.policyDefinition = window.queryEditor.getValue();
        //All data is collected. Policy can now be created.
        savePolicy();
    };
    $(".uu").click(function () {
        var policyName = $("#policy-name-input").val();
        var selectedProfiles = $("#profile-input").find(":selected");
        var selectedProfileId = selectedProfiles.data("id");
        var selectedUserRoles = $("#user-roles-input").val();
        var selectedUsers = $("#users-input").val();
        var selectedAction = $("#action-input").val();
    });

    var mime = MIME_TYPE_SIDDHI_QL;

    // get mime type
    if (window.location.href.indexOf('mime=') > -1) {
        mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
    }

    window.queryEditor = CodeMirror.fromTextArea(document.getElementById('policy-definition-input'), {
        mode: mime,
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: true,
        matchBrackets: true,
        autofocus: true,
        extraKeys: {
            "Shift-2": function (cm) {
                insertStr(cm, cm.getCursor(), '@');
                CodeMirror.showHint(cm, getAnnotationHints);
            },
            "Ctrl-Space": "autocomplete"
        }
    });

});

function getQueryParams(key) {
    var qs = document.location.search.split('+').join(' ');

    var params = {},
            tokens,
            re = /[?&]?([^=]+)=([^&]*)/g;

    while (tokens = re.exec(qs)) {
        params[decodeURIComponent(tokens[1])] = decodeURIComponent(tokens[2]);
    }

    return params[key];
}