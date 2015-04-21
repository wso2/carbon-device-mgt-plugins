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
package org.wso2.carbon.device.mgt.mobile.impl.ios.dao.impl;

import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dao.MobileFeatureDAO;
import org.wso2.carbon.device.mgt.mobile.dao.util.MobileDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.mobile.dto.MobileFeature;
import org.wso2.carbon.device.mgt.mobile.impl.ios.dao.FeatureManagementDAOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FeatureDAOImpl implements MobileFeatureDAO {


    public boolean addFeature(MobileFeature feature) throws MobileDeviceManagementDAOException {
        PreparedStatement stmt = null;
        boolean status = false;
        try {
            Connection conn = MobileDeviceManagementDAOFactory.getConnection();
            String sql = "INSERT INTO IOS_FEATURE(CODE, NAME, DESCRIPTION) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, feature.getCode());
            stmt.setString(2, feature.getName());
            stmt.setString(3, feature.getDescription());
            stmt.executeUpdate();
            status = true;
        } catch (SQLException e) {
            throw new FeatureManagementDAOException("Error occurred while adding feature '" +
                    feature.getName() + "' into the metadata repository", e);
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
            String sql = "DELETE FROM IOS_FEATURE WHERE CODE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, mblFeatureCode);
            stmt.execute();
            status = true;
        } catch (SQLException e) {
            throw new FeatureManagementDAOException("Error occurred while adding feature '" +
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
            String sql = "SELECT FEATURE_ID, CODE, NAME, DESCRIPTION FROM IOS_FEATURE WHERE CODE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, mblFeatureCode);
            rs = stmt.executeQuery();

            MobileFeature feature = null;
            if (rs.next()) {
                feature = new MobileFeature();
                feature.setId(rs.getInt("ID"));
                feature.setCode(rs.getString("CODE"));
                feature.setName(rs.getString("NAME"));
                feature.setDescription(rs.getString("DESCRIPTION"));
            }
            return feature;
        } catch (SQLException e) {
            throw new FeatureManagementDAOException("Error occurred while retrieving feature metadata '" +
                    mblFeatureCode + "' from the feature metadata repository", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<MobileFeature> getFeatureByDeviceType(String deviceType) throws MobileDeviceManagementDAOException {
        return null;
    }

    @Override
    public List<MobileFeature> getAllFeatures() throws MobileDeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<MobileFeature> features = new ArrayList<MobileFeature>();
        try {
            Connection conn = MobileDeviceManagementDAOFactory.getConnection();
            String sql = "SELECT FEATURE_ID, CODE, NAME, DESCRIPTION FROM IOS_FEATURE";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                MobileFeature feature = new MobileFeature();
                feature.setId(rs.getInt("FEATURE_ID"));
                feature.setCode(rs.getString("CODE"));
                feature.setName(rs.getString("NAME"));
                feature.setDescription(rs.getString("DESCRIPTION"));
                features.add(feature);
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
