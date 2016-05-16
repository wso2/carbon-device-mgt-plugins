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

package org.wso2.carbon.device.mgt.iot.androidsense.service.impl;

import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
@Path("enrollment")
@API(name = "android_sense_mgt", version = "1.0.0", context = "/android_sense_mgt", tags = {"android_sense"})
public interface AndroidSenseManagerService {

    @Path("/devices/{device_id}")
    @POST
    @Permission(scope = "android_sense_user", permissions = {"/permission/admin/device-mgt/user/devices"})
    Response register(@PathParam("device_id") String deviceId, @QueryParam("deviceName") String deviceName);

    @Path("/devices/{device_id}")
    @DELETE
    @Permission(scope = "android_sense_user", permissions = {"/permission/admin/device-mgt/user/devices/remove"})
    Response removeDevice(@PathParam("device_id") String deviceId);

    @Path("/devices/{device_id}")
    @PUT
    @Permission(scope = "android_sense_user", permissions = {"/permission/admin/device-mgt/user/devices/update"})
    Response updateDevice(@PathParam("device_id") String deviceId, @QueryParam("name") String name);

    @Path("/devices/{device_id}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Permission(scope = "android_sense_user", permissions = {"/permission/admin/device-mgt/user/devices/list"})
    Response getDevice(@PathParam("device_id") String deviceId);

}

