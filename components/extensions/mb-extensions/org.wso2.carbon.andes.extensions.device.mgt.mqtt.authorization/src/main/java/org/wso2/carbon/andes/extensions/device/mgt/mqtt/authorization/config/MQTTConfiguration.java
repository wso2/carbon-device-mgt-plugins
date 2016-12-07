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

package org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.config;

import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.util.ImmutableMetaProperties;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.util.MetaProperties;

import java.util.List;

public enum MQTTConfiguration {

    /**
     * List of properties that can define how the server will authenticate the user with the authentication service.
     */
    LIST_TRANSPORT_MQTT_AUTHORIZATION_PROPERTIES("transports/mqtt/security/authorizer/property/@name", "", List.class),

    /**
     * This can be used to access a property by giving its key. e.g. hosturl
     */
    TRANSPORT_MQTT_AUTHORIZATION_PROPERTIES("transports/mqtt/security/authorizer/property[@name = '{key}']", ""
            , String.class);

    /**
     * Meta data about configuration.
     */
    private final MetaProperties metaProperties;

    /**
     * Constructor to define a configuration in broker.
     *
     * @param keyInFile    Xpath (or any key value) which can be used to identify the configuration in the file.
     * @param defaultValue the default value
     * @param dataType     data type of the config ( e.g. boolean, string )
     */
    MQTTConfiguration(String keyInFile, String defaultValue, Class<?> dataType) {
        // We need to pass the enum name as the identifier : therefore this.name()
        this.metaProperties = new ImmutableMetaProperties(this.name(), keyInFile, defaultValue, dataType);
    }

    public MetaProperties get() {
        return metaProperties;
    }


}

