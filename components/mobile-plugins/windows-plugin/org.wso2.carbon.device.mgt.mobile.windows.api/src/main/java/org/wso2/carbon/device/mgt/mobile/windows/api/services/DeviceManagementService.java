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

import org.w3c.dom.Document;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.PluginConstants;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsConfigurationException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsDeviceEnrolmentException;
import org.wso2.carbon.device.mgt.mobile.windows.api.operations.WindowsOperationException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Interface for Windows 10 Device management phase.
 */
@Path("/devicemgt")
@Scopes(
        scopes = {
                @Scope(
                        name = "Pending operations",
                        description = "Register an Windows device",
                        key = "perm:windows:enroll",
                        permissions = {"/device-mgt/devices/enroll/windows"}
                )
        }
)
public interface DeviceManagementService {
    @Path("/pending-operations")
    @POST
    @Consumes({PluginConstants.SYNCML_MEDIA_TYPE, MediaType.APPLICATION_XML})
    @Produces(PluginConstants.SYNCML_MEDIA_TYPE)
    Response getResponse(Document request) throws WindowsDeviceEnrolmentException, WindowsOperationException,
            NotificationManagementException, WindowsConfigurationException;

}

