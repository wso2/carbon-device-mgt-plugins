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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.digitaldisplay.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.api.exception.DigitalDisplayException;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.api.util.DigitalDisplayMqttCommunicationHandler;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.constants.DigitalDisplayConstants;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;


@API(name = "digital_display", version = "1.0.0", context = "/digital_display")
@DeviceType(value = "digital_display")
public class DigitalDisplayControllerService {


    private static Log log = LogFactory.getLog(DigitalDisplayControllerService.class);

    private static DigitalDisplayMqttCommunicationHandler digitalDisplayMqttCommunicationHandler;

    public DigitalDisplayMqttCommunicationHandler getDigitalDisplayMqttCommunicationHandler() {
        return DigitalDisplayControllerService.digitalDisplayMqttCommunicationHandler;
    }

    public void setDigitalDisplayMqttCommunicationHandler(
            DigitalDisplayMqttCommunicationHandler digitalDisplayMqttCommunicationHandler) {
        DigitalDisplayControllerService.digitalDisplayMqttCommunicationHandler = digitalDisplayMqttCommunicationHandler;
        if (MqttConfig.getInstance().isEnabled()) {
            digitalDisplayMqttCommunicationHandler.connect();
        } else {
            log.warn("MQTT disabled in 'devicemgt-config.xml'. " +
                             "Hence, DigitalDisplayMqttCommunicationHandler not started.");
        }
    }

    /**
     * Restart the running browser in the given digital display.
     *
     * @param deviceId  id of the controlling digital display
     * @param owner     owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response  response type of the method
     */
    @Path("/restart-browser")
    @POST
    @Feature(code = "restart-browser", name = "Restart Browser", type="operation",
            description = "Restart Browser in Digital Display")
    public void restartBrowser(@HeaderParam("deviceId") String deviceId,
                               @HeaderParam("owner") String owner,
                               @HeaderParam("sessionId") String sessionId,
                               @Context HttpServletResponse response) {

        try {
            sendCommandViaMQTT(owner, deviceId, sessionId + "::" + DigitalDisplayConstants.RESTART_BROWSER_CONSTANT + ":", "");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        } catch (DigitalDisplayException e) {
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        }

    }

