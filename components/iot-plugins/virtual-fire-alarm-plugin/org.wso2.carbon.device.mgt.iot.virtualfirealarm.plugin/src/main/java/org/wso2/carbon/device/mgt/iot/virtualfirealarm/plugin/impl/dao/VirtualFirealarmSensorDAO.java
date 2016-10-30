/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.sensor.mgt.dao.DeviceSensorDAO;
import org.wso2.carbon.device.mgt.common.sensor.mgt.dao.DeviceSensorDAOException;
import org.wso2.carbon.device.mgt.common.sensor.mgt.dao.SensorTransactionObject;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.exception.VirtualFirealarmDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.util.VirtualFireAlarmUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualFirealarmSensorDAO implements DeviceSensorDAO {
    private static final Log log = LogFactory.getLog(VirtualFirealarmSensorDAO.class);

    @Override
    public boolean addSensor(SensorTransactionObject sensorTObject) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;

        String sensorIdentifier = sensorTObject.getSensorIdentifier();
        String deviceIdentifier = sensorTObject.getDeviceIdentifier();
        String sensorTypeUniqueName = sensorTObject.getSensorTypeUniqueName();
        Map<String, String> deviceSensorDynamicProperties = sensorTObject.getDynamicProperties();

        try {
            conn = this.getConnection();
            String insertDBQuery =
                    "INSERT INTO VIRTUAL_FIREALARM_DEVICE_SENSORS (" +
                            "SENSOR_IDENTIFIER," +
                            "DEVICE_IDENTIFIER," +
                            "SENSOR_TYPE_NAME) " +
                            "VALUES (?,?,?)";

            stmt = conn.prepareStatement(insertDBQuery);
            stmt.setString(1, sensorIdentifier);
            stmt.setString(2, deviceIdentifier);
            stmt.setString(3, sensorTypeUniqueName);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("DeviceSensor [" + sensorIdentifier + "] of VirtualFirealarm with " +
                                      "Id [" + deviceIdentifier + "] was added successfully.");
                }

                if (deviceSensorDynamicProperties != null &&
                        !addSensorProperties(sensorIdentifier, deviceSensorDynamicProperties)) {
                    String msg = "Error occurred whilst adding Properties of the Sensor " +
                            "[" + sensorTypeUniqueName + "] attached to the VirtualFirealarm with " +
                            "Id [" + deviceIdentifier + "]";
                    log.error(msg);
                    throw new DeviceSensorDAOException(msg);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst registering a new Sensor [" + sensorIdentifier + "] for the " +
                    "VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String msg = "Error occurred whilst trying to open connection to DB to register a new Sensor.";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } finally {
            VirtualFireAlarmUtils.cleanupResources(stmt, null);
        }
        return true;
    }

    @Override
    public boolean updateSensor(SensorTransactionObject sensorTObject) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;

        String sensorIdentifier = sensorTObject.getSensorIdentifier();
        String deviceIdentifier = sensorTObject.getDeviceIdentifier();
        String sensorTypeUniqueName = sensorTObject.getSensorTypeUniqueName();
        Map<String, String> deviceSensorDynamicProperties = sensorTObject.getDynamicProperties();

        try {
            conn = this.getConnection();
            String updateDBQuery =
                    "UPDATE VIRTUAL_FIREALARM_DEVICE_SENSORS SET SENSOR_TYPE_NAME = ? " +
                            "WHERE SENSOR_IDENTIFIER = ? AND DEVICE_IDENTIFIER = ?";

            stmt = conn.prepareStatement(updateDBQuery);
            stmt.setString(1, sensorTypeUniqueName);
            stmt.setString(2, sensorIdentifier);
            stmt.setString(3, deviceIdentifier);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Details of Sensor [" + sensorIdentifier + "] of VirtualFirealarm with " +
                                    "Id [" + deviceIdentifier + "] was updated successfully.");
                }

                if (deviceSensorDynamicProperties != null &&
                        !addSensorProperties(sensorIdentifier, deviceSensorDynamicProperties)) {
                    String msg = "Error occurred whilst upbating Properties of the Sensor " +
                            "[" + sensorTypeUniqueName + "] in VirtualFirealarm with " +
                            "Id [" + deviceIdentifier + "]";
                    log.error(msg);
                    throw new DeviceSensorDAOException(msg);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst registering the new Sensor [" + sensorIdentifier + "] for the " +
                    "VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String msg = "Error occurred whilst trying to open connection to DB to update a device Sensor.";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } finally {
            VirtualFireAlarmUtils.cleanupResources(stmt, null);
        }
        return true;
    }

    @Override
    public SensorTransactionObject getSensor(String deviceIdentifier, String sensorIdentifier)
            throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        SensorTransactionObject sensorTransactionObject = null;

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT SENSOR_TYPE_NAME " +
                            "FROM VIRTUAL_FIREALARM_DEVICE_SENSORS " +
                            "WHERE DEVICE_IDENTIFIER = ? AND SENSOR_IDENTIFIER = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, deviceIdentifier);
            stmt.setString(2, sensorIdentifier);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String sensorTypeUniqueName = resultSet.getString(
                        SensorTransactionObject.DAOConstants.SENSOR_TYPE_NAME);
                Map<String, String> dynamicProperties = getSensorProperties(sensorIdentifier);

                sensorTransactionObject = new SensorTransactionObject();
                sensorTransactionObject.setSensorIdentifier(sensorIdentifier);
                sensorTransactionObject.setDeviceIdentifier(deviceIdentifier);
                sensorTransactionObject.setSensorTypeUniqueName(sensorTypeUniqueName);
                sensorTransactionObject.setDynamicProperties(dynamicProperties);
            }

            return sensorTransactionObject;
        } catch (SQLException e) {
            String msg = "A SQL error occurred whilst trying to get the Sensor [" + sensorIdentifier + "] of the " +
                    "VirtualFirealarm with id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String msg = "Error occurred whilst trying to open connection to DB to fetch a device Sensor.";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } finally {
            VirtualFireAlarmUtils.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public SensorTransactionObject getSensor(String sensorIdentifier) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        SensorTransactionObject sensorTObject = null;

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT DEVICE_IDENTIFIER, " +
                            "SENSOR_TYPE_NAME " +
                            "FROM VIRTUAL_FIREALARM_DEVICE_SENSORS " +
                            "WHERE SENSOR_IDENTIFIER = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, sensorIdentifier);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Map<String, String> dynamicProperties = getSensorProperties(sensorIdentifier);
                String deviceIdentifier = resultSet.getString(SensorTransactionObject.DAOConstants.DEVICE_IDENTIFIER);
                String sensorTypeUniqueName = resultSet.getString(
                        SensorTransactionObject.DAOConstants.SENSOR_TYPE_NAME);

                sensorTObject = new SensorTransactionObject();
                sensorTObject.setSensorIdentifier(sensorIdentifier);
                sensorTObject.setDeviceIdentifier(deviceIdentifier);
                sensorTObject.setSensorTypeUniqueName(sensorTypeUniqueName);
                sensorTObject.setDynamicProperties(dynamicProperties);
            }

            return sensorTObject;
        } catch (SQLException e) {
            String msg = "A SQL error occurred whilst trying to get the Sensor [" + sensorIdentifier + "] of the " +
                    "VirtualFirealarm device.";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String msg = "Error occurred whilst trying to open connection to DB to fetch a device Sensor.";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } finally {
            VirtualFireAlarmUtils.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public List<SensorTransactionObject> getSensors(String deviceIdentifier) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<SensorTransactionObject> deviceSensorTObjects = new ArrayList<>();

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT SENSOR_IDENTIFIER, " +
                            "SENSOR_TYPE_NAME " +
                            "FROM VIRTUAL_FIREALARM_DEVICE_SENSORS " +
                            "WHERE DEVICE_IDENTIFIER = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, deviceIdentifier);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String sensorIdentifier = resultSet.getString(SensorTransactionObject.DAOConstants.SENSOR_IDENTIFIER);
                String sensorTypeUniqueName = resultSet.getString(
                        SensorTransactionObject.DAOConstants.SENSOR_TYPE_NAME);
                Map<String, String> dynamicProperties = getSensorProperties(sensorIdentifier);

                SensorTransactionObject sensorTObject = new SensorTransactionObject();
                sensorTObject.setSensorIdentifier(sensorIdentifier);
                sensorTObject.setDeviceIdentifier(deviceIdentifier);
                sensorTObject.setSensorTypeUniqueName(sensorTypeUniqueName);
                sensorTObject.setDynamicProperties(dynamicProperties);
                deviceSensorTObjects.add(sensorTObject);
            }
            return deviceSensorTObjects;
        } catch (SQLException e) {
            String msg =
                    "A SQL error occurred whilst trying to get all the Sensors of the VirtualFirealarm with " +
                            "id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String msg = "Error occurred whilst trying to open connection to DB to fetch Sensors of a device.";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } finally {
            VirtualFireAlarmUtils.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public boolean removeSensor(String deviceIdentifier, String sensorIdentifier) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            if (!removeSensorProperties(sensorIdentifier)) {
                String msg = "Error occurred whilst deleting Properties of the Sensor with " +
                        "Id [" + sensorIdentifier + "] of VirtualFirealarm with Id [" + deviceIdentifier + "]";
                log.error(msg);
                throw new DeviceSensorDAOException(msg);
            }

            String deleteDBQuery =
                    "DELETE FROM VIRTUAL_FIREALARM_DEVICE_SENSORS " +
                            "WHERE DEVICE_IDENTIFIER = ? AND SENSOR_IDENTIFIER = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, deviceIdentifier);
            stmt.setString(2, sensorIdentifier);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Sensor [" + sensorIdentifier + "] of VirtualFirealarm with Id [" + deviceIdentifier +
                                      "] " +
                                      "has been deleted successfully.");
                }
            }
        } catch (SQLException e) {
            String msg =
                    "Error occurred whilst trying to delete the Sensor [" + sensorIdentifier +
                            "] of the VirtualFirealarm " +
                            "device with Id [" + deviceIdentifier + "].";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String msg = "Error occurred whilst trying to open connection to DB to remove a device Sensor.";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } finally {
            VirtualFireAlarmUtils.cleanupResources(stmt, null);
        }
        return true;
    }

    @Override
    public boolean removeSensor(String sensorIdentifier) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            if (!removeSensorProperties(sensorIdentifier)) {
                String msg = "Error occurred whilst deleting Properties of VirtualFirealarm Sensor with " +
                        "Id [" + sensorIdentifier + "]";
                log.error(msg);
                throw new DeviceSensorDAOException(msg);
            }

            String deleteDBQuery =
                    "DELETE FROM VIRTUAL_FIREALARM_DEVICE_SENSORS WHERE SENSOR_IDENTIFIER = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, sensorIdentifier);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("VirtualFirealarm Sensor with Id [" + sensorIdentifier + "] " +
                                      "has been deleted successfully.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst trying to delete the VirtualFirealarm Sensor with " +
                    "Id [" + sensorIdentifier + "].";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String msg = "Error occurred whilst trying to open connection to DB to remove a Sensor.";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } finally {
            VirtualFireAlarmUtils.cleanupResources(stmt, null);
        }
        return true;
    }

    @Override
    public boolean removeSensors(String deviceIdentifier) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet;
        String sensorIdentifier;

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT SENSOR_IDENTIFIER " +
                            "FROM VIRTUAL_FIREALARM_DEVICE_SENSORS " +
                            "WHERE DEVICE_IDENTIFIER = ?";

            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, deviceIdentifier);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                sensorIdentifier = resultSet.getString(SensorTransactionObject.DAOConstants.SENSOR_IDENTIFIER);
                if (!removeSensorProperties(sensorIdentifier)) {
                    String msg = "Error occurred whilst deleting Properties of a VirtualFirealarm Sensor with " +
                            "Id [" + sensorIdentifier + "].";
                    log.error(msg);
                    throw new DeviceSensorDAOException(msg);
                }
            }
            stmt.close();

            String deleteDBQuery =
                    "DELETE FROM VIRTUAL_FIREALARM_DEVICE_SENSORS WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, deviceIdentifier);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("All Sensors of VirtualFirealarm with Id [" + deviceIdentifier + "] " +
                                      "has been deleted successfully.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst trying to delete the Sensors of the VirtualFirealarm with " +
                    "Id [" + deviceIdentifier + "].";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String msg = "Error occurred whilst trying to open connection to DB to remove Sensors of a device.";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } finally {
            VirtualFireAlarmUtils.cleanupResources(stmt, null);
        }
        return true;
    }

    /**
     * @return
     * @throws SQLException
     */
    private Connection getConnection() throws VirtualFirealarmDeviceMgtPluginException {
        return VirtualFireAlarmDAOUtil.getConnection();
    }


    private boolean addSensorProperties(String sensorIdentifier, Map<String, String> deviceSensorProperties)
            throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            for (String property : deviceSensorProperties.keySet()) {
                String value = deviceSensorProperties.get(property);

                conn = this.getConnection();
                String insertDBQuery =
                        "INSERT INTO VIRTUAL_FIREALARM_SENSOR_DYNAMIC_PROPERTIES (" +
                                "SENSOR_IDENTIFIER," +
                                "PROPERTY_KEY," +
                                "PROPERTY_VALUE) " +
                                "VALUES (?,?,?)";

                stmt = conn.prepareStatement(insertDBQuery, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, sensorIdentifier);
                stmt.setString(2, property);
                stmt.setString(3, value);
                int rows = stmt.executeUpdate();

                if (rows > 0 && log.isDebugEnabled()) {
                    log.debug("Properties of Sensor with Id [" + sensorIdentifier + "] " +
                                      "was added successfully.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst adding properties (after adding the sensor) of a new " +
                    "Sensor whose is Id [" + sensorIdentifier + "].";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String msg = "Error occurred whilst trying to open connection to DB to register a new Sensor.";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } finally {
            VirtualFireAlarmUtils.cleanupResources(stmt, null);
        }
        return true;
    }


    private Map<String, String> getSensorProperties(String sensorIdentifier) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Map<String, String> deviceSensorProperties = new HashMap<>();

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT PROPERTY_KEY, " +
                            "PROPERTY_VALUE " +
                            "FROM VIRTUAL_FIREALARM_SENSOR_DYNAMIC_PROPERTIES " +
                            "WHERE SENSOR_IDENTIFIER = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, sensorIdentifier);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String propertyKey = resultSet.getString(SensorTransactionObject.DAOConstants.PROPERTY_KEY);
                String propertyVal = resultSet.getString(SensorTransactionObject.DAOConstants.PROPERTY_VALUE);
                deviceSensorProperties.put(propertyKey, propertyVal);
            }
        } catch (SQLException e) {
            String msg = "A SQL error occurred whilst trying to get the properties of Sensor with " +
                    "ID [" + sensorIdentifier + "].";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String msg = "Error occurred whilst trying to open connection to DB to register a new Sensor.";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } finally {
            VirtualFireAlarmUtils.cleanupResources(stmt, resultSet);
        }
        return deviceSensorProperties;
    }

    private boolean removeSensorProperties(String sensorIdentifier) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            String deleteDBQuery =
                    "DELETE FROM VIRTUAL_FIREALARM_SENSOR_DYNAMIC_PROPERTIES WHERE SENSOR_IDENTIFIER = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, sensorIdentifier);
            int rows = stmt.executeUpdate();

            if (rows > 0 && log.isDebugEnabled()) {
                log.debug("Properties of Sensor with Id [" + sensorIdentifier + "] " + "was deleted successfully.");
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst adding properties (after adding the sensor) of a new " +
                    "Sensor whose is Id [" + sensorIdentifier + "].";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String msg = "Error occurred whilst trying to open connection to DB to register a new Sensor.";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } finally {
            VirtualFireAlarmUtils.cleanupResources(stmt, null);
        }
        return true;
    }

}
