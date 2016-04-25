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

package org.wso2.carbon.mdm.services.android.omadm.ddf.standardmos;

import org.wso2.carbon.mdm.services.android.omadm.ddf.util.DDFCommonUtils;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.MgmtTree;

import java.io.Serializable;

/**
 * This class represents the standard 'DevInfo' Management Object
 */
public class DevInfo implements Serializable {

    // Device Identifier
    public static final String DEV_ID = "/DevId";
    // Manufacturer
    public static final String MAN = "/Man";
    // Device model
    public static final String MOD = "/Mod";
    // Device model version
    public static final String DMV = "/DmV";
    // Device language
    public static final String LANG = "/Lang";
    // Path to the DevInfo DDF file
    public static final String DEV_INFO_DDF_PATH = "org/wso2/carbon/mdm/services/android/omadm/ddf/" +
            "resources/OMA-SUP-MO_DM_DevInfo-V1_2-20070209-A.xml";

    private MgmtTree mgmtTree = null;
    private String devId;
    private String manufacturer;
    private String model;
    private String deviceModelVersion;
    private String language;

    public DevInfo() {
        this.mgmtTree = DDFCommonUtils.generateTree(DEV_INFO_DDF_PATH);
    }

    public MgmtTree getTree() {
        if (this.mgmtTree != null) {
            return this.mgmtTree;
        } else {
            this.mgmtTree = DDFCommonUtils.generateTree(DEV_INFO_DDF_PATH);
            return this.mgmtTree;
        }
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDeviceModelVersion() {
        return deviceModelVersion;
    }

    public void setDeviceModelVersion(String deviceModelVersion) {
        this.deviceModelVersion = deviceModelVersion;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
