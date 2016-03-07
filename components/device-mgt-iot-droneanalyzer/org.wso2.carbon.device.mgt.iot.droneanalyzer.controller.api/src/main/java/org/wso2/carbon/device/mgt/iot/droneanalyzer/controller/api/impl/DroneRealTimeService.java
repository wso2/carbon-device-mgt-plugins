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
package org.wso2.carbon.device.mgt.iot.droneanalyzer.controller.api.impl;


import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.controller.api.impl.transport.DroneAnalyzerXMPPConnector;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.controller.api.impl.trasformer.MessageTransformer;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint("/datastream/{sessionId}")
public class DroneRealTimeService {

    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneRealTimeService.class);
    private static Map<String, Session> clientSessions = new HashMap<>();
    private MessageTransformer messageController;
    private DroneAnalyzerXMPPConnector xmppConnector;

    public DroneRealTimeService() {
        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                messageController = new MessageTransformer();
                xmppConnector = new DroneAnalyzerXMPPConnector(messageController);
                if (XmppConfig.getInstance().isEnabled()){
                    xmppConnector.connect();
                } else {
                    log.warn("XMPP disabled in 'devicemgt-config.xml'. Hence, DroneAnalyzerXMPPConnector not started.");
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
     * This method will be invoked when a client requests for a
     * WebSocket connection.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession, @PathParam("sessionId") String sessionId){
        log.info(userSession.getId() + " has opened a connection");
        try {
            clientSessions.put(sessionId, userSession);
            userSession.getBasicRemote().sendText("Connection Established");
        } catch (IOException e) {
            log.error( e.getMessage()+"\n"+ e);
        }
    }

    /**
     * This method will be invoked when a client send a message
     * @param message message coming form client
     * @param session the session which is opened.
     */
    @OnMessage
    @SuppressWarnings("InfiniteLoopStatement")
    public void onMessage(String message, Session session){
            while(true){
                try{
                    if((messageController !=null) && (!messageController.isEmptyQueue())){
                        String droneCurrentStatus = messageController.getMessage();
                        session.getBasicRemote().sendText(droneCurrentStatus);
                    }
                    Thread.sleep(DroneConstants.MINIMUM_TIME_DURATION);
                } catch (IOException | InterruptedException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
    }

    /**
     * This method will be invoked when a client closes a WebSocket
     * connection.
     *
     * @param session the session which is opened.
     */
    @OnClose
    public void onClose(Session session){
            xmppConnector.disconnect();
            clientSessions.values().remove(session);
            log.info("XMPP connection is disconnected");
    }

    /**
     * This method will be invoked when connection terminate unexpectedly
     *
     * @param session the session which is opened.
     */
    @OnError
    public void onError(Session session, Throwable t) {
        try {
            clientSessions.values().remove(session);
            xmppConnector.disconnect();
            session.getBasicRemote().sendText("Connection closed");
        } catch (IOException e) {
            log.error("Connection has been corrupted unexpectedly, "+ e);
        }
        log.info("XMPP connection is disconnected");
    }

}