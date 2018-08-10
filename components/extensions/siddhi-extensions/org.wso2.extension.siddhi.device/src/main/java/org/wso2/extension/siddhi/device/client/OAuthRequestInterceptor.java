/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.extension.siddhi.device.client;

import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jaxrs.JAXRSContract;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.extension.siddhi.device.client.configs.SiddhiExtensionConfigReader;
import org.wso2.extension.siddhi.device.client.dto.OAuthApplication;
import org.wso2.extension.siddhi.device.client.dto.RegistrationProfile;
import org.wso2.extension.siddhi.device.client.exception.APIMClientOAuthException;
import org.wso2.extension.siddhi.device.client.services.DCRService;
import org.wso2.extension.siddhi.device.utils.ClientUtils;
import org.wso2.extension.siddhi.device.utils.DeviceUtils;

/**
 * This is a request interceptor to add oauth token header.
 */
public class OAuthRequestInterceptor implements RequestInterceptor {

    private static final String APPLICATION_NAME = "siddhi_extension_client";
    private static final String REQUIRED_SCOPES = "perm:devices:operations";
    private static final String[] API_TAGS = {"device_management"};
    private DCRService dcrService;
    private static OAuthApplication oAuthApplication;
    private static final Log log = LogFactory.getLog(OAuthRequestInterceptor.class);

    /**
     * Creates an interceptor that authenticates all requests.
     */
    public OAuthRequestInterceptor() {
        String username = SiddhiExtensionConfigReader.getInstance().getConfig().getUsername();
        String password = SiddhiExtensionConfigReader.getInstance().getConfig().getPassword();
        dcrService = Feign.builder().client(new OkHttpClient(ClientUtils.getSSLClient())).logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .requestInterceptor(new BasicAuthRequestInterceptor(username, password))
                .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                .target(DCRService.class, ClientUtils.replaceProperties(
                        SiddhiExtensionConfigReader.getInstance().getConfig().getDcrEndpoint()));
    }

    @Override
    public void apply(RequestTemplate template) {
        if (oAuthApplication == null) {
            RegistrationProfile registrationProfile = new RegistrationProfile();
            registrationProfile.setApiApplicationName(APPLICATION_NAME);
            registrationProfile.setIsAllowedToAllDomains(true);
            registrationProfile.setTags(API_TAGS);
            oAuthApplication = dcrService.register(registrationProfile);
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration().getAdminUserName();
            if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                username = username + "@" + tenantDomain;
            }
            JWTClient jwtClient = DeviceUtils.getJWTClientManagerService().getJWTClient();
            AccessTokenInfo tenantBasedAccessTokenInfo = jwtClient.getAccessToken(oAuthApplication.getClientId(),
                    oAuthApplication.getClientSecret(), username, REQUIRED_SCOPES);
            if (tenantBasedAccessTokenInfo.getAccessToken() != null) {
                String headerValue = "Bearer " + tenantBasedAccessTokenInfo.getAccessToken();
                template.header("Authorization", headerValue);
            }
        } catch (JWTClientException e) {
            String msg = "Failed to retrieve oauth token using jwt";
            log.error(msg, e);
            throw new APIMClientOAuthException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Unable to retrieve realm config for tenant " + tenantDomain;
            log.error(msg, e);
            throw new APIMClientOAuthException(msg, e);
        }
    }

}
