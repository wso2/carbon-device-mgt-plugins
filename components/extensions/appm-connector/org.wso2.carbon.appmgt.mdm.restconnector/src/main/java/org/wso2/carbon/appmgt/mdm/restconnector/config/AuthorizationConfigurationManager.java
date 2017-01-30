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
package org.wso2.carbon.appmgt.mdm.restconnector.config;

import org.wso2.carbon.appmgt.mdm.restconnector.Constants;
import org.wso2.carbon.appmgt.mobile.utils.MobileConfigurations;

import java.util.HashMap;

/**
 * The DTO class to AuthorizationConfigurationManager.
 */
public class AuthorizationConfigurationManager {
    private String tokenApiURL;
    private String imageURL;
    private String serverURL;
    private String userName;
    private String password;
    private String tokenRefreshTimeOffset;

    private static AuthorizationConfigurationManager authorizationConfigurationManager = new
            AuthorizationConfigurationManager();

    private AuthorizationConfigurationManager() {
        MobileConfigurations configurations = MobileConfigurations.getInstance();
        HashMap<String, String> configProperties = configurations.getActiveMDMProperties();
        setTokenApiURL(configProperties.get(Constants.PROPERTY_TOKEN_API_URL));
        setImageURL(configProperties.get(Constants.PROPERTY_IMAGE_URL));
        setServerURL(configProperties.get(Constants.PROPERTY_SERVER_URL));
        setUserName(configProperties.get(Constants.PROPERTY_USERNAME));
        setPassword(configProperties.get(Constants.PROPERTY_PASSWORD));
        setTokenRefreshTimeOffset(configProperties.get(Constants.PROPERTY_TOKEN_REFRESH_TIME_OFFSET));
    }

    public static AuthorizationConfigurationManager getInstance() {
        return authorizationConfigurationManager;
    }

    public String getTokenApiURL() {
        return tokenApiURL;
    }

    public void setTokenApiURL(String tokenApiURL) {
        this.tokenApiURL = tokenApiURL;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static AuthorizationConfigurationManager getAuthorizationConfigurationManager() {
        return authorizationConfigurationManager;
    }

    public static void setAuthorizationConfigurationManager(
            AuthorizationConfigurationManager authorizationConfigurationManager) {
        AuthorizationConfigurationManager.authorizationConfigurationManager = authorizationConfigurationManager;
    }

    public String getTokenRefreshTimeOffset() {
        return tokenRefreshTimeOffset;
    }

    public void setTokenRefreshTimeOffset(String tokenRefreshTimeOffset) {
        this.tokenRefreshTimeOffset = tokenRefreshTimeOffset;
    }
}