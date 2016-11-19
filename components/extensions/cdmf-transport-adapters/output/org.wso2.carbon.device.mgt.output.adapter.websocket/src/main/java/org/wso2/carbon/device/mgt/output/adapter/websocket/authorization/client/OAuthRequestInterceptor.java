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

package org.wso2.carbon.device.mgt.output.adapter.websocket.authorization.client;

import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jaxrs.JAXRSContract;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authorization.client.dto.AccessTokenInfo;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authorization.client.dto.ApiApplicationKey;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authorization.client.dto.ApiApplicationRegistrationService;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authorization.client.dto.ApiRegistrationProfile;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authorization.client.dto.TokenIssuerService;
import org.wso2.carbon.device.mgt.output.adapter.websocket.config.Properties;
import org.wso2.carbon.device.mgt.output.adapter.websocket.config.Property;
import org.wso2.carbon.device.mgt.output.adapter.websocket.config.WebsocketConfig;

/**
 * This is a request interceptor to add oauth token header.
 */
public class OAuthRequestInterceptor implements RequestInterceptor {

    private AccessTokenInfo tokenInfo;
    private long refreshTimeOffset;
    private static final String API_APPLICATION_REGISTRATION_CONTEXT = "/api-application-registration";
    private static final String DEVICE_MANAGEMENT_SERVICE_TAG[] = {"device_management"};
    private static final String APPLICATION_NAME = "mqtt_broker";
    private static final String PASSWORD_GRANT_TYPE = "password";
    private static final String REFRESH_GRANT_TYPE = "refresh_token";
    private ApiApplicationRegistrationService apiApplicationRegistrationService;
    private TokenIssuerService tokenIssuerService;

    private static Log logger = LogFactory.getLog(OAuthRequestInterceptor.class);

    private static final String CONNECTION_USERNAME = "username";
    private static final String CONNECTION_PASSWORD = "password";
    private static final String TOKEN_ENDPOINT = "tokenEndpoint";
    private static final String TOKEN_REFRESH_TIME_OFFSET = "tokenRefreshTimeOffset";
    private static final String DEVICE_MGT_SERVER_URL = "deviceMgtServerUrl";
    private static String username;
    private static String password;
    private static String tokenEndpoint;
    private static String deviceMgtServerUrl;

    /**
     * Creates an interceptor that authenticates all requests.
     */
    public OAuthRequestInterceptor() {
        Properties properties =
                WebsocketConfig.getInstance().getWebsocketValidationConfigs().getAuthorizer().getProperties();
        deviceMgtServerUrl = getDeviceMgtServerUrl(properties);
        refreshTimeOffset = getRefreshTimeOffset(properties);
        username = getUsername(properties);
        password = getPassword(properties);
        tokenEndpoint = getTokenEndpoint(properties);
        apiApplicationRegistrationService = Feign.builder().requestInterceptor(
                new BasicAuthRequestInterceptor(username, password))
                .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                .target(ApiApplicationRegistrationService.class,
                        deviceMgtServerUrl + API_APPLICATION_REGISTRATION_CONTEXT);
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
            tokenInfo = tokenIssuerService.getToken(PASSWORD_GRANT_TYPE, username, password);
        }
        if (System.currentTimeMillis() + refreshTimeOffset > tokenInfo.getExpires_in()) {
            tokenInfo = tokenIssuerService.getToken(REFRESH_GRANT_TYPE, tokenInfo.getRefresh_token());
        }
        String headerValue = "Bearer " + tokenInfo.getAccess_token();
        template.header("Authorization", headerValue);
    }

    private String getUsername(Properties properties) {
        String username = null;
        for (Property property : properties.getProperty()) {
            if (property.getName().equals(CONNECTION_USERNAME)) {
                username = property.getValue();
                break;
            }
        }
        if (username == null || username.isEmpty()) {
            logger.error("username can't be empty ");
        }
        return username;
    }

    private String getPassword(Properties properties) {
        String password = null;
        for (Property property : properties.getProperty()) {
            if (property.getName().equals(CONNECTION_PASSWORD)) {
                password = property.getValue();
                break;
            }
        }
        if (password == null || password.isEmpty()) {
            logger.error("password can't be empty ");
        }
        return password;
    }

    private String getDeviceMgtServerUrl(Properties properties) {
        String deviceMgtServerUrl = null;
        for (Property property : properties.getProperty()) {
            if (property.getName().equals(DEVICE_MGT_SERVER_URL)) {
                deviceMgtServerUrl = property.getValue();
                break;
            }
        }
        if (deviceMgtServerUrl == null || deviceMgtServerUrl.isEmpty()) {
            logger.error("deviceMgtServerUrl can't be empty ");
        }
        return deviceMgtServerUrl;
    }

    private String getTokenEndpoint(Properties properties) {
        String tokenEndpoint = null;
        for (Property property : properties.getProperty()) {
            if (property.getName().equals(TOKEN_ENDPOINT)) {
                tokenEndpoint = property.getValue();
                break;
            }
        }
        if (tokenEndpoint == null || tokenEndpoint.isEmpty()) {
            logger.error("tokenEndpoint can't be empty ");
        }
        return tokenEndpoint;
    }

    private long getRefreshTimeOffset(Properties properties) {
        long refreshTimeOffset = 0;
        try {
            for (Property property : properties.getProperty()) {
                if (property.getName().equals(TOKEN_REFRESH_TIME_OFFSET)) {
                    refreshTimeOffset = Long.parseLong(property.getValue());
                    break;
                }
            }
        } catch (NumberFormatException e) {
            logger.error("refreshTimeOffset should be a number", e);
        }
        return refreshTimeOffset;
    }


}
