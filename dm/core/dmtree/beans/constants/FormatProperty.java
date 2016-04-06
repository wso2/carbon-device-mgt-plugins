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

package org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.constants;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Constants for the 'Format' Property in DFProperties
 */
@XmlEnum
public enum FormatProperty {

    @XmlEnumValue("b64")
    BASE64("b64"),
    @XmlEnumValue("bin")
    BINARY("bin"),
    @XmlEnumValue("bool")
    BOOLEAN("bool"),
    @XmlEnumValue("chr")
    CHAR("chr"),
    @XmlEnumValue("int")
    INTEGER("int"),
    @XmlEnumValue("node")
    NODE("node"),
    @XmlEnumValue("null")
    NULL("null"),
    @XmlEnumValue("xml")
    XML("xml"),
    @XmlEnumValue("date")
    DATE("date"),
    @XmlEnumValue("time")
    TIME("time"),
    @XmlEnumValue("float")
    FLOAT("float");

    private final String code;

    FormatProperty(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
