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

package org.wso2.carbon.device.mgt.mobile.windows.api.services.enrollment.beans;

import org.wso2.carbon.device.mgt.mobile.windows.api.common.PluginConstants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RequestSecurityTokenResponse", namespace = PluginConstants.WS_TRUST_TARGET_NAMESPACE,
        propOrder = {"TokenType", "DispositionMessage", "RequestedSecurityToken", "RequestID"})
@SuppressWarnings("unused")
public class RequestSecurityTokenResponse implements Serializable {

    @XmlElement(name = "TokenType", namespace = PluginConstants.WS_TRUST_TARGET_NAMESPACE)
    private String TokenType;

    // Windows 10 property
    @XmlElement(name = "DispositionMessage", namespace = PluginConstants.ENROLLMENT_POLICY_TARGET_NAMESPACE)
    private String DispositionMessage;

    @XmlElement(name = "RequestedSecurityToken", required = true,
            namespace = PluginConstants.WS_TRUST_TARGET_NAMESPACE)
    private org.wso2.carbon.device.mgt.mobile.windows.api.services.enrollment.beans.RequestedSecurityToken RequestedSecurityToken;

    @XmlElement(name = "RequestID", namespace = PluginConstants.ENROLLMENT_POLICY_TARGET_NAMESPACE)
    private int RequestID;

    public String getTokenType() {
        return TokenType;
    }

    public void setTokenType(String tokenType) {
        TokenType = tokenType;
    }

    public org.wso2.carbon.device.mgt.mobile.windows.api.services.enrollment.beans.RequestedSecurityToken getRequestedSecurityToken() {
        return RequestedSecurityToken;
    }

    public void setRequestedSecurityToken(org.wso2.carbon.device.mgt.mobile.windows.api.services.enrollment.beans.RequestedSecurityToken
                                                  requestedSecurityToken) {
        RequestedSecurityToken = requestedSecurityToken;
    }

    public int getRequestID() {
        return RequestID;
    }

    public void setRequestID(int requestID) {
        RequestID = requestID;
    }

    public String getDispositionMessage() {
        return DispositionMessage;
    }

    public void setDispositionMessage(String dispositionMessage) {
        DispositionMessage = dispositionMessage;
    }

}