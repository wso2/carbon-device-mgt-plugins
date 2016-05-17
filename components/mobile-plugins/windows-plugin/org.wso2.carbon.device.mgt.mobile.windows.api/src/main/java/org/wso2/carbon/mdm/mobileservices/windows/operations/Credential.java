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

/**
 * Credentials passed between the device and the server for security purposes.
 */
@ApiModel(value = "Credential",
        description = "This class carries all information related to syncml credentials")
public class Credential {
    @ApiModelProperty(name = "meta", value = "Syncml credential's MetaTag reference.)", required = true)
    MetaTag meta;
    @ApiModelProperty(name = "data", value = "Data of the Credential Tag.)", required = true)
    String data;

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

    public void buildCredentialElement(Document doc, Element rootElement) {
        Element credentials = doc.createElement(Constants.CREDENTIAL);
        rootElement.appendChild(credentials);
        if (getMeta() != null) {
            getMeta().buildMetaElement(doc, credentials);
        }
        if (getData() != null) {
            Element data = doc.createElement(Constants.DATA);
            data.appendChild(doc.createTextNode(getData()));
            credentials.appendChild(data);
        }
    }
}
