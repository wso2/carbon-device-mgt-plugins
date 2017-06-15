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
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.juddi.rmi.UDDISubscriptionService;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.andes.admin.mqtt.internal.xsd.Subscription;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.beans.TopicList;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.beans.TopicPaginationRequest;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.exception.AuthenticationException;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.service.api.admin.MQTTManagementAdminService;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.service.impl.util.TopicManagerUtil;
import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.util.MQTTMgtAPIUtils;
import org.wso2.carbon.andes.mqtt.stub.AndesMQTTAdminServiceBrokerManagerAdminException;
import org.wso2.carbon.andes.mqtt.stub.AndesMQTTAdminServiceStub;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsMgtDAOException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;


import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.rmi.RemoteException;
import java.security.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static sun.audio.AudioDevice.device;

@Path("/admin/topics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MQTTManagementAdminServiceImpl implements MQTTManagementAdminService {

    private static final Log log = LogFactory.getLog(MQTTManagementAdminServiceImpl.class);
    /**
     * required soap header for authorization
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * required soap header value for mutualSSL
     */
    private static final String AUTHORIZATION_HEADER_VALUE = "Bearer";

    private static final String KEY_STORE_TYPE = "JKS";
    /**
     * Default truststore type of the client
     */
    private static final String TRUST_STORE_TYPE = "JKS";
    /**
     * Default keymanager type of the client
     */
    private static final String KEY_MANAGER_TYPE = "SunX509"; //Default Key Manager Type
    /**
     * Default trustmanager type of the client
     */
    private static final String TRUST_MANAGER_TYPE = "SunX509"; //Default Trust Manager Type

    private static final String SSLV3 = "SSLv3";



    private KeyStore keyStore;
    private KeyStore trustStore;
    private char[] keyStorePassword;
    private SSLContext sslContext;

    private String tenantDomain;

    private static final String DEFAULT_RESOURCE_LOCATION = "/resources/devicetypes";
    private static final String CAR_FILE_LOCATION = CarbonUtils.getCarbonHome() + File.separator + "repository" +
            File.separator + "resources" + File.separator + "devicetypes";
    private static final String DAS_PORT = "${iot.analytics.https.port}";
    private static final String DAS_HOST_NAME = "${iot.analytics.host}";
    private static final String DEFAULT_HTTP_PROTOCOL = "https";
    private static final String IOT_MGT_PORT = "${iot.manager.https.port}";
    private static final String IOT_MGT_HOST_NAME = "${iot.manager.host}";
    private static final String DAS_URL = DEFAULT_HTTP_PROTOCOL + "://" + DAS_HOST_NAME
            + ":" + DAS_PORT + "/services/CarbonAppUploader/";
    private static final String DAS_EVENT_RECEIVER_EP = DEFAULT_HTTP_PROTOCOL + "://" + DAS_HOST_NAME
            + ":" + DAS_PORT + "/services/EventReceiverAdminService/";
    private static final String DAS_EVENT_STREAM_EP = DEFAULT_HTTP_PROTOCOL + "://" + DAS_HOST_NAME
            + ":" + DAS_PORT + "/services/EventStreamAdminService/";

    private static final String IOT_MGT_URL = DEFAULT_HTTP_PROTOCOL + "://" + IOT_MGT_HOST_NAME
            + ":" + IOT_MGT_PORT + "/services/CarbonAppUploader/";
    private static final String MEDIA_TYPE_XML = "application/xml";
    private static final String DEVICE_MANAGEMENT_TYPE = "device_management";
    private static final String TENANT_DOMAIN_PROPERTY = "\\$\\{tenant-domain\\}";
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
            @QueryParam("remaining_messages") int remaining_messages,
            @QueryParam("active") String active,
            @QueryParam("durable") String durable,
            @QueryParam("subscriber_name") String subscriber_name,
            @QueryParam("identifier") String identifier,
            @QueryParam("tenant-domain") String tenantDomain,
            @QueryParam("since") String since,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        try {
            if (!StringUtils.isEmpty(topic_name) && !StringUtils.isEmpty(active)) {
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("Request contains both name and role " +
                                "parameters. Only one is allowed " +
                                "at once.").build()).build();
            }
            int currentTenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (MultitenantConstants.SUPER_TENANT_ID != currentTenantId) {
                return Response.status(Response.Status.UNAUTHORIZED).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(
                                "Current logged in user is not authorized to perform this operation").build()).build();
            }
            String ten_Domain = getTenantDomain(currentTenantId);
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(ten_Domain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MQTTMgtAPIUtils.getTenantId(ten_Domain));


            RequestValidationUtil.validatePaginationParameters(offset, limit);
            HttpSession session = request.getSession();
            AndesMQTTAdminServiceStub andesAdminStub = getAndesMQTTAdminServiceStub(config, session, request);
            TopicPaginationRequest request = new TopicPaginationRequest(offset, limit);
            PaginationResult result;
            TopicList topics = new TopicList();

            if (topic_name != null && !topic_name.isEmpty()) {
                request.setTopic_name(topic_name);
            }
            if (remaining_messages != 0) {
                request.setRemaining_messages(remaining_messages);
            }
            if (active != null && !active.isEmpty()) {
                request.setActive(active);
            }
            if (durable != null && !durable.isEmpty()) {
                request.setDurable(durable);
            }
            if (subscriber_name != null && !subscriber_name.isEmpty()) {
                request.setSubscriber_name(subscriber_name);
            }
            if (identifier != null && !identifier.isEmpty()) {
                request.setIdentifier(identifier);
            }

