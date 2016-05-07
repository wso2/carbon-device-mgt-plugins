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
package org.wso2.carbon.device.mgt.iot.input.adapter.xmpp;

import org.wso2.carbon.device.mgt.iot.input.adapter.mqtt.util.MQTTEventAdapterConstants;
import org.wso2.carbon.device.mgt.iot.input.adapter.xmpp.util.XMPPEventAdapterConstants;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.core.MessageType;
import org.wso2.carbon.event.input.adapter.core.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The xmpp event adapter factory class to create a xmpp input adapter
 */
public class XMPPEventAdapterFactory extends InputEventAdapterFactory {

    private ResourceBundle resourceBundle = ResourceBundle.getBundle
            ("org.wso2.carbon.device.mgt.iot.input.adapter.xmpp.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return XMPPEventAdapterConstants.ADAPTER_TYPE_XMPP;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.JSON);
        supportInputMessageTypes.add(MessageType.TEXT);
        supportInputMessageTypes.add(MessageType.XML);
        supportInputMessageTypes.add(MessageType.WSO2EVENT);
        return supportInputMessageTypes;
    }

    @Override
    public List<Property> getPropertyList() {
        List<Property> propertyList = new ArrayList<Property>();
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
        resource.setRequired(true);
        resource.setHint(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_RESOURCE_HINT));

        //Content Validator details
        Property contentValidator = new Property(XMPPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME);
        contentValidator.setDisplayName(
                resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME));
        contentValidator.setRequired(false);
        contentValidator.setHint(
                resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME_HINT));
        contentValidator.setDefaultValue(org.wso2.carbon.device.mgt.iot.input.adapter.mqtt.Constants.DEFAULT);

        //Content Validator Params details
        Property contentValidatorParams = new Property(XMPPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_PARAMS);
        contentValidatorParams.setDisplayName(
                resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_PARAMS));
        contentValidatorParams.setRequired(false);
        contentValidatorParams.setHint(
                resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_PARAMS_HINT));
        contentValidatorParams.setDefaultValue(Constants.XMPP_CONTENT_VALIDATION_DEFAULT_PARAMETERS);

        Property jid = new Property(XMPPEventAdapterConstants.ADAPTER_CONF_RECIEVER_JID);
        jid.setDisplayName(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_RECIEVER_JID));
        jid.setRequired(true);
        jid.setHint(resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_RECIEVER_JID_HINT));

        //Content Transformer details
        Property contentTransformer = new Property(XMPPEventAdapterConstants.ADAPTER_CONF_CONTENT_TRANSFORMER_CLASSNAME);
        contentTransformer.setDisplayName(
                resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_CONTENT_TRANSFORMER_CLASSNAME));
        contentTransformer.setRequired(false);
        contentTransformer.setHint(
                resourceBundle.getString(XMPPEventAdapterConstants.ADAPTER_CONF_CONTENT_TRANSFORMER_CLASSNAME_HINT));
        contentTransformer.setDefaultValue(Constants.DEFAULT);

        propertyList.add(host);
        propertyList.add(port);
        propertyList.add(userName);
        propertyList.add(password);
        propertyList.add(timooutInterval);
        propertyList.add(resource);
        propertyList.add(contentValidator);
        propertyList.add(contentValidatorParams);
        propertyList.add(jid);
        propertyList.add(contentTransformer);
        return propertyList;
    }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                                                Map<String, String> globalProperties) {
        return new XMPPEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}
