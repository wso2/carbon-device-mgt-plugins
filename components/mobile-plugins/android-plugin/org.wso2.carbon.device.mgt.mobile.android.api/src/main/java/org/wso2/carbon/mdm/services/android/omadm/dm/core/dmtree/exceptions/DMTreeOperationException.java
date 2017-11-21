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

package org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.exceptions;

/**
 * Custom exception class for DM Tree operations related errors
 */
public class DMTreeOperationException extends DMException {
    private static final long serialVersionUID = 3987650447893900L;
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public DMTreeOperationException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
        setErrorMessage(msg);
    }

    public DMTreeOperationException(String message, Throwable cause) {
        super(message, cause);
        setErrorMessage(message);
    }

    public DMTreeOperationException(String msg) {
        super(msg);
        setErrorMessage(msg);
    }

    public DMTreeOperationException() {
        super();
    }

    public DMTreeOperationException(Throwable cause) {
        super(cause);
    }
}
