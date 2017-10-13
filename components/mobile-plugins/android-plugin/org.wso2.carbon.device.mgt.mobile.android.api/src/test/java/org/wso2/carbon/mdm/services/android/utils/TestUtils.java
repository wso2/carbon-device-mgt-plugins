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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mdm.services.android.utils;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.mdm.services.android.bean.ApplicationInstallation;
import org.wso2.carbon.mdm.services.android.bean.ApplicationUninstallation;
import org.wso2.carbon.mdm.services.android.bean.ApplicationUpdate;
import org.wso2.carbon.mdm.services.android.bean.BlacklistApplications;
import org.wso2.carbon.mdm.services.android.bean.Camera;
import org.wso2.carbon.mdm.services.android.bean.DeviceEncryption;
import org.wso2.carbon.mdm.services.android.bean.DeviceLock;
import org.wso2.carbon.mdm.services.android.bean.LockCode;
import org.wso2.carbon.mdm.services.android.bean.Notification;
import org.wso2.carbon.mdm.services.android.bean.PasscodePolicy;
import org.wso2.carbon.mdm.services.android.bean.UpgradeFirmware;
import org.wso2.carbon.mdm.services.android.bean.Vpn;
import org.wso2.carbon.mdm.services.android.bean.WebClip;
import org.wso2.carbon.mdm.services.android.bean.Wifi;
import org.wso2.carbon.mdm.services.android.bean.WipeData;
import org.wso2.carbon.mdm.services.android.bean.wrapper.AndroidApplication;
import org.wso2.carbon.mdm.services.android.bean.wrapper.AndroidDevice;
import org.wso2.carbon.mdm.services.android.bean.wrapper.ApplicationInstallationBeanWrapper;
import org.wso2.carbon.mdm.services.android.bean.wrapper.ApplicationUninstallationBeanWrapper;
import org.wso2.carbon.mdm.services.android.bean.wrapper.ApplicationUpdateBeanWrapper;
import org.wso2.carbon.mdm.services.android.bean.wrapper.BlacklistApplicationsBeanWrapper;
import org.wso2.carbon.mdm.services.android.bean.wrapper.CameraBeanWrapper;
import org.wso2.carbon.mdm.services.android.bean.wrapper.DeviceLockBeanWrapper;
import org.wso2.carbon.mdm.services.android.bean.wrapper.EncryptionBeanWrapper;
import org.wso2.carbon.mdm.services.android.bean.wrapper.LockCodeBeanWrapper;
import org.wso2.carbon.mdm.services.android.bean.wrapper.NotificationBeanWrapper;
import org.wso2.carbon.mdm.services.android.bean.wrapper.PasswordPolicyBeanWrapper;
import org.wso2.carbon.mdm.services.android.bean.wrapper.UpgradeFirmwareBeanWrapper;
import org.wso2.carbon.mdm.services.android.bean.wrapper.VpnBeanWrapper;
import org.wso2.carbon.mdm.services.android.bean.wrapper.WebClipBeanWrapper;
import org.wso2.carbon.mdm.services.android.bean.wrapper.WifiBeanWrapper;
import org.wso2.carbon.mdm.services.android.bean.wrapper.WipeDataBeanWrapper;
import org.wso2.carbon.mdm.services.android.util.AndroidConstants;
import org.wso2.carbon.mdm.services.android.util.AndroidDeviceUtils;

import java.util.ArrayList;
import java.util.List;

public class TestUtils {

    public static Activity getActivity() {
        Activity activity = new Activity();
        activity.setActivityId("ACTIVITY_1");
        activity.setCode("CODE");
        return activity;
    }

    public static String getDeviceId() {
        return "1a2b3c4d5e";
    }

    public static List<String> getDeviceIds() {
        List<String> deviceIds = new ArrayList<>();
        deviceIds.add(getDeviceId());
        return deviceIds;
    }

    public static Device getDevice() {
        Device device = new Device();
        device.setId(1);
        device.setName("Test");
        device.setDeviceIdentifier(getDeviceId());
        device.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setId(1);
        enrolmentInfo.setOwner("admin");
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
        device.setEnrolmentInfo(enrolmentInfo);
        return device;
    }

