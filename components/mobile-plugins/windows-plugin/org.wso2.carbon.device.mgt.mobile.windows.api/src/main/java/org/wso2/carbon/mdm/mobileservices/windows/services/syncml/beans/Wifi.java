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

package org.wso2.carbon.mdm.mobileservices.windows.services.syncml.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Bean for WIFI configurations.
 */
@ApiModel(value = "Wifi",
        description = "This class carries all information related to Wifi policy.")
public class Wifi extends BasicOperation {

    @ApiModelProperty(name = "networkName", value = "Name of the Wifi Network.", required = true)
    private String networkName;
    @ApiModelProperty(name = "ssid", value = "SSID.", required = true)
    private String ssid;
    @ApiModelProperty(name = "connectionType", value = "Type of the connection.", required = true)
    private String connectionType;
    @ApiModelProperty(name = "connectionMode", value = "Connection mode.", required = true)
    private String connectionMode;
    @ApiModelProperty(name = "authentication", value = "Authentication type.", required = true)
    private String authentication;
    @ApiModelProperty(name = "encryption", value = "Encryption type.", required = true)
    private String encryption;
    @ApiModelProperty(name = "keyType", value = "Key type.", required = true)
    private String keyType;
    @ApiModelProperty(name = "protection", value = "Protection.", required = true)
    private String protection;
    @ApiModelProperty(name = "keyMaterial", value = "Key Material.", required = true)
    private String keyMaterial;

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getConnectionMode() {
        return connectionMode;
    }

    public void setConnectionMode(String connectionMode) {
        this.connectionMode = connectionMode;
    }

    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getProtection() {
        return protection;
    }

    public void setProtection(String protection) {
        this.protection = protection;
    }

    public String getKeyMaterial() {
        return keyMaterial;
    }

    public void setKeyMaterial(String keyMaterial) {
        this.keyMaterial = keyMaterial;
    }
}
