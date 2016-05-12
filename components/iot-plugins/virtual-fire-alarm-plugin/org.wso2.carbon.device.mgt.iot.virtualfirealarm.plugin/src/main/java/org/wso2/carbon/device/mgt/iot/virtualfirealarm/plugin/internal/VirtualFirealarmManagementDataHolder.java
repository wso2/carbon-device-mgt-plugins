/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.internal;

import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherService;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.VirtualFireAlarmManagerService;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;

/**
 * DataHolder class of virtual firealarm plugins component.
 */
public class VirtualFirealarmManagementDataHolder {

    private OutputEventAdapterService outputEventAdapterService;
    private InputEventAdapterService inputEventAdapterService;
    private EventsPublisherService eventsPublisherService;
    private CertificateManagementService certificateManagementService;

	private static VirtualFirealarmManagementDataHolder thisInstance = new VirtualFirealarmManagementDataHolder();

	private VirtualFirealarmManagementDataHolder() {
	}

	public static VirtualFirealarmManagementDataHolder getInstance() {
		return thisInstance;
	}

    public OutputEventAdapterService getOutputEventAdapterService() {
        return outputEventAdapterService;
    }

    public void setOutputEventAdapterService(
            OutputEventAdapterService outputEventAdapterService) {
        this.outputEventAdapterService = outputEventAdapterService;
    }

    public InputEventAdapterService getInputEventAdapterService() {
        return inputEventAdapterService;
    }

    public void setInputEventAdapterService(InputEventAdapterService inputEventAdapterService) {
        this.inputEventAdapterService = inputEventAdapterService;
    }

    public EventsPublisherService getEventsPublisherService() {
        return eventsPublisherService;
    }

    public void setEventsPublisherService(
            EventsPublisherService eventsPublisherService) {
        this.eventsPublisherService = eventsPublisherService;
    }

    public CertificateManagementService getCertificateManagementService() {
        return certificateManagementService;
    }

    public void setCertificateManagementService(CertificateManagementService certificateManagementService) {
        this.certificateManagementService = certificateManagementService;
    }
}
