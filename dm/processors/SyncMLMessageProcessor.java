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

package org.wso2.carbon.mdm.services.android.omadm.dm.processors;

import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.SyncMLBody;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.SyncMLDocument;

/**
 * This class handles the processing of SyncML messages and DM Tree manipulation
 */
public class SyncMLMessageProcessor {

    private SyncMLDocument sourceDocument;
    private SyncMLDocument responseDocument;

    public SyncMLMessageProcessor(SyncMLDocument document) {
        this.sourceDocument = document;
    }

    public void processHeader() {

    }

    public void processBody() {
        SyncMLBody sourceBody = this.sourceDocument.getBody();
    }
     public void processStatus


}
