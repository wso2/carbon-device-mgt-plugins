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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.androidsense.service.impl;

import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature;
import org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util.DeviceData;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@DeviceType(value = "android_sense")
@API(name = "android_sense", version = "1.0.0", context = "/android_sense", tags = {"android_sense"})
public interface AndroidSenseControllerService {

    /**
     * Service to push all the sensor data collected by the Android. Called by the Android device
     *
     * @param dataMsg  The json string containing sensor readings
     */
    @Path("device/sensors")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response addSensorData(DeviceData dataMsg);

    /**
     * End point which is called by Front-end js to get Light sensor readings from the server.
     *
     * @param deviceId The registered device id
     * @return This method returns a SensorRecord object.
     */
    @Path("device/{deviceId}/sensors/light")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "light", name = "Light", description = "Read Light data from the device", type = "monitor")
    Response getLightData(@PathParam("deviceId") String deviceId);

    /**
     * End point which is called by Front-end js to get Battery data from the server.
     *
     * @param deviceId The registered device id
     * @return This method returns a SensorRecord object.
     */
    @Path("device/{deviceId}/sensors/battery")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "battery", name = "Battery", description = "Read Battery data from the device", type = "monitor")
    Response getBattery(@PathParam("deviceId") String deviceId);

    /**
     * End point which is called by Front-end js to get GPS data from the server.
     *
     * @param deviceId The registered device id call to this API.
     * @return This method returns a SensorRecord object.
     */
    @Path("device/{deviceId}/sensors/gps")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "gps", name = "gps", description = "Read GPS data from the device", type = "monitor")
    Response getGPS(@PathParam("deviceId") String deviceId);

    /**
     * End point which is called by Front-end js to get Magnetic data readings from the server.
     *
     * @param deviceId The registered device id
     *                 call to this API.
     * @return This method returns a SensorRecord object.
     */
    @Path("device/{deviceId}/sensors/magnetic")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "magnetic", name = "Magnetic", description = "Read Magnetic data from the device", type = "monitor")
    Response readMagnetic(@PathParam("deviceId") String deviceId);

    /**
     * End point which is called by Front-end js to get Accelerometer data from the server.
     *
     * @param deviceId The registered device id
     * @return This method returns a SensorRecord object.
     */
    @Path("device/{deviceId}/sensors/accelerometer")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "accelerometer", name = "Accelerometer", description = "Read Accelerometer data from the device",
            type = "monitor")
    Response readAccelerometer(@PathParam("deviceId") String deviceId);

    /**
     * End point which is called by Front-end js to get Rotation data from the server.
     *
     * @param deviceId The registered device id
     * @return This method returns a SensorRecord object.
     */
    @Path("device/{deviceId}/sensors/rotation")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "rotation", name = "Rotation", description = "Read Rotational Vector data from the device",
            type = "monitor")
    Response readRotation(@PathParam("deviceId") String deviceId);

    /**
     * End point which is called by Front-end js to get Proximity data from the server.
     *
     * @param deviceId The registered device id
     * @return This method returns a SensorRecord object.
     */
    @Path("device/{deviceId}/sensors/proximity")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "proximity", name = "Proximity", description = "Read Proximity data from the device",
            type = "monitor")
    Response readProximity(@PathParam("deviceId") String deviceId);

    /**
     * End point which is called by Front-end js to get Gyroscope data from the server.
     *
     * @param deviceId The registered device id
     * @return This method returns a SensorRecord object.
     */
    @Path("device/{deviceId}/sensors/gyroscope")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "gyroscope", name = "Gyroscope", description = "Read Gyroscope data from the device",
            type = "monitor")
    Response readGyroscope(@PathParam("deviceId") String deviceId);

    /**
     * End point which is called by Front-end js to get Pressure data from the server.
     *
     * @param deviceId The registered device id
     * @return This method returns a SensorRecord object.
     */
    @Path("device/{deviceId}/sensors/pressure")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "pressure", name = "Pressure", description = "Read Pressure data from the device", type = "monitor")
    Response readPressure(@PathParam("deviceId") String deviceId);

    /**
     * End point which is called by Front-end js to get Gravity data from the server.
     *
     * @param deviceId The registered device id
     * @return This method returns a SensorRecord object.
     */
    @Path("device/{deviceId}/sensors/gravity")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "gravity", name = "Gravity",
            description = "Read Gravity data from the device", type = "monitor")
    Response readGravity(@PathParam("deviceId") String deviceId);

    @Path("device/{deviceId}/sensors/words")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Feature(code = "words", name = "Words", description = "Get the key words and occurrences",
            type = "monitor")
    Response getWords(@PathParam("deviceId") String deviceId, @QueryParam("sessionId") String sessionId);

    /**
     * End point to send the key words to the device
     *
     * @param deviceId The registered device Id.
     * @param keywords The key words to be sent. (Comma separated values)
     */
    @Path("device/{deviceId}/sensors/words")
    @POST
    @Feature(code = "keywords", name = "Add Keywords", description = "Send keywords to the device",
            type = "operation")
    Response sendKeyWords(@PathParam("deviceId") String deviceId, @FormParam("keywords") String keywords);

    /**
     * End point to send the key words to the device
     *
     * @param deviceId  The registered device Id.
     * @param threshold The key words to be sent. (Comma separated values)
     */
    @Path("device/{deviceId}/sensors/words/threshold")
    @POST
    @Feature(code = "threshold", name = "Add a Threshold", description = "Set a threshold for word in the device",
            type = "operation")
    Response sendThreshold(@PathParam("deviceId") String deviceId, @FormParam("threshold") String threshold);

    @Path("device/{deviceId}/sensors/words")
    @DELETE
    @Feature(code = "remove", name = "Remove Keywords", description = "Remove the keywords",
            type = "operation")
    Response removeKeyWords(@PathParam("deviceId") String deviceId, @QueryParam("words") String words);

    /**
     * Retrieve Sensor data for the device type
     */
    @Path("stats/device/{deviceId}/sensors/{sensorName}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    Response getAndroidSenseDeviceStats(@PathParam("deviceId") String deviceId, @PathParam("sensorName") String sensor,
                                        @QueryParam("from") long from, @QueryParam("to") long to);

}
