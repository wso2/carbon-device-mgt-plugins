/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.extensions.remote.session.exception;


import javax.websocket.CloseReason;

/**
 * This Exception will be thrown, when there any interference with Remote RemoteSession.
 */
public class RemoteSessionInvalidException extends Exception {

    CloseReason closeReason;

    public RemoteSessionInvalidException(String msg, CloseReason closeReason, Exception nestedEx) {
        super(msg, nestedEx);
        this.closeReason = closeReason;
    }

    public RemoteSessionInvalidException(String message, CloseReason closeReason, Throwable cause) {
        super(message, cause);
        this.closeReason = closeReason;
    }

    public RemoteSessionInvalidException(String msg, CloseReason closeReason) {
        super(msg);
        this.closeReason = closeReason;
    }

    public CloseReason getCloseReason() {
        return closeReason;
    }

    public void setCloseReason(CloseReason closeReason) {
        this.closeReason = closeReason;
    }
}
