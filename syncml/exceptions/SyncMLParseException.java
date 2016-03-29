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

package org.wso2.carbon.mdm.services.android.omadm.syncml.exceptions;

/**
 * The base exception class for SyncML parsing-related errors
 */
public class SyncMLParseException extends  RuntimeException {
    static final long serialVersionUID = -4086729973971783390L;

    public SyncMLParseException(String msg) {
        super(msg);
    }

    public SyncMLParseException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SyncMLParseException(Throwable cause) {
        super(cause);
    }
}
