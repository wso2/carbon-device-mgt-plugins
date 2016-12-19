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

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BinarySecurityToken", namespace = PluginConstants.WS_SECURITY_TARGET_NAMESPACE,
        propOrder = {"ValueType", "EncodingType"})
@SuppressWarnings("unused")
public class BinarySecurityToken {

    @XmlAttribute(name = "ValueType")
    protected String ValueType;
    @XmlAttribute(name = "EncodingType")
    protected String EncodingType;
    @XmlValue
    protected String Token;

    public void setValueType(String valuetype) {
        this.ValueType = valuetype;
    }

    public String getValueType() {
        return this.ValueType;
    }

    public void setEncodingType(String encodingtype) {
        this.EncodingType = encodingtype;
    }

    public String getEncodingType() {
        return this.EncodingType;
    }

    public void setToken(String token) {
        this.Token = token;
    }

    public String getToken() {
        return this.Token;
    }

}

