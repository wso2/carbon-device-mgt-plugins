package org.wso2.carbon.device.mgt.iot.droneanalyzer.service.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.service.trasformer.MessageTransformer;


import java.util.Collection;

/**
 * Created by geesara on 12/7/15.
 */
public class DroneXMPPConnector {

    private static Log log = LogFactory.getLog(DroneXMPPConnector.class);

   // XmppManager xmppManager;

    private MessageTransformer messageController;
    private static String xmppServerIP;
    private static int xmppServerPort;
    private static String xmppAdminUsername;
    private static String xmppAdminPassword;
    private static String xmppAdminAccountJID;

    public DroneXMPPConnector(MessageTransformer messageController) {
        this.messageController = messageController;
        initConnector();
    }


    public void initConnector() {
       /* xmppServerIP = XmppConfig.getInstance().getXmppServerIP();
        xmppAdminUsername = XmppConfig.getInstance().getXmppUsername();
        xmppAdminPassword = XmppConfig.getInstance().getXmppPassword();
        xmppAdminAccountJID = xmppAdminUsername + "@" + xmppServerIP;*/
        xmppServerPort = 5222;
        xmppServerIP = "localhost";
        xmppAdminUsername = "admin";
        xmppAdminPassword = "admin";
        xmppAdminAccountJID = xmppAdminUsername + "@" + xmppServerIP;
    }

    /*public void connectAndLogin() {
        try {
            super.connectAndLogin(xmppAdminUsername, xmppAdminPassword, null);
            super.setMessageFilterOnReceiver(xmppAdminAccountJID);
        } catch (DeviceManagementException e) {
            log.error("Connect/Login attempt to XMPP Server at: " + xmppServerIP + " failed");
            retryXMPPConnection();
        }
    }*/



    private XMPPConnection xmppConnection;

    public void connect(String server, int port) throws Exception {
        if(xmppConnection == null){
            xmppConnection = new XMPPConnection(new ConnectionConfiguration(server, port));
            xmppConnection.connect();
        }else{
            System.out.println("Already user is connected");
        }
        /*xmppConnection = new XMPPConnection(new ConnectionConfiguration(server, port));
        xmppConnection.connect();*/
    }

    public void disconnect(){
        if(xmppConnection != null){
            xmppConnection.disconnect();
            //interrupt();
        }
    }

    public void login(String username, String password) throws Exception{
        printRoster();
        connect( xmppServerIP, xmppServerPort);
        xmppConnection.login(username, password);
    }

    public void run(){
        try {
            System.out.println(xmppAdminAccountJID+xmppAdminPassword);
            login(xmppAdminAccountJID, xmppAdminPassword);
            System.out.println("Login successful");
            listeningForMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listeningForMessages() {
        PacketFilter filter = new AndFilter(new PacketTypeFilter(Message.class));
        PacketCollector collector = xmppConnection.createPacketCollector(filter);
        while (true) {
            System.out.println("waiting ...");
            Packet packet = collector.nextResult();
            if (packet instanceof Message) {
                Message inbound_message = (Message) packet;
                if (inbound_message != null && inbound_message.getBody() != null){
                    System.out.println(inbound_message.getBody());
                    messageController.messageTranslater(inbound_message.getBody());
                }

            } else {
                log.error("Message has been corrupted");
            }
        }
    }

    public void printRoster() throws Exception {
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

    }

   /* private void retryXMPPConnection() {
        Thread retryToConnect = new Thread() {
            @Override
            public void run() {

                while (true) {
                    if (!isConnected()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Re-trying to reach XMPP Server....");
                        }

                        try {
                            VirtualFireAlarmXMPPConnector.super.connectAndLogin(xmppAdminUsername,
                                    xmppAdminPassword,
                                    null);
                            VirtualFireAlarmXMPPConnector.super.setMessageFilterOnReceiver(
                                    xmppAdminAccountJID);
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
    }*/

}
