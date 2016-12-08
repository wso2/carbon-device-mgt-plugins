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
package org.wso2.carbon.device.mgt.output.adapter.xmpp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.output.adapter.xmpp.util.XMPPEventAdapterConstants;
import org.wso2.carbon.device.mgt.output.adapter.xmpp.util.XMPPAdapterPublisher;
import org.wso2.carbon.device.mgt.output.adapter.xmpp.util.XMPPServerConnectionConfiguration;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Output XMPPEventAdapter will be used to publish events with MQTT protocol to specified broker and topic.
 */
public class XMPPEventAdapter implements OutputEventAdapter {

    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private XMPPAdapterPublisher xmppAdapterPublisher;
    private static ThreadPoolExecutor threadPoolExecutor;
    private static final Log log = LogFactory.getLog(XMPPEventAdapter.class);
    private int tenantId;

    public XMPPEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                            Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init() throws OutputEventAdapterException {
        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        //ThreadPoolExecutor will be assigned  if it is null
        if (threadPoolExecutor == null) {
            int minThread;
            int maxThread;
            int jobQueSize;
            long defaultKeepAliveTime;
            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(XMPPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME) != null) {
                minThread = Integer.parseInt(globalProperties.get(
                        XMPPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME));
            } else {
                minThread = XMPPEventAdapterConstants.DEFAULT_MIN_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(XMPPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME) != null) {
                maxThread = Integer.parseInt(globalProperties.get(
                        XMPPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME));
            } else {
                maxThread = XMPPEventAdapterConstants.DEFAULT_MAX_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(XMPPEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(globalProperties.get(
                        XMPPEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = XMPPEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS;
            }

            if (globalProperties.get(XMPPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
                jobQueSize = Integer.parseInt(globalProperties.get(
                        XMPPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME));
            } else {
                jobQueSize = XMPPEventAdapterConstants.DEFAULT_EXECUTOR_JOB_QUEUE_SIZE;
            }

            threadPoolExecutor = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime,
                                                        TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(
                    jobQueSize));
        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("Test connection is not available");
    }

    @Override
    public void connect() {
        int xmppPort = XMPPEventAdapterConstants.DEFAULT_XMPP_PORT;
        String xmppPortString = eventAdapterConfiguration.getStaticProperties()
                .get(XMPPEventAdapterConstants.ADAPTER_CONF_PORT);
        if (xmppPortString != null && !xmppPortString.trim().isEmpty()) {
            xmppPort = Integer.parseInt(xmppPortString);
        }
        int timeoutInterval = XMPPEventAdapterConstants.DEFAULT_TIMEOUT_INTERVAL;
        String timeoutIntervalString = eventAdapterConfiguration.getStaticProperties().get(
                XMPPEventAdapterConstants.ADAPTER_CONF_TIMEOUT_INTERVAL);
        if (timeoutIntervalString != null && !timeoutIntervalString.trim().isEmpty()) {
            timeoutInterval = Integer.parseInt(timeoutIntervalString);
        }
        XMPPServerConnectionConfiguration xmppServerConnectionConfiguration =
                new XMPPServerConnectionConfiguration(eventAdapterConfiguration.getStaticProperties()
                                                              .get(XMPPEventAdapterConstants.ADAPTER_CONF_HOST),
                                                      xmppPort,
                                                      eventAdapterConfiguration.getStaticProperties()
                                                              .get(XMPPEventAdapterConstants.ADAPTER_CONF_USERNAME),
                                                      eventAdapterConfiguration.getStaticProperties()
                                                              .get(XMPPEventAdapterConstants.ADAPTER_CONF_PASSWORD),
                                                      timeoutInterval,
                                                      eventAdapterConfiguration.getStaticProperties()
                                                              .get(XMPPEventAdapterConstants.ADAPTER_CONF_RESOURCE)
                );
        xmppAdapterPublisher = new XMPPAdapterPublisher(xmppServerConnectionConfiguration);
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {
        String jid = dynamicProperties.get(XMPPEventAdapterConstants.ADAPTER_CONF_JID);
        String subject = dynamicProperties.get(XMPPEventAdapterConstants.ADAPTER_CONF_SUBJECT);
        String messageType = dynamicProperties.get(XMPPEventAdapterConstants.ADAPTER_CONF_MESSAGETYPE);
        try {
            threadPoolExecutor.submit(new XMPPSender(jid, subject, (String)message, messageType));
        } catch (RejectedExecutionException e) {
            EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message, "Job queue is full", e, log,
                                        tenantId);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (xmppAdapterPublisher != null) {
                xmppAdapterPublisher.close();
                xmppAdapterPublisher = null;
            }
        } catch (OutputEventAdapterException e) {
            log.error("Exception when closing the mqtt publisher connection on Output MQTT Adapter '" +
                              eventAdapterConfiguration.getName() + "'", e);
        }
    }

    @Override
    public void destroy() {
        //not required
    }

    @Override
    public boolean isPolled() {
        return false;
    }

    class XMPPSender implements Runnable {

        String jid;
        String subject;
        String message;
        String messageType;

        XMPPSender(String jid, String subject, String message, String messageType) {
            this.jid = jid;
            this.message = message;
            this.subject = subject;
            this.messageType = messageType;
        }

        @Override
        public void run() {
            try {
                if (!xmppAdapterPublisher.isConnected()) {
                    synchronized (XMPPEventAdapter.class) {
                        if (!xmppAdapterPublisher.isConnected()) {
                            xmppAdapterPublisher.connect();
                        }
                    }
                }
                xmppAdapterPublisher.publish(jid, subject, message, messageType);
            } catch (Throwable t) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message, null, t, log, tenantId);
            }
        }
    }
}
