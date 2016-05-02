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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mdm.services.android.omadm.ddf.MgmtTreeManager;
import org.wso2.carbon.mdm.services.android.omadm.ddf.impl.MgmtTreeManagerImpl;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.MgmtTree;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.parsers.URIParser;
import org.wso2.carbon.mdm.services.android.omadm.dm.dao.DeviceMODao;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.*;
import org.wso2.carbon.mdm.services.android.omadm.syncml.util.Constants;
import org.wso2.carbon.mdm.services.android.omadm.syncml.util.SyncMLStatusCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the processing of SyncML messages and DM Tree manipulation
 */
public class SyncMLMessageProcessor {

    private SyncMLDocument sourceDocument;
    private SyncMLDocument responseDocument;
    private DeviceMODao moDao = DeviceMODao.getInstance();

    private static final int HEADER_STATUS_COMMAND_ID = 1;
    private static final int HEADER_COMMAND_REF_ID = 0;

    private int headerCommandId = HEADER_STATUS_COMMAND_ID;

    private static Log log = LogFactory.getLog(SyncMLMessageProcessor.class);

    public SyncMLMessageProcessor(SyncMLDocument document) {
        this.sourceDocument = document;
        this.responseDocument = new SyncMLDocument();
    }

    public void processHeader() {
    }

    public void processBody() {

        SyncMLBody sourceBody = this.sourceDocument.getBody();

        // Process status blocks
        if (sourceBody.getStatus() != null) {
            processStatuses();
        }
    }

    /**
     * This method processes the status blocks of the message while updating the DM Tree
     */
    public void processStatuses() {
        List<StatusTag> sourceStatuses = sourceDocument.getBody().getStatus();
        List<StatusTag> targetStatuses = new ArrayList<>();

        // Generate header status
        StatusTag headerStatus = new StatusTag(HEADER_STATUS_COMMAND_ID, sourceDocument.getHeader().getMsgID(),
                HEADER_COMMAND_REF_ID, Constants.SyncMLTags.SYNC_HDR,
                sourceDocument.getHeader().getSource().getLocURI(),
                SyncMLStatusCodes.AUTHENTICATION_ACCEPTED.getCode());
        targetStatuses.add(headerStatus);
    }

    public void processReplaceCommands() {
        ReplaceTag replaceTag = sourceDocument.getBody().getReplace();
        int replaceCmdId = replaceTag.getCommandId();
        List<ItemTag> items = replaceTag.getItems();
        List<StatusTag> statuses = new ArrayList<>();

        for (ItemTag item : items) {
            String locURI = item.getSource().getLocURI();
            StatusTag status = new StatusTag();

            MgmtTree tree = moDao.getMO(URIParser.getDMTreeName(locURI),
                    sourceDocument.getHeader().getSource().getLocURI());

            MgmtTreeManager treeManager = new MgmtTreeManagerImpl(tree);
            String statusCode = treeManager.replaceNodeDetails(locURI, item);
            status.setData(statusCode);
            status.setMessageReference(sourceDocument.getHeader().getMsgID());
            status.setCommandReference(replaceCmdId);
            status.setCommandId(++headerCommandId);
            statuses.add(status);
        }
    }

}
