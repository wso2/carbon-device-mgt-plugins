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
 * Represents an items that should be retrieved from the device or a command.
 */
@ApiModel(value = "Item",
        description = "This class carries all information related to Syncml Item.")
public class Item {
    @ApiModelProperty(name = "target", value = "Target reference value of the Syncml Message(ex:Device).", required = true)
    Target target;
    @ApiModelProperty(name = "source", value = "Source reference of the syncml message.(Ex:EMM Server).", required = true)
    Source source;
    @ApiModelProperty(name = "data", value = "Content of the syncml message.", required = true)
    String data;
    @ApiModelProperty(name = "meta", value = "Meta data of the syncml messsage.", required = true)
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

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public void buildItemElement(Document doc, Element rootElement) {
        Element item = doc.createElement(Constants.ITEM);
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
            Element data = doc.createElement(Constants.DATA);
            data.appendChild(doc.createTextNode(getData()));
            item.appendChild(data);
        }
        if (getMeta() != null) {
            getMeta().buildMetaElement(doc, item);
        }

    }
}
