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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.packet.Message;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppAccount;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppServerClient;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.transport.xmpp.XMPPTransportHandler;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.exception.VirtualFireAlarmException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.SecurityManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.VirtualFireAlarmServiceUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("no JAX-WS annotation")
public class VirtualFireAlarmXMPPConnector extends XMPPTransportHandler {
    private static Log log = LogFactory.getLog(VirtualFireAlarmXMPPConnector.class);

    private static String xmppServerIP;
    private static String xmppVFireAlarmAdminUsername;
    private static String xmppVFireAlarmAdminAccountJID;
    private static final String V_FIREALARM_XMPP_PASSWORD = "vfirealarm@123";
    private static final String DEVICEMGT_CONFIG_FILE = "devicemgt-config.xml";

    private ScheduledFuture<?> connectorServiceHandler;
    private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private VirtualFireAlarmXMPPConnector() {
        super(XmppConfig.getInstance().getXmppServerIP(), XmppConfig.getInstance().getSERVER_CONNECTION_PORT());
    }

    public void initConnector() {
        xmppVFireAlarmAdminUsername = "wso2admin_" + VirtualFireAlarmConstants.DEVICE_TYPE;
        xmppServerIP = XmppConfig.getInstance().getXmppServerIP();
        xmppVFireAlarmAdminAccountJID = xmppVFireAlarmAdminUsername + "@" + xmppServerIP;
        createXMPPAccountForDeviceType();
    }

    public void createXMPPAccountForDeviceType() {
        boolean accountExists;
        XmppServerClient xmppServerClient = new XmppServerClient();
        xmppServerClient.initControlQueue();

        try {
            accountExists = xmppServerClient.doesXMPPUserAccountExist(xmppVFireAlarmAdminUsername);

            if (!accountExists) {
                XmppAccount xmppAccount = new XmppAccount();

                xmppAccount.setAccountName(xmppVFireAlarmAdminUsername);
                xmppAccount.setUsername(xmppVFireAlarmAdminUsername);
                xmppAccount.setPassword(V_FIREALARM_XMPP_PASSWORD);
                xmppAccount.setEmail("");

                try {
                    boolean xmppCreated = xmppServerClient.createXMPPAccount(xmppAccount);
                    if (!xmppCreated) {
                        log.warn("Server XMPP Account was not created for device-type - " +
                                         VirtualFireAlarmConstants.DEVICE_TYPE +
                                         ". Check whether XMPP is enabled in \"devicemgt-config.xml\" & restart.");
                    } else {
                        log.info("Server XMPP Account [" + xmppVFireAlarmAdminUsername +
                                         "] was not created for device - " + VirtualFireAlarmConstants.DEVICE_TYPE);
                    }
                } catch (DeviceControllerException e) {
                    String errorMsg =
                            "An error was encountered whilst trying to create Server XMPP account for device-type - "
                                    + VirtualFireAlarmConstants.DEVICE_TYPE;
                    log.error(errorMsg, e);
                }
            }

        } catch (DeviceControllerException e) {
            if (e.getMessage().contains(DEVICEMGT_CONFIG_FILE)) {
                log.warn("XMPP not Enabled");
            } else {
                String errorMsg = "An error was encountered whilst trying to check whether Server XMPP account " +
                        "exists for device-type - " + VirtualFireAlarmConstants.DEVICE_TYPE ;
                log.error(errorMsg, e);
            }
        }
    }


