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

package org.wso2.carbon.mdm.services.android;

import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockObjectFactory;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.mdm.services.android.mocks.DeviceManagementProviderServiceMock;
import org.wso2.carbon.mdm.services.android.services.impl.DeviceManagementAdminServiceImpl;
import org.wso2.carbon.mdm.services.android.util.AndroidAPIUtils;
import org.wso2.carbon.mdm.services.android.utils.TestUtils;

import javax.ws.rs.core.Response;

@PowerMockIgnore({"javax.ws.rs.*", "org.apache.log4j.*"})
@PrepareForTest(AndroidAPIUtils.class)
public class DeviceManagementAdminServiceTests {

    private DeviceManagementAdminServiceImpl deviceManagementAdminService;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new PowerMockObjectFactory();
    }

    @BeforeClass
    public void init() throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        MockitoAnnotations.initMocks(this);
        deviceManagementAdminService = new DeviceManagementAdminServiceImpl();
    }

    private void mockDeviceManagementService()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        PowerMockito.stub(PowerMockito.method(AndroidAPIUtils.class, "getDeviceManagementService"))
                .toReturn(new DeviceManagementProviderServiceMock());
    }

    @Test
    public void testConfigureDeviceLock()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.configureDeviceLock(TestUtils.getDeviceLockBeanWrapper());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testConfigureDeviceUnlock()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.configureDeviceUnlock(TestUtils.getDeviceIds());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testGetDeviceLocation()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.getDeviceLocation(TestUtils.getDeviceIds());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testRemovePassword()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.removePassword(TestUtils.getDeviceIds());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testConfigureCamera()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.configureCamera(TestUtils.getCamerabeanWrapper());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testGetDeviceInformation()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.getDeviceInformation(TestUtils.getDeviceIds());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testGetDeviceLogcat()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.getDeviceLogcat(TestUtils.getDeviceIds());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testWipeDevice()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.wipeDevice(TestUtils.getDeviceIds());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testWipeData()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.wipeData(TestUtils.getWipeDataBeanWrapper());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testGetApplications()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.getApplications(TestUtils.getDeviceIds());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testRingDevice()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.ringDevice(TestUtils.getDeviceIds());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testRebootDevice()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.rebootDevice(TestUtils.getDeviceIds());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testMuteDevice()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.muteDevice(TestUtils.getDeviceIds());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testInstallApplication()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService
                .installApplication(TestUtils.getApplicationInstallationBeanWrapper());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testUpdateApplication()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.updateApplication(TestUtils.getApplicationUpdateBeanWrapper());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testUninstallApplicationPublic()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService
                .uninstallApplication(TestUtils.getApplicationUninstallationBeanWrapperPublic());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testUninstallApplicationWebApp()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService
                .uninstallApplication(TestUtils.getApplicationUninstallationBeanWrapperWebApp());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testBlacklistApplications()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService
                .blacklistApplications(TestUtils.getBlacklistApplicationsBeanWrapper());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testUpgradeFirmware()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.upgradeFirmware(TestUtils.getUpgradeFirmwareBeanWrapper());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testConfigureVPN()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.configureVPN(TestUtils.getVpnBeanWrapper());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testSendNotification()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.sendNotification(TestUtils.getNotificationBeanWrapper());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testConfigureWifi()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.configureWifi(TestUtils.getWifiBeanWrapper());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testEncryptStorage()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.encryptStorage(TestUtils.getEncryptionBeanWrapper());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testChangeLockCode()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.changeLockCode(TestUtils.getLockCodeBeanWrapper());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testSetPasswordPolicy()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.setPasswordPolicy(TestUtils.getPasswordPolicyBeanWrapper());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testSetWebClip()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementAdminService.setWebClip(TestUtils.getWebClipBeanWrapper());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

}

