/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.device.mgt.input.adapter.xmpp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.ToContainsFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStatus;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentInfo;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentTransformer;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentValidator;
import org.wso2.carbon.device.mgt.input.adapter.extension.transformer.DefaultContentTransformer;
import org.wso2.carbon.device.mgt.input.adapter.extension.validator.DefaultContentValidator;
import org.wso2.carbon.device.mgt.input.adapter.xmpp.exception.XMPPContentInitializationException;
import org.wso2.carbon.device.mgt.input.adapter.xmpp.internal.InputAdapterServiceDataHolder;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;
import java.util.HashMap;
import java.util.Map;

public class XMPPAdapterListener implements Runnable {
    private static final Log log = LogFactory.getLog(XMPPAdapterListener.class);

    private XMPPConnection xmppConnection;
    private XMPPServerConnectionConfiguration xmppServerConnectionConfiguration;
    private int tenantId;
    private boolean connectionSucceeded = false;
    private ContentValidator contentValidator;
    private ContentTransformer contentTransformer;
    private PacketListener packetListener;
    private boolean connectionInitialized;

    private InputEventAdapterListener eventAdapterListener = null;

    public XMPPAdapterListener(XMPPServerConnectionConfiguration xmppServerConnectionConfiguration,
                               InputEventAdapterListener inputEventAdapterListener, int tenantId) {
        this.xmppServerConnectionConfiguration = xmppServerConnectionConfiguration;
        this.eventAdapterListener = inputEventAdapterListener;
        this.tenantId = tenantId;

        String contentValidatorType = this.xmppServerConnectionConfiguration.getContentValidatorClassName();
        if (contentValidatorType == null || contentValidatorType.equals(XMPPEventAdapterConstants.DEFAULT)) {
            contentValidator = InputAdapterServiceDataHolder.getInputAdapterExtensionService().getDefaultContentValidator();
        } else  {
            contentValidator = InputAdapterServiceDataHolder.getInputAdapterExtensionService()
                    .getContentValidator(contentValidatorType);
        }

        String contentTransformerType = this.xmppServerConnectionConfiguration.getContentTransformerClassName();
        if (contentTransformer == null || contentTransformerType.equals(XMPPEventAdapterConstants.DEFAULT)) {
            this.contentTransformer = InputAdapterServiceDataHolder.getInputAdapterExtensionService()
                    .getDefaultContentTransformer();
        } else {
            this.contentTransformer = InputAdapterServiceDataHolder.getInputAdapterExtensionService()
                    .getContentTransformer(contentTransformerType);
        }

    }

    public void startListener() throws XMPPException {
        SmackConfiguration.setPacketReplyTimeout(xmppServerConnectionConfiguration.getTimeoutInterval());
        ConnectionConfiguration config = new ConnectionConfiguration(xmppServerConnectionConfiguration.getHost(),
                                                                     xmppServerConnectionConfiguration.getPort());
        config.setSASLAuthenticationEnabled(false);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        xmppConnection = new XMPPConnection(config);
        xmppConnection.connect();
        String resource = xmppServerConnectionConfiguration.getResource();
        String username = xmppServerConnectionConfiguration.getUsername();
        String password = xmppServerConnectionConfiguration.getPassword();
        try {
            if (resource == null || resource.trim().isEmpty()) {
                xmppConnection.login(username, password);
            } else {
                xmppConnection.login(username, password, resource);
            }
            setFilterOnReceiver(xmppServerConnectionConfiguration.getJid());
        } catch (XMPPException e) {
            String errorMsg = "Login attempt to the XMPP Server  with username - " + username + " failed.";
            log.info(errorMsg);
            throw new InputEventAdapterRuntimeException(errorMsg, e);
        }
    }

    public void stopListener(String adapterName) {
        if (connectionSucceeded) {
            // Un-subscribe accordingly and disconnect from the MQTT server.
            if (!ServerStatus.getCurrentStatus().equals(ServerStatus.STATUS_SHUTTING_DOWN)) {
                xmppConnection.removePacketListener(packetListener);
            }
            xmppConnection.disconnect();
        }
        //This is to stop all running reconnection threads
        connectionSucceeded = true;
    }

    protected void setFilterOnReceiver(String receiverJID) {
        PacketFilter packetFilter = new AndFilter(new PacketTypeFilter(Message.class), new ToContainsFilter(
                receiverJID));
        packetListener = new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                if (packet instanceof Message) {
                    final Message xmppMessage = (Message) packet;
                    Thread msgProcessThread = new Thread() {
                        public void run() {
                            processIncomingMessage(xmppMessage);
                        }
                    };
                    msgProcessThread.start();
                }
            }
        };
        xmppConnection.addPacketListener(packetListener, packetFilter);
    }

    public void processIncomingMessage(Message xmppMessage) {
        try {
            String from = xmppMessage.getFrom();
            String subject = xmppMessage.getSubject();
            String message = xmppMessage.getBody();
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

            if (log.isDebugEnabled()) {
                log.debug("Event received in MQTT Event Adapter - " + message);
            }

            if (contentValidator != null && contentTransformer != null) {
                Map<String, Object> dynamicParmaters = new HashMap<>();
                dynamicParmaters.put(XMPPEventAdapterConstants.FROM_KEY, from);
                dynamicParmaters.put(XMPPEventAdapterConstants.SUBJECT_KEY, subject);
                message = (String) contentTransformer.transform(message, dynamicParmaters);
                ContentInfo contentInfo = contentValidator.validate(message, dynamicParmaters);
                if (contentInfo != null && contentInfo.isValidContent()) {
                    eventAdapterListener.onEvent(contentInfo.getMessage());
                }
            } else {
                eventAdapterListener.onEvent(message);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void run() {
        while (!connectionSucceeded) {
            int connectionDuration = XMPPEventAdapterConstants.INITIAL_RECONNECTION_DURATION;
            try {
                connectionDuration = connectionDuration * XMPPEventAdapterConstants.RECONNECTION_PROGRESS_FACTOR;
                Thread.sleep(connectionDuration);
                startListener();
                connectionSucceeded = true;
                log.info("XMPP Connection successful");
            } catch (InterruptedException e) {
                log.error("Interruption occurred while waiting for reconnection", e);
            } catch (XMPPException e) {
                log.error("XMPP Exception occurred when starting listener", e);
            }
        }
    }

    public void createConnection() {
        connectionInitialized = true;
        new Thread(this).start();
    }

    public boolean isConnectionInitialized() {
        return connectionInitialized;
    }
}
