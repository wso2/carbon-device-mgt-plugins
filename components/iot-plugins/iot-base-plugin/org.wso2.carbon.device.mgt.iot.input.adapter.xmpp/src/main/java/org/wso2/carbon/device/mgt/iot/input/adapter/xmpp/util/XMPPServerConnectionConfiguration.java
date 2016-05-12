/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.device.mgt.iot.input.adapter.xmpp.util;

import java.util.Map;

/**
 * This holds the configurations related to MQTT Broker.
 */
public class XMPPServerConnectionConfiguration {

    private String host;
    private int port;
    private String username;
    private String password;
    private int timeoutInterval;
    private String resource;
    private String jid;
    private String contentValidatorClassName;
    private Map<String, String> contentValidatorParams;
    private String contentTransformerClassName;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getTimeoutInterval() {
        return timeoutInterval;
    }

    public String getResource() {
        return resource;
    }

    public String getContentValidatorClassName() {
        return contentValidatorClassName;
    }

    public Map<String, String> getContentValidatorParams() {
        return contentValidatorParams;
    }

    public String getJid() {
        return jid;
    }

    public String getContentTransformerClassName() {
        return contentTransformerClassName;
    }

    public XMPPServerConnectionConfiguration(String host, int port, String username, String password,
                                             int timeoutInterval, String resource, String contentValidatorClassName,
                                             Map<String, String> contentValidatorParams, String jid,
                                             String contentTransformerClassName) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.timeoutInterval = timeoutInterval;
        this.resource = resource;
        this.contentValidatorClassName = contentValidatorClassName;
        if (contentValidatorParams != null) {
            this.contentValidatorParams = contentValidatorParams;
        }
        this.contentTransformerClassName = contentTransformerClassName;
        this.jid = jid;
    }

}
