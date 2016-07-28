package org.wso2.carbon.device.mgt.iot.devicetype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.iot.devicetype.config.DeviceManagementConfiguration;
import org.wso2.carbon.device.mgt.iot.devicetype.config.exception.DeviceTypeConfigurationException;
import org.wso2.carbon.device.mgt.iot.devicetype.util.DeviceTypeConfigUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DeviceTypeConfigServiceImpl implements DeviceTypeConfigService {

    private static final Log log = LogFactory.getLog(DeviceTypeConfigServiceImpl.class);
    private static final String DEVICE_TYPE_CONFIG_PATH =
            CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + "device-mgt-plugins";
    private Map<DeviceTypeConfigIdentifier, DeviceManagementConfiguration> deviceTypeConfigurationMap = new HashMap<>();

    public void initialize() {
        File configurationDirectory = new File(DEVICE_TYPE_CONFIG_PATH);
        File[] deviceTypeConfigurationFiles = configurationDirectory.listFiles();
        if (deviceTypeConfigurationFiles != null) {
            for (File file : deviceTypeConfigurationFiles) {
                String filename = file.getName();
                if (filename.endsWith(".xml") || filename.endsWith(".XML")) {
                    try {
                        DeviceManagementConfiguration deviceManagementConfiguration = getDeviceTypeConfiguration(file);
                        String deviceType = deviceManagementConfiguration.getDeviceType();
                        String tenantDomain = deviceManagementConfiguration.getDeviceManagementConfigRepository()
                                .getProvisioningConfig().getTenantDomain();
                        if (deviceType != null && !deviceType.isEmpty() && tenantDomain != null
                                && !tenantDomain.isEmpty()) {
                            deviceTypeConfigurationMap.put(new DeviceTypeConfigIdentifier(deviceType, tenantDomain),
                                                           deviceManagementConfiguration);
                        }

                    } catch (DeviceTypeConfigurationException e) {
                        //continue reading other files
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    private DeviceManagementConfiguration getDeviceTypeConfiguration(File configurationFile)
            throws DeviceTypeConfigurationException {
        try {
            Document doc = DeviceTypeConfigUtil.convertToDocument(configurationFile);

            /* Un-marshaling Webapp Authenticator configuration */
            JAXBContext ctx = JAXBContext.newInstance(DeviceManagementConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            //unmarshaller.setSchema(getSchema());
            return (DeviceManagementConfiguration) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new DeviceTypeConfigurationException("Error occurred while un-marshalling the file " +
                                                               configurationFile.getAbsolutePath(), e);
        }
    }

    @Override
    public DeviceManagementConfiguration getConfiguration(String deviceType, String tenantDomain) {
        return deviceTypeConfigurationMap.get(new DeviceTypeConfigIdentifier(deviceType, tenantDomain));
    }
}