    public static DeviceLockBeanWrapper getDeviceLockBeanWrapper() {
        DeviceLockBeanWrapper deviceLockBeanWrapper = new DeviceLockBeanWrapper();
        deviceLockBeanWrapper.setDeviceIDs(getDeviceIds());
        DeviceLock deviceLockOperation = new DeviceLock();
        deviceLockOperation.setHardLockEnabled(true);
        deviceLockOperation.setMessage("Test Operation");
        deviceLockBeanWrapper.setOperation(deviceLockOperation);
        return deviceLockBeanWrapper;
    }

    public static CameraBeanWrapper getCamerabeanWrapper() {
        CameraBeanWrapper cameraBeanWrapper = new CameraBeanWrapper();
        cameraBeanWrapper.setDeviceIDs(getDeviceIds());
        Camera camera = new Camera();
        camera.setEnabled(false);
        cameraBeanWrapper.setOperation(camera);
        return cameraBeanWrapper;
    }

    public static WipeDataBeanWrapper getWipeDataBeanWrapper() {
        WipeDataBeanWrapper wipeDataBeanWrapper = new WipeDataBeanWrapper();
        wipeDataBeanWrapper.setDeviceIDs(getDeviceIds());
        WipeData wipeData = new WipeData();
        wipeData.setPin("1234");
        wipeDataBeanWrapper.setOperation(wipeData);
        return wipeDataBeanWrapper;
    }

    public static ApplicationInstallationBeanWrapper getApplicationInstallationBeanWrapper() {
        ApplicationInstallationBeanWrapper applicationInstallationBeanWrapper = new ApplicationInstallationBeanWrapper();
        applicationInstallationBeanWrapper.setDeviceIDs(getDeviceIds());
        ApplicationInstallation applicationInstallation = new ApplicationInstallation();
        applicationInstallation.setAppIdentifier("org.wso2.iot.agent");
        applicationInstallation.setUrl("https://github.com/wso2/cdmf-agent-android/releases/download/v3.1.21/client-standalone.apk");
        applicationInstallation.setType("enterprise");
        applicationInstallation.setSchedule("2017-10-11T18:46:19-0530");
        applicationInstallationBeanWrapper.setOperation(applicationInstallation);
        return applicationInstallationBeanWrapper;
    }

    public static ApplicationUpdateBeanWrapper getApplicationUpdateBeanWrapper() {
        ApplicationUpdateBeanWrapper applicationUpdateBeanWrapper = new ApplicationUpdateBeanWrapper();
        applicationUpdateBeanWrapper.setDeviceIDs(getDeviceIds());
        ApplicationUpdate applicationUpdate = new ApplicationUpdate();
        applicationUpdate.setAppIdentifier("org.wso2.iot.agent");
        applicationUpdate.setUrl("https://github.com/wso2/cdmf-agent-android/releases/download/v3.1.21/client-standalone.apk");
        applicationUpdate.setType("enterprise");
        applicationUpdate.setSchedule("2017-10-11T18:46:19-0530");
        applicationUpdateBeanWrapper.setOperation(applicationUpdate);
        return applicationUpdateBeanWrapper;
    }

    public static ApplicationUninstallationBeanWrapper getApplicationUninstallationBeanWrapperPublic() {
        ApplicationUninstallationBeanWrapper applicationUninstallationBeanWrapper = new ApplicationUninstallationBeanWrapper();
        applicationUninstallationBeanWrapper.setDeviceIDs(getDeviceIds());
        ApplicationUninstallation applicationUninstallation = new ApplicationUninstallation();
        applicationUninstallation.setAppIdentifier("org.wso2.iot.agent");
        applicationUninstallation.setType("public");
        applicationUninstallationBeanWrapper.setOperation(applicationUninstallation);
        return applicationUninstallationBeanWrapper;
    }

