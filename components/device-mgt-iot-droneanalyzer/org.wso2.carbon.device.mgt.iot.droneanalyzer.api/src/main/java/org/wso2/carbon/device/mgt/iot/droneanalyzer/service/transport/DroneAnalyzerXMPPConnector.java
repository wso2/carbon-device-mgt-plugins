package org.wso2.carbon.device.mgt.iot.droneanalyzer.service.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jivesoftware.smack.packet.Message;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConnector;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.service.trasformer.MessageTransformer;

public class DroneAnalyzerXMPPConnector extends XmppConnector {
    private static Log log = LogFactory.getLog(DroneAnalyzerXMPPConnector.class);

    private static String xmppServerIP;
    private static int xmppServerPort;
    private static String xmppAdminUsername;
    private static String xmppAdminPassword;
    private static String xmppAdminAccountJID;
    private MessageTransformer messageTransformer;

    public DroneAnalyzerXMPPConnector(MessageTransformer messageTransformer) {
        super(XmppConfig.getInstance().getXmppServerIP(),
                XmppConfig.getInstance().getSERVER_CONNECTION_PORT());
        this.messageTransformer = messageTransformer;

    }

    public void initConnector() {
        xmppServerIP = XmppConfig.getInstance().getXmppServerIP();
        xmppAdminUsername = XmppConfig.getInstance().getXmppUsername();
        xmppAdminPassword = XmppConfig.getInstance().getXmppPassword();
        xmppAdminAccountJID = xmppAdminUsername + "@" + xmppServerIP;
    }

    public void connectAndLogin() {
        try {
                super.connectAndLogin(xmppAdminUsername, xmppAdminPassword, null);
                super.setMessageFilterOnReceiver(xmppAdminAccountJID);
        } catch (DeviceManagementException e) {
            log.error("Connect/Login attempt to XMPP Server at: " + xmppServerIP + " failed");
            retryXMPPConnection();
        }
    }

    public void setMessageFilterOnReceiver(String receiver){
        super.setMessageFilterOnReceiver( receiver+ "@" + xmppServerIP);
    }

    public void connectLoginAndSetFilterOnReceiver(){
        initConnector();
        connectAndLogin();
        setMessageFilterOnReceiver(DroneConstants.DEVICE_ID);
    }

    public void disconnect(){
        super.closeConnection();
    }

    /*public void printRoster(XmppConnector xmppConnection) throws Exception {
        if(xmppConnection != null){
            Roster roster = xmppConnection.getRoster();
            if(roster !=null && roster.getEntries() != null){
                Collection<RosterEntry> entries = roster.getEntries();
                for (RosterEntry entry : entries) {
                    System.out.println(String.format("Buddy:%1$s - Status:%2$s",
                            entry.getName(), entry.getStatus()));
                }
            }

        }else{
            System.out.println("There are no users");
        }
    }*/

    @Override
    protected void processXMPPMessage(Message xmppMessage) {
        String from = xmppMessage.getFrom();
        String subject = xmppMessage.getSubject();
        String inbound_message = xmppMessage.getBody();
        //System.out.println("inbound message :"+inbound_message);
        int indexOfAt = from.indexOf("@");
        int indexOfSlash = from.indexOf("/");
        String deviceId = from.substring(0, indexOfAt);
        String resource = from.substring(indexOfSlash + 1, from.length());



        if ((inbound_message != null)&&(resource.equals(DroneConstants.MESSAGE_RESOURCE)) ){
            messageTransformer.messageTranslater(inbound_message);
        }
        else {
            log.error("Message is empty or it is not belongs to "+ DroneConstants.DEVICE_ID);
            System.out.println("Message is empty or it is not belongs to "+ DroneConstants.DEVICE_ID);
        }
    }

    private void retryXMPPConnection() {
        Thread retryToConnect = new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (!isConnected()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Re-trying to reach XMPP Server....");
                        }
                        try {
                            DroneAnalyzerXMPPConnector.super.connectAndLogin(xmppAdminUsername, xmppAdminPassword, null);
                            DroneAnalyzerXMPPConnector.super.setMessageFilterOnReceiver(xmppAdminAccountJID);
                        } catch (DeviceManagementException e1) {
                            if (log.isDebugEnabled()) {
                                log.debug("Attempt to re-connect to XMPP-Server failed");
                            }
                        }
                    } else {
                        break;
                    }

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        log.error("XMPP: Thread Sleep Interrupt Exception");
                    }
                }
            }
        };
        retryToConnect.setDaemon(true);
        retryToConnect.start();
    }
}
