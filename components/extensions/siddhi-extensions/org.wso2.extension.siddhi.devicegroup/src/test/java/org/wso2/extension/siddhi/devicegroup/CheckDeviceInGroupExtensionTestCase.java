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

package org.wso2.extension.siddhi.devicegroup;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
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
import org.wso2.extension.siddhi.devicegroup.test.util.SiddhiTestHelper;
import org.wso2.extension.siddhi.devicegroup.test.util.TestDataHolder;
import org.wso2.extension.siddhi.devicegroup.test.util.TestDeviceManagementService;
import org.wso2.extension.siddhi.devicegroup.test.util.TestUtils;
import org.wso2.extension.siddhi.devicegroup.utils.DeviceGroupUtils;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants.Permissions.DEFAULT_ADMIN_PERMISSIONS;
import static org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants.Roles.DEFAULT_ADMIN_ROLE;

public class CheckDeviceInGroupExtensionTestCase extends BaseDeviceManagementTest {
    private static final Logger log = Logger.getLogger(CheckDeviceInGroupExtensionTestCase.class);

    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;
    private GroupManagementProviderService groupManagementProviderService;
    private DeviceManagementProviderService deviceMgtService;
    private static String DEVICE_TYPE = "Test";

    @BeforeClass
    @Override
    public void init() throws Exception {
        log.info("Initializing");
        count.set(0);
        eventArrived = false;
        groupManagementProviderService = new GroupManagementProviderServiceImpl();
        deviceMgtService = new DeviceManagementProviderServiceImpl();
        DeviceGroupUtils.setGroupManagementProviderServiceForTest(groupManagementProviderService);

        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
        DeviceManagementDataHolder.getInstance().setRegistryService(getRegistryService());
        DeviceManagementDataHolder.getInstance().setDeviceAccessAuthorizationService(new DeviceAccessAuthorizationServiceImpl());
        DeviceManagementDataHolder.getInstance().setGroupManagementProviderService(groupManagementProviderService);
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(null);
        deviceMgtService.registerDeviceType(
                new TestDeviceManagementService(DEVICE_TYPE, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
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
        groupManagementProviderService.createGroup(TestUtils.createDeviceGroup1(), DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
        groupManagementProviderService.createGroup(TestUtils.createDeviceGroup2(), DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
    }

    @Test
    public void enrollDevice() {
        Device device = TestDataHolder.generateDummyDeviceData(DEVICE_TYPE);
        try {
            boolean enrollmentStatus = deviceMgtService.enrollDevice(device);
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
        List<DeviceIdentifier> list = TestUtils.getDeviceIdentifiersList(DEVICE_TYPE);
        DeviceGroup deviceGroup = groupManagementProviderService.getGroup(TestUtils.createDeviceGroup1().getName());
        Assert.assertNotNull(deviceGroup);
        groupManagementProviderService.addDevices(deviceGroup.getGroupId(), list);
    }

    @Test(dependsOnMethods = {"addDevices"})
    public void testIsDeviceInGroupExtension() throws InterruptedException, GroupManagementException {
        log.info("IsDeviceInGroup TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (groupId int, deviceId string, deviceType string);";
        String query = ("@info(name = 'query1') from inputStream[devicegroup:isDeviceInGroup(groupId, deviceId, deviceType) == true] " +
                        "select deviceId insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        DeviceIdentifier deviceIdentifier = TestUtils.getDeviceIdentifiersList(DEVICE_TYPE).get(0);
        inputHandler.send(new Object[]{groupManagementProviderService.getGroup(
                TestUtils.createDeviceGroup1().getName()).getGroupId(), deviceIdentifier.getId(), deviceIdentifier.getType()});
        inputHandler.send(new Object[]{groupManagementProviderService.getGroup(
                TestUtils.createDeviceGroup2().getName()).getGroupId(), deviceIdentifier.getId(), deviceIdentifier.getType()});
        SiddhiTestHelper.waitForEvents(100, 1, count, 10000);
        Assert.assertTrue(eventArrived);
        Assert.assertEquals(1, count.get());
        executionPlanRuntime.shutdown();
    }
}
