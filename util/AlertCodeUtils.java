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

package org.wso2.carbon.mdm.services.android.omadm.util;

/**
 * This class contains utilities related to status codes
 */
public class AlertCodeUtils {

    public static final String UNKNOWN = "-1";
    public static final String TWO_WAY = "200";
    public static final String SLOW = "201";
    public static final String ONE_WAY_FROM_CLIENT = "202";
    public static final String REFRESH_FROM_CLIENT = "203";
    public static final String ONE_WAY_FROM_SERVER = "204";
    public static final String REFRESH_FROM_SERVER = "205";
    public static final String TWO_WAY_BY_SERVER = "206";
    public static final String ONE_WAY_FROM_CLIENT_BY_SERVER = "207";
    public static final String REFRESH_FROM_CLIENT_BY_SERVER = "208";
    public static final String ONE_WAY_FROM_SERVER_BY_SERVER = "209";
    public static final String REFRESH_FROM_SERVER_BY_SERVER = "210";
    public static final String RESULT_ALERT = "221";
    public static final String NEXT_MESSAGE = "222";
    public static final String DISPLAY = "1100";
    public static final String CONFIRM_OR_REJECT = "1101";
    public static final String INPUT = "1102";
    public static final String SINGLE_CHOICE = "1103";
    public static final String MULTIPLE_CHOICE = "1104";
    public static final String SERVER_INITIATED_MANAGEMENT = "1200";
    public static final String CLIENT_INITIATED_MANAGEMENT = "1201";
    public static final String MORE_DATA = "1222";
    public static final String SESSION_ABORT = "1223";
    public static final String CLIENT_EVENT = "1224";
    public static final String NO_END_OF_DATA = "1225";
    public static final String GENERIC_ALERT = "1226";

    public static boolean isInitializationCode(int code) {
        return (code == 200) || (code == 201) || (code == 202) || (code == 203) || (code == 204) || (code == 205) ||
                (code == 206) || (code == 207) || (code == 208) || (code == 209) || (code == 210) || (code == 1200) ||
                (code == 1201) || (code == 1222);
    }

    public static boolean isClientOnlyCode(int code) {
        return (code == 202) || (code == 203);
    }

    public static boolean isUserAlertCode(int code) {
        return (code == 1100) || (code == 1101) || (code == 1102) || (code == 1103) || (code == 1104);
    }

}
