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
package org.wso2.carbon.device.mgt.input.adapter.thrift;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.input.adapter.thrift.internal.ThriftEventAdapterServiceHolder;
import org.wso2.carbon.event.input.adapter.core.EventAdapterConstants;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;

import java.util.Map;

public final class ThriftAdapter implements InputEventAdapter {

    private static final Log log = LogFactory.getLog(ThriftAdapter.class);
    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private InputEventAdapterListener eventAdaptorListener;

    public ThriftAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                         Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init(InputEventAdapterListener eventAdaptorListener) throws InputEventAdapterException {
        this.eventAdaptorListener = eventAdaptorListener;
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        String streamId = eventAdapterConfiguration.getInputStreamIdOfWso2eventMessageFormat();
        ThriftEventAdapterServiceHolder.registerAdapterService(tenantDomain, streamId, this);
    }

    @Override
    public void disconnect() {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        String streamId = eventAdapterConfiguration.getInputStreamIdOfWso2eventMessageFormat();
        ThriftEventAdapterServiceHolder.unregisterAdapterService(tenantDomain, streamId, this);
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isEventDuplicatedInCluster() {
        return Boolean.parseBoolean(eventAdapterConfiguration.getProperties().get(
                EventAdapterConstants.EVENTS_DUPLICATED_IN_CLUSTER));
    }

    @Override
    public boolean isPolling() {
        return false;
    }

    public String getEventAdapterName() {
        return eventAdapterConfiguration.getName();
    }

    public InputEventAdapterListener getEventAdaptorListener() {
        return eventAdaptorListener;
    }
}
