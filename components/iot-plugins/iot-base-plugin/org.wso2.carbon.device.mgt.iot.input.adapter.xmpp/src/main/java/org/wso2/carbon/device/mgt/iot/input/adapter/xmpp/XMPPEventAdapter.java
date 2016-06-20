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

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.iot.input.adapter.xmpp.util.XMPPAdapterListener;
import org.wso2.carbon.device.mgt.iot.input.adapter.xmpp.util.XMPPServerConnectionConfiguration;
import org.wso2.carbon.device.mgt.iot.input.adapter.xmpp.util.XMPPEventAdapterConstants;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Input XMPPEventAdapter will be used to receive events with XMPP protocol using specified broker and topic.
 */
public class XMPPEventAdapter implements InputEventAdapter {

    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private InputEventAdapterListener eventAdapterListener;
    private final String id = UUID.randomUUID().toString();
    private XMPPAdapterListener xmppAdapterListener;
    private XMPPServerConnectionConfiguration xmppServerConnectionConfiguration;


    public XMPPEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                            Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init(InputEventAdapterListener eventAdapterListener) throws InputEventAdapterException {
        this.eventAdapterListener = eventAdapterListener;
        try {
            int xmppPort = XMPPEventAdapterConstants.DEFAULT_XMPP_PORT;
            String xmppPortString = eventAdapterConfiguration.getProperties()
                    .get(XMPPEventAdapterConstants.ADAPTER_CONF_PORT);
            if (xmppPortString != null && !xmppPortString.trim().isEmpty()) {
                xmppPort = Integer.parseInt(xmppPortString);
            }
            int timeoutInterval = XMPPEventAdapterConstants.DEFAULT_TIMEOUT_INTERVAL;
            String timeoutIntervalString = eventAdapterConfiguration.getProperties().get(
                    XMPPEventAdapterConstants.ADAPTER_CONF_TIMEOUT_INTERVAL);
            if (timeoutIntervalString != null && !timeoutIntervalString.trim().isEmpty()) {
                timeoutInterval = Integer.parseInt(timeoutIntervalString);
            }
            xmppServerConnectionConfiguration =
                    new XMPPServerConnectionConfiguration(eventAdapterConfiguration.getProperties()
                        .get(XMPPEventAdapterConstants.ADAPTER_CONF_HOST),
                        xmppPort,
                        eventAdapterConfiguration.getProperties().get(XMPPEventAdapterConstants.ADAPTER_CONF_USERNAME),
                        eventAdapterConfiguration.getProperties().get(XMPPEventAdapterConstants.ADAPTER_CONF_PASSWORD),
                        timeoutInterval,
                        eventAdapterConfiguration.getProperties().get(XMPPEventAdapterConstants.ADAPTER_CONF_RESOURCE),
                        eventAdapterConfiguration.getProperties().get(XMPPEventAdapterConstants
                                                                          .ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME),
                        eventAdapterConfiguration.getProperties().get(XMPPEventAdapterConstants.ADAPTER_CONF_RECIEVER_JID),
                        eventAdapterConfiguration.getProperties().get(XMPPEventAdapterConstants
                                                                              .ADAPTER_CONF_CONTENT_TRANSFORMER_CLASSNAME)
                    );

            xmppAdapterListener = new XMPPAdapterListener(xmppServerConnectionConfiguration,
                    eventAdapterListener, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true));

        } catch (Throwable t) {
            throw new InputEventAdapterException(t.getMessage(), t);
        }
    }

    private String[] splitOnFirst(String str, char c) {
        int idx = str.indexOf(c);
        String head = str.substring(0, idx);
        String tail = str.substring(idx + 1);
        return new String[] { head, tail} ;
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        xmppAdapterListener.createConnection();
    }

    @Override
    public void disconnect() {
        if (xmppAdapterListener != null) {
            xmppAdapterListener.stopListener(eventAdapterConfiguration.getName());
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof XMPPEventAdapter)) return false;

        XMPPEventAdapter that = (XMPPEventAdapter) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


    @Override
    public boolean isEventDuplicatedInCluster() {
        return true;
    }

    @Override
    public boolean isPolling() {
        return true;
    }

}
