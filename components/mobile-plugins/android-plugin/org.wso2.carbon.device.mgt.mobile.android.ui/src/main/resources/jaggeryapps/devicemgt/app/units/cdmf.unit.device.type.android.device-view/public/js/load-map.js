/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var map;

function loadLeafletMap() {

    var deviceLocationID = "#device-location",
        locations = $(deviceLocationID).data("locations"),
        location_lat = $(deviceLocationID).data("lat"),
        location_long = $(deviceLocationID).data("long"),
        container = "device-location",
        zoomLevel = 13,
        tileSet = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
        attribution = "&copy; <a href='https://openstreetmap.org/copyright'>OpenStreetMap</a> contributors";
    if (locations && locations.locations.length > 0) {

        var locationSets = locations.locations;
        map = L.map(container).setView([locationSets[0].lat, locationSets[0].lng], zoomLevel);
        L.tileLayer(tileSet, {attribution: attribution}).addTo(map);

        var initTime = locations.times[0].time, lastTime = locations.times[locationSets.length - 1].time;
        var totalTime = lastTime - initTime;
        for (var i = 0; i < locationSets.length; i++) {
            var opacVal = (locations.times[i].time - initTime) / totalTime;
            var m = L.marker(locationSets[i], {"opacity": opacVal}).addTo(map).bindPopup(new Date(locations.times[i].time).toISOString());
            m.on('mouseover', function (e) {
                this.openPopup();
            });
            m.on('mouseout', function (e) {
                this.closePopup();
            });
        }
        $("#map-error").hide();
        $("#device-location").show();
        setTimeout(function(){ map.invalidateSize()}, 400);

    } else if (location_long && location_lat) {

        map = L.map(container).setView([location_lat, location_long], zoomLevel);
        L.tileLayer(tileSet, {attribution: attribution}).addTo(map);

        var m = L.marker([location_lat, location_long], {"opacity": opacVal}).addTo(map).bindPopup("Your device is here");
        m.on('mouseover', function (e) {
            this.openPopup();
        });
        m.on('mouseout', function (e) {
            this.closePopup();
        });
        $("#map-error").hide();
        $("#device-location").show();
        setTimeout(function(){ map.invalidateSize()}, 400);
    } else {
        $("#device-location").hide();
        $("#map-error").show();
    }
}

$(document).ready(function () {
    $(".location_tab").on("click", function () {
        loadLeafletMap();
    });
});
