/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.appmgt.mdm.restconnector;

import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jaxrs.JAXRSContract;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.OAuthRequestInterceptor;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.ApplicationManagementAdminService;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.ApplicationWrapper;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.DeviceManagementAdminService;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.MobileApp;
import org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.MobileAppTypes;
import org.wso2.carbon.appmgt.mdm.restconnector.config.AuthorizationConfigurationManager;
import org.wso2.carbon.appmgt.mobile.beans.ApplicationOperationAction;
import org.wso2.carbon.appmgt.mobile.beans.ApplicationOperationDevice;
import org.wso2.carbon.appmgt.mobile.beans.DeviceIdentifier;
import org.wso2.carbon.appmgt.mobile.interfaces.ApplicationOperations;
import org.wso2.carbon.appmgt.mobile.mdm.App;
import org.wso2.carbon.appmgt.mobile.mdm.Device;
import org.wso2.carbon.appmgt.mobile.utils.MobileApplicationException;
import org.wso2.carbon.appmgt.mobile.utils.MobileConfigurations;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ApplicationOperationsImpl implements ApplicationOperations {
    private static final String CDMF_SERVER_BASE_CONTEXT = "/api/device-mgt/v1.0";
    private static DeviceManagementAdminService deviceManagementAdminService;
    private static final Log log = LogFactory.getLog(ApplicationOperationsImpl.class);
    private static ApplicationManagementAdminService applicationManagementAdminService;

    /**
     * Constructor.
     */
    public ApplicationOperationsImpl() {
        String authorizationConfigManagerServerURL = AuthorizationConfigurationManager.getInstance().getServerURL();
        OAuthRequestInterceptor oAuthRequestInterceptor = new OAuthRequestInterceptor();
        deviceManagementAdminService = Feign.builder()
                .requestInterceptor(oAuthRequestInterceptor)
                .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                .target(DeviceManagementAdminService.class,
                        authorizationConfigManagerServerURL + CDMF_SERVER_BASE_CONTEXT);
        applicationManagementAdminService = Feign.builder()
                .requestInterceptor(oAuthRequestInterceptor)
                .contract(new JAXRSContract()).encoder(new GsonEncoder()).decoder(new GsonDecoder())
                .target(ApplicationManagementAdminService.class,
                        authorizationConfigManagerServerURL + CDMF_SERVER_BASE_CONTEXT);
    }

    /**
     * Install, uninstall, reinstall application in devices.
     *
     * @param applicationOperationAction {@link ApplicationOperationAction} object
     * @return Activity id
     * @throws MobileApplicationException on errors while trying to perform action in devices
     */
    @Override
    public String performAction(ApplicationOperationAction applicationOperationAction)
            throws MobileApplicationException {
        ApplicationWrapper applicationWrapper = new ApplicationWrapper();
        MobileApp mobileApp = new MobileApp();

        String type = applicationOperationAction.getType();
        String[] params = applicationOperationAction.getParams();
        int tenantId = applicationOperationAction.getTenantId();

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        List<org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.Device> deviceList;
        if (Constants.USER.equals(type)) {
            String platform = applicationOperationAction.getApp().getPlatform();
            String userName;
            for (String param : params) {
                userName = param;
                deviceList = deviceManagementAdminService.getDevices(userName, null).getList();

                for (org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.Device device : deviceList) {
                    if (Constants.WEBAPP.equals(platform) || platform.equalsIgnoreCase(device.getType())) {
                        if (Constants.ACTIVE.equalsIgnoreCase(device.getEnrolmentInfo().getStatus().toString())) {
                            deviceIdentifiers.add(getDeviceIdentifierByDevice(device));
                        }
                    }
                }
            }
        } else if (Constants.ROLE.equals(type)) {
            String userRole;
            for (String param : applicationOperationAction.getParams()) {
                userRole = param;
                deviceList = deviceManagementAdminService.getDevices(null, userRole).getList();
                for (org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.Device device : deviceList) {
                    if (Constants.ACTIVE.equalsIgnoreCase(device.getEnrolmentInfo().getStatus().toString())) {
                        deviceIdentifiers.add(getDeviceIdentifierByDevice(device));
                    }
                }
            }
        } else if (Constants.DEVICE.equals(type)) {
            DeviceIdentifier deviceIdentifier;
            for (String param : params) {
                deviceIdentifier = new DeviceIdentifier();
                if (isValidJSON(param)) {
                    JSONParser parser = new JSONParser();
                    try {
                        JSONObject parsedObj = (JSONObject) parser.parse(param);
                        deviceIdentifier.setId((String) parsedObj.get(Constants.ID));
                        String deviceType = (String) parsedObj.get(Constants.TYPE);
                        deviceIdentifier.setType(deviceType);
                        deviceIdentifiers.add(deviceIdentifier);
                    } catch (ParseException e) {
                        throw new MobileApplicationException("Device Identifier is not valid json object.", e);
                    }
                }
            }
        } else {
            throw new IllegalStateException("Invalid type is received from app store.");
        }

        App app = applicationOperationAction.getApp();
        mobileApp.setId(app.getId());
        mobileApp.setType(MobileAppTypes.valueOf(app.getType().toUpperCase()));
        mobileApp.setAppIdentifier(app.getAppIdentifier());
        mobileApp.setIconImage(app.getIconImage());
        mobileApp.setIdentifier(app.getIdentifier());
        mobileApp.setLocation(app.getLocation());
        mobileApp.setName(app.getName());
        mobileApp.setPackageName(app.getPackageName());
        mobileApp.setPlatform(app.getPlatform());
        mobileApp.setVersion(app.getVersion());
        Properties mobileAppProperties = new Properties();

        if (Constants.IOS.equals(app.getPlatform())) {
            if (Constants.ENTERPRISE.equals(app.getType())) {
                mobileAppProperties.put(Constants.IOSConstants.IS_REMOVE_APP, true);
                mobileAppProperties.put(Constants.IOSConstants.IS_PREVENT_BACKUP, true);
            } else if (Constants.IOSConstants.PUBLIC.equals(app.getType())) {
                mobileAppProperties.put(Constants.IOSConstants.I_TUNES_ID, app.getIdentifier());
                mobileAppProperties.put(Constants.IOSConstants.IS_REMOVE_APP, true);
                mobileAppProperties.put(Constants.IOSConstants.IS_PREVENT_BACKUP, true);
            } else if (Constants.WEBAPP.equals(app.getType())) {
                mobileAppProperties.put(Constants.IOSConstants.LABEL, app.getName());
                mobileAppProperties.put(Constants.IOSConstants.IS_REMOVE_APP, true);
            }
        } else if (Constants.WEBAPP.equals(app.getPlatform())) {
            mobileAppProperties.put(Constants.IOSConstants.LABEL, app.getName());
            mobileAppProperties.put(Constants.IOSConstants.IS_REMOVE_APP, true);
        }
        mobileApp.setProperties(mobileAppProperties);
        applicationWrapper.setApplication(mobileApp);
        Activity activity = null;

        if (deviceIdentifiers.size() > 0) {
            applicationWrapper.setDeviceIdentifiers(deviceIdentifiers);
            if (Constants.INSTALL.equals(applicationOperationAction.getAction())) {
                activity = applicationManagementAdminService.installApplication(applicationWrapper);
            } else if (Constants.UPDATE.equals(applicationOperationAction.getAction())) {
                activity = applicationManagementAdminService.installApplication(applicationWrapper);
            } else {
                activity = applicationManagementAdminService.uninstallApplication(applicationWrapper);
            }
        }

        if (activity != null) {
            return activity.getActivityId();
        }
        return null;
    }

    /**
     * Get devices.
     *
     * @param applicationOperationDevice {@link ApplicationOperationAction} object
     * @return list of {@link Device} objects
     * @throws MobileApplicationException on errors while trying to get devices list
     */
    @Override
    public List<Device> getDevices(ApplicationOperationDevice applicationOperationDevice)
            throws MobileApplicationException {
        Map<String, String> queryParamsMap = new HashMap<>();
        String platform = applicationOperationDevice.getPlatform();
        String platformVersion = applicationOperationDevice.getPlatformVersion();
        String userName = applicationOperationDevice.getCurrentUser().getUsername();
        queryParamsMap.put(Constants.PLATFORM, platform);
        queryParamsMap.put(Constants.PLATFORM_VERSION, platformVersion);
        queryParamsMap.put(Constants.USER, userName);
        String type = applicationOperationDevice.getType();
        queryParamsMap.put(Constants.TYPE, type);
        List<org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.Device> deviceList =
                deviceManagementAdminService.getDevices(userName, null).getList();

        List<Device> processedDevices = new ArrayList<>();
        for (org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.Device device : deviceList) {
            Device processedDevice = new Device();
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(device.getDeviceIdentifier().toString());
            deviceIdentifier.setType(device.getType().toString());
            processedDevice.setDeviceIdentifier(deviceIdentifier);
            processedDevice.setName(device.getName());
            processedDevice.setModel(device.getName());
            processedDevice.setType(device.getType());
            String imgUrl;
            if (Constants.ANDROID.equalsIgnoreCase((device.getType().toString()))) {
                imgUrl = String.format(getActiveMDMProperties().get(Constants.PROPERTY_IMAGE_URL), Constants.NEXUS);
            } else if (Constants.IOS.equalsIgnoreCase((device.getType().toString()))) {
                imgUrl = String.format(getActiveMDMProperties().get(Constants.PROPERTY_IMAGE_URL), Constants.IPHONE);
            } else {
                imgUrl = String.format(getActiveMDMProperties().get(Constants.PROPERTY_IMAGE_URL), Constants.NONE);
            }
            processedDevice.setImage(imgUrl);
            processedDevice.setPlatform(device.getType().toString());
            processedDevices.add(processedDevice);
        }
        return processedDevices;

    }

    private HashMap<String, String> getActiveMDMProperties() {
        MobileConfigurations configurations = MobileConfigurations.getInstance();
        return configurations.getActiveMDMProperties();
    }

    private static DeviceIdentifier getDeviceIdentifierByDevice(
            org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto.Device device) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(device.getDeviceIdentifier());
        deviceIdentifier.setType(device.getType());

        return deviceIdentifier;
    }

    private boolean isValidJSON(String json) {
        JSONParser parser = new JSONParser();
        try {
            parser.parse(json);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    private void logError(String errorMessage, Throwable e) {
        if (log.isDebugEnabled()) {
            log.error(errorMessage, e);
        } else {
            log.error(errorMessage);
        }
    }
}