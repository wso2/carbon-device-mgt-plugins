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

import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
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

import java.util.ArrayList;
import java.util.List;

public class TestUtils {

    public static Activity getActivity() {
        Activity activity = new Activity();
        activity.setActivityId("ACTIVITY_1");
        activity.setCode("CODE");
        return activity;
    }

    public static List<String> getDeviceIds() {
        List<String> deviceIds = new ArrayList<>();
        deviceIds.add("1a2b3c4d5e");
        return deviceIds;
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

}
