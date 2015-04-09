/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.mobile.impl.android.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceDAO;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.util.MobileDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.mobile.dto.MobileDevice;
import org.wso2.carbon.device.mgt.mobile.impl.android.util.AndroidPluginConstants;
import org.wso2.carbon.device.mgt.mobile.impl.android.util.AndroidUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements MobileDeviceDAO for Android Devices.
 */
public class AndroidDeviceDAOImpl implements MobileDeviceDAO{

	private DataSource dataSource;
	private static final Log log = LogFactory.getLog(AndroidDeviceDAOImpl.class);

	public AndroidDeviceDAOImpl(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public MobileDevice getMobileDevice(String mblDeviceId)
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement stmt = null;
		MobileDevice mobileDevice = null;
		try {
			conn = this.getConnection();
			String selectDBQuery =
					"SELECT ANDROID_DEVICE_ID, GCM_TOKEN, DEVICE_INFO, DEVICE_MODEL, SERIAL, " +
					"VENDOR, MAC_ADDRESS, DEVICE_NAME, LATITUDE, LONGITUDE, IMEI, IMSI, OS_VERSION" +
					" FROM AD_DEVICE WHERE ANDROID_DEVICE_ID = ?";
			stmt = conn.prepareStatement(selectDBQuery);
			stmt.setString(1, mblDeviceId);
			ResultSet resultSet = stmt.executeQuery();

			if (resultSet.next()) {
				mobileDevice = new MobileDevice();
				mobileDevice.setMobileDeviceId(resultSet.getString(AndroidPluginConstants.
						                                                   ANDROID_DEVICE_ID));
				mobileDevice.setModel(resultSet.getString(AndroidPluginConstants.DEVICE_MODEL));
				mobileDevice.setSerial(resultSet.getString(AndroidPluginConstants.SERIAL));
				mobileDevice.setVendor(resultSet.getString(AndroidPluginConstants.VENDOR));
				mobileDevice.setLatitude(resultSet.getString(AndroidPluginConstants.LATITUDE));
				mobileDevice.setLongitude(resultSet.getString(AndroidPluginConstants.LONGITUDE));
				mobileDevice.setImei(resultSet.getString(AndroidPluginConstants.IMEI));
				mobileDevice.setImsi(resultSet.getString(AndroidPluginConstants.IMSI));
				mobileDevice.setOsVersion(resultSet.getString(AndroidPluginConstants.OS_VERSION));

				Map<String, String> propertyMap = new HashMap<String, String>();
				propertyMap.put(AndroidPluginConstants.GCM_TOKEN,
				             resultSet.getString(AndroidPluginConstants.GCM_TOKEN));
				propertyMap.put(AndroidPluginConstants.DEVICE_INFO,
				             resultSet.getString(AndroidPluginConstants.DEVICE_INFO));
				propertyMap.put(AndroidPluginConstants.DEVICE_NAME,
				             resultSet.getString(AndroidPluginConstants.DEVICE_NAME));

				mobileDevice.setDeviceProperties(propertyMap);

				if (log.isDebugEnabled()) {
					log.debug("Android device " + mblDeviceId + " data has been fetched from " +
					          "Android database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while fetching Android device : '" + mblDeviceId + "'";
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
					"INSERT INTO AD_DEVICE(ANDROID_DEVICE_ID, GCM_TOKEN, DEVICE_INFO, SERIAL, " +
					"VENDOR, MAC_ADDRESS, DEVICE_NAME, LATITUDE, LONGITUDE, IMEI, IMSI, " +
					"OS_VERSION, DEVICE_MODEL) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			stmt = conn.prepareStatement(createDBQuery);
			stmt.setString(1, mobileDevice.getMobileDeviceId());

			if (mobileDevice.getDeviceProperties() == null) {
				mobileDevice.setDeviceProperties(new HashMap<String, String>());
			}

			stmt.setString(2, AndroidUtils.getDeviceProperty(
					mobileDevice.getDeviceProperties(),
					AndroidPluginConstants.GCM_TOKEN));
			stmt.setString(3, AndroidUtils.getDeviceProperty(mobileDevice.getDeviceProperties(),
			                                             AndroidPluginConstants.DEVICE_INFO));
			stmt.setString(4, mobileDevice.getSerial());
			stmt.setString(5, mobileDevice.getVendor());
			stmt.setString(6, mobileDevice.getMobileDeviceId());
			stmt.setString(7, AndroidUtils.getDeviceProperty(mobileDevice.getDeviceProperties(),
			                                             AndroidPluginConstants.DEVICE_NAME));
			stmt.setString(8, mobileDevice.getLongitude());
			stmt.setString(9, mobileDevice.getLongitude());
			stmt.setString(10, mobileDevice.getImsi());
			stmt.setString(11, mobileDevice.getImsi());
			stmt.setString(12, mobileDevice.getOsVersion());
			stmt.setString(13, mobileDevice.getModel());
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Android device " + mobileDevice.getMobileDeviceId() + " data has been" +
					          " added to the Android database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while adding the Android device '" +
			             mobileDevice.getMobileDeviceId() + "' to the Android db.";
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
					"UPDATE AD_DEVICE SET GCM_TOKEN = ?, DEVICE_INFO = ?, SERIAL = ?, VENDOR = ?, " +
					"MAC_ADDRESS = ?, DEVICE_NAME = ?, LATITUDE = ?, LONGITUDE = ?, IMEI = ?, " +
					"IMSI = ?, OS_VERSION = ?, DEVICE_MODEL = ? WHERE ANDROID_DEVICE_ID = ?";

			stmt = conn.prepareStatement(updateDBQuery);

			if (mobileDevice.getDeviceProperties() == null) {
				mobileDevice.setDeviceProperties(new HashMap<String, String>());
			}

			stmt.setString(1, AndroidUtils.getDeviceProperty(
					mobileDevice.getDeviceProperties(),
					AndroidPluginConstants.GCM_TOKEN));
			stmt.setString(2, AndroidUtils.getDeviceProperty(mobileDevice.getDeviceProperties(),
			                                                 AndroidPluginConstants.DEVICE_INFO));
			stmt.setString(3, mobileDevice.getSerial());
			stmt.setString(4, mobileDevice.getVendor());
			stmt.setString(5, mobileDevice.getMobileDeviceId());
			stmt.setString(6, AndroidUtils.getDeviceProperty(mobileDevice.getDeviceProperties(),
			                                                 AndroidPluginConstants.DEVICE_NAME));
			stmt.setString(7, mobileDevice.getLatitude());
			stmt.setString(8, mobileDevice.getLongitude());
			stmt.setString(9, mobileDevice.getImei());
			stmt.setString(10, mobileDevice.getImsi());
			stmt.setString(11, mobileDevice.getOsVersion());
			stmt.setString(12, mobileDevice.getModel());
			stmt.setString(13, mobileDevice.getMobileDeviceId());
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Android device " + mobileDevice.getMobileDeviceId() + " data has been" +
					          " modified.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while modifying the Android device '" +
			             mobileDevice.getMobileDeviceId() + "' data.";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
		}
		return status;
	}

	@Override
	public boolean deleteMobileDevice(String mblDeviceId)
			throws MobileDeviceManagementDAOException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.getConnection();
			String deleteDBQuery =
					"DELETE FROM AD_DEVICE WHERE ANDROID_DEVICE_ID = ?";
			stmt = conn.prepareStatement(deleteDBQuery);
			stmt.setString(1, mblDeviceId);
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Android device " + mblDeviceId + " data has deleted" +
					          " from the Android database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while deleting android device " + mblDeviceId;
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
		}
		return status;
	}

	@Override
	public List<MobileDevice> getAllMobileDevices()
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement stmt = null;
		MobileDevice mobileDevice;
		List<MobileDevice> mobileDevices = new ArrayList<MobileDevice>();
		try {
			conn = this.getConnection();
			String selectDBQuery =
					"SELECT ANDROID_DEVICE_ID, GCM_TOKEN, DEVICE_INFO, DEVICE_MODEL, SERIAL, " +
					"VENDOR, MAC_ADDRESS, DEVICE_NAME, LATITUDE, LONGITUDE, IMEI, IMSI, OS_VERSION " +
					"FROM AD_DEVICE";
			stmt = conn.prepareStatement(selectDBQuery);
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				mobileDevice = new MobileDevice();
				mobileDevice.setMobileDeviceId(resultSet.getString(AndroidPluginConstants.
						                                                   ANDROID_DEVICE_ID));
				mobileDevice.setModel(resultSet.getString(AndroidPluginConstants.DEVICE_MODEL));
				mobileDevice.setSerial(resultSet.getString(AndroidPluginConstants.SERIAL));
				mobileDevice.setVendor(resultSet.getString(AndroidPluginConstants.VENDOR));
				mobileDevice.setLatitude(resultSet.getString(AndroidPluginConstants.LATITUDE));
				mobileDevice.setLongitude(resultSet.getString(AndroidPluginConstants.LONGITUDE));
				mobileDevice.setImei(resultSet.getString(AndroidPluginConstants.IMEI));
				mobileDevice.setImsi(resultSet.getString(AndroidPluginConstants.IMSI));
				mobileDevice.setOsVersion(resultSet.getString(AndroidPluginConstants.OS_VERSION));

				Map<String, String> propertyMap = new HashMap<String, String>();
				propertyMap.put(AndroidPluginConstants.GCM_TOKEN,
				                resultSet.getString(AndroidPluginConstants.GCM_TOKEN));
				propertyMap.put(AndroidPluginConstants.DEVICE_INFO,
				                resultSet.getString(AndroidPluginConstants.DEVICE_INFO));
				propertyMap.put(AndroidPluginConstants.DEVICE_NAME,
				                resultSet.getString(AndroidPluginConstants.DEVICE_NAME));

				mobileDevice.setDeviceProperties(propertyMap);
				mobileDevices.add(mobileDevice);
			}
			if (log.isDebugEnabled()) {
				log.debug("All Android device details have fetched from Android database.");
			}
			return mobileDevices;
		} catch (SQLException e) {
			String msg = "Error occurred while fetching all Android device data'";
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
