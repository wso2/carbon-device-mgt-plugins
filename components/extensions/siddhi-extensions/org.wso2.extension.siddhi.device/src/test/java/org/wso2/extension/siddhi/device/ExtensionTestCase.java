/*
 * Copyright (c)  2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.device;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.cache.DeviceCacheConfiguration;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderServiceImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.extension.siddhi.device.test.util.SiddhiTestHelper;
import org.wso2.extension.siddhi.device.test.util.TestDataHolder;
import org.wso2.extension.siddhi.device.test.util.TestDeviceManagementService;
import org.wso2.extension.siddhi.device.utils.DeviceUtils;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants.Permissions.DEFAULT_ADMIN_PERMISSIONS;
import static org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants.Roles.DEFAULT_ADMIN_ROLE;

public class ExtensionTestCase extends BaseDeviceManagementTest {
    private static final Logger log = Logger.getLogger(ExtensionTestCase.class);

    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;
    private GroupManagementProviderService groupManagementProviderService;
    private DeviceManagementProviderService deviceManagementProviderService;
    private static String DEVICE_TYPE = "Test";

    private QueryCallback queryCallback = new QueryCallback() {
        @Override
        public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
            EventPrinter.print(timeStamp, inEvents, removeEvents);
            for (Event ignored : inEvents) {
                count.incrementAndGet();
                eventArrived = true;
            }
        }
    };

    @BeforeClass
    @Override
    public void init() throws Exception {
        log.info("Initializing");
        groupManagementProviderService = new GroupManagementProviderServiceImpl();
        deviceManagementProviderService = new DeviceManagementProviderServiceImpl();

        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceManagementProviderService);
        DeviceManagementDataHolder.getInstance().setRegistryService(getRegistryService());
        DeviceManagementDataHolder.getInstance().setDeviceAccessAuthorizationService(new DeviceAccessAuthorizationServiceImpl());
        DeviceManagementDataHolder.getInstance().setGroupManagementProviderService(groupManagementProviderService);
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(null);
        deviceManagementProviderService.registerDeviceType(
                new TestDeviceManagementService(DEVICE_TYPE, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));

        Field deviceManagementProviderServiceField = DeviceUtils.class.getDeclaredField("deviceManagementProviderService");
        deviceManagementProviderServiceField.setAccessible(true);
        deviceManagementProviderServiceField.set(null, deviceManagementProviderService);

        Field groupManagementProviderServiceField = DeviceUtils.class.getDeclaredField("groupManagementProviderService");
        groupManagementProviderServiceField.setAccessible(true);
        groupManagementProviderServiceField.set(null, groupManagementProviderService);
    }

    private RegistryService getRegistryService() throws RegistryException, UserStoreException,
                                                        DeviceManagementException {
        RealmService realmService = new InMemoryRealmService();
        RegistryDataHolder.getInstance().setRealmService(realmService);
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        realmService.getTenantManager().getSuperTenantDomain();
        DeviceConfigurationManager.getInstance().initConfig();

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }

    @Test
    public void createGroup() throws GroupManagementException, GroupAlreadyExistException {
        groupManagementProviderService.createGroup(TestDataHolder.generateDummyGroupData(1),
                                                   DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
        groupManagementProviderService.createGroup(TestDataHolder.generateDummyGroupData(2),
                                                   DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
    }

    @Test
    public void enrollDevice() {
        Device device = TestDataHolder.generateDummyDeviceData(DEVICE_TYPE);
        try {
            boolean enrollmentStatus = deviceManagementProviderService.enrollDevice(device);
            Assert.assertTrue(enrollmentStatus);
        } catch (DeviceManagementException e) {
            String msg = "Error Occurred while enrolling device";
            Assert.fail(msg, e);
        }
    }

    @Test(dependsOnMethods = {"createGroup", "enrollDevice"})
    public void addDevices() throws GroupManagementException, DeviceNotFoundException {
        DeviceCacheConfiguration configuration = new DeviceCacheConfiguration();
        configuration.setEnabled(false);

        DeviceConfigurationManager.getInstance().getDeviceManagementConfig().setDeviceCacheConfiguration(configuration);
        List<DeviceIdentifier> list = TestDataHolder.getDeviceIdentifiersList(DEVICE_TYPE);
        DeviceGroup deviceGroup = groupManagementProviderService.getGroup(TestDataHolder.generateDummyGroupData(1).getName());
        Assert.assertNotNull(deviceGroup);
        groupManagementProviderService.addDevices(deviceGroup.getGroupId(), list);
    }

    @Test(dependsOnMethods = {"addDevices"})
    public void testIsEnrolledExtension() throws InterruptedException, GroupManagementException {
        log.info("IsEnrolled TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        count.set(0);
        eventArrived = false;

        String inStreamDefinition = "define stream inputStream (deviceId string, deviceType string);";
        String query = ("@info(name = 'query1') from inputStream[device:isEnrolled(deviceId, deviceType)] " +
                        "select deviceId insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);
        executionPlanRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        DeviceIdentifier deviceIdentifier = TestDataHolder.getDeviceIdentifiersList(DEVICE_TYPE).get(0);
        inputHandler.send(new Object[]{deviceIdentifier.getId(), deviceIdentifier.getType()});
        inputHandler.send(new Object[]{"99999", deviceIdentifier.getType()});
        SiddhiTestHelper.waitForEvents(100, 1, count, 10000);
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(1, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testIsEnrolledExtension"})
    public void testIsInGroupExtension() throws InterruptedException, GroupManagementException {
        log.info("IsInGroup TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        count.set(0);
        eventArrived = false;

        String inStreamDefinition = "define stream inputStream (groupId int, deviceId string, deviceType string);";
        String query = ("@info(name = 'query1') from inputStream[device:isInGroup(groupId, deviceId, deviceType)] " +
                        "select deviceId insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);
        executionPlanRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        DeviceIdentifier deviceIdentifier = TestDataHolder.getDeviceIdentifiersList(DEVICE_TYPE).get(0);
        inputHandler.send(new Object[]{groupManagementProviderService.getGroup(
                TestDataHolder.generateDummyGroupData(1).getName()).getGroupId(), deviceIdentifier.getId(), deviceIdentifier.getType()});
        inputHandler.send(new Object[]{groupManagementProviderService.getGroup(
                TestDataHolder.generateDummyGroupData(2).getName()).getGroupId(), deviceIdentifier.getId(), deviceIdentifier.getType()});
        SiddhiTestHelper.waitForEvents(100, 1, count, 10000);
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(1, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testIsInGroupExtension"})
    public void testGetDevicesOfUserFunctionExecutor() throws InterruptedException, GroupManagementException {
        log.info("GetDevicesOfUser without status TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        count.set(0);
        eventArrived = false;

        String inStreamDefinition = "define stream inputStream (user string, deviceType string);";
        String query = ("@info(name = 'query1') from inputStream[device:hasDevicesOfUser(user, deviceType)] " +
                        "select device:getDevicesOfUser(user, deviceType) as devices insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);
        executionPlanRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        Device device = TestDataHolder.generateDummyDeviceData(DEVICE_TYPE);
        inputHandler.send(new Object[]{device.getEnrolmentInfo().getOwner(), device.getType()});
        SiddhiTestHelper.waitForEvents(100, 1, count, 10000);
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(1, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testGetDevicesOfUserFunctionExecutor"})
    public void testGetDevicesOfUserWithStatusFunctionExecutor() throws InterruptedException, GroupManagementException {
        log.info("GetDevicesOfUser with status TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        count.set(0);
        eventArrived = false;

        String inStreamDefinition = "define stream inputStream (user string, deviceType string, status string);";
        String query = ("@info(name = 'query1') from inputStream[device:hasDevicesOfUser(user, deviceType, status)] " +
                        "select device:getDevicesOfUser(user, deviceType, status) as devices insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);
        executionPlanRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        Device device = TestDataHolder.generateDummyDeviceData(DEVICE_TYPE);
        inputHandler.send(new Object[]{device.getEnrolmentInfo().getOwner(), device.getType(),
                                       device.getEnrolmentInfo().getStatus().toString()});
        SiddhiTestHelper.waitForEvents(100, 1, count, 10000);
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(1, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testGetDevicesOfUserWithStatusFunctionExecutor"})
    public void testGetDevicesOfStatusFunctionExecutor() throws InterruptedException, GroupManagementException {
        log.info("GetDevicesOfStatus without deviceType TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        count.set(0);
        eventArrived = false;

        String inStreamDefinition = "define stream inputStream (status string);";
        String query = ("@info(name = 'query1') from inputStream[device:hasDevicesOfStatus(status)] " +
                        "select device:getDevicesOfStatus(status) as devices insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);
        executionPlanRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        Device device = TestDataHolder.generateDummyDeviceData(DEVICE_TYPE);
        inputHandler.send(new Object[]{device.getEnrolmentInfo().getStatus().toString()});
        SiddhiTestHelper.waitForEvents(100, 1, count, 10000);
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(1, count.get());
        executionPlanRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testGetDevicesOfStatusFunctionExecutor"})
    public void testGetDevicesOfStatusWithTypeFunctionExecutor() throws InterruptedException, GroupManagementException {
        log.info("GetDevicesOfStatus with deviceType TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        count.set(0);
        eventArrived = false;

        String inStreamDefinition = "define stream inputStream (status string, deviceType string);";
        String query = ("@info(name = 'query1') from inputStream[device:hasDevicesOfStatus(status, deviceType)] " +
                        "select device:getDevicesOfStatus(status, deviceType) as devices insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);
        executionPlanRuntime.addCallback("query1", queryCallback);

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        Device device = TestDataHolder.generateDummyDeviceData(DEVICE_TYPE);
        inputHandler.send(new Object[]{device.getEnrolmentInfo().getStatus().toString(), device.getType()});
        SiddhiTestHelper.waitForEvents(100, 1, count, 10000);
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(1, count.get());
        executionPlanRuntime.shutdown();
    }
}
