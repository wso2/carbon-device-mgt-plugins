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
package org.wso2.carbon.device.mgt.output.adapter.xmpp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;

/**
 * XMPP publisher related configuration initialization and publishing capabilties are implemented here.
 */
public class XMPPAdapterPublisher {

    private static final Log log = LogFactory.getLog(XMPPAdapterPublisher.class);
    private XMPPServerConnectionConfiguration xmppServerConnectionConfiguration;
    XMPPConnection xmppConnection;

    public XMPPAdapterPublisher(XMPPServerConnectionConfiguration xmppServerConnectionConfiguration) {
        this.xmppServerConnectionConfiguration = xmppServerConnectionConfiguration;
        connect();
    }

    public void connect() {
        SmackConfiguration.setPacketReplyTimeout(xmppServerConnectionConfiguration.getTimeoutInterval());
        ConnectionConfiguration config = new ConnectionConfiguration(xmppServerConnectionConfiguration.getHost(),
                                                                     xmppServerConnectionConfiguration.getPort());
        config.setSASLAuthenticationEnabled(false);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        xmppConnection = new XMPPConnection(config);
        String resource = xmppServerConnectionConfiguration.getResource();
        String username = xmppServerConnectionConfiguration.getUsername();
        String password = xmppServerConnectionConfiguration.getPassword();
        try {
            xmppConnection.connect();
            if (resource == null || resource.trim().isEmpty()) {
                xmppConnection.login(username, password);
            } else {
                xmppConnection.login(username, password, resource);
            }
        } catch (XMPPException e) {
            String errorMsg = "Login attempt to the XMPP Server  with username - " + username + " failed.";
            log.info(errorMsg);
            throw new OutputEventAdapterRuntimeException(errorMsg, e);
        }
    }

    public boolean isConnected() {
        return xmppConnection.isConnected();
    }

    public void publish(String JID, String subject, String message, String messageType) {
        Message xmppMessage = new Message();
        xmppMessage.setTo(JID);
        xmppMessage.setSubject(subject);
        xmppMessage.setBody(message);
        if (messageType != null) {
            switch (messageType) {
                case XMPPEventAdapterConstants.MessageType.CHAT:
                    xmppMessage.setType(Message.Type.chat);
                    break;
                case XMPPEventAdapterConstants.MessageType.GROUP_CHAT:
                    xmppMessage.setType(Message.Type.groupchat);
                    break;
                case XMPPEventAdapterConstants.MessageType.ERROR:
                    xmppMessage.setType(Message.Type.error);
                    break;
                case XMPPEventAdapterConstants.MessageType.HEADLINE:
                    xmppMessage.setType(Message.Type.headline);
                    break;
                default:
                    xmppMessage.setType(Message.Type.normal);
            }
        } else {
            xmppMessage.setType(Message.Type.normal);
        }
        xmppConnection.sendPacket(xmppMessage);
    }

    public void close() throws OutputEventAdapterException {
        if (xmppConnection != null && isConnected()) {
            xmppConnection.disconnect();
        }
    }

}
