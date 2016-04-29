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

var ws;
var typeId = 3;
var batteryId = 5;
var gps_latId = 6;
var gps_longId = 7;
var accelerometer_xId = 8;
var accelerometer_yId = 9;
var accelerometer_zId = 10;
var magnetic_xId = 11;
var magnetic_yId = 12;
var magnetic_zId = 13;
var gyroscope_xId = 14;
var gyroscope_yId = 15;
var gyroscope_zId = 16;
var lightId = 17;
var pressureId = 18;
var proximityId = 19;
var gravity_xId = 20;
var gravity_yId = 21;
var gravity_zId = 22;
var rotation_xId = 23;
var rotation_yId = 24;
var rotation_zId = 25;
var wordId = 26;
var word_sessionIdId = 27;
var word_statusId = 28;

var battery;
var batteryData = [];

var light;
var lightData = [];

var pressure;
var pressureData = [];

var proximity;
var proximityData = [];

var accelerometer;
var accelerometer_xData = [];
var accelerometer_yData = [];
var accelerometer_zData = [];

var magnetic;
var magnetic_xData = [];
var magnetic_yData = [];
var magnetic_zData = [];

var gyroscope;
var gyroscope_xData = [];
var gyroscope_yData = [];
var gyroscope_zData = [];

var gravity;
var gravity_xData = [];
var gravity_yData = [];
var gravity_zData = [];

var rotation;
var rotation_xData = [];
var rotation_yData = [];
var rotation_zData = [];

var palette = new Rickshaw.Color.Palette({scheme: "classic9"});

$(window).load(function () {

	battery = lineGraph("battery", batteryData);
	light = lineGraph("light", lightData);
	pressure = lineGraph("pressure", pressureData);
	proximity = lineGraph("proximity", proximityData);
	accelerometer = threeDlineGraph("accelerometer", accelerometer_xData, accelerometer_yData, accelerometer_zData);
	magnetic = threeDlineGraph("magnetic", magnetic_xData, magnetic_yData, magnetic_zData);
	gyroscope = threeDlineGraph("gyroscope", gyroscope_xData, gyroscope_yData, gyroscope_zData);
	gravity = threeDlineGraph("gravity", gravity_xData, gravity_yData, gravity_zData);
	rotation = threeDlineGraph("rotation", rotation_xData, rotation_yData, rotation_zData);

	var websocketUrl = $("#div-chart").data("websocketurl");
	connect(websocketUrl)
});

$(window).unload(function () {
	disconnect();
});

function threeDlineGraph(type, xChartData, yChartData, zChartData) {
	var tNow = new Date().getTime() / 1000;
	for (var i = 0; i < 30; i++) {
		xChartData.push({
			x: tNow - (30 - i) * 15,
			y: parseFloat(0)
		});
		yChartData.push({
			x: tNow - (30 - i) * 15,
			y: parseFloat(0)
		});
		zChartData.push({
			x: tNow - (30 - i) * 15,
			y: parseFloat(0)
		});
	}

	var graph = new Rickshaw.Graph({
		element: document.getElementById("chart-" + type),
		width: $("#div-chart").width() - 50,
		height: 300,
		renderer: "line",
		padding: {top: 0.2, left: 0.0, right: 0.0, bottom: 0.2},
		xScale: d3.time.scale(),
		series: [
			{'color': palette.color(), 'data': xChartData, 'name': "x - " + type},
			{'color': palette.color(), 'data': yChartData, 'name': "y - " + type},
			{'color': palette.color(), 'data': zChartData, 'name': "z - " + type}
		]
	});

	graph.render();

	var xAxis = new Rickshaw.Graph.Axis.Time({
		graph: graph
	});

	xAxis.render();

	new Rickshaw.Graph.Legend({
		graph: graph,
		element: document.getElementById('legend-' + type)
	});

	var detail = new Rickshaw.Graph.HoverDetail({
		graph: graph
	});

	return graph;
}

