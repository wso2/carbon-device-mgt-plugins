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
package org.wso2.carbon.iot.android.sense.util;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import org.wso2.carbon.iot.android.sense.constants.SenseConstants;
import org.wso2.carbon.iot.android.sense.util.dto.AccessTokenInfo;
import org.wso2.carbon.iot.android.sense.util.dto.AndroidConfiguration;
import org.wso2.carbon.iot.android.sense.util.dto.AndroidSenseManagerService;
import org.wso2.carbon.iot.android.sense.util.dto.ApiApplicationKey;
import org.wso2.carbon.iot.android.sense.util.dto.ApiApplicationRegistrationService;
import org.wso2.carbon.iot.android.sense.util.dto.ApiRegistrationProfile;
import org.wso2.carbon.iot.android.sense.util.dto.DynamicClientRegistrationService;
import org.wso2.carbon.iot.android.sense.util.dto.OAuthApplicationInfo;
import org.wso2.carbon.iot.android.sense.util.dto.OAuthRequestInterceptor;
import org.wso2.carbon.iot.android.sense.util.dto.RegistrationProfile;
import org.wso2.carbon.iot.android.sense.util.dto.TokenIssuerService;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import feign.Client;
import feign.Feign;
import feign.FeignException;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.jaxrs.JAXRSContract;

public class SenseClientAsyncExecutor extends AsyncTask<String, Void, Map<String, String>> {

    private final static String TAG = "SenseService Client";
    private static final String STATUS = "status";
    private final String PASSWORD_GRANT_TYPE = "password";
    private final static String DEVICE_NAME = Build.MANUFACTURER + " " + Build.MODEL;
    private Context context;

    public SenseClientAsyncExecutor(Context context) {
        this.context = context;

    }

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

    Client disableHostnameVerification = new Client.Default(getTrustedSSLSocketFactory(), new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
    });

    @Override
    protected Map<String, String> doInBackground(String... parameters) {
        if (android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        String response;
        Map<String, String> response_params = new HashMap<>();
        String username = parameters[0];
        String password = parameters[1];
        String deviceId = parameters[2];
        String endpoint = parameters[3];
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put(STATUS, "200");
        AccessTokenInfo accessTokenInfo = null;
        try {
            //ApiApplicationRegistration
            ApiApplicationRegistrationService apiApplicationRegistrationService = Feign.builder().client(disableHostnameVerification)
                    .requestInterceptor(new BasicAuthRequestInterceptor(username, password))
                    .contract(new JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                    .target(ApiApplicationRegistrationService.class, endpoint + SenseConstants.API_APPLICATION_REGISTRATION_CONTEXT);
            ApiRegistrationProfile apiRegistrationProfile = new ApiRegistrationProfile();
            apiRegistrationProfile.setApplicationName("android_sense_" + deviceId);
            apiRegistrationProfile.setIsAllowedToAllDomains(false);
            apiRegistrationProfile.setIsMappingAnExistingOAuthApp(false);
            apiRegistrationProfile.setTags(new String[]{SenseConstants.DEVICE_TYPE});
            ApiApplicationKey apiApplicationKey = apiApplicationRegistrationService.register(apiRegistrationProfile);

            //PasswordGrantType
            TokenIssuerService tokenIssuerService = Feign.builder().client(disableHostnameVerification).requestInterceptor(
                    new BasicAuthRequestInterceptor(apiApplicationKey.getConsumerKey(), apiApplicationKey.getConsumerSecret()))
                    .contract(new JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                    .target(TokenIssuerService.class, endpoint);
            accessTokenInfo = tokenIssuerService.getToken(PASSWORD_GRANT_TYPE, username, password, "device_" + deviceId);

            //DeviceRegister
            AndroidSenseManagerService androidSenseManagerService = Feign.builder().client(disableHostnameVerification)
                    .requestInterceptor(new OAuthRequestInterceptor(accessTokenInfo.getAccess_token()))
                    .contract(new JAXRSContract()).encoder(new JacksonEncoder()).decoder(new JacksonDecoder())
                    .target(AndroidSenseManagerService.class, endpoint + SenseConstants.REGISTER_CONTEXT);
            AndroidConfiguration androidConfiguration = androidSenseManagerService.register(deviceId, DEVICE_NAME);
            if (androidConfiguration != null) {
                LocalRegistry.addAccessToken(context, accessTokenInfo.getAccess_token());
                LocalRegistry.addRefreshToken(context, accessTokenInfo.getRefresh_token());
                LocalRegistry.addMqttEndpoint(context, androidConfiguration.getMqttEndpoint());
                LocalRegistry.addTenantDomain(context, androidConfiguration.getTenantDomain());
            }
            return responseMap;
        } catch (FeignException e) {
            responseMap.put(STATUS, "" + e.status());
            return responseMap;
        }
    }

    private SSLSocketFactory getTrustedSSLSocketFactory() {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            return sc.getSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            Log.e(SenseClientAsyncExecutor.class.getName(), "Invalid Certificate");
            return null;
        }

    }
}
