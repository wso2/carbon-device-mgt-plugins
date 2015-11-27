var strVirtual = "virtual_";
var foundVirtual = false;

$('.real_devices .ast-name.truncate').each(function(){
    if($(this).attr('title').substr(0, strVirtual.length) == 'virtual_'){
        var parent = $(this).closest('.ctrl-wr-asset');
        parent.hide();
        foundVirtual = true;
    }
});

$('.virtual_devices .ast-name.truncate').each(function(){
    if($(this).attr('title').substr(0, strVirtual.length) != 'virtual_'){
        var parent = $(this).closest('.ctrl-wr-asset');
        parent.hide();
    }
});

if(foundVirtual){
    $(".try-device-message").removeClass("hidden");
}