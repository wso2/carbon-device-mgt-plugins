/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.device.mgt.output.adapter.http.util;

import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;

import java.util.Map;

public class HTTPConnectionConfiguration {

    private String adapterName;
    private String username;
    private String password;
    private String dcrUrl;
    private String scopes;
    private String tokenUrl;
    private boolean globalCredentialSet;

    public HTTPConnectionConfiguration(OutputEventAdapterConfiguration eventAdapterConfiguration,
                                       Map<String, String> globalProperties) {
        adapterName = eventAdapterConfiguration.getName();
        this.username = eventAdapterConfiguration.getStaticProperties().get(HTTPEventAdapterConstants.ADAPTER_USERNAME);
        this.password = eventAdapterConfiguration.getStaticProperties().get(HTTPEventAdapterConstants.ADAPTER_PASSWORD);
        if ((username == null || username.isEmpty()) && (password == null || password.isEmpty())) {
            username = globalProperties.get(HTTPEventAdapterConstants.ADAPTER_USERNAME);
            password = globalProperties.get(HTTPEventAdapterConstants.ADAPTER_PASSWORD);
            globalCredentialSet = true;
        }

        this.dcrUrl = PropertyUtils
                .replaceMqttProperty(globalProperties.get(HTTPEventAdapterConstants.ADAPTER_CONF_DCR_URL));
        this.tokenUrl = PropertyUtils
                .replaceMqttProperty(globalProperties.get(HTTPEventAdapterConstants.ADAPTER_CONF_TOKEN_URL));
        this.scopes = eventAdapterConfiguration.getStaticProperties().get(HTTPEventAdapterConstants.ADAPTER_CONF_SCOPES);
        if (scopes == null) {
            this.scopes = HTTPEventAdapterConstants.EMPTY_STRING;
        }
    }

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

    public String getAdapterName() {
        return adapterName;
    }

    public boolean isGlobalCredentialSet() {
        return globalCredentialSet;
    }

}
