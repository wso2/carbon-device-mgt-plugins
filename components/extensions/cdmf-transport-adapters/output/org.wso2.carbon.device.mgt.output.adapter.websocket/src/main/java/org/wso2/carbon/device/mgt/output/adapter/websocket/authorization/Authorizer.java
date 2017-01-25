/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.output.adapter.websocket.authorization;

import org.wso2.carbon.device.mgt.output.adapter.websocket.authentication.AuthenticationInfo;

import javax.websocket.Session;
import java.util.Map;

/**
 * Check whether the client is authorized to connect.
 */
public interface Authorizer {

    /**
     * This is used to initialize the authenticator
     * @param globalProperties related to the output adapter
     */
    void init(Map<String, String> globalProperties);

    /**
     * Check whether the client is authorized to connect with the stream.
     * @param authenticationInfo authenticated client information.
     * @param session request session of the client.
     * @param stream stream name of the client connecting to.
     * @return true if authorized else return false.
     */
    boolean isAuthorized(AuthenticationInfo authenticationInfo, Session session, String stream);
}
