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
import org.wso2.carbon.mdm.services.android.bean.AndroidPlatformConfiguration;
import org.wso2.carbon.mdm.services.android.bean.ErrorResponse;
import org.wso2.carbon.mdm.services.android.exception.UnexpectedServerErrorException;
import org.wso2.carbon.mdm.services.android.services.DeviceTypeConfigurationService;
import org.wso2.carbon.mdm.services.android.util.AndroidAPIUtils;
import org.wso2.carbon.mdm.services.android.util.AndroidConstants;
import org.wso2.carbon.mdm.services.android.util.Message;

import javax.validation.Valid;
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

    @GET
    @Override
    public Response getConfiguration(
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        String msg;
        PlatformConfiguration platformConfiguration;
        List<ConfigurationEntry> configs;
        try {
            platformConfiguration = AndroidAPIUtils.getDeviceManagementService().
                    getConfiguration(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
            if (platformConfiguration != null) {
                configs = platformConfiguration.getConfiguration();
            } else {
                platformConfiguration = new PlatformConfiguration();
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
                platformConfiguration.setConfiguration(configs);
            }
        } catch (DeviceManagementException e) {
            msg = "Error occurred while retrieving the Android tenant configuration";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity(platformConfiguration).build();
    }

    @PUT
    @Override
    public Response updateConfiguration(@Valid AndroidPlatformConfiguration androidPlatformConfiguration) {
        String msg;
        ConfigurationEntry licenseEntry = null;
        PlatformConfiguration configuration = new PlatformConfiguration();
        if (androidPlatformConfiguration == null) {
            String errorMessage = "The payload of the android platform configuration is incorrect.";
            log.error(errorMessage);
            throw new org.wso2.carbon.mdm.services.android.exception.BadRequestException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(errorMessage).build());
        }
        configuration.setConfiguration(androidPlatformConfiguration.getConfiguration());
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
        } catch (DeviceManagementException e) {
            msg = "Error occurred while modifying configuration settings of Android platform";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity("Android platform configuration has been updated successfully.").build();
    }


    @GET
    @Path("/license")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLicense(
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        License license;
        try {
            license =
                    AndroidAPIUtils.getDeviceManagementService().getLicense(
                            DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID,
                            DeviceManagementConstants.LanguageCodes.LANGUAGE_CODE_ENGLISH_US);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the license configured for Android device enrolment";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.OK).entity((license == null) ? null : license.getText()).build();
    }

}
