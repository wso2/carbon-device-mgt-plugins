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

package org.wso2.carbon.mdm.services.android.omadm.dmtree.beans;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.wso2.carbon.mdm.services.android.omadm.dmtree.beans.constants.RTPropertyFormat;
import org.wso2.carbon.mdm.services.android.omadm.dmtree.parsers.ACLParser;

import java.io.Serializable;


/**
 * Runtime Properties of a DMNode
 */
public class RTProperty implements Serializable {

    private static final String TYPE_DEFAULT_VALUE = "text/plain";

    private String acl;
    private RTPropertyFormat format;
    private String name;
    private long size;
    private String title;
    private String tStamp;
    private String verNo;
    private String type = TYPE_DEFAULT_VALUE;

    public RTProperty() {}

    public RTProperty(RTPropertyFormat format, String acl, String name, long size, String title, String tStamp, String verNo, String type) {
        this.format = format;
        this.acl = acl;
        this.name = name;
        this.size = size;
        this.title = title;
        this.tStamp = tStamp;
        this.verNo = verNo;
        this.type = type;
    }

    public String getAcl() {
        return acl;
    }

    public void setAcl(String acl) {
        if (ACLParser.validateACL(acl)) {
            this.acl = acl;
        }
    }

    public RTPropertyFormat getFormat() {
        return format;
    }

    public void setFormat(RTPropertyFormat format) {
        this.format = format;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
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

    public String getVerNo() {
        return verNo;
    }

    public void setVerNo(String verNo) {
        this.verNo = verNo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString()
    {
        ToStringBuilder localToStringBuilder = new ToStringBuilder(this);
        localToStringBuilder.append("acl", this.acl);
        localToStringBuilder.append("format", this.format.getCode());
        localToStringBuilder.append("name", this.name);
        localToStringBuilder.append("size", this.size);
        localToStringBuilder.append("title", this.title);
        localToStringBuilder.append("tStamp", this.tStamp);
        localToStringBuilder.append("type", this.type);
        localToStringBuilder.append("verNo", this.verNo);
        return localToStringBuilder.toString();
    }


}
