/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

var palette = new Rickshaw.Color.Palette({scheme: "munin"});
var graphMap = {};

function drawGraph_android_sense(from, to) {

    from = new Date();
    from.setDate(from.getDate()-1);
    from.setHours(0,0,0,0);
    from = parseInt(from.getTime());
    to = parseInt(new Date().getTime());

    var devices = $("#android_sense-details").data("devices");
    var tzOffset = new Date().getTimezoneOffset() * 60;

    var streamIndex = 0;
    var streams = ["battery", "light", "pressure", "proximity", "magnetic", "rotation", "gyroscope", "gravity", "accelerometer"];

    var graphType = $(".y-axis-label").text();
    populateGraph();

    function populateGraph() {
        if (streams.indexOf(graphType) < 4) {
            retrieveDataAndDrawGraph(graphType, from, to);
        } else if (streams.indexOf(graphType) < 9) {
            retrieveDataAndDrawMultiLineGraph(graphType, from, to);
        }
    }

    function clearContent(type) {
        $("#y-axis-" + type).html("");
        $("#smoother-" + type).html("");
        $("#legend-" + type).html("");
        $("#chart-" + type).html("");
        $("#x_axis-" + type).html("");
    }

    function initGraph(type, isMultilined) {
        if (graphMap[type]) {
            return graphMap[type];
        }

        var chartWrapperElmId = "#android_sense-div-chart";
        var graphWidth = $(chartWrapperElmId).width() - 100;

        var graphConfig = {
            element: document.getElementById("chart-" + type),
            width: graphWidth,
            height: 400,
            strokeWidth: 2,
            interpolation: "linear",
            xScale: d3.time.scale(),
            padding: {top: 0.2, left: 0.02, right: 0.02, bottom: 0.2},
            series: []
        };

        if (devices) {
            for (var i = 0; i < devices.length; i++) {
                graphConfig['series'].push(
                        {
                            'color': palette.color(),
                            'data': [{
                                x: parseInt(new Date().getTime() / 1000),
                                y: 0
                            }],
                            'name': devices[i].name
                        });
            }
        } else {
            if (isMultilined) {
                graphConfig['series'].push(
                        {
                            'color': palette.color(),
                            'data': [{
                                x: parseInt(new Date().getTime() / 1000),
                                y: 0
                            }],
                            'name': "x " + type
                        },
                        {
                            'color': palette.color(),
                            'data': [{
                                x: parseInt(new Date().getTime() / 1000),
                                y: 0
                            }],
                            'name': "y "  + type
                        },
                        {
                            'color': palette.color(),
                            'data': [{
                                x: parseInt(new Date().getTime() / 1000),
                                y: 0
                            }],
                            'name': "z "  + type
                        }
                );
            } else {
                graphConfig['series'].push(
                        {
                            'color': palette.color(),
                            'data': [{
                                x: parseInt(new Date().getTime() / 1000),
                                y: 0
                            }],
                            'name': type
                        });
            }
        }

        var graph = new Rickshaw.Graph(graphConfig);

        var xAxis = new Rickshaw.Graph.Axis.Time({
            graph: graph
        });
        xAxis.render();

        var yAxis = new Rickshaw.Graph.Axis.Y({
            graph: graph,
            orientation: 'left',
            width: 40,
            height: 410,
            tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
            element: document.getElementById("y-axis-" + type)

        });

        yAxis.render();

        var legend = new Rickshaw.Graph.Legend({
            graph: graph,
            element: document.getElementById("legend-" + type)
        });

        var hoverDetail = new Rickshaw.Graph.HoverDetail({
            graph: graph,
            formatter: function (series, x, y) {
                var date = '<span class="date">' +
                           moment((x + tzOffset) * 1000).format('Do MMM YYYY h:mm:ss a') + '</span>';
                var swatch = '<span class="detail_swatch" style="background-color: ' +
                             series.color + '"></span>';
                return swatch + series.name + ": " + parseInt(y) + '<br>' + date;
            }
        });

        new Rickshaw.Graph.Behavior.Series.Toggle({
            graph: graph,
            legend: legend
        });

        new Rickshaw.Graph.Behavior.Series.Order({
            graph: graph,
            legend: legend
        });

        new Rickshaw.Graph.Behavior.Series.Highlight({
            graph: graph,
            legend: legend
        });

        graph.render();

        graphMap[type] = {};
        graphMap[type].graph = graph;
        graphMap[type].config = graphConfig;
        return graphMap[type];
    }

    function retrieveDataAndDrawGraph(type, from, to) {
        clearContent(type);

        var graphObj = initGraph(type, false);
        var graph = graphObj.graph;
        var graphConfig = graphObj.config;
        var deviceIndex = 0;

        if (devices) {
            getData();
        } else {
            var backendApiUrl = $("#android_sense-div-chart").data("backend-api-url") + type + "?from=" + from + "&to=" + to;
            var successCallback = function (data) {
                if (data) {
                    drawGraph(JSON.parse(data));
                }
            };
            invokerUtil.get(backendApiUrl, successCallback, function (message) {
            });
        }

        function getData() {
            if (deviceIndex >= devices.length) {
                return;
            }
            var backendApiUrl = $("#android_sense-div-chart").data("backend-api-url") + devices[deviceIndex].deviceIdentifier
                                + "/sensors/" + type + "?from=" + from + "&to=" + to;
            var successCallback = function (data) {
                if (data) {
                    drawGraph(JSON.parse(data));
                }
                deviceIndex++;
                getData();
            };
            invokerUtil.get(backendApiUrl, successCallback, function (message) {
                deviceIndex++;
                getData();
            });
        }

        function drawGraph(data) {
            if (data.length === 0 || data.length === undefined) {
                return;
            }

            var chartData = [];
            console.log(data.length);
            for (var i = 0; i < data.length; i++) {
                chartData.push(
                        {
                            x: parseInt((data[i].values.meta_timestamp - tzOffset)/1000),
                            y: parseInt(getFieldData(data[i], type))
                        }
                );
            }
            graphConfig.series[deviceIndex].data = chartData;
            graph.update();
        }
    }

    function retrieveDataAndDrawMultiLineGraph(type, from, to) {
        clearContent(type);

        var graphObj = initGraph(type, true);
        var graph = graphObj.graph;
        var graphConfig = graphObj.config;

        var deviceIndex = 0;

        if (devices) {
            getData();
        } else {
            var backendApiUrl = $("#android_sense-div-chart").data("backend-api-url") + type + "?from=" + from + "&to=" + to;
            var successCallback = function (data) {
                if (data) {
                    drawGraph(JSON.parse(data));
                }
            };
            invokerUtil.get(backendApiUrl, successCallback, function (message) {
            });
        }

        function getData() {
            if (deviceIndex >= devices.length) {
                populateGraph();
                return;
            }
            var backendApiUrl = $("#android_sense-div-chart").data("backend-api-url") + devices[deviceIndex].deviceIdentifier
                                + "/sensors/" + type + "?from=" + from + "&to=" + to;
            var successCallback = function (data) {
                if (data) {
                    drawGraph(JSON.parse(data));
                }
                deviceIndex++;
                getData();
            };
            invokerUtil.get(backendApiUrl, successCallback, function (message) {
                deviceIndex++;
                getData();
            });
        }

        function drawGraph(data) {
            if (data.length === 0 || data.length === undefined) {
                return;
            }

            if (devices) {
                var chartData = [];
                for (var i = 0; i < data.length; i++) {
                    chartData.push(
                            {
                                x: parseInt(data[i].values.meta_timestamp) - tzOffset,
                                y: sqrt(pow(parseInt(data[i].values.Y), 2) + pow(parseInt(data[i].values.X), 2) + pow(parseInt(data[i].values.z), 2))
                            });
                }
                graphConfig.series[deviceIndex].data = chartData;
            } else {
                var chartDataX = [], chartDataY = [], chartDataZ = [];
                console.log(data.length);
                for (var i = 0; i < data.length; i++) {
                    if (data[i].values.axis==="X"){
                        console.log(new Date(parseInt(data[i].values.meta_timestamp)- tzOffset));
                        chartDataX.push(
                            {
                                x: parseInt(((data[i].values.meta_timestamp) - tzOffset)/1000),
                                y: parseInt(data[i].values.value)
                            });
                    }else if(data[i].values.axis==="Y"){
                        chartDataY.push(
                            {
                                x: parseInt(((data[i].values.meta_timestamp) - tzOffset)/1000),
                                y: parseInt(data[i].values.value)
                            });
                    }else if(data[i].values.axis==="Z"){
                        chartDataZ.push(
                            {
                                x: parseInt(((data[i].values.meta_timestamp) - tzOffset)/1000),
                                y: parseInt(data[i].values.value)
                            });
                    }else if(data[i].values.hasOwnProperty("x") && data[i].values.hasOwnProperty("y") && data[i].values.hasOwnProperty("z")){
                        chartDataX.push(
                            {
                                x: parseInt(((data[i].values.meta_timestamp) - tzOffset)/1000),
                                y: parseInt(data[i].values.x)
                            });
                        chartDataY.push(
                            {
                                x: parseInt(((data[i].values.meta_timestamp) - tzOffset)/1000),
                                y: parseInt(data[i].values.y)
                            });
                        chartDataZ.push(
                            {
                                x: parseInt(((data[i].values.meta_timestamp) - tzOffset)/1000),
                                y: parseInt(data[i].values.z)
                            });
                    }

                }
                graphConfig.series[0].data = chartDataX;
                graphConfig.series[1].data = chartDataY;
                graphConfig.series[2].data = chartDataZ;
            }

            graph.update();
        }

    }

    function getFieldData(data, type) {
        var columnData;
        switch (type) {
            case "battery" :
                columnData = data.values.level;
                break;
            case "light" :
                columnData = data.values.light;
                break;
            case "proximity" :
                columnData = data.values.proximity;
                break;
            case "pressure" :
                columnData = data.values.pressure;
                break;
        }

        return columnData;
    }
}

$(document).ready(function() {
    $("input:radio").click(function(e) {
        var offsetMode = e.target.value;
        var graphType = e.target.className;

        if (offsetMode == 'lines') {
            graphMap[graphType].graph.setRenderer('line');
            graphMap[graphType].graph.offset = 'zero';
        } else {
            graphMap[graphType].graph.setRenderer('stack');
            graphMap[graphType].graph.offset = offsetMode;
        }
        graphMap[graphType].graph.update();
    });

});