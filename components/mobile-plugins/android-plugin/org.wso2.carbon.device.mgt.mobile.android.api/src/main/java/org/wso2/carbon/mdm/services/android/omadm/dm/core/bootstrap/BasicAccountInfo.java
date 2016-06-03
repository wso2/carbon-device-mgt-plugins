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

package org.wso2.carbon.mdm.services.android.omadm.dm.core.bootstrap;

/**
 * This class holds basic information needed for a device bootstrapping
 */
public class BasicAccountInfo {

    private String deviceId;
    private String accountName;
    private String serverAddress;
    private int serverPort;
    private String serverId;
    private String serverPassword;
    private String serverNonce;
    private String clientUsername;
    private String clientPassword;
    private String clientNonce;
    private String authPref;
    private int addressType;

    public BasicAccountInfo(String deviceId, String accountName, String serverAddress, int serverPort,
                            String serverId, String serverPassword, String serverNonce, String clientUsername,
                            String clientPassword, String clientNonce, String authPref, int addressType) {
        this.deviceId = deviceId;
        this.accountName = accountName;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.serverId = serverId;
        this.serverPassword = serverPassword;
        this.serverNonce = serverNonce;
        this.clientUsername = clientUsername;
        this.clientPassword = clientPassword;
        this.clientNonce = clientNonce;
        this.authPref = authPref;
        this.addressType = addressType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerPassword() {
        return serverPassword;
    }

    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }

    public String getServerNonce() {
        return serverNonce;
    }

    public void setServerNonce(String serverNonce) {
        this.serverNonce = serverNonce;
    }

    public String getClientUsername() {
        return clientUsername;
    }

    public void setClientUsername(String clientUsername) {
        this.clientUsername = clientUsername;
    }

    public String getClientPassword() {
        return clientPassword;
    }

    public void setClientPassword(String clientPassword) {
        this.clientPassword = clientPassword;
    }

    public String getClientNonce() {
        return clientNonce;
    }

    public void setClientNonce(String clientNonce) {
        this.clientNonce = clientNonce;
    }

    public String getAuthPref() {
        return authPref;
    }

    public void setAuthPref(String authPref) {
        this.authPref = authPref;
    }

    public int getAddressType() {
        return addressType;
    }

    public void setAddressType(int addressType) {
        this.addressType = addressType;
    }
}
