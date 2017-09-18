/*
 * Copyright (c)  2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var dt = dt || {};
dt.table = null;
dt.polling_task = null;
dt.data = [];
dt.filter_context = null;
dt.filters_meta = {};
dt.filters = [];
dt.filter_prefix = "g_";
dt.selected_filter_groups = [];
dt.force_fetch = false;
dt.freeze = false;
dt.div = "#table";
dt.gadgetUrl = gadgetConfig.defaultSource;
dt.filterUrl = "";
dt.API_CHANGING_PARAMETER = "non-compliant-feature-code";
dt.deviceManageUrl = "";

var oTable;
var nanoScrollerSelector = $('.nano');

dt.meta = {
    "names": ["device-id", "connectivity-details", "platform", "ownership", "actions"],
    "types": ["ordinal", "ordinal", "ordinal", "ordinal", "ordinal"]
};
dt.config = {
    key: "device-id",
    title:"deviceTable",
    charts: [{
        type: "table",
        columns: ["device-id", "connectivity-details", "platform", "ownership", "actions"],
        columnTitles: ["Device id", "Connectivity status", "Platform", "Ownership", "Actions"]
    }],
    width: $(window).width()* 0.95,
    height: $(window).width() * 0.65 > $(window).height() ? $(window).height() : $(window).width() * 0.65,
    padding: { "top": 18, "left": 30, "bottom": 22, "right": 70 }
};
dt.cell_templates = {
    'device-id' : '{{device-id}}',
    'connectivity-details' : '{{connectivity-details}}',
    'platform' : '{{platform}}',
    'ownership' : '{{ownership}}',
    'actions' : '<a target="_blank" href="'+'{{actions}}'+'"><span class="fw fw-stack"><i class="fw fw-edit fw-stack-1x"></i><i class="fw fw-circle-outline fw-stack-2x"></i></span> Manage</a>'
};

dt.initialize = function () {
    dt.table = new vizg(
        [
            {
                "metadata": dt.meta,
                "data": dt.data
            }
        ],
        dt.config
    );
    dt.table.draw(dt.div);
    setTimeout(function () {
        oTable = $("#deviceTable").DataTable({
            dom: '<"dataTablesTop"' +
            'f' +
            '<"dataTables_toolbar">' +
            '>' +
            'rt' +
            '<"dataTablesBottom"' +
            'lip' +
            '>'
        });
        nanoScrollerSelector[0].nanoscroller.reset();
    }, 1000);
    dt.loadFiltersFromURL();
    dt.startPolling();
};

dt.loadFiltersFromURL = function () {
    console.log("TABLE");
    dt.deviceManageUrl = gadgetConfig.deviceManageUrl;
    var urlParams = getURLParams();
    for (var filter in urlParams) {
        if (urlParams.hasOwnProperty(filter)
            && filter.lastIndexOf(dt.filter_prefix, 0) === 0) {
            var filter_context = filter.substring(dt.filter_prefix.length);
            if(filter_context == dt.API_CHANGING_PARAMETER){
                dt.gadgetUrl = gadgetConfig.featureSource;
            }
            dt.updateFilters({
                filteringContext: filter_context,
                filteringGroups: urlParams[filter]
            });
        }
        dt.filterUrl = getFilteringUrl();
    }
};

dt.startPolling = function () {
    setTimeout(function () {
        dt.update();
    }, 500);
    this.polling_task = setInterval(function () {
        dt.update();
    }, gadgetConfig.polling_interval);
};

dt.update = function (force) {
    dt.force_fetch = !dt.force_fetch ? force || false : true;
    if (!dt.freeze) {
        //todo: redrawing the data table because there are no clear and insert method
        document.getElementById("table").innerHTML = "";
        dt.data = [];
        dt.table = new vizg(
            [
                {
                    "metadata": dt.meta,
                    "data": dt.data
                }
            ],
            dt.config
        );
        dt.table.draw(dt.div);
        setTimeout(function(){
            oTable.destroy();
            oTable = $("#deviceTable").DataTable({
                dom: '<"dataTablesTop"' +
                'f' +
                '<"dataTables_toolbar">' +
                '>' +
                'rt' +
                '<"dataTablesBottom"' +
                'lip' +
                '>'
            });
            nanoScrollerSelector[0].nanoscroller.reset();
        }, 1000);
        dt.fetch(function (data) {
            dt.table.insert(data);
        });
    }
};

dt.fetch = function (cb) {
    dt.data.length = 0;
    dt.data = [];
    dt.force_fetch = false;
    var endpointUrl = dt.gadgetUrl;

    if(dt.filterUrl != ""){
        endpointUrl = endpointUrl + "?" + dt.filterUrl + "&pagination-enabled=false";
    } else {
        endpointUrl = endpointUrl + "?pagination-enabled=false";
    }

    wso2.gadgets.XMLHttpRequest.get(endpointUrl,
        function(response){
            if (Object.prototype.toString.call(response) === '[object Array]' && response.length === 1) {
                dt.filter_context = response[0]["context"];
                var data = response[0]["data"];
                if (data && data.length > 0) {
                    for (var i = 0; i < data.length; i++) {
                        var managingUrl = dt.deviceManageUrl;
                        managingUrl = managingUrl.replace("$type$", data[i]["platform"]).replace("$id$", data[i]["deviceIdentification"]);
                        console.log(managingUrl);
                        dt.data.push(
                            [
                                Mustache.to_html(dt.cell_templates['device-id'], {'device-id': data[i]["deviceIdentification"]}),
                                Mustache.to_html(dt.cell_templates['connectivity-details'], {'connectivity-details': data[i]["connectivityStatus"]}),
                                Mustache.to_html(dt.cell_templates['platform'], {'platform': data[i]["platform"]}),
                                Mustache.to_html(dt.cell_templates['ownership'], {'ownership': data[i]["ownershipType"]}),
                                Mustache.to_html(dt.cell_templates['actions'], {'actions': managingUrl})
                            ]
                        );
                    }
                    if (dt.force_fetch) {
                        dt.update();
                    } else {
                        cb(dt.data);
                    }
                }
            } else {
                console.error("Invalid response structure found: " + JSON.stringify(response));
            }
        }, function(){
            console.warn("Error accessing source for : " + gadgetConfig.id);
        });
};

dt.subscribe = function (callback) {
    console.log("subscribed to filter-groups2: ");
    gadgets.HubSettings.onConnect = function () {
        gadgets.Hub.subscribe("subscriber", function (topic, data, subscriber) {
            callback(topic, data)
        });
    };
};

dt.onclick = function (event, item) {

};

dt.updateFilters = function (data) {
    var updated = false;
    dt.filterUrl = getFilteringUrl();
    //console.log("updating device table filters");
    if (typeof data != "undefined" && data != null) {
        if (typeof data.filteringGroups === "undefined"
            || data.filteringGroups === null
            || Object.prototype.toString.call(data.filteringGroups) !== '[object Array]'
            || data.filteringGroups.length === 0) {
            if (dt.filters_meta.hasOwnProperty(data.filteringContext)) {
                delete dt.filters_meta[data.filteringContext];
                updated = true;
            }
        } else {
            if (typeof data.filteringContext != "undefined"
                && data.filteringContext != null
                && typeof data.filteringGroups != "undefined"
                && data.filteringGroups != null
                && Object.prototype.toString.call(data.filteringGroups) === '[object Array]'
                && data.filteringGroups.length > 0) {
                dt.filters_meta[data.filteringContext] = data;
                updated = true;
            }
        }
    }
    if (updated) {
        dt.filters.length = 0;
        for (var i in dt.filters_meta) {
            if (dt.filters_meta.hasOwnProperty(i)) {
                dt.filters.push(dt.filters_meta[i]);
            }
        }
        dt.update(true);
    }
};

dt.subscribe(function (topic, data) {
    dt.updateFilters(data);
});

$(document).ready(function () {
    dt.initialize();
});