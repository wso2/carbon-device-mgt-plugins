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
 * Following function would execute
 * when a user clicks on the list item
 * initial mode and with out select mode.
 */
function InitiateViewOption() {
    if ($(".select-enable-btn").text() == "Select") {
        $(location).attr('href', $(this).data("url"));
    }
}

(function () {
    var validateAndReturn = function (value) {
        return (value == undefined || value == null) ? "Unspecified" : value;
    };
    Handlebars.registerHelper("deviceMap", function (device) {
        device.owner = validateAndReturn(device.owner);
        device.ownership = validateAndReturn(device.ownership);
        var arr = device.properties;
        if (arr){
            device.properties = arr.reduce(function (total, current) {
                total[current.name] = validateAndReturn(current.value);
                return total;
            }, {});
        }
    });
})();

/*
 * Setting-up global variables.
 */
var deviceCheckbox = "#ast-container .ctrl-wr-asset .itm-select input[type='checkbox']";

/*
 * Add selected style class to the parent element function.
 * @param checkbox: Selected checkbox
 */
function addDeviceSelectedClass(checkbox) {
    if ($(checkbox).is(":checked")) {
        $(checkbox).closest(".ctrl-wr-asset").addClass("selected device-select");
    } else {
        $(checkbox).closest(".ctrl-wr-asset").removeClass("selected device-select");
    }
}

function loadDevices() {
    var deviceListing = $("#device-listing");
    var currentUser = deviceListing.data("currentUser");

    function getPropertyValue(deviceProperties, propertyName) {
        var property;
        for (var i =0; i < deviceProperties.length; i++) {
            property = deviceProperties[i];
            if (property.name == propertyName) {
                return property.value;
            }
        }
        return {};
    }

    var dataFilter = function (data) {
        var noDeviceView = "#no-device-view";

        data = JSON.parse(data);
        var json;

        if (data["count"] == 0) {
            if ($(noDeviceView).length) {
                $(noDeviceView).removeClass('hidden');
            }

            $(".bulk-action-row").addClass('hidden');

            json = {
                "recordsTotal": 0,
                "recordsFiltered": 0,
                "data": []
            };

        } else if (data["count"] > 0) {
            $(noDeviceView).remove();
            $("#enroll-btn").removeClass('hidden');
            $("#advanced-search-btn").removeClass('hidden');
            $(".bulk-action-row").removeClass('hidden');
            $("#device-table").removeClass('hidden');

            var objects = [];

            $(data.devices).each(function (index) {
                objects.push(
                    {
                        model: getPropertyValue(data.devices[index].properties, "DEVICE_MODEL"),
                        vendor: getPropertyValue(data.devices[index].properties, "VENDOR"),
                        user: data.devices[index].enrolmentInfo.owner,
                        status: data.devices[index].enrolmentInfo.status,
                        ownership: data.devices[index].enrolmentInfo.ownership,
                        type: data.devices[index].type,
                        deviceIdentifier: data.devices[index].deviceIdentifier,
                        name : data.devices[index].name
                    }
                );
            });

            json = {
                "recordsTotal": data["count"],
                "recordsFiltered": data["count"],
                "data": objects
            };

        }

        return JSON.stringify(json);
    };

    // possible params - nRow, aData, dataIndex
    var fnCreatedRow = function (nRow, aData) {
        $(nRow).attr('data-type', 'selectable');
        $(nRow).attr('data-devicetype', aData["type"]);
        $(nRow).attr('data-deviceid', aData["deviceIdentifier"]);
    };

    var columns = [
        {
            class : 'remove-padding icon-only content-fill viewEnabledIcon',
            data : null,
            render: function (data, type, row) {
                var deviceType = row.type;
                var deviceIdentifier = row.deviceIdentifier;
                var url = "#";
                if (row.status != 'REMOVED') {
                    url = "device/" + deviceType + "?id=" + deviceIdentifier;
                }
                return '<div class=" viewEnabledIcon thumbnail icon"  data-url="' + url + '">' +
                    '<i class="square-element text fw fw-mobile"></i>' +
                    '</div>'
            }
        },
        {
            class: 'fade-edge',
            data: 'name',
            render: function (name, type, row) {
                var model = row.model;
                var vendor = row.vendor;
                var html = '<h4>Device ' + name + '</h4>';
                if (model) {
                    html += '<div>(' + vendor + '-' + model + ')</div>';
                }
                return html;
            }
        },
        {
            class: 'fade-edge remove-padding-top',
            data: 'user',
            render: function (user) {
                return '<div><label class="label-bold">Owner&nbsp;:&nbsp;&nbsp;</label>' + user + '</div>';
            }
        },
        {
            class: 'fade-edge remove-padding-top',
            data: 'status',
            render: function (status) {
                var html;
                switch (status) {
                    case 'ACTIVE' :
                        html = '<span><i class="fw fw-ok icon-success"></i>&nbsp;&nbsp;Active</span>';
                        break;
                    case 'UNREACHABLE' :
                        html = '<span><i class="fw fw-question-mark icon-warning"></i>&nbsp;&nbsp;Unreachable</span>';
                        break;
                    case 'INACTIVE' :
                        html = '<span><i class="fw fw-warning icon-error"></i>&nbsp;&nbsp;Inactive</span>';
                        break;
                    case 'REMOVED' :
                        html = '<span><i class="fw fw-delete icon-danger"></i>&nbsp;&nbsp;Removed</span>';
                        break;
                }
                return '<div><label class="label-bold">Status&nbsp;:&nbsp;&nbsp;</label>' + html + '</div>';
            }
        },
        {
            className: 'fade-edge remove-padding-top',
            data: 'type',
            render: function (type) {
                return '<div><label class="label-bold">Type&nbsp;:&nbsp;&nbsp;</label>' + type + '</div>';
            }
        },
        {
            className: 'fade-edge remove-padding-top',
            data: 'ownership',
            render: function (ownership) {
                return '<div><label class="label-bold">Ownership&nbsp;:&nbsp;&nbsp;</label>' + ownership + '</div>';
            }
        }
    ];

    $('#device-grid').datatables_extended_serverside_paging(
        null,
        "/api/device-mgt/v1.0/devices",
        dataFilter,
        columns,
        fnCreatedRow,
        function () {
            $(".icon .text").res_text(0.2);
            $('#device-grid').removeClass('hidden');
        }, {
            "placeholder" : "Search By Device Name",
            "searchKey" : "name"
        });

    $(deviceCheckbox).click(function () {
        addDeviceSelectedClass(this);
    });
}

/*
 * Setting-up global variables.
 */
//var assetContainer = "#ast-container";

//function openCollapsedNav(){
//    $('.wr-hidden-nav-toggle-btn').addClass('active');
//    $('#hiddenNav').slideToggle('slideDown', function(){
//        if($(this).css('display') == 'none'){
//            $('.wr-hidden-nav-toggle-btn').removeClass('active');
//        }
//    });
//}

/*
 * DOM ready functions.
 */
$(document).ready(function () {
    loadDevices();

    /* Adding selected class for selected devices */
    $(deviceCheckbox).each(function () {
        addDeviceSelectedClass(this);
    });

    /* for device list sorting drop down */
    $(".ctrl-filter-type-switcher").popover({
        html : true,
        content : function () {
            return $("#content-filter-types").html();
        }
    });

    /* for data tables*/
    $('[data-toggle="tooltip"]').tooltip();

    $("[data-toggle=popover]").popover();

    $('#nav').affix({
        offset: {
            top: $('header').height()
        }
    });
});
