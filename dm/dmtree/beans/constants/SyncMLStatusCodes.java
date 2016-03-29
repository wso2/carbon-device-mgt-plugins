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

package org.wso2.carbon.mdm.services.android.omadm.dm.dmtree.beans.constants;

/**
 * Contains all the SyncML status codes
 */
public enum SyncMLStatusCodes {
    NONE("0"),
    IN_PROGRESS("101"),
    SUCCESS("200"),
    AUTHENTICATION_ACCEPTED("212"),
    OPERATION_CANCELED("214"),
    NOT_EXECUTED("215"),
    NOT_MODIFIED("304"),
    INVALID_CREDENTIALS("401"),
    FORBIDDEN("403"),
    NOT_FOUND("404"),
    NOT_ALLOWED("405"),
    OPTIONAL_FEATURE_NOT_SUPPORTED("406"),
    MISSING_CREDENTIALS("407"),
    INCOMPLETE_COMMAND("412"),
    URI_TOO_LONG("414"),
    ALREADY_EXISTS("418"),
    DEVICE_FULL("420"),
    PERMISSION_DENIED("425"),
    COMMAND_FAILED("500"),
    COMMAND_NOT_IMPLEMENTED("501"),
    SESSION_INTERNAL("506"),
    ATOMIC_FAILED("507");

    private final String code;

    SyncMLStatusCodes(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