    public static ApplicationUninstallationBeanWrapper getApplicationUninstallationBeanWrapperWebApp() {
        ApplicationUninstallationBeanWrapper applicationUninstallationBeanWrapper = new ApplicationUninstallationBeanWrapper();
        applicationUninstallationBeanWrapper.setDeviceIDs(getDeviceIds());
        ApplicationUninstallation applicationUninstallation = new ApplicationUninstallation();
        applicationUninstallation.setAppIdentifier("org.wso2.iot.agent");
        applicationUninstallation.setType("webapp");
        applicationUninstallationBeanWrapper.setOperation(applicationUninstallation);
        return applicationUninstallationBeanWrapper;
    }

    public static BlacklistApplicationsBeanWrapper getBlacklistApplicationsBeanWrapper() {
        BlacklistApplicationsBeanWrapper blacklistApplicationsBeanWrapper = new BlacklistApplicationsBeanWrapper();
        blacklistApplicationsBeanWrapper.setDeviceIDs(getDeviceIds());
        BlacklistApplications blacklistApplications = new BlacklistApplications();
        List<String> appIds = new ArrayList<>();
        appIds.add("org.wso2.iot.agent");
        blacklistApplications.setAppIdentifier(appIds);
        blacklistApplicationsBeanWrapper.setOperation(blacklistApplications);
        return blacklistApplicationsBeanWrapper;
    }

    public static UpgradeFirmwareBeanWrapper getUpgradeFirmwareBeanWrapper() {
        UpgradeFirmwareBeanWrapper upgradeFirmwareBeanWrapper = new UpgradeFirmwareBeanWrapper();
        upgradeFirmwareBeanWrapper.setDeviceIDs(getDeviceIds());
        UpgradeFirmware upgradeFirmware = new UpgradeFirmware();
        upgradeFirmware.setServer("https://github.com/wso2/cdmf-agent-android/releases/download/");
        upgradeFirmware.setSchedule("2017-10-11T18:46:19-0530");
        upgradeFirmwareBeanWrapper.setOperation(upgradeFirmware);
        return upgradeFirmwareBeanWrapper;
    }

    public static VpnBeanWrapper getVpnBeanWrapper() {
        VpnBeanWrapper vpnBeanWrapper = new VpnBeanWrapper();
        vpnBeanWrapper.setDeviceIDs(getDeviceIds());
        Vpn vpn = new Vpn();
        vpnBeanWrapper.setOperation(vpn);
        return vpnBeanWrapper;
    }

    public static NotificationBeanWrapper getNotificationBeanWrapper() {
        NotificationBeanWrapper notificationBeanWrapper = new NotificationBeanWrapper();
        notificationBeanWrapper.setDeviceIDs(getDeviceIds());
        Notification notification = new Notification();
        notification.setMessageText("Message");
        notification.setMessageTitle("Title");
        notificationBeanWrapper.setOperation(notification);
        return notificationBeanWrapper;
    }

    public static WifiBeanWrapper getWifiBeanWrapper() {
        WifiBeanWrapper wifiBeanWrapper = new WifiBeanWrapper();
        wifiBeanWrapper.setDeviceIDs(getDeviceIds());
        Wifi wifi = new Wifi();
        wifiBeanWrapper.setOperation(wifi);
        return wifiBeanWrapper;
    }

    public static EncryptionBeanWrapper getEncryptionBeanWrapper() {
        EncryptionBeanWrapper encryptionBeanWrapper = new EncryptionBeanWrapper();
        encryptionBeanWrapper.setDeviceIDs(getDeviceIds());
        DeviceEncryption deviceEncryption = new DeviceEncryption();
        deviceEncryption.setEncrypted(true);
        encryptionBeanWrapper.setOperation(deviceEncryption);
        return encryptionBeanWrapper;
    }

    public static LockCodeBeanWrapper getLockCodeBeanWrapper() {
        LockCodeBeanWrapper lockCodeBeanWrapper = new LockCodeBeanWrapper();
        lockCodeBeanWrapper.setDeviceIDs(getDeviceIds());
        LockCode lockCode = new LockCode();
        lockCode.setLockCode("1234");
        lockCodeBeanWrapper.setOperation(lockCode);
        return lockCodeBeanWrapper;
    }

