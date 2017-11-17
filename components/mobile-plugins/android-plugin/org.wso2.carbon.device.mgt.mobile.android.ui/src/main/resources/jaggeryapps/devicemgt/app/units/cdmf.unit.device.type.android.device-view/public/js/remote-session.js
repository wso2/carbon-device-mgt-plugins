/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var rs_websocket;
var rs_WebSocketURL;
var shellMessageField = document.getElementById('shell-command');
var shellResponseField = document.getElementById('shell-response');
var canRemotelyControl = true;
WebSocket.prototype.set_opened = function() {
    this._opened = true;
};

WebSocket.prototype.set_closed = function() {
    this._opened = false;
};
WebSocket.prototype.get_opened = function() {
    return this._opened || false;
};

var remoteSessionWebSocketOnOpen = function() {
    rs_websocket.set_opened();
};

var remoteSessionWebSocketOnMessage = function(message) {

    $('#lbl-remote-session-status').text('Server Connected...');
    $('#remote-session-operations').removeClass('hidden');
    $('#loading-remote-session').addClass('hidden');
    $('#btn-close-remote-session').removeClass('hidden');
    $('#shell-terminal').removeClass('hidden');
    if (typeof message.data !== 'string') {
        $('#loading-screen').addClass('hidden');
        var url = URL.createObjectURL(event.data);
        var img = new Image;

        img.onload = function() {
            var ctx = document.getElementById("canvas").getContext('2d');
            ctx.canvas.height = this.height;
            ctx.canvas.width = this.width;
            ctx.drawImage(this, 0, 0);
            URL.revokeObjectURL(url);
        }
        img.src = url;
    } else {

        var json = $.parseJSON(message.data);
        if (json.code == "REMOTE_SHELL") {
            if (json.status != "NEW") {
                $('#loading-terminal').addClass('hidden');
            }
            if (json.operationResponse != null) {
                var shellResponse = $("#shell-response");
                shellResponse.val(shellResponse.val() + json.operationResponse);
            }

        } else if (json.code == "REMOTE_LOGCAT") {

            if (json.status != "NEW") {
                $('#btn-refresh-logCat').removeClass('hidden');
                $('#loading-logcat').addClass('hidden');
            }
            if (json.operationResponse != null) {
                var logcatResponse = $("#logcat-response");

                logcatResponse.val(logcatResponse.val() + json.operationResponse);
            }
        } else if (json.code == "REMOTE_CONNECT") {
            $('#loading-terminal').addClass('hidden');
            if (json.operationResponse != null) {
                var shellResponse = $("#shell-response");
                shellResponse.val(json.operationResponse);
            }
        } else if (json.code == "REMOTE_INPUT") {
            canRemotelyControl = false;
        } else {
            console.log("Message type not supported." + JSON.stringify(json));
        }
    }
};

var remoteSessionWebSocketOnClose = function(e) {
    location.reload();
};

var remoteSessionWebSocketOnError = function(err) {
    location.reload();
};

$("#btn-connect-device").click(function() {

    $('#loading-remote-session').removeClass('hidden');
    $('#btn-connect-device').addClass('hidden');
    $('#lbl-remote-session-status').text('Connecting to Server...');
    initializeRemoteSession();
});


$("#btn-close-remote-session").click(function() {
    // Close the WebSocket.
    rs_websocket.close();
    rs_websocket.set_closed();
    var canvas = document.getElementById("canvas");
    var ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    $('#btn-connect-device').removeClass('hidden');
    $('#remote-session-operations').addClass('hidden');
    $('#btn-close-remote-session').addClass('hidden');
    $('#loading-screen').addClass('hidden');
    $('#btn-stop-screen').addClass('hidden');
    $('#btn-start-screen').removeClass('hidden');
    //location.reload();
});

$("#btn-refresh-logCat").click(function() {

    $('#loading-logcat').removeClass('hidden');
    $('#btn-refresh-logCat').addClass('hidden');
    var message = new Object();
    message.code = "REMOTE_LOGCAT";

    // Send the message through the WebSocket.
    rs_websocket.send(JSON.stringify(message));

    // Clear out the message field.
    $("#logcat-response").val("");

});


function initializeRemoteSessionWebSocket() {

    rs_websocket = new WebSocket(rs_WebSocketURL);
    rs_websocket.onopen = remoteSessionWebSocketOnOpen;
    rs_websocket.onmessage = remoteSessionWebSocketOnMessage;
    rs_websocket.onclose = remoteSessionWebSocketOnClose;
    rs_websocket.onerror = remoteSessionWebSocketOnError;

}

