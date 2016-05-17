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
 * Source details of syncml header's.
 */
@ApiModel(value = "Source",
        description = "This class carries all information related to Syncml source.")
public class Source {
    @ApiModelProperty(name = "LocURI", value = "Location URI.(Source Location:Device)", required = true)
    private String LocURI;
    @ApiModelProperty(name = "LocName", value = "Name of the location", required = true)
    private String LocName;

    public String getLocURI() {
        return LocURI;
    }

    public void setLocURI(String locURI) {
        LocURI = locURI;
    }

    public String getLocName() {
        return LocName;
    }

    public void setLocName(String locName) {
        LocName = locName;
    }

    public void buildSourceElement(Document doc, Element rootElement) {
        Element target = doc.createElement(Constants.SOURCE);
        rootElement.appendChild(target);
        if (getLocURI() != null) {
            Element locURI = doc.createElement(Constants.LOC_URI);
            locURI.appendChild(doc.createTextNode(getLocURI()));
            target.appendChild(locURI);
        }
        if (getLocName() != null) {
            Element locName = doc.createElement(Constants.LOC_NAME);
            locName.appendChild(doc.createTextNode(getLocName()));
            target.appendChild(locName);
        }
    }
}
