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

/**
 * Inform an event occurred from device to server.
 */
public class AlertTag {

    int commandId = -1;
    String data;

    public int getCommandId() {
        return commandId;
    }

    public void setCommandId(int commandId) {
        this.commandId = commandId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void buildAlertElement(Document doc, Element rootElement) {
        Element alert = doc.createElement(SyncMLConstants.ALERT);
        rootElement.appendChild(alert);
        if (getCommandId() != -1) {
            Element commandId = doc.createElement(SyncMLConstants.COMMAND_ID);
            commandId.appendChild(doc.createTextNode(String.valueOf(getCommandId())));
            alert.appendChild(commandId);
        }
        if (getData() != null) {
            Element data = doc.createElement(SyncMLConstants.DATA);
            data.appendChild(doc.createTextNode(getData()));
            alert.appendChild(data);
        }
    }
}
