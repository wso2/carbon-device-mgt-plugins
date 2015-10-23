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

package org.wso2.carbon.device.mgt.mobile.impl.windows.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileFeatureDAO;
import org.wso2.carbon.device.mgt.mobile.dao.util.MobileDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.mobile.dto.MobileFeature;
import org.wso2.carbon.device.mgt.mobile.impl.windows.dao.WindowsDAOFactory;
import org.wso2.carbon.device.mgt.mobile.impl.windows.dao.WindowsFeatureManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.impl.windows.util.WindowsPluginConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implement MobileFeatureDAO for Windows devices.
 */
public class WindowsFeatureDAOImpl implements MobileFeatureDAO {

    private static final Log log = LogFactory.getLog(WindowsFeatureDAOImpl.class);

    public WindowsFeatureDAOImpl() {

    }
    @Override
    public boolean addFeature(MobileFeature mobileFeature) throws MobileDeviceManagementDAOException {
        PreparedStatement stmt = null;
        boolean status;
        Connection conn;
        try {
            conn = WindowsDAOFactory.getConnection();
            String sql = "INSERT INTO WINDOWS_FEATURE(CODE, NAME, DESCRIPTION) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, mobileFeature.getCode());
            stmt.setString(2, mobileFeature.getName());
            stmt.setString(3, mobileFeature.getDescription());
            stmt.executeUpdate();
            status = true;
            status = true;
        } catch (SQLException e) {
            throw new WindowsFeatureManagementDAOException(
                    "Error occurred while adding windows feature '" +
                            mobileFeature.getName() + "' into the metadata repository", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }

    @Override
    public boolean updateFeature(MobileFeature mobileFeature) throws MobileDeviceManagementDAOException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = WindowsDAOFactory.getConnection();
            String updateDBQuery =
                    "UPDATE WINDOWS_FEATURE SET NAME = ?, DESCRIPTION = ?" +
                            "WHERE CODE = ?";

            stmt = conn.prepareStatement(updateDBQuery);
            stmt.setString(1, mobileFeature.getName());
            stmt.setString(2, mobileFeature.getDescription());
            stmt.setString(3, mobileFeature.getCode());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("Windows Feature " + mobileFeature.getCode() + " data has been " +
                            "modified.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating the Windows Feature '" +
                    mobileFeature.getCode() + "' to the Android db.";
            log.error(msg, e);
            throw new WindowsFeatureManagementDAOException(msg, e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }

    @Override
    public boolean deleteFeatureById(int mblFeatureId) throws MobileDeviceManagementDAOException {
        PreparedStatement stmt = null;
        boolean status = false;
        Connection conn = null;
        try {
            conn = WindowsDAOFactory.getConnection();
            String sql = "DELETE FROM WINDOWS_FEATURE WHERE FEATURE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mblFeatureId);
            stmt.execute();
            status = true;
        } catch (SQLException e) {
            throw new WindowsFeatureManagementDAOException(
                    "Error occurred while deleting windows feature '" +
                            mblFeatureId + "' from Windows database.", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }

    @Override
    public boolean deleteFeatureByCode(String mblFeatureCode) throws MobileDeviceManagementDAOException {
        PreparedStatement stmt = null;
        boolean status = false;
        Connection conn = null;
        try {
            conn = WindowsDAOFactory.getConnection();
            String sql = "DELETE FROM WINDOWS_FEATURE WHERE CODE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, mblFeatureCode);
            stmt.execute();
            status = true;
        } catch (SQLException e) {
            throw new WindowsFeatureManagementDAOException(
                    "Error occurred while deleting windows feature '" +
                            mblFeatureCode + "' from Windows database.", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }

    @Override
    public MobileFeature getFeatureById(int mblFeatureId) throws MobileDeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = WindowsDAOFactory.getConnection();
            String sql = "SELECT FEATURE_ID, CODE, NAME, DESCRIPTION FROM WINDOWS_FEATURE WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, mblFeatureId);
            rs = stmt.executeQuery();

            MobileFeature mobileFeature = null;
            if (rs.next()) {
                mobileFeature = new MobileFeature();
                mobileFeature.setId(rs.getInt(WindowsPluginConstants.WINDOWS_FEATURE_ID));
                mobileFeature.setCode(rs.getString(WindowsPluginConstants.WINDOWS_FEATURE_CODE));
                mobileFeature.setName(rs.getString(WindowsPluginConstants.WINDOWS_FEATURE_NAME));
                mobileFeature.setDescription(rs.getString(WindowsPluginConstants.WINDOWS_FEATURE_DESCRIPTION));
                mobileFeature.setDeviceType(
                        DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
            }
            return mobileFeature;
        } catch (SQLException e) {
            throw new WindowsFeatureManagementDAOException(
                    "Error occurred while retrieving windows feature '" +
                            mblFeatureId + "' from the Windows database.", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, rs);
            WindowsDAOFactory.closeConnection();
        }
    }

    @Override
    public MobileFeature getFeatureByCode(String mblFeatureCode) throws MobileDeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn;

        try {
            conn = WindowsDAOFactory.getConnection();
            String sql = "SELECT FEATURE_ID, CODE, NAME, DESCRIPTION FROM WINDOWS_FEATURE WHERE CODE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, mblFeatureCode);
            rs = stmt.executeQuery();

            MobileFeature mobileFeature = null;
            if (rs.next()) {
                mobileFeature = new MobileFeature();
                mobileFeature.setId(rs.getInt(WindowsPluginConstants.WINDOWS_FEATURE_ID));
                mobileFeature.setCode(rs.getString(WindowsPluginConstants.WINDOWS_FEATURE_CODE));
                mobileFeature.setName(rs.getString(WindowsPluginConstants.WINDOWS_FEATURE_NAME));
                mobileFeature.setDescription(rs.getString(WindowsPluginConstants.WINDOWS_FEATURE_DESCRIPTION));
                mobileFeature.setDeviceType(
                        DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
            }
            return mobileFeature;
        } catch (SQLException e) {
            throw new WindowsFeatureManagementDAOException(
                    "Error occurred while retrieving windows feature '" +
                            mblFeatureCode + "' from the Windows database.", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, rs);
            WindowsDAOFactory.closeConnection();
        }
    }

    @Override
    public List<MobileFeature> getFeatureByDeviceType(String deviceType) throws MobileDeviceManagementDAOException {
        return this.getAllFeatures();
    }

    @Override
    public List<MobileFeature> getAllFeatures() throws MobileDeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn;
        List<MobileFeature> features = new ArrayList<>();

        try {
            conn = WindowsDAOFactory.getConnection();
            String sql = "SELECT FEATURE_ID, CODE, NAME, DESCRIPTION FROM WINDOWS_FEATURE";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            MobileFeature mobileFeature;

            while (rs.next()) {
                mobileFeature = new MobileFeature();
                mobileFeature.setId(rs.getInt(WindowsPluginConstants.WINDOWS_FEATURE_ID));
                mobileFeature.setCode(rs.getString(WindowsPluginConstants.WINDOWS_FEATURE_CODE));
                mobileFeature.setName(rs.getString(WindowsPluginConstants.WINDOWS_FEATURE_NAME));
                mobileFeature.setDescription(rs.getString(WindowsPluginConstants.WINDOWS_FEATURE_DESCRIPTION));
                mobileFeature.setDeviceType(
                        DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
                features.add(mobileFeature);
            }
            return features;
        } catch (SQLException e) {
            throw new WindowsFeatureManagementDAOException("Error occurred while retrieving all " +
                    "windows features from the Windows database.", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, rs);
            WindowsDAOFactory.closeConnection();
        }
    }
}
