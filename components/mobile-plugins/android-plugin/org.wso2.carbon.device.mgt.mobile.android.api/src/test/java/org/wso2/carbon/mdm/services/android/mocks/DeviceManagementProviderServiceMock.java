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

package org.wso2.carbon.mdm.services.android.mocks;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.MonitoringOperation;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import org.wso2.carbon.device.mgt.common.pull.notification.PullNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.EmailMetaInfo;
import org.wso2.carbon.mdm.services.android.utils.TestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DeviceManagementProviderServiceMock implements DeviceManagementProviderService {
    @Override
    public List<Device> getAllDevices(String s) throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Device> getAllDevices(String s, boolean b) throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Device> getAllDevices() throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Device> getAllDevices(boolean b) throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Device> getDevices(Date date) throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Device> getDevices(Date date, boolean b) throws DeviceManagementException {
        return null;
    }

    @Override
    public PaginationResult getDevicesByType(PaginationRequest paginationRequest) throws DeviceManagementException {
        return null;
    }

    @Override
    public PaginationResult getDevicesByType(PaginationRequest paginationRequest, boolean b)
            throws DeviceManagementException {
        return null;
    }

    @Override
    public PaginationResult getAllDevices(PaginationRequest paginationRequest) throws DeviceManagementException {
        return null;
    }

    @Override
    public PaginationResult getAllDevices(PaginationRequest paginationRequest, boolean b)
            throws DeviceManagementException {
        return null;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        if (TestUtils.getDeviceId().equals(deviceIdentifier.getId())) {
            return TestUtils.getDevice();
        } else {
            return null;
        }
    }

    @Override
    public Device getDeviceWithTypeProperties(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        return null;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceIdentifier, boolean b) throws DeviceManagementException {
        if (TestUtils.getDeviceId().equals(deviceIdentifier.getId())) {
            return TestUtils.getDevice();
        } else {
            return null;
        }
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceIdentifier, String s, boolean b) throws DeviceManagementException {
        return null;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceIdentifier, Date date) throws DeviceManagementException {
        return null;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceIdentifier, Date date, boolean b) throws DeviceManagementException {
        return null;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceIdentifier, String s, Date date, boolean b)
            throws DeviceManagementException {
        return null;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceIdentifier, EnrolmentInfo.Status status)
            throws DeviceManagementException {
        return null;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceIdentifier, EnrolmentInfo.Status status, boolean b)
            throws DeviceManagementException {
        return null;
    }

    @Override
    public PaginationResult getDevicesOfUser(PaginationRequest paginationRequest) throws DeviceManagementException {
        return null;
    }

    @Override
    public PaginationResult getDevicesOfUser(PaginationRequest paginationRequest, boolean b)
            throws DeviceManagementException {
        return null;
    }

    @Override
    public PaginationResult getDevicesByOwnership(PaginationRequest paginationRequest)
            throws DeviceManagementException {
        return null;
    }

    @Override
    public PaginationResult getDevicesByOwnership(PaginationRequest paginationRequest, boolean b)
            throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Device> getDevicesOfUser(String s) throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Device> getDevicesOfUser(String s, boolean b) throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Device> getDevicesOfUser(String s, String s1) throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Device> getDevicesOfUser(String s, String s1, boolean b) throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Device> getAllDevicesOfRole(String s) throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Device> getAllDevicesOfRole(String s, boolean b) throws DeviceManagementException {
        return null;
    }

    @Override
    public PaginationResult getDevicesByStatus(PaginationRequest paginationRequest) throws DeviceManagementException {
        return null;
    }

    @Override
    public PaginationResult getDevicesByStatus(PaginationRequest paginationRequest, boolean b)
            throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Device> getDevicesByNameAndType(PaginationRequest paginationRequest, boolean b)
            throws DeviceManagementException {
        return null;
    }

    @Override
    public PaginationResult getDevicesByName(PaginationRequest paginationRequest) throws DeviceManagementException {
        return null;
    }

    @Override
    public PaginationResult getDevicesByName(PaginationRequest paginationRequest, boolean b)
            throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Device> getDevicesByStatus(EnrolmentInfo.Status status) throws DeviceManagementException {
        return null;
    }

    @Override
    public List<Device> getDevicesByStatus(EnrolmentInfo.Status status, boolean b) throws DeviceManagementException {
        return null;
    }

    @Override
    public int getDeviceCount(String s) throws DeviceManagementException {
        return 0;
    }

    @Override
    public int getDeviceCount() throws DeviceManagementException {
        return 0;
    }

    @Override
    public HashMap<Integer, Device> getTenantedDevice(DeviceIdentifier deviceIdentifier)
            throws DeviceManagementException {
        return null;
    }

    @Override
    public void sendEnrolmentInvitation(String s, EmailMetaInfo emailMetaInfo)
            throws DeviceManagementException, ConfigurationManagementException {

    }

    @Override
    public void sendRegistrationEmail(EmailMetaInfo emailMetaInfo)
            throws DeviceManagementException, ConfigurationManagementException {

    }

    @Override
    public FeatureManager getFeatureManager(String s) throws DeviceManagementException {
        return null;
    }

    @Override
    public PlatformConfiguration getConfiguration(String s) throws DeviceManagementException {
        return null;
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceIdentifier, String s) throws DeviceManagementException {
        return false;
    }

    @Override
    public NotificationStrategy getNotificationStrategyByDeviceType(String s) throws DeviceManagementException {
        return null;
    }

    @Override
    public License getLicense(String s, String s1) throws DeviceManagementException {
        return null;
    }

    @Override
    public void addLicense(String s, License license) throws DeviceManagementException {

    }

    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        return TestUtils.getDeviceId().equals(device.getDeviceIdentifier());
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        return TestUtils.getDeviceId().equals(device.getDeviceIdentifier());
    }

    @Override
    public boolean saveConfiguration(PlatformConfiguration platformConfiguration) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        return TestUtils.getDeviceId().equals(deviceIdentifier.getId());
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean isActive(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setActive(DeviceIdentifier deviceIdentifier, boolean b) throws DeviceManagementException {
        return false;
    }

    @Override
    public List<String> getAvailableDeviceTypes() throws DeviceManagementException {
        return null;
    }

    @Override
    public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setOwnership(DeviceIdentifier deviceIdentifier, String s) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean isClaimable(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setStatus(DeviceIdentifier deviceIdentifier, String s, EnrolmentInfo.Status status)
            throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setStatus(String s, EnrolmentInfo.Status status) throws DeviceManagementException {
        return false;
    }

    @Override
    public void notifyOperationToDevices(Operation operation, List<DeviceIdentifier> list)
            throws DeviceManagementException {

    }

    @Override
    public Activity addOperation(String s, Operation operation, List<DeviceIdentifier> list)
            throws OperationManagementException, InvalidDeviceException {
        return TestUtils.getActivity();
    }

    @Override
    public List<? extends Operation> getOperations(DeviceIdentifier deviceIdentifier)
            throws OperationManagementException {
        return null;
    }

    @Override
    public PaginationResult getOperations(DeviceIdentifier deviceIdentifier, PaginationRequest paginationRequest)
            throws OperationManagementException {
        return null;
    }

    @Override
    public List<? extends Operation> getPendingOperations(DeviceIdentifier deviceIdentifier)
            throws OperationManagementException {
        return null;
    }

    @Override
    public Operation getNextPendingOperation(DeviceIdentifier deviceIdentifier) throws OperationManagementException {
        return null;
    }

    @Override
    public void updateOperation(DeviceIdentifier deviceIdentifier, Operation operation)
            throws OperationManagementException {

    }

    @Override
    public Operation getOperationByDeviceAndOperationId(DeviceIdentifier deviceIdentifier, int i)
            throws OperationManagementException {
        return null;
    }

    @Override
    public List<? extends Operation> getOperationsByDeviceAndStatus(DeviceIdentifier deviceIdentifier,
                                                                    Operation.Status status)
            throws OperationManagementException, DeviceManagementException {
        return null;
    }

    @Override
    public Operation getOperation(String s, int i) throws OperationManagementException {
        return null;
    }

    @Override
    public Activity getOperationByActivityId(String s) throws OperationManagementException {
        return null;
    }

    @Override
    public Activity getOperationByActivityIdAndDevice(String s, DeviceIdentifier deviceIdentifier)
            throws OperationManagementException {
        return null;
    }

    @Override
    public List<Activity> getActivitiesUpdatedAfter(long l, int i, int i1) throws OperationManagementException {
        return null;
    }

    @Override
    public int getActivityCountUpdatedAfter(long l) throws OperationManagementException {
        return 0;
    }

    @Override
    public List<MonitoringOperation> getMonitoringOperationList(String s) {
        return null;
    }

    @Override
    public int getDeviceMonitoringFrequency(String s) {
        return 0;
    }

    @Override
    public boolean isDeviceMonitoringEnabled(String s) {
        return false;
    }

    @Override
    public PolicyMonitoringManager getPolicyMonitoringManager(String s) {
        return null;
    }

    @Override
    public boolean changeDeviceStatus(DeviceIdentifier deviceIdentifier, EnrolmentInfo.Status status)
            throws DeviceManagementException {
        return false;
    }

    @Override
    public void registerDeviceType(DeviceManagementService deviceManagementService) throws DeviceManagementException {

    }

    @Override
    public DeviceType getDeviceType(String s) throws DeviceManagementException {
        return null;
    }

    @Override
    public List<DeviceType> getDeviceTypes() throws DeviceManagementException {
        return null;
    }

    @Override
    public void notifyPullNotificationSubscriber(DeviceIdentifier deviceIdentifier, Operation operation)
            throws PullNotificationExecutionFailedException {

    }

    @Override
    public List<Integer> getDeviceEnrolledTenants() throws DeviceManagementException {
        return null;
    }
}
