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
package org.wso2.carbon.device.mgt.output.adapter.mqtt.util;

import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;

import java.util.Map;

public class MQTTBrokerConnectionConfiguration {

    private String adapterName;
    private String username;
    private String password;
    private String dcrUrl;
    private String scopes;
    private String brokerUrl;
    private String tokenUrl;
    private boolean cleanSession = true;
    private int keepAlive;
    private boolean globalCredentailSet;
    private int qos;

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getDcrUrl() {
        return dcrUrl;
    }

    public String getScopes() {
        return scopes;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public boolean isGlobalCredentailSet() {
        return globalCredentailSet;
    }

    public int getQos() {
        return qos;
    }
    public MQTTBrokerConnectionConfiguration(OutputEventAdapterConfiguration eventAdapterConfiguration,
                                             Map<String, String> globalProperties) {
        adapterName = eventAdapterConfiguration.getName();
        this.username = eventAdapterConfiguration.getStaticProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_USERNAME);
        this.password = eventAdapterConfiguration.getStaticProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_PASSWORD);
        if ((username == null || username.isEmpty()) && (password == null || password.isEmpty())) {
            username = globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_USERNAME);
            password = globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_PASSWORD);
            globalCredentailSet = true;
        }
        String url = eventAdapterConfiguration .getStaticProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_URL);
        if (url == null || url.isEmpty()) {
            url = globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_URL);
        }
        this.brokerUrl = PropertyUtils.replaceMqttProperty(url);
        this.dcrUrl = PropertyUtils
                .replaceMqttProperty(globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_DCR_URL));
        this.tokenUrl = PropertyUtils
                .replaceMqttProperty(globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_TOKEN_URL));
        this.scopes = eventAdapterConfiguration.getStaticProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_SCOPES);
        if (scopes == null) {
            this.scopes = MQTTEventAdapterConstants.EMPTY_STRING;
        }
        String cleanSession = globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_CLEAN_SESSION);
        if (cleanSession == null || cleanSession.isEmpty()) {
            this.cleanSession = Boolean.parseBoolean(eventAdapterConfiguration.getStaticProperties()
                                                             .get(MQTTEventAdapterConstants.ADAPTER_CONF_CLEAN_SESSION));
        } else {
            this.cleanSession = Boolean.parseBoolean(cleanSession);
        }
        //If global properties are available those will be assigned else constant values will be assigned
        if (globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_KEEP_ALIVE) != null) {
            keepAlive = Integer.parseInt((globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_KEEP_ALIVE)));
        } else {
            keepAlive = MQTTEventAdapterConstants.ADAPTER_CONF_DEFAULT_KEEP_ALIVE;
        }
        String qosVal = globalProperties.get(MQTTEventAdapterConstants.ADAPTER_MESSAGE_QOS);
        if (qosVal == null || qosVal.isEmpty()) {
            this.qos = Integer.parseInt(qosVal);
        } else {
            qosVal = eventAdapterConfiguration.getStaticProperties().get(MQTTEventAdapterConstants.ADAPTER_MESSAGE_QOS);
            this.qos = Integer.parseInt(qosVal);
        }


    }

}
