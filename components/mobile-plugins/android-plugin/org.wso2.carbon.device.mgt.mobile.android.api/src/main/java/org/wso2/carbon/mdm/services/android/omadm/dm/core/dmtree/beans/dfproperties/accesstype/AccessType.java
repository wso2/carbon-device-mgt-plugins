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

package org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.dfproperties.accesstype;

import javax.xml.bind.annotation.*;

/**
 * The 'AccessType' property of the 'DFProperties' property
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "add",
        "copy",
        "delete",
        "exec",
        "get",
        "replace"
})
@XmlRootElement(name = "AccessType")
public class AccessType {

    @XmlElement(name = "Add")
    protected Add add;
    @XmlElement(name = "Copy")
    protected Copy copy;
    @XmlElement(name = "Delete")
    protected Delete delete;
    @XmlElement(name = "Exec")
    protected Exec exec;
    @XmlElement(name = "Get")
    protected Get get;
    @XmlElement(name = "Replace")
    protected Replace replace;

    public Add getAdd() {
        return add;
    }

    public void setAdd(Add add) {
        this.add = add;
    }

    public Copy getCopy() {
        return copy;
    }

    public void setCopy(Copy copy) {
        this.copy = copy;
    }

    public Delete getDelete() {
        return delete;
    }

    public void setDelete(Delete delete) {
        this.delete = delete;
    }

    public Exec getExec() {
        return exec;
    }

    public void setExec(Exec exec) {
        this.exec = exec;
    }

    public Get getGet() {
        return get;
    }

    public void setGet(Get get) {
        this.get = get;
    }

    public Replace getReplace() {
        return replace;
    }

    public void setReplace(Replace replace) {
        this.replace = replace;
    }
}