function lineGraph(type, chartData) {
	var tNow = new Date().getTime() / 1000;
	for (var i = 0; i < 30; i++) {
		chartData.push({
			x: tNow - (30 - i) * 15,
			y: parseFloat(0)
		});
	}

	var graph = new Rickshaw.Graph({
		element: document.getElementById("chart-" + type),
		width: $("#div-chart").width() - 50,
		height: 300,
		renderer: "line",
		padding: {top: 0.2, left: 0.0, right: 0.0, bottom: 0.2},
		xScale: d3.time.scale(),
		series: [{
			'color': palette.color(),
			'data': chartData,
			'name': type
		}]
	});

	graph.render();

	var xAxis = new Rickshaw.Graph.Axis.Time({
		graph: graph
	});

	xAxis.render();

	new Rickshaw.Graph.Axis.Y({
		graph: graph,
		orientation: 'left',
		height: 300,
		tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
		element: document.getElementById('y_axis')
	});

	new Rickshaw.Graph.Legend({
		graph: graph,
		element: document.getElementById('legend-' + type)
	});

	new Rickshaw.Graph.HoverDetail({
		graph: graph,
		formatter: function (series, x, y) {
			var date = '<span class="date">' + moment(x * 1000).format('Do MMM YYYY h:mm:ss a') + '</span>';
			var swatch = '<span class="detail_swatch" style="background-color: ' + series.color + '"></span>';
			return swatch + series.name + ": " + parseInt(y) + '<br>' + date;
		}
	});

	return graph;
}

//websocket connection
function connect(target) {
	if ('WebSocket' in window) {
		ws = new WebSocket(target);
	} else if ('MozWebSocket' in window) {
		ws = new MozWebSocket(target);
	} else {
		console.log('WebSocket is not supported by this browser.');
	}
	if (ws) {
		ws.onmessage = function (event) {
			var dataPoint = JSON.parse(event.data);
			if (dataPoint) {
				var time = parseInt(dataPoint[4]) / 1000;
				switch (dataPoint[typeId]) {
					case "battery":
						graphUpdate(batteryData, time, dataPoint[batteryId], battery);
						break;

					case "light":
						graphUpdate(lightData, time, dataPoint[lightId], light);
						break;

					case "pressure":
						graphUpdate(pressureData, time, dataPoint[pressureId], pressure);
						break;

					case "proximity":
						graphUpdate(proximityData, time, dataPoint[proximityId], proximity);
						break;

					case "accelerometer":
						dataUpdate(accelerometer_xData, time, dataPoint[accelerometer_xId]);
						dataUpdate(accelerometer_yData, time, dataPoint[accelerometer_yId]);
						dataUpdate(accelerometer_zData, time, dataPoint[accelerometer_zId]);
						accelerometer.update();
						break;

					case "magnetic":
						dataUpdate(magnetic_xData, time, dataPoint[magnetic_xId]);
						dataUpdate(magnetic_yData, time, dataPoint[magnetic_yId]);
						dataUpdate(magnetic_zData, time, dataPoint[magnetic_zId]);
						magnetic.update();
						break;

					case "gyroscope":
						dataUpdate(gyroscope_xData, time, dataPoint[gyroscope_xId]);
						dataUpdate(gyroscope_yData, time, dataPoint[gyroscope_yId]);
						dataUpdate(gyroscope_zData, time, dataPoint[gyroscope_zId]);
						gyroscope.update();
						break;

					case "rotation":
						dataUpdate(magnetic_xData, time, dataPoint[rotation_xId]);
						dataUpdate(magnetic_yData, time, dataPoint[rotation_yId]);
						dataUpdate(magnetic_zData, time, dataPoint[rotation_zId]);
						rotation.update();
						break;

					case "gravity":
						dataUpdate(gravity_xData, time, dataPoint[gravity_xId]);
						dataUpdate(gravity_yData, time, dataPoint[gravity_yId]);
						dataUpdate(gravity_zData, time, dataPoint[gravity_zId]);
						gravity.update();
						break;
				}
			}
		};
	}
}

function graphUpdate(chartData, xValue, yValue, graph) {
	chartData.push({
		x: parseInt(xValue),
		y: parseFloat(yValue)
	});
	chartData.shift();
	graph.update();
}

function dataUpdate(chartData, xValue, yValue) {
	chartData.push({
		x: parseInt(xValue),
		y: parseFloat(yValue)
	});
	chartData.shift();
}


function disconnect() {
	if (ws != null) {
		ws.close();
		ws = null;
	}
}