    @Override
    public void connect() {
        Runnable connector = new Runnable() {
            public void run() {
                if (!isConnected()) {
                    try {
                        connectToServer();
                        loginToServer(xmppVFireAlarmAdminUsername, V_FIREALARM_XMPP_PASSWORD, null);
                        setFilterOnReceiver(xmppVFireAlarmAdminAccountJID);

                    } catch (TransportHandlerException e) {
                        if (log.isDebugEnabled()) {
                            log.warn("Connection/Login to XMPP server at: " + server + " as " +
                                             xmppVFireAlarmAdminUsername + " failed for device-type [" +
                                             VirtualFireAlarmConstants.DEVICE_TYPE + "].", e);
                        }
                    }
                }
            }
        };

        connectorServiceHandler = service.scheduleAtFixedRate(connector, 0, timeoutInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void processIncomingMessage(Message xmppMessage) throws TransportHandlerException {
        String from = xmppMessage.getFrom();
        String subject = xmppMessage.getSubject();
        String message = xmppMessage.getBody();

        int indexOfAt = from.indexOf("@");
        int indexOfSlash = from.indexOf("/");

        if (indexOfAt != -1 && indexOfSlash != -1) {
            String deviceId = from.substring(0, indexOfAt);
            String owner = from.substring(indexOfSlash + 1, from.length());

            if (log.isDebugEnabled()) {
                log.debug("Received XMPP message for: [OWNER-" + owner + "] & [DEVICE.ID-" + deviceId + "]");
            }

            try {
                PublicKey clientPublicKey = VirtualFireAlarmServiceUtils.getDevicePublicKey(deviceId);
                PrivateKey serverPrivateKey = SecurityManager.getServerPrivateKey();
                String actualMessage = VirtualFireAlarmServiceUtils.extractMessageFromPayload(message, serverPrivateKey,
                                                                                       clientPublicKey);
                if (log.isDebugEnabled()) {
                    log.debug("XMPP: Received Message [" + actualMessage + "] from: [" + from + "]");
                }
                if (subject != null) {
                    switch (subject) {
                        case "PUBLISHER":
                            float temperature = Float.parseFloat(actualMessage.split(":")[1]);
                            PrivilegedCarbonContext.startTenantFlow();
                            String tenantDomain = MultitenantUtils.getTenantDomain(owner);
                            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
                            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(owner);
                            if (!VirtualFireAlarmServiceUtils.publishToDAS(deviceId, temperature)) {
                                log.error("XMPP Connector: Publishing VirtualFirealarm data to DAS failed.");
                            }
                            PrivilegedCarbonContext.endTenantFlow();
                            if (log.isDebugEnabled()) {
                                log.debug("XMPP: Publisher Message [" + actualMessage + "] from [" + from + "] " +
                                                  "was successfully published to DAS");
                            }
                            break;
                        case "CONTROL-REPLY":
                            String tempVal = actualMessage.split(":")[1];
                            SensorDataManager.getInstance().setSensorRecord(deviceId,
                                                                            VirtualFireAlarmConstants.SENSOR_TEMP,
                                                                            tempVal,
                                                                            Calendar.getInstance().getTimeInMillis());
                            break;

                        default:
                            if (log.isDebugEnabled()) {
                                log.warn("Unknown XMPP Message [" + actualMessage + "] from [" + from + "] received");
                            }
                            break;
                    }
                }
            } catch (VirtualFireAlarmException e) {
                String errorMsg =
                        "CertificateManagementService failure oo Signature-Verification/Decryption was unsuccessful.";
                log.error(errorMsg, e);
            }
        } else {
            log.warn("Received XMPP message from client with unexpected JID [" + from + "].");
        }
    }

    @Override
    public void publishDeviceData(String... publishData) throws TransportHandlerException {
        if (publishData.length != 4) {
            String errorMsg = "Incorrect number of arguments received to SEND-MQTT Message. " +
                    "Need to be [owner, deviceId, resource{BULB/TEMP}, state{ON/OFF or null}]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg);
        }

        String deviceOwner = publishData[0];
        String deviceId = publishData[1];
        String resource = publishData[2];
        String state = publishData[3];

        try {
            PublicKey devicePublicKey = VirtualFireAlarmServiceUtils.getDevicePublicKey(deviceId);
            PrivateKey serverPrivateKey = SecurityManager.getServerPrivateKey();

            String actualMessage = resource + ":" + state;
            String encryptedMsg = VirtualFireAlarmServiceUtils.prepareSecurePayLoad(actualMessage,
                                                                                    devicePublicKey,
                                                                                    serverPrivateKey);

            String clientToConnect = deviceId + "@" + xmppServerIP + File.separator + deviceOwner;
            sendXMPPMessage(clientToConnect, encryptedMsg, "CONTROL-REQUEST");

        } catch (VirtualFireAlarmException e) {
            String errorMsg = "Preparing Secure payload failed for device - [" + deviceId + "] of owner - " +
                    "[" + deviceOwner + "].";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        }
    }


    @Override
    public void disconnect() {
        Runnable stopConnection = new Runnable() {
            public void run() {
                while (isConnected()) {
                    connectorServiceHandler.cancel(true);
                    closeConnection();
                    if (log.isDebugEnabled()) {
                        log.warn("Unable to 'STOP' connection to XMPP server at: " + server +
                                         " for user - " + xmppVFireAlarmAdminUsername);
                    }

                    try {
                        Thread.sleep(timeoutInterval);
                    } catch (InterruptedException e1) {
                        log.error("XMPP-Terminator: Thread Sleep Interrupt Exception for "
                                          + VirtualFireAlarmConstants.DEVICE_TYPE + " type.", e1);
                    }

                }
            }
        };

        Thread terminatorThread = new Thread(stopConnection);
        terminatorThread.start();
    }


    @Override
    public void processIncomingMessage(Message message, String... messageParams) throws TransportHandlerException {
        // nothing to do
    }

    @Override
    public void processIncomingMessage() throws TransportHandlerException {
        // nothing to do
    }

    @Override
    public void publishDeviceData() throws TransportHandlerException {
        // nothing to do
    }

    @Override
    public void publishDeviceData(Message publishData) throws TransportHandlerException {
        // nothing to do
    }
}