    /**
     * Close the running browser in the given digital display.
     *
     * @param deviceId  id of the controlling digital display
     * @param owner     owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response  response type of the method
     */
    @Path("/close-browser")
    @POST
    @Feature(code = "close-browser", name = "Close Browser", type="operation",
            description = "Close Browser in Digital Display")
    public void closeBrowser(@HeaderParam("deviceId") String deviceId,
                             @HeaderParam("owner") String owner,
                             @HeaderParam("sessionId") String sessionId,
                             @Context HttpServletResponse response) {

        try {
            sendCommandViaMQTT(owner, deviceId, sessionId + "::" + DigitalDisplayConstants.CLOSE_BROWSER_CONSTANT + ":", "");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        } catch (DigitalDisplayException e) {
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Terminate all running processes. If this execute we have to reboot digital display manually.
     *
     * @param deviceId  id of the controlling digital display
     * @param owner     owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response  response type of the method
     */
    @Path("/terminate-display")
    @POST
    @Feature(code = "terminate-display", name = "Terminate Display", type="operation",
            description = "Terminate all running process in Digital Display")
    public void terminateDisplay(@HeaderParam("deviceId") String deviceId,
                                 @HeaderParam("owner") String owner,
                                 @HeaderParam("sessionId") String sessionId,
                                 @Context HttpServletResponse response) {

        try {
            sendCommandViaMQTT(owner, deviceId, sessionId + "::" + DigitalDisplayConstants.TERMINATE_DISPLAY_CONSTANT + ":", "");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        } catch (DigitalDisplayException e) {
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Restart python server in given digital display
     *
     * @param deviceId  id of the controlling digital display
     * @param owner     owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response  response type of the method
     */
    @Path("/restart-display")
    @POST
    @Feature(code = "restart-display", name = "Restart Display", type="operation",
            description = "Restart Digital Display")
    public void restartDisplay(@HeaderParam("deviceId") String deviceId,
                               @HeaderParam("owner") String owner,
                               @HeaderParam("sessionId") String sessionId,
                               @Context HttpServletResponse response) {

        try {
            sendCommandViaMQTT(owner, deviceId, sessionId + "::" + DigitalDisplayConstants.RESTART_DISPLAY_CONSTANT + ":", "");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        } catch (DigitalDisplayException e) {
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Search through the sequence and edit requested resource
     *
     * @param deviceId  id of the controlling digital display
     * @param owner     owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response  response type of the method
     * @param path      page no need to change
     * @param attribute this can be path,time or type
     * @param newValue  page is used to replace path
     */
    @Path("/edit-content")
    @POST
    @Feature(code = "edit-content", name = "Edit Content", type="operation",
            description = "Search through the sequence and edit requested resource in Digital Display")
    public void editContent(@HeaderParam("deviceId") String deviceId,
                            @HeaderParam("owner") String owner,
                            @FormParam("path") String path,
                            @FormParam("attribute") String attribute,
                            @FormParam("new-value") String newValue,
                            @HeaderParam("sessionId") String sessionId,
                            @Context HttpServletResponse response) {

        try {
            String params = path + "|" + attribute + "|" + newValue;
            sendCommandViaMQTT(owner, deviceId, sessionId + "::" + DigitalDisplayConstants.EDIT_SEQUENCE_CONSTANT + ":", params);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        } catch (DigitalDisplayException e) {
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Add new resource end to the existing sequence
     *
     * @param deviceId  id of the controlling digital display
     * @param owner     owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response  response type of the method
     * @param type      type of new resource
     * @param time      new resource visible time
     * @param path      URL of the new resource
     */
    @Path("/add-resource")
    @POST
    @Feature(code = "add-resource", name = "Add Resource", type="operation",
            description = "Add new resource end to the existing sequence in Digital Display")
    public void addNewResource(@HeaderParam("deviceId") String deviceId,
                               @HeaderParam("owner") String owner,
                               @FormParam("type") String type,
                               @FormParam("time") String time,
                               @FormParam("path") String path,
                               @FormParam("position") String position,
                               @HeaderParam("sessionId") String sessionId,
                               @Context HttpServletResponse response) {

        String params;
        try {

            if (position.isEmpty()){
                params = type + "|" + time + "|" + path;
            } else {
                params = type + "|" + time + "|" + path +
                         "|" + "after=" + position;
            }
            sendCommandViaMQTT(owner, deviceId, sessionId + "::" +
                                                DigitalDisplayConstants.ADD_NEW_RESOURCE_CONSTANT + ":", params);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        } catch (DigitalDisplayException e) {
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    /**
     * Delete a resource in sequence
     *
     * @param deviceId  id of the controlling digital display
     * @param owner     owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response  response type of the method
     * @param path      path of the page no need to delete
     */
    @Path("/remove-resource")
    @POST
    @Feature(code = "remove-resource", name = "Remove Resource", type="operation",
            description = "Delete a resource from sequence in Digital Display")
    public void removeResource(@HeaderParam("deviceId") String deviceId,
                               @HeaderParam("owner") String owner,
                               @FormParam("path") String path,
                               @HeaderParam("sessionId") String sessionId,
                               @Context HttpServletResponse response) {

        try {
            sendCommandViaMQTT(owner, deviceId, sessionId + "::" +
                                                DigitalDisplayConstants.REMOVE_RESOURCE_CONSTANT + ":", path);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        } catch (DigitalDisplayException e) {
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Remove directory and whole content
     *
     * @param deviceId      id of the controlling digital display
     * @param owner         owner of the digital display
     * @param sessionId     web socket id of the method invoke client
     * @param response      response type of the method
     * @param directoryName path of the folder need to delete
     */
    @Path("/remove-directory")
    @POST
    @Feature(code = "remove-directory", name = "Remove Directory", type="operation",
            description = "Remove directory and whole content in Digital Display")
    public void removeDirectory(@FormParam("directory-name") String directoryName,
                                @HeaderParam("deviceId") String deviceId,
                                @HeaderParam("owner") String owner,
                                @HeaderParam("sessionId") String sessionId,
                                @Context HttpServletResponse response) {

        try {
            sendCommandViaMQTT(owner, deviceId, sessionId + "::" +
                                                DigitalDisplayConstants.REMOVE_DIRECTORY_CONSTANT + ":", directoryName);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        } catch (DigitalDisplayException e) {
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Remove content from www folder
     *
     * @param deviceId      id of the controlling digital display
     * @param owner         owner of the digital display
     * @param sessionId     web socket id of the method invoke client
     * @param directoryName path of directory of request file contain
     * @param content       file name of need to delete
     * @param response      response type of the method
     */
    @Path("/remove-content")
    @POST
    @Feature(code = "remove-content", name = "Remove Content", type="operation",
            description = "Remove content from www folder in Digital Display")
    public void removeContent(@FormParam("directory-name") String directoryName,
                              @FormParam("content") String content,
                              @HeaderParam("deviceId") String deviceId,
                              @HeaderParam("owner") String owner,
                              @HeaderParam("sessionId") String sessionId,
                              @Context HttpServletResponse response) {

        try {
            String param = directoryName + "|" + content;
            sendCommandViaMQTT(owner, deviceId, sessionId + "::" + DigitalDisplayConstants.REMOVE_CONTENT_CONSTANT + ":", param);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        } catch (DigitalDisplayException e) {
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Stop specific display
     *
     * @param deviceId  id of the controlling digital display
     * @param owner     owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response  response type of the method
     */
    @Path("/shutdown-display")
    @POST
    @Feature(code = "shutdown-display", name = "Shut Down", type="operation",
            description = "Stop specific display in Digital Display")
    public void shutDownDisplay(@HeaderParam("deviceId") String deviceId,
                                @HeaderParam("owner") String owner,
                                @HeaderParam("sessionId") String sessionId,
                                @Context HttpServletResponse response) {

        try {
            sendCommandViaMQTT(owner, deviceId, sessionId + "::" + DigitalDisplayConstants.SHUTDOWN_DISPLAY_CONSTANT + ":", "");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        } catch (DigitalDisplayException e) {
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Check specific digital display power ON of OFF
     *
     * @param deviceId  id of the controlling digital display
     * @param owner     owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response  response type of the method
     */
    @Path("/get-status")
    @POST
    @Feature(code = "get-status", name = "Get Status", type="operation",
            description = "Check specific digital display power ON or OFF")
    public void getStatus(@HeaderParam("deviceId") String deviceId,
                          @HeaderParam("owner") String owner,
                          @HeaderParam("sessionId") String sessionId,
                          @Context HttpServletResponse response) {

        try {
            sendCommandViaMQTT(owner, deviceId, sessionId + ":" + DigitalDisplayConstants.GET_STATUS_CONSTANT, "");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        } catch (DigitalDisplayException e) {
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Send message via MQTT protocol
     *
     * @param deviceOwner owner of target digital display
     * @param deviceId    id of the target digital display
     * @param operation   operation need to execute
     * @param param       parameters need to given operation
     * @throws DeviceManagementException
     * @throws DigitalDisplayException
     */
    private void sendCommandViaMQTT(String deviceOwner, String deviceId, String operation,
                                    String param)
            throws DeviceManagementException, DigitalDisplayException {

        log.info(deviceOwner);
        String topic = String.format(DigitalDisplayConstants.PUBLISH_TOPIC, deviceOwner, deviceId);
        String payload = operation + ":" + param;

        try {
            digitalDisplayMqttCommunicationHandler.publishToDigitalDisplay(topic, payload, 2, true);
        } catch (TransportHandlerException e) {
            String errorMessage = "Error publishing data to device with ID " + deviceId;
            throw new DigitalDisplayException(errorMessage, e);
        }
    }

}
