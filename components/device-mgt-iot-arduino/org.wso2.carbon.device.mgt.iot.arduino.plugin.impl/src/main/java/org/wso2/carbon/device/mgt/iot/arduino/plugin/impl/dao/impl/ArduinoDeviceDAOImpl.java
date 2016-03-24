/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.arduino.plugin.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.iot.arduino.plugin.exception.ArduinoDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.iot.arduino.plugin.impl.util.ArduinoUtils;
import org.wso2.carbon.device.mgt.iot.arduino.plugin.constants.ArduinoConstants;
import org.wso2.carbon.device.mgt.iot.arduino.plugin.impl.dao.ArduinoDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements CRUD for arduino Devices.
 */
public class ArduinoDeviceDAOImpl {
	

	    private static final Log log = LogFactory.getLog(ArduinoDeviceDAOImpl.class);

	    public Device getDevice(String deviceId) throws ArduinoDeviceMgtPluginException {
	        Connection conn = null;
	        PreparedStatement stmt = null;
	        Device device = null;
	        ResultSet resultSet = null;
	        try {
	            conn = ArduinoDAO.getConnection();
	            String selectDBQuery =
						"SELECT ARDUINO_DEVICE_ID, DEVICE_NAME FROM ARDUINO_DEVICE WHERE ARDUINO_DEVICE_ID = ?";
	            stmt = conn.prepareStatement(selectDBQuery);
	            stmt.setString(1, deviceId);
	            resultSet = stmt.executeQuery();

	            if (resultSet.next()) {
					device = new Device();
					device.setName(resultSet.getString(ArduinoConstants.DEVICE_PLUGIN_DEVICE_NAME));
					if (log.isDebugEnabled()) {
						log.debug("Arduino device " + deviceId + " data has been fetched from " +
						          "Arduino database.");
					}
				}
	        } catch (SQLException e) {
	            String msg = "Error occurred while fetching Arduino device : '" + deviceId + "'";
	            log.error(msg, e);
	            throw new ArduinoDeviceMgtPluginException(msg, e);
	        } finally {
	            ArduinoUtils.cleanupResources(stmt, resultSet);
	            ArduinoDAO.closeConnection();
	        }
	        return device;
	    }

	    public boolean addDevice(Device iotDevice) throws ArduinoDeviceMgtPluginException {
	        boolean status = false;
	        Connection conn = null;
	        PreparedStatement stmt = null;
	        try {
	            conn = ArduinoDAO.getConnection();
	            String createDBQuery =
						"INSERT INTO ARDUINO_DEVICE(ARDUINO_DEVICE_ID, DEVICE_NAME) VALUES (?, ?)";

	            stmt = conn.prepareStatement(createDBQuery);
				stmt.setString(1, iotDevice.getDeviceIdentifier());
				stmt.setString(2,iotDevice.getName());
				int rows = stmt.executeUpdate();
				if (rows > 0) {
					status = true;
					if (log.isDebugEnabled()) {
						log.debug("Arduino device " + iotDevice.getDeviceIdentifier() + " data has been" +
						          " added to the Arduino database.");
					}
				}
	        } catch (SQLException e) {
	            String msg = "Error occurred while adding the Arduino device '" +
	                         iotDevice.getDeviceIdentifier() + "' to the Arduino db.";
	            log.error(msg, e);
	            throw new ArduinoDeviceMgtPluginException(msg, e);
	        } finally {
	            ArduinoUtils.cleanupResources(stmt, null);
	        }
	        return status;
	    }

	    public boolean updateDevice(Device iotDevice) throws ArduinoDeviceMgtPluginException {
	        boolean status = false;
	        Connection conn = null;
	        PreparedStatement stmt = null;
	        try {
	            conn = ArduinoDAO.getConnection();
	            String updateDBQuery =
						"UPDATE ARDUINO_DEVICE SET  DEVICE_NAME = ? WHERE ARDUINO_DEVICE_ID = ?";

				stmt = conn.prepareStatement(updateDBQuery);
				stmt.setString(1, iotDevice.getName());
				stmt.setString(2, iotDevice.getDeviceIdentifier());
				int rows = stmt.executeUpdate();
				if (rows > 0) {
					status = true;
					if (log.isDebugEnabled()) {
						log.debug("Arduino device " + iotDevice.getDeviceIdentifier() + " data has been" +
						          " modified.");
					}
				}
	        } catch (SQLException e) {
	            String msg = "Error occurred while modifying the Arduino device '" + iotDevice.getDeviceIdentifier()
						+ "' data.";
	            log.error(msg, e);
	            throw new ArduinoDeviceMgtPluginException(msg, e);
	        } finally {
	            ArduinoUtils.cleanupResources(stmt, null);
	        }
	        return status;
	    }

	    public boolean deleteDevice(String iotDeviceId) throws ArduinoDeviceMgtPluginException {
	        boolean status = false;
	        Connection conn = null;
	        PreparedStatement stmt = null;
	        try {
	            conn = ArduinoDAO.getConnection();
	            String deleteDBQuery =
						"DELETE FROM ARDUINO_DEVICE WHERE ARDUINO_DEVICE_ID = ?";
				stmt = conn.prepareStatement(deleteDBQuery);
				stmt.setString(1, iotDeviceId);
				int rows = stmt.executeUpdate();
				if (rows > 0) {
					status = true;
					if (log.isDebugEnabled()) {
						log.debug("Arduino device " + iotDeviceId + " data has deleted" +
						          " from the Arduino database.");
					}
				}
	        } catch (SQLException e) {
	            String msg = "Error occurred while deleting Arduino device " + iotDeviceId;
	            log.error(msg, e);
	            throw new ArduinoDeviceMgtPluginException(msg, e);
	        } finally {
	            ArduinoUtils.cleanupResources(stmt, null);
	        }
	        return status;
	    }

	    public List<Device> getAllDevices() throws ArduinoDeviceMgtPluginException {

	        Connection conn = null;
	        PreparedStatement stmt = null;
	        ResultSet resultSet = null;
	        Device device;
	        List<Device> devices = new ArrayList<Device>();
	        try {
	            conn = ArduinoDAO.getConnection();
	            String selectDBQuery =
						"SELECT ARDUINO_DEVICE_ID, DEVICE_NAME FROM ARDUINO_DEVICE";
				stmt = conn.prepareStatement(selectDBQuery);
				resultSet = stmt.executeQuery();
				while (resultSet.next()) {
					device = new Device();
					device.setDeviceIdentifier(resultSet.getString(ArduinoConstants.DEVICE_PLUGIN_DEVICE_ID));
					device.setName(resultSet.getString(ArduinoConstants.DEVICE_PLUGIN_DEVICE_NAME));
				}
	            if (log.isDebugEnabled()) {
	                log.debug("All Arduino device details have fetched from Arduino database.");
	            }
	            return devices;
	        } catch (SQLException e) {
	            String msg = "Error occurred while fetching all Arduino device data'";
	            log.error(msg, e);
	            throw new ArduinoDeviceMgtPluginException(msg, e);
	        } finally {
	            ArduinoUtils.cleanupResources(stmt, resultSet);
	            ArduinoDAO.closeConnection();
	        }
	    }
	}