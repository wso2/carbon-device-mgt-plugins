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

package org.wso2.carbon.device.mgt.mobile.windows.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsConfigurationException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.util.Message;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.util.WindowsAPIUtils;
import org.wso2.carbon.device.mgt.mobile.windows.api.services.PolicyManagementService;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@WebService
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class PolicyManagementServiceImpl implements PolicyManagementService {
    private static Log log = LogFactory.getLog(
            org.wso2.carbon.device.mgt.mobile.windows.api.services.impl.PolicyManagementServiceImpl.class);

    @GET
    @Path("{deviceId}")
    public Message getEffectivePolicy(@HeaderParam("Accept") String acceptHeader,
                                      @PathParam("deviceId") String deviceId) throws WindowsConfigurationException {

        DeviceIdentifier deviceIdentifier = WindowsAPIUtils.convertToDeviceIdentifierObject(deviceId);
        Message responseMessage = new Message();
        Policy policy;
        try {
            PolicyManagerService policyManagerService = WindowsAPIUtils.getPolicyManagerService();
            policy = policyManagerService.getEffectivePolicy(deviceIdentifier);
            if (policy == null) {
                responseMessage.setResponseCode(Response.Status.NO_CONTENT.toString());
                responseMessage.setResponseMessage("No effective policy found");
                return responseMessage;
            } else {
                responseMessage.setResponseCode(Response.Status.OK.toString());
                responseMessage.setResponseMessage("Effective policy added to operation");
                return responseMessage;
            }

        } catch (PolicyManagementException e) {
            String msg = "Error occurred while getting the policy.";
            log.error(msg, e);
            throw new WindowsConfigurationException(msg, e);
        }
    }
}
