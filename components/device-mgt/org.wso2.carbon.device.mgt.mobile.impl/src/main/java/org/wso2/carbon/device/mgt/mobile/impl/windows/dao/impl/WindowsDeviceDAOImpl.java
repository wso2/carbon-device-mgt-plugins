
/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.mobile.impl.windows.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceDAO;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.util.MobileDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.mobile.dto.MobileDevice;
import org.wso2.carbon.device.mgt.mobile.impl.windows.dao.WindowsDAOFactory;
import org.wso2.carbon.device.mgt.mobile.impl.windows.util.WindowsPluginConstants;
import org.wso2.carbon.device.mgt.mobile.impl.windows.util.WindowsUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * Implements MobileDeviceDAO for Windows Devices.
 */
public class WindowsDeviceDAOImpl implements MobileDeviceDAO {

    private static final Log log = LogFactory.getLog(WindowsDeviceDAOImpl.class);

    @Override
    public MobileDevice getMobileDevice(String mblDeviceId) throws MobileDeviceManagementDAOException {
        return null;
    }

    @Override
    public boolean addMobileDevice(MobileDevice mobileDevice) throws MobileDeviceManagementDAOException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = WindowsDAOFactory.getConnection();
            String createDBQuery =
                    "INSERT INTO WINDOWS_DEVICE(MOBILE_DEVICE_ID, CHANNEL_URI, DEVICE_INFO,  IMEI, " +
                            "IMSI, OS_VERSION,  DEVICE_MODEL,  VENDOR, LATITUDE,  LONGITUDE, SERIAL, " +
                            "MAC_ADDRESS,  DEVICE_NAME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(createDBQuery);
            stmt.setString(1, mobileDevice.getMobileDeviceId());

            if (mobileDevice.getDeviceProperties() == null) {
                mobileDevice.setDeviceProperties(new HashMap<String, String>());
            }

            stmt.setString(2, WindowsUtils.getDeviceProperty(mobileDevice.getDeviceProperties(),
                    WindowsPluginConstants.CHANNEL_URI));
            stmt.setString(3, WindowsUtils.getDeviceProperty(mobileDevice.getDeviceProperties(),
                    WindowsPluginConstants.DEVICE_INFO));
            stmt.setString(4, mobileDevice.getImei());
            stmt.setString(5, mobileDevice.getImsi());
            stmt.setString(6, mobileDevice.getOsVersion());
            stmt.setString(7, mobileDevice.getModel());
            stmt.setString(8, mobileDevice.getVendor());
            stmt.setString(9, mobileDevice.getLatitude());
            stmt.setString(10, mobileDevice.getLongitude());
            stmt.setString(11, mobileDevice.getSerial());
            stmt.setString(12, WindowsUtils.getDeviceProperty(mobileDevice.getDeviceProperties(),
                    WindowsPluginConstants.MAC_ADDRESS));
            stmt.setString(13, WindowsUtils.getDeviceProperty(mobileDevice.getDeviceProperties(),
                    WindowsPluginConstants.DEVICE_NAME));
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("Windows device " + mobileDevice.getMobileDeviceId() + " data has been" +
                            " added to the Windows database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding the Windows device '" +
                    mobileDevice.getMobileDeviceId() + "' to the Windows db.";
            log.error(msg, e);
            throw new MobileDeviceManagementDAOException(msg, e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;

    }

    @Override
    public boolean updateMobileDevice(MobileDevice mobileDevice) throws MobileDeviceManagementDAOException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = WindowsDAOFactory.getConnection();
            String updateDBQuery =
                    "UPDATE WINDOWS_DEVICE SET CHANNEL_URI = ?, DEVICE_INFO = ?, IMEI = ?, IMSI = ?, " +
                            "OS_VERSION = ?, DEVICE_MODEL = ?, VENDOR = ?, LATITUDE = ?, LONGITUDE = ?, " +
                            "SERIAL = ?, MAC_ADDRESS = ?, DEVICE_NAME = ? WHERE MOBILE_DEVICE_ID = ?";

            stmt = conn.prepareStatement(updateDBQuery);

            if (mobileDevice.getDeviceProperties() == null) {
                mobileDevice.setDeviceProperties(new HashMap<String, String>());
            }

            stmt.setString(1, WindowsUtils.getDeviceProperty(
                    mobileDevice.getDeviceProperties(),
                    WindowsPluginConstants.CHANNEL_URI));
            stmt.setString(2, WindowsUtils.getDeviceProperty(mobileDevice.getDeviceProperties(),
                    WindowsPluginConstants.DEVICE_INFO));
            stmt.setString(3, mobileDevice.getImei());
            stmt.setString(4, mobileDevice.getImsi());
            stmt.setString(5, mobileDevice.getOsVersion() );
            stmt.setString(6, mobileDevice.getModel());
            stmt.setString(7, mobileDevice.getVendor());
            stmt.setString(8, mobileDevice.getLatitude());
            stmt.setString(9, mobileDevice.getLongitude());
            stmt.setString(10, mobileDevice.getSerial());
            stmt.setString(11, WindowsUtils.getDeviceProperty(mobileDevice.getDeviceProperties(),
                    WindowsPluginConstants.MAC_ADDRESS));
            stmt.setString(12, WindowsUtils.getDeviceProperty(mobileDevice.getDeviceProperties(),
                    WindowsPluginConstants.DEVICE_NAME));
            stmt.setString(13, mobileDevice.getMobileDeviceId());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("Windows device " + mobileDevice.getMobileDeviceId() + " data has been" +
                            " modified.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while modifying the Windows device '" +
                    mobileDevice.getMobileDeviceId() + "' data.";
            log.error(msg, e);
            throw new MobileDeviceManagementDAOException(msg, e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }

    @Override
    public boolean deleteMobileDevice(String mblDeviceId) throws MobileDeviceManagementDAOException {
        return false;
    }

    @Override
    public List<MobileDevice> getAllMobileDevices() throws MobileDeviceManagementDAOException {
        return null;
    }
}
