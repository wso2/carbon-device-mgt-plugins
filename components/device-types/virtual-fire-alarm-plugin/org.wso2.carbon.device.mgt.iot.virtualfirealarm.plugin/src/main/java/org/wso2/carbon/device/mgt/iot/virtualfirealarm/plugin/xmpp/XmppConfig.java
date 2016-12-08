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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.xmpp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.config.DeviceManagementConfiguration;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.config.EventListenerConfiguration;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.config.VirtualFirealarmConfig;

import java.util.List;

public class XmppConfig {

    private String host;
    private int port;
    private String username;
    private String password;
    private String serverName;
    private boolean enabled;
    private String jid;
    private static XmppConfig xmppConfig = new XmppConfig();
    private static final Log log = LogFactory.getLog(XmppConfig.class);

    private XmppConfig() {
        DeviceManagementConfiguration deviceManagementConfiguration = VirtualFirealarmConfig.getInstance()
                .getDeviceTypeConfiguration();
        List<EventListenerConfiguration.Property> properties = deviceManagementConfiguration.getEventListenerConfiguration()
                .getProperties();
        String provider = deviceManagementConfiguration.getEventListenerConfiguration().getEventListenerProvider();
        if ("XMPP".equals(provider)) {
            enabled = true;
        }
        if (enabled) {
            for (EventListenerConfiguration.Property property : properties) {
                switch (property.getName()) {
                    case "host":
                        host = property.getValue();
                        break;
                    case "port":
                        port = Integer.parseInt(property.getValue());
                        break;
                    case "username":
                        username = property.getValue();
                        break;
                    case "password":
                        password = property.getValue();
                        break;
                    case "server.name":
                        serverName = property.getValue();
                        break;
                    case "jid":
                        jid = property.getValue();
                        break;

                }
            }
        }
    }

    public static XmppConfig getInstance() {
        return xmppConfig;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }
}
