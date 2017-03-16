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

package org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.client;

import feign.Client;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jaxrs.JAXRSContract;
import feign.slf4j.Slf4jLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.client.dto.AccessTokenInfo;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.client.dto.ApiApplicationKey;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.client.dto.ApiApplicationRegistrationService;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.client.dto.ApiRegistrationProfile;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.client.dto.TokenIssuerService;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.config.AuthorizationConfigurationManager;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

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
    private static final String REQUIRED_SCOPE = "perm:authorization:verify";
    private ApiApplicationRegistrationService apiApplicationRegistrationService;
    private TokenIssuerService tokenIssuerService;
    private static Log log = LogFactory.getLog(OAuthRequestInterceptor.class);

    /**
     * Creates an interceptor that authenticates all requests.
     */
    public OAuthRequestInterceptor() {
        refreshTimeOffset = AuthorizationConfigurationManager.getInstance().getTokenRefreshTimeOffset() * 1000;
        String username = AuthorizationConfigurationManager.getInstance().getUsername();
        String password = AuthorizationConfigurationManager.getInstance().getPassword();
        apiApplicationRegistrationService = Feign.builder().client(getSSLClient()).logger(new Slf4jLogger()).logLevel(
                Logger.Level.FULL).requestInterceptor(new BasicAuthRequestInterceptor(username, password))
                .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                .target(ApiApplicationRegistrationService.class,
                        AuthorizationConfigurationManager.getInstance().getDeviceMgtServerUrl() +
                                API_APPLICATION_REGISTRATION_CONTEXT);
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
            String username = AuthorizationConfigurationManager.getInstance().getUsername();
            String password = AuthorizationConfigurationManager.getInstance().getPassword();
            tokenIssuerService = Feign.builder().client(getSSLClient()).logger(new Slf4jLogger()).logLevel(Logger.Level.FULL)
                    .requestInterceptor(new BasicAuthRequestInterceptor(consumerKey, consumerSecret))
                    .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                    .target(TokenIssuerService.class,
                            AuthorizationConfigurationManager.getInstance().getTokenEndpoint());
            tokenInfo = tokenIssuerService.getToken(PASSWORD_GRANT_TYPE, username, password, REQUIRED_SCOPE);
            tokenInfo.setExpires_in(System.currentTimeMillis() + (tokenInfo.getExpires_in() * 1000));
        }
        synchronized (this) {
            if (System.currentTimeMillis() + refreshTimeOffset > tokenInfo.getExpires_in()) {
                tokenInfo = tokenIssuerService.getToken(REFRESH_GRANT_TYPE, tokenInfo.getRefresh_token());
                tokenInfo.setExpires_in(System.currentTimeMillis() + tokenInfo.getExpires_in());
            }
        }
        String headerValue = "Bearer " + tokenInfo.getAccess_token();
        template.header("Authorization", headerValue);
    }

    private static Client getSSLClient() {
        return new Client.Default(getTrustedSSLSocketFactory(), new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
    }

    private static SSLSocketFactory getTrustedSSLSocketFactory() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return sc.getSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            return null;
        }
    }

}
