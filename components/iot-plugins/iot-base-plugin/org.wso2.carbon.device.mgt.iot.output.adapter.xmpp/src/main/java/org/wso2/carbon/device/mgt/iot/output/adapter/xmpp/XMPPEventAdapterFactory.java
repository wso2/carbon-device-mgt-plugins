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
package org.wso2.carbon.device.mgt.iot.output.adapter.xmpp;

import org.wso2.carbon.device.mgt.iot.output.adapter.xmpp.util.XMPPEventAdapterConstants;
import org.wso2.carbon.event.output.adapter.core.*;

import java.util.*;

/**
 * The xmpp event adapter factory class to create a xmpp output adapter
 */
public class XMPPEventAdapterFactory extends OutputEventAdapterFactory {
    private ResourceBundle resourceBundle =
            ResourceBundle.getBundle("org.wso2.carbon.device.mgt.iot.output.adapter.xmpp.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return XMPPEventAdapterConstants.ADAPTER_TYPE_XMPP;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportedMessageFormats = new ArrayList<String>();
        supportedMessageFormats.add(MessageType.XML);
        supportedMessageFormats.add(MessageType.JSON);
        supportedMessageFormats.add(MessageType.TEXT);
        return supportedMessageFormats;
    }

    @Override
    public List<Property> getStaticPropertyList() {

        List<Property> staticPropertyList = new ArrayList<Property>();
        // Url
        Property host = new Property(XMPPEventAdapterConstants.ADAPTER_CONF_HOST);
        host.setDisplayName(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_HOST));
        host.setRequired(true);
        host.setHint(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_HOST_HINT));

        // Host Port
        Property port = new Property(XMPPEventAdapterConstants.ADAPTER_CONF_PORT);
        port.setDisplayName(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_PORT));
        port.setRequired(true);
        port.setHint(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_PORT_HINT));

        //Broker Username
        Property userName = new Property(XMPPEventAdapterConstants.ADAPTER_CONF_USERNAME);
        userName.setDisplayName(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_USERNAME));
        userName.setRequired(true);
        userName.setHint(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_USERNAME_HINT));

        //Broker Password
        Property password = new Property(XMPPEventAdapterConstants.ADAPTER_CONF_PASSWORD);
        password.setDisplayName(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_PASSWORD));
        password.setRequired(true);
        password.setHint(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_PASSWORD_HINT));

        //Timeout Interval
        Property timooutInterval = new Property(XMPPEventAdapterConstants.ADAPTER_CONF_TIMEOUT_INTERVAL);
        timooutInterval.setDisplayName(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_TIMEOUT_INTERVAL_HINT));
        timooutInterval.setRequired(false);
        timooutInterval.setHint(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_TIMEOUT_INTERVAL_HINT));

        //Resource
        Property resource = new Property(XMPPEventAdapterConstants.ADAPTER_CONF_RESOURCE);
        resource.setDisplayName(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_RESOURCE));
        resource.setRequired(false);
        resource.setHint(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_RESOURCE_HINT));

        staticPropertyList.add(host);
        staticPropertyList.add(port);
        staticPropertyList.add(userName);
        staticPropertyList.add(password);
        staticPropertyList.add(timooutInterval);
        staticPropertyList.add(resource);
        return staticPropertyList;
    }

    @Override
    public List<Property> getDynamicPropertyList() {
        List<Property> dynamicPropertyList = new ArrayList<Property>();
        // set topic
        Property jidProperty = new Property(XMPPEventAdapterConstants.ADAPTER_CONF_JID);
        jidProperty.setDisplayName(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_JID_HINT));
        jidProperty.setRequired(true);

        Property subjectProperty = new Property(XMPPEventAdapterConstants.ADAPTER_CONF_SUBJECT);
        subjectProperty.setDisplayName(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_SUBJECT_HINT));
        subjectProperty.setRequired(false);

        Property messageType = new Property(XMPPEventAdapterConstants.ADAPTER_CONF_MESSAGETYPE);
        messageType.setDisplayName(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_MESSAGETYPE_HINT));
        messageType.setRequired(true);
        messageType.setOptions(
                new String[]{XMPPEventAdapterConstants.MessageType.CHAT, XMPPEventAdapterConstants.MessageType.ERROR,
                        XMPPEventAdapterConstants.MessageType.GROUP_CHAT, XMPPEventAdapterConstants.MessageType.NORMAL,
                        XMPPEventAdapterConstants.MessageType.HEADLINE});
        messageType.setDefaultValue(XMPPEventAdapterConstants.MessageType.NORMAL);

        dynamicPropertyList.add(jidProperty);
        dynamicPropertyList.add(subjectProperty);
        dynamicPropertyList.add(messageType);
        return dynamicPropertyList;
    }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                                                 Map<String, String> globalProperties) {
        return new XMPPEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}
