/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.mobile.impl.windows.util;

import org.wso2.carbon.device.mgt.mobile.dto.MobileDevice;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Contains utility methods which are used by Windows plugin.
 */
public class WindowsUtils {

    public static String getDeviceProperty(Map<String, String> deviceProperties, String property) {

        String deviceProperty = deviceProperties.get(property);
        if (deviceProperty == null) {
            return null;
        }
        return deviceProperty;
    }

    public static MobileDevice loadMobileDevices(ResultSet rs) throws SQLException {

        MobileDevice mobileDevice = new MobileDevice();
        mobileDevice.setMobileDeviceId(rs.getString(WindowsPluginConstants.DEVICE_ID));
        mobileDevice.setImei(rs.getString(WindowsPluginConstants.IMEI));
        mobileDevice.setImsi(rs.getString(WindowsPluginConstants.IMSI));
        mobileDevice.setModel(rs.getString(WindowsPluginConstants.DEVICE_MODEL));
        mobileDevice.setVendor(rs.getString(WindowsPluginConstants.VENDOR));
        mobileDevice.setLatitude(rs.getString(WindowsPluginConstants.LATITUDE));
        mobileDevice.setLongitude(rs.getString(WindowsPluginConstants.LONGITUDE));
        mobileDevice.setSerial(rs.getString(WindowsPluginConstants.SERIAL));
        mobileDevice.setOsVersion(rs.getString(WindowsPluginConstants.LATITUDE));
        return mobileDevice;
    }

    public static MobileDevice loadMatchingMobileDevices(ResultSet rs) throws SQLException {
        MobileDevice mobileDevice = new MobileDevice();
        mobileDevice.setMobileDeviceId(rs.getString(WindowsPluginConstants.DEVICE_ID));
        mobileDevice.setVendor(rs.getString(WindowsPluginConstants.IMEI));
        mobileDevice.setLatitude(rs.getString(WindowsPluginConstants.IMSI));
        mobileDevice.setLongitude(rs.getString(WindowsPluginConstants.OS_VERSION));
        mobileDevice.setImei(rs.getString(WindowsPluginConstants.DEVICE_MODEL));
        mobileDevice.setImsi(rs.getString(WindowsPluginConstants.VENDOR));
        mobileDevice.setOsVersion(rs.getString(WindowsPluginConstants.LATITUDE));
        return mobileDevice;
    }
}
