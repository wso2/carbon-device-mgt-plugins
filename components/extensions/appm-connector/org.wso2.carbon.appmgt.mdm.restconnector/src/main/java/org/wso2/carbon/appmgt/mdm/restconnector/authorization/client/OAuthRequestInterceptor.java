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

import feign.Client;
import feign.Feign;
import feign.FeignException;
import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jaxrs.JAXRSContract;
import feign.slf4j.Slf4jLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appmgt.mdm.restconnector.Constants;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.AccessTokenInfo;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.ApiApplicationKey;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.ApiApplicationRegistrationService;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.ApiRegistrationProfile;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.TokenIssuerService;
import org.wso2.carbon.appmgt.mdm.restconnector.config.AuthorizationConfigurationManager;
import org.wso2.carbon.appmgt.mdm.restconnector.internal.AuthorizationDataHolder;
import org.wso2.carbon.base.ServerConfiguration;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

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
    private static final String REFRESH_GRANT_TYPE = "refresh_token";
    private ApiApplicationRegistrationService apiApplicationRegistrationService;
    private TokenIssuerService tokenIssuerService;
    private static Log log = LogFactory.getLog(OAuthRequestInterceptor.class);
    private ApiApplicationKey apiApplicationKey;

    /**
     * Creates an interceptor that authenticates all requests.
     */
    public OAuthRequestInterceptor() {
        refreshTimeOffset = AuthorizationConfigurationManager.getInstance().getTokenRefreshTimeOffset();
        String username = AuthorizationConfigurationManager.getInstance().getUserName();
        String password = AuthorizationConfigurationManager.getInstance().getPassword();
        apiApplicationRegistrationService = Feign.builder().client(getSSLClient()).logger(new Slf4jLogger()).logLevel(
                Logger.Level.FULL).requestInterceptor(new BasicAuthRequestInterceptor(username, password))
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
            if (apiApplicationKey == null) {
                ApiRegistrationProfile apiRegistrationProfile = new ApiRegistrationProfile();
                apiRegistrationProfile.setApplicationName(APPLICATION_NAME);
                apiRegistrationProfile.setIsAllowedToAllDomains(false);
                apiRegistrationProfile.setIsMappingAnExistingOAuthApp(false);
                apiRegistrationProfile.setTags(DEVICE_MANAGEMENT_SERVICE_TAG);
                apiApplicationKey = apiApplicationRegistrationService.register(apiRegistrationProfile);
            }
            String consumerKey = apiApplicationKey.getConsumerKey();
            String consumerSecret = apiApplicationKey.getConsumerSecret();
            String username = AuthorizationConfigurationManager.getInstance().getUserName();
            String password = AuthorizationConfigurationManager.getInstance().getPassword();
            if (tokenIssuerService == null) {
                tokenIssuerService = Feign.builder().client(getSSLClient()).logger(new Slf4jLogger()).logLevel(
                        Logger.Level.FULL)
                        .requestInterceptor(new BasicAuthRequestInterceptor(consumerKey, consumerSecret))
                        .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                        .target(TokenIssuerService.class,
                                AuthorizationConfigurationManager.getInstance().getTokenApiURL());
            }
            tokenInfo = tokenIssuerService.getToken(PASSWORD_GRANT_TYPE, username, password);
            tokenInfo.setExpires_in(System.currentTimeMillis() + tokenInfo.getExpires_in());
        } else {
            synchronized (this) {
                if (System.currentTimeMillis() + Long.parseLong(refreshTimeOffset) > tokenInfo.getExpires_in()) {
                    try {
                        tokenInfo = tokenIssuerService.getToken(REFRESH_GRANT_TYPE, tokenInfo.getRefresh_token());
                        tokenInfo.setExpires_in(System.currentTimeMillis() + tokenInfo.getExpires_in());
                    } catch (FeignException e) {
                        tokenInfo = null;
                        apply(template);
                    }
                }
            }
        }
        String headerValue = Constants.RestConstants.BEARER + tokenInfo.getAccess_token();
        template.header(Constants.RestConstants.AUTHORIZATION, headerValue);
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
