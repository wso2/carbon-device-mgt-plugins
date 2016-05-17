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

/**
 * Represents a base format of a syncml document
 */
@ApiModel(value = "SyncmlDocument", description = "This class carries all information related to SyncmlDocument.")
public class SyncmlDocument {
    @ApiModelProperty(name = "header", value = "Header of the syncmlDocument.", required = true)
    SyncmlHeader header;
    @ApiModelProperty(name = "body", value = "Body of the SyncmlDocument.", required = true)
    SyncmlBody body;

    public SyncmlHeader getHeader() {
        return header;
    }

    public void setHeader(SyncmlHeader header) {
        this.header = header;
    }

    public SyncmlBody getBody() {
        return body;
    }

    public void setBody(SyncmlBody body) {
        this.body = body;
    }

    public void buildDocument(Document doc, Element rootElement) {
        if (getHeader() != null) {
            getHeader().buildSyncmlHeaderElement(doc, rootElement);
        }
        if (getBody() != null) {
            getBody().buildBodyElement(doc, rootElement);
        }
    }
}
