/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

var modalPopup = ".wr-modalpopup";
var modalPopupContainer = modalPopup + " .modalpopup-container";
var modalPopupContent = modalPopup + " .modalpopup-content";
var body = "body";

/*
 * set popup maximum height function.
 */
function setPopupMaxHeight() {
    $(modalPopupContent).css('max-height', ($(body).height() - ($(body).height() / 100 * 30)));
    $(modalPopupContainer).css('margin-top', (-($(modalPopupContainer).height() / 2)));
}

/*
 * show popup function.
 */
function showPopup() {
    $(modalPopup).show();
    setPopupMaxHeight();
}

/*
 * hide popup function.
 */
function hidePopup() {
    $(modalPopupContent).html('');
    $(modalPopup).hide();
}

$(document).ready(function () {
    formatDates();
});

function formatDates() {
    $(".formatDate").each(function () {
        var timeStamp = $(this).html();

        var monthNames = [
            "Jan", "Feb", "Mar",
            "Apr", "May", "Jun", "Jul",
            "Aug", "Sept", "Oct",
            "Nov", "Dec"
        ];

        var date = new Date(parseInt(timeStamp));
        var day = date.getDate();
        var monthIndex = date.getMonth() + 1;
        if (monthIndex < 10)monthIndex = "0" + monthIndex;
        var year = date.getFullYear();

        var hours = date.getHours();
        var amPm = hours < 12 ? "AM" : "PM";
        if (amPm) hours -= 12;
        if (hours == 0)hours = 12;
        //+ ' @' + hours + ':' + date.getMinutes()+amPm
        $(this).html(day + '-' + monthNames[monthIndex - 1] + '-' + year);
    });
}

/**
 * Following click function would execute
 * when a user clicks on "Remove" link
 * on User Management page in WSO2 MDM Console.
 */
$("a.remove-policy-link").click(function () {
    var deviceType = $(this).data("devicetype");
    var policyName = $(this).data("policyname");
    var policyUUID = $(this).data("policyuuid");
    ///{context}/api/policies/{deviceType}/{policyName}/remove
    var removePolicyAPI = "/store/apis/policies/" + deviceType + "/" + policyName + "/remove";

    $(modalPopupContent).html($('#remove-policy-modal-content').html());
    showPopup();

    $("a#remove-policy-yes-link").click(function () {
        invokerUtil.get(
            removePolicyAPI,
            function (data) {
                if (data == 200 || data == "true") {
                    $(modalPopupContent).html($('#remove-policy-200-content').html());
                    $('#' + policyUUID).remove();
                    $("a#remove-policy-200-link").click(function () {
                        hidePopup();
                    });
                } else if (data == 400) {
                    $(modalPopupContent).html($('#remove-policy-400-content').html());
                    $("a#remove-policy-400-link").click(function () {
                        hidePopup();
                    });
                } else if (data == 403) {
                    $(modalPopupContent).html($('#remove-policy-403-content').html());
                    $("a#remove-policy-403-link").click(function () {
                        hidePopup();
                    });
                } else if (data == 409 || data == "false") {
                    $(modalPopupContent).html($('#remove-policy-409-content').html());
                    $("a#remove-policy-409-link").click(function () {
                        hidePopup();
                    });
                } else if (data == 500) {
                    $(modalPopupContent).html($('#remove-policy-unexpected-content').html());
                    $("a#remove-policy-unexpected-link").click(function () {
                        hidePopup();
                    });
                }
            },
            function (err) {
                console.log(err);
                $(modalPopupContent).html($('#remove-policy-unexpected-error-content').html());
                $("a#remove-policy-unexpected-error-link").click(function () {
                    hidePopup();
                });
            }
        );
    });

    $("a#remove-policy-cancel-link").click(function () {
        hidePopup();
    });
});