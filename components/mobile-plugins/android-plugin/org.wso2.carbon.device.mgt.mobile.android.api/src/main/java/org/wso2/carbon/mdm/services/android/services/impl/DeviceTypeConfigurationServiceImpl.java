/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.mdm.services.android.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.mdm.services.android.exception.AndroidAgentException;
import org.wso2.carbon.mdm.services.android.services.DeviceTypeConfigurationService;
import org.wso2.carbon.mdm.services.android.util.AndroidAPIUtils;
import org.wso2.carbon.mdm.services.android.util.AndroidConstants;
import org.wso2.carbon.mdm.services.android.util.Message;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/configuration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceTypeConfigurationServiceImpl implements DeviceTypeConfigurationService {

    private static final Log log = LogFactory.getLog(DeviceTypeConfigurationServiceImpl.class);
    
    @POST
    @Override
    public Response addConfiguration(PlatformConfiguration configuration) throws AndroidAgentException {
        Message responseMsg = new Message();
        String msg;
        ConfigurationEntry licenseEntry = null;
        try {
            configuration.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
            List<ConfigurationEntry> configs = configuration.getConfiguration();
            for (ConfigurationEntry entry : configs) {
                if (AndroidConstants.TenantConfigProperties.LICENSE_KEY.equals(entry.getName())) {
                    License license = new License();
                    license.setName(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
                    license.setLanguage(AndroidConstants.TenantConfigProperties.LANGUAGE_US);
                    license.setVersion("1.0.0");
                    license.setText(entry.getValue().toString());
                    AndroidAPIUtils.getDeviceManagementService().addLicense(DeviceManagementConstants.
                            MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID, license);
                    licenseEntry = entry;
                    break;
                }
            }

            if (licenseEntry != null) {
                configs.remove(licenseEntry);
            }
            configuration.setConfiguration(configs);
            AndroidAPIUtils.getDeviceManagementService().saveConfiguration(configuration);
            Response.status(Response.Status.CREATED);
            responseMsg.setResponseMessage("Android platform configuration saved successfully.");
            responseMsg.setResponseCode(Response.Status.CREATED.toString());
        } catch (DeviceManagementException e) {
            msg = "Error occurred while configuring the android platform";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.CREATED).entity(responseMsg).build();
    }

    @GET
    @Override
    public Response getConfiguration() throws AndroidAgentException {
        String msg;
        PlatformConfiguration PlatformConfiguration = null;
        List<ConfigurationEntry> configs;
        try {
            PlatformConfiguration = AndroidAPIUtils.getDeviceManagementService().
                    getConfiguration(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
            if (PlatformConfiguration != null) {
                configs = PlatformConfiguration.getConfiguration();
            } else {
                PlatformConfiguration = new PlatformConfiguration();
                configs = new ArrayList<>();
            }

            ConfigurationEntry entry = new ConfigurationEntry();
            License license = AndroidAPIUtils.getDeviceManagementService().getLicense(
                    DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID, AndroidConstants.
                    TenantConfigProperties.LANGUAGE_US);

            if (license != null && configs != null) {
                entry.setContentType(AndroidConstants.TenantConfigProperties.CONTENT_TYPE_TEXT);
                entry.setName(AndroidConstants.TenantConfigProperties.LICENSE_KEY);
                entry.setValue(license.getText());
                configs.add(entry);
                PlatformConfiguration.setConfiguration(configs);
            }
        } catch (DeviceManagementException e) {
            msg = "Error occurred while retrieving the Android tenant configuration";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(PlatformConfiguration).build();
    }

    @PUT
    @Override
    public Response updateConfiguration(PlatformConfiguration configuration) throws AndroidAgentException {
        String msg;
        Message responseMsg = new Message();
        ConfigurationEntry licenseEntry = null;
        try {
            configuration.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
            List<ConfigurationEntry> configs = configuration.getConfiguration();
            for (ConfigurationEntry entry : configs) {
                if (AndroidConstants.TenantConfigProperties.LICENSE_KEY.equals(entry.getName())) {
                    License license = new License();
                    license.setName(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
                    license.setLanguage(AndroidConstants.TenantConfigProperties.LANGUAGE_US);
                    license.setVersion("1.0.0");
                    license.setText(entry.getValue().toString());
                    AndroidAPIUtils.getDeviceManagementService().addLicense(DeviceManagementConstants.
                            MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID, license);
                    licenseEntry = entry;
                }
            }

            if (licenseEntry != null) {
                configs.remove(licenseEntry);
            }
            configuration.setConfiguration(configs);
            AndroidAPIUtils.getDeviceManagementService().saveConfiguration(configuration);
            //AndroidAPIUtils.getGCMService().resetTenantConfigCache();
            Response.status(Response.Status.ACCEPTED);
            responseMsg.setResponseMessage("Android platform configuration has updated successfully.");
            responseMsg.setResponseCode(Response.Status.ACCEPTED.toString());
        } catch (DeviceManagementException e) {
            msg = "Error occurred while modifying configuration settings of Android platform";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.CREATED).entity(responseMsg).build();
    }


    @GET
    @Path("license")
    @Produces("text/html")
    public Response getLicense() throws AndroidAgentException {
        License license = null;

        try {
            license =
                    AndroidAPIUtils.getDeviceManagementService().getLicense(
                            DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID,
                            DeviceManagementConstants.LanguageCodes.LANGUAGE_CODE_ENGLISH_US);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the license configured for Android device enrolment";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity((license == null) ? null : license.getText()).build();
    }

}
