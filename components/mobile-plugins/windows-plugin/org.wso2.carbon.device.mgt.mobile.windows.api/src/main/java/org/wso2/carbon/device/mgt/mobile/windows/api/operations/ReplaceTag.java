/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.device.mgt.mobile.windows.api.operations;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.device.mgt.mobile.windows.api.operations.util.Constants;

import java.util.Iterator;
import java.util.List;

/**
 * Commands sent from the device.
 */
public class ReplaceTag {
    int commandId = -1;
    List<ItemTag> items;

    public int getCommandId() {
        return commandId;
    }

    public void setCommandId(int commandId) {
        this.commandId = commandId;
    }

    public List<ItemTag> getItems() {
        return items;
    }

    public void setItems(List<ItemTag> items) {
        this.items = items;
    }

    public void buildReplaceElement(Document doc, Element rootElement) {
        if (getItems() != null) {
            Element replace = doc.createElement(Constants.REPLACE);
            rootElement.appendChild(replace);
            if (getCommandId() != -1) {
                Element commandId = doc.createElement(Constants.COMMAND_ID);
                commandId.appendChild(doc.createTextNode(String.valueOf(getCommandId())));
                replace.appendChild(commandId);
            }
            if (getItems() != null) {
                for (Iterator<ItemTag> itemIterator = getItems().iterator(); itemIterator.hasNext(); ) {
                    ItemTag item = itemIterator.next();
                    if (item != null) {
                        item.buildItemElement(doc, replace);
                    }
                }
            }
        }
    }
}