//            Subscription[] filteredNormalTopicSubscriptionList = andesAdminStub.getFilteredMQTTSubscriptions(false, true,
//                    "MQTT", "TOPIC", "", false,
//                    "", false, "All", offset,
//                    10,currentTenantId,tenantDomain);

            Subscription[] filteredNormalTopicSubscriptionList = andesAdminStub.getFilteredSubscriptions(false, true,
                    "MQTT", "TOPIC", topic_name, false,
                    "", false, "All", offset,
                    10);
            Map<String, Subscription[]> subscriptions = new HashMap<>();
            subscriptions.put("subscriptions", filteredNormalTopicSubscriptionList);

            //getPaginatedTopics(request,subscriptions);

            if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                Date sinceDate;
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(ifModifiedSince);
                } catch (ParseException e) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date " +
                                    "string is provided in 'If-Modified-Since' header").build()).build();
                }
                request.setSince(sinceDate);
                result = getPaginatedTopics(request,subscriptions);
                if (result == null || result.getData() == null || result.getData().size() <= 0) {
                    return Response.status(Response.Status.NOT_MODIFIED).entity("No device is modified " +
                            "after the timestamp provided in 'If-Modified-Since' header").build();
                }
            } else if (since != null && !since.isEmpty()) {
                Date sinceDate;
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(since);
                } catch (ParseException e) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date " +
                                    "string is provided in 'since' filter").build()).build();
                }
                request.setSince(sinceDate);
                result = getPaginatedTopics(request,subscriptions);
                if (result == null || result.getData() == null || result.getData().size() <= 0) {
                    topics.setList(new ArrayList<Subscription>());
                    topics.setCount(0);
                    return Response.status(Response.Status.OK).entity(topics).build();
                }
            } else {
                result = getPaginatedTopics(request,subscriptions);
                int resultCount = result.getRecordsTotal();
                if (resultCount == 0) {
                    Response.status(Response.Status.OK).entity(topics).build();
                }
            }

            topics.setList((List<Subscription>) result.getData());
            topics.setCount(result.getRecordsTotal());


            return Response.status(Response.Status.OK).entity(subscriptions).build();
//            return Response.ok().entity(subscriptions).build();
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
        } catch (AuthenticationException e) {
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


    private PaginationResult getPaginatedTopics(TopicPaginationRequest request,Map<String, Subscription[]> subscriptions) throws DeviceManagementException {
        PaginationResult paginationResult = new PaginationResult();
        List<Subscription> allSubscriptions =  new ArrayList<>();
        Map<String,Subscription[]> ss = subscriptions;
        int count = 0;
        int tenantId = this.getTenantId();
        request = TopicManagerUtil.validateTopicListPageSize(request);

        List<Subscription[]> subscriptionList = new ArrayList<>(subscriptions.values());

//        allSubscriptions = Arrays.asList(subscriptionList.size());
        count = subscriptionList.size();

//        paginationResult.setData(allSubscriptions);
        paginationResult.setRecordsFiltered(count);
        paginationResult.setRecordsTotal(count);
        return paginationResult;
    }

    private int getTenantId() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private static String getTenantDomain(int tenantId) throws AuthenticationException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();

            RealmService realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
            if (realmService == null) {
                String msg = "RealmService is not initialized";
                log.error(msg);
                throw new AuthenticationException(msg);
            }

            return realmService.getTenantManager().getDomain(tenantId);

        } catch (UserStoreException e) {
            String msg = "User store not initialized";
            log.error(msg);
            throw new AuthenticationException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Initializes the SSL Context
     */
    private void initSSLConnection() throws NoSuchAlgorithmException, UnrecoverableKeyException,
            KeyStoreException, KeyManagementException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KEY_MANAGER_TYPE);
        keyManagerFactory.init(keyStore, keyStorePassword);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TRUST_MANAGER_TYPE);
        trustManagerFactory.init(trustStore);

        // Create and initialize SSLContext for HTTPS communication
        sslContext = SSLContext.getInstance(SSLV3);
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        SSLContext.setDefault(sslContext);
    }

    private void cleanup(Stub stub) {
        if (stub != null) {
            try {
                stub.cleanup();
            } catch (AxisFault axisFault) {
                //do nothing
            }
        }
    }

}
