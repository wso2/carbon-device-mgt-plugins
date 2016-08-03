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
import org.wso2.carbon.device.mgt.common.sensor.mgt.DeviceTypeSensor;
import org.wso2.carbon.device.mgt.common.sensor.mgt.Sensor;
import org.wso2.carbon.device.mgt.common.sensor.mgt.dao.DeviceSensorDAO;
import org.wso2.carbon.device.mgt.common.sensor.mgt.dao.DeviceSensorDAOException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.exception.VirtualFirealarmDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.util.VirtualFireAlarmUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VirtualFirealarmSensorDAO implements DeviceSensorDAO {
    private static final Log log = LogFactory.getLog(VirtualFirealarmSensorDAO.class);

    @Override
    public boolean addSensor(Sensor sensor) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;

        String sensorIdentifier = sensor.getSensorIdentifier();
        String deviceIdentifier = sensor.getDeviceIdentifier();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        byte[] sensorTypeAsBytes;
        byte[] sensorPropertiesAsBytes;
        ByteArrayInputStream sensorTypeByteStream;
        ByteArrayInputStream sensorPropertiesByteStream;

        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            // Write byte stream of the DeviceTypeSensor Object of this Device Sensor.
            objectOutputStream.writeObject(sensor.getDeviceTypeSensor());
            sensorTypeAsBytes = byteArrayOutputStream.toByteArray();
            sensorTypeByteStream = new ByteArrayInputStream(sensorTypeAsBytes);
            // Flush the ByteStream plus the ObjectStreams before reuse.
            byteArrayOutputStream.flush();
            objectOutputStream.flush();
            // Write byte stream of the properties HashMap Object of this DeviceType Sensor
            objectOutputStream.writeObject(sensor.getDynamicProperties());
            sensorPropertiesAsBytes = byteArrayOutputStream.toByteArray();
            sensorPropertiesByteStream = new ByteArrayInputStream(sensorPropertiesAsBytes);

            conn = this.getConnection();
            String insertDBQuery =
                    "INSERT INTO VIRTUAL_FIREALARM_DEVICE_SENSORS (" +
                            "SENSOR_ID," +
                            "DEVICE_ID," +
                            "DEVICE_TYPE_SENSOR," +
                            "DYNAMIC_PROPERTIES," +
                            "VALUES (?,?,?,?)";

            stmt = conn.prepareStatement(insertDBQuery);
            stmt.setString(1, sensorIdentifier);
            stmt.setString(2, deviceIdentifier);
            stmt.setBinaryStream(3, sensorTypeByteStream, sensorTypeAsBytes.length);
            stmt.setBinaryStream(4, sensorPropertiesByteStream, sensorPropertiesAsBytes.length);
            int rows = stmt.executeUpdate();

            if (rows > 0 && log.isDebugEnabled()) {
                log.debug("DeviceSensor [" + sensorIdentifier + "] of VirtualFirealarm with " +
                                  "Id [" + deviceIdentifier + "] was added successfully.");
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst registering a new Sensor [" + sensorIdentifier + "] for the " +
                    "VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } catch (IOException e) {
            String msg = "Error occurred whilst trying to get the byte streams of the " +
                    "'DeviceTypeSensor' Object and 'dynamicProperties' HashMap of the " +
                    "Sensor [" + sensorIdentifier + "] to store as BLOBs in the DB";
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
    public boolean updateSensor(Sensor sensor) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;

        String sensorIdentifier = sensor.getSensorIdentifier();
        String deviceIdentifier = sensor.getDeviceIdentifier();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        byte[] sensorTypeAsBytes;
        byte[] sensorPropertiesAsBytes;
        ByteArrayInputStream sensorTypeByteStream;
        ByteArrayInputStream sensorPropertiesByteStream;

        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            // Write byte stream of the SensorType Object of this DeviceType Sensor.
            objectOutputStream.writeObject(sensor.getDeviceTypeSensor());
            sensorTypeAsBytes = byteArrayOutputStream.toByteArray();
            sensorTypeByteStream = new ByteArrayInputStream(sensorTypeAsBytes);
            // Flush the ByteStream plus the ObjectStreams before reuse.
            byteArrayOutputStream.flush();
            objectOutputStream.flush();
            // Write byte stream of the properties HashMap Object of this DeviceType Sensor
            objectOutputStream.writeObject(sensor.getDynamicProperties());
            sensorPropertiesAsBytes = byteArrayOutputStream.toByteArray();
            sensorPropertiesByteStream = new ByteArrayInputStream(sensorPropertiesAsBytes);

            conn = this.getConnection();
            String updateDBQuery =
                    "UPDATE VIRTUAL_FIREALARM_DEVICE_SENSORS SET " +
                            "DEVICE_TYPE_SENSOR = ?," +
                            "DYNAMIC_PROPERTIES = ?," +
                            "WHERE SENSOR_ID = ? AND DEVICE_ID = ?";

            stmt = conn.prepareStatement(updateDBQuery);
            stmt.setBinaryStream(1, sensorTypeByteStream, sensorTypeAsBytes.length);
            stmt.setBinaryStream(2, sensorPropertiesByteStream, sensorPropertiesAsBytes.length);
            stmt.setString(3, sensorIdentifier);
            stmt.setString(4, deviceIdentifier);
            int rows = stmt.executeUpdate();

            if (rows > 0 && log.isDebugEnabled()) {
                log.debug(
                        "Details of Sensor [" + sensorIdentifier + "] of VirtualFirealarm with " +
                                "Id [" + deviceIdentifier + "] was updated successfully.");
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst registering the new Sensor [" + sensorIdentifier + "] for the " +
                    "VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceSensorDAOException(msg, e);
        } catch (IOException e) {
            String msg = "Error occurred whilst trying to get the byte streams of the " +
                    "'DeviceTypeSensor' Object and 'dynamicProperties' HashMap of the " +
                    "Sensor [" + sensorIdentifier + "] to store as BLOBs in the DB";
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
    public Sensor getSensor(String deviceId, String sensorId) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Sensor sensor = null;

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT " +
                            "DEVICE_TYPE_SENSOR AS DEVICE_TYPE_SENSOR " +
                            "DYNAMIC_PROPERTIES AS DYNAMIC_PROPERTIES " +
                            "FROM VIRTUAL_FIREALARM_DEVICE_SENSORS " +
                            "WHERE DEVICE_ID = ? AND SENSOR_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, deviceId);
            stmt.setString(2, sensorId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                DeviceTypeSensor deviceTypeSensor;
                Map<String, Object> dynamicProperties;

                try {
                    // Read the BLOB of SensorType Object from DB as bytes.
                    deviceTypeSensor = (DeviceTypeSensor) deSerializeBlobData(
                            resultSet.getObject(Sensor.DAOConstants.DEVICE_TYPE_SENSOR), DeviceTypeSensor.class);
                    // Read the BLOB of Static-Properties Map from DB as bytes.
                    dynamicProperties = (Map<String, Object>) deSerializeBlobData(
                            resultSet.getObject(Sensor.DAOConstants.DYNAMIC_PROPERTIES), Map.class);
                } catch (ClassNotFoundException | IOException e) {
                    String msg = "An error occurred whilst trying to cast the BLOB data of Sensor [" + sensorId + "] " +
                            "of VirtualFirealarm with id [" + deviceId + "]";
                    log.error(msg, e);
                    throw new DeviceSensorDAOException(msg, e);
                }

                sensor = new Sensor();
                sensor.setSensorIdentifier(sensorId);
                sensor.setDeviceIdentifier(deviceId);
                sensor.setDeviceTypeSensor(deviceTypeSensor);
                sensor.setDynamicProperties(dynamicProperties);
            }

            return sensor;
        } catch (SQLException e) {
            String msg = "A SQL error occurred whilst trying to get the Sensor [" + sensorId + "] of the " +
                    "VirtualFirealarm with id [" + deviceId + "]";
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
    public Sensor getSensor(String sensorId) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Sensor sensor = null;

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT " +
                            "DEVICE_ID AS DEVICE_ID, " +
                            "DEVICE_TYPE_SENSOR AS DEVICE_TYPE_SENSOR " +
                            "DYNAMIC_PROPERTIES AS DYNAMIC_PROPERTIES " +
                            "FROM VIRTUAL_FIREALARM_DEVICE_SENSORS " +
                            "WHERE SENSOR_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, sensorId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                DeviceTypeSensor deviceTypeSensor;
                Map<String, Object> dynamicProperties;
                String deviceIdentifier = resultSet.getString(Sensor.DAOConstants.DEVICE_ID);

                try {
                    // Read the BLOB of SensorType Object from DB as bytes.
                    deviceTypeSensor = (DeviceTypeSensor) deSerializeBlobData(
                            resultSet.getObject(Sensor.DAOConstants.DEVICE_TYPE_SENSOR), DeviceTypeSensor.class);
                    // Read the BLOB of Static-Properties Map from DB as bytes.
                    dynamicProperties = (Map<String, Object>) deSerializeBlobData(
                            resultSet.getObject(Sensor.DAOConstants.DYNAMIC_PROPERTIES), Map.class);
                } catch (ClassNotFoundException | IOException e) {
                    String msg = "An error occurred whilst trying to cast the BLOB data of Sensor [" + sensorId + "] " +
                            "of VirtualFirealarm with id [" + deviceIdentifier + "]";
                    log.error(msg, e);
                    throw new DeviceSensorDAOException(msg, e);
                }

                sensor = new Sensor();
                sensor.setSensorIdentifier(sensorId);
                sensor.setDeviceIdentifier(deviceIdentifier);
                sensor.setDeviceTypeSensor(deviceTypeSensor);
                sensor.setDynamicProperties(dynamicProperties);
            }

            return sensor;
        } catch (SQLException e) {
            String msg = "A SQL error occurred whilst trying to get the Sensor [" + sensorId + "] of the " +
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
    public List<Sensor> getSensors(String deviceId) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Sensor> deviceSensors = new ArrayList<>();

        try {
            conn = this.getConnection();
            String selectDBQuery =
                    "SELECT " +
                            "SENSOR_ID AS SENSOR_ID, " +
                            "DEVICE_TYPE_SENSOR AS DEVICE_TYPE_SENSOR " +
                            "DYNAMIC_PROPERTIES AS DYNAMIC_PROPERTIES " +
                            "FROM VIRTUAL_FIREALARM_DEVICE_SENSORS " +
                            "WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, deviceId);
            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                DeviceTypeSensor deviceTypeSensor;
                Map<String, Object> dynamicProperties;
                String sensorIdentifier = resultSet.getString(Sensor.DAOConstants.SENSOR_ID);

                try {
                    // Read the BLOB of DeviceSensorType Object from DB as bytes.
                    deviceTypeSensor = (DeviceTypeSensor) deSerializeBlobData(
                            resultSet.getObject(Sensor.DAOConstants.DEVICE_TYPE_SENSOR), DeviceTypeSensor.class);
                    // Read the BLOB of Dynamic-Properties Map from DB as bytes.
                    dynamicProperties = (Map<String, Object>) deSerializeBlobData(
                            resultSet.getObject(Sensor.DAOConstants.DYNAMIC_PROPERTIES), Map.class);
                } catch (ClassNotFoundException | IOException e) {
                    String msg = "An error occurred whilst trying to cast the BLOB data of Sensor " +
                            "[" + sensorIdentifier + "] of VirtualFirealarm with id [" + deviceId + "]";
                    log.error(msg, e);
                    throw new DeviceSensorDAOException(msg, e);
                }

                Sensor sensor = new Sensor();
                sensor.setSensorIdentifier(sensorIdentifier);
                sensor.setDeviceIdentifier(deviceId);
                sensor.setDeviceTypeSensor(deviceTypeSensor);
                sensor.setDynamicProperties(dynamicProperties);
                deviceSensors.add(sensor);
            }
            return deviceSensors;
        } catch (SQLException e) {
            String msg =
                    "A SQL error occurred whilst trying to get all the Sensors of the VirtualFirealarm with " +
                            "id [" + deviceId + "]";
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
    public boolean removeSensor(String deviceId, String sensorId) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String deleteDBQuery =
                    "DELETE FROM VIRTUAL_FIREALARM_DEVICE_SENSORS WHERE DEVICE_ID = ? AND SENSOR_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, deviceId);
            stmt.setString(2, sensorId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("DeviceTypeSensor [" + sensorId + "] of VirtualFirealarm with Id [" + deviceId + "] " +
                                      "has been deleted successfully.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst trying to delete the Sensor [" + sensorId + "] of the VirtualFirealarm " +
                    "device with Id [" + deviceId + "].";
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
    public boolean removeSensor(String sensorId) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String deleteDBQuery =
                    "DELETE FROM VIRTUAL_FIREALARM_DEVICE_SENSORS WHERE SENSOR_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, sensorId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("VirtualFirealarm Sensor with Id [" + sensorId + "] " +
                                      "has been deleted successfully.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst trying to delete the VirtualFirealarm Sensor with " +
                    "Id [" + sensorId + "].";
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
    public boolean removeSensors(String deviceId) throws DeviceSensorDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String deleteDBQuery =
                    "DELETE FROM VIRTUAL_FIREALARM_DEVICE_SENSORS WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, deviceId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("All Sensors of VirtualFirealarm with Id [" + deviceId + "] " +
                                      "has been deleted successfully.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred whilst trying to delete the Sensors of the VirtualFirealarm with " +
                    "Id [" + deviceId + "].";
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

    /**
     * @param blobObjectFromDB
     * @param blobClass
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private Object deSerializeBlobData(Object blobObjectFromDB, Class blobClass)
            throws IOException, ClassNotFoundException {
        Object dataObjectFromDB = null;

        byte[] blobByteArray = (byte[]) blobObjectFromDB;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(blobByteArray);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        if (blobClass.isInstance(objectInputStream.readObject())) {
            dataObjectFromDB = objectInputStream.readObject();
        }
        return dataObjectFromDB;
    }
}
