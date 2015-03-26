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

import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.mobile.dao.util.MobileDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.mobile.impl.ios.dao.FeatureDAO;
import org.wso2.carbon.device.mgt.mobile.impl.ios.dao.FeatureManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.impl.ios.dao.FeatureManagementDAOFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FeatureDAOImpl implements FeatureDAO {

    @Override
    public void addFeature(Feature feature) throws FeatureManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = FeatureManagementDAOFactory.getConnection();
            String sql = "INSERT INTO IOS_FEATURE(CODE, NAME, DESCRIPTION) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, feature.getCode());
            stmt.setString(2, feature.getName());
            stmt.setString(3, feature.getDescription());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new FeatureManagementDAOException("Error occurred while adding feature '" +
                    feature.getName() + "' into the metadata repository", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void removeFeature(String code) throws FeatureManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = FeatureManagementDAOFactory.getConnection();
            String sql = "DELETE FROM IOS_FEATURE WHERE CODE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);
            stmt.execute();
        } catch (SQLException e) {
            throw new FeatureManagementDAOException("Error occurred while adding feature '" +
                    code + "' into the metadata repository", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public Feature getFeature(String code) throws FeatureManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = FeatureManagementDAOFactory.getConnection();
            String sql = "SELECT ID, CODE, NAME, DESCRIPTION FROM IOS_FEATURE WHERE CODE = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);
            rs = stmt.executeQuery();

            Feature feature = null;
            if (rs.next()) {
                feature = new Feature();
                feature.setId(rs.getInt("ID"));
                feature.setCode(rs.getString("CODE"));
                feature.setName(rs.getString("NAME"));
                feature.setDescription(rs.getString("DESCRIPTION"));
            }
            return feature;
        } catch (SQLException e) {
            throw new FeatureManagementDAOException("Error occurred while retrieving feature metadata '" +
                    code + "' from the feature metadata repository", e);
        } finally {
            MobileDeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<Feature> getFeatures() throws FeatureManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Feature> features = new ArrayList<Feature>();
        try {
            Connection conn = FeatureManagementDAOFactory.getConnection();
            String sql = "SELECT ID, CODE, NAME, DESCRIPTION FROM IOS_FEATURE";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Feature feature = new Feature();
                feature.setId(rs.getInt("ID"));
                feature.setCode(rs.getString("CODE"));
                feature.setName(rs.getString("NAME"));
                feature.setDescription(rs.getString("DESCRIPTION"));
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
