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

package org.wso2.carbon.mdm.services.android.omadm.security;

/**
 * This class acts as an entity which holds HMAC authentication details
 */
public class HMACAuthentication extends Authentication {

    private static final String MD5 = "MD5";
    private static final String DEFAULT_ALGORITHM = "MD5";
    private static final String SYNCML_AUTH_MAC = "mac";
    private static final String B64 = "b64";
    private static final String ALGORITHM = "algorithm";
    private static final String USERNAME = "username";
    private static final String MAC = "mac";


    private String userMac;
    private String algorithm;
    private String calculatedMac;

    public HMACAuthentication() {
    }

    public HMACAuthentication(String paramString) {
        super(SYNCML_AUTH_MAC, B64, paramString);
    }

    public void setData(String paramString) {
        if (paramString == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        int i = paramString.indexOf(ALGORITHM);
        int j = -1;
        if (i == -1) {
            this.algorithm = MD5;
        } else {
            j = paramString.indexOf(",", i);
            this.algorithm = paramString.substring(i + 10, j);
        }
        int k = paramString.indexOf(USERNAME, j + 1);
        if (k == -1) {
            throw new IllegalArgumentException("Username missing in hmac header");
        }
        int m = paramString.indexOf("\"", k + 10);
        if (m == -1) {
            throw new IllegalArgumentException("Unable to get username from hmac header [" + paramString + "]");
        }
        while (paramString.charAt(m - 1) == '\\') {
            m = paramString.indexOf("\"", m + 1);
        }
        if (m == -1) {
            throw new IllegalArgumentException("Unable to get username from hmac header [" + paramString + "]");
        }
        setUsername(paramString.substring(k + 10, m));
        int n = paramString.indexOf(MAC, m);
        if (n == -1) {
            throw new IllegalArgumentException("Mac value missing in hmac header");
        }
        this.userMac = paramString.substring(n + 4);
    }

    public String getUserMac() {
        return this.userMac;
    }

    public void setUserMac(String paramString) {
        this.userMac = paramString;
    }

    public String getCalculatedMac() {
        return this.calculatedMac;
    }

    public void setCalculatedMac(String paramString) {
        this.calculatedMac = paramString;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

}

