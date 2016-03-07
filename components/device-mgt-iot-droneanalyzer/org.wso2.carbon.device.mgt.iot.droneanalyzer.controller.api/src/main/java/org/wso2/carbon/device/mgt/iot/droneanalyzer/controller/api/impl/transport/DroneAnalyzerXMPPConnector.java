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
package org.wso2.carbon.device.mgt.iot.droneanalyzer.controller.api.impl.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.packet.Message;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.controller.api.impl.trasformer.MessageTransformer;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.transport.xmpp.XMPPTransportHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DroneAnalyzerXMPPConnector extends XMPPTransportHandler {
    private static Log log = LogFactory.getLog(DroneAnalyzerXMPPConnector.class);
    private static String xmppAdminUsername;
    private static String xmppAdminPassword;
    private static String xmppAdminAccountJID;
    private MessageTransformer messageTransformer;
    private ScheduledFuture<?> connectorServiceHandler;
    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public DroneAnalyzerXMPPConnector(MessageTransformer messageTransformer) {
        super(XmppConfig.getInstance().getXmppServerIP(),XmppConfig.getInstance().getSERVER_CONNECTION_PORT());
        this.messageTransformer = messageTransformer;
    }

    /**
     * {@inheritDoc}
     * Device-type specific implementation to connect to the XMPP server.
     * This method is called to initiate a MQTT communication.
     */
    @Override
    public void connect() {
        Runnable connector = new Runnable() {
            @Override
            public void run() {
                if (!isConnected()) {
                    try {
                        initConnector();
                        connectToServer();
                        loginToServer(xmppAdminUsername, xmppAdminPassword, null);
                        setFilterOnReceiver(xmppAdminAccountJID);
                    } catch (TransportHandlerException e) {
                        if (log.isDebugEnabled()) {
                            log.warn("Connection/Login to XMPP server at: " + server + " as " +
                                             xmppAdminUsername + " failed for device-type [" +
                                             DroneConstants.DEVICE_TYPE + "].", e);
                        }
                    }
                }
            }
        };
        connectorServiceHandler = service.scheduleAtFixedRate(connector, 0, timeoutInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * Initialize connection with XMPP server
     */
    public void initConnector() {
        String xmppServerIP = XmppConfig.getInstance().getXmppServerIP();
        xmppAdminUsername = XmppConfig.getInstance().getXmppUsername();
        xmppAdminPassword = XmppConfig.getInstance().getXmppPassword();
        xmppAdminAccountJID = xmppAdminUsername + "@" + xmppServerIP;
    }

    /**
     * {@inheritDoc}
     * Device-type specific implementation to process incoming messages. This is the specific
     * method signature of the overloaded "processIncomingMessage" method that gets called from the messageArrived()
     * callback of the "XMPPTransportHandler".
     */
    @Override
    public void processIncomingMessage(Message message) throws TransportHandlerException {
        try{
            String from = message.getFrom();
            String inbound_message = message.getBody();
            int indexOfSlash = from.indexOf("/");
            if(indexOfSlash == 0){
                if(log.isDebugEnabled()){
                    log.debug("Required resource not available.");
                }
            }else{
                String resource = from.substring(indexOfSlash + 1, from.length());
                if ((inbound_message != null)&&(resource.equals(DroneConstants.MESSAGE_RESOURCE)) ){
                    messageTransformer.messageTranslator(inbound_message);
                } else {
                    if(log.isDebugEnabled()){
                        log.debug("Message is empty or it is not belongs to " + xmppAdminUsername);
                    }
                }
            }
        }catch(ArrayIndexOutOfBoundsException e){
            log.error("Wrong message format: input message", e);
        }catch(RuntimeException e){
            log.error("Unexpected error has been occurred, ", e);
        }
    }

    /**
     * {@inheritDoc}
     * Device-type specific implementation to publish data to the device.
     */
    @Override
    public void publishDeviceData(String... publishData) throws TransportHandlerException {
        String xmppJID = publishData[0];
        String xmppMessage = publishData[1];
        String xmppSubject = publishData[2];
        sendXMPPMessage(xmppJID, xmppMessage, xmppSubject);
    }

    /**
     * Disconnect from XMPP server
     */
    public void disconnect(){
        Runnable stopConnection = new Runnable() {
            public void run() {
                while (isConnected()) {
                    connectorServiceHandler.cancel(true);
                    closeConnection();
                    if (log.isDebugEnabled()) {
                        log.warn("Unable to 'STOP' connection to XMPP server at: " + server +
                                         " for user - " + xmppAdminUsername);
                    }
                    try {
                        Thread.sleep(timeoutInterval);
                    } catch (InterruptedException e1) {
                        log.error("XMPP-Terminator: Thread Sleep Interrupt Exception for "
                                          + DroneConstants.DEVICE_TYPE + " type.", e1);
                    }
                }
            }
        };
        Thread terminatorThread = new Thread(stopConnection);
        terminatorThread.setDaemon(true);
        terminatorThread.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processIncomingMessage() throws TransportHandlerException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processIncomingMessage(Message message, String... messageParams) throws TransportHandlerException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishDeviceData() throws TransportHandlerException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishDeviceData(Message publishData) throws TransportHandlerException {

    }
}
