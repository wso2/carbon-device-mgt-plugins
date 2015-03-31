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

package org.wso2.carbon.device.mgt.mobile.impl.ios.dao.impl;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IOSDeviceDAOImpl implements MobileDeviceDAO {

	private DataSource dataSource;
	private static final Log log = LogFactory.getLog(IOSDeviceDAOImpl.class);

	public IOSDeviceDAOImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

    public static final String SERIAL = "SERIAL";
    public static final String PRODUCT = "PRODUCT";
    public static final String MAC_ADDRESS = "MAC_ADDRESS";
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String ICCID = "ICCID";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";

	@Override
	public MobileDevice getMobileDevice(String deviceID) throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement stmt = null;
		MobileDevice mobileDevice = null;
		try {
			conn = this.getConnection();
			String selectDBQuery =
					"SELECT MOBILE_DEVICE_ID, APNS_PUSH_TOKEN, MAGIC_TOKEN, MDM_TOKEN, UNLOCK_TOKEN, " +
                            "CHALLENGE_TOKEN, DEVICE_INFO, SERIAL, PRODUCT, MAC_ADDRESS, DEVICE_NAME, ICCID," +
                            "LATITUDE, LONGITUDE FROM IOS_DEVICE WHERE MOBILE_DEVICE_ID = ?";
			stmt = conn.prepareStatement(selectDBQuery);
			stmt.setString(1, deviceID);
			ResultSet resultSet = stmt.executeQuery();

			if (resultSet.next()) {
				mobileDevice = new MobileDevice();
				mobileDevice.setMobileDeviceId(resultSet.getString(IOSPluginConstants.MOBILE_DEVICE_ID));

                Map<String, String> tokenMap = new HashMap<String, String>();
                tokenMap.put(IOSPluginConstants.APNS_PUSH_TOKEN,
                        resultSet.getString(IOSPluginConstants.APNS_PUSH_TOKEN));
                tokenMap.put(IOSPluginConstants.MAGIC_TOKEN, resultSet.getString(IOSPluginConstants.MAGIC_TOKEN));
                tokenMap.put(IOSPluginConstants.MDM_TOKEN, resultSet.getString(IOSPluginConstants.MDM_TOKEN));
                tokenMap.put(IOSPluginConstants.UNLOCK_TOKEN, resultSet.getString(IOSPluginConstants.UNLOCK_TOKEN));
                tokenMap.put(IOSPluginConstants.CHALLENGE_TOKEN,
                        resultSet.getString(IOSPluginConstants.CHALLENGE_TOKEN));
                tokenMap.put(IOSPluginConstants.DEVICE_INFO, resultSet.getString(IOSPluginConstants.DEVICE_INFO));
                tokenMap.put(IOSPluginConstants.SERIAL, resultSet.getString(IOSPluginConstants.SERIAL));
                tokenMap.put(IOSPluginConstants.PRODUCT, resultSet.getString(IOSPluginConstants.PRODUCT));
                tokenMap.put(IOSPluginConstants.MAC_ADDRESS, resultSet.getString(IOSPluginConstants.MAC_ADDRESS));
                tokenMap.put(IOSPluginConstants.DEVICE_NAME, resultSet.getString(IOSPluginConstants.DEVICE_NAME));
                tokenMap.put(IOSPluginConstants.ICCID, resultSet.getString(IOSPluginConstants.ICCID));
                tokenMap.put(IOSPluginConstants.LATITUDE, resultSet.getString(IOSPluginConstants.LATITUDE));
                tokenMap.put(IOSPluginConstants.LONGITUDE, resultSet.getString(IOSPluginConstants.LONGITUDE));

                mobileDevice.setDeviceProperties(tokenMap);

				if (log.isDebugEnabled()) {
					log.debug("Mobile device " + deviceID + " data has been fetched from iOS database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while fetching mobile device '" + deviceID + "'";
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
					"INSERT INTO IOS_DEVICE(MOBILE_DEVICE_ID, APNS_PUSH_TOKEN, MAGIC_TOKEN, MDM_TOKEN, UNLOCK_TOKEN, " +
                            "CHALLENGE_TOKEN, DEVICE_INFO, SERIAL, PRODUCT, MAC_ADDRESS, DEVICE_NAME, ICCID, " +
                            "LATITUDE, LONGITUDE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			stmt = conn.prepareStatement(createDBQuery);
			stmt.setString(1, mobileDevice.getMobileDeviceId());

            if (mobileDevice.getDeviceProperties() == null) {
                mobileDevice.setDeviceProperties(new HashMap<String, String>());
            }

			stmt.setString(2, mobileDevice.getDeviceProperties().get(IOSPluginConstants.APNS_PUSH_TOKEN));
			stmt.setString(3, mobileDevice.getDeviceProperties().get(IOSPluginConstants.MAGIC_TOKEN));
			stmt.setString(4, mobileDevice.getDeviceProperties().get(IOSPluginConstants.MDM_TOKEN));
			stmt.setString(5, mobileDevice.getDeviceProperties().get(IOSPluginConstants.UNLOCK_TOKEN));
			stmt.setString(6, mobileDevice.getDeviceProperties().get(IOSPluginConstants.CHALLENGE_TOKEN));
			stmt.setString(7, mobileDevice.getDeviceProperties().get(IOSPluginConstants.DEVICE_INFO));
            stmt.setString(8, mobileDevice.getDeviceProperties().get(IOSPluginConstants.SERIAL));
            stmt.setString(9, mobileDevice.getDeviceProperties().get(IOSPluginConstants.PRODUCT));
            stmt.setString(10, mobileDevice.getDeviceProperties().get(IOSPluginConstants.MAC_ADDRESS));
            stmt.setString(11, mobileDevice.getDeviceProperties().get(IOSPluginConstants.DEVICE_NAME));
            stmt.setString(12, mobileDevice.getDeviceProperties().get(IOSPluginConstants.ICCID));
            stmt.setString(13, mobileDevice.getDeviceProperties().get(IOSPluginConstants.LATITUDE));
            stmt.setString(14, mobileDevice.getDeviceProperties().get(IOSPluginConstants.LONGITUDE));

			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Mobile device " + mobileDevice.getMobileDeviceId() + " data has been added" +
					          " to the iOS database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while adding the mobile device '" +
			             mobileDevice.getMobileDeviceId() + "' to the iOS db.";
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
					"UPDATE IOS_DEVICE SET APNS_PUSH_TOKEN = ?, MAGIC_TOKEN = ?, MDM_TOKEN = ?, UNLOCK_TOKEN = ?, " +
					"CHALLENGE_TOKEN = ?, DEVICE_INFO = ?, SERIAL = ?, PRODUCT = ?, MAC_ADDRESS = ?, " +
                    "DEVICE_NAME = ?, ICCID = ?, LATITUDE = ?, LONGITUDE = ? WHERE MOBILE_DEVICE_ID = ?";
			stmt = conn.prepareStatement(updateDBQuery);
            stmt.setString(1, mobileDevice.getDeviceProperties().get(IOSPluginConstants.APNS_PUSH_TOKEN));
            stmt.setString(2, mobileDevice.getDeviceProperties().get(IOSPluginConstants.MAGIC_TOKEN));
            stmt.setString(3, mobileDevice.getDeviceProperties().get(IOSPluginConstants.MDM_TOKEN));
            stmt.setString(4, mobileDevice.getDeviceProperties().get(IOSPluginConstants.UNLOCK_TOKEN));
            stmt.setString(5, mobileDevice.getDeviceProperties().get(IOSPluginConstants.CHALLENGE_TOKEN));
            stmt.setString(6, mobileDevice.getDeviceProperties().get(IOSPluginConstants.DEVICE_INFO));
            stmt.setString(7, mobileDevice.getDeviceProperties().get(IOSPluginConstants.SERIAL));
            stmt.setString(8, mobileDevice.getDeviceProperties().get(IOSPluginConstants.PRODUCT));
            stmt.setString(9, mobileDevice.getDeviceProperties().get(IOSPluginConstants.MAC_ADDRESS));
            stmt.setString(10, mobileDevice.getDeviceProperties().get(IOSPluginConstants.DEVICE_NAME));
            stmt.setString(11, mobileDevice.getDeviceProperties().get(IOSPluginConstants.ICCID));
            stmt.setString(12, mobileDevice.getDeviceProperties().get(IOSPluginConstants.LATITUDE));
            stmt.setString(13, mobileDevice.getDeviceProperties().get(IOSPluginConstants.LONGITUDE));
            stmt.setString(14, mobileDevice.getDeviceProperties().get(IOSPluginConstants.MOBILE_DEVICE_ID));

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
					"DELETE FROM IOS_DEVICE WHERE MOBILE_DEVICE_ID = ?";
			stmt = conn.prepareStatement(deleteDBQuery);
			stmt.setString(1, mblDeviceId);
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Mobile device " + mblDeviceId + " data has deleted" +
					          " from the iOS database.");
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

		List<MobileDevice> mobileDevices = new ArrayList<MobileDevice>();
		try {
			conn = this.getConnection();
            String selectDBQuery =
                    "SELECT MOBILE_DEVICE_ID, APNS_PUSH_TOKEN, MAGIC_TOKEN, MDM_TOKEN, UNLOCK_TOKEN, " +
                            "CHALLENGE_TOKEN, DEVICE_INFO, SERIAL, PRODUCT, MAC_ADDRESS, DEVICE_NAME, ICCID," +
                            "LATITUDE, LONGITUDE FROM IOS_DEVICE";
			stmt = conn.prepareStatement(selectDBQuery);
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {

                MobileDevice mobileDevice = new MobileDevice();
                mobileDevice.setMobileDeviceId(resultSet.getString(IOSPluginConstants.MOBILE_DEVICE_ID));

                Map<String, String> tokenMap = new HashMap<String, String>();
                tokenMap.put(IOSPluginConstants.APNS_PUSH_TOKEN,
                        resultSet.getString(IOSPluginConstants.APNS_PUSH_TOKEN));
                tokenMap.put(IOSPluginConstants.MAGIC_TOKEN, resultSet.getString(IOSPluginConstants.MAGIC_TOKEN));
                tokenMap.put(IOSPluginConstants.MDM_TOKEN, resultSet.getString(IOSPluginConstants.MDM_TOKEN));
                tokenMap.put(IOSPluginConstants.UNLOCK_TOKEN, resultSet.getString(IOSPluginConstants.UNLOCK_TOKEN));
                tokenMap.put(IOSPluginConstants.CHALLENGE_TOKEN,
                        resultSet.getString(IOSPluginConstants.CHALLENGE_TOKEN));
                tokenMap.put(IOSPluginConstants.DEVICE_INFO, resultSet.getString(IOSPluginConstants.DEVICE_INFO));
                tokenMap.put(IOSPluginConstants.SERIAL, resultSet.getString(IOSPluginConstants.SERIAL));
                tokenMap.put(IOSPluginConstants.PRODUCT, resultSet.getString(IOSPluginConstants.PRODUCT));
                tokenMap.put(IOSPluginConstants.MAC_ADDRESS, resultSet.getString(IOSPluginConstants.MAC_ADDRESS));
                tokenMap.put(IOSPluginConstants.DEVICE_NAME, resultSet.getString(IOSPluginConstants.DEVICE_NAME));
                tokenMap.put(IOSPluginConstants.ICCID, resultSet.getString(IOSPluginConstants.ICCID));
                tokenMap.put(IOSPluginConstants.LATITUDE, resultSet.getString(IOSPluginConstants.LATITUDE));
                tokenMap.put(IOSPluginConstants.LONGITUDE, resultSet.getString(IOSPluginConstants.LONGITUDE));

                mobileDevice.setDeviceProperties(tokenMap);

                mobileDevices.add(mobileDevice);
			}
			if (log.isDebugEnabled()) {
				log.debug("All Mobile device details have fetched from iOS database.");
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