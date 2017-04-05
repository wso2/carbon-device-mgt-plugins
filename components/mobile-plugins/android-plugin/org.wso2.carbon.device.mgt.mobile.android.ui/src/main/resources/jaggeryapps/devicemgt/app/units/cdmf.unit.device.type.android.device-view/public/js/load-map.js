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
var isAnalitics = false;
var marker;

function loadLeafletMap(refresh) {

    var deviceLocationID = "#device-location",
        locations = $(deviceLocationID).data("locations"),
        location_lat = $(deviceLocationID).data("lat"),
        location_long = $(deviceLocationID).data("long"),
        container = "device-location",
        zoomLevel = 13,
        tileSet = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
        attribution = "&copy; <a href='https://openstreetmap.org/copyright'>OpenStreetMap</a> contributors";

    if (refresh && !isAnalitics) {
        console.log("holaaaa");
        $("#map-spinner").removeClass("hidden");
        var applicationsList = $("#applications-list");
        var deviceId = applicationsList.data("device-id");
        var deviceType = applicationsList.data("device-type");
        invokerUtil.get(
            "/api/device-mgt/v1.0/devices/" + deviceType + "/" + deviceId + "/location",
            // success-callback
            function (data, textStatus, jqXHR) {
                if (jqXHR.status == 200 && data) {
                    data = JSON.parse(data);
                    if (data.latitude && data.longitude) {
                        map.removeLayer(marker);
                        // marker = L.marker([6.912853, 79.855635], {"opacity": opacVal}).addTo(map).bindPopup("Your device is here");
                        marker = L.marker([data.latitude, data.longitude], {"opacity": opacVal}).addTo(map).bindPopup("Your device is here");
                        map.panTo(new L.LatLng(data.latitude, data.longitude));
                        // map.panTo(new L.LatLng(40.737, -73.923));
                        marker.on('mouseover', function (e) {
                            this.openPopup();
                        });
                        marker.on('mouseout', function (e) {
                            this.closePopup();
                        });
                    }
                    $("#map-spinner").addClass("hidden");
                } else {
                    $("#map-spinner").adddClass("hidden");
                    $("#device-location").hide();
                    $("#map-error").show();
                }
            },
            // error-callback
            function () {
                $("#map-spinner").addClass("hidden");
                $("#device-location").hide();
                $("#map-error").show();
            });

    } else if (locations && locations.locations.length > 0) {
        isAnalitics = true;
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
        setTimeout(function () {
            map.invalidateSize()
        }, 400);

    } else if (location_long && location_lat) {
        map = L.map(container).setView([location_lat, location_long], zoomLevel);
        L.tileLayer(tileSet, {attribution: attribution}).addTo(map);

        marker = L.marker([location_lat, location_long], {"opacity": opacVal}).addTo(map).bindPopup("Your device is here");
        marker.on('mouseover', function (e) {
            this.openPopup();
        });
        marker.on('mouseout', function (e) {
            this.closePopup();
        });
        $("#map-error").hide();
        $("#device-location").show();
        setTimeout(function () {
            map.invalidateSize()
        }, 400);
    } else {
        $("#device-location").hide();
        $("#map-error").show();
    }
}

$(document).ready(function () {
    $(".location_tab").on("click", function () {
        loadLeafletMap(false);
    });

    $("#refresh-location").on("click", function () {
        loadLeafletMap(true);
    });
});
