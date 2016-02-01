var displayClient = null;

window.onload = function () {
    connect();
};

window.onbeforeunload = function () {
    disconnect();
}

function connect() {
    displayClient = new WebSocket("wss://" + host + ":" + port + "/digital_display/" + sessionId);

    displayClient.onmessage = function (event) {
        var message = event.data;
        var operationDiv = document.getElementById("div-operation-status");
        var type = message.split('||')[0];
        var reply = message.split('||')[1];

        if (type.valueOf() == new String("Screenshot").valueOf()) {
            document.getElementById('img').setAttribute('src', 'data:image/png;base64,' + reply);
            document.getElementById('zoom-image').setAttribute('href', 'data:image/png;base64,' + reply);
        } else if (type.valueOf() == new String("Success").valueOf()) {
            $('#div-operation-status').removeClass('hidden');
            $('#div-operation-status').text(reply);
            operationDiv.style.backgroundColor = '#DFF2BF';
            operationDiv.style.color = '#4F8A10';
        } else if (type.valueOf() == new String("Failed").valueOf()) {
            $('#div-operation-status').removeClass('hidden');
            $('#div-operation-status').text(reply);
            operationDiv.style.backgroundColor = '#FFBABA';
            operationDiv.style.color = '#D8000C';
        } else if (type.valueOf() == new String("ContentList").valueOf()) {
            var resources = reply.split("-");
            var ul = document.getElementById("content-list");
            ul.innerHTML = "";
            for (i = 0; i < resources.length; i++) {
                var li = document.createElement("li");
                li.appendChild(document.createTextNode(resources[i]));
                ul.appendChild(li);
            }
        } else if (type.valueOf() == new String("DeviceStatus").valueOf()) {
            var resources = reply.split("-");
            var ul = document.getElementById("device-statics");
            ul.innerHTML = "";
            for (i = 0; i < resources.length; i++) {
                var li = document.createElement("li");
                li.appendChild(document.createTextNode(resources[i]));
                ul.appendChild(li);
            }
        }

        setTimeout(function () {
            $('#div-operation-status').addClass('hidden');
        }, 10000);

    };

}

function disconnect() {
    displayClient.close();
}