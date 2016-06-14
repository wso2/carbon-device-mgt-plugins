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

package org.wso2.carbon.mdm.mobileservices.windows.services.devicemgtservice;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.mdm.mobileservices.windows.common.exceptions.WindowsConfigurationException;
import org.wso2.carbon.mdm.mobileservices.windows.common.util.Message;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Windows Device Management REST-API implementation.
 * All end points supports JSON, XMl with content negotiation.
 */
@Api(value = "DeviceManagementService", description = "Windows Device Management REST-API implementation.")
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public interface DeviceManagementService {

    /**
     * Get all devices.Returns list of Windows devices registered in MDM.
     *
     * @return Device List
     * @throws WindowsConfigurationException
     */
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of All Windows Devices",
            notes = "Use this REST API to retrieve the details " +
                    "(e.g., the Android device type, serial number, International Mobile Station Equipment Identity " +
                    "(IMEI), owner, version, model etc.) of all Windows devices that are registered with WSO2 EMM.",
            response = Device.class,
            responseContainer = "List"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of Devices"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device list")
    })
    List<Device> getAllDevices() throws WindowsConfigurationException;

    /**
     * Fetch Windows device details of a given device Id.
     *
     * @param id Device Id
     * @return Device
     * @throws WindowsConfigurationException
     */
    @GET
    @Path("{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of an Windows Device",
            notes = "Use this REST API to retrieve the details " +
                    "(e.g., the Android device type, serial number, International Mobile Station Equipment Identity " +
                    "(IMEI), owner, version, model etc.) of a specific Windows device that is registered with WSO2 EMM",
            response = Device.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Information of the given device"),
            @ApiResponse(code = 500, message = "Error occurred while fetching the device information")
    })
    org.wso2.carbon.device.mgt.common.Device getDevice(@PathParam("id") String id)
            throws WindowsConfigurationException;

    /**
     * Update Windows device details of given device id.
     *
     * @param id     Device Id
     * @param device Device Details
     * @return Message
     * @throws WindowsConfigurationException
     */
    @PUT
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating the Details of an Windows Device",
            notes = "Use this REST API to update the details of an Windows device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "The device enrollment details has been updated successfully"),
            @ApiResponse(code = 500, message = "Error occurred while modifying the device information"),
            @ApiResponse(code = 304, message = "Device not found for the update")
    })
    @Path("{id}")
    Message updateDevice(@PathParam("id") String id, Device device) throws WindowsConfigurationException;

    @GET
    @Path("license")
    @Produces("application/json")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the License Agreement for Windows Device Registration",
            notes = "Use this REST API to retrieve the license agreement that is used for the Windows device " +
                    "registration process",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Licence agreement"),
            @ApiResponse(code = 500, message = "Error occurred while retrieving the license configured for Windows " +
                    "device enrolment")
    })
    License getLicense() throws WindowsConfigurationException;
}

