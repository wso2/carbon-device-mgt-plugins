/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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
package org.wso2.carbon.device.mgt.mobile.windows.impl.dao.impl;

import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dao.WindowsDAOFactory;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dao.WindowsEnrollmentTokenDAO;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dao.util.MobileDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dto.MobileCacheEntry;
import org.wso2.carbon.device.mgt.mobile.windows.impl.util.WindowsPluginConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WindowsEnrollmentTokenDAOImpl implements WindowsEnrollmentTokenDAO {

    public WindowsEnrollmentTokenDAOImpl() {
    }

    @Override
    public MobileCacheEntry getCacheToken(String token) throws MobileDeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        MobileCacheEntry cacheEntry = null;
        try {
            conn = WindowsDAOFactory.getConnection();
            String selectDBQuery = "SELECT TENANT_DOMAIN, TENANT_ID, ENROLLMENT_TOKEN, DEVICE_ID, USERNAME, OWNERSHIP " +
                            "FROM WINDOWS_ENROLLMENT_TOKEN WHERE ENROLLMENT_TOKEN = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, token);
            rs = stmt.executeQuery();

            while (rs.next()) {
                cacheEntry = new MobileCacheEntry();
                cacheEntry.setDeviceID(rs.getString(WindowsPluginConstants.DEVICE_ID));
                cacheEntry.setTenantDomain(rs.getString(WindowsPluginConstants.TENANT_DOMAIN));
                cacheEntry.setTenanatID(rs.getInt(WindowsPluginConstants.TENANT_ID));
                cacheEntry.setUsername(rs.getString(WindowsPluginConstants.USER_NAME));
                cacheEntry.setOwnership(rs.getString(WindowsPluginConstants.OWNERSHIP));
            }
        } catch (SQLException e) {
            throw new MobileDeviceManagementDAOException("Error occurred while fetching the Windows device token for the enrollment token '" +
                    token + "' from the Windows db.", e);
        }
        return cacheEntry;
    }

    @Override public MobileCacheEntry getCacheTokenFromDeviceId(String deviceId)
            throws MobileDeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        MobileCacheEntry cacheEntry = null;
        try {
            conn = WindowsDAOFactory.getConnection();
            String selectDBQuery = "SELECT TENANT_DOMAIN, TENANT_ID, ENROLLMENT_TOKEN, DEVICE_ID, USERNAME, OWNERSHIP " +
                    "FROM WINDOWS_ENROLLMENT_TOKEN WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, deviceId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                cacheEntry = new MobileCacheEntry();
                cacheEntry.setDeviceID(rs.getString(WindowsPluginConstants.DEVICE_ID));
                cacheEntry.setTenantDomain(rs.getString(WindowsPluginConstants.TENANT_DOMAIN));
                cacheEntry.setTenanatID(rs.getInt(WindowsPluginConstants.TENANT_ID));
                cacheEntry.setUsername(rs.getString(WindowsPluginConstants.USER_NAME));
                cacheEntry.setOwnership(rs.getString(WindowsPluginConstants.OWNERSHIP));
            }
        } catch (SQLException e) {
            throw new MobileDeviceManagementDAOException("Error occurred while fetching the Windows device token for the enrollment token '" +
                    deviceId + "' from the Windows db.", e);
        }
        return cacheEntry;
    }

    @Override
    public boolean addCacheToken(MobileCacheEntry cacheEntry) throws MobileDeviceManagementDAOException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = WindowsDAOFactory.getConnection();
            String sql = "INSERT INTO WINDOWS_ENROLLMENT_TOKEN(TENANT_DOMAIN, TENANT_ID, ENROLLMENT_TOKEN, DEVICE_ID, "
                    + "USERNAME, OWNERSHIP) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, cacheEntry.getTenantDomain());
            stmt.setInt(2, cacheEntry.getTenanatID());
            stmt.setString(3, cacheEntry.getCacheToken());
            stmt.setString(4, cacheEntry.getDeviceID());
            stmt.setString(5, cacheEntry.getUsername());
            stmt.setString(6, cacheEntry.getOwnership());
            int rows = stmt.executeUpdate();
            if(rows > 0) {
                status = true;
            }
        } catch (SQLException e) {
            throw new MobileDeviceManagementDAOException("Error occurred while adding the Windows device enrollment token for'" +
                    cacheEntry.getDeviceID() + "' to the Windows db.", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }

    @Override
    public boolean updateCacheToken(MobileCacheEntry cacheEntry) throws MobileDeviceManagementDAOException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = WindowsDAOFactory.getConnection();
            String updateDBQuery =
                    "UPDATE WINDOWS_ENROLLMENT_TOKEN SET TENANT_DOMAIN = ?, TENANT_ID = ?, ENROLLMENT_TOKEN = ?, USERNAME = ?, " +
                            "DEVICE_ID = ? WHERE ENROLLMENT_TOKEN = ?";
            stmt = conn.prepareStatement(updateDBQuery);
            stmt.setString(1, cacheEntry.getTenantDomain());
            stmt.setInt(2, cacheEntry.getTenanatID());
            stmt.setString(3, cacheEntry.getCacheToken());
            stmt.setString(4, cacheEntry.getUsername());
            stmt.setString(5, cacheEntry.getDeviceID());
            stmt.setString(6, cacheEntry.getCacheToken());
            int rows = stmt.executeUpdate();
            if(rows > 0) {
                status = true;
            }
        } catch (SQLException e) {
            throw new MobileDeviceManagementDAOException("Error occurred while updating the Windows device enrollment token for'" +
                    cacheEntry.getDeviceID() + "' to the Windows db.", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }

    @Override
    public boolean deleteCacheToken(String mobileDeviceId) throws MobileDeviceManagementDAOException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = WindowsDAOFactory.getConnection();
            String deleteDBQuery = "DELETE FROM WINDOWS_ENROLLMENT_TOKEN WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, mobileDeviceId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
            }
        } catch (SQLException e) {
            throw new MobileDeviceManagementDAOException("Error occurred while deleting windows device '" +
                    mobileDeviceId + "'", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }
}
