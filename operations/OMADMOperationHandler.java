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
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementService;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.mdm.services.android.omadm.operations.beans.ProfileOperation;
import org.wso2.carbon.mdm.services.android.omadm.operations.util.OperationCodes;
import org.wso2.carbon.mdm.services.android.omadm.operations.util.OperationUtils;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.ItemTag;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.ResultsTag;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.StatusTag;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.SyncMLDocument;
import org.wso2.carbon.mdm.services.android.omadm.syncml.util.Constants;
import org.wso2.carbon.mdm.services.android.util.AndroidAPIUtils;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.ProfileFeature;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceFeature;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class acts as an entity which maps server
 * supported device operations to OMADM operations
 */
public class OMADMOperationHandler {

    private static Log log = LogFactory.getLog(OMADMOperationHandler.class);

    private SyncMLDocument sourceDocument;
    DeviceIdentifier deviceIdentifier;
    int commandId = 0;

    private static final String FEATURE_ENABLED = "1";

    public OMADMOperationHandler(SyncMLDocument sourceDocument, int startingCmdId) {
        this.sourceDocument = sourceDocument;
        this.deviceIdentifier = OperationUtils.convertToDeviceIdentifier(sourceDocument.
                getHeader().getSource().getLocURI());
        this.commandId = startingCmdId;
    }

    /**
     * Returns a list of pending operations
     *
     * @return - A list of operations
     */
    private List<? extends Operation> getPendingOperations() {
        List<? extends Operation> pendingOperations = null;
        try {
            pendingOperations = AndroidAPIUtils.getDeviceManagementService().getPendingOperations(deviceIdentifier);
        } catch (OperationManagementException e) {
            log.error("Issue in retrieving operation management service instance", e);
        }
        return pendingOperations;
    }

