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
$(window).load(function () {
    var websocketUrl = $("#div-chart").data("websocketurl");
    connect(websocketUrl)
});

$(window).unload(function() {
    disconnect();
});


// set up our data series with 150 random data points

var graph = new Rickshaw.Graph({
    element: document.getElementById("chart"),
    width: $("#chartWrapper").width() - 50,
    height: 300,
    renderer: "line",
    padding: {top: 0.2, left: 0.0, right: 0.0, bottom: 0.2},
    min :0,
    max : 80,
    series: new Rickshaw.Series.FixedDuration([{
        name: 'one', color: 'steelblue'
    }], undefined, {
        timeInterval: 1000,
        maxDataPoints: 10,
        timeBase: new Date().getTime() / 1000
    })
});



graph.render();

new Rickshaw.Graph.Axis.Y({
    graph: graph,
    orientation: 'left',
    height: 300,
    tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
    element: document.getElementById('y_axis')
});

new Rickshaw.Graph.Legend({
    graph: graph,
    element: document.getElementById('legend')
});

new Rickshaw.Graph.HoverDetail({
    graph: graph,
    xFormatter: function(x) { return x + "seconds" },
    yFormatter: function(y) { return Math.floor(y) + " C" }
});



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
            console.log('Received: ' + event.data);
            var dataPoint = JSON.parse(event.data);
            var data = {
                one: parseFloat(dataPoint[5])
            };
            graph.series.addData(data);
            graph.render();
        };
    }
}

function disconnect() {
    if (ws != null) {
        ws.close();
        ws = null;
    }
}