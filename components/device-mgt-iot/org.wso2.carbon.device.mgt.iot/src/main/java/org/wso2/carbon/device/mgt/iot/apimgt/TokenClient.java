
/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.apimgt;

import java.io.IOException;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.iot.config.server.DeviceManagementConfigurationManager;
import org.wso2.carbon.device.mgt.iot.config.server.datasource.ApiManagerConfig;
import org.wso2.carbon.device.mgt.iot.exception.AccessTokenException;
import org.wso2.carbon.device.mgt.iot.exception.IoTException;
import org.wso2.carbon.device.mgt.iot.util.IoTUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TokenClient {

    private static Log log = LogFactory.getLog(TokenClient.class);
    private String tokenURL;
    private String grantType;
    private String scope;
    private String appToken = "";
    private String deviceType;

    public TokenClient(String deviceType) {
        this.deviceType = deviceType;

        ApiManagerConfig apiManagerConfig =
                DeviceManagementConfigurationManager.getInstance().getDeviceCloudMgtConfig().getApiManager();

        tokenURL = apiManagerConfig.getAccessTokenURL();
        grantType = DeviceManagementConfigurationManager.getInstance().getDeviceCloudMgtConfig()
                .getApiManager()
                .getDeviceGrantType();
        scope = "device_scope";
        appToken = ApisAppClient.getInstance().getBase64EncodedConsumerKeyAndSecret(deviceType);
    }

    public AccessTokenInfo getAccessToken(String username, String deviceId)
            throws AccessTokenException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", grantType));
        params.add(new BasicNameValuePair("device_id", deviceId));
        params.add(new BasicNameValuePair("device_type", deviceType));
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("scope", scope));
        return getTokenInfo(params);
    }

    public AccessTokenInfo getAccessToken(String refreshToken) throws AccessTokenException {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "refresh_token"));
        params.add(new BasicNameValuePair("refresh_token", refreshToken));
        params.add(new BasicNameValuePair("scope", scope));
        return getTokenInfo(params);
    }

    private AccessTokenInfo getTokenInfo(List<NameValuePair> nameValuePairs) throws AccessTokenException {
        try {
            URL tokenUrl = new URL(tokenURL);
            HttpClient httpClient = null;
            try {
                httpClient = IoTUtil.getHttpClient(tokenUrl.getPort(), tokenUrl.getProtocol());
            } catch (Exception e) {
                String msg = "Error on getting a http client for port :" + tokenUrl.getPort() + " protocol :"
                             + tokenUrl.getProtocol();
                log.error(msg);
                throw new AccessTokenException(msg);
            }
            HttpPost postMethod = new HttpPost(tokenURL);
            postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            if (appToken != null) {
                postMethod.addHeader("Authorization", "Basic " + appToken);
            }
            postMethod.addHeader("Content-Type", "application/x-www-form-urlencoded");

            HttpResponse httpResponse = httpClient.execute(postMethod);
            String response = IoTUtil.getResponseString(httpResponse);

            if (log.isDebugEnabled()) {
                log.debug(response);
            }

            JSONObject jsonObject = new JSONObject(response);
            AccessTokenInfo accessTokenInfo = new AccessTokenInfo();

            if (!jsonObject.get("access_token").equals(null)) {
                accessTokenInfo.setAccess_token(jsonObject.getString("access_token"));
            }
            if (!jsonObject.get("refresh_token").equals(null)) {
                accessTokenInfo.setRefresh_token(jsonObject.getString("refresh_token"));
            }
            if (!jsonObject.get("expires_in").equals(null)) {
                accessTokenInfo.setExpires_in(jsonObject.getInt("expires_in"));
            }
            if (!jsonObject.get("token_type").equals(null)) {
                accessTokenInfo.setToken_type(jsonObject.getString("token_type"));
            }

            return accessTokenInfo;

        } catch (IOException | JSONException | IoTException e) {
            log.error(e.getMessage());
            throw new AccessTokenException("Configuration Error for Access Token Generation");
        }
    }
}
