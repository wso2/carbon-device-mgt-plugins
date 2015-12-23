/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.iot.controlqueue.xmpp;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.iot.controlqueue.ControlQueueConnector;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.exception.IoTException;
import org.wso2.carbon.device.mgt.iot.util.IoTUtil;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class XmppServerClient implements ControlQueueConnector {

    private static final Log log = LogFactory.getLog(XmppServerClient.class);

    private static final String XMPP_SERVER_API_CONTEXT = "/plugins/restapi/v1";
    private static final String XMPP_USERS_API = "/users";
    private static final String XMPP_SESSIONS_API = "/sessions";
    @SuppressWarnings("unused")
    private static final String XMPP_GROUPS_API = "/groups";
    @SuppressWarnings("unused")
    private static final String APPLICATION_JSON_MT = "application/json";
    private static final String DEVICEMGT_CONFIG_FILE = "devicemgt-config.xml";

    private String xmppEndpoint;
    private String xmppUsername;
    private String xmppPassword;
    private boolean xmppEnabled = false;

    public XmppServerClient() {
    }

    @Override
    public void initControlQueue() {
        xmppEndpoint = XmppConfig.getInstance().getXmppEndpoint();
        xmppUsername = XmppConfig.getInstance().getXmppUsername();
        xmppPassword = XmppConfig.getInstance().getXmppPassword();
        xmppEnabled = XmppConfig.getInstance().isEnabled();
    }

    @Override
    public void enqueueControls(HashMap<String, String> deviceControls)
            throws DeviceControllerException {
        if (!xmppEnabled) {
            log.warn(String.format("XMPP <Enabled> set to false in [%s]", DEVICEMGT_CONFIG_FILE));
        }
    }

    public boolean createXMPPAccount(XmppAccount newUserAccount) throws DeviceControllerException {
        if (xmppEnabled) {
            String xmppUsersAPIEndpoint = xmppEndpoint + XMPP_SERVER_API_CONTEXT + XMPP_USERS_API;
            if (log.isDebugEnabled()) {
                log.debug("The Create-UserAccount Endpoint URL of the XMPP Server is set to: " + xmppUsersAPIEndpoint);
            }

            String encodedString = xmppUsername + ":" + xmppPassword;
            encodedString = new String(Base64.encodeBase64(encodedString.getBytes(StandardCharsets.UTF_8)));
            String authorizationHeader = "Basic " + encodedString;
            String jsonRequest = "{\n" +
                    "    \"username\": \"" + newUserAccount.getUsername() + "\"," +
                    "    \"password\": \"" + newUserAccount.getPassword() + "\"," +
                    "    \"name\": \"" + newUserAccount.getAccountName() + "\"," +
                    "    \"email\": \"" + newUserAccount.getEmail() + "\"," +
                    "    \"properties\": {" +
                    "        \"property\": [" +
                    "            {" +
                    "                \"@key\": \"console.rows_per_page\"," +
                    "                \"@value\": \"user-summary=8\"" +
                    "            }," +
                    "            {" +
                    "                \"@key\": \"console.order\"," +
                    "                \"@value\": \"session-summary=1\"" +
                    "            }" +
                    "        ]" +
                    "    }" +
                    "}";

            StringEntity requestEntity;
            try {
                requestEntity = new StringEntity(jsonRequest, MediaType.APPLICATION_JSON,
                                                 StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                return false;
            }

            URL xmppUserApiUrl;
            try {
                xmppUserApiUrl = new URL(xmppUsersAPIEndpoint);
            } catch (MalformedURLException e) {
                String errMsg = "Malformed XMPP URL + " + xmppUsersAPIEndpoint;
                log.error(errMsg);
                throw new DeviceControllerException(errMsg);
            }
            HttpClient httpClient;
            try {
                httpClient = IoTUtil.getHttpClient(xmppUserApiUrl.getPort(), xmppUserApiUrl.getProtocol());
            } catch (Exception e) {
                log.error("Error on getting a http client for port :" + xmppUserApiUrl.getPort() + " protocol :"
                                  + xmppUserApiUrl.getProtocol());
                return false;
            }

            HttpPost httpPost = new HttpPost(xmppUsersAPIEndpoint);
            httpPost.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
            httpPost.setEntity(requestEntity);

            try {
                HttpResponse httpResponse = httpClient.execute(httpPost);

                if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                    String response = IoTUtil.getResponseString(httpResponse);
                    String errorMsg = "XMPP Server returned status: '" + httpResponse.getStatusLine().getStatusCode() +
                            "' for account creation with error:\n" + response;
                    log.error(errorMsg);
                    throw new DeviceControllerException(errorMsg);
                } else {
                    EntityUtils.consume(httpResponse.getEntity());
                    return true;
                }
            } catch (IOException | IoTException e) {
                String errorMsg = "Error occured whilst trying a 'POST' at : " + xmppUsersAPIEndpoint;
                log.error(errorMsg);
                throw new DeviceControllerException(errorMsg, e);
            }

        } else {
            log.warn(String.format("XMPP <Enabled> set to false in [%s]", DEVICEMGT_CONFIG_FILE));
            return false;
        }
    }


    public boolean doesXMPPUserAccountExist(String username) throws DeviceControllerException {
        if (xmppEnabled) {
            String xmppCheckUserAPIEndpoint = xmppEndpoint + XMPP_SERVER_API_CONTEXT + XMPP_USERS_API + "/" + username;
            if (log.isDebugEnabled()) {
                log.debug("The Check-User-Account Endpoint URL of the XMPP Server is set to: " +
                                  xmppCheckUserAPIEndpoint);
            }

            String encodedString = xmppUsername + ":" + xmppPassword;
            encodedString = new String(Base64.encodeBase64(encodedString.getBytes(StandardCharsets.UTF_8)));
            String authorizationHeader = "Basic " + encodedString;

            URL xmppUserApiUrl;
            try {
                xmppUserApiUrl = new URL(xmppCheckUserAPIEndpoint);
            } catch (MalformedURLException e) {
                String errMsg = "Malformed XMPP URL + " + xmppCheckUserAPIEndpoint;
                log.error(errMsg);
                throw new DeviceControllerException(errMsg, e);
            }

            HttpClient httpClient;
            try {
                httpClient = IoTUtil.getHttpClient(xmppUserApiUrl.getPort(), xmppUserApiUrl.getProtocol());
            } catch (Exception e) {
                String errorMsg = "Error on getting a http client for port :" + xmppUserApiUrl.getPort() +
                        " protocol :" + xmppUserApiUrl.getProtocol();
                log.error(errorMsg);
                throw new DeviceControllerException(errorMsg, e);
            }

            HttpGet httpGet = new HttpGet(xmppCheckUserAPIEndpoint);
            httpGet.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);

            try {
                HttpResponse httpResponse = httpClient.execute(httpGet);

                if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    String response = IoTUtil.getResponseString(httpResponse);
                    if (log.isDebugEnabled()) {
                        log.debug("XMPP Server returned status: '" + httpResponse.getStatusLine().getStatusCode() +
                                          "' for checking existence of account [" + username + "] with message:\n" +
                                          response + "\nProbably, an account with this username does not exist.");
                    }
                    return false;
                }

            } catch (IOException | IoTException e) {
                String errorMsg = "Error occured whilst trying a 'GET' at : " + xmppCheckUserAPIEndpoint;
                log.error(errorMsg);
                throw new DeviceControllerException(errorMsg, e);
            }

            if (log.isDebugEnabled()) {
                log.debug("XMPP Server already has an account for the username - [" + username + "].");
            }
            return true;
        } else {
            String warnMsg = String.format("XMPP <Enabled> set to false in [%s]", DEVICEMGT_CONFIG_FILE);
            log.warn(warnMsg);
            throw new DeviceControllerException(warnMsg);
        }
    }


    public JSONArray getAllCurrentUserSessions() throws DeviceControllerException {
        if (xmppEnabled) {
            JSONArray xmppSessions;
            String xmppSessionsAPIEndpoint = xmppEndpoint + XMPP_SERVER_API_CONTEXT + XMPP_SESSIONS_API;

            if (log.isDebugEnabled()) {
                log.debug("The Get-Sessions Endpoint URL of the XMPP Server is set to: " + xmppSessionsAPIEndpoint);
            }

            String encodedString = xmppUsername + ":" + xmppPassword;
            encodedString = new String(Base64.encodeBase64(encodedString.getBytes(StandardCharsets.UTF_8)));
            String authorizationHeader = "Basic " + encodedString;

            URL xmppUserApiUrl;
            try {
                xmppUserApiUrl = new URL(xmppSessionsAPIEndpoint);
            } catch (MalformedURLException e) {
                String errMsg = "Malformed XMPP URL + " + xmppSessionsAPIEndpoint;
                log.error(errMsg);
                throw new DeviceControllerException(errMsg, e);
            }

            HttpClient httpClient;
            try {
                httpClient = IoTUtil.getHttpClient(xmppUserApiUrl.getPort(), xmppUserApiUrl.getProtocol());
            } catch (Exception e) {
                String errorMsg = "Error on getting a http client for port :" + xmppUserApiUrl.getPort() +
                        " protocol :" + xmppUserApiUrl.getProtocol();
                log.error(errorMsg);
                throw new DeviceControllerException(errorMsg, e);
            }

            HttpGet httpGet = new HttpGet(xmppSessionsAPIEndpoint);
            httpGet.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
            httpGet.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

            try {
                HttpResponse httpResponse = httpClient.execute(httpGet);

                if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    String errorMsg = "XMPP Server returned status: '" + httpResponse.getStatusLine().getStatusCode() +
                            "' for checking current XMPP Sessions.";
                    log.error(errorMsg);
                    throw new DeviceControllerException(errorMsg);
                }

                String response = IoTUtil.getResponseString(httpResponse);
                xmppSessions = new JSONObject(response).getJSONArray("session");
                return xmppSessions;

            } catch (IOException | IoTException e) {
                String errorMsg = "Error occured whilst trying a 'GET' at : " + xmppSessionsAPIEndpoint;
                log.error(errorMsg);
                throw new DeviceControllerException(errorMsg, e);
            }

        } else {
            String warnMsg = String.format("XMPP <Enabled> set to false in [%s]", DEVICEMGT_CONFIG_FILE);
            log.warn(warnMsg);
            throw new DeviceControllerException(warnMsg);
        }
    }


    public void deleteCurrentXmppSessions() throws DeviceControllerException {
        JSONArray xmppSessionsArray;

        try {
            xmppSessionsArray = getAllCurrentUserSessions();
        } catch (DeviceControllerException e) {
            if (e.getMessage().contains(DEVICEMGT_CONFIG_FILE)) {
                log.warn(String.format("XMPP <Enabled> set to false in [%s]", DEVICEMGT_CONFIG_FILE));
                return;
            } else {
                throw e;
            }
        }

        if (xmppSessionsArray.length() != 0) {
            String xmppSessionsAPIEndpoint = xmppEndpoint + XMPP_SERVER_API_CONTEXT + XMPP_SESSIONS_API;
            String encodedString = xmppUsername + ":" + xmppPassword;
            encodedString = new String(Base64.encodeBase64(encodedString.getBytes(StandardCharsets.UTF_8)));
            String authorizationHeader = "Basic " + encodedString;

            if (log.isDebugEnabled()) {
                log.debug("The Get-Sessions Endpoint URL of the XMPP Server is set to: " + xmppSessionsAPIEndpoint);
            }

            URL xmppUserApiUrl;
            try {
                xmppUserApiUrl = new URL(xmppSessionsAPIEndpoint);
            } catch (MalformedURLException e) {
                String errMsg = "Malformed XMPP URL + " + xmppSessionsAPIEndpoint;
                log.error(errMsg);
                throw new DeviceControllerException(errMsg, e);
            }

            HttpClient httpClient;
            try {
                httpClient = IoTUtil.getHttpClient(xmppUserApiUrl.getPort(), xmppUserApiUrl.getProtocol());
            } catch (Exception e) {
                String errorMsg = "Error on getting a http client for port :" + xmppUserApiUrl.getPort() +
                        " protocol :" + xmppUserApiUrl.getProtocol();
                log.error(errorMsg);
                throw new DeviceControllerException(errorMsg, e);
            }

            for (int i = 0; i < xmppSessionsArray.length(); i++) {

                String sessionName = xmppSessionsArray.getJSONObject(i).getString("username");
                String xmppUserSessionsAPIEndpoint = xmppSessionsAPIEndpoint + "/" + sessionName;

                HttpDelete httpDelete = new HttpDelete(xmppUserSessionsAPIEndpoint);
                httpDelete.addHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);

                try {
                    HttpResponse httpResponse = httpClient.execute(httpDelete);

                    if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        String errorMsg =
                                "XMPP Server returned status: '" + httpResponse.getStatusLine().getStatusCode() +
                                        "' for checking current XMPP Sessions.";
                        log.error(errorMsg);
                        throw new DeviceControllerException(errorMsg);
                    }

                } catch (IOException e) {
                    String errorMsg = "Error occured whilst trying a 'DELETE' user-session [" + sessionName + "] " +
                            "at : " + xmppUserSessionsAPIEndpoint;
                    log.error(errorMsg);
                    throw new DeviceControllerException(errorMsg, e);
                }
            }
        }
    }
}
