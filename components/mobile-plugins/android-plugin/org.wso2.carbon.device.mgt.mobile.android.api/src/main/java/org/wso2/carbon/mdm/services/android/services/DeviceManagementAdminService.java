/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.mdm.services.android.services;

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.mdm.services.android.bean.wrapper.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@API(name = "Android Device Management Administrative Service", version = "1.0.0",
        context = "api/device-mgt/android/v1.0/admin/devices",
        tags = {"devicemgt_android"})

@Path("/admin/devices")
@Api(value = "Android Device Management Administrative Service", description = "Device management related admin APIs.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceManagementAdminService {

    @POST
    @Path("/lock-devices")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adds a Device Lock on Android Devices",
            notes = "Using this API you have the option of hard locking an Android device, where the Administrator " +
                    "permanently locks the device or screen locking an Android device",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Device lock operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new lock operation.")
    })
    @Scope(key = "device:android:operation:lock", name = "Lock device", description = "")
    Response configureDeviceLock(
            @ApiParam(name = "deviceLockBeanWrapper",
                    value = "Device lock configurations with device IDs") DeviceLockBeanWrapper deviceLockBeanWrapper);

    @POST
    @Path("/unlock-devices")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a Device Unlock on Android Devices",
            responseContainer = "List",
            notes = "Using this API you have the option of unlocking an Android device, where the Administrator " +
                    "unlocks the device",
            response = Activity.class,
            tags = "Android Device Management Administrative Service")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Device un-lock operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new un-lock operation.")
    })
    @Scope(key = "device:android:operation:unlock", name = "Unlock device", description = "")
    Response configureDeviceUnlock(
            @ApiParam(name = "deviceIDs", value = "DeviceIds to be enable device unlock operation")
            List<String> deviceIDs);


    @POST
    @Path("/location")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Requesting Location Coordinates of Android Devices",
            responseContainer = "List",
            notes = "Request location coordinates of Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service")
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Get-location operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new get-location operation.")})
    @Scope(key = "device:android:operation:location", name = "Get device location", description = "")
    Response getDeviceLocation(
            @ApiParam(name = "deviceIDs", value = "DeviceIDs to be requested to get device location")
            List<String> deviceIDs);

    @POST
    @Path("/clear-password")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Clearing the Password on Android Devices",
            notes = "Clear the password on Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Clear password operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new clear password operation.")
    })
    @Scope(key = "device:android:operation:clear-password", name = "Clear password of device", description = "")
    Response removePassword(
            @ApiParam(name = "deviceIDs",
                    value = "DeviceIds to be requested to remove password")  List<String> deviceIDs);

    @POST
    @Path("/control-camera")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Enabling or Disabling the Camera on Android Devices",
            notes = "Enable or disable the camera on Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Control camera operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new control camera operation.")
    })
    @Scope(key = "device:android:operation:camera", name = "Enable/Disable camera", description = "")
    Response configureCamera(
            @ApiParam(name = "cameraBeanWrapper", value = "Camera enable/disable configurations with device IDs")
            CameraBeanWrapper cameraBeanWrapper);

    @POST
    @Path("/info")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Requesting Information from Android Devices",
            notes = "Using this REST API you are able to request for Android device details. Once this REST API is" +
                    " executed it will be in the Android operation queue until the device calls the server to retrieve " +
                    "the list of operations that needs to be executed on the device",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Device info operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new device info operation.")
    })
    @Scope(key = "device:android:operation:info", name = "Get device information", description = "")
    Response getDeviceInformation(
            @ApiParam(name = "deviceIds", value = "Device IDs to be requested to get device information")
            List<String> deviceIDs);

    @POST
    @Path("/info")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Requesting Information from Android Devices",
            notes = "Using this REST API you are able to request for Android device details. Once this REST API is" +
                    " executed it will be in the Android operation queue until the device calls the server to retrieve " +
                    "the list of operations that needs to be executed on the device",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Device logcat operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                                  "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                                  "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                              "Server error occurred while adding a new device logcat operation.")
    })
    Response getDeviceLogcat(
            @ApiParam(name = "deviceIds", value = "Device IDs to be requested to get device logcat")
                    List<String> deviceIDs);

    @POST
    @Path("/enterprise-wipe")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Enterprise Wiping Android Devices",
            notes = "Enterprise wipe is the process of deleting enterprise related data on a device while keeping the " +
                    "personal data intact. You are able to enterprise wipe Android devices using this REST API",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Enterprise wipe operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a enterprise wipe operation.")})
    @Scope(key = "device:android:operation:enterprise-wipe", name = "Enterprise wipe", description = "")
    Response wipeDevice(@ApiParam(name = "deviceIDs", value = "Device IDs to be requested to do enterprise-wipe")
                        List<String> deviceIDs);

    @POST
    @Path("/wipe")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Factory Resetting an Android Device",
            notes = "Factory rest or erases all the data stored in the Android devices " +
                    "to restore them back to the original system",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Device wipe operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a device wipe operation.")})
    @Scope(key = "device:android:operation:wipe", name = "Factory reset device", description = "")
    Response wipeData(
            @ApiParam(name = "wipeDataBeanWrapper", value = "Configurations and DeviceIds needed to do wipe-data")
            WipeDataBeanWrapper wipeDataBeanWrapper);

    @POST
    @Path("/applications")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Requesting the List of Installed Applications on Android Devices",
            notes = "Using this REST API the server requests for the list of applications that are installed on" +
                    " the Android devices. Once this REST API is executed it will be in the Android operation queue " +
                    "until the device calls the server to retrieve the list of operations that needs to be executed " +
                    "on the device",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Get-applications operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new get-applications operation.")
    })
    @Scope(key = "device:android:operation:applications", name = "Get installed applications", description = "")
    Response getApplications(
            @ApiParam(name = "deviceIDs", value = "Device Ids needed to get applications that are already installed")
            List<String> deviceIDs);

    @POST
    @Path("/ring")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Ringing Android Devices",
            notes = "Ring Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Device ring operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new device ring operation.")
    })
    @Scope(key = "device:android:operation:ring", name = "Ring device", description = "")
    Response ringDevice(
            @ApiParam(name = "deviceIDs", value = "Device Ids needed for ring")
            List<String> deviceIDs);

    @POST
    @Path("/reboot")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Rebooting Android Devices",
            notes = "Reboot Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Device reboot operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new device reboot operation.")
    })
    @Scope(key = "device:android:operation:reboot", name = "Reboot device", description = "")
    Response rebootDevice(
            @ApiParam(name = "deviceIDs", value = "Device Ids needed for reboot.")
            List<String> deviceIDs);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Muting Android Devices",
            notes = "Mute Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Device mute operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new device mute operation.")
    })
    @Path("/mute")
    @Scope(key = "device:android:operation:mute", name = "Mute device", description = "")
    Response muteDevice(
            @ApiParam(name = "deviceIDs", value = "DeviceIDs need to be muted")
            List<String> deviceIDs);

    @POST
    @Path("/install-application")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Installing an Application on Android Devices",
            notes = "Install an application on an Android device. If the device you are installing the application" +
                    " on has the WSO2 system service installed, the application installation will happen in silent " +
                    "mode, else the device user's consent will be required",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Install application operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new install-application operation.")
    })
    @Scope(key = "device:android:operation:install-app", name = "Install applications", description = "")
    Response installApplication(
            @ApiParam(name = "applicationInstallationBeanWrapper", value = "Properties of installed apps and device IDs")
            ApplicationInstallationBeanWrapper applicationInstallationBeanWrapper);

    @POST
    @Path("/update-application")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Updating an Application on Android Devices",
            notes = "Update an application on an Android device. If the device you are updating the application" +
                    " has the WSO2 system service installed, the application update will happen in silent " +
                    "mode, else the device user's consent will be required",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Update-application operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new update-application operation.")
    })
    @Scope(key = "device:android:operation:update-app", name = "Update installed applications", description = "")
    Response updateApplication(
            @ApiParam(name = "applicationUpdateBeanWrapper", value = "Properties of updated apps and device IDs")
            ApplicationUpdateBeanWrapper applicationUpdateBeanWrapper);

    @POST
    @Path("/uninstall-application")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Uninstalling an Application from Android Devices",
            notes = "Uninstall an application from Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Uninstall-application operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new uninstall-application operation.")
    })
    @Scope(key = "device:android:operation:uninstall-app", name = "Uninstall applications", description = "")
    Response uninstallApplication(
            @ApiParam(name = "applicationUninstallationBeanWrapper",
                    value = "applicationUninstallationConfigs and Device Ids")
            ApplicationUninstallationBeanWrapper applicationUninstallationBeanWrapper);

    @POST
    @Path("/blacklist-applications")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Get BlackListed Applications",
            notes = "Getting BlackListed Applications",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Blacklist-applications operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new blacklist-applications operation.")
    })
    @Scope(key = "device:android:operation:blacklist-app", name = "Blacklist applications", description = "")
    Response blacklistApplications(
            @ApiParam(name = "blacklistApplicationsBeanWrapper", value = "BlacklistApplications " +
                    "Configuration and DeviceIds")
            BlacklistApplicationsBeanWrapper blacklistApplicationsBeanWrapper);

    @POST
    @Path("/upgrade-firmware")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Upgrading device firmware",
            notes = "Device firmware upgrade",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Upgrade firmware operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new upgrade firmware operation.")
    })
    @Scope(key = "device:android:operation:upgrade", name = "Upgrade firmware", description = "")
    Response upgradeFirmware(
            @ApiParam(name = "upgradeFirmwareBeanWrapper",
                    value = "Firmware upgrade configuration and DeviceIds")
            UpgradeFirmwareBeanWrapper upgradeFirmwareBeanWrapper);

    @POST
    @Path("/configure-vpn")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Configuring VPN on Android devices",
            notes = "Configure VPN on Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Configure VPN operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new configure VPN operation.")
    })
    @Scope(key = "device:android:operation:vpn", name = "Add VPN profiles", description = "")
    Response configureVPN(
            @ApiParam(name = "vpnBeanWrapper",
                    value = "VPN configuration and DeviceIds")
            VpnBeanWrapper vpnBeanWrapper);

    @POST
    @Path("/send-notification")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Sending a Notification to Android Devices",
            notes = "Send a notification to Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Send notification operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new send notification operation.")
    })
    @Scope(key = "device:android:operation:notification", name = "Send notifications", description = "")
    Response sendNotification(
            @ApiParam(name = "notificationBeanWrapper",
                    value = "Notification Configurations and device Ids")
            NotificationBeanWrapper notificationBeanWrapper);

    @POST
    @Path("/configure-wifi")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Configuring Wi-Fi on Android Devices",
            notes = "Configure Wi-Fi on Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Configure wifi operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new configure wifi operation.")
    })
    @Scope(key = "device:android:operation:wifi", name = "Add WiFi configurations", description = "")
    Response configureWifi(
            @ApiParam(name = "wifiBeanWrapper",
                    value = "WifiConfigurations and Device Ids") WifiBeanWrapper wifiBeanWrapper);

    @POST
    @Path("/encrypt-storage")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Encrypting Storage on Android Devices",
            notes = "Encrypt the data stored on Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Encrypt storage operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new encrypt storage operation.")
    })
    @Scope(key = "device:android:operation:encrypt", name = "Encrypt device", description = "")
    Response encryptStorage(
            @ApiParam(name = "encryptionBeanWrapper",
                    value = "Configurations and deviceIds need to be done data encryption")
            EncryptionBeanWrapper encryptionBeanWrapper);

    @POST
    @Path("/change-lock-code")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Changing the Lock Code on Android Devices",
            notes = "Change the lock code on Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Change lock code operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new change lock code operation.")
    })
    @Scope(key = "device:android:operation:change-lock", name = "Change password of device", description = "")
    Response changeLockCode(
            @ApiParam(name = "lockCodeBeanWrapper",
                    value = "Configurations and device Ids need to be done change lock code")
            LockCodeBeanWrapper lockCodeBeanWrapper);

    @POST
    @Path("/set-password-policy")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Setting a Passcode Policy on Android Devices",
            notes = "Set a password policy on Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Set password policy operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new set password policy operation.")
    })
    @Scope(key = "device:android:operation:password-policy", name = "Set password policy", description = "")
    Response setPasswordPolicy(
            @ApiParam(name = "passwordPolicyBeanWrapper",
                    value = "Password Policy Configurations and Device Ids")
            PasswordPolicyBeanWrapper passwordPolicyBeanWrapper);

    @POST
    @Path("set-webclip")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Setting a Web Clip on Android Devices",
            notes = "Set a web clip on Android devices. A web clip is used to add a bookmark to a web application",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Set webclip operation has successfully been scheduled",
                    response = Activity.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n Source can be retrieved from the URL specified at the Location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The entity of the request was in a not supported format."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while adding a new set webclip operation.")
    })
    @Scope(key = "device:android:operation:webclip", name = "Add webclips", description = "")
    Response setWebClip(
            @ApiParam(name = "webClipBeanWrapper",
                    value = "Configurations to need set web clip on device and device Ids")
            WebClipBeanWrapper webClipBeanWrapper);


}
