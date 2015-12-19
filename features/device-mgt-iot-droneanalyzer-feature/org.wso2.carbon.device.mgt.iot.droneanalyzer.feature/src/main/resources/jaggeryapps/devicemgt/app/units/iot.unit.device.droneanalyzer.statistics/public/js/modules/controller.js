
$("#module_control button").click(function(index) {
         console.log("Asking Server to send the "+$(this).attr('id')+" command to Ar Drone");
         var url = config_api.drone_control+"?action="+$(this).attr('id')+"&speed=6&duration=7";
         ajax_handler.ajaxRequest(url, config_api.drone_controlType, {action:$(this).attr('id'),speed:7,duration:7},
             config_api.drone_controlDataType, function(data, status){
                 console.log(JSON.stringify(data));
             }
         );
});



