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

var palette = new Rickshaw.Color.Palette({scheme: "classic9"});
var graphMap = {};

function drawGraph_android_sense(from, to) {
    var devices = $("#android_sense-details").data("devices");
    var tzOffset = new Date().getTimezoneOffset() * 60;

    var streamIndex = 0;
    var streams = ["battery", "light", "pressure", "proximity", "accelerometer", "magnetic", "rotation", "gyroscope", "gravity"];

    populateGraph();

    function populateGraph() {
        if (streamIndex < 4) {
            retrieveDataAndDrawLineGraph(streams[streamIndex], from, to);
        } else if (streamIndex < 9) {
            retrieveDataAndDrawMultiLineGraph(streams[streamIndex], from, to);
        }
        streamIndex++;
    }

    function clearContent(type) {
        $("#y_axis-" + type).html("");
        $("#smoother-" + type).html("");
        $("#legend-" + type).html("");
        $("#chart-" + type).html("");
        $("#x_axis-" + type).html("");
        $("#slider-" + type).html("");
    }

    function initGraph(type, isMultilined) {
        if (graphMap[type]) {
            return graphMap[type];
        }

        var chartWrapperElmId = "#android_sense-div-chart";
        var graphWidth = $(chartWrapperElmId).width() - 50;

        var graphConfig = {
            element: document.getElementById("chart-" + type),
            width: graphWidth,
            height: 400,
            strokeWidth: 2,
            renderer: 'line',
            interpolation: "linear",
            unstack: true,
            stack: false,
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
                            'name': "x"
                        },
                        {
                            'color': palette.color(),
                            'data': [{
                                x: parseInt(new Date().getTime() / 1000),
                                y: 0
                            }],
                            'name': "y"
                        },
                        {
                            'color': palette.color(),
                            'data': [{
                                x: parseInt(new Date().getTime() / 1000),
                                y: 0
                            }],
                            'name': "z"
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
                            'name': $("#android_sense-details").data("devicename")
                        });
            }
        }

        var graph = new Rickshaw.Graph(graphConfig);
        graph.render();

        var xAxis = new Rickshaw.Graph.Axis.Time({
            graph: graph
        });
        xAxis.render();

        var yAxis = new Rickshaw.Graph.Axis.Y({
            graph: graph,
            orientation: 'left',
            element: document.getElementById("y_axis-" + type),
            width: 40,
            height: 410
        });
        yAxis.render();

        var slider = new Rickshaw.Graph.RangeSlider.Preview({
            graph: graph,
            element: document.getElementById("slider-" + type)
        });

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

        var shelving = new Rickshaw.Graph.Behavior.Series.Toggle({
            graph: graph,
            legend: legend
        });

        var order = new Rickshaw.Graph.Behavior.Series.Order({
            graph: graph,
            legend: legend
        });

        var highlighter = new Rickshaw.Graph.Behavior.Series.Highlight({
            graph: graph,
            legend: legend
        });

        graphMap[type] = {};
        graphMap[type].graph = graph;
        graphMap[type].config = graphConfig;
        return graphMap[type];
    }

    function retrieveDataAndDrawLineGraph(type, from, to) {
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
                    drawLineGraph(JSON.parse(data));
                }
                populateGraph();
            };
            invokerUtil.get(backendApiUrl, successCallback, function (message) {
                populateGraph();
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
                    drawLineGraph(JSON.parse(data));
                }
                deviceIndex++;
                getData();
            };
            invokerUtil.get(backendApiUrl, successCallback, function (message) {
                deviceIndex++;
                getData();
            });
        }

        function drawLineGraph(data) {
            if (data.length === 0 || data.length === undefined) {
                return;
            }

            var chartData = [];
            for (var i = 0; i < data.length; i++) {
                chartData.push(
                        {
                            x: parseInt(data[i].values.time) - tzOffset,
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
                    drawLineGraph(JSON.parse(data));
                }
                populateGraph();
            };
            invokerUtil.get(backendApiUrl, successCallback, function (message) {
                populateGraph();
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
                    drawLineGraph(JSON.parse(data));
                }
                deviceIndex++;
                getData();
            };
            invokerUtil.get(backendApiUrl, successCallback, function (message) {
                deviceIndex++;
                getData();
            });
        }

        function drawLineGraph(data) {
            if (data.length === 0 || data.length === undefined) {
                return;
            }

            if (devices) {
                var chartData = [];
                for (var i = 0; i < data.length; i++) {
                    chartData.push(
                            {
                                x: parseInt(data[i].values.time) - tzOffset,
                                y: sqrt(pow(parseInt(data[i].values.y), 2) + pow(parseInt(data[i].values.x), 2) + pow(parseInt(data[i].values.z), 2))
                            });
                }
                graphConfig.series[deviceIndex].data = chartData;
            } else {
                var chartDataX = [], chartDataY = [], chartDataZ = [];
                for (var i = 0; i < data.length; i++) {
                    chartDataX.push(
                            {
                                x: parseInt(data[i].values.time) - tzOffset,
                                y: parseInt(data[i].values.y)
                            });

                    chartDataY.push(
                            {
                                x: parseInt(data[i].values.time) - tzOffset,
                                y: parseInt(data[i].values.x)
                            });

                    chartDataZ.push(
                            {
                                x: parseInt(data[i].values.time) - tzOffset,
                                y: parseInt(data[i].values.z)
                            });
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