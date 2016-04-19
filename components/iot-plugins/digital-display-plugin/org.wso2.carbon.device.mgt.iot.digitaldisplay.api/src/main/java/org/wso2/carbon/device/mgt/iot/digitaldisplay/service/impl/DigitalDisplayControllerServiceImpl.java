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

package org.wso2.carbon.device.mgt.iot.digitaldisplay.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.service.impl.exception.DigitalDisplayException;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.service.impl.util.DigitalDisplayMQTTConnector;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.plugin.constants.DigitalDisplayConstants;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;

import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

public class DigitalDisplayControllerServiceImpl implements DigitalDisplayControllerService {

    private static Log log = LogFactory.getLog(DigitalDisplayControllerServiceImpl.class);
    private static DigitalDisplayMQTTConnector digitalDisplayMQTTConnector;

    @Path("device/{deviceId}/restart-browser")
    @POST
    public Response restartBrowser(@PathParam("deviceId") String deviceId, @HeaderParam("sessionId") String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.RESTART_BROWSER_CONSTANT + "::", "");
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    @Path("device/{deviceId}/terminate-display")
    @POST
    public Response terminateDisplay(@HeaderParam("sessionId") String sessionId,
                                     @PathParam("deviceId") String deviceId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.TERMINATE_DISPLAY_CONSTANT + "::", "");
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    @Path("device/{deviceId}/restart-display")
    @POST
    public Response restartDisplay(@PathParam("deviceId") String deviceId, @HeaderParam("sessionId") String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.RESTART_DISPLAY_CONSTANT + "::", "");
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    @Path("device/{deviceId}/edit-sequence")
    @POST
    public Response editSequence(@PathParam("deviceId") String deviceId, @FormParam("name") String name,
                                 @FormParam("attribute") String attribute, @FormParam("new-value") String newValue,
                                 @HeaderParam("sessionId") String sessionId) {
        try {
            String params = name + "|" + attribute + "|" + newValue;
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.EDIT_SEQUENCE_CONSTANT + "::", params);
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }


    @Path("device/{deviceId}/upload-content")
    @POST
    public Response uploadContent(@PathParam("deviceId") String deviceId, @FormParam("remote-path") String remotePath,
                                  @FormParam("screen-name") String screenName,
                                  @HeaderParam("sessionId") String sessionId) {
        try {
            String params = remotePath + "|" + screenName;
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.UPLOAD_CONTENT_CONSTANT + "::",
                               params);
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    @Path("device/{deviceId}/add-resource")
    @POST
    public Response addNewResource(@PathParam("deviceId") String deviceId, @FormParam("type") String type,
                                   @FormParam("time") String time, @FormParam("path") String path,
                                   @FormParam("name") String name, @FormParam("position") String position,
                                   @HeaderParam("sessionId") String sessionId) {
        String params;
        try {
            if (position.isEmpty()) {
                params = type + "|" + time + "|" + path + "|" + name;
            } else {
                params = type + "|" + time + "|" + path + "|" + name +
                         "|" + "after=" + position;
            }
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.ADD_NEW_RESOURCE_CONSTANT + "::", params);
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    @Path("device/{deviceId}/remove-resource")
    @POST
    public Response removeResource(@PathParam("deviceId") String deviceId, @FormParam("name") String name,
                                   @HeaderParam("sessionId") String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.REMOVE_RESOURCE_CONSTANT + "::", name);
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    @Path("device/{deviceId}/restart-server")
    @POST
    public Response restartServer(@PathParam("deviceId") String deviceId, @HeaderParam("sessionId") String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.RESTART_SERVER_CONSTANT + "::", "");
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }


    @Path("device/{deviceId}/screenshot")
    @POST
    public Response showScreenshot(@PathParam("deviceId") String deviceId, @HeaderParam("sessionId") String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.SCREENSHOT_CONSTANT + "::", "");
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    @Path("device/{deviceId}/get-device-status")
    @POST
    public Response getDevicestatus(@PathParam("deviceId") String deviceId,
                                    @HeaderParam("sessionId") String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.GET_DEVICE_STATUS_CONSTANT + "::", "");
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    @Path("device/{deviceId}/get-content-list")
    @POST
    public Response getResources(@PathParam("deviceId") String deviceId, @HeaderParam("sessionId") String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.GET_CONTENTLIST_CONSTANT + "::", "");
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    /**
     * Send message via MQTT protocol
     *
     * @param deviceId  id of the target digital display
     * @param operation operation need to execute
     * @param param     parameters need to given operation
     * @throws DeviceManagementException
     * @throws DigitalDisplayException
     */
    private void sendCommandViaMQTT(String deviceId, String operation, String param)
            throws DeviceManagementException, DigitalDisplayException {
        String topic = String.format(DigitalDisplayConstants.PUBLISH_TOPIC, deviceId);
        String payload = operation + param;
        try {
            digitalDisplayMQTTConnector.publishToDigitalDisplay(topic, payload, 2, false);
        } catch (TransportHandlerException e) {
            String errorMessage = "Error publishing data to device with ID " + deviceId;
            throw new DigitalDisplayException(errorMessage, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private boolean waitForServerStartup() {
        while (!IoTServerStartupListener.isServerReady()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                return true;
            }
        }
        return false;
    }

    public DigitalDisplayMQTTConnector getDigitalDisplayMQTTConnector() {
        return DigitalDisplayControllerServiceImpl.digitalDisplayMQTTConnector;
    }

    public void setDigitalDisplayMQTTConnector(final
                                               DigitalDisplayMQTTConnector digitalDisplayMQTTConnector) {

        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                DigitalDisplayControllerServiceImpl.digitalDisplayMQTTConnector = digitalDisplayMQTTConnector;
                if (MqttConfig.getInstance().isEnabled()) {
                    digitalDisplayMQTTConnector.connect();
                } else {
                    log.warn("MQTT disabled in 'devicemgt-config.xml'. " +
                             "Hence, DigitalDisplayMQTTConnector not started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.start();
    }

}
