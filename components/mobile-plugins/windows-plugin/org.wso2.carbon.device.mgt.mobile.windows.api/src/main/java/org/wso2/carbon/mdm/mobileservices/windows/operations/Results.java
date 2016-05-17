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

package org.wso2.carbon.mdm.mobileservices.windows.operations;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.mdm.mobileservices.windows.operations.util.Constants;

import java.util.Iterator;
import java.util.List;

/**
 * Results sent for the requests made to the device.
 */
@ApiModel(value = "Results",
        description = "This class carries all information related to Syncml Item.")
public class Results {
    @ApiModelProperty(name = "commandId", value = "CommandID of the Syncml Results Tag.", required = true)
    int commandId = -1;
    @ApiModelProperty(name = "messageReference", value = "MessageReference of the Syncml Results Tag.", required = true)
    int messageReference = -1;
    @ApiModelProperty(name = "commandReference", value = "CommandReference of the Syncml Results Tag.", required = true)
    int commandReference = -1;
    @ApiModelProperty(name = "item", value = "List of Items in Syncml ResultTag.", required = true)
    List<Item> item;

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

    public List<Item> getItem() {
        return item;
    }

    public void setItem(List<Item> item) {
        this.item = item;
    }

    public void buildResultElement(Document doc, Element rootElement) {
        Element results = doc.createElement(Constants.RESULTS);
        rootElement.appendChild(results);
        if (getCommandId() != -1) {
            Element commandId = doc.createElement(Constants.COMMAND_ID);
            commandId.appendChild(doc.createTextNode(String.valueOf(getCommandId())));
            results.appendChild(commandId);
        }
        if (getMessageReference() != -1) {
            Element messageReference = doc.createElement(Constants.MESSAGE_REFERENCE);
            messageReference.appendChild(doc.createTextNode(String.valueOf(getMessageReference())));
            results.appendChild(messageReference);
        }
        if (getCommandReference() != -1) {
            Element messageReference = doc.createElement(Constants.COMMAND_REFERENCE);
            messageReference.appendChild(doc.createTextNode(String.valueOf(getCommandReference())));
            results.appendChild(messageReference);
        }
        if (getItem() != null) {
            for (Iterator<Item> itemIterator = getItem().iterator(); itemIterator.hasNext(); ) {
                Item item = itemIterator.next();
                if (item != null) {
                    item.buildItemElement(doc, results);
                }
            }
        }
    }
}
