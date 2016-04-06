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

package org.wso2.carbon.mdm.services.android.omadm.ddf;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.Node;
import org.wso2.carbon.mdm.services.android.omadm.ddf.constants.DMTreeConstants;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the OMA Device Management Tree
 */
public class ManagementTree {

    private static String NODE_URI_DELIMETER = "/";
    private static String ROOT_NODE_DELIMETER = ".";

    // OMADM Defined Properties
    private String name;
    private String verDTD = DMTreeConstants.VERDTD_DEFAULT_VALUE;
    private String man;
    private String mod;
    private Map<String, Node> childNodes = new HashMap<>();

    // Additional Properties
    private URL publicPath;
    private String fileName;
    private String type;

    public ManagementTree() {}

    public ManagementTree(String name, String man, String mod, String verDTD, Node[] childNodes) {
        this.name = name;
        this.man = man;
        this.mod = mod;
        this.verDTD = verDTD;
        if (childNodes != null) {
            setChildNodes(childNodes);
            generatePaths(this.childNodes, this.name);
        }
    }

    public ManagementTree(String name, String man, String mod, String verDTD, ArrayList<Node> childNodes) {
        this.name = name;
        this.man = man;
        this.mod = mod;
        this.verDTD = verDTD;
        if (childNodes != null) {
            setChildNodes(childNodes);
            generatePaths(this.childNodes, this.name);
        }
    }

    public void setChildNodes(ArrayList<Node> nodes) {
        for (Node node : nodes) {
            this.childNodes.put(node.getName(), node);
        }
    }

    public void setChildNodes(Node[] nodes) {
        for (Node node : nodes) {
            this.childNodes.put(node.getName(), node);
        }
    }

    /**
     * This recursive method generates absolute paths of each node in the given Node list
     * @param treeNodes node list
     * @param currParentURI URI of the parent node
     */
    public void generatePaths(Map<String, Node> treeNodes, String currParentURI) {
        Node[] nodes = treeNodes.values().toArray((new Node[0]));
        String tempStr;
        for (Node node : nodes) {
            tempStr = currParentURI + NODE_URI_DELIMETER + node.getName();
            node.setPath(currParentURI);
            if (node.getSubNodes() != null) {
                generatePaths(node.getSubNodes(), tempStr);
            }
        }
    }

    public boolean addNode(Node node) {
        if (node != null) {
            childNodes.put(node.getName(), node);
            return true;
        }
        return false;
    }

    public int getChildNodeCount () {
        return this.childNodes.size();
    }

    public Map<String, Node> getChildNodes() {
        return childNodes;
    }

    public String toString()
    {
        ToStringBuilder localToStringBuilder = new ToStringBuilder(this);
        localToStringBuilder.append("childNodes", this.childNodes);
        localToStringBuilder.append("VerDTD", this.verDTD);
        localToStringBuilder.append("man", this.man);
        localToStringBuilder.append("mod", this.mod);
        return localToStringBuilder.toString();
    }
}
