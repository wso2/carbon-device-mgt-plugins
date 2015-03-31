/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.mobile.impl.android.dao.impl;

import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dao.MobileFeatureDAO;
import org.wso2.carbon.device.mgt.mobile.dao.util.MobileDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.mobile.dto.MobileFeature;
import org.wso2.carbon.device.mgt.mobile.impl.android.dao.FeatureManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.util.MobileDeviceManagementUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FeatureDAOImpl implements MobileFeatureDAO {

    @Override
    public boolean addFeature(MobileFeature mobileFeature) throws MobileDeviceManagementDAOException {
        PreparedStatement stmt = null;
        boolean status = false;
        try {
            Connection conn = MobileDeviceManagementDAOFactory.getConnection();
            String sql = "INSERT INTO AD_FEATURE(CODE, NAME, DESCRIPTION) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, mobileFeature.getCode());
            stmt.setString(2, mobileFeature.getName());
            stmt.setString(3, mobileFeature.getDescription());
            stmt.executeUpdate();
            status = true;
            status = true;
        } catch (SQLException e) {
            throw new org.wso2.carbon.device.mgt.mobile.impl.ios.dao.FeatureManagementDAOException(
                    "Error occurred while adding feature '" +
                            mobileFeature.getName() + "' into the metadata repository", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }

    @Override
    public boolean updateFeature(MobileFeature mobileFeature) throws MobileDeviceManagementDAOException {
        return false;
    }

    @Override
    public boolean deleteFeatureById(int mblFeatureId) throws MobileDeviceManagementDAOException {
        return false;
    }

    @Override
    public boolean deleteFeatureByCode(String mblFeatureCode) throws MobileDeviceManagementDAOException {
        PreparedStatement stmt = null;
        boolean status = false;
        try {
            Connection conn = MobileDeviceManagementDAOFactory.getConnection();
            String sql = "DELETE FROM AD_FEATURE WHERE CODE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, mblFeatureCode);
            stmt.execute();
            status = true;
        } catch (SQLException e) {
            throw new org.wso2.carbon.device.mgt.mobile.impl.ios.dao.FeatureManagementDAOException(
                    "Error occurred while adding feature '" +
                            mblFeatureCode + "' into the metadata repository", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }

    @Override
    public MobileFeature getFeatureById(int mblFeatureId) throws MobileDeviceManagementDAOException {
        return null;
    }

    @Override
    public MobileFeature getFeatureByCode(String mblFeatureCode) throws MobileDeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = MobileDeviceManagementDAOFactory.getConnection();
            String sql = "SELECT ID, CODE, NAME, DESCRIPTION FROM AD_FEATURE WHERE CODE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, mblFeatureCode);
            rs = stmt.executeQuery();

            Feature feature = null;
            if (rs.next()) {
                feature = new Feature();
                feature.setId(rs.getInt("ID"));
                feature.setCode(rs.getString("CODE"));
                feature.setName(rs.getString("NAME"));
                feature.setDescription(rs.getString("DESCRIPTION"));
            }
            MobileFeature mobileFeature = MobileDeviceManagementUtil.convertToMobileFeature(feature);
            return mobileFeature;
        } catch (SQLException e) {
            throw new org.wso2.carbon.device.mgt.mobile.impl.ios.dao.FeatureManagementDAOException(
                    "Error occurred while retrieving feature metadata '" +
                            mblFeatureCode + "' from the feature metadata repository", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<MobileFeature> getFeatureByDeviceType(String deviceType)
            throws MobileDeviceManagementDAOException {
        return null;
    }

    @Override
    public List<MobileFeature> getAllFeatures() throws MobileDeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<MobileFeature> features = new ArrayList<MobileFeature>();

        try {
            Connection conn = MobileDeviceManagementDAOFactory.getConnection();
            String sql = "SELECT ID, CODE, NAME, DESCRIPTION FROM AD_FEATURE";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            MobileFeature mobileFeature;

            while (rs.next()) {
                Feature feature = new Feature();
                feature.setId(rs.getInt("ID"));
                feature.setCode(rs.getString("CODE"));
                feature.setName(rs.getString("NAME"));
                feature.setDescription(rs.getString("DESCRIPTION"));
                mobileFeature = MobileDeviceManagementUtil.convertToMobileFeature(feature);
                features.add(mobileFeature);
            }
            return features;
        } catch (SQLException e) {
            throw new FeatureManagementDAOException("Error occurred while retrieving all feature metadata from the " +
                    "feature metadata repository", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }
}
