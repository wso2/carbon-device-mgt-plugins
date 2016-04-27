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

function drawGraph(from, to) {
	var backendApiUrl = $("#chart").data("backend-api-url") + "?from=" + from + "&to=" + to;

	var successCallback = function (data) {
		if (data) {
			data = JSON.parse(data);
			for (i =1; i < data.length; i++) {
				if (data[i-1].values.time > data[i].values.time) {
					alert(i);
					alert(data[i-1].values.time + "," + data[i].values.time)
				}
			}
			drawLineGraph(data)
		}
	};
	invokerUtil.get(backendApiUrl, successCallback, function (message) {
		console.log(message);
	});
}
var isGraphAdded = false;
function drawLineGraph(data) {
	$("#chart").empty();
	$("#slider").empty();
	$("#x_axis").empty();
	$("#y_axis").empty();
	$("#smoother").empty();
	$("#legend").empty();
	var chartWrapperElmId = "#div-chart";
	var graphWidth = $(chartWrapperElmId).width() - 50;
	if (data.length == 0 || data.length == undefined) {
		$("#chart").html("<br/>No data available...");
		return;
	}

	var graphConfig = {
		element: document.getElementById("chart"),
		width: graphWidth,
		height: 400,
		strokeWidth: 2,
		renderer: 'lineplot',
		interpolation: "linear",
		unstack: true,
		stack: false,
		xScale: d3.time.scale(),
		min :0,
		max : 80,
		padding: {top: 0.2, left: 0.02, right: 0.02, bottom: 0.2},
		series: []
	};

	var min = Number.MAX_VALUE;
	var max = Number.MIN_VALUE;
	var range_min = 99999, range_max = 0;
	var chartData = [];
	var max_val = parseInt(data[0].values.temperature);
	var min_val = max_val;
	for (var i = 0; i < data.length; i++) {
		var y_val = parseInt(data[i].values.temperature);
		if (y_val > max_val) {
			max_val = y_val;
		} else if (y_val < min_val) {
			min_val = y_val;
		}
		chartData.push({
			x: parseInt(data[i].values.time),
			y: y_val
		});
	}
	if (range_max < max_val) {
		range_max = max_val;
	}
	if (range_min > min_val) {
		range_min = min_val;
	}
	graphConfig['series'].push({
		'color': "steelblue",
		'data': summerizeLine(chartData),
		'name': "temperature"
	});

	if (graphConfig['series'].length == 0) {
		$("#chart").html("<br/>No data available...");
		return;
	}

	var graph = new Rickshaw.Graph(graphConfig);

	graph.render();

	var xAxis = new Rickshaw.Graph.Axis.Time({
		graph: graph
	});

	xAxis.render();

	yAxis = new Rickshaw.Graph.Axis.Y({
		graph: graph,
		orientation: 'left',
		height: 300,
		tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
		element: document.getElementById('y_axis')
	});

	yAxis.render();

	var slider = new Rickshaw.Graph.RangeSlider.Preview({
		graph: graph,
		element: document.getElementById("slider")
	});

	var legend = new Rickshaw.Graph.Legend({
		graph: graph,
		element: document.getElementById('legend')
	});

	var hoverDetail = new Rickshaw.Graph.HoverDetail({
		graph: graph,
		formatter: function (series, x, y) {
			var date = '<span class="date">' +
				moment((x) * 1000).format('Do MMM YYYY h:mm:ss a') + '</span>';
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

function summerizeLine(data) {
	if (data.length > 1500) {
		var nData = [];
		var i = 1;
		while (i < data.length) {
			var t_avg = (data[i - 1].x + data[i].x) / 2;
			var v_avg = (data[i - 1].y + data[i].y) / 2;
			nData.push({x: t_avg, y: v_avg});
			i += 2;
		}
		return summerizeLine(nData);
	} else {
		return data;
	}
}