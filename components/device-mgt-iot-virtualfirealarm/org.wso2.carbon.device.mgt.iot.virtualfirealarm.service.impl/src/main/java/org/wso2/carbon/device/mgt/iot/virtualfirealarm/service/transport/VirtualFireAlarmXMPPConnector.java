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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.packet.Message;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConnector;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.util.VirtualFireAlarmServiceUtils;

import java.util.Calendar;

public class VirtualFireAlarmXMPPConnector extends XmppConnector {
    private static Log log = LogFactory.getLog(VirtualFireAlarmXMPPConnector.class);

    private static String xmppServerIP;
    //	private static int xmppServerPort;
    private static String xmppAdminUsername;
    private static String xmppAdminPassword;
    private static String xmppAdminAccountJID;

    private VirtualFireAlarmXMPPConnector() {
        super(XmppConfig.getInstance().getXmppServerIP(),
              XmppConfig.getInstance().getSERVER_CONNECTION_PORT());
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

    @Override
    protected void processXMPPMessage(Message xmppMessage) {
        String from = xmppMessage.getFrom();
        String subject = xmppMessage.getSubject();
        String message = xmppMessage.getBody();

        int indexOfAt = from.indexOf("@");
        int indexOfSlash = from.indexOf("/");

        if (indexOfAt != -1 && indexOfSlash != -1) {
            String deviceId = from.substring(0, indexOfAt);
            String owner = from.substring(indexOfSlash + 1, from.length());

            if (log.isDebugEnabled()) {
                log.debug("Received XMPP message for: {OWNER-" + owner + "} & {DEVICE.ID-" + deviceId + "}");
            }

            if (subject != null) {
                switch (subject) {
                    case "PUBLISHER":
                        float temperature = Float.parseFloat(message.split(":")[1]);
                        if (!VirtualFireAlarmServiceUtils.publishToDAS(owner, deviceId, temperature)) {
                            log.error("XMPP Connector: Publishing data to DAS failed.");
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("XMPP: Publisher Message [" + message + "] from [" + from + "]");
                            log.debug("XMPP Connector: Published data to DAS successfully.");
                        }
                        break;
                    case "CONTROL-REPLY":
                        if (log.isDebugEnabled()) {
                            log.debug("XMPP: Reply Message [" + message + "] from [" + from + "]");
                        }
                        String tempVal = message.split(":")[1];
                        SensorDataManager.getInstance().setSensorRecord(deviceId,
                                                                        VirtualFireAlarmConstants.SENSOR_TEMPERATURE,
                                                                        tempVal,
                                                                        Calendar.getInstance().getTimeInMillis());
                        break;
                    default:
                        if (log.isDebugEnabled()) {
                            log.warn("Unknown XMPP Message [" + message + "] from [" + from + "] received");
                        }
                        break;
                }
            }
        } else {
            log.warn("Received XMPP message from client with unexpected JID [" + from + "].");
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
    }
}
