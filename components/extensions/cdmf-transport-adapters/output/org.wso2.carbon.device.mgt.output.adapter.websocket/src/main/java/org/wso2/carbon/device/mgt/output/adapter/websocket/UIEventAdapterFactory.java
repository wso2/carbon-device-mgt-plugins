/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.device.mgt.output.adapter.websocket;

import org.wso2.carbon.device.mgt.output.adapter.websocket.util.UIEventAdapterConstants;
import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.core.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The UI event adapter factory class to create a UI output adapter
 */
public class UIEventAdapterFactory extends OutputEventAdapterFactory {

    private ResourceBundle resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.device.mgt.output.adapter.websocket.i18n" +
            ".Resources", Locale.getDefault());

    public UIEventAdapterFactory() {
    }

    @Override
    public String getType() {
        return UIEventAdapterConstants.ADAPTER_TYPE_UI;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportedMessageFormats = new ArrayList<String>();
        supportedMessageFormats.add(MessageType.WSO2EVENT);
        supportedMessageFormats.add(MessageType.JSON);
        return supportedMessageFormats;
    }

    @Override
    public List<Property> getStaticPropertyList() {
        return null;
    }

    @Override
    public List<Property> getDynamicPropertyList() {
        return null;
    }

    @Override
    public String getUsageTips() {
        return resourceBundle.getString(UIEventAdapterConstants.ADAPTER_USAGE_TIPS_PREFIX) + " "
                + resourceBundle.getString(UIEventAdapterConstants.ADAPTER_USAGE_TIPS_POSTFIX);
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                                                 Map<String, String> globalProperties) {
        return new UIEventAdapter(eventAdapterConfiguration, globalProperties);
    }

}
