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
        $('#div-operation-status').removeClass('hidden');
        $('#div-operation-status').text(message);

        var operationDiv = document.getElementById("div-operation-status");

        if(message.indexOf('Success') != -1){
            operationDiv.style.backgroundColor = '#DFF2BF';
            operationDiv.style.color = '#4F8A10';
        }else{
            operationDiv.style.backgroundColor = '#FFBABA';
            operationDiv.style.color = '#D8000C';
        }

        setTimeout(function(){
            $('#div-operation-status').addClass('hidden');
        }, 5000);

    };

}

function disconnect() {
    displayClient.close();
}
