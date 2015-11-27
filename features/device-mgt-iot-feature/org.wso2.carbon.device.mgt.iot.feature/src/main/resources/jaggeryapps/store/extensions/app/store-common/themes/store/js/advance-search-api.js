/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
$(function(){
	var SEARCH_API = '/apis/assets?q=';
	var SEARCH_BUTTON = '#search-btn';
	var SEARCH_FORM = '#search-form';
    var rows_added = 0;
    var last_to = 0;
    var items_per_row = 0;
    var doPagination = true;
    store.infiniteScroll ={};
    store.infiniteScroll.recalculateRowsAdded = function(){
        return (last_to - last_to%items_per_row)/items_per_row;
    };
    store.infiniteScroll.addItemsToPage = function(query){

        var screen_width = $(window).width();
        var screen_height = $(window).height();


        var header_height = 400;
        var thumb_width = 170;
        var thumb_height = 280;
        var gutter_width = 20;

        screen_width = screen_width - gutter_width; // reduce the padding from the screen size
        screen_height = screen_height - header_height;

        items_per_row = (screen_width-screen_width%thumb_width)/thumb_width;
        //var rows_per_page = (screen_height-screen_height%thumb_height)/thumb_height;
        var scroll_pos = $(document).scrollTop();
        var row_current =  (screen_height+scroll_pos-(screen_height+scroll_pos)%thumb_height)/thumb_height;
        row_current +=3 ; // We increase the row current by 2 since we need to provide one additional row to scroll down without loading it from backend


        var from = 0;
        var to = 0;
        if(row_current > rows_added && doPagination){
            from = rows_added * items_per_row;
            to = row_current*items_per_row;
            last_to = to; //We store this os we can recalculate rows_added when resolution change
            rows_added = row_current;
            store.infiniteScroll.getItems(from,to,query);
            //console.info('getting items from ' + from + " to " + to + " screen_width " + screen_width + " items_per_row " + items_per_row);
        }

    };
    store.infiniteScroll.getItems = function(from, to, query ){
        var count = to-from;
        var dynamicData = {};
        dynamicData["from"] = from;
        dynamicData["to"] = to;
        var path = window.location.href; //current page path
        // Returns the jQuery ajax method
        var url = caramel.tenantedUrl(SEARCH_API+query+"&paginationLimit=" + to + "&start="+from+"&count="+count);
        console.info(url);

        caramel.render('loading','Loading assets from ' + from + ' to ' + to + '.', function( info , content ){
            $('.loading-animation-big').remove();
            $('body').append($(content));
        });

        $.ajax({
            url:url,
            method:'GET',
            success:function(data){

                var results = data.data || [];
                if(results.length==0) {
                        if(from == 0){
                            $('#search-results').html('We are sorry but we could not find any matching assets');
                        }
                        $('.loading-animation-big').remove();
                        doPagination = false;
                } else {
                    for (var index in results) {
                        var asset = results[index];
                        //Doing this because when there are no value specified in column such as thumbnail column it return string "null"
                        // value which need be explicitly set to null
                        if(asset.thumbnail == 'null') {
                            asset.thumbnail = null;
                        }
                    }
                    results = {assets:results,showType:true};
                    loadPartials('assets', function(partials) {
                        caramel.partials(partials, function () {
                            caramel.render('assets-thumbnails', results, function (info, content) {
                                $('#search-results').append($(content));
                                $('.loading-animation-big').remove();
                            });
                        });
                    });
                }
            },error:function(){
                doPagination = false;
                $('.loading-animation-big').remove();
            }
        });
    };
    store.infiniteScroll.showAll = function(query){
        store.infiniteScroll.addItemsToPage(query);
        $(window).scroll(function(){
            store.infiniteScroll.addItemsToPage(query);
        });
        $(window).resize(function () {
            //recalculate "rows_added"
            rows_added = store.infiniteScroll.recalculateRowsAdded();
            store.infiniteScroll.addItemsToPage(query);
        });
    };

	var processInputField = function(field){
		var result = field;
		switch(field.type) {
			case 'text':
				result = field;
				break;
			default:
				break;
		}
		return result;
	};
	var getInputFields = function(){
		var obj = {};
		var fields = $(SEARCH_FORM).find(':input');
		var field;
		for(var index = 0; index < fields.length; index++){
			field = fields[index];
			field = processInputField(field);
			if((field.name)&&(field.value)){
				obj[field.name] = field.value;
			}
		}
		return obj;
	};
	var createQueryString = function(key,value){
		return '"'+key+'":"'+value+'"';
	};
	var buildQuery = function(){
		var fields = getInputFields();
		var queryString =[];
		var value;
		for(var key in fields){
			value = fields[key];
			queryString.push(createQueryString(key,value));
		}
		return queryString.join(',');
	};
	var isEmptyQuery = function(query) {
		query = query.trim();
		return (query.length <= 0);
	};
    var loadPartials = function (partial, done) {
        $.ajax({
            url: caramel.url('/apis/partials') + '?partial=' + partial,
            success: function (data) {
                done(data);
            },
            error: function () {
                done(err);
            }
        });
    };
	$(SEARCH_BUTTON).on('click',function(e){
		e.preventDefault();
        doPagination = true;
        rows_added = 0;
        $('#search-results').html('');       
		var query = buildQuery();
		if(isEmptyQuery(query)) {
			console.log('User has not entered anything');
			return;
		}
        store.infiniteScroll.showAll(query);
	});
});