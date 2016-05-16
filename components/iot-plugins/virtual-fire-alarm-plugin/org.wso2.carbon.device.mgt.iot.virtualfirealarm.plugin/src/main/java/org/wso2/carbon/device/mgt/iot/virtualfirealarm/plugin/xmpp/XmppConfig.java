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
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.util.VirtualFireAlarmUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class XmppConfig {

    private String xmppServerIP;
    private int xmppServerPort;
    private String xmppUsername;
    private String xmppPassword;
    private boolean isEnabled;
    private String virtualFirealarmAdminUsername;
    private String virtualFirealarmAdminPassword;
    private String virtualFirealarmAdminJID;
    private String xmppServerName;
    private static XmppConfig xmppConfig = new XmppConfig();
    private static final Log log = LogFactory.getLog(XmppConfig.class);

    private XmppConfig() {
        File configFile = new File(VirtualFireAlarmConstants.XMPP_CONFIG_LOCATION);
        if (configFile.exists()) {
            try {
                InputStream propertyStream = configFile.toURI().toURL().openStream();
                Properties properties = new Properties();
                properties.load(propertyStream);
                xmppServerIP = properties.getProperty(VirtualFireAlarmConstants.HOST_KEY);
                xmppServerName = properties.getProperty(VirtualFireAlarmConstants.SERVER_NAME);
                xmppServerPort = Integer.parseInt(properties.getProperty(VirtualFireAlarmConstants.PORT_KEY));
                isEnabled = Boolean.parseBoolean(properties.getProperty(VirtualFireAlarmConstants.IS_ENABLED_KEY));
                xmppUsername = properties.getProperty(VirtualFireAlarmConstants.ADMIN_USERNAME);
                xmppPassword = properties.getProperty(VirtualFireAlarmConstants.ADMIN_PASSWORD);
                virtualFirealarmAdminUsername = "wso2admin_" + VirtualFireAlarmConstants.DEVICE_TYPE;
                virtualFirealarmAdminJID = virtualFirealarmAdminUsername + "@" + xmppServerName;
                virtualFirealarmAdminPassword = VirtualFireAlarmConstants.XMPP_SERVER_PASSWORD;
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    public String getXmppServerIP() {
        return xmppServerIP;
    }

    public int getXmppServerPort() {
        return xmppServerPort;
    }

    public String getXmppUsername() {
        return xmppUsername;
    }

    public String getXmppPassword() {
        return xmppPassword;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public static XmppConfig getInstance() {
        return xmppConfig;
    }

    public String getVirtualFirealarmAdminUsername() {
        return virtualFirealarmAdminUsername;
    }

    public String getVirtualFirealarmAdminPassword() {
        return virtualFirealarmAdminPassword;
    }

    public String getVirtualFirealarmAdminJID() {
        return virtualFirealarmAdminJID;
    }

    public String getXmppServerName() {
        return xmppServerName;
    }
}
