/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherService;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.config.VirtualFirealarmConfig;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.VirtualFirealarmSecurityManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.VirtualFirealarmStartupListener;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;

/**
 * @scr.component name="org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.internal
 * .VirtualFirealarmManagementServiceComponent"
 * immediate="true"
 * @scr.reference name="event.input.adapter.service"
 * interface="org.wso2.carbon.event.input.adapter.core.InputEventAdapterService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setInputEventAdapterService"
 * unbind="unsetInputEventAdapterService"
 * @scr.reference name="certificate.management.service"
 * interface="org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setCertificateManagementService"
 * unbind="unsetCertificateManagementService"
 * @scr.reference name="event.publisher.service"
 * interface="org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setEventsPublisherService"
 * unbind="unsetEventsPublisherService"
 */
public class VirtualFirealarmManagementServiceComponent {

    private static final Log log = LogFactory.getLog(VirtualFirealarmManagementServiceComponent.class);
    private ServiceRegistration firealarmServiceRegRef;

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating Virtual Firealarm Device Management Service Component");
        }
        try {
            VirtualFirealarmConfig.initialize();
            BundleContext bundleContext = ctx.getBundleContext();
            bundleContext.registerService(ServerStartupObserver.class.getName(), new VirtualFirealarmStartupListener(),
                                          null);
            if (log.isDebugEnabled()) {
                log.debug("Virtual Firealarm Device Management Service Component has been successfully activated");
            }
            VirtualFirealarmSecurityManager.initVerificationManager();
        } catch (Throwable e) {
            log.error("Error occurred while activating Virtual Firealarm Device Management Service Component", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Virtual Firealarm Device Management Service Component");
        }
    }

    /**
     * Initialize the Input EventAdapter Service dependency
     *
     * @param inputEventAdapterService Input EventAdapter Service reference
     */
    protected void setInputEventAdapterService(InputEventAdapterService inputEventAdapterService) {
        VirtualFirealarmManagementDataHolder.getInstance().setInputEventAdapterService(inputEventAdapterService);
    }

    /**
     * De-reference the Input EventAdapter Service dependency.
     */
    protected void unsetInputEventAdapterService(InputEventAdapterService inputEventAdapterService) {
        VirtualFirealarmManagementDataHolder.getInstance().setInputEventAdapterService(null);
    }

    protected void setCertificateManagementService(CertificateManagementService certificateManagementService) {
        VirtualFirealarmManagementDataHolder.getInstance().setCertificateManagementService(certificateManagementService);
    }

    protected void unsetCertificateManagementService(CertificateManagementService certificateManagementService) {
        VirtualFirealarmManagementDataHolder.getInstance().setCertificateManagementService(null);
    }

    protected void setEventsPublisherService(EventsPublisherService eventsPublisherService) {
        VirtualFirealarmManagementDataHolder.getInstance().setEventsPublisherService(eventsPublisherService);
    }

    protected void unsetEventsPublisherService(EventsPublisherService eventsPublisherService) {
        VirtualFirealarmManagementDataHolder.getInstance().setEventsPublisherService(null);
    }
}
