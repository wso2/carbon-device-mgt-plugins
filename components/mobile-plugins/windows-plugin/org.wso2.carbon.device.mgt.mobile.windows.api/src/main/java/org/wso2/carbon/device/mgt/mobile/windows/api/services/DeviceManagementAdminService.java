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

import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsDeviceEnrolmentException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Interface for Admin operations persisting. This interface accepts operations added via UI.
 */
@Path("/admin/devices")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface DeviceManagementAdminService {

    @POST
    @Path("/lock-devices")
    @Permission(name = "Lock Device", permission = "/device-mgt/devices/owning/operations/windows/lock")
    Response lock(@HeaderParam("Accept") String headerParam, List<String> deviceIds) throws
                                                                                     WindowsDeviceEnrolmentException;

    @POST
    @Path("/disenroll-devices")
    @Permission(name = "Disenroll Device", permission = "/device-mgt/devices/disenroll/windows")
    Response disenroll(@HeaderParam("Accept") String headerParam, List<String> deviceIds) throws
                                                                                          WindowsDeviceEnrolmentException;

    @POST
    @Path("/wipe-devices")
    @Permission(name = "Wipe Device", permission = "/device-mgt/devices/owning/operations/windows/wipe")
    Response wipe(@HeaderParam("Accept") String headerParam, List<String> deviceIds) throws
                                                                                     WindowsDeviceEnrolmentException;

    @POST
    @Path("/ring-devices")
    @Permission(name = "Ring Device", permission = "/device-mgt/devices/owning/operations/windows/ring")
    Response ring(@HeaderParam("Accept") String headerParam, List<String> deviceIds) throws
                                                                                     WindowsDeviceEnrolmentException;

    @POST
    @Path("/lockreset-devices")
    @Permission(name = "Lock-Reset Device", permission = "/device-mgt/devices/owning/operations/windows/lockreset")
    Response lockReset(@HeaderParam("Accept") String acceptHeader, List<String> deviceIds)
            throws WindowsDeviceEnrolmentException;
}
