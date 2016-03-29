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

package org.wso2.carbon.mdm.services.android.omadm.dm.dmtree.ddf.standardmos;

/**
 * This class represents the standard 'DevInfo' Management Object
 */
public class DevDetail {

    // Maximum depth of the management tree supported by the device
    public static final String URI_MAX_DEPTH = "/URI/MaxDepth";
    // Maximum total length of any URI used to address a node or node property
    public static final String URI_MAX_TOT_LEN = "/URI/MaxToLen";
    // Maximum total length of any URI segment in a URI used to address a node or node property
    public static final String URI_MAX_SEG_LEN = "/URI/MaxSegLen";
    // Device type
    public static final String DEV_TYP = "/DevTyp";
    // Original Equipment Manufacturer of the device
    public static final String OEM = "/OEM";
    // Firmware version of the device
    public static final String FWV = "/FwV";
    // Software version of the device
    public static final String SWV = "/SwV";
    // Hardware version of the device
    public static final String HWV = "/HwV";
    // Indicates whether the device supports the OMA DM Large Object Handling specification
    public static final String LRG_OBJ = "/LrgObj";

    private String uriMaxDepth;
    private String uriMaxLen;
    private String uriMaxSegLen;
    private String devTyp;
    private String oem;
    private String fwVersion;
    private String swVersion;
    private String hwVersion;
    private String lrgObj;

    public DevDetail() {}

    public DevDetail(String uriMaxDepth, String uriMaxLen, String uriMaxSegLen, String devTyp, String oem,
                     String fwVersion, String swVersion, String hwVersion, String lrgObj) {
        this.uriMaxDepth = uriMaxDepth;
        this.uriMaxLen = uriMaxLen;
        this.uriMaxSegLen = uriMaxSegLen;
        this.devTyp = devTyp;
        this.oem = oem;
        this.fwVersion = fwVersion;
        this.swVersion = swVersion;
        this.hwVersion = hwVersion;
        this.lrgObj = lrgObj;
    }

    public String getUriMaxDepth() {
        return uriMaxDepth;
    }

    public void setUriMaxDepth(String uriMaxDepth) {
        this.uriMaxDepth = uriMaxDepth;
    }

    public String getUriMaxLen() {
        return uriMaxLen;
    }

    public void setUriMaxLen(String uriMaxLen) {
        this.uriMaxLen = uriMaxLen;
    }

    public String getUriMaxSegLen() {
        return uriMaxSegLen;
    }

    public void setUriMaxSegLen(String uriMaxSegLen) {
        this.uriMaxSegLen = uriMaxSegLen;
    }

    public String getDevTyp() {
        return devTyp;
    }

    public void setDevTyp(String devTyp) {
        this.devTyp = devTyp;
    }

    public String getOem() {
        return oem;
    }

    public void setOem(String oem) {
        this.oem = oem;
    }

    public String getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(String fwVersion) {
        this.fwVersion = fwVersion;
    }

    public String getSwVersion() {
        return swVersion;
    }

    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    public String getHwVersion() {
        return hwVersion;
    }

    public void setHwVersion(String hwVersion) {
        this.hwVersion = hwVersion;
    }

    public String getLrgObj() {
        return lrgObj;
    }

    public void setLrgObj(String lrgObj) {
        this.lrgObj = lrgObj;
    }
}
