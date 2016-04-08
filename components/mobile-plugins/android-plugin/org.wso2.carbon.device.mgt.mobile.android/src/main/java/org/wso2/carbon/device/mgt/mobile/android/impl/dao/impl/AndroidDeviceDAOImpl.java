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

package org.wso2.carbon.device.mgt.mobile.android.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.mobile.android.impl.dao.AndroidDAOFactory;
import org.wso2.carbon.device.mgt.mobile.android.impl.util.AndroidPluginConstants;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceDAO;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.util.MobileDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.mobile.dto.MobileDevice;

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

	private static final Log log = LogFactory.getLog(AndroidDeviceDAOImpl.class);

	@Override
	public MobileDevice getMobileDevice(String mblDeviceId) throws MobileDeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
		MobileDevice mobileDevice = null;
        ResultSet rs = null;
		try {
			conn = AndroidDAOFactory.getConnection();
			String selectDBQuery =
					"SELECT DEVICE_ID, GCM_TOKEN, DEVICE_INFO, DEVICE_MODEL, SERIAL, " +
					"VENDOR, MAC_ADDRESS, DEVICE_NAME, LATITUDE, LONGITUDE, IMEI, IMSI, OS_VERSION" +
					" FROM AD_DEVICE WHERE DEVICE_ID = ?";
			stmt = conn.prepareStatement(selectDBQuery);
			stmt.setString(1, mblDeviceId);
			rs = stmt.executeQuery();

			if (rs.next()) {
				mobileDevice = new MobileDevice();
				mobileDevice.setMobileDeviceId(rs.getString(AndroidPluginConstants.DEVICE_ID));
				mobileDevice.setModel(rs.getString(AndroidPluginConstants.DEVICE_MODEL));
				mobileDevice.setSerial(rs.getString(AndroidPluginConstants.SERIAL));
				mobileDevice.setVendor(rs.getString(AndroidPluginConstants.VENDOR));
				mobileDevice.setLatitude(rs.getString(AndroidPluginConstants.LATITUDE));
				mobileDevice.setLongitude(rs.getString(AndroidPluginConstants.LONGITUDE));
				mobileDevice.setImei(rs.getString(AndroidPluginConstants.IMEI));
				mobileDevice.setImsi(rs.getString(AndroidPluginConstants.IMSI));
				mobileDevice.setOsVersion(rs.getString(AndroidPluginConstants.OS_VERSION));

				Map<String, String> propertyMap = new HashMap<String, String>();
				propertyMap.put(AndroidPluginConstants.GCM_TOKEN, rs.getString(AndroidPluginConstants.GCM_TOKEN));
				propertyMap.put(AndroidPluginConstants.DEVICE_INFO, rs.getString(AndroidPluginConstants.DEVICE_INFO));
				propertyMap.put(AndroidPluginConstants.DEVICE_NAME, rs.getString(AndroidPluginConstants.DEVICE_NAME));
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
			MobileDeviceManagementDAOUtil.cleanupResources(stmt, rs);
            AndroidDAOFactory.closeConnection();
		}

		return mobileDevice;
	}

	@Override
	public boolean addMobileDevice(MobileDevice mobileDevice) throws MobileDeviceManagementDAOException {
		boolean status = false;
		Connection conn;
		PreparedStatement stmt = null;
		try {
			conn = AndroidDAOFactory.getConnection();
			String createDBQuery =
					"INSERT INTO AD_DEVICE(DEVICE_ID, GCM_TOKEN, DEVICE_INFO, SERIAL, " +
					"VENDOR, MAC_ADDRESS, DEVICE_NAME, LATITUDE, LONGITUDE, IMEI, IMSI, " +
					"OS_VERSION, DEVICE_MODEL) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			stmt = conn.prepareStatement(createDBQuery);
			stmt.setString(1, mobileDevice.getMobileDeviceId());

            Map<String, String> properties = mobileDevice.getDeviceProperties();
			stmt.setString(2, properties.get(AndroidPluginConstants.GCM_TOKEN));
			stmt.setString(3, properties.get(AndroidPluginConstants.DEVICE_INFO));
			stmt.setString(4, mobileDevice.getSerial());
			stmt.setString(5, mobileDevice.getVendor());
			stmt.setString(6, mobileDevice.getMobileDeviceId());
			stmt.setString(7, properties.get(AndroidPluginConstants.DEVICE_NAME));
			stmt.setString(8, mobileDevice.getLatitude());
			stmt.setString(9, mobileDevice.getLongitude());
			stmt.setString(10, mobileDevice.getImei());
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
			throw new MobileDeviceManagementDAOException("Error occurred while adding the Android device '" +
                    mobileDevice.getMobileDeviceId() + "' information to the Android plugin data store.", e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
		}
		return status;
	}

	@Override
	public boolean updateMobileDevice(MobileDevice mobileDevice) throws MobileDeviceManagementDAOException {
		boolean status = false;
		Connection conn;
		PreparedStatement stmt = null;
		try {
			conn = AndroidDAOFactory.getConnection();
			String updateDBQuery =
					"UPDATE AD_DEVICE SET GCM_TOKEN = ?, DEVICE_INFO = ?, SERIAL = ?, VENDOR = ?, " +
					"MAC_ADDRESS = ?, DEVICE_NAME = ?, LATITUDE = ?, LONGITUDE = ?, IMEI = ?, " +
					"IMSI = ?, OS_VERSION = ?, DEVICE_MODEL = ? WHERE DEVICE_ID = ?";
			stmt = conn.prepareStatement(updateDBQuery);

			Map<String, String> properties = mobileDevice.getDeviceProperties();
			stmt.setString(1, properties.get(AndroidPluginConstants.GCM_TOKEN));
			stmt.setString(2, properties.get(AndroidPluginConstants.DEVICE_INFO));
			stmt.setString(3, mobileDevice.getSerial());
			stmt.setString(4, mobileDevice.getVendor());
			stmt.setString(5, mobileDevice.getMobileDeviceId());
			stmt.setString(6, properties.get(AndroidPluginConstants.DEVICE_NAME));
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
			MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
		}
		return status;
	}

	@Override
	public boolean deleteMobileDevice(String mblDeviceId)
			throws MobileDeviceManagementDAOException {
		boolean status = false;
		Connection conn;
		PreparedStatement stmt = null;
		try {
			conn = AndroidDAOFactory.getConnection();
			String deleteDBQuery =
					"DELETE FROM AD_DEVICE WHERE DEVICE_ID = ?";
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
			throw new MobileDeviceManagementDAOException("Error occurred while deleting android device '" +
                    mblDeviceId + "'", e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(stmt, null);
		}
		return status;
	}

	@Override
	public List<MobileDevice> getAllMobileDevices() throws MobileDeviceManagementDAOException {
		Connection conn;
		PreparedStatement stmt = null;
        ResultSet rs = null;
		MobileDevice mobileDevice;
		List<MobileDevice> mobileDevices = new ArrayList<MobileDevice>();
		try {
			conn = AndroidDAOFactory.getConnection();
			String selectDBQuery =
					"SELECT DEVICE_ID, GCM_TOKEN, DEVICE_INFO, DEVICE_MODEL, SERIAL, " +
					"VENDOR, MAC_ADDRESS, DEVICE_NAME, LATITUDE, LONGITUDE, IMEI, IMSI, OS_VERSION " +
					"FROM AD_DEVICE";
			stmt = conn.prepareStatement(selectDBQuery);
			rs = stmt.executeQuery();

			while (rs.next()) {
				mobileDevice = new MobileDevice();
				mobileDevice.setMobileDeviceId(rs.getString(AndroidPluginConstants.DEVICE_ID));
				mobileDevice.setModel(rs.getString(AndroidPluginConstants.DEVICE_MODEL));
				mobileDevice.setSerial(rs.getString(AndroidPluginConstants.SERIAL));
				mobileDevice.setVendor(rs.getString(AndroidPluginConstants.VENDOR));
				mobileDevice.setLatitude(rs.getString(AndroidPluginConstants.LATITUDE));
				mobileDevice.setLongitude(rs.getString(AndroidPluginConstants.LONGITUDE));
				mobileDevice.setImei(rs.getString(AndroidPluginConstants.IMEI));
				mobileDevice.setImsi(rs.getString(AndroidPluginConstants.IMSI));
				mobileDevice.setOsVersion(rs.getString(AndroidPluginConstants.OS_VERSION));

				Map<String, String> propertyMap = new HashMap<>();
				propertyMap.put(AndroidPluginConstants.GCM_TOKEN, rs.getString(AndroidPluginConstants.GCM_TOKEN));
				propertyMap.put(AndroidPluginConstants.DEVICE_INFO, rs.getString(AndroidPluginConstants.DEVICE_INFO));
				propertyMap.put(AndroidPluginConstants.DEVICE_NAME, rs.getString(AndroidPluginConstants.DEVICE_NAME));
				mobileDevice.setDeviceProperties(propertyMap);

				mobileDevices.add(mobileDevice);
			}
			if (log.isDebugEnabled()) {
				log.debug("All Android device details have fetched from Android database.");
			}
			return mobileDevices;
		} catch (SQLException e) {
			throw new MobileDeviceManagementDAOException("Error occurred while fetching all Android device data", e);
		} finally {
			MobileDeviceManagementDAOUtil.cleanupResources(stmt, rs);
            AndroidDAOFactory.closeConnection();
		}
	}

}