    /**
     * Updates the status of a single operation
     *
     * @param deviceId  - Device Identifier
     * @param operation - Operation object
     */
    private static void updateOperationStatus(DeviceIdentifier deviceId, Operation operation) {
        try {
            AndroidAPIUtils.updateOperation(deviceId.getId(), operation);
        } catch (OperationManagementException e) {
            log.error("Issue in retrieving operation management service instance", e);
        } catch (PolicyComplianceException e) {
            log.error("Issue in updating Monitoring operation");
        } catch (ApplicationManagementException e) {
            log.error("Issue in retrieving application management service instance", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Updating operation '" + operation.toString() + "'");
        }
    }

    public void updateOperations() {
        List<? extends Operation> pendingOperations;
        List<StatusTag> statuses = sourceDocument.getBody().getStatus();

        pendingOperations = getPendingOperations();

        for (StatusTag status : statuses) {
            // Process 'Exec' tags
            if (Constants.SyncMLTags.EXECUTE.equals(status.getCommand())) {
                // If the whole execution operation batch is successful
                if (status.getTargetReference() == null) {
                    updateExecutionOperationBatch(status, pendingOperations);
                } else {
                    if (OperationCodes.Command.DEVICE_LOCK.getCode().equals(status.getTargetReference())) {
                        updateLockOperationStatus(status, pendingOperations);
                    }
                    if (OperationCodes.Command.DEVICE_RING.getCode().equals(status.getTargetReference())) {
                        updateRingOperationStatus(status, pendingOperations);
                    }
                    if (OperationCodes.Command.DEVICE_WIPE.getCode().equals(status.getTargetReference())) {
                        updateWipeOperation(status, pendingOperations);
                    }
                }
            }
            // Process 'Sequence' tags
            if (Constants.SEQUENCE.equals(status.getCommand())) {
                if (Constants.SyncMLResponseCodes.ACCEPTED.equals(status.getData())) {
                    for (Operation operation : pendingOperations) {
                        if (Constants.OperationCodes.POLICY_BUNDLE.equals(operation.getCode()) &&
                                operation.getId() == status.getCommandReference()) {
                            operation.setStatus(Operation.Status.COMPLETED);
                        }
                        if (Constants.OperationCodes.MONITOR.equals(operation.getCode()) &&
                                operation.getId() == status.getCommandReference()) {
                            operation.setStatus(Operation.Status.COMPLETED);
                        }
                        updateOperationStatus(deviceIdentifier, operation);
                    }
                } else {
                    for (Operation operation : pendingOperations) {
                        if (Constants.OperationCodes.POLICY_BUNDLE.equals(operation.getCode()) &&
                                operation.getId() == status.getCommandReference()) {
                            operation.setStatus(Operation.Status.ERROR);
                        }
                        if (Constants.OperationCodes.MONITOR.equals(operation.getCode()) &&
                                operation.getId() == status.getCommandReference()) {
                            operation.setStatus(Operation.Status.ERROR);
                        }
                        updateOperationStatus(deviceIdentifier, operation);
                        sendOperationStatusNotification(status.getCommandReference());
                    }
                }
            }
        }
        checkComplianceFeatureStatuses();
    }

    /**
     * Updates the status of a Ring Operation
     *
     * @param status     - A status block in a SyncML Document
     * @param operations - A list of operations
     */
    private void updateWipeOperation(StatusTag status, List<? extends Operation> operations) {
        if (Constants.SyncMLResponseCodes.ACCEPTED.equals(status.getData())) {
            for (Operation operation : operations) {
                if ((OperationCodes.Command.DEVICE_WIPE.getCode().equals(operation.getCode()))
                        && operation.getId() == status.getCommandReference()) {
                    operation.setStatus(Operation.Status.COMPLETED);
                    updateOperationStatus(deviceIdentifier, operation);
                }
            }
        } else {
            for (Operation operation : operations) {
                if ((OperationCodes.Command.DEVICE_WIPE.getCode().equals(operation.getCode()))
                        && operation.getId() == status.getCommandReference()) {
                    operation.setStatus(Operation.Status.ERROR);
                    updateOperationStatus(deviceIdentifier, operation);
                    sendOperationStatusNotification(status.getCommandReference());
                }
            }
        }
    }

    /**
     * Updates the status of a Ring Operation
     *
     * @param status     - A status block in a SyncML Document
     * @param operations - A list of operations
     */
    private void updateRingOperationStatus(StatusTag status, List<? extends Operation> operations) {
        if (Constants.SyncMLResponseCodes.ACCEPTED.equals(status.getData())) {
            for (Operation operation : operations) {
                if ((OperationCodes.Command.DEVICE_RING.getCode().equals(operation.getCode()))
                        && operation.getId() == status.getCommandReference()) {
                    operation.setStatus(Operation.Status.COMPLETED);
                    updateOperationStatus(deviceIdentifier, operation);
                }
            }
        } else {
            for (Operation operation : operations) {
                if ((OperationCodes.Command.DEVICE_RING.getCode().equals(operation.getCode()))
                        && operation.getId() == status.getCommandReference()) {
                    operation.setStatus(Operation.Status.ERROR);
                    updateOperationStatus(deviceIdentifier, operation);
                    sendOperationStatusNotification(status.getCommandReference());
                }
            }
        }
    }

    /**
     * Updates the status of a Lock Operation
     *
     * @param status     - A status block in a SyncML Document
     * @param operations - A list of operations
     */
    private void updateLockOperationStatus(StatusTag status, List<? extends Operation> operations) {
        if (Constants.SyncMLResponseCodes.ACCEPTED.equals(status.getData())) {
            for (Operation operation : operations) {
                if ((OperationCodes.Command.DEVICE_LOCK.getCode().equals(operation.getCode()))
                        && operation.getId() == status.getCommandReference()) {
                    operation.setStatus(Operation.Status.COMPLETED);
                    updateOperationStatus(deviceIdentifier, operation);
                }
            }
        } else {
            for (Operation operation : operations) {
                if ((OperationCodes.Command.DEVICE_LOCK.getCode().equals(operation.getCode()))
                        && operation.getId() == status.getCommandReference()) {
                    operation.setStatus(Operation.Status.ERROR);
                    updateOperationStatus(deviceIdentifier, operation);
                    sendOperationStatusNotification(status.getCommandReference());
                }
            }
        }
    }

    /**
     * Executes a set of operations included in a single 'Exec' block
     *
     * @param status     - A status block in a SyncML Document
     * @param operations - A list of operations
     */
    private void updateExecutionOperationBatch(StatusTag status, List<? extends Operation> operations) {
        if (Constants.SyncMLResponseCodes.ACCEPTED.equals(status.getData()) ||
                (Constants.SyncMLResponseCodes.ACCEPTED_FOR_PROCESSING.equals(status.getData()))) {
            for (Operation operation : operations) {
                if (operation.getId() == status.getCommandReference()) {
                    operation.setStatus(Operation.Status.COMPLETED);
                    updateOperationStatus(deviceIdentifier, operation);
                }
            }
        } else {
            for (Operation operation : operations) {
                if (operation.getId() == status.getCommandReference()) {
                    operation.setStatus(Operation.Status.ERROR);
                    updateOperationStatus(deviceIdentifier, operation);
                    sendOperationStatusNotification(status.getCommandReference());
                }
            }
        }
    }

    /**
     * Generates a list of Profile Operations from a 'Results' block of a SyncML Document
     *
     * @return - A list of Profile Operations
     */
    private List<ProfileOperation> generateProfileOperations() {
        List<ProfileOperation> profileOperations = new ArrayList<>();
        ResultsTag results = sourceDocument.getBody().getResults();

        if (results != null) {
            List<ItemTag> items = results.getItems();

            for (ItemTag item : items) {
                // Camera status
                if (item.getSource().getLocURI().equals(OperationCodes.Info.CAMERA_STATUS.getCode())) {
                    ProfileOperation cameraProfile = new ProfileOperation();
                    cameraProfile.setFeatureCode(Constants.OperationCodes.CAMERA);
                    cameraProfile.setData(item.getData());
                    if (FEATURE_ENABLED.equals(item.getData())) {
                        cameraProfile.setIsEnabled(true);
                    } else {
                        cameraProfile.setIsEnabled(false);
                    }
                    profileOperations.add(cameraProfile);
                }
                // Storage encryption status
                if (item.getSource().getLocURI().equals(OperationCodes.Info.ENCRYPT_STORAGE_STATUS.getCode())) {
                    ProfileOperation encryptProfile = new ProfileOperation();
                    encryptProfile.setFeatureCode(Constants.OperationCodes.ENCRYPT_STORAGE);
                    encryptProfile.setData(item.getData());
                    if (FEATURE_ENABLED.equals(item.getData())) {
                        encryptProfile.setIsEnabled(true);
                    } else {
                        encryptProfile.setIsEnabled(false);
                    }
                    profileOperations.add(encryptProfile);
                }
                // Passcode status
                if (item.getSource().getLocURI().equals(OperationCodes.Info.DEVICE_PASSCODE_STATUS.getCode())) {
                    ProfileOperation passcodeProfile = new ProfileOperation();
                    passcodeProfile.setFeatureCode(Constants.OperationCodes.PASSCODE_POLICY);
                    passcodeProfile.setData(item.getData());
                    if (FEATURE_ENABLED.equals(item.getData())) {
                        passcodeProfile.setIsEnabled(true);
                    } else {
                        passcodeProfile.setIsEnabled(false);
                    }
                    profileOperations.add(passcodeProfile);
                }
            }
        }
        return profileOperations;
    }

    private void checkComplianceFeatureStatuses() {
        List<ProfileOperation> profileOperations = generateProfileOperations();
        List<ProfileFeature> profileFeatures = null;

        if (profileOperations != null && profileOperations.size() > 0) {

            try {
                profileFeatures = AndroidAPIUtils.getPolicyManagerService().getAppliedPolicyToDevice(deviceIdentifier).getProfile().
                        getProfileFeaturesList();
            } catch (PolicyManagementException e) {
                log.error("Issue in retrieving profile features.", e);
            }

            if (profileFeatures != null) {
                List<ComplianceFeature> complianceFeatures = new ArrayList<>();
                for (ProfileFeature activeFeature : profileFeatures) {
                    try {
                        JSONObject policyContent = new JSONObject(activeFeature.getContent().toString());
                    } catch (JSONException e) {
                        log.error("Issue in parsing JSON.", e);
                    }

                    for (ProfileOperation profileOperation : profileOperations) {
                        if (profileOperation.getFeatureCode().equals(activeFeature.getFeatureCode()) &&
                                profileOperation.getFeatureCode().equals(Constants.OperationCodes.CAMERA)) {
                            if (profileOperation.isEnabled()) {
                                profileOperation.setIsCompliant(true);
                            } else {
                                profileOperation.setIsCompliant(false);
                            }
                            ComplianceFeature complianceFeature = generateComplianceFeature(activeFeature,
                                    profileOperation);
                            complianceFeatures.add(complianceFeature);
                        }
                        if (profileOperation.getFeatureCode().equals(activeFeature.getFeatureCode()) &&
                                profileOperation.getFeatureCode().equals(Constants.OperationCodes.ENCRYPT_STORAGE)) {
                            if (profileOperation.isEnabled()) {
                                profileOperation.setIsCompliant(true);
                            } else {
                                profileOperation.setIsCompliant(false);
                            }
                            ComplianceFeature complianceFeature = generateComplianceFeature(activeFeature,
                                    profileOperation);
                            complianceFeatures.add(complianceFeature);
                        }
                        if (profileOperation.getFeatureCode().equals(activeFeature.getFeatureCode()) &&
                                profileOperation.getFeatureCode().equals(Constants.OperationCodes.PASSCODE_POLICY)) {
                            if (profileOperation.isEnabled()) {
                                profileOperation.setIsCompliant(true);
                            } else {
                                profileOperation.setIsCompliant(false);
                            }
                            ComplianceFeature complianceFeature = generateComplianceFeature(activeFeature,
                                    profileOperation);
                            complianceFeatures.add(complianceFeature);
                        }
                        try {
                            AndroidAPIUtils.getPolicyManagerService().checkPolicyCompliance(deviceIdentifier, complianceFeatures);
                        } catch (PolicyComplianceException e) {
                            log.error("Issue in checking policy compliance.", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates a ComplianceFeature object from a Profile Feature and a Profile Operation
     *
     * @param profileFeature - Profile Feature
     * @param profileOperation - Profile Operation
     * @return - Generated Compliance Feature
     */
    private ComplianceFeature generateComplianceFeature(ProfileFeature profileFeature, ProfileOperation profileOperation) {
        ComplianceFeature complianceFeature = new ComplianceFeature();
        complianceFeature.setFeature(profileFeature);
        complianceFeature.setFeatureCode(profileFeature.getFeatureCode());
        complianceFeature.setCompliance(profileOperation.isCompliant());
        return complianceFeature;
    }

    /**
     * Sends a operation notification to the core
     *
     * @param operationId - Operation Identifier
     */
    private void sendOperationStatusNotification(int operationId) {
        // Send a failure notification
        NotificationManagementService nmService = AndroidAPIUtils.getNotificationManagementService();
        Notification commandFailedNotification = new Notification();
        commandFailedNotification.setOperationId(operationId);
        commandFailedNotification.setStatus(String.valueOf(Notification.Status.NEW));
        commandFailedNotification.setDeviceIdentifier(deviceIdentifier);
        commandFailedNotification.setDescription("Command failed.");
        try {
            nmService.addNotification(commandFailedNotification);
        } catch (NotificationManagementException e) {
            log.error("Issue in retrieving Notification management service instance", e);
        }
    }

}
