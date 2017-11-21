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

package org.wso2.carbon.mdm.services.android.omadm.syncml.beans;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.mdm.services.android.omadm.syncml.util.SyncMLConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Status of a previously sent message to device;
 */
public class StatusTag {
    int commandId = -1;
    int messageReference = -1;
    int commandReference = -1;
    String command;
    String targetReference;
    String data;
    List<ItemTag> items;
    ChallengeTag challenge;

    public StatusTag(int commandId, int messageReference, int commandReference, String command,
                     String targetReference, String data) {
        this.commandId = commandId;
        this.messageReference = messageReference;
        this.commandReference = commandReference;
        this.command = command;
        this.targetReference = targetReference;
        this.data = data;
    }

    public StatusTag() {
    }

    public ChallengeTag getChallenge() {
        return challenge;
    }

    public void setChallenge(ChallengeTag challenge) {
        this.challenge = challenge;
    }

    public String getTargetReference() {
        return targetReference;
    }

    public void setTargetReference(String targetReference) {
        this.targetReference = targetReference;
    }

    public int getCommandId() {
        return commandId;
    }

    public void setCommandId(int commandId) {
        this.commandId = commandId;
    }

    public int getMessageReference() {
        return messageReference;
    }

    public void setMessageReference(int messageReference) {
        this.messageReference = messageReference;
    }

    public int getCommandReference() {
        return commandReference;
    }

    public void setCommandReference(int commandReference) {
        this.commandReference = commandReference;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<ItemTag> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public void setItems(List<ItemTag> items) {
        this.items = items;
    }

    public void buildStatusElement(Document doc, Element rootElement) {
        Element status = doc.createElement(SyncMLConstants.STATUS);
        rootElement.appendChild(status);
        if (getCommandId() != -1) {
            Element commandId = doc.createElement(SyncMLConstants.COMMAND_ID);
            commandId.appendChild(doc.createTextNode(String.valueOf(getCommandId())));
            status.appendChild(commandId);
        }
        if (getMessageReference() != -1) {
            Element msgReference = doc.createElement(SyncMLConstants.MESSAGE_REFERENCE);
            msgReference.appendChild(doc.createTextNode(String.valueOf(getMessageReference())));
            status.appendChild(msgReference);
        }
        if (getCommandReference() != -1) {
            Element commandReference = doc.createElement(SyncMLConstants.COMMAND_REFERENCE);
            commandReference.appendChild(doc.createTextNode(String.valueOf(getCommandReference())));
            status.appendChild(commandReference);
        }
        if (getCommand() != null) {
            Element command = doc.createElement(SyncMLConstants.COMMAND);
            command.appendChild(doc.createTextNode(getCommand()));
            status.appendChild(command);
        }
        if (getTargetReference() != null) {
            Element targetReference = doc.createElement(SyncMLConstants.TARGET_REFERENCE);
            targetReference.appendChild(doc.createTextNode(getTargetReference()));
            status.appendChild(targetReference);
        }
        if (getChallenge() != null) {
            getChallenge().buildChallengeElement(doc, status);
        }
        if (getData() != null) {
            Element data = doc.createElement(SyncMLConstants.DATA);
            data.appendChild(doc.createTextNode(getData()));
            status.appendChild(data);
        }
        if (getItems() != null) {
            for (Iterator<ItemTag> itemIterator = getItems().iterator(); itemIterator.hasNext(); ) {
                ItemTag item = itemIterator.next();
                if (item != null) {
                    item.buildItemElement(doc, status);
                }
            }
        }
    }

}
