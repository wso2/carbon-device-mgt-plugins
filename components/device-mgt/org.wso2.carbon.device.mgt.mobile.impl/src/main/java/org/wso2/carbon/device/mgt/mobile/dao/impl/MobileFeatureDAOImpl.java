/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.mobile.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileFeatureDAO;
import org.wso2.carbon.device.mgt.mobile.dao.util.MobileDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.mobile.dto.MobileFeature;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of MobileFeatureDAO.
 */
public class MobileFeatureDAOImpl implements MobileFeatureDAO {

	private DataSource dataSource;
	private static final Log log = LogFactory.getLog(MobileFeatureDAOImpl.class);

	public MobileFeatureDAOImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public boolean addFeature(MobileFeature mobileFeature)
			throws MobileDeviceManagementDAOException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			String createDBQuery =
					"INSERT INTO AD_FEATURE(CODE, NAME, DESCRIPTION, DEVICE_TYPE) VALUES (?, ?, ?, ?)";

			stmt = conn.prepareStatement(createDBQuery);
			stmt.setString(1, mobileFeature.getCode());
			stmt.setString(2, mobileFeature.getName());
			stmt.setString(3, mobileFeature.getDescription());
			stmt.setString(4, mobileFeature.getDeviceType());
			int rows = stmt.executeUpdate();
			if (rows > 0) {
                if (log.isDebugEnabled()) {
					log.debug("Added a new MobileFeature " + mobileFeature.getCode() + " to the" +
					          " MDM database.");
				}
                status = true;
			}

		} catch (SQLException e) {
			String msg = "Error occurred while adding feature code - '" +
			             mobileFeature.getCode() + "' to feature table";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
		}
		return status;
	}

	@Override
	public boolean addFeatures(List<MobileFeature> mobileFeatures) throws MobileDeviceManagementDAOException {
		return false;
	}

	@Override
	public boolean updateFeature(MobileFeature mobileFeature)
			throws MobileDeviceManagementDAOException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			String updateDBQuery =
					"UPDATE AD_FEATURE SET CODE = ?, NAME = ?, DESCRIPTION = ?, DEVICE_TYPE = ?" +
					" WHERE FEATURE_ID = ?";
			stmt = conn.prepareStatement(updateDBQuery);
			stmt.setString(1, mobileFeature.getCode());
			stmt.setString(2, mobileFeature.getName());
			stmt.setString(3, mobileFeature.getDescription());
			stmt.setString(4, mobileFeature.getDeviceType());
			stmt.setInt(5, mobileFeature.getId());
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Updated MobileFeature " + mobileFeature.getCode());
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while updating the feature with feature code - '" +
			             mobileFeature.getId() + "'";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
		}
		return status;
	}

	@Override
	public boolean deleteFeatureByCode(String mblFeatureCode)
			throws MobileDeviceManagementDAOException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			String deleteDBQuery =
					"DELETE FROM AD_FEATURE WHERE CODE = ?";
			stmt = conn.prepareStatement(deleteDBQuery);
			stmt.setString(1, mblFeatureCode);
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Deleted MobileFeature code " + mblFeatureCode + " from the" +
					          " MDM database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while deleting feature with code - " + mblFeatureCode;
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
		}
		return status;
	}

	@Override
	public boolean deleteFeatureById(int mblFeatureId)
			throws MobileDeviceManagementDAOException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			String deleteDBQuery =
					"DELETE FROM AD_FEATURE WHERE FEATURE_ID = ?";
			stmt = conn.prepareStatement(deleteDBQuery);
			stmt.setInt(1, mblFeatureId);
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Deleted MobileFeature id " + mblFeatureId + " from the" +
					          " MDM database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while deleting feature with id - " + mblFeatureId;
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
		}
		return status;
	}

	@Override
	public MobileFeature getFeatureByCode(String mblFeatureCode)
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement stmt = null;
		MobileFeature mobileFeature = null;
		try {
			conn = this.getConnection();
			String selectDBQuery =
					"SELECT FEATURE_ID, CODE, NAME, DESCRIPTION, DEVICE_TYPE FROM AD_FEATURE " +
					"WHERE CODE = ?";
			stmt = conn.prepareStatement(selectDBQuery);
			stmt.setString(1, mblFeatureCode);
			ResultSet resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				mobileFeature = new MobileFeature();
				mobileFeature.setId(resultSet.getInt(1));
				mobileFeature.setCode(resultSet.getString(2));
				mobileFeature.setName(resultSet.getString(3));
				mobileFeature.setDescription(resultSet.getString(4));
				mobileFeature.setDeviceType(resultSet.getString(5));
				if (log.isDebugEnabled()) {
					log.debug("Fetched MobileFeature " + mblFeatureCode + " from the" +
					          " MDM database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while fetching feature code - '" +
			             mblFeatureCode + "'";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
		}
		return mobileFeature;
	}

	@Override
	public MobileFeature getFeatureById(int mblFeatureId)
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement stmt = null;
		MobileFeature mobileFeature = null;
		try {
			conn = this.getConnection();
			String selectDBQuery =
					"SELECT FEATURE_ID, CODE, NAME, DESCRIPTION, DEVICE_TYPE FROM AD_FEATURE" +
					" WHERE FEATURE_ID = ?";
			stmt = conn.prepareStatement(selectDBQuery);
			stmt.setInt(1, mblFeatureId);
			ResultSet resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				mobileFeature = new MobileFeature();
				mobileFeature.setId(resultSet.getInt(1));
				mobileFeature.setCode(resultSet.getString(2));
				mobileFeature.setName(resultSet.getString(3));
				mobileFeature.setDescription(resultSet.getString(4));
				mobileFeature.setDeviceType(resultSet.getString(5));
				if (log.isDebugEnabled()) {
					log.debug("Fetched MobileFeatureId" + mblFeatureId + " from the" +
					          " MDM database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while fetching feature id - '" +
			             mblFeatureId + "'";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
		}
		return mobileFeature;
	}

	@Override
	public List<MobileFeature> getAllFeatures() throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement stmt = null;
		MobileFeature mobileFeature;
		List<MobileFeature> mobileFeatures = new ArrayList<MobileFeature>();
		try {
			conn = this.getConnection();
			String selectDBQuery =
					"SELECT FEATURE_ID, CODE, NAME, DESCRIPTION, DEVICE_TYPE FROM AD_FEATURE";
			stmt = conn.prepareStatement(selectDBQuery);
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				mobileFeature = new MobileFeature();
				mobileFeature.setId(resultSet.getInt(1));
				mobileFeature.setCode(resultSet.getString(2));
				mobileFeature.setName(resultSet.getString(3));
				mobileFeature.setDescription(resultSet.getString(4));
				mobileFeature.setDeviceType(resultSet.getString(5));
				mobileFeatures.add(mobileFeature);
			}
			if (log.isDebugEnabled()) {
				log.debug("Fetched all MobileFeatures from the MDM database.");
			}
			return mobileFeatures;
		} catch (SQLException e) {
			String msg = "Error occurred while fetching all features.'";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
		}
	}

	@Override
	public List<MobileFeature> getFeatureByDeviceType(String deviceType) throws
	                                                                           MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement stmt = null;
		MobileFeature mobileFeature;
		List<MobileFeature> mobileFeatures = new ArrayList<MobileFeature>();
		try {
			conn = this.getConnection();
			String selectDBQuery =
					"SELECT FEATURE_ID, CODE, NAME, DESCRIPTION, DEVICE_TYPE FROM AD_FEATURE" +
					" WHERE DEVICE_TYPE = ?";
			stmt = conn.prepareStatement(selectDBQuery);
			stmt.setString(1, deviceType);
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				mobileFeature = new MobileFeature();
				mobileFeature.setId(resultSet.getInt(1));
				mobileFeature.setCode(resultSet.getString(2));
				mobileFeature.setName(resultSet.getString(3));
				mobileFeature.setDescription(resultSet.getString(4));
				mobileFeature.setDeviceType(resultSet.getString(5));
				mobileFeatures.add(mobileFeature);
			}
			if (log.isDebugEnabled()) {
				log.debug("Fetched all MobileFeatures of type " + deviceType + " from the MDM" +
				          " database.");
			}
			return mobileFeatures;
		} catch (SQLException e) {
			String msg = "Error occurred while fetching all features.'";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
		}
	}

	private Connection getConnection() throws MobileDeviceManagementDAOException {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			String msg = "Error occurred while obtaining a connection from the mobile specific " +
			             "datasource.";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		}
	}
}
