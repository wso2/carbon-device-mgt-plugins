package org.wso2.carbon.device.mgt.iot.digitaldisplay.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.api.exception.DigitalDisplayException;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.api.transport.CommunicationHandlerException;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.api.util.DigitalDisplayMqttCommunicationHandler;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.constants.DigitalDisplayConstants;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Created by nuwan on 11/13/15.
 */

public class DigitalDisplayControllerService {


    private static Log log = LogFactory.getLog(DigitalDisplayControllerService.class);

    private static DigitalDisplayMqttCommunicationHandler digitalDisplayMqttCommunicationHandler;

    public void setDigitalDisplayMqttCommunicationHandler(DigitalDisplayMqttCommunicationHandler digitalDisplayMqttCommunicationHandler){
        DigitalDisplayControllerService.digitalDisplayMqttCommunicationHandler = digitalDisplayMqttCommunicationHandler;

        digitalDisplayMqttCommunicationHandler.connect();

    }

    public DigitalDisplayMqttCommunicationHandler getDigitalDisplayMqttCommunicationHandler(){
        return DigitalDisplayControllerService.digitalDisplayMqttCommunicationHandler;
    }


    /**
     * Restart the running browser in the given digital display.
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response response type of the method
     */
    @Path("/restart-browser")
    @POST
    public void restartBrowser(@FormParam("deviceId") String deviceId ,
                               @FormParam("owner") String owner,
                               @FormParam("sessionId") String sessionId,
                               @Context HttpServletResponse response){

        log.info("Restrat Browser : " + deviceId);

        try {
            sendCommandViaMQTT(owner,deviceId,sessionId +"::" + DigitalDisplayConstants.RESTART_BROWSER_CONSTANT + ":","");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        }

    }

