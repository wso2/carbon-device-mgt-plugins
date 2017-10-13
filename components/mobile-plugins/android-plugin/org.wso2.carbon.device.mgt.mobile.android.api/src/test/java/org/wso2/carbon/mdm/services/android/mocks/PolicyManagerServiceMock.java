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

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.Profile;
import org.wso2.carbon.device.mgt.common.policy.mgt.ProfileFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.PolicyAdministratorPoint;
import org.wso2.carbon.policy.mgt.common.PolicyEvaluationPoint;
import org.wso2.carbon.policy.mgt.common.PolicyInformationPoint;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.PolicyMonitoringTaskException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.policy.mgt.core.task.TaskScheduleService;

import java.util.List;

public class PolicyManagerServiceMock implements PolicyManagerService {

    @Override
    public Profile addProfile(Profile profile) throws PolicyManagementException {
        return null;
    }

    @Override
    public Profile updateProfile(Profile profile) throws PolicyManagementException {
        return null;
    }

    @Override
    public Policy addPolicy(Policy policy) throws PolicyManagementException {
        return null;
    }

    @Override
    public Policy updatePolicy(Policy policy) throws PolicyManagementException {
        return null;
    }

    @Override
    public boolean deletePolicy(Policy policy) throws PolicyManagementException {
        return false;
    }

    @Override
    public boolean deletePolicy(int i) throws PolicyManagementException {
        return false;
    }

    @Override
    public Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return null;
    }

    @Override
    public List<ProfileFeature> getEffectiveFeatures(DeviceIdentifier deviceIdentifier)
            throws FeatureManagementException {
        return null;
    }

    @Override
    public List<Policy> getPolicies(String s) throws PolicyManagementException {
        return null;
    }

    @Override
    public List<Feature> getFeatures() throws FeatureManagementException {
        return null;
    }

    @Override
    public PolicyAdministratorPoint getPAP() throws PolicyManagementException {
        return null;
    }

    @Override
    public PolicyInformationPoint getPIP() throws PolicyManagementException {
        return null;
    }

    @Override
    public PolicyEvaluationPoint getPEP() throws PolicyManagementException {
        return null;
    }

    @Override
    public TaskScheduleService getTaskScheduleService() throws PolicyMonitoringTaskException {
        return null;
    }

    @Override
    public int getPolicyCount() throws PolicyManagementException {
        return 0;
    }

    @Override
    public Policy getAppliedPolicyToDevice(DeviceIdentifier deviceIdentifier) throws PolicyManagementException {
        return null;
    }

    @Override
    public List<ComplianceFeature> checkPolicyCompliance(DeviceIdentifier deviceIdentifier, Object o)
            throws PolicyComplianceException {
        return null;
    }

    @Override
    public boolean checkCompliance(DeviceIdentifier deviceIdentifier, Object o) throws PolicyComplianceException {
        return false;
    }

    @Override
    public NonComplianceData getDeviceCompliance(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {
        return null;
    }

    @Override
    public boolean isCompliant(DeviceIdentifier deviceIdentifier) throws PolicyComplianceException {
        return false;
    }
}
