/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.device.mgt.input.adapter.http;

import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.core.MessageType;
import org.wso2.carbon.event.input.adapter.core.Property;
import org.wso2.carbon.device.mgt.input.adapter.http.util.HTTPEventAdapterConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The http event adapter factory class to create a http input adapter
 */
public class HTTPEventAdapterFactory extends InputEventAdapterFactory {

	private ResourceBundle resourceBundle =
			ResourceBundle.getBundle("org.wso2.carbon.device.mgt.input.adapter.http.i18n.Resources", Locale.getDefault());
	private int httpPort;
	private int httpsPort;

	public HTTPEventAdapterFactory() {
		int portOffset = getPortOffset();
		httpPort = HTTPEventAdapterConstants.DEFAULT_HTTP_PORT + portOffset;
		httpsPort = HTTPEventAdapterConstants.DEFAULT_HTTPS_PORT + portOffset;
	}

	@Override
	public String getType() {
		return HTTPEventAdapterConstants.ADAPTER_TYPE_HTTP;
	}

	@Override
	public List<String> getSupportedMessageFormats() {
		List<String> supportInputMessageTypes = new ArrayList<>();
		supportInputMessageTypes.add(MessageType.JSON);
		supportInputMessageTypes.add(MessageType.TEXT);
		supportInputMessageTypes.add(MessageType.XML);
		supportInputMessageTypes.add(MessageType.WSO2EVENT);
		return supportInputMessageTypes;
	}

	@Override
	public List<Property> getPropertyList() {

		List<Property> propertyList = new ArrayList<>();

		// Transport Exposed
		Property exposedTransportsProperty = new Property(HTTPEventAdapterConstants.EXPOSED_TRANSPORTS);
		exposedTransportsProperty.setRequired(false);
		exposedTransportsProperty.setDisplayName(
				resourceBundle.getString(HTTPEventAdapterConstants.EXPOSED_TRANSPORTS));
		exposedTransportsProperty.setOptions(
				new String[]{HTTPEventAdapterConstants.HTTPS, HTTPEventAdapterConstants.HTTP,
						HTTPEventAdapterConstants.LOCAL, HTTPEventAdapterConstants.ALL});
		exposedTransportsProperty.setDefaultValue(HTTPEventAdapterConstants.ALL);
		propertyList.add(exposedTransportsProperty);

		//Content Validator details
		Property contentValidator = new Property(HTTPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_TYPE);
		contentValidator.setDisplayName(
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_TYPE));
		contentValidator.setRequired(false);
		contentValidator.setHint(
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_TYPE_HINT));
		contentValidator.setDefaultValue(HTTPEventAdapterConstants.DEFAULT);
		propertyList.add(contentValidator);

		//Content Transformer details
		Property contentTransformer = new Property(HTTPEventAdapterConstants.ADAPTER_CONF_CONTENT_TRANSFORMER_CLASSNAME);
		contentTransformer.setDisplayName(
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_CONF_CONTENT_TRANSFORMER_CLASSNAME));
		contentTransformer.setRequired(false);
		contentTransformer.setHint(
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_CONF_CONTENT_TRANSFORMER_CLASSNAME_HINT));
		contentTransformer.setDefaultValue(HTTPEventAdapterConstants.DEFAULT);
		propertyList.add(contentTransformer);
		return propertyList;
	}

	@Override
	public String getUsageTips() {
		return resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_PREFIX) + httpPort +
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_MID1) + httpsPort +
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_MID2) + httpPort +
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_MID3) + httpsPort +
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_POSTFIX);
	}

	@Override
	public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
												Map<String, String> globalProperties) {
		return new HTTPEventAdapter(eventAdapterConfiguration, globalProperties);
	}

	private int getPortOffset() {
		return CarbonUtils.getPortFromServerConfig(HTTPEventAdapterConstants.CARBON_CONFIG_PORT_OFFSET_NODE) + 1;
	}
}
