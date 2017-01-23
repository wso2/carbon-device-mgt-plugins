/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.arduino.service.impl.constants;

public class ArduinoConstants {
    public final static String DEVICE_TYPE = "arduino";

    public static final String URL_PREFIX = "http://";
    //sensor events summerized table name
    public static final String TEMPERATURE_EVENT_TABLE = "DEVICE_TEMPERATURE_SUMMARY";

    public static final String APIM_APPLICATION_TOKEN_VALIDITY_PERIOD = "3600";

    public static final String SCOPE = "scope";

    public static final String PERM_ENROLL_ARDUINO = "/permission/admin/device-mgt/devices/enroll/arduino";
    public static final String PERM_OWNING_DEVICE_VIEW = "/permission/admin/device-mgt/devices/owning-device/view";

    public static final String ROLE_NAME = "internal/devicemgt-user";

}
