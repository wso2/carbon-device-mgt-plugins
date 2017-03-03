/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization;

import feign.Client;
import feign.Feign;
import feign.FeignException;
import feign.Logger;
import feign.Request;
import feign.Response;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jaxrs.JAXRSContract;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dna.mqtt.moquette.server.IAuthorizer;
import org.wso2.andes.configuration.enums.MQTTAuthoriztionPermissionLevel;
import org.wso2.andes.mqtt.MQTTAuthorizationSubject;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.client.OAuthRequestInterceptor;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.client.dto.AuthorizationRequest;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.client.dto.DeviceAccessAuthorizationAdminService;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.client.dto.DeviceAuthorizationResult;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.client.dto.DeviceIdentifier;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.config.AuthorizationConfigurationManager;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.internal.AuthorizationDataHolder;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.util.AuthorizationCacheKey;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.Caching;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Authorize the connecting users against CDMF. Intended usage is
 * via providing fully qualified class name in broker.xml
 * <p/>
 * This is just a simple authorization model. For dynamic topics use an implementation based on IAuthorizer
 */
public class DeviceAccessBasedMQTTAuthorizer implements IAuthorizer {

    private static final String UI_EXECUTE = "ui.execute";
    private static Log log = LogFactory.getLog(DeviceAccessBasedMQTTAuthorizer.class);
    AuthorizationConfigurationManager MQTTAuthorizationConfiguration;
    private static final String CDMF_SERVER_BASE_CONTEXT = "/api/device-mgt/v1.0";
    private static final String CACHE_MANAGER_NAME = "mqttAuthorizationCacheManager";
    private static final String CACHE_NAME = "mqttAuthorizationCache";
    private static DeviceAccessAuthorizationAdminService deviceAccessAuthorizationAdminService;


    public DeviceAccessBasedMQTTAuthorizer() {
        this.MQTTAuthorizationConfiguration = AuthorizationConfigurationManager.getInstance();
        deviceAccessAuthorizationAdminService = Feign.builder().client(getSSLClient()).logger(getLogger())
                .logLevel(Logger.Level.FULL).requestInterceptor(new OAuthRequestInterceptor())
                .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                .target(DeviceAccessAuthorizationAdminService.class,
                        MQTTAuthorizationConfiguration.getDeviceMgtServerUrl() + CDMF_SERVER_BASE_CONTEXT);
    }

