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

package org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.exception.RaspberrypiDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.impl.util.RaspberrypiUtils;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.impl.dao.RaspberrypiDAO;
import org.wso2.carbon.device.mgt.iot.raspberrypi.plugin.constants.RaspberrypiConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements CRUD for Raspberrypi Devices.
 */
public class RaspberrypiDeviceDAOImpl {

	private static final Log log = LogFactory.getLog(RaspberrypiDeviceDAOImpl.class);

	public Device getDevice(String iotDeviceId) throws RaspberrypiDeviceMgtPluginException {
		Connection conn;
		PreparedStatement stmt = null;
		Device device = null;
		ResultSet resultSet = null;
		try {
			conn = RaspberrypiDAO.getConnection();
			String selectDBQuery =
					"SELECT RASPBERRYPI_DEVICE_ID, DEVICE_NAME FROM RASPBERRYPI_DEVICE WHERE RASPBERRYPI_DEVICE_ID = ?";
			stmt = conn.prepareStatement(selectDBQuery);
			stmt.setString(1, iotDeviceId);
			resultSet = stmt.executeQuery();

			if (resultSet.next()) {
				device = new Device();
				device.setName(resultSet.getString(RaspberrypiConstants.DEVICE_PLUGIN_DEVICE_NAME));
				if (log.isDebugEnabled()) {
					log.debug("Raspberrypi device " + iotDeviceId + " data has been fetched from " +
							  "Raspberrypi database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while fetching Raspberrypi device : '" + iotDeviceId + "'";
			log.error(msg, e);
			throw new RaspberrypiDeviceMgtPluginException(msg, e);
		} finally {
			RaspberrypiUtils.cleanupResources(stmt, resultSet);
			RaspberrypiDAO.closeConnection();
		}
		return device;
	}

	public boolean addDevice(Device device) throws RaspberrypiDeviceMgtPluginException {
		boolean status = false;
		Connection conn;
		PreparedStatement stmt = null;
		try {
			conn = RaspberrypiDAO.getConnection();
			String createDBQuery =
					"INSERT INTO RASPBERRYPI_DEVICE(RASPBERRYPI_DEVICE_ID, DEVICE_NAME) VALUES (?, ?)";

			stmt = conn.prepareStatement(createDBQuery);
			stmt.setString(1, device.getDeviceIdentifier());
			stmt.setString(2, device.getName());
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Raspberrypi device " + device.getDeviceIdentifier() + " data has been" +
							  " added to the Raspberrypi database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while adding the Raspberrypi device '" +
						 device.getDeviceIdentifier() + "' to the Raspberrypi db.";
			log.error(msg, e);
			throw new RaspberrypiDeviceMgtPluginException(msg, e);
		} finally {
			RaspberrypiUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public boolean updateDevice(Device device) throws RaspberrypiDeviceMgtPluginException {
		boolean status = false;
		Connection conn;
		PreparedStatement stmt = null;
		try {
			conn = RaspberrypiDAO.getConnection();
			String updateDBQuery = "UPDATE RASPBERRYPI_DEVICE SET  DEVICE_NAME = ? WHERE RASPBERRYPI_DEVICE_ID = ?";
			stmt = conn.prepareStatement(updateDBQuery);
			stmt.setString(1, device.getName());
			stmt.setString(2, device.getDeviceIdentifier());
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Raspberrypi device " + device.getDeviceIdentifier() + " data has been" +
							  " modified.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while modifying the Raspberrypi device '" + device.getDeviceIdentifier()
					+ "' data.";
			log.error(msg, e);
			throw new RaspberrypiDeviceMgtPluginException(msg, e);
		} finally {
			RaspberrypiUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public boolean deleteDevice(String iotDeviceId) throws RaspberrypiDeviceMgtPluginException {
		boolean status = false;
		Connection conn;
		PreparedStatement stmt = null;
		try {
			conn = RaspberrypiDAO.getConnection();
			String deleteDBQuery = "DELETE FROM RASPBERRYPI_DEVICE WHERE RASPBERRYPI_DEVICE_ID = ?";
			stmt = conn.prepareStatement(deleteDBQuery);
			stmt.setString(1, iotDeviceId);
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Raspberrypi device " + iotDeviceId + " data has deleted" +
							  " from the Raspberrypi database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while deleting Raspberrypi device " + iotDeviceId;
			log.error(msg, e);
			throw new RaspberrypiDeviceMgtPluginException(msg, e);
		} finally {
			RaspberrypiUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public List<Device> getAllDevices() throws RaspberrypiDeviceMgtPluginException {
		Connection conn;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		Device device;
		List<Device> devices = new ArrayList<>();
		try {
			conn = RaspberrypiDAO.getConnection();
			String selectDBQuery = "SELECT RASPBERRYPI_DEVICE_ID, DEVICE_NAME FROM RASPBERRYPI_DEVICE";
			stmt = conn.prepareStatement(selectDBQuery);
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				device = new Device();
				device.setDeviceIdentifier(resultSet.getString(RaspberrypiConstants.DEVICE_PLUGIN_DEVICE_ID));
				device.setName(resultSet.getString(RaspberrypiConstants.DEVICE_PLUGIN_DEVICE_NAME));
				devices.add(device);
			}
			if (log.isDebugEnabled()) {
				log.debug("All Raspberrypi device details have fetched from Raspberrypi database.");
			}
			return devices;
		} catch (SQLException e) {
			String msg = "Error occurred while fetching all Raspberrypi device data'";
			log.error(msg, e);
			throw new RaspberrypiDeviceMgtPluginException(msg, e);
		} finally {
			RaspberrypiUtils.cleanupResources(stmt, resultSet);
			RaspberrypiDAO.closeConnection();
		}
	}
}