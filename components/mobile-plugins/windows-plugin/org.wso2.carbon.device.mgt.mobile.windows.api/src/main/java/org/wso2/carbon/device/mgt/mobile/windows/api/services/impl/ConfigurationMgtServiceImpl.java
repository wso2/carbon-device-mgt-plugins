/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.mobile.windows.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.PluginConstants;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.UnexpectedServerErrorException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsConfigurationException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.util.WindowsAPIUtils;
import org.wso2.carbon.device.mgt.mobile.windows.api.services.ConfigurationMgtService;
import javax.ws.rs.core.MediaType;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@WebService
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Path("/configuration")
public class ConfigurationMgtServiceImpl implements ConfigurationMgtService {

    private static Log log = LogFactory.getLog(
            org.wso2.carbon.device.mgt.mobile.windows.api.services.impl.ConfigurationMgtServiceImpl.class);

    /**
     * Retrieve Tenant configurations according to the device type.
     *
     * @return Tenant configuration object contains specific tenant configurations.
     * @throws WindowsConfigurationException
     */
    @GET
    public Response getConfiguration(@HeaderParam("If-Modified-Since") String ifModifiedSince) {
        String msg;
        PlatformConfiguration platformConfiguration;
        List<ConfigurationEntry> configs;
        try {
            platformConfiguration = WindowsAPIUtils.getDeviceManagementService().
                    getConfiguration(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
            if (platformConfiguration != null) {
                configs = platformConfiguration.getConfiguration();
            } else {
                platformConfiguration = new PlatformConfiguration();
                configs = new ArrayList<>();
            }

            ConfigurationEntry entry = new ConfigurationEntry();
            License license = WindowsAPIUtils.getDeviceManagementService().getLicense(
                    DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS,
                    PluginConstants.TenantConfigProperties.LANGUAGE_US);

            if (license != null && configs != null) {
                entry.setContentType(PluginConstants.TenantConfigProperties.CONTENT_TYPE_TEXT);
                entry.setName(PluginConstants.TenantConfigProperties.LICENSE_KEY);
                entry.setValue(license.getText());
                configs.add(entry);
                platformConfiguration.setConfiguration(configs);
            }
        } catch (DeviceManagementException e) {
            msg = "Error occurred while retrieving the Windows tenant configuration";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity(platformConfiguration).build();
    }

    /**
     * Update Tenant Configurations for the specific Device type.
     *
     * @param configurations to be updated.
     * @return Response message.
     * @throws WindowsConfigurationException
     */
    @PUT
    public Response updateConfiguration(PlatformConfiguration windowsPlatformConfiguration) throws WindowsConfigurationException {
        String message;
        ConfigurationEntry licenseEntry = null;
        PlatformConfiguration configuration = new PlatformConfiguration();
        try {
            configuration.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
            List<ConfigurationEntry> configs = configuration.getConfiguration();
            for (ConfigurationEntry entry : configs) {
                if (PluginConstants.TenantConfigProperties.LICENSE_KEY.equals(entry.getName())) {
                    License license = new License();
                    license.setName(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS);
                    license.setLanguage(PluginConstants.TenantConfigProperties.LANGUAGE_US);
                    license.setVersion("1.0.0");
                    license.setText(entry.getValue().toString());
                    WindowsAPIUtils.getDeviceManagementService().addLicense(DeviceManagementConstants.
                            MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS, license);
                    licenseEntry = entry;
                }
            }

            if (licenseEntry != null) {
                configs.remove(licenseEntry);
            }
            configuration.setConfiguration(configs);
            WindowsAPIUtils.getDeviceManagementService().saveConfiguration(configuration);
            Response.status(Response.Status.CREATED);
        } catch (DeviceManagementException e) {
            message = "Error occurred while modifying configuration settings of Windows platform.";
            log.error(message, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(message).build());
        }
        return Response.status(Response.Status.OK).
                entity("Windows platform configuration has been updated successfully.").build();

    }

    @GET
    @Path("/license")
    public Response getLicense(
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        License license;
        try {
            license =
                    WindowsAPIUtils.getDeviceManagementService().getLicense(
                            DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_WINDOWS,
                            DeviceManagementConstants.LanguageCodes.LANGUAGE_CODE_ENGLISH_US);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the license configured for Windows device enrolment";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity(license).build();
    }
}

