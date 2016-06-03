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

package org.wso2.carbon.mdm.services.android.omadm.ddf.standardmos;

import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.MgmtTree;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.Node;

import java.util.HashMap;
import java.util.List;

/**
 * This class acts as an entity which holds all the Management Trees of a device.
 * The need of this type of an entity is mainly to keep track of the currently
 * initiated management trees.
 */
public class DMRoot {

    // Holds references to Management Trees
    private static HashMap<String, MgmtTree> treeMap;
    // Holds references to Management Nodes
    private static HashMap<String, Node> nodeMap;

    private static String NODE_URI_DELIMETER = "/";

    public DMRoot() {
        treeMap = new HashMap<>();
        nodeMap = new HashMap<>();
    }

    public void addMgmtTree(MgmtTree tree) {
        if (tree != null) {
            this.treeMap.put(tree.getName(), tree);
            for (Node node : tree.getNodes()) {
                String absolutePath = node.getPath() + NODE_URI_DELIMETER + node.getNodeName();
                nodeMap.put(absolutePath, node);
            }
        }
    }

    public void addNode(Node node) {
        String absolutePath = node.getPath() + NODE_URI_DELIMETER + node.getNodeName();
        nodeMap.put(absolutePath, node);
    }

    public void addNodes(List<Node> nodes) {
        if (nodes != null) {
            for (Node node : nodes) {
                String absolutePath = node.getPath() + NODE_URI_DELIMETER + node.getNodeName();
                nodeMap.put(absolutePath, node);
            }
        }
    }

    public static HashMap<String, MgmtTree> getTreeMap() {
        return treeMap;
    }

    public static HashMap<String, Node> getNodeMap() {
        return nodeMap;
    }

}
