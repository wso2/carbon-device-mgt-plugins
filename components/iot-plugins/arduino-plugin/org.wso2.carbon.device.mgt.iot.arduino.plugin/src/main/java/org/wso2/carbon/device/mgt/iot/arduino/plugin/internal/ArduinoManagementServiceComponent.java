/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.iot.arduino.plugin.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.iot.arduino.plugin.exception.ArduinoDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.iot.arduino.plugin.impl.ArduinoManagerService;
import org.wso2.carbon.device.mgt.iot.arduino.plugin.impl.util.ArduinoUtils;
/**
 * @scr.component name="org.wso2.carbon.device.mgt.iot.arduino.internal.ArduinoManagementServiceComponent"
 * immediate="true"
 */
public class ArduinoManagementServiceComponent {

    private static final Log log = LogFactory.getLog(ArduinoManagementServiceComponent.class);
    private ServiceRegistration arduinoServiceRegRef;

    protected void activate(ComponentContext ctx) {
    	if (log.isDebugEnabled()) {
            log.debug("Activating Arduino Device Management Service Component");
        }
        try {
            BundleContext bundleContext = ctx.getBundleContext();
            arduinoServiceRegRef =
                    bundleContext.registerService(DeviceManagementService.class.getName(),
                                                  new ArduinoManagerService(), null);
            String setupOption = System.getProperty("setup");
            if (setupOption != null) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "-Dsetup is enabled. Iot Device management repository schema initialization is about " +
                                    "to begin");
                }
                try {
                    ArduinoUtils.setupDeviceManagementSchema();
                } catch (ArduinoDeviceMgtPluginException e) {
                    log.error("Exception occurred while initializing device management database schema", e);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Arduino Device Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Arduino Device Management Service Component", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Arduino Device Management Service Component");
        }
        try {
            if (arduinoServiceRegRef != null) {
                arduinoServiceRegRef.unregister();
            }

            if (log.isDebugEnabled()) {
                log.debug(
                        "Arduino Device Management Service Component has been successfully de-activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while de-activating Arduino Device Management bundle", e);
        }
    }
}
