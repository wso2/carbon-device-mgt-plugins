/*
 * Copyright (c) 2015, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mdm.services.android.util;

import com.google.gson.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.api.AnalyticsDataAPIUtil;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherService;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementService;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.search.mgt.impl.Utils;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.mdm.services.android.bean.DeviceState;
import org.wso2.carbon.mdm.services.android.bean.ErrorResponse;
import org.wso2.carbon.mdm.services.android.exception.BadRequestException;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AndroidAPIUtil class provides utility functions used by Android REST-API classes.
 */
public class AndroidAPIUtils {

    private static Log log = LogFactory.getLog(AndroidAPIUtils.class);

    public static DeviceIdentifier convertToDeviceIdentifierObject(String deviceId) {
        DeviceIdentifier identifier = new DeviceIdentifier();
        identifier.setId(deviceId);
        identifier.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
        return identifier;
    }

    public static String getAuthenticatedUser() {
        PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String username = threadLocalCarbonContext.getUsername();
        String tenantDomain = threadLocalCarbonContext.getTenantDomain();
        if (username != null && username.endsWith(tenantDomain)) {
            return username.substring(0, username.lastIndexOf("@"));
        }
        return username;
    }

    public static DeviceManagementProviderService getDeviceManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceManagementProviderService deviceManagementProviderService =
                (DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
        if (deviceManagementProviderService == null) {
            String msg = "Device Management service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return deviceManagementProviderService;
    }

//    public static GCMService getGCMService() {
//        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
//        GCMService gcmService = (GCMService) ctx.getOSGiService(GCMService.class, null);
//        if (gcmService == null) {
//            String msg = "GCM service has not initialized.";
//            log.error(msg);
//            throw new IllegalStateException(msg);
//        }
//        return gcmService;
//    }

    public static MediaType getResponseMediaType(String acceptHeader) {
        MediaType responseMediaType;
        if (MediaType.WILDCARD.equals(acceptHeader)) {
            responseMediaType = MediaType.APPLICATION_JSON_TYPE;
        } else {
            responseMediaType = MediaType.valueOf(acceptHeader);
        }
        return responseMediaType;
    }

    public static Response getOperationResponse(List<String> deviceIDs, Operation operation)
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        if (deviceIDs == null || deviceIDs.size() == 0) {
            String errorMessage = "Device identifier list is empty";
            log.error(errorMessage);
            throw new BadRequestException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage(errorMessage).build());
        }
        DeviceIdentifier deviceIdentifier;
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for (String deviceId : deviceIDs) {
            deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(AndroidConstants.DEVICE_TYPE_ANDROID);
            deviceIdentifiers.add(deviceIdentifier);
        }
        Activity activity = getDeviceManagementService().addOperation(
                    DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID, operation, deviceIdentifiers);
//        if (activity != null) {
//            GCMService gcmService = getGCMService();
//            if (gcmService.isGCMEnabled()) {
//                List<DeviceIdentifier> deviceIDList = deviceIDHolder.getValidDeviceIDList();
//                List<Device> devices = new ArrayList<Device>(deviceIDList.size());
//                for (DeviceIdentifier deviceIdentifier : deviceIDList) {
//                    devices.add(getDeviceManagementService().getDevice(deviceIdentifier));
//                }
//                getGCMService().sendNotification(operation.getCode(), devices);
//            }
//        }
        return Response.status(Response.Status.CREATED).entity(activity).build();
    }


    public static PolicyManagerService getPolicyManagerService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        PolicyManagerService policyManagerService = (PolicyManagerService) ctx.getOSGiService(
                PolicyManagerService.class, null);
        if (policyManagerService == null) {
            String msg = "Policy Manager service has not initialized";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return policyManagerService;
    }

    public static ApplicationManagementProviderService getApplicationManagerService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ApplicationManagementProviderService applicationManagementProviderService =
                (ApplicationManagementProviderService) ctx.getOSGiService(ApplicationManagementProviderService.class, null);
        if (applicationManagementProviderService == null) {
            String msg = "Application Management provider service has not initialized";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return applicationManagementProviderService;
    }

    public static NotificationManagementService getNotificationManagementService() {
        NotificationManagementService notificationManagementService;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        notificationManagementService = (NotificationManagementService) ctx.getOSGiService(
                NotificationManagementService.class, null);
        if (notificationManagementService == null) {
            String msg = "Notification Management service not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return notificationManagementService;
    }

    public static EventsPublisherService getEventPublisherService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        EventsPublisherService eventsPublisherService =
                (EventsPublisherService) ctx.getOSGiService(EventsPublisherService.class, null);
        if (eventsPublisherService == null) {
            String msg = "Event Publisher service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return eventsPublisherService;
    }

    public static AnalyticsDataAPI getAnalyticsDataAPI() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        AnalyticsDataAPI analyticsDataAPI =
                (AnalyticsDataAPI) ctx.getOSGiService(AnalyticsDataAPI.class, null);
        if (analyticsDataAPI == null) {
            String msg = "Analytics api service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return analyticsDataAPI;
    }

    public static List<DeviceState> getAllEventsForDevice(String tableName, String query) throws AnalyticsException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        AnalyticsDataAPI analyticsDataAPI = getAnalyticsDataAPI();
        int eventCount = analyticsDataAPI.searchCount(tenantId, tableName, query);
        if (eventCount == 0) {
            return null;
        }
        List<SearchResultEntry> resultEntries = analyticsDataAPI.search(tenantId, tableName, query, 0, eventCount);
        List<String> recordIds = getRecordIds(resultEntries);
        AnalyticsDataResponse response = analyticsDataAPI.get(tenantId, tableName, 1, null, recordIds);
        Map<String, DeviceState> deviceStateses = createDeviceStatusData(AnalyticsDataAPIUtil.listRecords(
                analyticsDataAPI, response));
        return getSortedDeviceStateData(deviceStateses, resultEntries);
    }

    private static List<String> getRecordIds(List<SearchResultEntry> searchResults) {
        List<String> ids = new ArrayList();
        for (SearchResultEntry searchResult : searchResults) {
            ids.add(searchResult.getId());
        }
        return ids;
    }

    public static Map<String, DeviceState> createDeviceStatusData(List<Record> records) {
        Map<String, DeviceState> deviceStatuses = new HashMap();
        for (Record record : records) {
            DeviceState deviceState = createDeviceStatusData(record);
            deviceStatuses.put(deviceState.getId(), deviceState);
        }
        return deviceStatuses;
    }

    private static DeviceState createDeviceStatusData(Record record) {
        DeviceState deviceState = new DeviceState();
        deviceState.setId(record.getId());
        deviceState.setValues(record.getValues());
        return deviceState;
    }

    public static List<DeviceState> getSortedDeviceStateData(Map<String, DeviceState> sensorDatas,
                                                             List<SearchResultEntry> searchResults) {
        List<DeviceState> sortedRecords = new ArrayList();
        for (SearchResultEntry searchResultEntry : searchResults) {
            sortedRecords.add(sensorDatas.get(searchResultEntry.getId()));
        }
        return sortedRecords;
    }

    public static void updateOperation(String deviceId, Operation operation)
            throws OperationManagementException, PolicyComplianceException, ApplicationManagementException {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);

        if (!Operation.Status.ERROR.equals(operation.getStatus()) &&
            AndroidConstants.OperationCodes.MONITOR.equals(operation.getCode())) {
            if (log.isDebugEnabled()) {
                log.info("Received compliance status from MONITOR operation ID: " + operation.getId());
            }
            getPolicyManagerService().checkPolicyCompliance(deviceIdentifier, operation.getPayLoad());
        } else if (!Operation.Status.ERROR.equals(operation.getStatus()) && AndroidConstants.
                OperationCodes.APPLICATION_LIST.equals(operation.getCode())) {
            if (log.isDebugEnabled()) {
                log.info("Received applications list from device '" + deviceId + "'");
            }
            updateApplicationList(operation, deviceIdentifier);

        } else if (!Operation.Status.ERROR.equals(operation.getStatus()) && AndroidConstants.
                OperationCodes.DEVICE_INFO.equals(operation.getCode())) {

            try {
                if (log.isDebugEnabled()){
                    log.debug("Operation response: " + operation.getOperationResponse());
                }
                Device device = new Gson().fromJson(operation.getOperationResponse(), Device.class);
                org.wso2.carbon.device.mgt.common.device.details.DeviceInfo deviceInfo = convertDeviceToInfo(device);
                updateDeviceInfo(deviceIdentifier, deviceInfo);
            } catch (DeviceDetailsMgtException e) {
                throw new OperationManagementException("Error occurred while updating the device information.", e);
            }


        } else if (!Operation.Status.ERROR.equals(operation.getStatus()) &&
                   AndroidConstants.OperationCodes.DEVICE_LOCATION.equals(operation.getCode())) {
            try {
                DeviceLocation location = new Gson().fromJson(operation.getOperationResponse(), DeviceLocation.class);
                // reason for checking "location.getLatitude() != null" because when device fails to provide
                // device location and send status instead, above Gson converter create new location object
                // with null attributes
                if (location != null && location.getLatitude() != null) {
                    location.setDeviceIdentifier(deviceIdentifier);
                    updateDeviceLocation(location);
                }
            } catch (DeviceDetailsMgtException e) {
                throw new OperationManagementException("Error occurred while updating the device location.", e);
            }
        }
        getDeviceManagementService().updateOperation(deviceIdentifier, operation);
    }

    public static List<? extends Operation> getPendingOperations
            (DeviceIdentifier deviceIdentifier) throws OperationManagementException {

        List<? extends Operation> operations;
        operations = getDeviceManagementService().getPendingOperations(deviceIdentifier);
        return operations;
    }

    private static void updateApplicationList(Operation operation, DeviceIdentifier deviceIdentifier)
            throws ApplicationManagementException {
        // Parsing json string to get applications list.
        if (operation.getOperationResponse() != null) {
            JsonElement jsonElement = new JsonParser().parse(operation.getOperationResponse());
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            Application app;
            List<Application> applications = new ArrayList<Application>(jsonArray.size());
            for (JsonElement element : jsonArray) {
                app = new Application();
                app.setName(element.getAsJsonObject().
                        get(AndroidConstants.ApplicationProperties.NAME).getAsString());
                app.setApplicationIdentifier(element.getAsJsonObject().
                        get(AndroidConstants.ApplicationProperties.IDENTIFIER).getAsString());
                app.setPlatform(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
                if (element.getAsJsonObject().get(AndroidConstants.ApplicationProperties.USS) != null) {
                    app.setMemoryUsage(element.getAsJsonObject().get(AndroidConstants.ApplicationProperties.USS).getAsInt());
                }
                if (element.getAsJsonObject().get(AndroidConstants.ApplicationProperties.VERSION) != null) {
                    app.setVersion(element.getAsJsonObject().get(AndroidConstants.ApplicationProperties.VERSION).getAsString());
                }
                if (element.getAsJsonObject().get(AndroidConstants.ApplicationProperties.IS_ACTIVE) != null) {
                    app.setActive(element.getAsJsonObject().get(AndroidConstants.ApplicationProperties.IS_ACTIVE).getAsBoolean());
                }
                applications.add(app);
            }
            getApplicationManagerService().updateApplicationListInstalledInDevice(deviceIdentifier, applications);
        } else {
            log.error("Operation Response is null.");
        }

    }


    private static void updateDeviceLocation(DeviceLocation deviceLocation) throws DeviceDetailsMgtException {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceInformationManager informationManager =
                (DeviceInformationManager) ctx.getOSGiService(DeviceInformationManager.class, null);

        informationManager.addDeviceLocation(deviceLocation);
    }


    private static void updateDeviceInfo(DeviceIdentifier deviceId, DeviceInfo deviceInfo)
            throws DeviceDetailsMgtException {

        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceInformationManager informationManager =
                (DeviceInformationManager) ctx.getOSGiService(DeviceInformationManager.class, null);

        informationManager.addDeviceInfo(deviceId, deviceInfo);
    }


    private static org.wso2.carbon.device.mgt.common.device.details.DeviceInfo convertDeviceToInfo(Device device) {

        org.wso2.carbon.device.mgt.common.device.details.DeviceInfo deviceInfo =
                new org.wso2.carbon.device.mgt.common.device.details.DeviceInfo();
        if (deviceInfo.getDeviceDetailsMap() == null) {
            deviceInfo.setDeviceDetailsMap(new HashMap<String, String>());
        }
        List<Device.Property> props = device.getProperties();

        for (Device.Property prop : props) {
            if (Utils.getDeviceDetailsColumnNames().containsValue(prop.getName())) {
                if (prop.getName().equalsIgnoreCase("DEVICE_MODEL")) {
                    deviceInfo.setDeviceModel(prop.getValue());
                } else if (prop.getName().equalsIgnoreCase("VENDOR")) {
                    deviceInfo.setVendor(prop.getValue());
                } else if (prop.getName().equalsIgnoreCase("OS_VERSION")) {
                    deviceInfo.setOsVersion(prop.getValue());
                } else if (prop.getName().equalsIgnoreCase("IMEI")) {
                    deviceInfo.getDeviceDetailsMap().put("IMEI", prop.getValue());
                } else if (prop.getName().equalsIgnoreCase("IMSI")) {
                    deviceInfo.getDeviceDetailsMap().put("IMSI", prop.getValue());
                } else if (prop.getName().equalsIgnoreCase("MAC")) {
                    deviceInfo.getDeviceDetailsMap().put("mac", prop.getValue());
                } else if (prop.getName().equalsIgnoreCase("SERIAL")) {
                    deviceInfo.getDeviceDetailsMap().put("serial", prop.getValue());
                } else if (prop.getName().equalsIgnoreCase("OS_BUILD_DATE")) {
                    deviceInfo.setOsBuildDate(prop.getValue());
                }
            } else {
                if (prop.getName().equalsIgnoreCase("CPU_INFO")) {
                    deviceInfo.getDeviceDetailsMap().put("cpuUser",
                            getProperty(prop.getValue(), "User"));
                    deviceInfo.getDeviceDetailsMap().put("cpuSystem",
                            getProperty(prop.getValue(), "System"));
                    deviceInfo.getDeviceDetailsMap().put("IOW",
                            getProperty(prop.getValue(), "IOW"));
                    deviceInfo.getDeviceDetailsMap().put("IRQ",
                            getProperty(prop.getValue(), "IRQ"));
                } else if (prop.getName().equalsIgnoreCase("RAM_INFO")) {
                    deviceInfo.setTotalRAMMemory(Double.parseDouble(getProperty(prop.getValue(), "TOTAL_MEMORY")));
                    deviceInfo.setAvailableRAMMemory(Double.parseDouble(getProperty(prop.getValue(), "AVAILABLE_MEMORY")));

                    deviceInfo.getDeviceDetailsMap().put("ramThreshold",
                            getProperty(prop.getValue(), "THRESHOLD"));
                    deviceInfo.getDeviceDetailsMap().put("ramLowMemory",
                            getProperty(prop.getValue(), "LOW_MEMORY"));
                } else if (prop.getName().equalsIgnoreCase("BATTERY_INFO")) {
                    deviceInfo.setPluggedIn(Boolean.parseBoolean(getProperty(prop.getValue(), "PLUGGED")));

                    deviceInfo.getDeviceDetailsMap().put("batteryLevel",
                            getProperty(prop.getValue(), "BATTERY_LEVEL"));
                    deviceInfo.getDeviceDetailsMap().put("batteryScale",
                            getProperty(prop.getValue(), "SCALE"));
                    deviceInfo.getDeviceDetailsMap().put("batteryVoltage",
                            getProperty(prop.getValue(), "BATTERY_VOLTAGE"));
                    deviceInfo.getDeviceDetailsMap().put("batteryTemperature",
                            getProperty(prop.getValue(), "TEMPERATURE"));
                    deviceInfo.getDeviceDetailsMap().put("batteryCurrentTemperature",
                            getProperty(prop.getValue(), "CURRENT_AVERAGE"));
                    deviceInfo.getDeviceDetailsMap().put("batteryTechnology",
                            getProperty(prop.getValue(), "TECHNOLOGY"));
                    deviceInfo.getDeviceDetailsMap().put("batteryHealth",
                            getProperty(prop.getValue(), "HEALTH"));
                    deviceInfo.getDeviceDetailsMap().put("batteryStatus",
                            getProperty(prop.getValue(), "STATUS"));
                } else if (prop.getName().equalsIgnoreCase("NETWORK_INFO")) {
                    deviceInfo.setSsid(getProperty(prop.getValue(), "WIFI_SSID"));
                    deviceInfo.setConnectionType(getProperty(prop.getValue(), "CONNECTION_TYPE"));

                    deviceInfo.getDeviceDetailsMap().put("mobileSignalStrength",
                            getProperty(prop.getValue(), "MOBILE_SIGNAL_STRENGTH"));
                    deviceInfo.getDeviceDetailsMap().put("wifiSignalStrength",
                            getProperty(prop.getValue(), "WIFI_SIGNAL_STRENGTH"));
                } else if (prop.getName().equalsIgnoreCase("DEVICE_INFO")) {
                    deviceInfo.setBatteryLevel(Double.parseDouble(
                            getProperty(prop.getValue(), "BATTERY_LEVEL")));
                    deviceInfo.setInternalTotalMemory(Double.parseDouble(
                            getProperty(prop.getValue(), "INTERNAL_TOTAL_MEMORY")));
                    deviceInfo.setInternalAvailableMemory(Double.parseDouble(
                            getProperty(prop.getValue(), "INTERNAL_AVAILABLE_MEMORY")));
                    deviceInfo.setExternalTotalMemory(Double.parseDouble(
                            getProperty(prop.getValue(), "EXTERNAL_TOTAL_MEMORY")));
                    deviceInfo.setExternalAvailableMemory(Double.parseDouble(
                            getProperty(prop.getValue(), "EXTERNAL_AVAILABLE_MEMORY")));

                    deviceInfo.getDeviceDetailsMap().put("encryptionEnabled",
                            getProperty(prop.getValue(), "ENCRYPTION_ENABLED"));
                    deviceInfo.getDeviceDetailsMap().put("passcodeEnabled",
                            getProperty(prop.getValue(), "PASSCODE_ENABLED"));
                    deviceInfo.getDeviceDetailsMap().put("operator",
                            getProperty(prop.getValue(), "OPERATOR"));
                }
            }
        }
        return deviceInfo;
    }

    private static String getProperty(String a, String needed) {

        JsonElement jsonElement = new JsonParser().parse(a);
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        boolean exist = false;
        for (JsonElement element : jsonArray) {
            //  if (((JsonObject) element).entrySet().iterator().next().getValue().getAsString().equalsIgnoreCase(needed));
            for (Map.Entry<String, JsonElement> ob : ((JsonObject) element).entrySet()) {
                if (exist) {
                    return ob.getValue().getAsString().replace("%", "");
                }
                if (ob.getValue().getAsString().equalsIgnoreCase(needed)) {
                    exist = true;
                }
            }
        }
        return "";
    }
}
