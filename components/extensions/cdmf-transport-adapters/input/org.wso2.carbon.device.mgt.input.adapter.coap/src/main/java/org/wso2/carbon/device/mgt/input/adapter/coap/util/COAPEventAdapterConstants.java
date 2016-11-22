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
package org.wso2.carbon.device.mgt.input.adapter.coap.util;

/**
 * This holds the constants related to COAP event adapter.
 */
public class COAPEventAdapterConstants {

    public static final String ADAPTER_TYPE_COAP = "oauth-coap";
    public static final int DEFAULT_COAP_PORT = 5683;
    public static final int DEFAULT_COAPS_PORT = 5684;

    public static final String EXPOSED_TRANSPORTS = "transports";
    public static final String COAPS = "coaps";
    public static final String COAP = "coap";
    public static final String LOCAL = "local";
    public static final String ALL = "all";


    public static final String USERNAME = "username";
    public static final String USERNAME_HINT = "username.hint";
    public static final String PASSWORD = "password";
    public static final String PASSWORD_HINT = "password.hint";

    //resource directory constants
    public static final String RESOURCE_DIRECTORY="rd";
    public static final String RESOURCE_DIRECTORY_LOOKUP="rd-lookup";
}
