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

package org.wso2.carbon.device.mgt.iot.androidsense.controller.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.androidsense.controller.service.impl.transport
		.AndroidSenseMQTTConnector;
import org.wso2.carbon.device.mgt.iot.androidsense.controller.service.impl.util.SensorData;
import org.wso2.carbon.device.mgt.iot.androidsense.plugin.constants.AndroidSenseConstants;
import org.wso2.carbon.device.mgt.iot.androidsense.controller.service.impl.util.DeviceData;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The api for
 */
@DeviceType(value = "android_sense")
@API(name = "android_sense", version = "1.0.0", context = "/android_sense")
public class AndroidSenseControllerService {
	@Context  //injected response proxy supporting multiple thread
	private HttpServletResponse response;
	private static Log log = LogFactory.getLog(AndroidSenseControllerService.class);
	//TODO; replace this tenant domain
	private final String SUPER_TENANT = "carbon.super";
	private static AndroidSenseMQTTConnector androidSenseMQTTConnector;

	/**
	 * Fetches the `AndroidSenseMQTTConnector` specific to this Android Sense controller service.
	 *
	 * @return the 'AndroidSenseMQTTConnector' instance bound to the 'AndroidSenseMQTTConnector' variable of
	 * this service.
	 */
	@SuppressWarnings("Unused")
	public AndroidSenseMQTTConnector getAndroidSenseMQTTConnector() {
		return androidSenseMQTTConnector;
	}

	/**
	 * Sets the `AndroidSenseMQTTConnector` variable of this Android Sense controller service.
	 *
	 * @param androidSenseMQTTConnector a 'AndroidSenseMQTTConnector' object that handles all MQTT related
	 *                                      communications of any connected Android Sense device-type
	 */
	@SuppressWarnings("Unused")
	public void setAndroidSenseMQTTConnector(
			final AndroidSenseMQTTConnector androidSenseMQTTConnector) {
		Runnable connector = new Runnable() {
			public void run() {
				if (waitForServerStartup()) {
					return;
				}
				AndroidSenseControllerService.androidSenseMQTTConnector = androidSenseMQTTConnector;
				if (MqttConfig.getInstance().isEnabled()) {
					androidSenseMQTTConnector.connect();
				} else {
					log.warn("MQTT disabled in 'devicemgt-config.xml'. Hence, VirtualFireAlarmMQTTConnector not started.");
				}
			}
		};
		Thread connectorThread = new Thread(connector);
		connectorThread.setDaemon(true);
		connectorThread.start();
	}

