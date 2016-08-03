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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.sensor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.sensor.mgt.DeviceTypeSensor;
import org.wso2.carbon.device.mgt.common.sensor.mgt.Sensor;
import org.wso2.carbon.device.mgt.common.sensor.mgt.SensorManager;
import org.wso2.carbon.device.mgt.common.sensor.mgt.SensorType;
import org.wso2.carbon.device.mgt.common.sensor.mgt.dao.DeviceSensorDAOException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.exception.VirtualFirealarmDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.dao.VirtualFireAlarmDAOUtil;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.dao.VirtualFirealarmSensorDAO;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.util.VirtualFireAlarmUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualFirealarmSensorManager implements SensorManager {

    private static final Log log = LogFactory.getLog(VirtualFirealarmSensorManager.class);

    private Map<String, SensorType> sensorTypeList = new HashMap<>();
    private List<DeviceTypeSensor> deviceTypeSensorList = new ArrayList<>();
    private VirtualFirealarmSensorDAO virtualFirealarmSensorDAO = new VirtualFirealarmSensorDAO();

    private class VirtualFirealarmSensorTypes {
        private static final String DHT11_TEMPERATURE_SENSOR = "DHT11_Temperature_Sensor";
        private static final String ADAFRUIT_CAMERA_SENSOR = "Adafruit_Camera_Module";
    }

    @Override
    public void initDeviceTypeSensors() throws DeviceManagementException {
        loadSensorTypes();

        Map<String, Object> sensorProperties = new HashMap<>();
        // Sensor ID will added during the time of Device instantiation.
        DeviceTypeSensor leftTempSensor = new DeviceTypeSensor(
                sensorTypeList.get(VirtualFirealarmSensorTypes.DHT11_TEMPERATURE_SENSOR));
        leftTempSensor.setName("Left_End_Temperature");
        leftTempSensor.setDescription("The DHT11 Temperature sensor found at the left end of the firealarm");
        leftTempSensor.addMetaPropertyToStreamDefinition("sensorId", "STRING");

        sensorProperties.put("Tilted_Angle", "60");
        leftTempSensor.setStaticProperties(sensorProperties);
        sensorProperties.clear();
        // --------------------------------------------------------------
        DeviceTypeSensor rightTempSensor = new DeviceTypeSensor(
                sensorTypeList.get(VirtualFirealarmSensorTypes.DHT11_TEMPERATURE_SENSOR));
        rightTempSensor.setName("Right_End_Temperature");
        rightTempSensor.setDescription("The DHT11 Temperature sensor found at the right end of the firealarm");
        rightTempSensor.addMetaPropertyToStreamDefinition("sensorId", "STRING");

        sensorProperties.put("Tilted_Angle", "75");
        rightTempSensor.setStaticProperties(sensorProperties);
        sensorProperties.clear();
        // --------------------------------------------------------------
        DeviceTypeSensor leftCamera = new DeviceTypeSensor(
                sensorTypeList.get(VirtualFirealarmSensorTypes.ADAFRUIT_CAMERA_SENSOR));
        leftCamera.setName("Left_End_Camera");
        leftCamera.setDescription("The Adafruit Technologies camera at the left end of the firealarm");
        leftCamera.addMetaPropertyToStreamDefinition("sensorId", "STRING");

        sensorProperties.put("Quality", "LOW");
        leftCamera.setStaticProperties(sensorProperties);
        sensorProperties.clear();
        // --------------------------------------------------------------
        DeviceTypeSensor rightCamera = new DeviceTypeSensor(
                sensorTypeList.get(VirtualFirealarmSensorTypes.ADAFRUIT_CAMERA_SENSOR));
        rightCamera.setName("Right_End_Camera");
        rightCamera.setDescription("The Adafruit Technologies camera at the left end of the firealarm");
        rightCamera.addMetaPropertyToStreamDefinition("sensorId", "STRING");

        sensorProperties.put("Quality", "HIGH");
        rightCamera.setStaticProperties(sensorProperties);
        sensorProperties.clear();
        // --------------------------------------------------------------

        this.deviceTypeSensorList.add(leftTempSensor);
        this.deviceTypeSensorList.add(leftCamera);
        this.deviceTypeSensorList.add(rightTempSensor);
        this.deviceTypeSensorList.add(rightCamera);
    }

    @Override
    public List<DeviceTypeSensor> getDeviceTypeSensors() throws DeviceManagementException {
        return this.deviceTypeSensorList;
    }

    @Override
    public boolean addSensor(String deviceId, Sensor sensor) throws DeviceManagementException {
        String sensorId = sensor.getSensorIdentifier();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Adding sensor [" + sensorId + "] for Virtual Firealarm device : " + deviceId);
            }
            sensor.setDeviceIdentifier(deviceId);
            VirtualFireAlarmDAOUtil.beginTransaction();
            virtualFirealarmSensorDAO.addSensor(sensor);
            VirtualFireAlarmDAOUtil.commitTransaction();

        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            try {
                VirtualFireAlarmDAOUtil.rollbackTransaction();
            } catch (VirtualFirealarmDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred whilst rolling back the Sensor addition transaction of " +
                        "VirtualFirealarm [" + deviceId + "] for sensor [" + sensorId + "]";
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error in getting connection to the DB or Committing the transaction when adding Sensor [" +
                    sensorId + "] of the VirtualFirealarm with Id [" + deviceId + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceSensorDAOException e) {
            String msg = "Error while adding Sensor [" + sensorId + "] of the VirtualFirealarm with Id [" + deviceId + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return true;
    }

    @Override
    public boolean addSensors(String deviceId, List<Sensor> sensors) throws DeviceManagementException {
        try {
            VirtualFireAlarmDAOUtil.beginTransaction();
            for (Sensor sensor : sensors) {
                if (log.isDebugEnabled()) {
                    log.debug("Adding sensor [" + sensor.getSensorIdentifier() + "] for " +
                                      "Virtual Firealarm device : " + deviceId);
                }
                sensor.setDeviceIdentifier(deviceId);
                virtualFirealarmSensorDAO.addSensor(sensor);
            }
            VirtualFireAlarmDAOUtil.commitTransaction();

        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            try {
                VirtualFireAlarmDAOUtil.rollbackTransaction();
            } catch (VirtualFirealarmDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred whilst rolling back the Sensor addition transaction of " +
                        "VirtualFirealarm [" + deviceId + "]";
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error in getting connection to the DB or Committing the transaction when adding Sensors " +
                    "of the VirtualFirealarm with Id [" + deviceId + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceSensorDAOException e) {
            String msg = "Error while adding Sensors of the VirtualFirealarm with Id [" + deviceId + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return true;
    }

    @Override
    public boolean updateSensor(String deviceId, Sensor sensor) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean updateSensors(String deviceId, List<Sensor> sensors) throws DeviceManagementException {
        return false;
    }

    @Override
    public Sensor getSensor(String deviceId, String sensorId) throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Sensor> getSensors(String deviceId) throws DeviceManagementException {
        return null;
    }

    @Override
    public boolean removeSensor(String deviceId, String sensorId) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean removeSensors(String deviceId) throws DeviceManagementException {
        return false;
    }

    public void loadSensorTypes() throws DeviceManagementException {
//        Sensor Type 1 - DHT Sensor
        SensorType dht11TemperatureSensor = new SensorType();
        dht11TemperatureSensor.setTypeID(VirtualFireAlarmUtils.shortUUID());
        dht11TemperatureSensor.setTypeName(VirtualFirealarmSensorTypes.DHT11_TEMPERATURE_SENSOR);
        dht11TemperatureSensor.setTypeTAG(SensorType.CommonSensorTypes.TEMPERATURE.getValue());
        dht11TemperatureSensor.setDescription("A DHT11 type Temperature sensor.");

        Map<String, Object> sensorTypeProperties_1 = new HashMap<>();
        sensorTypeProperties_1.put("Unit", "C");
        sensorTypeProperties_1.put("Max_Measure", "100");
        sensorTypeProperties_1.put("Min_Meansure", "10");
        dht11TemperatureSensor.setTypeProperties(sensorTypeProperties_1);
        dht11TemperatureSensor.setStreamDefinitionVersion("1.0.0");

        Map<String, String> metaData_1 = new HashMap<>();
        metaData_1.put("owner", "STRING");
        metaData_1.put("deviceType", "STRING");
        metaData_1.put("deviceId", "STRING");
        metaData_1.put("time", "STRING");
        dht11TemperatureSensor.setMetaData(metaData_1);

        Map<String, String> payLoadData_1 = new HashMap<>();
        payLoadData_1.put("temperature", "FLOAT");
        dht11TemperatureSensor.setPayloadData(payLoadData_1);
        dht11TemperatureSensor.buildStreamDefinition();
//-----------------------------------------------------------------------
//        Sensor Type 2 - GPS Sensor
        SensorType adafruitGPSSensor = new SensorType();
        adafruitGPSSensor.setTypeID(VirtualFireAlarmUtils.shortUUID());
        adafruitGPSSensor.setTypeName(VirtualFirealarmSensorTypes.ADAFRUIT_CAMERA_SENSOR);
        adafruitGPSSensor.setTypeTAG(SensorType.CommonSensorTypes.CAMERA.getValue());
        adafruitGPSSensor.setDescription("An Adafruit technologies Camera module.");

        Map<String, Object> sensorTypeProperties_2 = new HashMap<>();
        sensorTypeProperties_2.put("Pixels", "360p");
        sensorTypeProperties_2.put("Lag", "2sec");
        adafruitGPSSensor.setTypeProperties(sensorTypeProperties_2);
        adafruitGPSSensor.setStreamDefinitionVersion("1.0.0");

        Map<String, String> metaData_2 = new HashMap<>();
        metaData_2.put("owner", "STRING");
        metaData_2.put("deviceType", "STRING");
        metaData_2.put("deviceId", "STRING");
        metaData_2.put("time", "STRING");
        adafruitGPSSensor.setMetaData(metaData_2);

        Map<String, String> payLoadData_2 = new HashMap<>();
        payLoadData_2.put("cameraStream", "STRING");
        adafruitGPSSensor.setPayloadData(payLoadData_2);
        adafruitGPSSensor.buildStreamDefinition();

        sensorTypeList.put(VirtualFirealarmSensorTypes.DHT11_TEMPERATURE_SENSOR, dht11TemperatureSensor);
        sensorTypeList.put(VirtualFirealarmSensorTypes.ADAFRUIT_CAMERA_SENSOR, adafruitGPSSensor);
    }
}