    public static PasswordPolicyBeanWrapper getPasswordPolicyBeanWrapper() {
        PasswordPolicyBeanWrapper passwordPolicyBeanWrapper = new PasswordPolicyBeanWrapper();
        passwordPolicyBeanWrapper.setDeviceIDs(getDeviceIds());
        PasscodePolicy passcodePolicy = new PasscodePolicy();
        passwordPolicyBeanWrapper.setOperation(passcodePolicy);
        return passwordPolicyBeanWrapper;
    }

    public static WebClipBeanWrapper getWebClipBeanWrapper() {
        WebClipBeanWrapper webClipBeanWrapper = new WebClipBeanWrapper();
        webClipBeanWrapper.setDeviceIDs(getDeviceIds());
        WebClip webClip = new WebClip();
        webClipBeanWrapper.setOperation(webClip);
        return webClipBeanWrapper;
    }

    public static List<AndroidApplication> getAndroidApplications() {
        List<AndroidApplication> androidApplications = new ArrayList<>();
        AndroidApplication androidApplication = new AndroidApplication();
        androidApplications.add(androidApplication);
        return androidApplications;
    }

    public static List<Operation> getSuccessMonitorOperationResponse() {
        List<Operation> operations = new ArrayList<>();
        Operation operation = new Operation();
        operation.setActivityId(getActivity().getActivityId());
        operation.setCode(AndroidConstants.OperationCodes.MONITOR);
        operation.setId(1);
        operation.setOperationResponse("Operation success.");
        operation.setStatus(Operation.Status.COMPLETED);
        operations.add(operation);
        return operations;
    }

    public static List<Operation> getSuccessApplicationOperationResponse() {
        List<Operation> operations = new ArrayList<>();
        Operation operation = new Operation();
        operation.setActivityId(getActivity().getActivityId());
        operation.setCode(AndroidConstants.OperationCodes.APPLICATION_LIST);
        operation.setId(1);
        operation.setOperationResponse("[{\"name\":\"Widget%20Preview\",\"package\":\"com.android.widgetpreview\"," +
                                       "\"version\":\"7.1.1\",\"isSystemApp\":false,\"isActive\":false}," +
                                       "{\"name\":\"com.android.gesture.builder\"," +
                                       "\"package\":\"com.android.gesture.builder\",\"version\":\"7.1.1\"," +
                                       "\"isSystemApp\":false,\"isActive\":false},{\"name\":\"API%20Demos\"," +
                                       "\"package\":\"com.example.android.apis\",\"version\":\"7.1.1\"," +
                                       "\"isSystemApp\":false,\"isActive\":false}," +
                                       "{\"name\":\"WSO2%20Device%20Management%20Agent\"," +
                                       "\"package\":\"org.wso2.iot.agent\",\"version\":\"3.1.21\"," +
                                       "\"isSystemApp\":false,\"isActive\":true}," +
                                       "{\"name\":\"com.android.smoketest.tests\"," +
                                       "\"package\":\"com.android.smoketest.tests\",\"version\":\"7.1.1\"," +
                                       "\"isSystemApp\":false,\"isActive\":false}," +
                                       "{\"name\":\"Sample%20Soft%20Keyboard\"," +
                                       "\"package\":\"com.example.android.softkeyboard\",\"version\":\"7.1.1\"," +
                                       "\"isSystemApp\":false,\"isActive\":false},{\"name\":\"Example%20Wallpapers\"," +
                                       "\"package\":\"com.example.android.livecubes\",\"version\":\"7.1.1\"," +
                                       "\"isSystemApp\":false,\"isActive\":false},{\"name\":\"com.android.smoketest\"," +
                                       "\"package\":\"com.android.smoketest\",\"version\":\"7.1.1\"," +
                                       "\"isSystemApp\":false,\"isActive\":false}]");
        operation.setStatus(Operation.Status.COMPLETED);
        operations.add(operation);
        return operations;
    }

