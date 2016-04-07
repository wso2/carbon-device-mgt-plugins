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

package org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.rtproperties;

import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.rtproperties.rtformat.Format;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.rtproperties.rttype.Type;

import javax.xml.bind.annotation.*;

/**
 * This class represents the 'RTProperties' element in a node
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "acl",
        "format",
        "name",
        "size",
        "title",
        "tStamp",
        "type",
        "verNo"
})
@XmlRootElement(name = "RTProperties")
public class RTProperties {

    @XmlElement(name = "ACL")
    protected String acl;
    @XmlElement(name = "Format")
    protected Format format;
    @XmlElement(name = "Name")
    protected String name;
    @XmlElement(name = "Size")
    protected String size;
    @XmlElement(name = "Title")
    protected String title;
    @XmlElement(name = "TStamp")
    protected String tStamp;
    @XmlElement(name = "Type", required = true)
    protected Type type;
    @XmlElement(name = "VerNo")
    protected String verNo;

    public String getAcl() {
        return acl;
    }

    public void setAcl(String acl) {
        this.acl = acl;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String gettStamp() {
        return tStamp;
    }

    public void settStamp(String tStamp) {
        this.tStamp = tStamp;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getVerNo() {
        return verNo;
    }

    public void setVerNo(String verNo) {
        this.verNo = verNo;
    }
}
