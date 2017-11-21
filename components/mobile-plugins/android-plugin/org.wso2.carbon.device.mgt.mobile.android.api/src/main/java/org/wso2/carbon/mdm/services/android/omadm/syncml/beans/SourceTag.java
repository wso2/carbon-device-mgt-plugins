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
 * Source details of syncml header's.
 */
public class SourceTag {

    private String locURI;
    private String locName;

    public String getLocURI() {
        return locURI;
    }

    public void setLocURI(String locURI) {
        this.locURI = locURI;
    }

    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public void buildSourceElement(Document doc, Element rootElement) {
        Element target = doc.createElement(SyncMLConstants.SOURCE);
        rootElement.appendChild(target);
        if (getLocURI() != null) {
            Element locURI = doc.createElement(SyncMLConstants.LOC_URI);
            locURI.appendChild(doc.createTextNode(getLocURI()));
            target.appendChild(locURI);
        }
        if (getLocName() != null) {
            Element locName = doc.createElement(SyncMLConstants.LOC_NAME);
            locName.appendChild(doc.createTextNode(getLocName()));
            target.appendChild(locName);
        }
    }
}
