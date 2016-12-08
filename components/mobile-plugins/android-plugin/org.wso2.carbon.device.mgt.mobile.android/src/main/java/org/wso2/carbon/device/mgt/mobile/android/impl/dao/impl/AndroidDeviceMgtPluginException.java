/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.wso2.carbon.device.mgt.mobile.android.impl.dao.impl;


public class AndroidDeviceMgtPluginException extends Exception{

    private static final long serialVersionUID = -2297311387874900305L;
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public AndroidDeviceMgtPluginException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
        setErrorMessage(msg);
    }

    public AndroidDeviceMgtPluginException(String message, Throwable cause) {
        super(message, cause);
        setErrorMessage(message);
    }

    public AndroidDeviceMgtPluginException(String msg) {
        super(msg);
        setErrorMessage(msg);
    }

    public AndroidDeviceMgtPluginException() {
        super();
    }

    public AndroidDeviceMgtPluginException(Throwable cause) {
        super(cause);
    }

}
