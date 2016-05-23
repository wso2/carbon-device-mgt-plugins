package org.wso2.carbon.mdm.services.android.omadm.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.mdm.services.android.omadm.cachemanager.OMADMCacheManager;
import org.wso2.carbon.mdm.services.android.omadm.cachemanager.beans.DMTreeOperationCacheEntry;
import org.wso2.carbon.mdm.services.android.omadm.cachemanager.impl.OMADMCacheManagerImpl;
import org.wso2.carbon.mdm.services.android.omadm.operations.util.OperationCodes;
import org.wso2.carbon.mdm.services.android.omadm.operations.util.OperationUtils;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.*;
import org.wso2.carbon.mdm.services.android.omadm.syncml.util.SyncMLConstants;
import org.wso2.carbon.mdm.services.android.util.AndroidAPIUtils;
import org.wso2.carbon.policy.mgt.common.FeatureManagementException;
import org.wso2.carbon.policy.mgt.common.ProfileFeature;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the appending of operations to a SyncML Document Object
 */
public class OperationAppender {

    private static Log log = LogFactory.getLog(OperationAppender.class);

    private SyncMLDocument targetDocument;
    private DeviceIdentifier deviceIdentifier;

    private static final String OPERATION_ENABLED = "1";
    private static final String OPERATION_DISABLED = "0";

    private int commandId = 0;

    public OperationAppender(SyncMLDocument targetDocument, int startingCmdId) {
        this.targetDocument = targetDocument;
        this.deviceIdentifier = OperationUtils.convertToDeviceIdentifier(targetDocument.
                getHeader().getTarget().getLocURI());
        this.commandId = startingCmdId;
    }

    /**
     * Converts and appends pending operations to a SyncML document object
     */
    public void appendOperations() {
        List<? extends Operation> operations = null;

        try {
            operations = AndroidAPIUtils.getPendingOperations(deviceIdentifier);
        } catch (OperationManagementException e) {
            log.error("Issue in retrieving operation management service instance", e);
        }

        // If no pending operations are found for the device
        if (operations == null) {
            return;
        }

        for (Operation operation : operations) {
            Operation.Type operationType = operation.getType();

            switch (operationType) {
                case POLICY: {
                    generatePolicyOperation(operation);
                    break;
                }
                case CONFIG: {
                    generateConfigOperation(operation);
                    break;
                }
                case INFO: {
                    generateInfoOperation(operation);
                    break;
                }
                case COMMAND: {
                    generateCommandOperation(operation);
                    break;
                }
            }
        }

        // Persist the SyncML body for rolling back changes and further processing of the DM Tree
        OMADMCacheManager cacheManager = OMADMCacheManagerImpl.getInstance();
        DMTreeOperationCacheEntry cacheEntry = new DMTreeOperationCacheEntry(targetDocument.getBody());
        cacheManager.addOperationEntry(deviceIdentifier.getId(), cacheEntry);
    }

    /**
     * Generates relevant SyncML messages for command operations
     *
     * @param operation - Operation
     */
    private void generateCommandOperation(Operation operation) {
        ExecuteTag executeBlock;
        // Lock operation
        if (SyncMLConstants.OperationCodes.DEVICE_LOCK.equals(operation.getCode())) {
            executeBlock = generateExecuteBlock(operation);
            targetDocument.getBody().getExec().add(executeBlock);
        }
        // Ring operation
        if (SyncMLConstants.OperationCodes.DEVICE_RING.equals(operation.getCode())) {
            executeBlock = generateExecuteBlock(operation);
            targetDocument.getBody().getExec().add(executeBlock);
        }
        // Wipe data operation
        if (SyncMLConstants.OperationCodes.WIPE_DATA.equals(operation.getCode())) {
            executeBlock = generateExecuteBlock(operation);
            targetDocument.getBody().getExec().add(executeBlock);
        }
        // Reset lock operation
        if (SyncMLConstants.OperationCodes.DEVICE_MUTE.equals(operation.getCode())) {
            executeBlock = generateExecuteBlock(operation);
            targetDocument.getBody().getExec().add(executeBlock);
        }
        // Monitor operation
        if (SyncMLConstants.OperationCodes.MONITOR.equals(operation.getCode())) {
            SequenceTag monitorSequence = generateMonitorSequenceBlock(operation);
            targetDocument.getBody().setSequence(monitorSequence);
        }
    }

    private void generatePolicyOperation(Operation operation) {
        SequenceTag sequence = new SequenceTag();
        List<ReplaceTag> replaceBlocks = new ArrayList<>();
        sequence.setCommandId(operation.getId());

        if (OperationCodes.POLICY_BUNDLE.equals(operation.getCode())) {
            List<? extends Operation> policyOperations;

            try {
                policyOperations = (List<? extends Operation>) operation.getPayLoad();
            } catch (ClassCastException e) {
                throw new ClassCastException();
            }

            for (Operation policyOperation : policyOperations) {
                if (OperationCodes.CAMERA.equals(policyOperation.getCode())) {
                    ReplaceTag replaceCamera = new ReplaceTag();
                    ItemTag item = generatePolicyCommandItem(operation);
                    replaceCamera.setCommandId(operation.getId());
                    replaceCamera.getItems().add(item);
                    replaceBlocks.add(replaceCamera);
                }
            }
            if (!replaceBlocks.isEmpty()) {
                sequence.setReplaces(replaceBlocks);
            }
        }
        targetDocument.getBody().setSequence(sequence);
    }

