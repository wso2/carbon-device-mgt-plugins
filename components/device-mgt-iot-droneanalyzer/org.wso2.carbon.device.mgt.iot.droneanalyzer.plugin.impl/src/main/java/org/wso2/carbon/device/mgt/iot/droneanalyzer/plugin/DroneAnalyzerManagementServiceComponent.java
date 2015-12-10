package org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.impl.DroneAnalyzerManagerService;
import org.wso2.carbon.device.mgt.iot.service.DeviceTypeService;

/**
 * Created by geesara on 12/9/15.
 */
public class DroneAnalyzerManagementServiceComponent {
    private ServiceRegistration firealarmServiceRegRef;

    private static final Log log = LogFactory.getLog(DroneAnalyzerManagementServiceComponent.class);

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating Drone Device Management Service Component");
        }
        try {
            BundleContext bundleContext = ctx.getBundleContext();
            firealarmServiceRegRef =
                    bundleContext.registerService(DeviceManagementService.class.getName(),
                            new DroneAnalyzerManagerService(), null);
            if (log.isDebugEnabled()) {
                log.debug("Drone Device Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Drone Device Management Service Component", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Virtual Firealarm Device Management Service Component");
        }
        try {
            if (firealarmServiceRegRef != null) {
                firealarmServiceRegRef.unregister();
            }
            if (log.isDebugEnabled()) {
                log.debug(
                        "Virtual Firealarm Device Management Service Component has been successfully de-activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while de-activating Virtual Firealarm Device Management bundle", e);
        }
    }

    protected void setDeviceTypeService(DeviceTypeService deviceTypeService) {
        if (log.isDebugEnabled()) {
            log.debug("Data source service set to mobile service component");
        }
    }

    protected void unsetDeviceTypeService(DeviceTypeService deviceTypeService) {
        //do nothing
    }
}
