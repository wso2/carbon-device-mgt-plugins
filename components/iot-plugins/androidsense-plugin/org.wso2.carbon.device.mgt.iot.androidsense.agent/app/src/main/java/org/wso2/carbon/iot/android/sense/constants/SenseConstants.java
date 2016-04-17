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
package org.wso2.carbon.iot.android.sense.constants;

public class SenseConstants {
    public final static String DEVICE_TYPE = "android_sense";
    public final static String REGISTER_CONTEXT = "/android_sense_mgt";
    public final static String DCR_CONTEXT = "/dynamic-client-web";
    public final static String TOKEN_ISSUER_CONTEXT = "/oauth2";
    public final static String API_APPLICATION_REGISTRATION_CONTEXT = "/api-application-registration";

    public static final int MQTT_BROKER_PORT = 1883;
    public static final String EVENT_LISTENER_STARTED = "xxStartedxx";
    public static final String EVENT_LISTENER_FINISHED = "xxFinishedxx";
    public static final String EVENT_LISTENER_ONGOING = "xxOngoingxx";

    public final class Request {
        public final static String REQUEST_SUCCESSFUL = "200";
        public final static String REQUEST_CONFLICT = "409";
        public final static int MAX_ATTEMPTS = 2;
    }
}
