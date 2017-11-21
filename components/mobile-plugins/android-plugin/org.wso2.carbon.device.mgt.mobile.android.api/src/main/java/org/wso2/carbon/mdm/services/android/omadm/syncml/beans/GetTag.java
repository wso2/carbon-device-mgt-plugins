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

import java.util.Iterator;
import java.util.List;

/**
 * Data that needs to be retrieved from the device.
 */
public class GetTag {
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

    public void buildGetElement(Document doc, Element rootElement) {
        if (getItems() != null) {
            Element get = doc.createElement(SyncMLConstants.GET);
            rootElement.appendChild(get);
            if (getCommandId() != -1) {
                Element commandId = doc.createElement(SyncMLConstants.COMMAND_ID);
                commandId.appendChild(doc.createTextNode(String.valueOf(getCommandId())));
                get.appendChild(commandId);
            }
            if (getItems() != null) {
                for (Iterator<ItemTag> itemIterator = getItems().iterator(); itemIterator.hasNext(); ) {
                    ItemTag item = itemIterator.next();
                    if (item != null) {
                        item.buildItemElement(doc, get);
                    }
                }
            }
        }
    }

}