    public static List<Operation> getSuccessInfoOperationResponse() {
        List<Operation> operations = new ArrayList<>();
        Operation operation = new Operation();
        operation.setActivityId(getActivity().getActivityId());
        operation.setCode(AndroidConstants.OperationCodes.DEVICE_INFO);
        operation.setId(1);
        operation.setOperationResponse("{\"description\":\"generic_x86\",\"deviceIdentifier\":\"1d9612def9d205f9\"," +
                                       "\"enrolmentInfo\":null,\"name\":\"generic_x86\",\"properties\":[" +
                                       "{\"name\":\"SERIAL\",\"value\":\"unknown\"}," +
                                       "{\"name\":\"IMEI\",\"value\":null}," +
                                       "{\"name\":\"IMSI\",\"value\":\"310260000000000\"}," +
                                       "{\"name\":\"MAC\",\"value\":\"02:00:00:00:00:00\"}," +
                                       "{\"name\":\"DEVICE_MODEL\",\"value\":\"Android SDK built for x86\"}," +
                                       "{\"name\":\"VENDOR\",\"value\":\"unknown\"}," +
                                       "{\"name\":\"OS_VERSION\",\"value\":\"7.1.1\"}," +
                                       "{\"name\":\"OS_BUILD_DATE\",\"value\":\"1487782847000\"}," +
                                       "{\"name\":\"DEVICE_NAME\",\"value\":\"generic_x86\"}," +
                                       "{\"name\":\"LATITUDE\",\"value\":\"6.90988\"}," +
                                       "{\"name\":\"LONGITUDE\",\"value\":\"79.85249999999999\"}," +
                                       "{\"name\":\"NETWORK_INFO\",\"value\":\"[" +
                                       "{\\\"name\\\":\\\"CONNECTION_TYPE\\\",\\\"value\\\":\\\"MOBILE\\\"}," +
                                       "{\\\"name\\\":\\\"MOBILE_CONNECTION_TYPE\\\",\\\"value\\\":\\\"LTE\\\"}," +
                                       "{\\\"name\\\":\\\"MOBILE_SIGNAL_STRENGTH\\\",\\\"value\\\":\\\"-89\\\"}]\"}," +
                                       "{\"name\":\"CPU_INFO\",\"value\":\"[]\"},{\"name\":\"RAM_INFO\",\"value\":\"[" +
                                       "{\\\"name\\\":\\\"TOTAL_MEMORY\\\",\\\"value\\\":\\\"1055113216\\\"}," +
                                       "{\\\"name\\\":\\\"AVAILABLE_MEMORY\\\",\\\"value\\\":\\\"708997120\\\"}," +
                                       "{\\\"name\\\":\\\"THRESHOLD\\\",\\\"value\\\":\\\"150994944\\\"}," +
                                       "{\\\"name\\\":\\\"LOW_MEMORY\\\",\\\"value\\\":\\\"false\\\"}]\"}," +
                                       "{\"name\":\"BATTERY_INFO\",\"value\":\"[" +
                                       "{\\\"name\\\":\\\"BATTERY_LEVEL\\\",\\\"value\\\":\\\"100\\\"}," +
                                       "{\\\"name\\\":\\\"SCALE\\\",\\\"value\\\":\\\"100\\\"}," +
                                       "{\\\"BATTERY_VOLTAGE\\\":\\\"0\\\"}," +
                                       "{\\\"name\\\":\\\"HEALTH\\\",\\\"value\\\":\\\"GOOD_CONDITION\\\"}," +
                                       "{\\\"name\\\":\\\"STATUS\\\"}," +
                                       "{\\\"name\\\":\\\"PLUGGED\\\",\\\"value\\\":\\\"AC\\\"}]\"}," +
                                       "{\"name\":\"DEVICE_INFO\",\"value\":\"[" +
                                       "{\\\"name\\\":\\\"ENCRYPTION_ENABLED\\\",\\\"value\\\":\\\"false\\\"}," +
                                       "{\\\"name\\\":\\\"PASSCODE_ENABLED\\\",\\\"value\\\":\\\"true\\\"}," +
                                       "{\\\"name\\\":\\\"BATTERY_LEVEL\\\",\\\"value\\\":\\\"100\\\"}," +
                                       "{\\\"name\\\":\\\"INTERNAL_TOTAL_MEMORY\\\",\\\"value\\\":\\\"0.76\\\"}," +
                                       "{\\\"name\\\":\\\"INTERNAL_AVAILABLE_MEMORY\\\",\\\"value\\\":\\\"0.67\\\"}," +
                                       "{\\\"name\\\":\\\"EXTERNAL_TOTAL_MEMORY\\\",\\\"value\\\":\\\"0.1\\\"}," +
                                       "{\\\"name\\\":\\\"EXTERNAL_AVAILABLE_MEMORY\\\",\\\"value\\\":\\\"0.1\\\"}," +
                                       "{\\\"name\\\":\\\"OPERATOR\\\",\\\"value\\\":\\\"Android\\\"}," +
                                       "{\\\"name\\\":\\\"PHONE_NUMBER\\\",\\\"value\\\":\\\"15555215554\\\"}]\"}]}");
        operation.setStatus(Operation.Status.COMPLETED);
        operations.add(operation);
        return operations;
    }

