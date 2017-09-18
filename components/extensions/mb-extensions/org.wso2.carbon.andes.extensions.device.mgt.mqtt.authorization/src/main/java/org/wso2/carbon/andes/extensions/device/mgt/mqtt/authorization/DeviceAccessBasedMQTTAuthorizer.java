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
import feign.slf4j.Slf4jLogger;
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
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.Caching;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
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
    private AuthorizationConfigurationManager MQTTAuthorizationConfiguration;
    private static final String CDMF_SERVER_BASE_CONTEXT = "/api/device-mgt/v1.0";
    private static final String DEFAULT_ADMIN_PERMISSION = "permission/admin/device-mgt";
    private static final String CACHE_MANAGER_NAME = "mqttAuthorizationCacheManager";
    private static final String CACHE_NAME = "mqttAuthorizationCache";
    private static DeviceAccessAuthorizationAdminService deviceAccessAuthorizationAdminService;
    private static OAuthRequestInterceptor oAuthRequestInterceptor;
    private static final String GATEWAY_ERROR_CODE = "<am:code>404</am:code>";
    private static final String ALL_TENANT_DOMAIN = "+";

    public DeviceAccessBasedMQTTAuthorizer() {
        oAuthRequestInterceptor = new OAuthRequestInterceptor();
        this.MQTTAuthorizationConfiguration = AuthorizationConfigurationManager.getInstance();
        deviceAccessAuthorizationAdminService = Feign.builder().client(getSSLClient()).logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL).requestInterceptor(oAuthRequestInterceptor)
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
            if (ALL_TENANT_DOMAIN.equals(tenantDomainFromTopic)) {
                if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(authorizationSubject.getTenantDomain())
                        && isUserAuthorized(authorizationSubject, DEFAULT_ADMIN_PERMISSION, UI_EXECUTE)) {
                    return true;
                }
                return false;
            }
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
                    oAuthRequestInterceptor.resetApiApplicationKey();
                    if (e.getMessage().contains(GATEWAY_ERROR_CODE) || e.status() == 404 || e.status() == 403) {
                        log.error("Failed to connect to the device authorization service, Retrying....");
                    } else {
                        log.error(e.getMessage(), e);
                    }
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
                oAuthRequestInterceptor.resetApiApplicationKey();
                //This is to avoid failure where it tries to call authorization service before the api is published
                if (e.getMessage().contains(GATEWAY_ERROR_CODE) || e.status() == 404 || e.status() == 403) {
                    log.error("Failed to connect to the device authorization service, Retrying....");
                } else {
                    log.error(e.getMessage(), e);
                }
            }
            return false;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
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

    public static Client getSSLClient() {
        boolean isIgnoreHostnameVerification = Boolean.parseBoolean(System.getProperty("org.wso2.ignoreHostnameVerification"));
        if(isIgnoreHostnameVerification) {
            return new Client.Default(getSimpleTrustedSSLSocketFactory(), new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        }else {
            return new Client.Default(getTrustedSSLSocketFactory(), null);
        }
    }

    private static SSLSocketFactory getSimpleTrustedSSLSocketFactory() {
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

    //FIXME - I know hard-cording values is a bad practice , this code is repeating in
    // several class, so this hard-coding strings will be removed once this code block is moved into a central location
    // this should be done after the 3.1.0 release.
    private static SSLSocketFactory getTrustedSSLSocketFactory() {
        try {
            String keyStorePassword = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Password");
            String keyStoreLocation = ServerConfiguration.getInstance().getFirstProperty("Security.KeyStore.Location");
            String trustStorePassword = ServerConfiguration.getInstance().getFirstProperty(
                    "Security.TrustStore.Password");
            String trustStoreLocation = ServerConfiguration.getInstance().getFirstProperty(
                    "Security.TrustStore.Location");

            KeyStore keyStore = loadKeyStore(keyStoreLocation,keyStorePassword,"JKS");
            KeyStore trustStore = loadTrustStore(trustStoreLocation,trustStorePassword);
            return initSSLConnection(keyStore,keyStorePassword,trustStore);
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException
                |CertificateException | IOException | UnrecoverableKeyException e) {
            log.error("Error while creating the SSL socket factory due to "+e.getMessage(),e);
            return null;
        }
    }

    private static SSLSocketFactory initSSLConnection(KeyStore keyStore,String keyStorePassword,KeyStore trustStore) throws NoSuchAlgorithmException, UnrecoverableKeyException,
            KeyStoreException, KeyManagementException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustStore);

        // Create and initialize SSLContext for HTTPS communication
        SSLContext sslContext = SSLContext.getInstance("SSLv3");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        SSLContext.setDefault(sslContext);
        return  sslContext.getSocketFactory();
    }

    private static KeyStore loadKeyStore(String keyStorePath, String ksPassword, String type)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        InputStream fileInputStream = null;
        try {
            char[] keypassChar = ksPassword.toCharArray();
            KeyStore keyStore = KeyStore.getInstance(type);
            fileInputStream = new FileInputStream(keyStorePath);
            keyStore.load(fileInputStream, keypassChar);
            return keyStore;
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    private static KeyStore loadTrustStore(String trustStorePath, String tsPassword)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

        return loadKeyStore(trustStorePath,tsPassword,"JKS");
    }

}