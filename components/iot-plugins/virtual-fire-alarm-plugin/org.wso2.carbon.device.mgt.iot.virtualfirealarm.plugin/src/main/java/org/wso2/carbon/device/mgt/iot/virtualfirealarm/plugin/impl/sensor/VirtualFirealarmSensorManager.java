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
import org.wso2.carbon.device.mgt.common.sensor.mgt.SensorManager;
import org.wso2.carbon.device.mgt.common.sensor.mgt.dao.DeviceSensorDAOException;
import org.wso2.carbon.device.mgt.common.sensor.mgt.dao.SensorTransactionObject;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.exception.VirtualFirealarmDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.dao.VirtualFireAlarmDAOUtil;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.dao.VirtualFirealarmSensorDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualFirealarmSensorManager implements SensorManager {

    private static final Log log = LogFactory.getLog(VirtualFirealarmSensorManager.class);

    private List<DeviceTypeSensor> deviceTypeSensorList = new ArrayList<>();
    private VirtualFirealarmSensorDAO virtualFirealarmSensorDAO = new VirtualFirealarmSensorDAO();

    private class VirtualFirealarmSensorTypes {
        private static final String DHT11_TEMPERATURE_SENSOR = "DHT11_Temperature_Sensor";
        private static final String ADAFRUIT_CAMERA_SENSOR = "Adafruit_Camera_Module";
    }

    @Override
    public void initDeviceTypeSensors() throws DeviceManagementException {
        Map<String, String> sensorProperties = new HashMap<>();
        // Sensor ID will added during the time of Device instantiation.
        DeviceTypeSensor leftTempSensor = new DeviceTypeSensor(
                VirtualFirealarmSensorTypes.DHT11_TEMPERATURE_SENSOR, "NO STREAMDEF");
        leftTempSensor.setUniqueSensorName("Left_End_Temperature");
        leftTempSensor.setDescription("The DHT11 Temperature sensor found at the left end of the firealarm");
        leftTempSensor.addMetaPropertyToStreamDefinition("sensorId", "STRING");

        sensorProperties.put("Tilted_Angle", "60");
        leftTempSensor.setStaticProperties(sensorProperties);
        this.deviceTypeSensorList.add(leftTempSensor);
        // --------------------------------------------------------------
        DeviceTypeSensor rightTempSensor = new DeviceTypeSensor(
                VirtualFirealarmSensorTypes.DHT11_TEMPERATURE_SENSOR, "NO STREAMDEF");
        rightTempSensor.setUniqueSensorName("Right_End_Temperature");
        rightTempSensor.setDescription("The DHT11 Temperature sensor found at the right end of the firealarm");
        rightTempSensor.addMetaPropertyToStreamDefinition("sensorId", "STRING");

        sensorProperties.put("Tilted_Angle", "75");
        rightTempSensor.setStaticProperties(sensorProperties);
        this.deviceTypeSensorList.add(rightTempSensor);
        // --------------------------------------------------------------
        DeviceTypeSensor leftCamera = new DeviceTypeSensor(
                VirtualFirealarmSensorTypes.ADAFRUIT_CAMERA_SENSOR, "NO STREAMDEF");
        leftCamera.setUniqueSensorName("Left_End_Camera");
        leftCamera.setDescription("The Adafruit Technologies camera at the left end of the firealarm");
        leftCamera.addMetaPropertyToStreamDefinition("sensorId", "STRING");

        sensorProperties.put("Quality", "LOW");
        leftCamera.setStaticProperties(sensorProperties);
        this.deviceTypeSensorList.add(leftCamera);
        // --------------------------------------------------------------
        DeviceTypeSensor rightCamera = new DeviceTypeSensor(
                VirtualFirealarmSensorTypes.ADAFRUIT_CAMERA_SENSOR, "NO STREAMDEF");
        rightCamera.setUniqueSensorName("Right_End_Camera");
        rightCamera.setDescription("The Adafruit Technologies camera at the left end of the firealarm");
        rightCamera.addMetaPropertyToStreamDefinition("sensorId", "STRING");

        sensorProperties.put("Quality", "HIGH");
        rightCamera.setStaticProperties(sensorProperties);
        this.deviceTypeSensorList.add(rightCamera);
        // --------------------------------------------------------------
    }

    @Override
    public List<DeviceTypeSensor> getDeviceTypeSensors() throws DeviceManagementException {
        return this.deviceTypeSensorList;
    }

    @Override
    public boolean addSensor(SensorTransactionObject sensorTObject) throws DeviceManagementException {
        String sensorIdentifier = sensorTObject.getSensorIdentifier();
        String deviceIdentifier = sensorTObject.getDeviceIdentifier();
        try {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Adding sensor [" + sensorIdentifier + "] for Virtual Firealarm device : " + deviceIdentifier);
            }
            VirtualFireAlarmDAOUtil.beginTransaction();
            virtualFirealarmSensorDAO.addSensor(sensorTObject);
            VirtualFireAlarmDAOUtil.commitTransaction();

        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            try {
                VirtualFireAlarmDAOUtil.rollbackTransaction();
            } catch (VirtualFirealarmDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred whilst rolling back the Sensor addition transaction of " +
                        "VirtualFirealarm [" + deviceIdentifier + "] for sensor [" + sensorIdentifier + "]";
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error in getting connection to the DB or Committing the transaction when adding Sensor [" +
                    sensorIdentifier + "] of the VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceSensorDAOException e) {
            String msg = "Error while adding Sensor [" + sensorIdentifier + "] of the VirtualFirealarm with Id [" +
                    deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return true;
    }

    @Override
    public boolean addSensors(String deviceIdentifier, List<SensorTransactionObject> sensorTObjects)
            throws DeviceManagementException {
        try {
            VirtualFireAlarmDAOUtil.beginTransaction();
            for (SensorTransactionObject sensorTObject : sensorTObjects) {
                String sensorIdentifier = sensorTObject.getSensorIdentifier();

                if (log.isDebugEnabled()) {
                    log.debug("Adding sensor [" + sensorIdentifier + "] for Virtual Firealarm " +
                                      "device : " + deviceIdentifier);
                }
                virtualFirealarmSensorDAO.addSensor(sensorTObject);
            }
            VirtualFireAlarmDAOUtil.commitTransaction();

        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            try {
                VirtualFireAlarmDAOUtil.rollbackTransaction();
            } catch (VirtualFirealarmDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred whilst rolling back the Sensor addition transaction of " +
                        "VirtualFirealarm [" + deviceIdentifier + "]";
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error in getting connection to the DB or Committing the transaction when adding Sensors " +
                    "of the VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceSensorDAOException e) {
            String msg = "Error while adding Sensors of the VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return true;
    }

    @Override
    public boolean updateSensor(SensorTransactionObject sensorTObject) throws DeviceManagementException {
        String sensorIdentifier = sensorTObject.getSensorIdentifier();
        String deviceIdentifier = sensorTObject.getDeviceIdentifier();

        try {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Updating sensor [" + sensorIdentifier + "] for Virtual Firealarm device : " +
                                deviceIdentifier);
            }
            VirtualFireAlarmDAOUtil.beginTransaction();
            virtualFirealarmSensorDAO.updateSensor(sensorTObject);
            VirtualFireAlarmDAOUtil.commitTransaction();

        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            try {
                VirtualFireAlarmDAOUtil.rollbackTransaction();
            } catch (VirtualFirealarmDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred whilst rolling back the Sensor update transaction of " +
                        "VirtualFirealarm [" + deviceIdentifier + "] for sensor [" + sensorIdentifier + "]";
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error in getting connection to the DB or Committing the transaction when updating Sensor [" +
                    sensorIdentifier + "] of the VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceSensorDAOException e) {
            String msg = "Error while updating Sensor [" + sensorIdentifier + "] of the VirtualFirealarm with Id [" +
                    deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return true;
    }

    @Override
    public boolean updateSensors(String deviceIdentifier, List<SensorTransactionObject> sensorTObjects)
            throws DeviceManagementException {
        try {
            VirtualFireAlarmDAOUtil.beginTransaction();
            for (SensorTransactionObject sensorTObject : sensorTObjects) {
                String sensorIdentifier = sensorTObject.getSensorIdentifier();

                if (log.isDebugEnabled()) {
                    log.debug("Adding sensor [" + sensorIdentifier + "] for Virtual Firealarm " +
                                      "device : " + deviceIdentifier);
                }
                virtualFirealarmSensorDAO.updateSensor(sensorTObject);
            }
            VirtualFireAlarmDAOUtil.commitTransaction();

        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            try {
                VirtualFireAlarmDAOUtil.rollbackTransaction();
            } catch (VirtualFirealarmDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred whilst rolling back the Sensor update transaction of " +
                        "VirtualFirealarm [" + deviceIdentifier + "]";
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error in getting connection to the DB or Committing the transaction when updating Sensors " +
                    "of the VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceSensorDAOException e) {
            String msg = "Error while updating Sensors of the VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return true;
    }

    @Override
    public SensorTransactionObject getSensor(String deviceIdentifier, String sensorIdentifier)
            throws DeviceManagementException {
        SensorTransactionObject sensorTObject;
        try {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Fetching sensor [" + sensorIdentifier + "] for Virtual Firealarm device : " +
                                deviceIdentifier);
            }
            VirtualFireAlarmDAOUtil.beginTransaction();
            sensorTObject = virtualFirealarmSensorDAO.getSensor(deviceIdentifier, sensorIdentifier);
            VirtualFireAlarmDAOUtil.commitTransaction();
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            try {
                VirtualFireAlarmDAOUtil.rollbackTransaction();
            } catch (VirtualFirealarmDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred whilst rolling back the Sensor fetch transaction of " +
                        "VirtualFirealarm [" + deviceIdentifier + "] for sensor [" + sensorIdentifier + "]";
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error in getting connection to the DB or Committing the transaction after fetching Sensor [" +
                    sensorIdentifier + "] of the VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceSensorDAOException e) {
            String msg = "Error while fetching Sensor [" + sensorIdentifier + "] of the VirtualFirealarm with Id [" +
                    deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return sensorTObject;
    }

    @Override
    public List<SensorTransactionObject> getSensors(String deviceIdentifier) throws DeviceManagementException {
        List<SensorTransactionObject> sensorTObjects;
        try {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Fetching sensors for Virtual Firealarm device : " + deviceIdentifier);
            }
            VirtualFireAlarmDAOUtil.beginTransaction();
            sensorTObjects = virtualFirealarmSensorDAO.getSensors(deviceIdentifier);
            VirtualFireAlarmDAOUtil.commitTransaction();
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            try {
                VirtualFireAlarmDAOUtil.rollbackTransaction();
            } catch (VirtualFirealarmDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred whilst rolling back the Sensors fetch transaction of " +
                        "VirtualFirealarm [" + deviceIdentifier + "].";
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error in getting connection to the DB or Committing the transaction after fetching Sensors " +
                    "of the VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceSensorDAOException e) {
            String msg = "Error while fetching Sensors of the VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return sensorTObjects;
    }

    @Override
    public boolean removeSensor(String deviceIdentifier, String sensorIdentifier) throws DeviceManagementException {
        try {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Deleting sensor [" + sensorIdentifier + "] for Virtual Firealarm device : " +
                                deviceIdentifier);
            }
            VirtualFireAlarmDAOUtil.beginTransaction();
            virtualFirealarmSensorDAO.removeSensor(deviceIdentifier, sensorIdentifier);
            VirtualFireAlarmDAOUtil.commitTransaction();
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            try {
                VirtualFireAlarmDAOUtil.rollbackTransaction();
            } catch (VirtualFirealarmDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred whilst rolling back the Sensor remove transaction of " +
                        "VirtualFirealarm [" + deviceIdentifier + "] for sensor [" + sensorIdentifier + "]";
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error in getting connection to the DB or Committing the transaction after removing Sensor [" +
                    sensorIdentifier + "] of the VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceSensorDAOException e) {
            String msg = "Error while removing Sensor [" + sensorIdentifier + "] of the VirtualFirealarm with Id [" +
                    deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return true;
    }

    @Override
    public boolean removeSensors(String deviceIdentifier) throws DeviceManagementException {
        try {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Removing sensors for Virtual Firealarm device : " + deviceIdentifier);
            }
            VirtualFireAlarmDAOUtil.beginTransaction();
            virtualFirealarmSensorDAO.removeSensors(deviceIdentifier);
            VirtualFireAlarmDAOUtil.commitTransaction();
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            try {
                VirtualFireAlarmDAOUtil.rollbackTransaction();
            } catch (VirtualFirealarmDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred whilst rolling back the Sensors remove transaction of " +
                        "VirtualFirealarm [" + deviceIdentifier + "].";
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error in getting connection to the DB or Committing the transaction after removing Sensors " +
                    "of the VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceSensorDAOException e) {
            String msg = "Error while removing Sensors of the VirtualFirealarm with Id [" + deviceIdentifier + "]";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return true;
    }

}
