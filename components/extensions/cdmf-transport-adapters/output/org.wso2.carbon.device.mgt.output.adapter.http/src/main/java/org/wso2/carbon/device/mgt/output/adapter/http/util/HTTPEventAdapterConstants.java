/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
*/
package org.wso2.carbon.device.mgt.output.adapter.http.util;

public class HTTPEventAdapterConstants {

    public static final String ADAPTER_TYPE_HTTP = "oauth-http";
    public static final String ADAPTER_MESSAGE_URL = "http.url";
    public static final String ADAPTER_MESSAGE_URL_TEMPLATED = "http.url.templated";
    public static final String ADAPTER_MESSAGE_URL_TEMPLATED_HINT = "http.url.templated.hint";
    public static final String ADAPTER_MESSAGE_URL_HINT = "http.url.hint";
    public static final String ADAPTER_CONF_DCR_URL = "dcrUrl";
    public static final String ADAPTER_CONF_TOKEN_URL = "tokenUrl";
    public static final int ADAPTER_MIN_THREAD_POOL_SIZE = 8;
    public static final int ADAPTER_MAX_THREAD_POOL_SIZE = 100;
    public static final int ADAPTER_EXECUTOR_JOB_QUEUE_SIZE = 2000;
    public static final long DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS = 20000;
    public static final String ADAPTER_MIN_THREAD_POOL_SIZE_NAME = "minThread";
    public static final String ADAPTER_MAX_THREAD_POOL_SIZE_NAME = "maxThread";
    public static final String ADAPTER_KEEP_ALIVE_TIME_NAME = "keepAliveTimeInMillis";
    public static final String ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME = "jobQueueSize";
    public static final String ADAPTER_USERNAME = "username";
    public static final String ADAPTER_USERNAME_HINT = "http.username.hint";
    public static final String ADAPTER_PASSWORD = "password";
    public static final String ADAPTER_PASSWORD_HINT = "http.password.hint";
    public static final String ADAPTER_CONF_SCOPES = "scopes";
    public static final String ADAPTER_CONF_SCOPES_HINT = "scopes.hint";
    public static final String ADAPTER_HEADERS = "http.headers";
    public static final String ADAPTER_HEADERS_HINT = "http.headers.hint";
    public static final String HEADER_SEPARATOR = ",";
    public static final String ENTRY_SEPARATOR = ":";
    public static final String ADAPTER_HTTP_CLIENT_METHOD = "http.client.method";
    public static final String CONSTANT_HTTP_POST = "HttpPost";
    public static final String CONSTANT_HTTP_PUT = "HttpPut";

    public static final String EMPTY_STRING = "";
    public static final String DEFAULT_CALLBACK = "";
    public static final String DEFAULT_PASSWORD = "";
    public static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
    public static final String TOKEN_SCOPE = "production";
    public static final String APPLICATION_NAME_PREFIX = "OutputAdapter_";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";

    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    public static final String AUTHORIZATION_HEADER_VALUE_PREFIX = "Basic ";

    //configurations for the httpConnectionManager
    public static final String DEFAULT_MAX_CONNECTIONS_PER_HOST = "defaultMaxConnectionsPerHost";
    public static final int DEFAULT_DEFAULT_MAX_CONNECTIONS_PER_HOST = 2;
    public static final String MAX_TOTAL_CONNECTIONS = "maxTotalConnections";
    public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 20;
}
