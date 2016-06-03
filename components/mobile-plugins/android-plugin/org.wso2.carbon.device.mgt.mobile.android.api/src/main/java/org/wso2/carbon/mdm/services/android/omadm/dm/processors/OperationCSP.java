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

package org.wso2.carbon.mdm.services.android.omadm.dm.processors;

/**
 * Contains operation Configuration Service Providers. This class has been
 * used to map MDM operations supported by the server to OMADM specific CSPs.
 */
public class OperationCSP {

    public static enum Info {
        DEV_ID("./DevInfo/DevId"),
        MANUFACTURER("./DevInfo/Man"),
        DEVICE_MODEL("./DevInfo/Mod"),
        DM_VERSION("./DevInfo/DmV"),
        LANGUAGE("./DevInfo/Lang"),
        IMSI("./Vendor/MSFT/DeviceInstanceService/Identity/Identity1/IMSI"),
        IMEI("./Vendor/MSFT/DeviceInstanceService/Identity/Identity1/IMEI"),
        SOFTWARE_VERSION("./DevDetail/SwV"),
        VENDOR("./DevDetail/OEM"),
        MAC_ADDRESS("./DevDetail/Ext/WLANMACAddress"),
        RESOLUTION("./DevDetail/Ext/Microsoft/Resolution"),
        DEVICE_NAME("./DevDetail/Ext/Microsoft/DeviceName"),

        // Operation CSPs below are vendor specific
        CHANNEL_URI(""),
        LOCK_PIN(""),
        LOCK_RESET(""),
        CAMERA(""),
        CAMERA_STATUS(""),
        ENCRYPT_STORAGE_STATUS(""),
        DEVICE_PASSWORD_STATUS(""),
        DEVICE_PASSCODE_DELETE(""),
        LONGITUDE(""),
        LATITUDE("");

        private final String code;

        Info(String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }
    }


}