function initializeRemoteSession() {

    rs_WebSocketURL = $("#remote-session").data("remote-session-uri");
    if (rs_WebSocketURL != null) {
        initializeRemoteSessionWebSocket();
        window.onbeforeunload = function() {
            rs_websocket.close();
        }
        $('#lbl-remote-session-status').text('Waiting on device to connect...');

    } else {
        noty({
            text: 'Remote Session endpoint connection Failed!',
            type: 'error'
        });
        $('#btn-connect-device').removeClass('hidden');
        $('#loading-remote-session').addClass('hidden');
    }
}


$("#shell-command").keyup(function(event) {
    if (event.keyCode == 13) {
        var message = new Object();
        message.code = "REMOTE_SHELL";
        message.payload = $("#shell-command").val();


        // Send the message through the WebSocket.
        rs_websocket.send(JSON.stringify(message));

        // Clear out the message field.
        $("#shell-command").val("");
        $("#shell-response").val("");
        $('#loading-terminal').removeClass('hidden');
    }
});


$("#btn-start-screen").click(function() {

    canRemotelyControl = true;
    $('#loading-screen').removeClass('hidden');
    $('#btn-start-screen').addClass('hidden');
    $('#remote-control-pannel').removeClass('hidden');
    $('#btn-stop-screen').removeClass('hidden');
    var message = new Object();
    var input = new Object();
    input.action = "start";
    input.height = 768;
    input.width = 1024;
    message.code = "REMOTE_SCREEN";
    message.payload = JSON.stringify(input);
    // Send the message through the WebSocket.
    rs_websocket.send(JSON.stringify(message));
});


$("#btn-stop-screen").click(function() {

    canRemotelyControl = false;
    var canvas = document.getElementById("canvas");
    var ctx = canvas.getContext('2d');
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    $('#loading-screen').addClass('hidden');
    $('#remote-control-pannel').addClass('hidden');
    $('#btn-stop-screen').addClass('hidden');
    $('#btn-start-screen').removeClass('hidden');
    var message = new Object();
    var input = new Object();
    input.action = "stop";
    message.code = "REMOTE_SCREEN";
    message.payload = JSON.stringify(input);
    // Send the message through the WebSocket.
    rs_websocket.send(JSON.stringify(message));
});


$(function() {
    var lastDuration = 0;
    var minDuration = 50;
    var durationDiff = 0;
    var status = "ready";
    var bounds, x, y;
    //Enable swiping...
    $("#canvas").swipe({
        swipeStatus: function(event, phase, direction, distance, duration, fingers, fingerData, currentDirection) {

            if (canRemotelyControl) {
                bounds = event.target.getBoundingClientRect();
                x = event.clientX - bounds.left;
                y = event.clientY - bounds.top;
                durationDiff = duration - lastDuration;
                if (x < 0 || y < 0 || status == "blocked") {
                    return;
                }
                var inputMessage = new Object();
                var input = new Object();
                input.x = x / bounds.width;
                input.y = y / bounds.height;
                input.duration = durationDiff;
                inputMessage.code = "REMOTE_INPUT";

                if (status == "ready" && phase == "start") {
                    input.action = "down";
                    inputMessage.payload = JSON.stringify(input);
                    rs_websocket.send(JSON.stringify(inputMessage));
                    status = "unblocked";
                } else if (status == "unblocked") {
                    if (phase == "move") {
                        if (durationDiff < minDuration) {
                            return;
                        } else {
                            input.action = "move";
                            inputMessage.payload = JSON.stringify(input);
                            rs_websocket.send(JSON.stringify(inputMessage));
                            lastDuration = duration;
                        }
                    } else {
                        input.action = "up";
                        lastDuration = 0;
                        status = "blocked";
                        if (durationDiff < minDuration) {
                            setTimeout(function() {
                                inputMessage.payload = JSON.stringify(input);
                                rs_websocket.send(JSON.stringify(inputMessage));
                                status = "ready";
                            }, minDuration);
                        } else {
                            inputMessage.payload = JSON.stringify(input);
                            rs_websocket.send(JSON.stringify(inputMessage));
                            status = "ready";
                        }
                    }

                }

            }

        },
        threshold: 200,
        maxTimeThreshold: 5000,
        fingers: 'all'
    });
});;
