 var xcom=0;
  var ycom=0;
  var zcom=0;
 
  
  $(document).ready(function(){
  	
  	$("#betteryprogressbar").progressbar({value:0,max:100});
  	$("#progressbar").find( ".ui-progressbar-value" ).css({"background": '#' + Math.floor( Math.random()*16777215 ).toString( 16 )  });
   	$( "#progressbar,#progressbar1,#progressbar2" ).progressbar({value:0,max:50 });
        $( "button" ).click(function( event ) {
     // var target = $( event.target );
     			if ( this.id== "colorButton" ) {$("#progressbar").find( ".ui-progressbar-value" ).css({"background": '#' + Math.floor( Math.random() * 16777215 ).toString( 16 )  });}
    			 if ( this.id== "colorButton1" ) {$("#progressbar1").find( ".ui-progressbar-value" ).css({"background": '#' + Math.floor( Math.random() * 16777215 ).toString( 16 )  });}
    			 if ( this.id== "colorButton2" ) {$("#progressbar2").find( ".ui-progressbar-value" ).css({"background": '#' + Math.floor( Math.random() * 16777215 ).toString( 16 )  });}
   	 });
  	var r=160;
  	 $(function() {
		$( "#slider" ).slider({min:0,max:50});
		$( "#slider1" ).slider({min:0,max:20});
		$( "#slider2" ).slider({min:0,max:50});
	  });
  	
  	$("#slider").slider({
		change: function( event, ui ) {
			$("#takedata").html(parseInt($("#slider").slider( "option", "value" ),10)/$("#speed2").val());
			$("#dura").html(parseInt($("#slider").slider( "option", "value" ),10));
		}
	});
	$("#slider1" ).slider({
		change: function( event, ui ) {
			
			$("#duru").html(parseInt($("#slider1").slider( "option", "value" ),10));
		}
	});
	$("#slider2" ).slider({
		change: function( event, ui ) {
			
			$("#duru1").html(parseInt($("#slider2").slider( "option", "value" ),10));
		}
	});
  	$(function() {
				 $(function() {
						$( "#tabs" ).tabs();
					});
				$( "#dialog" ).dialog({
							autoOpen: false,
							maxWidth: 1000,
							height:500,
							width:1000,
							maxHeight: 500,
							
							show: {
									effect: "blind",
									duration: 1000
									},
							hide: {
									effect: "explode",
									duration: 1000
									}
									});
				$( "#opener" ).click(function() {
				$( "#dialog" ).dialog( "open" );
					});
			});
			 
				$("#opener").button();

  
  });
 
