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
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.mdm.services.android.bean.wrapper.AndroidDevice;
import org.wso2.carbon.mdm.services.android.mocks.ApplicationManagementProviderServiceMock;
import org.wso2.carbon.mdm.services.android.mocks.DeviceInformationManagerServiceMock;
import org.wso2.carbon.mdm.services.android.mocks.DeviceManagementProviderServiceMock;
import org.wso2.carbon.mdm.services.android.mocks.NotificationManagementServiceMock;
import org.wso2.carbon.mdm.services.android.mocks.PolicyManagerServiceMock;
import org.wso2.carbon.mdm.services.android.services.impl.DeviceManagementServiceImpl;
import org.wso2.carbon.mdm.services.android.util.AndroidAPIUtils;
import org.wso2.carbon.mdm.services.android.utils.TestUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@PowerMockIgnore({"javax.ws.rs.*", "org.apache.log4j.*"})
@PrepareForTest(AndroidAPIUtils.class)
public class DeviceManagementServiceTests {

    private DeviceManagementServiceImpl deviceManagementService;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new PowerMockObjectFactory();
    }

    @BeforeClass
    public void init() throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        MockitoAnnotations.initMocks(this);
        deviceManagementService = new DeviceManagementServiceImpl();
    }

    private void mockDeviceManagementService()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        PowerMockito.stub(PowerMockito.method(AndroidAPIUtils.class, "getDeviceManagementService"))
                .toReturn(new DeviceManagementProviderServiceMock());
    }

    private void mockApplicationManagerService()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        PowerMockito.stub(PowerMockito.method(AndroidAPIUtils.class, "getApplicationManagerService"))
                .toReturn(new ApplicationManagementProviderServiceMock());
    }

    private void mockPolicyManagerService()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        PowerMockito.stub(PowerMockito.method(AndroidAPIUtils.class, "getPolicyManagerService"))
                .toReturn(new PolicyManagerServiceMock());
    }

    private void mockDeviceInformationManagerService()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        PowerMockito.stub(PowerMockito.method(AndroidAPIUtils.class, "getDeviceInformationManagerService"))
                .toReturn(new DeviceInformationManagerServiceMock());
    }

    private void mockNotificationManagementService()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        PowerMockito.stub(PowerMockito.method(AndroidAPIUtils.class, "getNotificationManagementService"))
                .toReturn(new NotificationManagementServiceMock());
    }

    private void mockUser()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        PowerMockito.stub(PowerMockito.method(AndroidAPIUtils.class, "getAuthenticatedUser"))
                .toReturn("admin");
    }

    @Test
    public void testUpdateApplicationList()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockApplicationManagerService();
        Response response = deviceManagementService
                .updateApplicationList(TestUtils.getDeviceId(), TestUtils.getAndroidApplications());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    public void testGetPendingOperationsForNullDevice()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        Response response = deviceManagementService
                .getPendingOperations(null, null, null);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetPendingOperationsInvalidDevice()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementService
                .getPendingOperations("1234", null, null);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetPendingOperationsNullResponse()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementService
                .getPendingOperations(TestUtils.getDeviceId(), null, null);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testGetPendingOperationsWithMonitorResponse()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        mockPolicyManagerService();
        Response response = deviceManagementService
                .getPendingOperations(TestUtils.getDeviceId(), null,
                                      TestUtils.getSuccessMonitorOperationResponse());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testGetPendingOperationsWithApplicationResponse()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        mockApplicationManagerService();
        Response response = deviceManagementService
                .getPendingOperations(TestUtils.getDeviceId(), null,
                                      TestUtils.getSuccessApplicationOperationResponse());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testGetPendingOperationsWithDeviceInfoResponse()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        mockDeviceInformationManagerService();
        Response response = deviceManagementService
                .getPendingOperations(TestUtils.getDeviceId(), null,
                                      TestUtils.getSuccessInfoOperationResponse());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testGetPendingOperationsWithInProgressResponse()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementService
                .getPendingOperations(TestUtils.getDeviceId(), null,
                                      TestUtils.getInProgressOperationResponse());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testGetPendingOperationsWithErrorResponse()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        mockNotificationManagementService();
        Response response = deviceManagementService
                .getPendingOperations(TestUtils.getDeviceId(), null,
                                      TestUtils.getErrorOperationResponse());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testEnrollDeviceWithoutLocationSuccess()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        mockPolicyManagerService();
        mockUser();
        Response response = deviceManagementService.enrollDevice(TestUtils.getBasicAndroidDevice());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testEnrollDeviceWithLocationSuccess()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        mockDeviceInformationManagerService();
        mockPolicyManagerService();
        mockUser();
        AndroidDevice androidDevice = TestUtils.getBasicAndroidDevice();

        List<Device.Property> properties = new ArrayList<>();
        Device.Property property = new Device.Property();
        property.setName("LATITUDE");
        property.setValue("79.5");
        properties.add(property);
        property = new Device.Property();
        property.setName("LONGITUDE");
        property.setValue("6.9");
        properties.add(property);
        androidDevice.setProperties(properties);

        Response response = deviceManagementService.enrollDevice(androidDevice);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testEnrollDeviceUnSuccess()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        mockUser();
        AndroidDevice androidDevice = TestUtils.getBasicAndroidDevice();
        androidDevice.setDeviceIdentifier("1234");
        Response response = deviceManagementService.enrollDevice(androidDevice);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void testIsEnrolledExists()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementService.isEnrolled(TestUtils.getDeviceId(), null);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testIsEnrolledNonExist()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementService.isEnrolled("1234", null);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testIsEnrolledNull()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementService.isEnrolled(null, null);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testModifyEnrollmentSuccess()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        mockUser();
        Response response = deviceManagementService
                .modifyEnrollment(TestUtils.getDeviceId(), TestUtils.getBasicAndroidDevice());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    public void testModifyEnrollmentUnSuccess()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        mockUser();
        AndroidDevice androidDevice = TestUtils.getBasicAndroidDevice();
        androidDevice.setDeviceIdentifier("1234");
        Response response = deviceManagementService
                .modifyEnrollment(TestUtils.getDeviceId(), androidDevice);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_MODIFIED.getStatusCode());
    }

    @Test
    public void testDisEnrollDeviceSuccess()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementService.disEnrollDevice(TestUtils.getDeviceId());
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void testDisenrollUnSuccess()
            throws DeviceManagementException, OperationManagementException, InvalidDeviceException {
        mockDeviceManagementService();
        Response response = deviceManagementService.disEnrollDevice("1234");
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

}

