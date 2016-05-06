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

package org.wso2.carbon.mdm.services.android.omadm.dm.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mdm.services.android.omadm.dm.processors.SyncMLMessageProcessor;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.SyncMLDocument;
import org.wso2.carbon.mdm.services.android.omadm.syncml.exceptions.SyncMLException;
import org.wso2.carbon.mdm.services.android.omadm.syncml.parsers.SyncMLGenerator;
import org.wso2.carbon.mdm.services.android.omadm.syncml.util.Constants;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The service class for OMA-DM SyncML Message handling
 */
@Path("/manage")
public class OMADMService {

    private static Log log = LogFactory.getLog(OMADMService.class);

    @POST
    @Consumes({Constants.APPLICATION_SYNCML, MediaType.APPLICATION_XML})
    @Produces(MediaType.APPLICATION_XML)
    public Response processRequest(SyncMLDocument syncMLDoc) {

        SyncMLGenerator syncGenerator = new SyncMLGenerator();
        SyncMLMessageProcessor syncMsgProcessor = new SyncMLMessageProcessor(syncMLDoc);
        SyncMLDocument responseObj;

        if (syncMLDoc == null) {
            String msg = "Error occurred while parsing the SyncML request.";
            log.error(msg);
            throw new SyncMLException(msg);
        }

        responseObj = syncMsgProcessor.processMessage();
        String responseStr = syncGenerator.generatePayload(responseObj);
        return Response.status(Response.Status.OK).entity(responseStr).build();
    }

}
