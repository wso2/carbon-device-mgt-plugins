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

import com.hazelcast.util.Base64;
import java.io.Serializable;

/**
 * This class represents a single DMAccount in the 'DMAcc' management tree.
 */
public class DMAccount implements Serializable {

    public static final String DMACC_ADDR = "/Addr";
    public static final String DMACC_ADDR_TYPE = "/AddrType";
    public static final String DMACC_PORT_NBR = "/PortNbr";
    public static final String DMACC_CON_REF = "/ConRef";
    public static final String DMACC_SERVER_ID = "/ServerId";
    public static final String DMACC_SERVER_PASSWORD = "/ServerPW";
    public static final String DMACC_SERVER_NONCE = "/ServerNonce";
    public static final String DMACC_USERNAME = "/UserName";
    public static final String DMACC_CLIENT_PASSWORD = "/ClientPW";
    public static final String DMACC_CLIENT_NONCE = "/ClientNonce";
    public static final String DMACC_AUTH_PREF = "/AuthPref";
    public static final String DMACC_NAME = "/Name";

    private String address;
    private int addressType;
    private int portNumber;
    private String conRef;
    private String serverId;
    private String serverPassword;
    private byte[] serverNonce;
    private String userName;
    private String clientPassword;
    private byte[] clientNonce;
    private String authPref;
    private String name;

    public DMAccount() {}

    public DMAccount(String address, int addressType, int portNumber, String conRef, String serverId,
                     String serverPassword, byte[] serverNonce, String userName, String clientPassword,
                     byte[] clientNonce, String authPref, String name)
    {
        this.address = address;
        this.addressType = addressType;
        this.portNumber = portNumber;
        this.conRef = conRef;
        this.serverId = serverId;
        this.serverPassword = serverPassword;
        this.serverNonce = serverNonce;
        this.userName = userName;
        this.clientPassword = clientPassword;
        this.clientNonce = clientNonce;
        this.authPref = authPref;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAddressType() {
        return addressType;
    }

    public void setAddressType(int addressType) {
        this.addressType = addressType;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    public String getConRef() {
        return conRef;
    }

    public void setConRef(String conRef) {
        this.conRef = conRef;
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

    public byte[] getServerNonce() {
        return serverNonce;
    }

    public void setServerNonce(byte[] serverNonce) {
        this.serverNonce = Base64.decode(serverNonce);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getClientPassword() {
        return clientPassword;
    }

    public void setClientPassword(String clientPassword) {
        this.clientPassword = clientPassword;
    }

    public byte[] getClientNonce() {
        return clientNonce;
    }

    public void setClientNonce(byte[] clientNonce) {
        this.clientNonce = clientNonce;
    }

    public String getAuthPref() {
        return authPref;
    }

    public void setAuthPref(String authPref) {
        this.authPref = authPref;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
