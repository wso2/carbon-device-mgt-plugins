/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.appmgt.mdm.restconnector.authorization.client;

import feign.Feign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jaxrs.JAXRSContract;
import org.wso2.carbon.appmgt.mdm.restconnector.Constants;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.AccessTokenInfo;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.ApiApplicationKey;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.ApiApplicationRegistrationService;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.ApiRegistrationProfile;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.TokenIssuerService;
import org.wso2.carbon.appmgt.mdm.restconnector.config.AuthorizationConfigurationManager;
import org.wso2.carbon.appmgt.mdm.restconnector.internal.AuthorizationDataHolder;

/**
 * This is a request interceptor to add oauth token header.
 */
public class OAuthRequestInterceptor implements RequestInterceptor {
    private AccessTokenInfo tokenInfo;
    private String refreshTimeOffset;
    private static final String API_APPLICATION_REGISTRATION_CONTEXT = "/api-application-registration";
    private static final String DEVICE_MANAGEMENT_SERVICE_TAG[] = {"device_management"};
    private static final String APPLICATION_NAME = "appm_restconnector_application";
    private static final String PASSWORD_GRANT_TYPE = "password";
    private static final String REFRESH_GRANT_TYPE = "refreshToken";
    private ApiApplicationRegistrationService apiApplicationRegistrationService;
    private TokenIssuerService tokenIssuerService;

    /**
     * Creates an interceptor that authenticates all requests.
     */
    public OAuthRequestInterceptor() {
        refreshTimeOffset = AuthorizationConfigurationManager.getInstance().getTokenRefreshTimeOffset();
        String username = AuthorizationConfigurationManager.getInstance().getUserName();
        String password = AuthorizationConfigurationManager.getInstance().getPassword();
        apiApplicationRegistrationService = Feign.builder().requestInterceptor(
                new BasicAuthRequestInterceptor(username, password))
                .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                .target(ApiApplicationRegistrationService.class,
                        AuthorizationConfigurationManager.getInstance().getServerURL() +
                                API_APPLICATION_REGISTRATION_CONTEXT);
        AuthorizationDataHolder.getInstance().setApiApplicationRegistrationService(apiApplicationRegistrationService);

    }

    /**
     * Api create.
     *
     * @param template {@link RequestTemplate} object
     */
    @Override
    public void apply(RequestTemplate template) {
        if (tokenInfo == null) {
            ApiRegistrationProfile apiRegistrationProfile = new ApiRegistrationProfile();
            apiRegistrationProfile.setApplicationName(APPLICATION_NAME);
            apiRegistrationProfile.setIsAllowedToAllDomains(true);
            apiRegistrationProfile.setIsMappingAnExistingOAuthApp(false);
            apiRegistrationProfile.setTags(DEVICE_MANAGEMENT_SERVICE_TAG);
            ApiApplicationKey apiApplicationKey = apiApplicationRegistrationService.register(apiRegistrationProfile);
            String consumerKey = apiApplicationKey.getConsumerKey();
            String consumerSecret = apiApplicationKey.getConsumerSecret();
            String username = AuthorizationConfigurationManager.getInstance().getUserName();
            String password = AuthorizationConfigurationManager.getInstance().getPassword();
            tokenIssuerService = Feign.builder().requestInterceptor(
                    new BasicAuthRequestInterceptor(consumerKey, consumerSecret))
                    .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                    .target(TokenIssuerService.class, AuthorizationConfigurationManager.getInstance().getTokenApiURL());
            tokenInfo = tokenIssuerService.getToken(PASSWORD_GRANT_TYPE, username, password);
            tokenInfo.setExpiresIn(System.currentTimeMillis() + tokenInfo.getExpiresIn());
        }
        synchronized (this) {
            if (System.currentTimeMillis() + Long.parseLong(refreshTimeOffset) > tokenInfo.getExpiresIn()) {
                tokenInfo = tokenIssuerService.getToken(REFRESH_GRANT_TYPE, tokenInfo.getRefreshToken());
                tokenInfo.setExpiresIn(System.currentTimeMillis() + tokenInfo.getExpiresIn());
            }
        }
        String headerValue = Constants.RestConstants.BEARER + tokenInfo.getAccessToken();
        template.header(Constants.RestConstants.AUTHORIZATION, headerValue);
    }
}
