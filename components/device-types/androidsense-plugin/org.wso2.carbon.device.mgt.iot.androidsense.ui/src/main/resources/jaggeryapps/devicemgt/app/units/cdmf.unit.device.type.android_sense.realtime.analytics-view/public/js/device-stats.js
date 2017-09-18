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

var ws;
var typeId = 3;
var batteryId = 5;

var accelerometer_xId = 11;
var accelerometer_yId = 12;
var accelerometer_zId = 13;

var magnetic_xId = 16;
var magnetic_yId = 17;
var magnetic_zId = 18;

var gyroscope_xId = 19;
var gyroscope_yId = 20;
var gyroscope_zId = 21;

var lightId = 22;

var pressureId = 23;

var proximityId = 24;

var gravity_xId = 25;
var gravity_yId = 26;
var gravity_zId = 27;

var rotation_xId = 28;
var rotation_yId = 29;
var rotation_zId = 30;

var batteryData = [];

var lightData = [];

var pressureData = [];

var proximityData = [];

var accelerometer_xData = [];
var accelerometer_yData = [];
var accelerometer_zData = [];

var magnetic_xData = [];
var magnetic_yData = [];
var magnetic_zData = [];

var gyroscope_xData = [];
var gyroscope_yData = [];
var gyroscope_zData = [];

var gravity_xData = [];
var gravity_yData = [];
var gravity_zData = [];

var rotation_xData = [];
var rotation_yData = [];
var rotation_zData = [];

var graphMap = {};
var graphSettingsMap = {};

var palette = new Rickshaw.Color.Palette({scheme: "munin"});

var elemTop;

$(window).load(function () {

	graphMap["battery"]=lineGraph("battery", batteryData);
	graphMap["light"]=lineGraph("light", lightData);
	graphMap["pressure"]=lineGraph("pressure", pressureData);
	graphMap["proximity"]=lineGraph("proximity", proximityData);
	graphMap["accelerometer"]=threeDlineGraph("accelerometer", accelerometer_xData, accelerometer_yData, accelerometer_zData);
	graphMap["magnetic"]=threeDlineGraph("magnetic", magnetic_xData, magnetic_yData, magnetic_zData);
	graphMap["gyroscope"]=threeDlineGraph("gyroscope", gyroscope_xData, gyroscope_yData, gyroscope_zData);
	graphMap["gravity"]=threeDlineGraph("gravity", gravity_xData, gravity_yData, gravity_zData);
	graphMap["rotation"]=threeDlineGraph("rotation", rotation_xData, rotation_yData, rotation_zData);

	var websocketUrl = $("#stat-section").data("websocketurl");
	connect(websocketUrl)
});

