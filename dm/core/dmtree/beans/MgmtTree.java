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

import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.exceptions.DMNodeException;

import javax.xml.bind.annotation.*;
import java.net.URL;
import java.util.ArrayList;
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

    private static String NODE_URI_DELIMETER = "/";
    private static String ROOT_NODE_PATH_DELIMETER = ".";

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

    public boolean addNodes(Node [] nodes) {
        for (Node node : nodes) {
            if (this.nodes.contains(node) && node != null) {
                throw new DMNodeException("A node with the same node name already exists.");
            }
            this.nodes.add(node);
            updatePaths(this.nodes, ROOT_NODE_PATH_DELIMETER);
            return true;
        }
        return false;
    }

    public boolean addNodes(ArrayList<Node> nodes) {
        for (Node node : nodes) {
            if (this.nodes.contains(node) && node != null) {
                throw new DMNodeException("A node with the same node name already exists.");
            }
            this.nodes.add(node);
            updatePaths(this.nodes, ROOT_NODE_PATH_DELIMETER);
            return true;
        }
        return false;
    }

    /**
     * This recursive method generates absolute paths of each node in the given Node list
     *
     * @param treeNodes     node list
     * @param currParentURI URI of the parent node
     */
    public void updatePaths(List<Node> treeNodes, String currParentURI) {
        String tempStr;
        for (Node node : treeNodes) {
            tempStr = currParentURI + NODE_URI_DELIMETER + node.getNodeName();
            node.setPath(currParentURI);
            if (node.getNodes() != null) {
                System.out.println(node.getNodeName() + " : " + node.getPath());
                updatePaths(node.getNodes(), tempStr);
            }
        }
    }
}
