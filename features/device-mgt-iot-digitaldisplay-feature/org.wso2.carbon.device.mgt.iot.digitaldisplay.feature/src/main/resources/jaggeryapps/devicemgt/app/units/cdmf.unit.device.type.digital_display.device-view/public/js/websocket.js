var displayClient = null;

window.onload = function () {
    connect();
};

window.onbeforeunload = function(){
    disconnect();
}

function connect() {

    displayClient = new WebSocket("wss://"+host+":"+port+"/digital_display/" + token);

    displayClient.onmessage = function (event) {
        var message  = event.data;

        var operationDiv = document.getElementById("div-operation-status");

        if(message.indexOf('Success') != -1){
            $('#div-operation-status').removeClass('hidden');
            $('#div-operation-status').text(message);
            operationDiv.style.backgroundColor = '#DFF2BF';
            operationDiv.style.color = '#4F8A10';
        }else if(message.indexOf('Failed') != -1){
            $('#div-operation-status').removeClass('hidden');
            $('#div-operation-status').text(message);
            operationDiv.style.backgroundColor = '#FFBABA';
            operationDiv.style.color = '#D8000C';
        }else{
            //document.getElementById('img').setAttribute( 'src', 'data:image/png;base64,'+message);
            //document.getElementById('zoom-image').setAttribute( 'href', 'data:image/png;base64,'+message);
            var resources = message.split("-");
            var ul = document.getElementById("content-list");
            ul.innerHTML = "";
            for(i = 0 ; i < resources.length ; i++){
                var li = document.createElement("li");
                li.appendChild(document.createTextNode(resources[i]));
                ul.appendChild(li);
            }

        }

        setTimeout(function(){
            $('#div-operation-status').addClass('hidden');
        }, 10000);

    };

}

function disconnect() {
    displayClient.close();
}