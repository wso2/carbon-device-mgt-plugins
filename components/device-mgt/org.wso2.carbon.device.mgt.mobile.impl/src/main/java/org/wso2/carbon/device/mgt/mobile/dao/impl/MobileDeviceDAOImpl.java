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
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceDAO;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.util.MobileDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.mobile.dto.MobileDevice;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of MobileDeviceDAO.
 */
public class MobileDeviceDAOImpl implements MobileDeviceDAO {

	private DataSource dataSource;
	private static final Log log = LogFactory.getLog(MobileDeviceDAOImpl.class);

	public MobileDeviceDAOImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public MobileDevice getMobileDevice(String mblDeviceId) throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement stmt = null;
		MobileDevice mobileDevice = null;
		try {
			conn = this.getConnection();
			String selectDBQuery =
					"SELECT MOBILE_DEVICE_ID, PUSH_TOKEN, IMEI, IMSI, OS_VERSION,DEVICE_MODEL, VENDOR, " +
					"LATITUDE, LONGITUDE, CHALLENGE, SERIAL, TOKEN, UNLOCK_TOKEN FROM AD_DEVICE" +
					" WHERE MOBILE_DEVICE_ID = ?";
			stmt = conn.prepareStatement(selectDBQuery);
			stmt.setString(1, mblDeviceId);
			ResultSet resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				mobileDevice = new MobileDevice();
				mobileDevice.setMobileDeviceId(resultSet.getString(1));
				mobileDevice.setPushToken(resultSet.getString(2));
				mobileDevice.setImei(resultSet.getString(3));
				mobileDevice.setImsi(resultSet.getString(4));
				mobileDevice.setOsVersion(resultSet.getString(5));
				mobileDevice.setModel(resultSet.getString(6));
				mobileDevice.setVendor(resultSet.getString(7));
				mobileDevice.setLatitude(resultSet.getString(8));
				mobileDevice.setLongitude(resultSet.getString(9));
				mobileDevice.setChallenge(resultSet.getString(10));
				mobileDevice.setSerial(resultSet.getString(11));
				mobileDevice.setToken(resultSet.getString(12));
				mobileDevice.setUnlockToken(resultSet.getString(13));
				if (log.isDebugEnabled()) {
					log.debug("Mobile device " + mblDeviceId + " data has fetched from MDM database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while fetching mobile device '" +
			             mblDeviceId + "'";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
		}
		return mobileDevice;
	}

	@Override
	public boolean addMobileDevice(MobileDevice mobileDevice)
			throws MobileDeviceManagementDAOException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			String createDBQuery =
					"INSERT INTO AD_DEVICE(MOBILE_DEVICE_ID, PUSH_TOKEN, IMEI, IMSI, OS_VERSION," +
					"DEVICE_MODEL, VENDOR, LATITUDE, LONGITUDE, CHALLENGE, SERIAL, TOKEN, " +
					"UNLOCK_TOKEN) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			stmt = conn.prepareStatement(createDBQuery);
			stmt.setString(1, mobileDevice.getMobileDeviceId());
			stmt.setString(2, mobileDevice.getPushToken());
			stmt.setString(3, mobileDevice.getImei());
			stmt.setString(4, mobileDevice.getImsi());
			stmt.setString(5, mobileDevice.getOsVersion());
			stmt.setString(6, mobileDevice.getModel());
			stmt.setString(7, mobileDevice.getVendor());
			stmt.setString(8, mobileDevice.getLatitude());
			stmt.setString(9, mobileDevice.getLongitude());
			stmt.setString(10, mobileDevice.getChallenge());
			stmt.setString(11, mobileDevice.getSerial());
			stmt.setString(12, mobileDevice.getToken());
			stmt.setString(13, mobileDevice.getUnlockToken());
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Mobile device " + mobileDevice.getMobileDeviceId() + " data has added" +
					          " to the MDM database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while adding the mobile device '" +
			             mobileDevice.getMobileDeviceId() + "' to the mobile db.";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
		}
		return status;
	}

	@Override
	public boolean updateMobileDevice(MobileDevice mobileDevice)
			throws MobileDeviceManagementDAOException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			String updateDBQuery =
					"UPDATE AD_DEVICE SET PUSH_TOKEN = ?, IMEI = ?, IMSI = ?, OS_VERSION = ?," +
					"DEVICE_MODEL = ?, VENDOR = ? , LATITUDE = ?, LONGITUDE = ?, CHALLENGE = ?," +
					"SERIAL = ?, TOKEN = ?, UNLOCK_TOKEN = ? WHERE MOBILE_DEVICE_ID = ?";
			stmt = conn.prepareStatement(updateDBQuery);
			stmt.setString(1, mobileDevice.getPushToken());
			stmt.setString(2, mobileDevice.getImei());
			stmt.setString(3, mobileDevice.getImsi());
			stmt.setString(4, mobileDevice.getOsVersion());
			stmt.setString(5, mobileDevice.getModel());
			stmt.setString(6, mobileDevice.getVendor());
			stmt.setString(7, mobileDevice.getLatitude());
			stmt.setString(8, mobileDevice.getLongitude());
			stmt.setString(9, mobileDevice.getChallenge());
			stmt.setString(10, mobileDevice.getSerial());
			stmt.setString(11, mobileDevice.getToken());
			stmt.setString(12, mobileDevice.getUnlockToken());
			stmt.setString(13, mobileDevice.getMobileDeviceId());
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Mobile device " + mobileDevice.getMobileDeviceId() + " data has" +
					          " updated");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while updating the mobile device '" +
			             mobileDevice.getMobileDeviceId() + "'";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
		}
		return status;
	}

	@Override
	public boolean deleteMobileDevice(String mblDeviceId) throws MobileDeviceManagementDAOException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			String deleteDBQuery =
					"DELETE FROM AD_DEVICE WHERE MOBILE_DEVICE_ID = ?";
			stmt = conn.prepareStatement(deleteDBQuery);
			stmt.setString(1, mblDeviceId);
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Mobile device " + mblDeviceId + " data has deleted" +
					          " from the MDM database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while deleting mobile device " + mblDeviceId;
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
		}
		return status;
	}

	@Override
	public List<MobileDevice> getAllMobileDevices() throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement stmt = null;
		MobileDevice mobileDevice;
		List<MobileDevice> mobileDevices = new ArrayList<MobileDevice>();
		try {
			conn = this.getConnection();
			String selectDBQuery =
					"SELECT MOBILE_DEVICE_ID, PUSH_TOKEN, IMEI, IMSI, OS_VERSION,DEVICE_MODEL, VENDOR," +
					"LATITUDE, LONGITUDE, CHALLENGE, SERIAL, TOKEN, UNLOCK_TOKEN FROM AD_DEVICE";
			stmt = conn.prepareStatement(selectDBQuery);
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				mobileDevice = new MobileDevice();
				mobileDevice.setMobileDeviceId(resultSet.getString(1));
				mobileDevice.setPushToken(resultSet.getString(2));
				mobileDevice.setImei(resultSet.getString(3));
				mobileDevice.setImsi(resultSet.getString(4));
				mobileDevice.setOsVersion(resultSet.getString(5));
				mobileDevice.setModel(resultSet.getString(6));
				mobileDevice.setVendor(resultSet.getString(7));
				mobileDevice.setLatitude(resultSet.getString(8));
				mobileDevice.setLongitude(resultSet.getString(9));
				mobileDevice.setChallenge(resultSet.getString(10));
				mobileDevice.setSerial(resultSet.getString(11));
				mobileDevice.setToken(resultSet.getString(12));
				mobileDevice.setUnlockToken(resultSet.getString(13));
				mobileDevices.add(mobileDevice);
			}
			if (log.isDebugEnabled()) {
				log.debug("All Mobile device details have fetched from MDM database.");
			}
			return mobileDevices;
		} catch (SQLException e) {
			String msg = "Error occurred while fetching all mobile device data'";
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
			String msg = "Error occurred while obtaining a connection from the mobile device " +
			             "management metadata repository datasource";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		}
	}
}