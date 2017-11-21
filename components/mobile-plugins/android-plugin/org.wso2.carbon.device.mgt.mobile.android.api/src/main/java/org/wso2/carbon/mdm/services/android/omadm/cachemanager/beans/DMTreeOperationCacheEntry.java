package org.wso2.carbon.mdm.services.android.omadm.cachemanager.beans;

import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.*;

/**
 * This class wraps the DM Tree operations for caching
 */
public class DMTreeOperationCacheEntry {

    private SyncMLBody operationBody;
    private boolean isOperationsAvaliable = false;

    public DMTreeOperationCacheEntry(SyncMLBody syncMLBody) {
        this.operationBody = syncMLBody;
    }

    public SyncMLBody getOperationBody() {
        return operationBody;
    }

    public void setOperationBody(SyncMLBody operationBody) {
        this.operationBody = operationBody;
    }

    private enum OperationType {

        SEQUENCE("Sequence"),
        EXEC("Exec"),
        GET("Get"),
        ATOMIC("Atomic"),
        REPLACE("Replace"),
        ADD("Add"),
        DELETE("Delete");

        private final String code;

        OperationType(String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }
    }

}
