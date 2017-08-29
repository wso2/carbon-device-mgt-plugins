/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.input.adapter.thrift.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.databridge.core.DataBridgeSubscriberService;
import org.wso2.carbon.device.mgt.input.adapter.thrift.ThriftAdapter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * common place to hold some OSGI bundle references.
 */
public final class ThriftEventAdapterServiceHolder {

    private static DataBridgeSubscriberService dataBridgeSubscriberService;
    private static ConfigurationContext configurationContext;

    private static ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ThriftAdapter>>>
            inputEventAdapterListenerMap =
            new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, ThriftAdapter>>>();

    private ThriftEventAdapterServiceHolder() {
    }

    public static void registerDataBridgeSubscriberService(
            DataBridgeSubscriberService dataBridgeSubscriberService) {
        ThriftEventAdapterServiceHolder.dataBridgeSubscriberService = dataBridgeSubscriberService;
    }

    public static DataBridgeSubscriberService getDataBridgeSubscriberService() {
        return dataBridgeSubscriberService;
    }

    public static synchronized void registerAdapterService(String tenantDomain, String streamId,
                                                           ThriftAdapter thriftAdapter) {

        ConcurrentHashMap<String, ConcurrentHashMap<String, ThriftAdapter>>
                tenantSpecificInputEventAdapterListenerMap = inputEventAdapterListenerMap.get(tenantDomain);

        if (tenantSpecificInputEventAdapterListenerMap == null) {
            tenantSpecificInputEventAdapterListenerMap =
                    new ConcurrentHashMap<String, ConcurrentHashMap<String, ThriftAdapter>>();
            inputEventAdapterListenerMap.put(tenantDomain, tenantSpecificInputEventAdapterListenerMap);
        }
        ConcurrentHashMap<String, ThriftAdapter> streamSpecificInputEventAdapterListenerMap =
                tenantSpecificInputEventAdapterListenerMap.get(streamId);
        if (streamSpecificInputEventAdapterListenerMap == null) {
            streamSpecificInputEventAdapterListenerMap = new ConcurrentHashMap<String, ThriftAdapter>();
            tenantSpecificInputEventAdapterListenerMap.put(streamId, streamSpecificInputEventAdapterListenerMap);
        }
        streamSpecificInputEventAdapterListenerMap.put(thriftAdapter.getEventAdapterName(), thriftAdapter);


    }

    public static void unregisterAdapterService(String tenantDomain, String streamId,
                                                ThriftAdapter thriftAdapter) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, ThriftAdapter>>
                tenantSpecificInputEventAdapterListenerMap = inputEventAdapterListenerMap.get(tenantDomain);

        if (tenantSpecificInputEventAdapterListenerMap != null) {
            ConcurrentHashMap<String, ThriftAdapter> streamSpecificInputEventAdapterListenerMap =
                    tenantSpecificInputEventAdapterListenerMap.get(streamId);
            if (streamSpecificInputEventAdapterListenerMap != null) {
                streamSpecificInputEventAdapterListenerMap.remove(thriftAdapter.getEventAdapterName());
            }
        }
    }

    public static ConcurrentHashMap<String, ThriftAdapter> getAdapterService(String tenantDomain, String streamId) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, ThriftAdapter>>
                tenantSpecificInputEventAdapterListenerMap = inputEventAdapterListenerMap.get(tenantDomain);
        if (tenantSpecificInputEventAdapterListenerMap != null) {
            return tenantSpecificInputEventAdapterListenerMap.get(streamId);
        }
        return null;
    }

    public static ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public static void setConfigurationContext(ConfigurationContext configurationContext) {
        ThriftEventAdapterServiceHolder.configurationContext = configurationContext;
    }
}
