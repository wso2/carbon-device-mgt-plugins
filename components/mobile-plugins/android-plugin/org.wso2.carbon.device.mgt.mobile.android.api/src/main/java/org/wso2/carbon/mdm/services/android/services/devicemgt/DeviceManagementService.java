/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mdm.services.android.services.devicemgt;

import io.swagger.annotations.*;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.mdm.services.android.exception.AndroidAgentException;
import org.wso2.carbon.mdm.services.android.util.Message;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Android Device Management REST-API implementation.
 * All end points supports JSON, XMl with content negotiation.
 */

@Api(value = "DeviceManagementService", description = "Android Device Management REST-API implementation.")
@WebService
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public interface DeviceManagementService {


    /**
     * Get all devices.Returns list of Android devices registered in MDM.
     *
     * @return Device List
     * @throws org.wso2.carbon.mdm.services.android.exception.AndroidAgentException
     */
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of All Android Devices",
            notes = "Use this REST API to retrieve the details " +
                    "(e.g., the Android device type, serial number, International Mobile Station Equipment Identity " +
                    "(IMEI), owner, version, model etc.) of all Android devices that are registered with WSO2 EMM.",
            response = Device.class,
            responseContainer = "List"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of Devices"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list")
    })
    List<org.wso2.carbon.device.mgt.common.Device> getAllDevices() throws AndroidAgentException;

    /**
     * Fetch Android device details of a given device Id.
     *
     * @param id Device Id
     * @return Device
     * @throws org.wso2.carbon.mdm.services.android.exception.AndroidAgentException
     */
    @GET
    @Path("{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of an Android Device",
            notes = "Use this REST API to retrieve the details " +
                    "(e.g., the Android device type, serial number, International Mobile Station Equipment Identity " +
                    "(IMEI), owner, version, model etc.) of a specific Android device that is registered with WSO2 EMM",
            response = Device.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Information of the given device"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device information")
    })
    org.wso2.carbon.device.mgt.common.Device getDevice(@ApiParam(name = "id", value = "deviceIdentifier")
                                                       @PathParam("id") String id) throws AndroidAgentException;

    /**
     * Update Android device details of given device id.
     *
     * @param id     Device Id
     * @param device Device Details
     * @return Message
     * @throws AndroidAgentException
     */
    @PUT
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating the Details of an Android Device",
            notes = "Use this REST API to update the details of an Android device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "The device enrollment details has been updated successfully"),
            @ApiResponse(code = 500, message = "Error occurred while modifying the device information"),
            @ApiResponse(code = 304, message = "Device not found for the update")
    })
    @Path("{id}")
    Message updateDevice(@ApiParam(name = "id", value = "deviceIdentifier")
                         @PathParam("id") String id, @ApiParam(name = "device", value = "deviceIdentifier")
                                 Device device) throws AndroidAgentException;

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Updating an ApplicationList",
            notes = "Update application list in server side."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "Device information has modified successfully"),
            @ApiResponse(code = 500, message = "Error occurred while modifying the application list")
    })
    Message updateApplicationList(@ApiParam(name = "id", value = "deviceIdentifier") @PathParam("id") String id,
                                  @ApiParam(name = "applications", value = "updatable applications")
                                          List<Application> applications);

    @GET
    @Path("license")
    @Produces("text/html")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the License Agreement for Android Device Registration",
            notes = "Use this REST API to retrieve the license agreement that is used for the Android device " +
                    "registration process",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Licence agreement"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the license configured for Android " +
                    "device enrolment")
    })
    String getLicense() throws AndroidAgentException;
}