window.onbeforeunload = function() {
	disconnect();
};

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

	var $elem = $("#chart-" + type);

	var graph = new Rickshaw.Graph({
		element: $elem[0],
		width: $elem.width() - 100,
		height: 300,
		renderer: "line",
		interpolation: "linear",
		padding: {top: 0.2, left: 0.0, right: 0.0, bottom: 0.2},
		xScale: d3.time.scale(),
		series: [
			{'color': palette.color(), 'data': xChartData, 'name': "x - " + type},
			{'color': palette.color(), 'data': yChartData, 'name': "y - " + type},
			{'color': palette.color(), 'data': zChartData, 'name': "z - " + type}
		]
	});

	var xAxis = new Rickshaw.Graph.Axis.Time({
		graph: graph
	});

	xAxis.render();

	new Rickshaw.Graph.Axis.Y({
		graph: graph,
		orientation: 'left',
		height: 300,
		tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
		element: document.getElementById("y-axis-"+type)
	});

	new Rickshaw.Graph.Legend({
		graph: graph,
		element: document.getElementById('legend-' + type)
	});

	var detail = new Rickshaw.Graph.HoverDetail({
		graph: graph
	});

    new Rickshaw.Graph.HoverDetail({
        graph: graph,
        formatter: function (series, x, y) {
            var date = '<span class="date">' + moment(x * 1000).format('Do MMM YYYY h:mm:ss a') + '</span>';
            var swatch = '<span class="detail_swatch" style="background-color: ' + series.color + '"></span>';
            return swatch + series.name + ": " + parseInt(y) + '<br>' + date;
        }
    });

	graph.render();

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

	var $elem = $("#chart-" + type);

	var graph = new Rickshaw.Graph({
		element: $elem[0],
		width: $elem.width() - 100,
		height: 300,
		renderer: "line",
		interpolation: "linear",
		padding: {top: 0.2, left: 0.0, right: 0.0, bottom: 0.2},
		xScale: d3.time.scale(),
		series: [{
			'color': palette.color(),
			'data': chartData,
			'name': type
		}]
	});

	var xAxis = new Rickshaw.Graph.Axis.Time({
		graph: graph
	});

	xAxis.render();

	new Rickshaw.Graph.Axis.Y({
		graph: graph,
		orientation: 'left',
		height: 300,
		tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
		element: document.getElementById('y-axis-'+type)
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

	graph.render();

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
						graphUpdate(batteryData, time, dataPoint[batteryId]);
                        graphMap["battery"].update();
						break;

					case "light":
						graphUpdate(lightData, time, dataPoint[lightId]);
                        graphMap["light"].update();
						break;

					case "pressure":
						graphUpdate(pressureData, time, dataPoint[pressureId]);
                        graphMap["pressure"].update();
						break;

					case "proximity":
						graphUpdate(proximityData, time, dataPoint[proximityId]);
                        graphMap["proximity"].update();
						break;

					case "accelerometer":
                        graphUpdate(accelerometer_xData, time, dataPoint[accelerometer_xId]);
                        graphUpdate(accelerometer_yData, time, dataPoint[accelerometer_yId]);
                        graphUpdate(accelerometer_zData, time, dataPoint[accelerometer_zId]);
						graphMap["accelerometer"].update();
						break;

					case "magnetic":
                        graphUpdate(magnetic_xData, time, dataPoint[magnetic_xId]);
                        graphUpdate(magnetic_yData, time, dataPoint[magnetic_yId]);
                        graphUpdate(magnetic_zData, time, dataPoint[magnetic_zId]);
						graphMap["magnetic"].update();
						break;

					case "gyroscope":
                        graphUpdate(gyroscope_xData, time, dataPoint[gyroscope_xId]);
                        graphUpdate(gyroscope_yData, time, dataPoint[gyroscope_yId]);
                        graphUpdate(gyroscope_zData, time, dataPoint[gyroscope_zId]);
						graphMap["gyroscope"].update();
						break;

					case "rotation":
                        graphUpdate(magnetic_xData, time, dataPoint[rotation_xId]);
                        graphUpdate(magnetic_yData, time, dataPoint[rotation_yId]);
                        graphUpdate(magnetic_zData, time, dataPoint[rotation_zId]);
						graphMap["rotation"].update();
						break;

					case "gravity":
                        graphUpdate(gravity_xData, time, dataPoint[gravity_xId]);
                        graphUpdate(gravity_yData, time, dataPoint[gravity_yId]);
                        graphUpdate(gravity_zData, time, dataPoint[gravity_zId]);
						graphMap["gravity"].update();
						break;
				}
			}
		};
	}
}

function graphUpdate(chartData, xValue, yValue) {
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

function maximizeGraph(graph, width,height){
	graphSettingsMap[graph.element.id] = {'width': graph.width, 'height': graph.height};
	graph.configure({
		width: width*2,
		height: height*2

	});
	graph.update();
}

function minimizeGraph(graph){
	var graphSettings = graphSettingsMap[graph.element.id];
	graph.configure({
		width: graphSettings.width,
		height: graphSettings.height
	});
	graph.update();
}

//maximize minimize functionality
$(".fw-expand").click(function(e) {
	var innerGraph= graphMap[e.target.nextElementSibling.innerHTML];
	var width = $(".chartWrapper").width();
	var height = $(".chartWrapper").height();

	if($(this).hasClass("default-view")){
		elemTop = $('#'+innerGraph.element.id).parents('.graph')[0].offsetTop;
		$(this).removeClass("default-view");
		$(this).removeClass("fw-expand");
		$(this).addClass("fw-contract");
		maximizeGraph(innerGraph,width,height);
		$(this).parent().parent().addClass("max");
		$(this).closest(".graph").siblings().addClass("max_hide");
		$(this).closest(".graph").parent().siblings().addClass("max_hide");
	}else{
		$(this).addClass("default-view");
		$(this).addClass("fw-expand");
		$(this).removeClass("fw-contract");
		minimizeGraph(innerGraph);
		$(this).parent().parent().removeClass("max");
		$(this).closest(".graph").siblings().removeClass("max_hide");
		$(this).closest(".graph").parent().siblings().removeClass("max_hide");
		focusToArea()
	}
});

//graph focusing function
function focusToArea(){
	var container = $("body");
	container.animate({
		scrollTop: elemTop
	});
}