    /**
     * {@inheritDoc} Authorize the user against carbon device mgt model.
     */
    @Override
    public boolean isAuthorizedForTopic(MQTTAuthorizationSubject authorizationSubject, String topic,
                                        MQTTAuthoriztionPermissionLevel permissionLevel) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        try {
            String topics[] = topic.split("/");
            String tenantDomainFromTopic = topics[0];
            if (!tenantDomainFromTopic.equals(authorizationSubject.getTenantDomain())) {
                return false;
            }
            Cache<AuthorizationCacheKey, Boolean> cache = getCache();
            if (topics.length < 3) {
                AuthorizationCacheKey authorizationCacheKey = new AuthorizationCacheKey(tenantDomainFromTopic
                        , authorizationSubject.getUsername(), "", "");
                if (cache.get(authorizationCacheKey) != null && cache.get(authorizationCacheKey)) {
                    return true;
                }
                AuthorizationRequest authorizationRequest = new AuthorizationRequest();
                authorizationRequest.setTenantDomain(tenantDomainFromTopic);
                try {
                    DeviceAuthorizationResult deviceAuthorizationResult =
                            deviceAccessAuthorizationAdminService.isAuthorized(authorizationRequest);
                    if (deviceAuthorizationResult != null) {
                        cache.put(authorizationCacheKey, true);
                        return true;
                    }
                    return false;
                } catch (FeignException e) {
                    log.error(e.getMessage(), e);
                    return false;
                }
            }
            String deviceType = topics[1];
            String deviceId = topics[2];
            AuthorizationCacheKey authorizationCacheKey = new AuthorizationCacheKey(tenantDomainFromTopic
                    , authorizationSubject.getUsername(), deviceId, deviceType);
            if (cache.get(authorizationCacheKey) != null && cache.get(authorizationCacheKey)) {
                return true;
            }

            List<String> requiredPermission;
            if (permissionLevel == MQTTAuthoriztionPermissionLevel.SUBSCRIBE) {
                requiredPermission = MQTTAuthorizationConfiguration.getSubscriberPermissions();
            } else {
                requiredPermission = MQTTAuthorizationConfiguration.getPublisherPermissions();
            }

            AuthorizationRequest authorizationRequest = new AuthorizationRequest();
            authorizationRequest.setTenantDomain(tenantDomainFromTopic);
            if (requiredPermission != null) {
                authorizationRequest.setPermissions(requiredPermission);
            }
            authorizationRequest.setUsername(authorizationSubject.getUsername());
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(deviceType);
            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(deviceIdentifier);
            authorizationRequest.setDeviceIdentifiers(deviceIdentifiers);
            try {
                DeviceAuthorizationResult deviceAuthorizationResult =
                        deviceAccessAuthorizationAdminService.isAuthorized(authorizationRequest);
                List<DeviceIdentifier> devices = deviceAuthorizationResult.getAuthorizedDevices();
                if (devices != null && devices.size() > 0) {
                    DeviceIdentifier authorizedDevice = devices.get(0);
                    if (authorizedDevice.getId().equals(deviceId) && authorizedDevice.getType().equals(deviceType)) {
                        cache.put(authorizationCacheKey, true);
                        return true;
                    }
                }
            } catch (FeignException e) {
                log.error(e.getMessage(), e);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        return false;
    }

    /**
     * {@inheritDoc} Authorized the user against carbon device mgt model.
     */
    @Override
    public boolean isAuthorizedToConnect(MQTTAuthorizationSubject authorizationSubject) {
        if (MQTTAuthorizationConfiguration.getConnectionPermission() == null ||
                MQTTAuthorizationConfiguration.getConnectionPermission().isEmpty()) {
            //allow authenticated client to connect.
            return true;
        }
        return isUserAuthorized(authorizationSubject, MQTTAuthorizationConfiguration.getConnectionPermission()
                , UI_EXECUTE);
    }

    /**
     * Check whether the client is authorized with the given permission and action.
     *
     * @param authorizationSubject this contains the client information
     * @param permission           Carbon permission that requires for the use
     * @param action               Carbon permission action that requires for the given permission.
     * @return boolean - true if user is authorized else return false.
     */
    private boolean isUserAuthorized(MQTTAuthorizationSubject authorizationSubject, String permission, String action) {
        String username = authorizationSubject.getUsername();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    authorizationSubject.getTenantDomain(), true);
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserRealm userRealm = AuthorizationDataHolder.getInstance().getRealmService()
                    .getTenantUserRealm(tenantId);
            return userRealm != null && userRealm.getAuthorizationManager() != null &&
                    userRealm.getAuthorizationManager().isUserAuthorized(username, permission, action);
        } catch (UserStoreException e) {
            String errorMsg = String.format("Unable to authorize the user : %s", username);
            log.error(errorMsg, e);
            return false;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * This method is used to create the Caches.
     * @return          Cachemanager
     */
    private synchronized Cache<AuthorizationCacheKey, Boolean> getCache() {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        try {
            if (MQTTAuthorizationConfiguration.getCacheDuration() == 0) {
                return Caching.getCacheManagerFactory().getCacheManager(CACHE_MANAGER_NAME).getCache(CACHE_NAME);
            } else {
                return Caching.getCacheManagerFactory().getCacheManager(CACHE_MANAGER_NAME).<AuthorizationCacheKey, Boolean>createCacheBuilder(CACHE_NAME).
                        setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(
                                TimeUnit.SECONDS, MQTTAuthorizationConfiguration.getCacheDuration())).
                        setStoreByValue(false).build();
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
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

    private static Logger getLogger() {
        return new Logger() {
            @Override
            protected void log(String configKey, String format, Object... args) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format(methodTag(configKey) + format, args));
                }
            }

            @Override
            protected void logRequest(String configKey, Level logLevel, Request request) {
                if (log.isDebugEnabled()) {
                    super.logRequest(configKey, logLevel, request);
                }
            }

            @Override
            protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response,
                                                      long elapsedTime) throws IOException {
                if (log.isDebugEnabled()) {
                    return super.logAndRebufferResponse(configKey, logLevel, response, elapsedTime);
                }
                return response;
            }
        };
    }

}