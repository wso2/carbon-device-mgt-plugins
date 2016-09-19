/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.mobile.windows.api.services;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsConfigurationException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.util.Message;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Windows Device Management REST-API implementation.
 * All end points supports JSON, XMl with content negotiation.
 */
@WebService
@Path("/devices")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public interface DeviceManagementService {

    /**
     * Get all devices.Returns list of Windows devices registered in MDM.
     *
     * @return Returns retrieved devices.
     * @throws WindowsConfigurationException occurred while retrieving all the devices from DB.
     */
    @GET
    List<Device> getAllDevices() throws WindowsConfigurationException;

    /**
     * Fetch Windows device details of a given device Id.
     *
     * @param deviceId Device Id
     * @return Returns retrieved device.
     * @throws WindowsConfigurationException occurred while getting device from DB.
     */
    @GET
    @Path("{id}")
    Device getDevice(@PathParam("id") String deviceId) throws WindowsConfigurationException;

    /**
     * Update Windows device details of given device id.
     *
     * @param deviceId     Device Id.
     * @param device Device details to be updated.
     * @return Returns the message whether device update or not.
     * @throws WindowsConfigurationException occurred while updating the Device Info.
     */
    @PUT
    @Path("{id}")
    Message updateDevice(@PathParam("id") String deviceId, Device device) throws WindowsConfigurationException;
    /**
     * Fetch the Licence agreement for specific windows platform.
     *
     * @return Returns License agreement.
     * @throws WindowsConfigurationException occurred while getting licence for specific platform and Language.
     */
    @GET
    @Path("license")
    @Produces("application/json")
    License getLicense() throws WindowsConfigurationException;
}