    public static List<Operation> getInProgressOperationResponse() {
        List<Operation> operations = new ArrayList<>();
        Operation operation = new Operation();
        operation.setActivityId(getActivity().getActivityId());
        operation.setCode(AndroidConstants.OperationCodes.NOTIFICATION);
        operation.setId(1);
        operation.setOperationResponse("Operation in progress.");
        operation.setStatus(Operation.Status.IN_PROGRESS);
        operations.add(operation);
        return operations;
    }

    public static List<Operation> getErrorOperationResponse() {
        List<Operation> operations = new ArrayList<>();
        Operation operation = new Operation();
        operation.setActivityId(getActivity().getActivityId());
        operation.setCode(AndroidConstants.OperationCodes.DEVICE_INFO);
        operation.setId(1);
        operation.setOperationResponse("Operation failure.");
        operation.setStatus(Operation.Status.ERROR);
        operations.add(operation);
        return operations;
    }

    public static DeviceLocation getDeviceLocation() {
        DeviceLocation location = new DeviceLocation();
        location.setCity("Colombo");
        location.setCountry("Sri Lanka");
        location.setLatitude(6.9);
        location.setLongitude(79.5);
        location.setDeviceIdentifier(AndroidDeviceUtils.convertToDeviceIdentifierObject(getDeviceId()));
        return location;
    }

    public static DeviceInfo getDeviceInfo() {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceModel("nexus");
        deviceInfo.setAvailableRAMMemory(2.0);
        deviceInfo.setBatteryLevel(100.0);
        deviceInfo.setConnectionType("4G");
        deviceInfo.setCpuUsage(1.0);
        deviceInfo.setExternalAvailableMemory(2.3);
        deviceInfo.setExternalTotalMemory(4.0);
        deviceInfo.setInternalAvailableMemory(1.0);
        deviceInfo.setInternalTotalMemory(4.0);
        deviceInfo.setLocation(getDeviceLocation());
        return deviceInfo;
    }

    public static List<Application> getApplications() {
        List<Application> applications = new ArrayList<>();
        Application app = new Application();
        app.setName("WSO2 IoT Agent");
        app.setApplicationIdentifier("org.wos2.iot.agent");
        app.setVersion("1.0.0");
        app.setPlatform("Android");
        applications.add(app);
        return applications;
    }

    public static AndroidDevice getBasicAndroidDevice() {
        AndroidDevice androidDevice = new AndroidDevice();
        androidDevice.setName(getDevice().getName());
        androidDevice.setDescription(getDevice().getDescription());
        androidDevice.setDeviceIdentifier(getDeviceId());
        androidDevice.setDeviceInfo(getDeviceInfo());
        androidDevice.setApplications(getApplications());
        androidDevice.setEnrolmentInfo(getDevice().getEnrolmentInfo());
        return androidDevice;
    }

}
