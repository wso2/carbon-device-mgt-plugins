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
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MQTTMgtAPIUtils.getTenantId(tenantDomain));

            HttpSession session = request.getSession();

            AndesMQTTAdminServiceStub andesAdminStub = getAndesMQTTAdminServiceStub(config, session, request);

            Subscription[] filteredNormalTopicSubscriptionList = andesAdminStub.getFilteredSubscriptions(false, true,
                    "MQTT", "TOPIC", "", false,
                    "", false, "All", offset,
                    10);
            Map<String, Subscription[]> subscriptions = new HashMap<>();
            subscriptions.put("subscriptions", filteredNormalTopicSubscriptionList);
            return Response.ok().entity(subscriptions).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred at server side while fetching device list.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (AxisFault e) {
            String msg = "Error occurred at server side while fetching service stub.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (RemoteException e) {
            String msg = "Error occurred at server side while fetching service stub.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (AndesMQTTAdminServiceBrokerManagerAdminException e) {
            String msg = "Error occurred at server side while fetching service stub.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Gets the AndesAdminServices stub.
     *
     * @param config  the servlet configuration
     * @param session the http session
     * @param request the http servlet request
     * @return an AndesAdminServiceStub
     * @throws AxisFault
     */
    private static AndesMQTTAdminServiceStub getAndesMQTTAdminServiceStub(ServletConfig config,
                                                                          HttpSession session,
                                                                          HttpServletRequest request)
            throws AxisFault {

        String hostName = CarbonUtils.getServerConfiguration().getFirstProperty("HostName");
        final String MQTT_ENDPOINT = "9446";

        if (hostName == null) {
            hostName = System.getProperty("carbon.local.ip");
        }

        String backendServerURL = "https://" + hostName + ":" + MQTT_ENDPOINT + "/services/AndesMQTTAdminService.AndesMQTTAdminServiceHttpsSoap11Endpoint/";
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        AndesMQTTAdminServiceStub stub = new AndesMQTTAdminServiceStub(configContext, backendServerURL);
        HttpTransportProperties.Authenticator basicAuthentication = new HttpTransportProperties.Authenticator();
        basicAuthentication.setUsername("admin");
        basicAuthentication.setPassword("admin");
        stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, basicAuthentication);

        return stub;
    }

}
