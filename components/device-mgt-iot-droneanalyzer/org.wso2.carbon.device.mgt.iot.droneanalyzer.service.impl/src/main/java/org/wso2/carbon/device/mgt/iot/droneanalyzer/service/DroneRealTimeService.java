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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.iot.droneanalyzer.service;


import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.service.transport.DroneXMPPConnector;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.service.trasformer.MessageTransformer;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint("/datastream/drone_status")
public class DroneRealTimeService {

    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneRealTimeService.class);
    public MessageTransformer messageController;
    public DroneXMPPConnector xmppConn;
    Thread mqttStarterThread;

    public DroneRealTimeService(){
        messageController = new MessageTransformer();
        xmppConn = new DroneXMPPConnector(messageController);
        Runnable mqttStarter = new Runnable() {
            @Override
            public void run() {
                try {
                    xmppConn.run();
                } catch (Exception e) {
                    log.error(e.getMessage()+ "\n", e);
                }
            }
        };
        mqttStarterThread = new Thread(mqttStarter);
        //mqttStarterThread.setDaemon(true);
        mqttStarterThread.start();
    }

    @OnOpen
    public void onOpen(Session session){
        log.info(session.getId() + " has opened a connection");
        try {
            session.getBasicRemote().sendText("Connection Established");
        } catch (IOException e) {
            log.error( e.getMessage()+"\n"+ e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session){
        try {
            while(true){
                if(!messageController.isEmptyQueue()){
                    String message1 = messageController.getMessage();
                    session.getBasicRemote().sendText(message1);
                }
                /*if((current_message = messageController.getCurrentMessage())!= null){
                    session.getBasicRemote().sendText( current_message );
                }*/
                Thread.sleep(DroneConstants.MINIMUM_TIME_DURATION);
            }

        } catch (IOException ex) {
            log.error(ex.getMessage() + "\n" + ex);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session){
        try {
            session.getBasicRemote().sendText("Connection closed");
            xmppConn.disconnect();
            log.info("XMPP connection is disconnected");
        } catch (Exception e) {
            log.error(e.getMessage()+"\n"+ e);
        }
        log.info("Session " + session.getId() + " has ended");
    }

}