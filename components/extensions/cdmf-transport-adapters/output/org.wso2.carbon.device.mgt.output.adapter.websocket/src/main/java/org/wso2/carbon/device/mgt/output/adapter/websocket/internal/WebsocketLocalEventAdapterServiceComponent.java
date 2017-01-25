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
package org.wso2.carbon.device.mgt.output.adapter.websocket.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.output.adapter.websocket.WebsocketEventAdapterFactory;
import org.wso2.carbon.device.mgt.output.adapter.websocket.WebsocketOutputCallbackControllerServiceImpl;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.device.mgt.output.adapter.websocket.WebsocketOutputCallbackControllerService;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * @scr.component component.name="output.extensions.secured.websocket.AdapterService.component" immediate="true"
 * @scr.reference name="eventStreamService.service"
 * interface="org.wso2.carbon.event.stream.core.EventStreamService" cardinality="1..1"
 * policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 */
public class WebsocketLocalEventAdapterServiceComponent {

    private static final Log log = LogFactory.getLog(WebsocketLocalEventAdapterServiceComponent.class);

    /**
     * initialize the websocket adapter service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            WebsocketEventAdapterFactory websocketEventAdapterFactory = new WebsocketEventAdapterFactory();
            context.getBundleContext().registerService(OutputEventAdapterFactory.class.getName()
                    , websocketEventAdapterFactory, null);
            WebsocketOutputCallbackControllerServiceImpl UIOutputCallbackRegisterServiceImpl =
                    new WebsocketOutputCallbackControllerServiceImpl();
            context.getBundleContext().registerService(WebsocketOutputCallbackControllerService.class.getName(),
                    UIOutputCallbackRegisterServiceImpl, null);

            websocketEventAdapterFactory.setBundleContext(context.getBundleContext());

            WebsocketEventAdaptorServiceDataHolder.registerUIOutputCallbackRegisterServiceInternal(
                    UIOutputCallbackRegisterServiceImpl);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the output websocket adapter service");
            }
        } catch (RuntimeException e) {
            log.error("Can not create the output websocket adapter service ", e);
        } catch (Throwable e) {
            log.error("Error occurred while activating UI Event Adapter Service Component", e);
        }
    }

    protected void setEventStreamService(EventStreamService eventStreamService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the EventStreamService reference for the UILocalEventAdaptor Service");
        }
        WebsocketEventAdaptorServiceDataHolder.registerEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        if (log.isDebugEnabled()) {
            log.debug("Un-Setting the EventStreamService reference for the UILocalEventAdaptor Service");
        }
        WebsocketEventAdaptorServiceDataHolder.registerEventStreamService(null);
    }

    /**
     * Sets Registry Service.
     *
     * @param registryService An instance of RegistryService
     */
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Registry Service");
        }
        WebsocketEventAdaptorServiceDataHolder.setRegistryService(registryService);
    }

    /**
     * Unsets Registry Service.
     *
     * @param registryService An instance of RegistryService
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Un setting Registry Service");
        }
        WebsocketEventAdaptorServiceDataHolder.setRegistryService(null);
    }
}
