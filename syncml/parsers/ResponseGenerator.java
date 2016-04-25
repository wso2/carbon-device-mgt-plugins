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

package org.wso2.carbon.mdm.services.android.omadm.syncml.parsers;

import com.google.gson.Gson;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.*;
import org.wso2.carbon.mdm.services.android.omadm.syncml.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates the SyncML Response according to the request
 */
public class ResponseGenerator {

    private SyncMLDocument syncMLDocument;
    private SyncMLDocument responseSyncMLDocument;
    private int headerCommandId = 1;
    private static final int HEADER_STATUS_ID = 0;
    private static final String RESULTS_COMMAND_TEXT = "Results";
    private static final String HEADER_COMMAND_TEXT = "SyncHdr";
    private static final String ALERT_COMMAND_TEXT = "Alert";
    private static final String REPLACE_COMMAND_TEXT = "Replace";
    private static final String GET_COMMAND_TEXT = "Get";
    private static final String EXEC_COMMAND_TEXT = "Exec";
    private List<? extends Operation> operations;
    Gson gson = new Gson();

    public ResponseGenerator(SyncMLDocument syncMLDocument) {
        this.syncMLDocument = syncMLDocument;
        responseSyncMLDocument = new SyncMLDocument();
    }

    public SyncMLBody generateStatuses() {
        SyncMLBody sourceSyncMLBody = syncMLDocument.getBody();
        SyncMLHeader sourceHeader = syncMLDocument.getHeader();
        StatusTag headerStatus;
        SyncMLBody syncMLBodyReply = new SyncMLBody();
        List<StatusTag> statuses = new ArrayList<>();
        List<StatusTag> sourceStatuses = sourceSyncMLBody.getStatus();
        if (sourceStatuses.isEmpty()) {
            headerStatus =
                    new StatusTag(headerCommandId, sourceHeader.getMsgID(), HEADER_STATUS_ID,
                            HEADER_COMMAND_TEXT, sourceHeader.getSource().getLocURI(),
                            String.valueOf(Constants.SyncMLResponseCodes.AUTHENTICATION_ACCEPTED));
            statuses.add(headerStatus);
        } else {
            for (StatusTag sourceStatus : sourceStatuses) {
                if (sourceStatus.getChallenge() != null && HEADER_COMMAND_TEXT.equals(sourceStatus.getCommand())) {

                    headerStatus =
                            new StatusTag(headerCommandId, sourceHeader.getMsgID(), HEADER_STATUS_ID,
                                    HEADER_COMMAND_TEXT, sourceHeader.getSource().getLocURI(),
                                    String.valueOf(Constants.SyncMLResponseCodes.AUTHENTICATION_ACCEPTED));
                    statuses.add(headerStatus);
                }
            }
        }
        if (sourceSyncMLBody.getResults() != null) {
            int ResultCommandId = ++headerCommandId;
            StatusTag resultStatus = new StatusTag(ResultCommandId, sourceHeader.getMsgID(),
                    sourceSyncMLBody.getResults().getCommandId(), RESULTS_COMMAND_TEXT, null,
                    String.valueOf(Constants.SyncMLResponseCodes.ACCEPTED));
            statuses.add(resultStatus);
        }
        if (sourceSyncMLBody.getAlert() != null) {
            int alertCommandId = ++headerCommandId;
            StatusTag alertStatus = new StatusTag(alertCommandId,
                    sourceHeader.getMsgID(),
                    sourceSyncMLBody.getAlert().getCommandId(),
                    ALERT_COMMAND_TEXT, null,
                    String.valueOf(Constants.SyncMLResponseCodes.ACCEPTED));
            statuses.add(alertStatus);
        }
        if (sourceSyncMLBody.getReplace() != null) {
            int replaceCommandId = ++headerCommandId;
            StatusTag replaceStatus = new StatusTag(replaceCommandId, sourceHeader.getMsgID(),
                    sourceSyncMLBody.getReplace().getCommandId(), REPLACE_COMMAND_TEXT, null,
                    String.valueOf(Constants.SyncMLResponseCodes.ACCEPTED)
            );
            statuses.add(replaceStatus);
        }
        if (sourceSyncMLBody.getExec() != null) {
            List<ExecuteTag> Executes = sourceSyncMLBody.getExec();
            for (ExecuteTag exec : Executes) {
                int execCommandId = ++headerCommandId;
                StatusTag execStatus = new StatusTag(execCommandId, sourceHeader.getMsgID(),
                        exec.getCommandId(), EXEC_COMMAND_TEXT, null, String.valueOf(
                        Constants.SyncMLResponseCodes.ACCEPTED));
                statuses.add(execStatus);
            }
        }
        if (sourceSyncMLBody.getGet() != null) {
            int getCommandId = ++headerCommandId;
            StatusTag execStatus = new StatusTag(getCommandId, sourceHeader.getMsgID(), sourceSyncMLBody
                    .getGet().getCommandId(), GET_COMMAND_TEXT, null, String.valueOf(
                    Constants.SyncMLResponseCodes.ACCEPTED));
            statuses.add(execStatus);
        }
        syncMLBodyReply.setStatus(statuses);
        return syncMLBodyReply;
    }

}
