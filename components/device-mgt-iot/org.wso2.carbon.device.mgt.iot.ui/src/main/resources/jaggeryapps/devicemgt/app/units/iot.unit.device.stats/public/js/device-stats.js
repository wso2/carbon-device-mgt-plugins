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

var xAxis;

var deviceType = $("#details").data("devicetype");
var deviceId = $(".device-id").data("deviceid");
var monitorOperations = $("#details").data("monitor");
var appContext = $("#details").data("appcontext");

var marker1 = appContext + "/public/iot.unit.device.stats/images/map-marker-1.png";
var marker2 = appContext + "/public/iot.unit.device.stats/images/map-marker-2.png";

var map, mapPoints = [], mapPaths = [], mapMarkers = [];
var palette = new Rickshaw.Color.Palette({scheme: "classic9"});

function initMap() {
    if ($('#map').length) {
        map = new google.maps.Map(document.getElementById("map"), {
            center: {lat: 6.9344, lng: 79.8428},
            zoom: 12
        });
    }
}

function formatDates() {
    $(".formatDate").each(function () {
        var timeStamp = $(this).html();
        $(this).html(getDateString(timeStamp));
    });
}

function getDateString(timeStamp) {
    var monthNames = [
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"
    ];

    var date = new Date(parseInt(timeStamp));
    var day = date.getDate();
    var monthIndex = date.getMonth() + 1;
    if (monthIndex < 10) {
        monthIndex = "0" + monthIndex;
    }

    var year = date.getFullYear();
    var hours = date.getHours();
    var amPm = hours < 12 ? "AM" : "PM";

    if (hours > 12) {
        hours -= 12;
    }
    if (hours == 0) {
        hours = 12;
    }

    return day + '-' + monthNames[monthIndex - 1] + '-' + year + ' ' + hours + ':' + date.getMinutes() + amPm;
}

$(window).on("resize", function () {
    location.reload(false);
});

$(document).ready(function () {
    formatDates();
    updateGraphs();
});

function updateGraphs() {
    var tv = 5000;
    var graphs = {};
    for (var op in monitorOperations) {
        var opName = monitorOperations[op].name;
        if (opName == "gps") {
            $("#map").removeClass("hidden");
        } else {
            var xLabel = "", yLabel = "";
            if (monitorOperations[op].ui_unit) {
                var graph_data = monitorOperations[op].ui_unit.data;
                for (var d in graph_data) {
                    if (graph_data[d].hasOwnProperty("column")) {
                        if (graph_data[d]["column"]["ui-mapping"] == 'x-axis') {
                            xLabel = graph_data[d]["column"]["label"];
                        } else if (graph_data[d]["column"]["ui-mapping"] == 'y-axis') {
                            yLabel = graph_data[d]["column"]["label"];
                        }
                    }
                }
            }
            var graphHtml = '<div class="chartWrapper" id="chartWrapper-' + opName + '">' +
                            '<div id="y_axis-' + opName + '" class="custom_y_axis">' + yLabel + '</div>' +
                            '<div class="legend_container">' +
                            '<div id="smoother-' + opName + '" title="Smoothing"></div>' +
                            '<div id="legend-' + opName + '"></div>' +
                            '</div>' +
                            '<div id="chart-' + opName + '" class="custom_rickshaw_graph"></div>' +
                            '<div class="custom_x_axis">' + xLabel + '</div>' +
                            '</div>';
            $("#div-chart").append(graphHtml);

            var graph = new Rickshaw.Graph({
                element: document.getElementById("chart-" + opName),
                width: $("#chartWrapper").width() - 50,
                height: 300,
                renderer: "line",
                padding: {top: 0.2, left: 0.0, right: 0.0, bottom: 0.2},
                series: new Rickshaw.Series.FixedDuration([{name: monitorOperations[op].name}], undefined, {
                    timeInterval: 10000,
                    maxDataPoints: 20,
                    color: palette.color(),
                    timeBase: new Date().getTime() / 1000
                })
            });

            graph.render();

            xAxis = new Rickshaw.Graph.Axis.Time({
                graph: graph
            });

            xAxis.render();

            new Rickshaw.Graph.Axis.Y({
                graph: graph,
                orientation: 'left',
                height: 300,
                tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
                element: document.getElementById('y_axis-' + opName)
            });

            new Rickshaw.Graph.Legend({
                graph: graph,
                element: document.getElementById('legend-' + opName)
            });

            new Rickshaw.Graph.HoverDetail({
                graph: graph,
                formatter: function (series, x, y) {
                    var date = '<span class="date">' + moment(x * 1000).format('Do MMM YYYY h:mm:ss a') + '</span>';
                    var swatch = '<span class="detail_swatch" style="background-color: ' + series.color + '"></span>';
                    return swatch + series.name + ": " + parseInt(y) + '<br>' + date;
                }
            });

            graphs[opName] = graph;
        }
    }

    setInterval(function () {

        var getStatsRequest = $.ajax({
                                         url: appContext + "/api/operations/" + deviceType + "/stats?deviceId=" +
                                              deviceId,
                                         method: "get"
                                     });

        getStatsRequest.done(function (data) {
            var stats = data.data;
            var lastUpdate = -1;
            for (var s in stats) {
                var val = stats[s];
                if (!val) {
                    continue;
                }
                if (val.time > lastUpdate) {
                    lastUpdate = val.time;
                }
                delete val["time"];
                if (val.map) {
                    mapPoints.push(val.map);
                    var marker = new google.maps.Marker({
                        position: val.map,
                        map: map,
                        icon: marker1,
                        title: "Seen at " + getDateString(lastUpdate)
                    });
                    marker.setMap(map);
                    map.panTo(val.map);
                    mapMarkers.push(marker);

                    if (mapPoints.length > 1) {
                        var l = mapPoints.length;
                        var path = new google.maps.Polyline({
                            path: [mapPoints[l - 1], mapPoints[l - 2]],
                            geodesic: true,
                            strokeColor: "#FF0000",
                            strokeOpacity: 1.0,
                            strokeWeight: 2
                        });

                        path.setMap(map);
                        mapPaths.push(path);

                        mapMarkers[l - 2].setIcon(marker2);
                    }

                    if (mapPoints.length >= 10) {
                        mapMarkers[0].setMap(null);
                        mapMarkers.splice(0, 1);

                        mapPaths[0].setMap(null);
                        mapPaths.splice(0, 1);

                        mapPoints.splice(0, 1);
                    }
                } else {
                    var graphVals = {};
                    for (var key in val) {
                        graphVals[key] = val[key];
                        graphs[key].series.addData(graphVals);
                        graphs[key].render();
                    }
                }
            }

            if (lastUpdate == -1) {
                $('#last_seen').text("Not seen recently");
            }

            var timeDiff = new Date().getTime() - lastUpdate;
            if (timeDiff < tv * 2) {
                $('#last_seen').text("Last seen: A while ago");
            } else if (timeDiff < 60 * 1000) {
                $('#last_seen').text("Last seen: Less than a minute ago");
            } else if (timeDiff < 60 * 60 * 1000) {
                $('#last_seen').text("Last seen: " + Math.round(timeDiff / (60 * 1000))
                                     + " minutes ago");
            } else {
                $('#last_seen').text("Last seen: " + getDateString(lastUpdate));
            }
        });
    }, tv);
}