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
package org.wso2.carbon.device.mgt.input.adapter.mqtt.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.device.mgt.input.adapter.extension.InputAdapterExtensionService;
import org.wso2.carbon.device.mgt.input.adapter.mqtt.MQTTEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="input.iot.mqtt.AdapterService.component" immediate="true"
 * @scr.reference name="input.extension.service" interface="org.wso2.carbon.device.mgt.input.adapter.extension.InputAdapterExtensionService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setInputAdapterExtensionService"
 * unbind="unsetInputAdapterExtensionService"
 * @scr.reference name="jwt.client.service" interface="org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setJWTClientManagerService"
 * unbind="unsetJWTClientManagerService"
 * @scr.reference name="input.adapter.service" interface="org.wso2.carbon.event.input.adapter.core.InputEventAdapterService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setInputEventAdapterService"
 * unbind="unsetInputEventAdapterService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class InputAdapterServiceComponent {

	private static final Log log = LogFactory.getLog(InputAdapterServiceComponent.class);

	protected void activate(ComponentContext context) {
		try {
			InputEventAdapterFactory mqttEventAdapterFactory = new MQTTEventAdapterFactory();
			context.getBundleContext().registerService(InputEventAdapterFactory.class.getName(),
													   mqttEventAdapterFactory, null);
			if (log.isDebugEnabled()) {
				log.debug("Successfully deployed the input adapter service");
			}
		} catch (RuntimeException e) {
			log.error("Can not create the input adapter service ", e);
		}
	}

	protected void setHttpService(HttpService httpService) {
		InputAdapterServiceDataHolder.registerHTTPService(httpService);
	}

	protected void unsetHttpService(HttpService httpService) {
		InputAdapterServiceDataHolder.registerHTTPService(null);
	}

    protected void setInputAdapterExtensionService(InputAdapterExtensionService inputAdapterExtensionService) {
        InputAdapterServiceDataHolder.setInputAdapterExtensionService(inputAdapterExtensionService);
    }

    protected void unsetInputAdapterExtensionService(InputAdapterExtensionService inputAdapterExtensionService) {
        InputAdapterServiceDataHolder.setInputAdapterExtensionService(null);
    }

    protected void setJWTClientManagerService(JWTClientManagerService jwtClientManagerService) {
        InputAdapterServiceDataHolder.setJwtClientManagerService(jwtClientManagerService);
    }

    protected void unsetJWTClientManagerService(JWTClientManagerService jwtClientManagerService) {
        InputAdapterServiceDataHolder.setJwtClientManagerService(null);
    }

	protected void setInputEventAdapterService(InputEventAdapterService inputEventAdapterService) {
		InputAdapterServiceDataHolder.setInputEventAdapterService(inputEventAdapterService);
	}

	protected void unsetInputEventAdapterService(InputEventAdapterService inputEventAdapterService) {
		InputAdapterServiceDataHolder.setInputEventAdapterService(null);
	}

	protected void setConfigurationContextService(ConfigurationContextService contextService) {
		ConfigurationContext serverConfigContext = contextService.getServerConfigContext();
		InputAdapterServiceDataHolder.setMainServerConfigContext(serverConfigContext);
	}

	protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
		InputAdapterServiceDataHolder.setMainServerConfigContext(null);
	}

}
