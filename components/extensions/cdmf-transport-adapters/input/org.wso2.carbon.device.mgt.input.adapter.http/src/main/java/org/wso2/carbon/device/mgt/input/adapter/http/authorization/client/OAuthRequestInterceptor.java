/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package org.wso2.carbon.device.mgt.input.adapter.http.authorization.client;

import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jaxrs.JAXRSContract;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.input.adapter.http.authorization.client.dto.AccessTokenInfo;
import org.wso2.carbon.device.mgt.input.adapter.http.authorization.client.dto.ApiApplicationKey;
import org.wso2.carbon.device.mgt.input.adapter.http.authorization.client.dto.ApiApplicationRegistrationService;
import org.wso2.carbon.device.mgt.input.adapter.http.authorization.client.dto.ApiRegistrationProfile;
import org.wso2.carbon.device.mgt.input.adapter.http.authorization.client.dto.TokenIssuerService;
import org.wso2.carbon.device.mgt.input.adapter.http.util.PropertyUtils;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;

import java.util.Map;

/**
 * This is a request interceptor to add oauth token header.
 */
public class OAuthRequestInterceptor implements RequestInterceptor {

    private AccessTokenInfo tokenInfo;
    private long refreshTimeOffset;
    private static final String API_APPLICATION_REGISTRATION_CONTEXT = "/api-application-registration";
    private static final String DEVICE_MANAGEMENT_SERVICE_TAG[] = {"device_management"};
    private static final String APPLICATION_NAME = "websocket-app";
    private static final String PASSWORD_GRANT_TYPE = "password";
    private static final String REFRESH_GRANT_TYPE = "refresh_token";
    private static final String REQUIRED_SCOPE = "perm:authorization:verify";
    private ApiApplicationRegistrationService apiApplicationRegistrationService;
    private TokenIssuerService tokenIssuerService;

    private static Log logger = LogFactory.getLog(OAuthRequestInterceptor.class);

    private static final String CONNECTION_USERNAME = "username";
    private static final String CONNECTION_PASSWORD = "password";
    private static final String TOKEN_REFRESH_TIME_OFFSET = "tokenRefreshTimeOffset";
    private static final String TOKEN_SCOPES = "scopes";
    private static final String DEVICE_MGT_SERVER_URL = "deviceMgtServerUrl";
    private static final String TOKEN_ENDPOINT_CONTEXT = "tokenUrl";
    private static String username;
    private static String password;
    private static String tokenEndpoint;
    private static String deviceMgtServerUrl;
    private static String scopes;
    private static Map<String, String> globalProperties;


    /**
     * Creates an interceptor that authenticates all requests.
     */
    public OAuthRequestInterceptor(Map<String, String> globalProperties) {
        this.globalProperties = globalProperties;
        try {
            deviceMgtServerUrl = getDeviceMgtServerUrl(globalProperties);
            refreshTimeOffset = getRefreshTimeOffset(globalProperties) * 1000;
            username = getUsername(globalProperties);
            password = getPassword(globalProperties);
            tokenEndpoint = getTokenEndpoint(globalProperties);
            apiApplicationRegistrationService = Feign.builder().requestInterceptor(
                    new BasicAuthRequestInterceptor(username, password))
                    .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                    .target(ApiApplicationRegistrationService.class,
                            deviceMgtServerUrl + API_APPLICATION_REGISTRATION_CONTEXT);
        } catch (InputEventAdapterException e) {
            logger.error("Invalid url: deviceMgtServerUrl" + deviceMgtServerUrl + " or tokenEndpoint:" + tokenEndpoint,
                         e);
        }
    }

    @Override
    public void apply(RequestTemplate template) {
        if (tokenInfo == null) {
            //had to do on demand initialization due to start up error.
            ApiRegistrationProfile apiRegistrationProfile = new ApiRegistrationProfile();
            apiRegistrationProfile.setApplicationName(APPLICATION_NAME);
            apiRegistrationProfile.setIsAllowedToAllDomains(false);
            apiRegistrationProfile.setIsMappingAnExistingOAuthApp(false);
            apiRegistrationProfile.setTags(DEVICE_MANAGEMENT_SERVICE_TAG);
            ApiApplicationKey apiApplicationKey = apiApplicationRegistrationService.register(apiRegistrationProfile);
            String consumerKey = apiApplicationKey.getConsumerKey();
            String consumerSecret = apiApplicationKey.getConsumerSecret();
            tokenIssuerService = Feign.builder().requestInterceptor(
                    new BasicAuthRequestInterceptor(consumerKey, consumerSecret))
                    .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                    .target(TokenIssuerService.class, tokenEndpoint);
            tokenInfo = tokenIssuerService.getToken(PASSWORD_GRANT_TYPE, username, password, REQUIRED_SCOPE);
            tokenInfo.setExpires_in(System.currentTimeMillis() + (tokenInfo.getExpires_in() * 1000));
        }
        synchronized(this) {
            if (System.currentTimeMillis() + refreshTimeOffset > tokenInfo.getExpires_in()) {
                tokenInfo = tokenIssuerService.getToken(REFRESH_GRANT_TYPE, tokenInfo.getRefresh_token());
                tokenInfo.setExpires_in(System.currentTimeMillis() + tokenInfo.getExpires_in());
            }
        }
        String headerValue = "Bearer " + tokenInfo.getAccess_token();
        template.header("Authorization", headerValue);
    }

    private String getUsername(Map<String, String> globalProperties) {
        String username = globalProperties.get(CONNECTION_USERNAME);
        if (username == null || username.isEmpty()) {
            logger.error("username can't be empty ");
        }
        return username;
    }

    private String getPassword(Map<String, String> globalProperties) {
        String password = globalProperties.get(CONNECTION_PASSWORD);;
        if (password == null || password.isEmpty()) {
            logger.error("password can't be empty ");
        }
        return password;
    }

    private String getDeviceMgtServerUrl(Map<String, String> globalProperties) throws InputEventAdapterException {
        String deviceMgtServerUrl = globalProperties.get(DEVICE_MGT_SERVER_URL);
        if (deviceMgtServerUrl == null || deviceMgtServerUrl.isEmpty()) {
            logger.error("deviceMgtServerUrl can't be empty ");
        }
        return PropertyUtils.replaceProperty(deviceMgtServerUrl);
    }

    private String getTokenEndpoint(Map<String, String> globalProperties) throws InputEventAdapterException {
        String tokenEndpoint = globalProperties.get(TOKEN_ENDPOINT_CONTEXT);
        if ( tokenEndpoint.isEmpty()) {
            logger.error("tokenEndpoint can't be empty ");
        }
        return PropertyUtils.replaceProperty(tokenEndpoint);
    }

    private long getRefreshTimeOffset(Map<String, String> globalProperties) {
        long refreshTimeOffset = 100;
        try {
            refreshTimeOffset = Long.parseLong(globalProperties.get(TOKEN_REFRESH_TIME_OFFSET));
        } catch (NumberFormatException e) {
            logger.error("refreshTimeOffset should be a number", e);
        }
        return refreshTimeOffset;
    }

}
