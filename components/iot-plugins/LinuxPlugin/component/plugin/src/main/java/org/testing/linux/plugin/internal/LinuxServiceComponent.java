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

package org.testing.linux.plugin.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testing.linux.plugin.impl.LinuxManagerService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;

public class LinuxServiceComponent {
	private static final Log log =  LogFactory.getLog(LinuxServiceComponent.class);
    private ServiceRegistration linuxServiceRegRef;

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating Linux Service Component");
        }
        try {
            BundleContext bundleContext = ctx.getBundleContext();
            linuxServiceRegRef =
                    bundleContext.registerService(DeviceManagementService.class.getName(),
                                                  new LinuxManagerService(), null);

            if (log.isDebugEnabled()) {
                log.debug("Linux device has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating device", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Linux Service Component");
        }
        try {
            if (linuxServiceRegRef != null) {
                linuxServiceRegRef.unregister();
            }

            if (log.isDebugEnabled()) {
                log.debug("Linux Service Component has been successfully de-activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while de-activating linux Service Component", e);
        }
    }

}