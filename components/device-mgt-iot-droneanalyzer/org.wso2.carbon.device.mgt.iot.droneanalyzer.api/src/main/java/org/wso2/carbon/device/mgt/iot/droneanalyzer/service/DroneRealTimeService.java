package org.wso2.carbon.device.mgt.iot.droneanalyzer.service;


import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.service.transport.DroneAnalyzerXMPPConnector;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.service.transport.DroneXMPPConnector;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.service.trasformer.MessageTransformer;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;

@ServerEndpoint("/datastream/drone_status")
public class DroneRealTimeService {

    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneRealTimeService.class);
    private MessageTransformer messageController;
    private DroneAnalyzerXMPPConnector xmppConnector;

    public DroneRealTimeService(){
        messageController = new MessageTransformer();
        xmppConnector = new DroneAnalyzerXMPPConnector(messageController);
        xmppConnector.connectLoginAndSetFilterOnReceiver();
    }

    @OnOpen
    public void onOpen(Session session){
        log.info(session.getId() + " has opened a connection");
        System.out.println(session.getId() + " has opened a connection");
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
                if((messageController !=null) && (!messageController.isEmptyQueue())){
                    String message1 = messageController.getMessage();
                    System.out.println("Out going message :"+ message1);
                    session.getBasicRemote().sendText(message1);
                }
                Thread.sleep(DroneConstants.MINIMUM_TIME_DURATION);
            }
        } catch (IOException ex) {
            log.error(ex.getMessage() + "\n" + ex);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            System.out.println(e.getMessage()+" "+ e);
        }
    }

    @OnClose
    public void onClose(Session session){

        try {
            xmppConnector.disconnect();
            log.info("XMPP connection is disconnected");
            System.out.println("XMPP connection is disconnected");
        }
        catch (Exception e) {
            log.error(e.getMessage() + "\n" + e);
            System.out.println(e.getMessage()+"\n"+ e);
        }
        log.info("Session " + session.getId() + " has ended");
        System.out.println("Session " + session.getId() + " has ended");
    }

    @OnError
    public void onError(Session session, Throwable t) {
        try {
            session.getBasicRemote().sendText("Connection closed");
            xmppConnector.disconnect();
            log.info("XMPP connection is disconnected");
            System.out.println("00XMPP connection is disconnected");
        } catch (Exception e) {
            log.error(e.getMessage()+"\n"+ e);
            System.out.println("00"+ e.getMessage()+"\n"+ e);
        }
        log.info("Session " + session.getId() + " has ended");
        System.out.println("00 Session " + session.getId() + " has ended");
    }

}