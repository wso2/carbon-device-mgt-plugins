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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.xmpp;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.exception.VirtualFirealarmDeviceMgtPluginException;

import java.util.HashMap;
import java.util.Map;

public class XmppServerClient {

    public static boolean createAccount(XmppAccount xmppAccount) throws VirtualFirealarmDeviceMgtPluginException {
        if (XmppConfig.getInstance().isEnabled()) {
            if (xmppAccount != null) {
                try {
                    ConnectionConfiguration config = new ConnectionConfiguration(XmppConfig.getInstance().getHost(),
                                                                                 XmppConfig.getInstance().getPort(),
                                                                                 "Accounts");
                    XMPPConnection xmppConnection = new XMPPConnection(config);
                    xmppConnection.connect();
                    xmppConnection.login(XmppConfig.getInstance().getUsername(), XmppConfig.getInstance().getPassword());
                    AccountManager accountManager = xmppConnection.getAccountManager();
                    Map<String, String> attributes = new HashMap<>();
                    attributes.put("username", xmppAccount.getUsername());
                    attributes.put("password", xmppAccount.getPassword());
                    attributes.put("email", xmppAccount.getEmail());
                    attributes.put("name", xmppAccount.getAccountName());
                    accountManager.createAccount(xmppAccount.getUsername(), xmppAccount.getPassword(), attributes);
                    xmppConnection.disconnect();
                    return true;
                } catch (XMPPException e) {
                    if (e.getXMPPError().getCode() == 409) {
                        //AccountAlreadyExist
                        return true;
                    } else {
                        throw new VirtualFirealarmDeviceMgtPluginException(
                                "XMPP account creation failed. Error: " + e.getLocalizedMessage(), e);
                    }
                }
            } else {
                throw new VirtualFirealarmDeviceMgtPluginException("Invalid XMPP attributes");
            }
        } else {
            return true;
        }
    }
}
