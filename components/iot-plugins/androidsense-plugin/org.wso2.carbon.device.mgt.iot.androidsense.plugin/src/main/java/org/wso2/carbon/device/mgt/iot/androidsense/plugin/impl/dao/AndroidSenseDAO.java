/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.device.mgt.iot.androidsense.plugin.impl.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.iot.androidsense.plugin.constants.AndroidSenseConstants;
import org.wso2.carbon.device.mgt.iot.androidsense.plugin.exception.AndroidSenseDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.iot.androidsense.plugin.impl.dao.util.AndroidSenseUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements dao impl for android Devices.
 */
public class AndroidSenseDAO {
	

	    private static final Log log = LogFactory.getLog(AndroidSenseDAO.class);

	    public Device getDevice(String deviceId) throws AndroidSenseDeviceMgtPluginException {
	        Connection conn = null;
	        PreparedStatement stmt = null;
	        Device device = null;
	        ResultSet resultSet = null;
	        try {
	            conn = AndroidSenseDAOUtil.getConnection();
	            String selectDBQuery =
						"SELECT ANDROID_DEVICE_ID, DEVICE_NAME" +
						" FROM ANDROID_SENSE_DEVICE WHERE ANDROID_DEVICE_ID = ?";
	            stmt = conn.prepareStatement(selectDBQuery);
	            stmt.setString(1, deviceId);
	            resultSet = stmt.executeQuery();

	            if (resultSet.next()) {
					device = new Device();
					device.setName(resultSet.getString(AndroidSenseConstants.DEVICE_PLUGIN_DEVICE_NAME));
					if (log.isDebugEnabled()) {
						log.debug("Android device " + deviceId + " data has been fetched from " +
						          "Android database.");
					}
				}
	        } catch (SQLException e) {
	            String msg = "Error occurred while fetching Android device : '" + deviceId + "'";
	            log.error(msg, e);
	            throw new AndroidSenseDeviceMgtPluginException(msg, e);
	        } finally {
				AndroidSenseUtils.cleanupResources(stmt, resultSet);
	            AndroidSenseDAOUtil.closeConnection();
	        }
	        return device;
	    }

	    public boolean addDevice(Device device)throws AndroidSenseDeviceMgtPluginException {
	        boolean status = false;
	        Connection conn = null;
	        PreparedStatement stmt = null;
	        try {
	            conn = AndroidSenseDAOUtil.getConnection();
	            String createDBQuery =
						"INSERT INTO ANDROID_SENSE_DEVICE(ANDROID_DEVICE_ID, DEVICE_NAME) VALUES (?, ?)";

	            stmt = conn.prepareStatement(createDBQuery);
				stmt.setString(1, device.getDeviceIdentifier());
				stmt.setString(2, device.getName());
				int rows = stmt.executeUpdate();
				if (rows > 0) {
					status = true;
					if (log.isDebugEnabled()) {
						log.debug("Android device " + device.getDeviceIdentifier() + " data has been" +
						          " added to the Android database.");
					}
				}
	        } catch (SQLException e) {
	            String msg = "Error occurred while adding the Android device '" + device.getDeviceIdentifier()
						+ "' to the Android db.";
	            log.error(msg, e);
	            throw new AndroidSenseDeviceMgtPluginException(msg, e);
	        } finally {
				AndroidSenseUtils.cleanupResources(stmt, null);
	        }
	        return status;
	    }

	    public boolean updateDevice(Device device) throws AndroidSenseDeviceMgtPluginException {
	        boolean status = false;
	        Connection conn = null;
	        PreparedStatement stmt = null;
	        try {
	            conn = AndroidSenseDAOUtil.getConnection();
	            String updateDBQuery =
						"UPDATE ANDROID_SENSE_DEVICE SET  DEVICE_NAME = ? WHERE ANDROID_DEVICE_ID = ?";

				stmt = conn.prepareStatement(updateDBQuery);
				stmt.setString(1, device.getName());
				stmt.setString(2, device.getDeviceIdentifier());
				int rows = stmt.executeUpdate();
				if (rows > 0) {
					status = true;
					if (log.isDebugEnabled()) {
						log.debug("Android device " + device.getDeviceIdentifier() + " data has been modified.");
					}
				}
	        } catch (SQLException e) {
	            String msg = "Error occurred while modifying the Android device '" +
	                         device.getDeviceIdentifier() + "' data.";
	            log.error(msg, e);
	            throw new AndroidSenseDeviceMgtPluginException(msg, e);
	        } finally {
				AndroidSenseUtils.cleanupResources(stmt, null);
	        }
	        return status;
	    }

	    public boolean deleteDevice(String deviceId) throws AndroidSenseDeviceMgtPluginException {
	        boolean status = false;
	        Connection conn = null;
	        PreparedStatement stmt = null;
	        try {
	            conn = AndroidSenseDAOUtil.getConnection();
	            String deleteDBQuery =
						"DELETE FROM ANDROID_SENSE_DEVICE WHERE ANDROID_DEVICE_ID = ?";
				stmt = conn.prepareStatement(deleteDBQuery);
				stmt.setString(1, deviceId);
				int rows = stmt.executeUpdate();
				if (rows > 0) {
					status = true;
					if (log.isDebugEnabled()) {
						log.debug("Android device " + deviceId + " data has deleted from the Android database.");
					}
				}
	        } catch (SQLException e) {
	            String msg = "Error occurred while deleting Android device " + deviceId;
	            log.error(msg, e);
	            throw new AndroidSenseDeviceMgtPluginException(msg, e);
	        } finally {
	            AndroidSenseUtils.cleanupResources(stmt, null);
	        }
	        return status;
	    }

	    public List<Device> getAllDevices() throws AndroidSenseDeviceMgtPluginException {

	        Connection conn = null;
	        PreparedStatement stmt = null;
	        ResultSet resultSet = null;
	        Device device;
	        List<Device> iotDevices = new ArrayList<>();

	        try {
	            conn = AndroidSenseDAOUtil.getConnection();
	            String selectDBQuery =
						"SELECT ANDROID_DEVICE_ID, DEVICE_NAME FROM ANDROID_SENSE_DEVICE";
				stmt = conn.prepareStatement(selectDBQuery);
				resultSet = stmt.executeQuery();
				while (resultSet.next()) {
					device = new Device();
					device.setDeviceIdentifier(resultSet.getString(AndroidSenseConstants.DEVICE_PLUGIN_DEVICE_ID));
					device.setName(resultSet.getString(AndroidSenseConstants.DEVICE_PLUGIN_DEVICE_NAME));
					iotDevices.add(device);
				}
	            if (log.isDebugEnabled()) {
	                log.debug("All Android device details have fetched from Android database.");
	            }
	            return iotDevices;
	        } catch (SQLException e) {
	            String msg = "Error occurred while fetching all Android device data'";
	            log.error(msg, e);
	            throw new AndroidSenseDeviceMgtPluginException(msg, e);
	        } finally {
	            AndroidSenseUtils.cleanupResources(stmt, resultSet);
	            AndroidSenseDAOUtil.closeConnection();
	        }
	        
	    }

	}