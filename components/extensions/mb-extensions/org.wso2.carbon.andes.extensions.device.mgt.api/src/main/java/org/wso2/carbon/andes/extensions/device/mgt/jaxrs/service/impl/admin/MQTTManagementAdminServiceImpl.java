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
import org.wso2.carbon.andes.core.types.xsd.MQTTSubscription;
import org.wso2.carbon.andes.core.types.xsd.Subscription;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.beans.SubscriptionList;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.service.api.admin.MQTTManagementAdminService;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.andes.mqtt.stub.AndesMQTTAdminServiceBrokerManagerAdminException;
import org.wso2.carbon.andes.mqtt.stub.AndesMQTTAdminServiceStub;
import org.wso2.carbon.context.CarbonContext;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.rmi.RemoteException;
import java.util.*;

@Path("/admin/topics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MQTTManagementAdminServiceImpl implements MQTTManagementAdminService {

    private static final Log log = LogFactory.getLog(MQTTManagementAdminServiceImpl.class);
    private static final String PROTOCOL_TYPE = "MQTT";
    private static final String DESTINATION_TYPE = "TOPIC";
    private
    @Context
    ServletConfig config;
    private
    @Context
    HttpServletRequest request;

    @Override
    @GET
    public Response getFilteredSubscriptions(
            @QueryParam("name") String topic_name,
            @QueryParam("active") String active,
            @QueryParam("durable") String durable,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        try {

            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            AndesMQTTAdminServiceStub andesAdminStub = getAndesMQTTAdminServiceStub(config);
            MQTTSubscription mqttSubscription = new MQTTSubscription();
            mqttSubscription.setFilteredNamePattern("");
            mqttSubscription.setDurable(false);
            mqttSubscription.setActive(true);
            mqttSubscription.setProtocolType(PROTOCOL_TYPE);
            mqttSubscription.setDestinationType(DESTINATION_TYPE);
            mqttSubscription.setFilteredNameByExactMatch(false);
            mqttSubscription.setIdentifierPattern("");
            mqttSubscription.setIdentifierPatternByExactMatch(false);
            mqttSubscription.setOwnNodeId("All");
            mqttSubscription.setPageNumber(offset);
            mqttSubscription.setSubscriptionCountPerPage(limit);

            if (topic_name != null && !topic_name.isEmpty()) {
                mqttSubscription.setFilteredNamePattern(topic_name);
            }
            if (active != null && !active.isEmpty()) {
                mqttSubscription.setActive(Boolean.parseBoolean(active));
            }
            if (durable != null && !durable.isEmpty()) {
                mqttSubscription.setDurable(Boolean.parseBoolean(durable));
            }

            Subscription[] filteredNormalTopicSubscriptionList = andesAdminStub.getFilteredSubscriptions(
                    mqttSubscription, tenantDomain);
            SubscriptionList topics = new SubscriptionList();

            if (filteredNormalTopicSubscriptionList != null) {
                topics.setList(Arrays.asList(filteredNormalTopicSubscriptionList));
                topics.setCount(filteredNormalTopicSubscriptionList.length);
            }

            return Response.status(Response.Status.OK).entity(topics).build();
        } catch (RemoteException | AndesMQTTAdminServiceBrokerManagerAdminException e) {
            String msg = "Error occurred at server side while fetching topic list.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * Gets the AndesAdminServices stub.
     *
     * @param config  the servlet configuration
     * @return an AndesAdminServiceStub
     * @throws AxisFault
     */
    private static AndesMQTTAdminServiceStub getAndesMQTTAdminServiceStub(ServletConfig config)
            throws AxisFault {

        String hostName = System.getProperty("mqtt.broker.host");
        final String MQTT_ENDPOINT = System.getProperty("mqtt.broker.https.port");

        if (hostName == null) {
            hostName = System.getProperty("mqtt.broker.host");
        }

        String backendServerURL = "https://" + hostName + ":" + MQTT_ENDPOINT +
                "/services/AndesMQTTAdminService.AndesMQTTAdminServiceHttpsSoap11Endpoint/";
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        AndesMQTTAdminServiceStub stub = new AndesMQTTAdminServiceStub(configContext, backendServerURL);

        // TODO: Need to use JWT Authenticator instead of Basic Auth
        HttpTransportProperties.Authenticator basicAuthentication = new HttpTransportProperties.Authenticator();
        basicAuthentication.setUsername("admin");
        basicAuthentication.setPassword("admin");
        stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, basicAuthentication);

        return stub;
    }

}
