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

function drawGraph_android_sense(from, to) {
    retrieveDataAndDrawLineGraph("battery", from, to);
    retrieveDataAndDrawLineGraph("light", from, to);
    retrieveDataAndDrawLineGraph("pressure", from, to);
    retrieveDataAndDrawLineGraph("proximity", from, to);
    retrieveDataAndDrawMultiLineGraph("accelerometer", from, to);
    retrieveDataAndDrawMultiLineGraph("magnetic", from, to);
    retrieveDataAndDrawMultiLineGraph("rotation", from, to);
    retrieveDataAndDrawMultiLineGraph("gyroscope", from, to);
    retrieveDataAndDrawMultiLineGraph("gravity", from, to);
}

function retrieveDataAndDrawLineGraph(sensorType, from, to) {
    var backendApiUrl = $("#device-chart").data("backend-api-url") + sensorType + "?from=" + from + "&to=" + to;
    var successCallback = function (data) {
        if (data) {
            drawLineGraph(JSON.parse(data), sensorType);
        }
    };
    invokerUtil.get(backendApiUrl, successCallback, function (message) {
        console.log(message);
    });
}

function retrieveDataAndDrawMultiLineGraph(sensorType, from, to) {
    var backendApiUrl = $("#device-chart").data("backend-api-url") + sensorType + "?from=" + from + "&to=" + to;
    var successCallback = function (data) {
        if (data) {
            drawMultiLineGraph(JSON.parse(data), sensorType);
        }
    };
    invokerUtil.get(backendApiUrl, successCallback, function (message) {
        console.log(message);
    });
}

function drawLineGraph(data, type) {
	var chartWrapperElmId = "#device-chart";
	var graphWidth = $(chartWrapperElmId).width() - 50;
	if (data.length == 0 || data.length == undefined) {
		$("#chart-" + type).html("<br/>No data available...");
		return;
	}
    $("#chart-" + type).empty();

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

    var tzOffset = new Date().getTimezoneOffset() * 60;

    var min = Number.MAX_VALUE;
    var max = Number.MIN_VALUE;
    var range_min = 99999, range_max = 0;
    var max_val = parseInt(getData(data[0], type));
    var min_val = max_val;
    var chartData = [];
    for (var i = 0; i < data.length; i++) {
        var y_val = parseInt(getData(data[i], type));
        if (y_val > max_val) {
            max_val = y_val;
        } else if (y_val < min_val) {
            min_val = y_val;
        }
        chartData.push(
                {
                    x: parseInt(data[i].values.time) - tzOffset,
                    y: y_val
                }
        );
    }
    if (range_max < max_val) {
        range_max = max_val;
    }
    if (range_min > min_val) {
        range_min = min_val;
    }
    graphConfig['series'].push(
            {
                'color': palette.color(),
                'data': chartData,
                'name': type,
                'scale': d3.scale.linear().domain([Math.min(min, min_val), Math.max(max, max_val)])
                        .nice()
            }
    );

    var graph = new Rickshaw.Graph(graphConfig);

    graph.render();

    var xAxis = new Rickshaw.Graph.Axis.Time({
        graph: graph
    });

    xAxis.render();

    var yAxis = new Rickshaw.Graph.Axis.Y.Scaled({
        graph: graph,
        orientation: 'left',
        element: document.getElementById("y_axis-" + type),
        width: 40,
        height: 410,
        'scale': d3.scale.linear().domain([Math.min(min, range_min), Math.max(max, range_max)]).nice()
    });

    yAxis.render();

    var slider = new Rickshaw.Graph.RangeSlider.Preview({
        graph: graph,
        element: document.getElementById("slider-" + type)
    });

    var legend = new Rickshaw.Graph.Legend({
        graph: graph,
        element: document.getElementById('legend-' + type)
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
}


function drawMultiLineGraph(data, type) {
    var chartWrapperElmId = "#device-chart";
    var graphWidth = $(chartWrapperElmId).width() - 50;
    if (data.length == 0 || data.length == undefined) {
        $("#chart-" + type).html("<br/>No data available...");
        return;
    }
    $("#chart-" + type).empty();

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

    var tzOffset = new Date().getTimezoneOffset() * 60;

    var min = Number.MAX_VALUE;
    var max = Number.MIN_VALUE;
    var range_min = 99999, range_max = 0;
    var max_valX = parseInt(data[0].values.x);
    var min_valX = max_valX;

    var max_valY = parseInt(data[0].values.x);
    var min_valY = max_valY;

    var max_valZ = parseInt(data[0].values.x);
    var min_valZ = max_valZ;
    var chartDataX = [];
    var chartDataY = [];
    var chartDataZ = [];
    for (var i = 0; i < data.length; i++) {
        var y_valX = parseInt(data[i].values.x);
        if (y_valX > max_valX) {
            max_valX = y_valX;
        } else if (y_valX < min_valX) {
            min_valX = y_valX;
        }

        var y_valY = parseInt(data[i].values.y);
        if (y_valY > max_valY) {
            max_valY = y_valY;
        } else if (y_valY < min_valY) {
            min_valY = y_valY;
        }

        var y_valZ = parseInt(data[i].values.z);
        if (y_valZ > max_valZ) {
            max_valZ = y_valZ;
        } else if (y_valZ < min_valZ) {
            min_valZ = y_valZ;
        }

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
    graphConfig['series'].push(
        {
            'color': palette.color(),
            'data': chartDataX,
            'name': "x",
            'scale': d3.scale.linear().domain([Math.min(min, min_valX), Math.max(max, max_valX)]).nice()
        },
        {
            'color': palette.color(),
            'data': chartDataY,
            'name': "y",
            'scale': d3.scale.linear().domain([Math.min(min, min_valY), Math.max(max, max_valY)]).nice()
        },
        {
            'color': palette.color(),
            'data': chartDataZ,
            'name': "z",
            'scale': d3.scale.linear().domain([Math.min(min, min_valZ), Math.max(max, max_valZ)]).nice()
        }
    );

    var graph = new Rickshaw.Graph(graphConfig);

    graph.render();

    var xAxis = new Rickshaw.Graph.Axis.Time({
        graph: graph
    });

    xAxis.render();

    var yAxis = new Rickshaw.Graph.Axis.Y({
        graph: graph,
        element: document.getElementById("y_axis-" + type)
    });

    yAxis.render();

    var slider = new Rickshaw.Graph.RangeSlider.Preview({
        graph: graph,
        element: document.getElementById("slider-" + type)
    });

    var legend = new Rickshaw.Graph.Legend({
        graph: graph,
        element: document.getElementById('legend-' + type)
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
}


function getData(data, type) {
    var columnData
    switch (type) {
        case "battery" :
            columnData = data.values.level
            break;
        case "light" :
            columnData = data.values.light
            break;
        case "proximity" :
            columnData = data.values.proximity
            break;
        case "pressure" :
            columnData = data.values.pressure
            break;
    }

    return columnData;
}