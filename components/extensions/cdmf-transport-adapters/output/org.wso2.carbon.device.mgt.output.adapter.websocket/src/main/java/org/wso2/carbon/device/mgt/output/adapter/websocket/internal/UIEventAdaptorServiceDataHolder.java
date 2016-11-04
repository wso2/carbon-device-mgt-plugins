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

import org.wso2.carbon.device.mgt.output.adapter.websocket.UIOutputCallbackControllerServiceImpl;
import org.wso2.carbon.event.stream.core.EventStreamService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Creates a holder of type UIOutputCallbackRegisterServiceImpl.
 */
public final class UIEventAdaptorServiceDataHolder {

    private static UIOutputCallbackControllerServiceImpl UIOutputCallbackRegisterServiceImpl;
    private static ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>>
            tenantSpecificOutputEventStreamAdapterMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, ConcurrentHashMap<String, LinkedBlockingDeque<Object>>>
            tenantSpecificStreamEventMap = new ConcurrentHashMap<>();
    private static EventStreamService eventStreamService;

    public static void registerEventStreamService(EventStreamService eventBuilderService) {
        UIEventAdaptorServiceDataHolder.eventStreamService = eventBuilderService;
    }

    public static EventStreamService getEventStreamService() {
        return UIEventAdaptorServiceDataHolder.eventStreamService;
    }

    public static void registerUIOutputCallbackRegisterServiceInternal(
            UIOutputCallbackControllerServiceImpl UIOutputCallbackRegisterServiceImpl) {
        UIEventAdaptorServiceDataHolder.UIOutputCallbackRegisterServiceImpl =
                UIOutputCallbackRegisterServiceImpl;
    }

    public static UIOutputCallbackControllerServiceImpl getUIOutputCallbackRegisterServiceImpl() {
        return UIEventAdaptorServiceDataHolder.UIOutputCallbackRegisterServiceImpl;
    }

    public static ConcurrentHashMap<Integer,ConcurrentHashMap<String, String>>
    getTenantSpecificOutputEventStreamAdapterMap() {
        return tenantSpecificOutputEventStreamAdapterMap;
    }

    public static ConcurrentHashMap<Integer, ConcurrentHashMap<String, LinkedBlockingDeque<Object>>>
    getTenantSpecificStreamEventMap() {
        return tenantSpecificStreamEventMap;
    }
}
