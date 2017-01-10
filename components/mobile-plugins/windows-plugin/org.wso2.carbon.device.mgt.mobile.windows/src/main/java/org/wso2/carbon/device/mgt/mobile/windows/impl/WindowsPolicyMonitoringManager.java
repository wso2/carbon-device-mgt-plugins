/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.mobile.windows.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;

import java.util.ArrayList;
import java.util.List;

public class WindowsPolicyMonitoringManager implements PolicyMonitoringManager {

    private static Log log = LogFactory.getLog(WindowsPolicyMonitoringManager.class);

    @Override
    public NonComplianceData checkPolicyCompliance(DeviceIdentifier deviceIdentifier, Policy policy, Object compliancePayload)
            throws PolicyComplianceException {
        if (log.isDebugEnabled()) {
            log.debug("checking policy compliance status of device '" + deviceIdentifier.getId() + "'");
        }
        List<ComplianceFeature> complianceFeatures = (List<ComplianceFeature>) compliancePayload;
        List<ComplianceFeature> nonComplianceFeatures = new ArrayList<>();
        NonComplianceData complianceData = new NonComplianceData();

        if (policy == null || compliancePayload == null) {
            return complianceData;
        }

        for (ComplianceFeature complianceFeature : complianceFeatures) {
            if (!complianceFeature.isCompliant()) {
                complianceData.setStatus(false);
                nonComplianceFeatures.add(complianceFeature);
                break;
            }
        }
        complianceData.setComplianceFeatures(nonComplianceFeatures);
        return complianceData;
    }

}
