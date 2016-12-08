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

package org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.util;

/**
 * Contains methods that should be implemented to access meta properties of a config
 * property.
 */
public interface MetaProperties {

    /**
     * @return actual key with which the property is set in relevant config file.
     */
    String getKeyInFile();

    /**
     * @return Default value specified for the config property,
     * in case it is not set in file.
     */
    String getDefaultValue();

    /**
     * @return Datatype of the property. (There could be numeric,date or boolean values.)
     */
    Class<?> getDataType();

    /**
     * @return Name of the property (e.g. TRANSPORTS_AMQP_DEFAULT_CONNECTION_PORT)
     */
    String getName();

}
