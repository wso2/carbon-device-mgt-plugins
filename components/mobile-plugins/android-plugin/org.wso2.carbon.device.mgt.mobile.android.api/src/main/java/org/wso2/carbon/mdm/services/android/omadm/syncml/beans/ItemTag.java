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
 * Represents an items that should be retrieved from the device or a command.
 */
public class ItemTag {

    TargetTag target;
    SourceTag source;
    String data;
    MetaTag meta;

    public MetaTag getMeta() {
        return meta;
    }

    public void setMeta(MetaTag meta) {
        this.meta = meta;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public SourceTag getSource() {
        return source;
    }

    public void setSource(SourceTag source) {
        this.source = source;
    }

    public TargetTag getTarget() {
        return target;
    }

    public void setTarget(TargetTag target) {
        this.target = target;
    }

    public void buildItemElement(Document doc, Element rootElement) {
        Element item = doc.createElement(SyncMLConstants.ITEM);
        rootElement.appendChild(item);

        if (getTarget() != null || getSource() != null) {

            if (getTarget() != null) {
                getTarget().buildTargetElement(doc, item);
            }
            if (getSource() != null) {
                getSource().buildSourceElement(doc, item);
            }
        }
        if (getData() != null) {
            Element data = doc.createElement(SyncMLConstants.DATA);
            data.appendChild(doc.createTextNode(getData()));
            item.appendChild(data);
        }
        if (getMeta() != null) {
            getMeta().buildMetaElement(doc, item);
        }

    }
}
