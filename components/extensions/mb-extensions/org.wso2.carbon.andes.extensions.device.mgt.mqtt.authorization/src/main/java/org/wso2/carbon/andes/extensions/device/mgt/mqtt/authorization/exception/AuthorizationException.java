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

package org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.exception;

public class AuthorizationException extends Exception {
    /**
     * Default Serialization UID
     */
    private static final long serialVersionUID = 1L;

    /**
     * error code for our custom exception type to identify specific scenarios and handle them properly.
     */
    private String errorCode = "";

    public AuthorizationException() {
    }

    public AuthorizationException(String message) {
        super(message);
    }

    /***
     * Constructor
     * @param message descriptive message
     * @param errorCode one of the above defined constants that classifies the error.
     */
    public AuthorizationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /***
     * Constructor
     * @param message descriptive message
     * @param cause reference to the exception for reference.
     */
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    /***
     * Constructor
     * @param message descriptive message
     * @param errorCode one of the above defined constants that classifies the error.
     * @param cause reference to the exception for reference.
     */
    public AuthorizationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /***
     * Constructor
     * @param cause reference to the exception for reference.
     */
    public AuthorizationException(Throwable cause) {
        super(cause);
    }

    /***
     * One of the above defined constants that classifies the error. e.g.- MESSAGE_CONTENT_OBSOLETE
     * @return
     */
    public String getErrorCode() {
        return errorCode;
    }
}
