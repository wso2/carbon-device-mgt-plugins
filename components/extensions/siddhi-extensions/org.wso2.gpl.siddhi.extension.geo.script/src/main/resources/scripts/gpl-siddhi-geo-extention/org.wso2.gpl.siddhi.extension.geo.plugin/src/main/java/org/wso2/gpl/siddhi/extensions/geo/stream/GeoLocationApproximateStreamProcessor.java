/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.gpl.siddhi.extensions.geo.stream;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * geoLocationApproximate(locationRecorder, latitude, longitude, sensorProximity, sensorUUID, sensorWeight, timestamp)
 *
 * This method computed the average location of the locationRecorder using the collection iBeacons which the location
 * recorder resides.
 *
 * locationRecorder - unique id of the object or item
 * latitude         - latitude value of the iBeacon
 * longitude        - longitude value of the iBeacon
 * sensorProximity  - proximity which will be given by the iBeacon (eg: ENTER, RANGE, EXIT)
 * sensorUUID       - unique id of the iBeacon
 * sensorWeight     - weight of the iBeacon which influence the averaging of the location (eg: approximate distance from
 *                    the beacon
 * timestamp        - timestamp of the log which will be used to remove iBeacon from one's collection when there is no
 *                    new log for 5 minutes
 *
 * Accept Type(s) for geoLocationApproximate(locationRecorder, latitude, longitude, sensorProximity, sensorUUID,
 *                                          sensorWeight, timestamp);
 *  locationRecorder : STRING
 *  latitude : DOUBLE
 *  longitude : DOUBLE
 *  sensorProximity : STRING
 *  sensorUUID : STRING
 *  sensorWeight : DOUBLE
 *  timestamp : LONG
 *
 * Return Type(s): DOUBLE, DOUBLE, BOOL
 *
 */
public class GeoLocationApproximateStreamProcessor extends StreamFunctionProcessor {

    //locationRecorder,uuid -> BeaconValueHolder
    private Map<String, Map<String, BeaconValueHolder>>
            personSpecificRecordLocatorMaps;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Object[] currentState() {
        return new Object[]{personSpecificRecordLocatorMaps};
    }

    @Override
    public void restoreState(Object[] state) {
        personSpecificRecordLocatorMaps = (Map<String, Map<String, BeaconValueHolder>>) state[0];
    }

    @Override
    protected Object[] process(Object[] data) {
        if (data[0] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to geo:locationApproximate() " +
                                                    "function. First argument should be string");
        }
        if (data[1] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to geo:locationApproximate() " +
                                                    "function. Second argument should be double");
        }
        if (data[2] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to geo:locationApproximate() " +
                                                    "function. Third argument should be double");
        }
        if (data[3] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to geo:locationApproximate() " +
                                                    "function. Forth argument should be string");
        }
        if (data[4] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to geo:locationApproximate() " +
                                                    "function. Fifth argument should be string");
        }
        if (data[5] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to geo:locationApproximate() " +
                                                    "function. Sixth argument should be double");
        }
        if (data[6] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to geo:locationApproximate() " +
                                                    "function. Seventh argument should be long");
        }

        String locationRecorder = (String) data[0];
        double latitude = (Double) data[1];
        double longitude = (Double) data[2];
        String beaconProximity = (String) data[3];
        String uuid = (String) data[4];
        double weight = (Double) data[5]; //is calculated previously eg: distance
        long timestamp = (Long) data[6];

        if (personSpecificRecordLocatorMaps.get(locationRecorder) == null) {
            personSpecificRecordLocatorMaps.put(locationRecorder, new ConcurrentHashMap<String, BeaconValueHolder>());
        }
        //both "enter" and "range" attributes are there in the logs I retrieve when it comes to cleaning logs
        if ("ENTER".equalsIgnoreCase(beaconProximity) || "RANGE".equalsIgnoreCase(beaconProximity)) {
            if (personSpecificRecordLocatorMaps.get(locationRecorder).containsKey(uuid)) {
                BeaconValueHolder tempBeaconValue = personSpecificRecordLocatorMaps.get(locationRecorder).get(uuid);
                if (tempBeaconValue.getLastUpdatedTime() < timestamp) {
                    BeaconValueHolder beaconValueHolder = new BeaconValueHolder(latitude, longitude, timestamp, weight);
                    personSpecificRecordLocatorMaps.get(locationRecorder).put(uuid, beaconValueHolder);
                }
            } else {
                BeaconValueHolder beaconValueHolder = new BeaconValueHolder(latitude, longitude, timestamp, weight);
                personSpecificRecordLocatorMaps.get(locationRecorder).put(uuid, beaconValueHolder);
            }
        } else {
            if (personSpecificRecordLocatorMaps.get(locationRecorder).containsKey(uuid)) {
                BeaconValueHolder tempBeaconValue = personSpecificRecordLocatorMaps.get(locationRecorder).get(uuid);
                if (tempBeaconValue.getLastUpdatedTime() < timestamp) {
                    personSpecificRecordLocatorMaps.get(locationRecorder).remove(uuid);
                }
            }
        }

        int noOfSensors = personSpecificRecordLocatorMaps.get(locationRecorder).size();
        double sensorValues[][] = new double[noOfSensors][3];
        int actualNoOfSensors = 0;
        double totalWeight = 0;
        for (Map.Entry<String, BeaconValueHolder> beaconLocation : personSpecificRecordLocatorMaps.get(locationRecorder).entrySet()) {
            BeaconValueHolder beaconValueHolder = beaconLocation.getValue();
            long prevTimestamp = beaconValueHolder.getLastUpdatedTime();
            if ((timestamp - prevTimestamp) > 300000) {
                //if there is a beacon which has a log older than 5 minutes, removing the beacon assuming the
                //device has gone away from that beacon
                personSpecificRecordLocatorMaps.get(locationRecorder).remove(beaconLocation.getKey());
            } else {
                sensorValues[actualNoOfSensors][0] = beaconValueHolder.getLatitude();
                sensorValues[actualNoOfSensors][1] = beaconValueHolder.getLongitude();
                sensorValues[actualNoOfSensors][2] = beaconValueHolder.getBeaconDistance();
                totalWeight += beaconValueHolder.getBeaconDistance();
                actualNoOfSensors++;
            }
        }
        if (actualNoOfSensors == 0) {
            return new Object[]{latitude, longitude, false};
        }

        double tempLatitude, tempLongitude;
        double x = 0;
        double y = 0;
        double z = 0;
        for (int i = 0; i < actualNoOfSensors; i++) {
            weight = sensorValues[i][2] / totalWeight;
            tempLatitude = sensorValues[i][0] * Math.PI / 180.0;
            tempLongitude = sensorValues[i][1] * Math.PI / 180.0;
            x += Math.cos(tempLatitude) * Math.cos(tempLongitude) * weight;
            y += Math.cos(tempLatitude) * Math.sin(tempLongitude) * weight;
            z += Math.sin(tempLatitude) * weight;
        }
        longitude = Math.atan2(y, x) * 180 / Math.PI;
        double hyp = Math.sqrt(x * x + y * y);
        latitude = Math.atan2(z, hyp) * 180 / Math.PI;

        return new Object[]{latitude, longitude, true};
    }

    @Override
    protected Object[] process(Object data) {
        return new Object[0];
    }

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors,
                                   ExecutionPlanContext executionPlanContext) {
        personSpecificRecordLocatorMaps = new ConcurrentHashMap<String, Map<String, BeaconValueHolder>>();
        if (attributeExpressionExecutors.length != 7) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to " +
                                                       "geo:locationApproximate() function, " +
                                                       "requires 7, but found " + attributeExpressionExecutors.length);
        }
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(3);
        attributes.add(new Attribute("averagedLatitude", Attribute.Type.DOUBLE));
        attributes.add(new Attribute("averagedLongitude", Attribute.Type.DOUBLE));
        attributes.add(new Attribute("averageExist", Attribute.Type.BOOL));
        return attributes;
    }

    private class BeaconValueHolder {
        private double latitude;
        private double longitude;
        private long lastUpdatedTime;
        private double beaconDistance;

        public BeaconValueHolder(double latitude, double longitude, long lastUpdatedTime, double
                beaconDistance) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.lastUpdatedTime = lastUpdatedTime;
            this.beaconDistance = beaconDistance;
        }

        public long getLastUpdatedTime() {
            return lastUpdatedTime;
        }

        public double getBeaconDistance() {
            return beaconDistance;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}
