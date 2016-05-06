/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mdm.services.android.omadm.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.mdm.services.android.omadm.operations.util.OperationUtils;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.StatusTag;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.SyncMLDocument;
import org.wso2.carbon.mdm.services.android.util.AndroidAPIUtils;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;

import java.util.List;

/**
 * This class acts as an entity which maps server
 * supported device operations to OMADM operations
 */
public class OperationHandler {

    private static Log log = LogFactory.getLog(OperationHandler.class);

    private SyncMLDocument sourceDocument;
    DeviceIdentifier deviceIdentifier;

    public OperationHandler(SyncMLDocument sourceDocument) {
        this.sourceDocument = sourceDocument;
        this.deviceIdentifier = OperationUtils.convertToDeviceIdentifier(sourceDocument.
                getHeader().getSource().getLocURI());
    }

    public List<? extends Operation> getPendingOperations() throws OperationManagementException {
        List<? extends Operation> pendingOperations;
        pendingOperations = AndroidAPIUtils.getDeviceManagementService().getPendingOperations(deviceIdentifier);
        return pendingOperations;
    }

    public static void updateOperations(String deviceId, List<? extends Operation> operations)
            throws PolicyComplianceException, ApplicationManagementException, OperationManagementException {
        for (Operation operation : operations) {
            AndroidAPIUtils.updateOperation(deviceId, operation);
            if (log.isDebugEnabled()) {
                log.debug("Updating operation '" + operation.toString() + "'");
            }
        }
    }

    private void processOperationResults() throws OperationManagementException {
        List<? extends Operation> pendingOperations = getPendingOperations();
        List<StatusTag> statuses = sourceDocument.getBody().getStatus();

        for (StatusTag status : statuses) {

        }
    }

}