    private void generateInfoOperation(Operation operation) {

    }

    private void generateConfigOperation(Operation operation) {

    }

    /**
     * Generates a SyncML 'Exec' block from an operation
     *
     * @param operation - Operation
     * @return - Generated 'Exec' element object
     */
    private ExecuteTag generateExecuteBlock(Operation operation) {
        ExecuteTag execBlock = new ExecuteTag();
        execBlock.setCommandId(operation.getId());
        ItemTag item = new ItemTag();

        for (OperationCodes.Command command : OperationCodes.Command.values()) {
            if (operation.getCode() != null && operation.getCode().equals(command.name())) {
                TargetTag target = new TargetTag();
                target.setLocURI(command.getCode());
                item.setTarget(target);
            }
        }
        execBlock.getItems().add(item);
        return execBlock;
    }

    /**
     * Generates a SyncML 'Sequence' block to represent a monitor operation
     *
     * @param operation - Operation
     * @return - Generated 'Sequence' element object
     */
    private SequenceTag generateMonitorSequenceBlock(Operation operation) {
        SequenceTag sequence = new SequenceTag();
        GetTag get = new GetTag();
        List<ItemTag> items = new ArrayList<>();
        List<ProfileFeature> profileFeatures = null;

        sequence.setCommandId(operation.getId());
        try {
            profileFeatures = AndroidAPIUtils.getPolicyManagerService().getEffectiveFeatures(deviceIdentifier);
        } catch (FeatureManagementException e) {
            log.error("Issue in retrieving feature management service instance", e);
        }

        Operation monitorOperation;
        for (ProfileFeature profileFeature : profileFeatures) {

            if (OperationCodes.CAMERA.equals
                    (profileFeature.getFeatureCode())) {
                monitorOperation = new Operation();
                monitorOperation.setCode(OperationCodes.CAMERA_STATUS);
                ItemTag item = generateInfoCommandItem(monitorOperation);
                items.add(item);
            }
            if (OperationCodes.ENCRYPT_STORAGE.equals
                    (profileFeature.getFeatureCode())) {
                monitorOperation = new Operation();
                monitorOperation.setCode(OperationCodes.ENCRYPT_STORAGE_STATUS);
                ItemTag item = generateInfoCommandItem(monitorOperation);
                items.add(item);
            }
            if (OperationCodes.PASSCODE_POLICY.equals
                    (profileFeature.getFeatureCode())) {
                monitorOperation = new Operation();
                monitorOperation.setCode(OperationCodes.DEVICE_PASSWORD_STATUS);
                ItemTag item = generateInfoCommandItem(monitorOperation);
                items.add(item);
            }
        }
        if (!items.isEmpty()) {
            get.setCommandId(operation.getId());
            get.setItems(items);
        }
        sequence.setGet(get);
        return sequence;
    }

    /**
     * Generates a SyncML 'Item' block from an operation
     *
     * @param operation - Operation
     * @return - generated 'Item' element object
     */
    private ItemTag generateInfoCommandItem(Operation operation) {
        ItemTag item = new ItemTag();
        String operationCode = operation.getCode();
        for (OperationCodes.Info info : OperationCodes.Info.values()) {
            if (operationCode != null && operationCode.equals(info.name())) {
                TargetTag target = new TargetTag();
                target.setLocURI(info.getCode());
                item.setTarget(target);
            }
        }
        return item;
    }

    /**
     * Generates an 'Item' block from a given operation
     *
     * @param operation - Operation
     * @return - Generated item tag
     */
    private ItemTag generatePolicyCommandItem(Operation operation) {
        ItemTag item = new ItemTag();
        TargetTag target = new TargetTag();
        JSONObject payload = null;

        try {
            payload = new JSONObject(operation.getPayLoad().toString());
        } catch (JSONException e) {
            log.error("Issue in parsing JSON message.", e);
        }

        for (OperationCodes.Command command : OperationCodes.Command.values()) {
            if (operation.getCode() != null && operation.getCode().equals(command.name())) {
                target.setLocURI(command.getCode());

                if (OperationCodes.CAMERA.equals(operation.getCode()) && payload != null) {
                    try {
                        MetaTag meta = new MetaTag();
                        meta.setFormat(SyncMLConstants.SyncMLTags.META_FORMAT_INT);
                        item.setTarget(target);
                        item.setMeta(meta);
                        if (payload.getBoolean("enabled")) {
                            item.setData(OPERATION_ENABLED);
                        } else {
                            item.setData(OPERATION_DISABLED);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return item;
    }

}
