/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.device.mgt.output.adapter.websocket.WebsocketOutputCallbackControllerServiceImpl;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Creates a holder of type UIOutputCallbackRegisterServiceImpl.
 */
public final class WebsocketEventAdaptorServiceDataHolder {

    private static WebsocketOutputCallbackControllerServiceImpl UIOutputCallbackRegisterServiceImpl;
    private static ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>>
            tenantSpecificOutputEventStreamAdapterMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, ConcurrentHashMap<String, LinkedBlockingDeque<Object>>>
            tenantSpecificStreamEventMap = new ConcurrentHashMap<>();
    private static EventStreamService eventStreamService;
    private static RegistryService registryService;

    public static void registerEventStreamService(EventStreamService eventBuilderService) {
        WebsocketEventAdaptorServiceDataHolder.eventStreamService = eventBuilderService;
    }

    public static EventStreamService getEventStreamService() {
        return WebsocketEventAdaptorServiceDataHolder.eventStreamService;
    }

    public static void registerUIOutputCallbackRegisterServiceInternal(
            WebsocketOutputCallbackControllerServiceImpl UIOutputCallbackRegisterServiceImpl) {
        WebsocketEventAdaptorServiceDataHolder.UIOutputCallbackRegisterServiceImpl =
                UIOutputCallbackRegisterServiceImpl;
    }

    public static WebsocketOutputCallbackControllerServiceImpl getUIOutputCallbackRegisterServiceImpl() {
        return WebsocketEventAdaptorServiceDataHolder.UIOutputCallbackRegisterServiceImpl;
    }

    public static ConcurrentHashMap<Integer,ConcurrentHashMap<String, String>>
    getTenantSpecificOutputEventStreamAdapterMap() {
        return tenantSpecificOutputEventStreamAdapterMap;
    }

    public static ConcurrentHashMap<Integer, ConcurrentHashMap<String, LinkedBlockingDeque<Object>>>
    getTenantSpecificStreamEventMap() {
        return tenantSpecificStreamEventMap;
    }

    public static RegistryService getRegistryService() {
        if (registryService == null) {
            throw new IllegalStateException("Registry service is not initialized properly");
        }
        return registryService;
    }

    public static void setRegistryService(RegistryService registryService) {
        WebsocketEventAdaptorServiceDataHolder.registryService = registryService;
    }
}
