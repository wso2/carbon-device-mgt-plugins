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

package org.wso2.carbon.mdm.services.android.omadm.dm.security;

import org.apache.axiom.om.util.Base64;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.MetaTag;

/**
 * This class represents a generic authentication entity in the OMADM
 */
public class Authentication {

    private String data = null;
    private String username = null;
    private String password = null;
    private boolean encode = false;
    private String deviceId = null;
    private String syncMLVerProto = null;
    private MetaTag meta = null;

    // Auth types
    private final String SYNCML_AUTH_BASIC = "syncml:auth-basic";
    private final String SYNCML_AUTH_CLEAR = "syncml:auth-clear";
    private final String SYNCML_AUTH_MD5 = "syncml:auth-md5";
    private final String SYNCML_AUTH_MAC = "syncml:auth-MAC";

    private final String B64 = "b64";
    private final String CLEAR = "clear";

    public Authentication() {
    }

    public Authentication(MetaTag meta, String data) {
        this.meta = meta;
        createAuthentication(meta.getType(), data);
    }

    public Authentication(String authType, String data) {
        createAuthentication(authType, data);
    }

    public Authentication(String authType, String data, boolean encode) {
        this.encode = encode;
        createAuthentication(authType, data);
    }

    public Authentication(String authType, String username, String password) {
        this(authType, username + ":" + password, true);
        if ((username == null) || (password == null)) {
            throw new IllegalArgumentException("The authentication username and password cannot be null");
        }
    }

    public void createAuthentication(String authType, String data) {
        String str = SYNCML_AUTH_BASIC + "," + SYNCML_AUTH_CLEAR + "," + SYNCML_AUTH_MD5 + "," + SYNCML_AUTH_MAC;
        if (str.indexOf(authType) < 0) {
            authType = SYNCML_AUTH_BASIC;
        }
        if (SYNCML_AUTH_BASIC.equals(authType)) {
            setType(SYNCML_AUTH_BASIC);
            setFormat(B64);
            setData(data);
        } else if (SYNCML_AUTH_CLEAR.equals(authType)) {
            setType(SYNCML_AUTH_CLEAR);
            setFormat(CLEAR);
            setData(data);
        } else if (SYNCML_AUTH_MD5.equals(authType)) {
            setType(SYNCML_AUTH_MD5);
            setData(data);
        } else if (SYNCML_AUTH_MAC.equals(authType)) {
            setType(SYNCML_AUTH_MAC);
            setData(data);
        }
    }

    public String getType() {
        return this.meta == null ? null : this.meta.getType();
    }

    public void setType(String type) {
        if (this.meta == null) {
            this.meta = new MetaTag();
        }
        this.meta.setType(type);
    }

    public String getFormat() {
        return this.meta == null ? null : this.meta.getFormat();
    }

    public void setFormat(String format) {
        if (this.meta == null) {
            this.meta = new MetaTag();
        }
        this.meta.setFormat(format);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null");
        }
        String strType = getType();
        if (strType.equals(SYNCML_AUTH_CLEAR)) {
            this.data = data;
            int i = data.indexOf(':');
            if (i == -1) {
                setUsername(data);
                setPassword(null);
            } else {
                this.username = (i > 0 ? data.substring(0, i) : "");
                this.password = (i < data.length() ? data.substring(i) : "");
            }
        } else if (strType.equals(SYNCML_AUTH_BASIC)) {
            String strData = null;
            if (this.encode) {
                this.data = new String(Base64.encode(data.getBytes()));
                strData = data;
            } else {
                strData = new String(Base64.decode(data));
                this.data = data;
            }
            int j = strData.indexOf(':');
            if (j == -1) {
                setUsername(strData);
                setPassword(null);
            } else {
                this.username = (j > 0 ? strData.substring(0, j) : "");
                this.password = (j < data.length() ? strData.substring(j) : "");
            }
        } else if (strType.equals(SYNCML_AUTH_BASIC)) {
            if (this.meta.getFormat() == null) {
                setFormat(B64);
            }
            this.username = data;
            this.data = data;
        }
    }

    public String getNextNonce() {
        return this.meta == null ? null : this.meta.getNextNonce();
    }

    public void setNextNonce(String nextNonce) {
        if (this.meta == null) {
            this.meta = new MetaTag();
        }
        this.meta.setNextNonce(nextNonce);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEncode() {
        return encode;
    }

    public void setEncode(boolean encode) {
        this.encode = encode;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSyncMLVerProto() {
        return syncMLVerProto;
    }

    public void setSyncMLVerProto(String syncMLVerProto) {
        this.syncMLVerProto = syncMLVerProto;
    }

    public MetaTag getMeta() {
        return meta;
    }

    public void setMeta(MetaTag meta) {
        this.meta = meta;
    }
}
