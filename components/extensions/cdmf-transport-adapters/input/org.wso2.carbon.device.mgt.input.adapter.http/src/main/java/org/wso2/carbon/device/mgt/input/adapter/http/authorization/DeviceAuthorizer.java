/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.input.adapter.http.authorization;

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
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.device.mgt.input.adapter.http.authorization.client.OAuthRequestInterceptor;
import org.wso2.carbon.device.mgt.input.adapter.http.authorization.client.dto.AuthorizationRequest;
import org.wso2.carbon.device.mgt.input.adapter.http.authorization.client.dto.DeviceAccessAuthorizationAdminService;
import org.wso2.carbon.device.mgt.input.adapter.http.authorization.client.dto.DeviceAuthorizationResult;
import org.wso2.carbon.device.mgt.input.adapter.http.authorization.client.dto.DeviceIdentifier;
import org.wso2.carbon.device.mgt.input.adapter.http.util.AuthenticationInfo;
import org.wso2.carbon.device.mgt.input.adapter.http.util.PropertyUtils;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This authorizer crossvalidates the request with device id and device type.
 */
public class DeviceAuthorizer {

    private static DeviceAccessAuthorizationAdminService deviceAccessAuthorizationAdminService;
    private static final String CDMF_SERVER_BASE_CONTEXT = "/api/device-mgt/v1.0";
    private static final String DEVICE_MGT_SERVER_URL = "deviceMgtServerUrl";
    private static Log log = LogFactory.getLog(DeviceAuthorizer.class);

    public DeviceAuthorizer(Map<String, String> globalProperties) {
        try {
            deviceAccessAuthorizationAdminService = Feign.builder().client(getSSLClient()).logger(new Slf4jLogger())
                    .logLevel(Logger.Level.FULL).requestInterceptor(new OAuthRequestInterceptor(globalProperties))
                    .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                    .target(DeviceAccessAuthorizationAdminService.class, getDeviceMgtServerUrl(globalProperties)
                            + CDMF_SERVER_BASE_CONTEXT);
        } catch (InputEventAdapterException e) {
            log.error("Invalid value for deviceMgtServerUrl in globalProperties.");
        }
    }


    public boolean isAuthorized(AuthenticationInfo authenticationInfo, String deviceId, String deviceType) {

        if (deviceId != null && !deviceId.isEmpty() && deviceType != null && !deviceType.isEmpty()) {

            AuthorizationRequest authorizationRequest = new AuthorizationRequest();
            authorizationRequest.setTenantDomain(authenticationInfo.getTenantDomain());
            authorizationRequest.setUsername(authenticationInfo.getUsername());
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
                        return true;
                    }
                }
            } catch (FeignException e) {
                log.error(e.getMessage(), e);
            }
        }
        return false;
    }

    private String getDeviceMgtServerUrl(Map<String, String> properties) throws InputEventAdapterException {
        String deviceMgtServerUrl = PropertyUtils.replaceProperty(properties.get(DEVICE_MGT_SERVER_URL));
        if (deviceMgtServerUrl == null || deviceMgtServerUrl.isEmpty()) {
            log.error("deviceMgtServerUrl can't be empty ");
        }
        return deviceMgtServerUrl;
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