	private boolean waitForServerStartup() {
		while (!DeviceManagement.isServerReady()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Service to push all the sensor data collected by the Android. Called by the Android device
	 *
	 * @param dataMsg  The json string containing sensor readings
	 * @param response the HTTP servlet response object received  by default as part of the HTTP
	 *                 call to this API
	 */
	@Path("controller/sensordata")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void pushSensorData(
			final DeviceData dataMsg, @Context HttpServletResponse response) {

		PrivilegedCarbonContext.startTenantFlow();
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		ctx.setTenantDomain("carbon.super", true);
		DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx
				.getOSGiService(DeviceAnalyticsService.class, null);

		SensorData[] sensorData = dataMsg.values;
		String streamDef = null;
		Object payloadData[] = null;
		String sensorName = null;

		for (SensorData sensor : sensorData) {
			if (sensor.key.equals("battery")) {
				streamDef = AndroidSenseConstants.BATTERY_STREAM_DEFINITION;
				payloadData = new Float[]{Float.parseFloat(sensor.value)};
				sensorName = AndroidSenseConstants.SENSOR_BATTERY;
			} else if (sensor.key.equals("GPS")) {
				streamDef = AndroidSenseConstants.GPS_STREAM_DEFINITION;
				String gpsValue = sensor.value;
				String gpsValuesString[] = gpsValue.split(",");
				Float gpsValues[] = new Float[2];
				gpsValues[0] = Float.parseFloat(gpsValuesString[0]);
				gpsValues[1] = Float.parseFloat(gpsValuesString[0]);
				payloadData = gpsValues;
				sensorName = AndroidSenseConstants.SENSOR_GPS;
			} else {

				try {
					int androidSensorId = Integer.parseInt(sensor.key);
					String value = sensor.value;
					String sensorValueString[] = value.split(",");
					Float sensorValues[] = new Float[1];
					switch (androidSensorId) {
						case 1:
							streamDef = AndroidSenseConstants.ACCELEROMETER_STREAM_DEFINITION;
							sensorValues[0] = Float.parseFloat(sensorValueString[0]) *
									Float.parseFloat(sensorValueString[0]) * Float
									.parseFloat(sensorValueString[0]);
							payloadData = sensorValues;
							sensorName = AndroidSenseConstants.SENSOR_ACCELEROMETER;
							break;
						case 2:
							streamDef = AndroidSenseConstants.MAGNETIC_STREAM_DEFINITION;
							sensorValues[0] = Float.parseFloat(sensorValueString[0]) *
									Float.parseFloat(sensorValueString[0]) * Float
									.parseFloat(sensorValueString[0]);
							payloadData = sensorValues;
							sensorName = AndroidSenseConstants.SENSOR_MAGNETIC;
							break;
						case 4:
							streamDef = AndroidSenseConstants.GYROSCOPE_STREAM_DEFINITION;
							sensorValues[0] = Float.parseFloat(sensorValueString[0]) *
									Float.parseFloat(sensorValueString[0]) * Float
									.parseFloat(sensorValueString[0]);
							payloadData = sensorValues;
							sensorName = AndroidSenseConstants.SENSOR_GYROSCOPE;
							break;
						case 5:
							streamDef = AndroidSenseConstants.LIGHT_STREAM_DEFINITION;
							sensorName = AndroidSenseConstants.SENSOR_LIGHT;
							payloadData = new Float[]{Float.parseFloat(sensorValueString[0])};
							break;
						case 6:
							streamDef = AndroidSenseConstants.PRESSURE_STREAM_DEFINITION;
							sensorName = AndroidSenseConstants.SENSOR_PRESSURE;
							payloadData = new Float[]{Float.parseFloat(sensorValueString[0])};
							break;
						case 8:
							streamDef = AndroidSenseConstants.PROXIMITY_STREAM_DEFINITION;
							sensorName = AndroidSenseConstants.SENSOR_PROXIMITY;
							payloadData = new Float[]{Float.parseFloat(sensorValueString[0])};
							break;
						case 9:
							streamDef = AndroidSenseConstants.GRAVITY_STREAM_DEFINITION;
							sensorValues[0] = Float.parseFloat(sensorValueString[0]) *
									Float.parseFloat(sensorValueString[0]) * Float
									.parseFloat(sensorValueString[0]);
							payloadData = sensorValues;
							sensorName = AndroidSenseConstants.SENSOR_GRAVITY;
							break;
						case 11:
							streamDef = AndroidSenseConstants.ROTATION_STREAM_DEFINITION;
							sensorValues[0] = Float.parseFloat(sensorValueString[0]) *
									Float.parseFloat(sensorValueString[0]) * Float
									.parseFloat(sensorValueString[0]);
							payloadData = sensorValues;
							sensorName = AndroidSenseConstants.SENSOR_ROTATION;
							break;
					}
				} catch (NumberFormatException e) {
					log.error("Invalid sensor value is sent from the device");
					continue;
				}

			}
			Object metaData[] = {dataMsg.owner, AndroidSenseConstants.DEVICE_TYPE, dataMsg
					.deviceId,
					sensor.time};
			if (streamDef != null && payloadData != null && payloadData.length > 0) {
				try {
					SensorDataManager.getInstance()
							.setSensorRecord(dataMsg.deviceId, sensorName, sensor.value,
											 sensor.time);
					deviceAnalyticsService.publishEvent(streamDef, "1.0.0", metaData, new
																Object[0],
														payloadData);
				} catch (DataPublisherConfigurationException e) {
					response.setStatus(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
				}
			}

		}

	}


	/**
	 * End point which is called by Front-end js to get Light sensor readings from the server.
	 *
	 * @param owner    The device owner
	 * @param deviceId The registered device id
	 * @param response the HTTP servlet response object received  by default as part of the HTTP
	 *                 call to this API.
	 * @return This method returns a SensorRecord object.
	 */
	//TODO below endpoints needs to be removed and cep websocket have to be added
	@Path("controller/readlight")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	@Feature(code = "readlight", name = "Light", description = "Read Light data from the device",
			 type = "monitor")
	public SensorRecord readLight(@HeaderParam("owner") String owner,
								  @HeaderParam("deviceId") String deviceId,
								  @Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;

		try {
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
														AndroidSenseConstants.SENSOR_LIGHT);
			response.setStatus(Response.Status.OK.getStatusCode());
		} catch (DeviceControllerException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return sensorRecord;
	}

	/**
	 * End point which is called by Front-end js to get Battery data from the server.
	 *
	 * @param owner    The device owner
	 * @param deviceId The registered device id
	 * @param response the HTTP servlet response object received  by default as part of the HTTP
	 *                 call to this API.
	 * @return This method returns a SensorRecord object.
	 */
	@Path("controller/readbattery")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	@Feature(code = "readbattery", name = "Battery",
			 description = "Read Battery data from the device",
			 type = "monitor")
	public SensorRecord readBattery(@HeaderParam("owner") String owner,
									@HeaderParam("deviceId") String deviceId,
									@Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;

		try {
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
														AndroidSenseConstants.SENSOR_BATTERY);
			response.setStatus(Response.Status.OK.getStatusCode());
		} catch (DeviceControllerException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return sensorRecord;
	}

	/**
	 * End point which is called by Front-end js to get GPS data from the server.
	 *
	 * @param owner    The device owner
	 * @param deviceId The registered device id
	 * @param response the HTTP servlet response object received  by default as part of the HTTP
	 *                 call to this API.
	 * @return This method returns a SensorRecord object.
	 */
	@Path("controller/readgps")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	@Feature(code = "readgps", name = "gps", description = "Read GPS data from the device",
			 type = "monitor")
	public SensorRecord readGPS(@HeaderParam("owner") String owner,
								@HeaderParam("deviceId") String deviceId,
								@Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;

		try {
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
														AndroidSenseConstants.SENSOR_GPS);
			response.setStatus(Response.Status.OK.getStatusCode());
		} catch (DeviceControllerException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return sensorRecord;
	}

	/**
	 * End point which is called by Front-end js to get Magnetic data readings from the server.
	 *
	 * @param owner    The device owner
	 * @param deviceId The registered device id
	 * @param response the HTTP servlet response object received  by default as part of the HTTP
	 *                 call to this API.
	 * @return This method returns a SensorRecord object.
	 */
	@Path("controller/readmagnetic")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	@Feature(code = "readmagnetic", name = "Magnetic",
			 description = "Read Magnetic data from the device",
			 type = "monitor")
	public SensorRecord readMagnetic(@HeaderParam("owner") String owner,
									 @HeaderParam("deviceId") String deviceId,
									 @Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;

		try {
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
															AndroidSenseConstants.SENSOR_MAGNETIC);
			response.setStatus(Response.Status.OK.getStatusCode());
		} catch (DeviceControllerException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return sensorRecord;
	}

	/**
	 * End point which is called by Front-end js to get Accelerometer data from the server.
	 *
	 * @param owner    The device owner
	 * @param deviceId The registered device id
	 * @param response the HTTP servlet response object received  by default as part of the HTTP
	 *                 call to this API.
	 * @return This method returns a SensorRecord object.
	 */
	@Path("controller/readaccelerometer")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	@Feature(code = "readaccelerometer", name = "Accelerometer",
			 description = "Read Accelerometer data from the " +
					 "device", type = "monitor")
	public SensorRecord readAccelerometer(@HeaderParam("owner") String owner,
										  @HeaderParam("deviceId") String deviceId,
										  @Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;

		try {
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
											AndroidSenseConstants.SENSOR_ACCELEROMETER);
			response.setStatus(Response.Status.OK.getStatusCode());
		} catch (DeviceControllerException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return sensorRecord;
	}

	/**
	 * End point which is called by Front-end js to get Rotation data from the server.
	 *
	 * @param owner    The device owner
	 * @param deviceId The registered device id
	 * @param response the HTTP servlet response object received  by default as part of the HTTP
	 *                 call to this API.
	 * @return This method returns a SensorRecord object.
	 */
	@Path("controller/readrotation")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	@Feature(code = "readrotation", name = "Rotation",
			 description = "Read Rotational Vector data from the device",
			 type = "monitor")
	public SensorRecord readRotation(@HeaderParam("owner") String owner,
									 @HeaderParam("deviceId") String deviceId,
									 @Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;

		try {
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
											AndroidSenseConstants.SENSOR_ROTATION);
			response.setStatus(Response.Status.OK.getStatusCode());
		} catch (DeviceControllerException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return sensorRecord;
	}

	/**
	 * End point which is called by Front-end js to get Proximity data from the server.
	 *
	 * @param owner    The device owner
	 * @param deviceId The registered device id
	 * @param response the HTTP servlet response object received  by default as part of the HTTP
	 *                 call to this API.
	 * @return This method returns a SensorRecord object.
	 */
	@Path("controller/readproximity")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	@Feature(code = "readproximity", name = "Proximity",
			 description = "Read Proximity data from the device",
			 type = "monitor")
	public SensorRecord readProximity(@HeaderParam("owner") String owner,
									  @HeaderParam("deviceId") String deviceId,
									  @Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;

		try {
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
															AndroidSenseConstants.SENSOR_PROXIMITY);
			response.setStatus(Response.Status.OK.getStatusCode());
		} catch (DeviceControllerException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return sensorRecord;
	}

	/**
	 * End point which is called by Front-end js to get Gyroscope data from the server.
	 *
	 * @param owner    The device owner
	 * @param deviceId The registered device id
	 * @param response the HTTP servlet response object received  by default as part of the HTTP
	 *                 call to this API.
	 * @return This method returns a SensorRecord object.
	 */
	@Path("controller/readgyroscope")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	@Feature(code = "readgyroscope", name = "Gyroscope",
			 description = "Read Gyroscope data from the device",
			 type = "monitor")
	public SensorRecord readGyroscope(@HeaderParam("owner") String owner,
									  @HeaderParam("deviceId") String deviceId,
									  @Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;

		try {
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
															AndroidSenseConstants.SENSOR_GYROSCOPE);
			response.setStatus(Response.Status.OK.getStatusCode());
		} catch (DeviceControllerException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return sensorRecord;
	}

	/**
	 * End point which is called by Front-end js to get Pressure data from the server.
	 *
	 * @param owner    The device owner
	 * @param deviceId The registered device id
	 * @param response the HTTP servlet response object received  by default as part of the HTTP
	 *                 call to this API.
	 * @return This method returns a SensorRecord object.
	 */
	@Path("controller/readpressure")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	@Feature(code = "readpressure", name = "Pressure",
			 description = "Read Pressure data from the device", type = "monitor")
	public SensorRecord readPressure(@HeaderParam("owner") String owner,
									 @HeaderParam("deviceId") String deviceId,
									 @Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;

		try {
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
															AndroidSenseConstants.SENSOR_PRESSURE);
			response.setStatus(Response.Status.OK.getStatusCode());
		} catch (DeviceControllerException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return sensorRecord;
	}

	/**
	 * End point which is called by Front-end js to get Gravity data from the server.
	 *
	 * @param owner    The device owner
	 * @param deviceId The registered device id
	 * @param response the HTTP servlet response object received  by default as part of the HTTP
	 *                 call to this API.
	 * @return This method returns a SensorRecord object.
	 */
	@Path("controller/readgravity")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	@Feature(code = "readgravity", name = "Gravity",
			 description = "Read Gravity data from the device", type = "monitor")
	public SensorRecord readGravity(@HeaderParam("owner") String owner,
									@HeaderParam("deviceId") String deviceId,
									@Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;

		try {
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
															AndroidSenseConstants.SENSOR_GRAVITY);
			response.setStatus(Response.Status.OK.getStatusCode());
		} catch (DeviceControllerException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		return sensorRecord;
	}

	/**
	 * @param owner
	 * @param deviceId
	 * @param sessionId
	 * @param response
	 * @return
	 */
	@Path("controller/getwords")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	@Feature(code = "getwords", name = "Words", description = "Get the key words and occurrences",
			 type = "monitor")
	public SensorRecord getWords(@HeaderParam("owner") String owner,
								 @HeaderParam("deviceId") String deviceId,
								 @HeaderParam("sessionId") String sessionId,
								 @Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;
		try {
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
													AndroidSenseConstants.SENSOR_WORDCOUNT);
			response.setStatus(Response.Status.OK.getStatusCode());
		} catch (DeviceControllerException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}
		return sensorRecord;
	}

	/**
	 * End point to send the key words to the device
	 *
	 * @param owner    The device owner.
	 * @param deviceId The registered device Id.
	 * @param keywords The key words to be sent. (Comma separated values)
	 * @param response the HTTP servlet response object received  by default as part of the HTTP
	 *                 call to this API
	 */
	@Path("controller/words")
	@POST
	@Feature(code = "keywords", name = "Add Keywords", description = "Send keywords to the device",
			 type = "operation")
	public void sendKeyWords(@HeaderParam("owner") String owner,
							 @HeaderParam("deviceId") String deviceId,
							 @HeaderParam("protocol") String protocol,
							 @FormParam("keywords") String keywords,
							 @Context HttpServletResponse response) {
		try {
			androidSenseMQTTConnector.publishDeviceData(owner, deviceId, "add", keywords);
			response.setStatus(Response.Status.OK.getStatusCode());
		} catch (TransportHandlerException e) {
			log.error(e);
			response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
		}
	}

	/**
	 * End point to send the key words to the device
	 *
	 * @param owner     The device owner.
	 * @param deviceId  The registered device Id.
	 * @param threshold The key words to be sent. (Comma separated values)
	 * @param response  the HTTP servlet response object received  by default as part of the HTTP
	 *                  call to this API
	 */
	@Path("controller/words/threshold")
	@POST
	@Feature(code = "threshold", name = "Add a Threshold", description = "Set a threshold for word in the device",
			 type = "operation")
	public void sendThreshold(@HeaderParam("owner") String owner,
							  @HeaderParam("deviceId") String deviceId,
							  @HeaderParam("protocol") String protocol,
							  @FormParam("threshold") String threshold,
							  @Context HttpServletResponse response) {
		try {
			androidSenseMQTTConnector.publishDeviceData(owner, deviceId, "threshold", threshold);
			response.setStatus(Response.Status.OK.getStatusCode());
		} catch (TransportHandlerException e) {
			log.error(e);
			response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
		}
	}

	@Path("controller/words/remove")
	@POST
	@Feature(code = "remove", name = "Remove Keywords", description = "Remove the keywords",
			 type = "operation")
	public void removeKeyWords(@HeaderParam("owner") String owner,
							   @HeaderParam("deviceId") String deviceId,
							   @HeaderParam("protocol") String protocol,
							   @FormParam("words") String words,
							   @Context HttpServletResponse response) {
		try {
			androidSenseMQTTConnector.publishDeviceData(owner, deviceId, "remove", words);
			response.setStatus(Response.Status.OK.getStatusCode());
		} catch (TransportHandlerException e) {
			log.error(e);
			response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
		}
	}

	/**
	 * Retrieve Sensor data for the device type
	 *
	 * @param deviceId
	 * @param sensor
	 * @param user
	 * @param from
	 * @param to
	 * @return
	 */
	@Path("controller/stats/device/{deviceId}/sensors/{sensorName}")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	public SensorData[] getAndroidSenseDeviceStats(@PathParam("deviceId") String deviceId,
												   @PathParam("sensorName") String sensor,
												   @QueryParam("username") String user,
												   @QueryParam("from") long from,
												   @QueryParam("to") long to) {

		String fromDate = String.valueOf(from);
		String toDate = String.valueOf(to);

		List<SensorData> sensorDatas = new ArrayList<>();
		PrivilegedCarbonContext.startTenantFlow();
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		//TODO - get the JWT from api manager.
		ctx.setTenantDomain("carbon.super", true);
		DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx
				.getOSGiService(DeviceAnalyticsService.class, null);
		String query = "owner:" + user + " AND deviceId:" + deviceId + " AND deviceType:" +
				AndroidSenseConstants.DEVICE_TYPE + " AND time : [" + fromDate + " TO " + toDate +
				"]";

		if (sensor.equals(AndroidSenseConstants.SENSOR_WORDCOUNT)) {
			query = "owner:" + user + " AND deviceId:" + deviceId;
		}


		String sensorTableName = getSensorEventTableName(sensor);
		try {
			List<Record> records = deviceAnalyticsService.getAllEventsForDevice(sensorTableName,
																				query);
			if (sensor.equals(AndroidSenseConstants.SENSOR_WORDCOUNT)) {
				for (Record record : records) {
					SensorData sensorData = new SensorData();
					sensorData.setKey((String)record.getValue("word"));
					sensorData.setTime((long) record.getValue("occurence"));
					sensorData.setValue((String) record.getValue("sessionId"));
					sensorDatas.add(sensorData);
				}
			} else {
				Collections.sort(records, new Comparator<Record>() {
					@Override
					public int compare(Record o1, Record o2) {
						long t1 = (Long) o1.getValue("time");
						long t2 = (Long) o2.getValue("time");
						if (t1 < t2) {
							return -1;
						} else if (t1 > t2) {
							return 1;
						} else {
							return 0;
						}
					}
				});
				for (Record record : records) {
					SensorData sensorData = new SensorData();
					sensorData.setTime((long) record.getValue("time"));
					sensorData.setValue("" + (float) record.getValue(sensor));
					sensorDatas.add(sensorData);
				}
			}
			return sensorDatas.toArray(new SensorData[sensorDatas.size()]);
		} catch (AnalyticsException e) {
			String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " +
							query;
			log.error(errorMsg);
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return sensorDatas.toArray(new SensorData[sensorDatas.size()]);
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}

	/**
	 * get the event table from the sensor name.
	 * TODO : this needs to be managed with sensor management.
	 *
	 * @param sensorName
	 * @return
	 */
	private String getSensorEventTableName(String sensorName) {
		String sensorEventTableName;
		switch (sensorName) {
			case AndroidSenseConstants.SENSOR_ACCELEROMETER:
				sensorEventTableName = "DEVICE_ACCELEROMETER_SUMMARY";
				break;
			case AndroidSenseConstants.SENSOR_BATTERY:
				sensorEventTableName = "DEVICE_BATTERY_SUMMARY";
				break;
			case AndroidSenseConstants.SENSOR_GPS:
				sensorEventTableName = "DEVICE_GPS_SUMMARY";
				break;
			case AndroidSenseConstants.SENSOR_GRAVITY:
				sensorEventTableName = "DEVICE_GRAVITY_SUMMARY";
				break;
			case AndroidSenseConstants.SENSOR_GYROSCOPE:
				sensorEventTableName = "DEVICE_GYROSCOPE_SUMMARY";
				break;
			case AndroidSenseConstants.SENSOR_LIGHT:
				sensorEventTableName = "DEVICE_LIGHT_SUMMARY";
				break;
			case AndroidSenseConstants.SENSOR_MAGNETIC:
				sensorEventTableName = "DEVICE_MAGNETIC_SUMMARY";
				break;
			case AndroidSenseConstants.SENSOR_PRESSURE:
				sensorEventTableName = "DEVICE_PRESSURE_SUMMARY";
				break;
			case AndroidSenseConstants.SENSOR_PROXIMITY:
				sensorEventTableName = "DevicePROXIMITYSummaryData";
				break;
			case AndroidSenseConstants.SENSOR_ROTATION:
				sensorEventTableName = "DEVICE_ROTATION_SUMMARY";
				break;
			case AndroidSenseConstants.SENSOR_WORDCOUNT:
				sensorEventTableName = "WORD_COUNT_SUMMARY";
				break;
			default:
				sensorEventTableName = "";
		}
		return sensorEventTableName;
	}
}