    /**
     * Close the running browser in the given digital display.
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response response type of the method
     */
    @Path("/close-browser")
    @POST
    public void closeBrowser(@FormParam("deviceId") String deviceId,
                             @FormParam("owner") String owner,
                             @FormParam("sessionId") String sessionId,
                             @Context HttpServletResponse response){

        log.info("Close Browser : " + deviceId);

        try {
            sendCommandViaMQTT(owner,deviceId,sessionId +"::" + DigitalDisplayConstants.CLOSE_BROWSER_CONSTANT + ":" ,"");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Terminate all running processes. If this execute we have to reboot digital display manually.
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response response type of the method
     */
    @Path("/terminate-display")
    @POST
    public void terminateDisplay(@FormParam("deviceId") String deviceId,
                                 @FormParam("owner") String owner,
                                 @FormParam("sessionId") String sessionId,
                                 @Context HttpServletResponse response){

        log.info("Terminate Display : " + deviceId);

        try {
            sendCommandViaMQTT(owner,deviceId,sessionId +"::" + DigitalDisplayConstants.TERMINATE_DISPLAY_CONSTANT +":" ,"");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Restart python server in given digital display
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response response type of the method
     */
    @Path("/restart-display")
    @POST
    public void restartDisplay(@FormParam("deviceId") String deviceId,
                               @FormParam("owner") String owner,
                               @FormParam("sessionId") String sessionId,
                               @Context HttpServletResponse response){

        log.info("Restrat Display : " + deviceId);

        try {
            sendCommandViaMQTT(owner,deviceId,sessionId +"::" + DigitalDisplayConstants.RESTART_DISPLAY_CONSTANT + ":" ,"");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Search through the sequence and edit requested resource
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response response type of the method
     * @param path page no need to change
     * @param attribute this can be path,time or type
     * @param newValue page is used to replace path
     */
    @Path("/edit-content")
    @POST
    public void editContent(@FormParam("deviceId") String deviceId,
                            @FormParam("owner") String owner,
                            @FormParam("path") String path,
                            @FormParam("attribute") String attribute,
                            @FormParam("new-value") String newValue,
                            @FormParam("sessionId") String sessionId,
                            @Context HttpServletResponse response){

        log.info("Edit Content Display Id - " + deviceId + " by " + owner);

        try {
            String params = path + "|" + attribute + "|" + newValue;
            sendCommandViaMQTT(owner,deviceId,sessionId + "::" + DigitalDisplayConstants.EDIT_SEQUENCE_CONSTANT +":" ,params);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Add new resource end to the existing sequence
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response response type of the method
     * @param type type of new resource
     * @param time new resource visible time
     * @param path URL of the new resource
     */
    @Path("/add-resource")
    @POST
    public void addNewResource(@FormParam("deviceId") String deviceId,
                               @FormParam("owner") String owner,
                               @FormParam("type") String type,
                               @FormParam("time") String time,
                               @FormParam("path") String path,
                               @FormParam("sessionId") String sessionId,
                               @Context HttpServletResponse response){

        log.info("Add Sequence : " + deviceId);

        try {
            String params = type + "|" + time + "|" + path;
            sendCommandViaMQTT(owner,deviceId,sessionId + "::" +
                            DigitalDisplayConstants.ADD_NEW_RESOURCE_CONSTANT +":" ,params);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    /**
     * Add new resource to sequence before given page no
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response response type of the method
     * @param type type of the new resource
     * @param time new resource visible time
     * @param path URL of the new resource
     * @param nextPage next page no of after adding new resource
     */
    @Path("/add-resource-before")
    @POST
    public void addNewResourceBefore(@FormParam("deviceId") String deviceId,
                               @FormParam("owner") String owner,
                               @FormParam("sessionId") String sessionId,
                               @FormParam("type") String type,
                               @FormParam("time") String time,
                               @FormParam("path") String path,
                               @FormParam("next-page") String nextPage,
                               @Context HttpServletResponse response){

        log.info("Add Sequence : " + deviceId);

        try {
            String params = type + "|" + time + "|" + path +
                    "|" + "before=" + nextPage;
            sendCommandViaMQTT(owner,deviceId,sessionId + "::" +
                    DigitalDisplayConstants.ADD_NEW_RESOURCE_CONSTANT +":" ,params);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }


    /**
     * Add new resource to sequence after given page
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response response type of the method
     * @param type type of the new resource
     * @param time new resource visible time
     * @param path URL of the new resource
     * @param beforePage before page no of after adding new resource
     */
    @Path("/add-resource-next")
    @POST
    public void addNewResourceAfter(@FormParam("deviceId") String deviceId,
                                     @FormParam("owner") String owner,
                                     @FormParam("type") String type,
                                     @FormParam("time") String time,
                                     @FormParam("path") String path,
                                     @FormParam("before-page") String beforePage,
                                     @FormParam("sessionId") String sessionId,
                                     @Context HttpServletResponse response){

        log.info("Add Sequence : " + deviceId);

        try {
            String params = type + "|" + time + "|" + path +
                    "|" + "after=" + beforePage;
            sendCommandViaMQTT(owner,deviceId,sessionId + "::" +
                    DigitalDisplayConstants.ADD_NEW_RESOURCE_CONSTANT + ":",params);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    /**
     * Delete a resource in sequence
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response response type of the method
     * @param path path of the page no need to delete
     */
    @Path("/remove-resource")
    @POST
    public void removeResource(@FormParam("deviceId") String deviceId,
                               @FormParam("owner") String owner,
                               @FormParam("path") String path,
                               @FormParam("sessionId") String sessionId,
                               @Context HttpServletResponse response){

        log.info("Remove Resource : " + deviceId);

        try {
            sendCommandViaMQTT(owner,deviceId,sessionId + "::" +
                    DigitalDisplayConstants.REMOVE_RESOURCE_CONSTANT + ":",path);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Remove directory and whole content
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response response type of the method
     * @param directoryName path of the folder need to delete
     */
    @Path("/remove-directory")
    @POST
    public void removeDirectory(@FormParam("directory-name") String directoryName,
                              @FormParam("deviceId") String deviceId ,
                              @FormParam("owner") String owner,
                              @FormParam("sessionId") String sessionId,
                              @Context HttpServletResponse response){

        log.info("Remove Directory : " + deviceId);
        try {
            sendCommandViaMQTT(owner,deviceId,sessionId + "::" +
                    DigitalDisplayConstants.REMOVE_DIRECTORY_CONSTANT + ":",directoryName);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Remove content from www folder
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param directoryName path of directory of request file contain
     * @param content file name of need to delete
     * @param response response type of the method
     */
    @Path("/remove-content")
    @POST
    public void removeContent(@FormParam("directory-name") String directoryName,
                              @FormParam("content") String content,
                              @FormParam("deviceId") String deviceId ,
                              @FormParam("owner") String owner,
                              @FormParam("sessionId") String sessionId,
                              @Context HttpServletResponse response){

        log.info("Remove Content : " + deviceId);
        try {
            String param = directoryName + "|" + content;
            sendCommandViaMQTT(owner,deviceId,sessionId + "::" + DigitalDisplayConstants.REMOVE_CONTENT_CONSTANT + ":",param);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Stop specific display
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response response type of the method
     */
    @Path("/shutdown-display")
    @POST
    public void shutDownDisplay(@FormParam("deviceId") String deviceId,
                                @FormParam("owner") String owner,
                                @FormParam("sessionId") String sessionId,
                                @Context HttpServletResponse response){

        log.info("Shut down display : " + deviceId);

        try {
            sendCommandViaMQTT(owner,deviceId,sessionId + "::" + DigitalDisplayConstants.SHUTDOWN_DISPLAY_CONSTANT + ":" ,"");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Check specific digital display power ON of OFF
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param sessionId web socket id of the method invoke client
     * @param response response type of the method
     */
    @Path("/get-status")
    @GET
    public void getStatus(@FormParam("deviceId") String deviceId,
                          @FormParam("owner") String owner,
                          @FormParam("sessionId") String sessionId,
                          @Context HttpServletResponse response){

        log.info("Status : " + deviceId);

        try {
            sendCommandViaMQTT(owner,deviceId,sessionId + ":" + DigitalDisplayConstants.GET_STATUS_CONSTANT,"");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
             log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Send message via MQTT protocol
     *
     * @param deviceOwner owner of target digital display
     * @param deviceId id of the target digital display
     * @param operation operation need to execute
     * @param param parameters need to given operation
     * @throws DeviceManagementException
     * @throws DigitalDisplayException
     */
    private void sendCommandViaMQTT(String deviceOwner, String deviceId, String operation,
                                       String param) throws DeviceManagementException, DigitalDisplayException {

        log.info(deviceOwner);
        String topic = String.format(DigitalDisplayConstants.PUBLISH_TOPIC , deviceOwner , deviceId);
        String payload = operation + ":" + param;

        try {
            digitalDisplayMqttCommunicationHandler.publishToDigitalDisplay(topic, payload, 2, true);
        } catch (CommunicationHandlerException e) {
            String errorMessage = "Error publishing data to device with ID " + deviceId;
            throw new DigitalDisplayException(errorMessage,e);
        }
    }

}
