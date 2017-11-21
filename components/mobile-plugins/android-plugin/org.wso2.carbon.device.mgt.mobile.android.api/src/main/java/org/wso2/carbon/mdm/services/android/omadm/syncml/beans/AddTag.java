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
 * Configurations that needs to be added on the device.
 */
public class AddTag {
    int commandId = -1;
    List<ItemTag> items;
    MetaTag meta;

    public int getCommandId() {
        return commandId;
    }

    public void setCommandId(int commandId) {
        this.commandId = commandId;
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

    public MetaTag getMeta() {
        return meta;
    }

    public void setMeta(MetaTag meta) {
        this.meta = meta;
    }

    public void buildAddElement(Document doc, Element rootElement) {
        Element add = doc.createElement(SyncMLConstants.ADD);
        if (getItems() != null) {
            rootElement.appendChild(add);
            if (getCommandId() != -1) {
                Element commandId = doc.createElement(SyncMLConstants.COMMAND_ID);
                commandId.appendChild(doc.createTextNode(String.valueOf(getCommandId())));
                add.appendChild(commandId);
            }
            for (Iterator<ItemTag> itemIterator = getItems().iterator(); itemIterator.hasNext();) {
                ItemTag item = itemIterator.next();
                if (item != null) {
                    item.buildItemElement(doc, add);
                }
            }
        }
        if (getMeta() != null) {
            getMeta().buildMetaElement(doc, add);
        }
    }
}
