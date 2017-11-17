/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.device.mgt.output.adapter.http;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.output.adapter.http.internal.OutputAdapterServiceDataHolder;
import org.wso2.carbon.device.mgt.output.adapter.http.util.HTTPConnectionConfiguration;
import org.wso2.carbon.device.mgt.output.adapter.http.util.HTTPEventAdapterConstants;
import org.wso2.carbon.device.mgt.output.adapter.http.util.HTTPUtil;
import org.wso2.carbon.device.mgt.output.adapter.http.util.RegistrationProfile;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(OutputEventAdapter.class);

    private ExecutorService executorService;
    private HttpConnectionManager connectionManager;
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private String clientMethod;
    private int tenantId;
    private HTTPConnectionConfiguration httpConnectionConfiguration;
    private String contentType;
    private HttpClient httpClient = null;
    private HostConfiguration hostConfiguration = null;
    private String clientId;
    private String clientSecret;


    public HTTPEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                            Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
        this.clientMethod = eventAdapterConfiguration.getStaticProperties()
                .get(HTTPEventAdapterConstants.ADAPTER_HTTP_CLIENT_METHOD);
    }

    @Override
    public void init() throws OutputEventAdapterException {

        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        //ExecutorService will be assigned  if it is null
        if (executorService == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;
            int jobQueSize;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME) != null) {
                minThread = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME));
            } else {
                minThread = HTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME) != null) {
                maxThread = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME));
            } else {
                maxThread = HTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = HTTPEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
                jobQueSize = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME));
            } else {
                jobQueSize = HTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE;
            }
            executorService = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime, TimeUnit.MILLISECONDS,
                                                     new LinkedBlockingQueue<>(jobQueSize));

            //configurations for the httpConnectionManager which will be shared by every http adapter
            int defaultMaxConnectionsPerHost;
            int maxTotalConnections;

            if (globalProperties.get(HTTPEventAdapterConstants.DEFAULT_MAX_CONNECTIONS_PER_HOST) != null) {
                defaultMaxConnectionsPerHost = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.DEFAULT_MAX_CONNECTIONS_PER_HOST));
            } else {
                defaultMaxConnectionsPerHost = HTTPEventAdapterConstants.DEFAULT_DEFAULT_MAX_CONNECTIONS_PER_HOST;
            }

            if (globalProperties.get(HTTPEventAdapterConstants.MAX_TOTAL_CONNECTIONS) != null) {
                maxTotalConnections = Integer
                        .parseInt(globalProperties.get(HTTPEventAdapterConstants.MAX_TOTAL_CONNECTIONS));
            } else {
                maxTotalConnections = HTTPEventAdapterConstants.DEFAULT_MAX_TOTAL_CONNECTIONS;
            }

            connectionManager = new MultiThreadedHttpConnectionManager();
            connectionManager.getParams().setDefaultMaxConnectionsPerHost(defaultMaxConnectionsPerHost);
            connectionManager.getParams().setMaxTotalConnections(maxTotalConnections);
        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("Test connection is not available");
    }

    @Override
    public void connect() {
        this.checkHTTPClientInit();
        httpConnectionConfiguration =
                new HTTPConnectionConfiguration(eventAdapterConfiguration, globalProperties);
        generateToken();
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {
        //Load dynamic properties
        String url = dynamicProperties.get(HTTPEventAdapterConstants.ADAPTER_MESSAGE_URL);
        Map<String, String> headers = this
                .extractHeaders(dynamicProperties.get(HTTPEventAdapterConstants.ADAPTER_HEADERS));
        String payload = message.toString();

        if ("true".equals(dynamicProperties.get(HTTPEventAdapterConstants.ADAPTER_MESSAGE_URL_TEMPLATED))) {
            try {
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonPayload = (JSONObject) jsonParser.parse(payload);

                List<String> matchList = new ArrayList<>();
                Pattern regex = Pattern.compile("\\{(.*?)\\}");
                Matcher regexMatcher = regex.matcher(url);

                while (regexMatcher.find()) {//Finds Matching Pattern in String
                    matchList.add(regexMatcher.group(1));//Fetching Group from String
                }

                for(String str:matchList) {
                    if (jsonPayload.containsKey(str)) {
                        url = url.replace("{" + str + "}", jsonPayload.get(str).toString());
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Modified url: " + url);
                }
            } catch (ParseException e) {
                log.error("Unable to parse request body to Json.", e);
            }
        }

        try {
            executorService.submit(new HTTPSender(url, payload, headers, httpClient));
        } catch (RejectedExecutionException e) {
            EventAdapterUtil
                    .logAndDrop(eventAdapterConfiguration.getName(), message, "Job queue is full", e, log, tenantId);
        }
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public void destroy() {
        //not required
    }

    @Override
    public boolean isPolled() {
        return false;
    }

    private void checkHTTPClientInit() {

        if (this.httpClient != null) {
            return;
        }

        synchronized (HTTPEventAdapter.class) {
            if (this.httpClient != null) {
                return;
            }

            httpClient = new HttpClient(connectionManager);

            String messageFormat = eventAdapterConfiguration.getMessageFormat();
            if (messageFormat.equalsIgnoreCase("json")) {
                contentType = "application/json";
            } else if (messageFormat.equalsIgnoreCase("text")) {
                contentType = "text/plain";
            } else {
                contentType = "text/xml";
            }
        }
    }

    private Map<String, String> extractHeaders(String headers) {
        if (headers == null || headers.trim().length() == 0) {
            return null;
        }

        String[] entries = headers.split(HTTPEventAdapterConstants.HEADER_SEPARATOR);
        String[] keyValue;
        Map<String, String> result = new HashMap<>();
        for (String header : entries) {
            try {
                keyValue = header.split(HTTPEventAdapterConstants.ENTRY_SEPARATOR, 2);
                result.put(keyValue[0].trim(), keyValue[1].trim());
            } catch (Exception e) {
                log.warn("Header property '" + header + "' is not defined in the correct format.", e);
            }
        }
        return result;
    }

    private void generateToken() {
        String username = httpConnectionConfiguration.getUsername();
        String password = httpConnectionConfiguration.getPassword();
        String dcrUrlString = httpConnectionConfiguration.getDcrUrl();

        if (dcrUrlString != null && !dcrUrlString.isEmpty()) {
            try {
                URL dcrUrl = new URL(dcrUrlString);
                org.apache.http.client.HttpClient dcrHttpClient = HTTPUtil.getHttpClient(dcrUrl.getProtocol());
                HttpPost postMethod = new HttpPost(dcrUrlString);
                RegistrationProfile registrationProfile = new RegistrationProfile();
                registrationProfile.setCallbackUrl(HTTPEventAdapterConstants.EMPTY_STRING);
                registrationProfile.setGrantType(HTTPEventAdapterConstants.GRANT_TYPE);
                registrationProfile.setOwner(username);
                registrationProfile.setTokenScope(HTTPEventAdapterConstants.TOKEN_SCOPE);
                if (!httpConnectionConfiguration.isGlobalCredentialSet()) {
                    registrationProfile.setClientName(HTTPEventAdapterConstants.APPLICATION_NAME_PREFIX
                                                      + httpConnectionConfiguration.getAdapterName() +
                                                      "_" + tenantId);
                    registrationProfile.setIsSaasApp(false);
                } else {
                    registrationProfile.setClientName(HTTPEventAdapterConstants.APPLICATION_NAME_PREFIX
                                                      + httpConnectionConfiguration.getAdapterName());
                    registrationProfile.setIsSaasApp(true);
                }
                String jsonString = registrationProfile.toJSON();
                StringEntity requestEntity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
                postMethod.setEntity(requestEntity);
                String basicAuth = getBase64Encode(username, password);
                postMethod.setHeader(new BasicHeader(HTTPEventAdapterConstants.AUTHORIZATION_HEADER_NAME,
                                                     HTTPEventAdapterConstants.AUTHORIZATION_HEADER_VALUE_PREFIX +
                                                     basicAuth));
                HttpResponse httpResponse = dcrHttpClient.execute(postMethod);
                if (httpResponse != null) {
                    String response = HTTPUtil.getResponseString(httpResponse);
                    try {
                        if (response != null) {
                            JSONParser jsonParser = new JSONParser();
                            JSONObject jsonPayload = (JSONObject) jsonParser.parse(response);
                            clientId = (String) jsonPayload.get(HTTPEventAdapterConstants.CLIENT_ID);
                            clientSecret = (String) jsonPayload.get(HTTPEventAdapterConstants.CLIENT_SECRET);
                        }
                    } catch (ParseException e) {
                        String msg = "error occurred while parsing generating token for the adapter";
                        log.error(msg, e);
                    }
                }
            } catch (MalformedURLException e) {
                throw new OutputEventAdapterRuntimeException("Invalid dcrUrl : " + dcrUrlString);
            } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IOException e) {
                throw new OutputEventAdapterRuntimeException("Failed to create an https connection.", e);
            }
        } else {
            throw new OutputEventAdapterRuntimeException("Invalid configuration for mqtt publisher");
        }
    }

    private String getBase64Encode(String key, String value) {
        return new String(org.apache.commons.ssl.Base64.encodeBase64((key + ":" + value).getBytes()));
    }


    /**
     * This class represents a job to send an HTTP request to a target URL.
     */
    class HTTPSender implements Runnable {

        private String url;
        private String payload;
        private Map<String, String> headers;

        private HttpClient httpClient;

        HTTPSender(String url, String payload, Map<String, String> headers,
                   HttpClient httpClient) {
            this.url = url;
            this.payload = payload;
            this.headers = headers;
            this.httpClient = httpClient;
        }

        String getUrl() {
            return url;
        }

        String getPayload() {
            return payload;
        }

        Map<String, String> getHeaders() {
            return headers;
        }

        HttpClient getHttpClient() {
            return httpClient;
        }

        public void run() {

            EntityEnclosingMethod method = null;

            try {

                if (clientMethod.equalsIgnoreCase(HTTPEventAdapterConstants.CONSTANT_HTTP_PUT)) {
                    method = new PutMethod(this.getUrl());
                } else {
                    method = new PostMethod(this.getUrl());
                }

                if (hostConfiguration == null) {
                    URL hostUrl = new URL(this.getUrl());
                    hostConfiguration = new HostConfiguration();
                    hostConfiguration.setHost(hostUrl.getHost(), hostUrl.getPort(), hostUrl.getProtocol());
                }

                method.setRequestEntity(new StringRequestEntity(this.getPayload(), contentType, "UTF-8"));
                method.setRequestHeader("Authorization",
                                        "Bearer " + getToken(clientId, clientSecret));

                if (this.getHeaders() != null) {
                    for (Map.Entry<String, String> header : this.getHeaders().entrySet()) {
                        method.setRequestHeader(header.getKey(), header.getValue());
                    }
                }

                this.getHttpClient().executeMethod(hostConfiguration, method);

            } catch (UnknownHostException e) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), this.getPayload(),
                                            "Cannot connect to " + this.getUrl(), e, log, tenantId);
            } catch (IOException e) {
                EventAdapterUtil
                        .logAndDrop(eventAdapterConfiguration.getName(), this.getPayload(), null, e, log, tenantId);
            } catch (JWTClientException | UserStoreException e) {
                log.error("Failed to create an oauth token with jwt grant type.", e);
            } finally {
                if (method != null) {
                    method.releaseConnection();
                }
            }
        }

        private String getToken(String clientId, String clientSecret)
                throws UserStoreException, JWTClientException {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            try {
                String scopes = httpConnectionConfiguration.getScopes();
                String username = httpConnectionConfiguration.getUsername();
                if (httpConnectionConfiguration.isGlobalCredentialSet()) {
                    username = PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                       .getUserRealm().getRealmConfiguration().getAdminUserName() + "@" + PrivilegedCarbonContext
                                       .getThreadLocalCarbonContext().getTenantDomain(true);
                }

                JWTClientManagerService jwtClientManagerService =
                        OutputAdapterServiceDataHolder.getJwtClientManagerService();
                AccessTokenInfo accessTokenInfo = jwtClientManagerService.getJWTClient().getAccessToken(
                        clientId, clientSecret, username, scopes);
                return accessTokenInfo.getAccessToken();
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

}