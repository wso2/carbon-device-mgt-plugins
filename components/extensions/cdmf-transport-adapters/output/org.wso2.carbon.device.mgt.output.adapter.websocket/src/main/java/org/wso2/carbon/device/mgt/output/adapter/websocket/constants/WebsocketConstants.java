/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.device.mgt.output.adapter.websocket.constants;

/**
 * This holds the constants related to this feature
 */
public class WebsocketConstants {

    private WebsocketConstants() {
    }

    public static final String SCOPE_IDENTIFIER = "scopes";
    public static final String MAXIMUM_TOTAL_HTTP_CONNECTION = "maximumTotalHttpConnection";
    public static final String MAXIMUM_HTTP_CONNECTION_PER_HOST = "maximumHttpConnectionPerHost";
    public static final String TOKEN_VALIDATION_ENDPOINT_URL = "tokenValidationUrl";
    public static final String TOKEN_VALIDATION_CONTEX = "/services/OAuth2TokenValidationService";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String TOKEN_PARAM = "token";
}
