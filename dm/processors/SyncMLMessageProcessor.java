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
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.Node;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.parsers.URIParser;
import org.wso2.carbon.mdm.services.android.omadm.dm.dao.DeviceMODao;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.*;
import org.wso2.carbon.mdm.services.android.omadm.syncml.util.Constants;
import org.wso2.carbon.mdm.services.android.omadm.syncml.util.SyncMLStatusCodes;
import org.wso2.carbon.mdm.services.android.omadm.util.AlertCodeUtils;

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

    // Kept as a counter to generate command IDs
    private int headerCommandId = HEADER_STATUS_COMMAND_ID;

    private static Log log = LogFactory.getLog(SyncMLMessageProcessor.class);

    public SyncMLMessageProcessor(SyncMLDocument document) {
        this.sourceDocument = document;
        this.responseDocument = new SyncMLDocument();
    }

    public SyncMLDocument processMessage() {
        processHeader();
        processBody();
        return this.responseDocument;
    }

    private void processHeader() {
        SyncMLHeader sourceHeader = sourceDocument.getHeader();
        SyncMLHeader targetHeader = new SyncMLHeader();
        targetHeader.setMsgID(sourceHeader.getMsgID());
        targetHeader.setHexadecimalSessionId(Integer.toHexString(sourceHeader.getSessionId()));

        TargetTag target = new TargetTag();
        target.setLocURI(sourceHeader.getSource().getLocURI());
        targetHeader.setTarget(target);

        SourceTag source = new SourceTag();
        source.setLocURI(sourceHeader.getTarget().getLocURI());
        targetHeader.setSource(source);

        responseDocument.setHeader(targetHeader);
    }

    private void processBody() {

        SyncMLBody sourceBody = this.sourceDocument.getBody();
        this.responseDocument.setBody(new SyncMLBody());

        // Process status blocks
        if (sourceBody.getStatus() != null) {
            processStatuses();
        }

        // Generate alert status
        processAlert();

        // Process replace blocks
        if (sourceBody.getReplace() != null) {
            processReplaceCommand();
        }
    }

    /**
     * This method processes the status blocks of the message while updating the DM Tree
     */
    private void processStatuses() {
        List<StatusTag> sourceStatuses = sourceDocument.getBody().getStatus();

        // Generate header status
        StatusTag headerStatus = new StatusTag(HEADER_STATUS_COMMAND_ID, sourceDocument.getHeader().getMsgID(),
                HEADER_COMMAND_REF_ID, Constants.SyncMLTags.SYNC_HDR,
                sourceDocument.getHeader().getSource().getLocURI(),
                SyncMLStatusCodes.AUTHENTICATION_ACCEPTED.getCode());

        if (responseDocument.getBody().getStatus() == null) {
            responseDocument.getBody().setStatus(new ArrayList<StatusTag>());
            responseDocument.getBody().getStatus().add(headerStatus);
        } else {
            responseDocument.getBody().getStatus().add(headerStatus);
        }
    }

    private void processReplaceCommand() {
        ReplaceTag replaceTag = sourceDocument.getBody().getReplace();
        int replaceCmdId = replaceTag.getCommandId();
        List<ItemTag> items = replaceTag.getItems();
        List<StatusTag> statuses = new ArrayList<>();
        boolean wholeBlockFlag = false;

        for (ItemTag item : items) {
            String locURI = item.getSource().getLocURI();
            StatusTag status = new StatusTag();

            MgmtTree tree = moDao.getMO(URIParser.getDMTreeName(locURI),
                    sourceDocument.getHeader().getSource().getLocURI());

            // If the tree does not exist
            if (tree == null) {
                status.setData(SyncMLStatusCodes.NOT_FOUND.getCode());
                status.setMessageReference(sourceDocument.getHeader().getMsgID());
                status.setCommandReference(replaceCmdId);
                status.setCommandId(++headerCommandId);
                status.setCommand(Constants.REPLACE);
                statuses.add(status);
                wholeBlockFlag = true;
                break;
            }

            MgmtTreeManager treeManager = new MgmtTreeManagerImpl(tree);

            String statusCode = treeManager.replaceNodeDetails(locURI, item);
            if (!statusCode.equals(SyncMLStatusCodes.SUCCESS.getCode())) {
                wholeBlockFlag = true;
            }
        }

        if (!wholeBlockFlag) {
            StatusTag status = new StatusTag();
            status.setData(SyncMLStatusCodes.SUCCESS.getCode());
            status.setMessageReference(sourceDocument.getHeader().getMsgID());
            status.setCommandReference(replaceCmdId);
            status.setCommandId(++headerCommandId);
            status.setCommand(Constants.REPLACE);
            responseDocument.getBody().getStatus().add(status);
        } else {
            StatusTag status = new StatusTag();
            status.setData(SyncMLStatusCodes.UNSUPPORTED_MEDIA_TYPE.getCode());
            status.setMessageReference(sourceDocument.getHeader().getMsgID());
            status.setCommandReference(replaceCmdId);
            status.setCommandId(++headerCommandId);
            status.setCommand(Constants.REPLACE);
            responseDocument.getBody().getStatus().add(status);
        }
    }

    private void processAlert() {
        StatusTag status = new StatusTag();
        AlertTag alert = sourceDocument.getBody().getAlert();
        status.setCommand(Constants.ALERT);
        status.setCommandId(++headerCommandId);
        status.setCommandReference(alert.getCommandId());
        status.setMessageReference(sourceDocument.getHeader().getMsgID());
        status.setData(SyncMLStatusCodes.SUCCESS.getCode());
        responseDocument.getBody().getStatus().add(status);
    }

    private void processGetCommands() {
        GetTag getCommands = sourceDocument.getBody().getGet();
        List<ItemTag> items = getCommands.getItems();
        List<ItemTag> targetTtems = new ArrayList<>();
        ResultsTag results = new ResultsTag();

        for (ItemTag item : items) {
            ItemTag targetItem = new ItemTag();
            String locURI = item.getTarget().getLocURI();

            MgmtTree tree = moDao.getMO(URIParser.getDMTreeName(locURI),
                    sourceDocument.getHeader().getSource().getLocURI());

            if (tree == null) {
                SourceTag source = new SourceTag();
                source.setLocURI(locURI);
                targetItem.setSource(source);
                targetItem.setData(SyncMLStatusCodes.NOT_FOUND.getCode());
            } else {
                MgmtTreeManager treeManager = new MgmtTreeManagerImpl(tree);
                Node node = treeManager.getNode(locURI);
                SourceTag source = new SourceTag();
                source.setLocURI(locURI);
                targetItem.setSource(source);

                if (node == null) {
                    targetItem.setData(SyncMLStatusCodes.NOT_FOUND.getCode());
                } else {
                    targetItem.setData(node.getValue());
                }
            }
            targetTtems.add(targetItem);
        }
        results.setItems(targetTtems);
        results.setCommandId(++headerCommandId);
        results.setMessageReference(sourceDocument.getHeader().getMsgID());
        results.setCommandReference(sourceDocument.getBody().getGet().getCommandId());

        responseDocument.getBody().setResults(results);
    }

}
