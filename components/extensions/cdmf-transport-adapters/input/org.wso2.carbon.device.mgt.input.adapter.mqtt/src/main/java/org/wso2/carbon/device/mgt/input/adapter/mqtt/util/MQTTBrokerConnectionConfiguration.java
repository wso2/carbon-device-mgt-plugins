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
package org.wso2.carbon.device.mgt.input.adapter.mqtt.util;

import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;

/**
 * This holds the configurations related to MQTT Broker.
 */
public class MQTTBrokerConnectionConfiguration {

    private String brokerUsername = null;
    private String brokerScopes = null;
    private boolean cleanSession = true;
    private int keepAlive;
    private String brokerUrl;
    private String dcrUrl;
    private String contentValidatorClassName;
    private String contentTransformerClassName;

    public String getBrokerScopes() {
        return brokerScopes;
    }

    public String getBrokerUsername() {
        return brokerUsername;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public String getDcrUrl() {
        return dcrUrl;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public String getContentValidatorClassName() {
        return contentValidatorClassName;
    }

    public String getContentTransformerClassName() {
        return contentTransformerClassName;
    }

    public MQTTBrokerConnectionConfiguration(String brokerUrl, String brokerUsername, String brokerScopes,
                                             String dcrUrl, String cleanSession, int keepAlive,
                                             String contentValidatorClassName, String contentTransformerClassName) throws InputEventAdapterException {
        this.brokerUsername = brokerUsername;
        this.brokerScopes = brokerScopes;
        if (brokerScopes == null) {
            this.brokerScopes = MQTTEventAdapterConstants.EMPTY_STRING;
        }
        this.brokerUrl = PropertyUtils.replaceMqttProperty(brokerUrl);
        this.dcrUrl = PropertyUtils.replaceMqttProperty(dcrUrl);
        this.contentValidatorClassName = contentValidatorClassName;
        if (cleanSession != null) {
            this.cleanSession = Boolean.parseBoolean(cleanSession);
        }
        this.keepAlive = keepAlive;
        this.contentTransformerClassName = contentTransformerClassName;
    }
}
