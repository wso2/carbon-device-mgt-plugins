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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.xmpp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
    private static final String ENABLED = "enabled";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String SERVERNAME = "serverName";
    private static final String JID = "jid";

    private XmppConfig() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("../xmpp.properties");
        Properties properties = new Properties();
        try {
            properties.load(input);
            enabled = Boolean.parseBoolean(properties.getProperty(ENABLED, "false"));
            host = properties.getProperty(HOST);
            port = Integer.parseInt(properties.getProperty(PORT));
            username = properties.getProperty(USERNAME);
            password = properties.getProperty(PASSWORD);
            serverName = properties.getProperty(SERVERNAME);
            jid = properties.getProperty(JID);
        } catch (IOException e) {
            log.error("Failed to load xmpp config properties.");
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
