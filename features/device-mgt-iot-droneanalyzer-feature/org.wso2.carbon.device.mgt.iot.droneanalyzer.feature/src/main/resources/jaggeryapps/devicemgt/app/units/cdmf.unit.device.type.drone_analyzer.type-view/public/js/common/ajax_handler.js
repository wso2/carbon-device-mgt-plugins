var ajax_handler = function(){
    var api = this;
    api.response="v";
    api.ajaxRequest=function(url,type,data,dataType,callback){
        var response;
        //alert(url);
        $.ajax({
            url: url,
            type: type,
            dataType: dataType,
            success: function (data) {
                api.response=data;
            },
            success: function (data, success) {
                console.log(" success "+ JSON.stringify(success));
                console.log(" data " +JSON.stringify(data));
                callback(data, success);
            },
            error :function( jqxhr, textStatus, error ) {
                var err = textStatus + ', ' + error;
                console.log( "Request Failed: " + err);
                callback(data, error);
                api.response=data;
            },
            data: data
        });
        return api.response;
    }
    api.makeJSONObject=function(){
        var object={};
        for (var i=0; i<arguments.length-1; i=i+2){
            object[arguments[i]]=arguments[i+1];
        }
        return object;
    }
}