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
package org.wso2.carbon.device.mgt.input.adapter.extension.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentTransformer;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentValidator;
import org.wso2.carbon.device.mgt.input.adapter.extension.InputAdapterExtensionService;
import org.wso2.carbon.device.mgt.input.adapter.extension.InputAdapterExtensionServiceImpl;
import org.wso2.carbon.device.mgt.input.adapter.extension.transformer.DefaultContentTransformer;
import org.wso2.carbon.device.mgt.input.adapter.extension.transformer.MQTTContentTransformer;
import org.wso2.carbon.device.mgt.input.adapter.extension.validator.DefaultContentValidator;
import org.wso2.carbon.device.mgt.input.adapter.extension.validator.HTTPContentValidator;
import org.wso2.carbon.device.mgt.input.adapter.extension.validator.MQTTContentValidator;

/**
 * @scr.component name="input.adapter.extension.adapterService.component" immediate="true"
 * @scr.reference name="InputAdapterServiceComponent.content.validator.service"
 * interface="org.wso2.carbon.device.mgt.input.adapter.extension.ContentValidator"
 * cardinality="0..n"
 * policy="dynamic"
 * bind="setContentValidator"
 * unbind="unsetContentValidator"
 * @scr.reference name="InputAdapterServiceComponent.transformer.service"
 * interface="org.wso2.carbon.device.mgt.input.adapter.extension.ContentTransformer"
 * cardinality="0..n"
 * policy="dynamic"
 * bind="setContentTransformer"
 * unbind="unsetContentTransformer"
 */
public class InputAdapterServiceComponent {

	private static final Log log = LogFactory.getLog(
			InputAdapterServiceComponent.class);

	protected void activate(ComponentContext context) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Successfully deployed the input adapter extension service");
			}

            InputAdapterServiceDataHolder.getInstance().addContentTransformer(new DefaultContentTransformer());
            InputAdapterServiceDataHolder.getInstance().addContentTransformer(new MQTTContentTransformer());
            InputAdapterServiceDataHolder.getInstance().addContentValidator(new DefaultContentValidator());
            InputAdapterServiceDataHolder.getInstance().addContentValidator(new HTTPContentValidator());
            InputAdapterServiceDataHolder.getInstance().addContentValidator(new MQTTContentValidator());

            context.getBundleContext().registerService(InputAdapterExtensionService.class,
                                                       new InputAdapterExtensionServiceImpl(), null);
        } catch (RuntimeException e) {
            log.error("Can not create the input adapter service ", e);
        }
    }

    protected void setContentValidator(ContentValidator contentValidator) {
        if (log.isDebugEnabled()) {
            log.debug("Setting ContentValidator Service");
        }
        InputAdapterServiceDataHolder.getInstance().addContentValidator(contentValidator);
    }

    protected void unsetContentValidator(ContentValidator contentValidator) {
        if (log.isDebugEnabled()) {
            log.debug("Un-setting ContentValidator Service");
        }
    }

    protected void setContentTransformer(ContentTransformer contentTransformer) {
        if (log.isDebugEnabled()) {
            log.debug("Setting contentTransformer Service");
        }
        InputAdapterServiceDataHolder.getInstance().addContentTransformer(contentTransformer);
    }

    protected void unsetContentTransformer(ContentTransformer contentTransformer) {
        if (log.isDebugEnabled()) {
            log.debug("Un-setting ContentTransformer Service");
        }
    }

}
