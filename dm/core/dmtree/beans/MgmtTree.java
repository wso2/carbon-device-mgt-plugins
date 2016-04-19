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

package org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans;

import javax.xml.bind.annotation.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the OMA Device Management Tree
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "verDTD",
        "man",
        "mod",
        "nodes"
})
@XmlRootElement(name = "MgmtTree")
public class MgmtTree {

    @XmlElement(name = "VerDTD", required = true)
    protected String verDTD;
    @XmlElement(name = "Man")
    protected String man;
    @XmlElement(name = "Mod")
    protected String mod;
    @XmlElement(name = "Node")
    protected List<Node> nodes = new ArrayList<>();

    // Additional Properties
    @XmlTransient
    private URL publicPath;
    @XmlTransient
    private String fileName;
    @XmlTransient
    private String type;

    // A map to keep track of connected nodes. Used for search operations
    @XmlTransient
    Map<String, Node> treeMap = new HashMap<>();

    public MgmtTree() {
    }

    public MgmtTree(String verDTD, String man, String mod, List<Node> nodes) {
        this.verDTD = verDTD;
        this.man = man;
        this.mod = mod;
        this.nodes = nodes;
    }

    public MgmtTree(String verDTD, String man, String mod) {
        this.verDTD = verDTD;
        this.man = man;
        this.mod = mod;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Map<String, Node> getTreeMap() {
        return treeMap;
    }

    public String getVerDTD() {
        return verDTD;
    }

    public void setVerDTD(String verDTD) {
        this.verDTD = verDTD;
    }

    public String getMan() {
        return man;
    }

    public void setMan(String man) {
        this.man = man;
    }

    public String getMod() {
        return mod;
    }

    public void setMod(String mod) {
        this.mod = mod;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public URL getPublicPath() {
        return publicPath;
    }

    public void setPublicPath(URL publicPath) {
        this.publicPath = publicPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
