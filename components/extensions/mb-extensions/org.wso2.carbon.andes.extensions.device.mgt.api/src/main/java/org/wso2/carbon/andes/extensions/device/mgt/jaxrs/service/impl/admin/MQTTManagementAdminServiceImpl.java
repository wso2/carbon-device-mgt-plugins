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
package org.wso2.carbon.andes.extensions.device.mgt.jaxrs.service.impl.admin;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.andes.admin.mqtt.internal.xsd.Subscription;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.service.api.admin.MQTTManagementAdminService;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.util.MQTTMgtAPIUtils;
import org.wso2.carbon.andes.mqtt.stub.AndesMQTTAdminServiceBrokerManagerAdminException;
import org.wso2.carbon.andes.mqtt.stub.AndesMQTTAdminServiceStub;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

@Path("/admin/topics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MQTTManagementAdminServiceImpl implements MQTTManagementAdminService {

    private static final Log log = LogFactory.getLog(MQTTManagementAdminServiceImpl.class);
    private
    @Context
    ServletConfig config;
    private
    @Context
    HttpServletRequest request;

    @Override
    @GET
    public Response getFilteredSubscriptions(
            @QueryParam("tenant-domain") String tenantDomain,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        try {
            int currentTenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (MultitenantConstants.SUPER_TENANT_ID != currentTenantId) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(
                                "Current logged in user is not authorized to perform this operation").build()).build();
            }
            return null;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
