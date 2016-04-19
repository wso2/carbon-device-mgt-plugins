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

package org.wso2.carbon.mdm.services.android.omadm.ddf.impl;

import org.wso2.carbon.mdm.services.android.omadm.ddf.MgmtTreeManager;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.MgmtTree;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.Node;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.exceptions.DMException;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.exceptions.DMNodeException;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.parsers.URIParser;

import java.util.List;

/**
 * The implementation of the MgmtTreeManager
 */
public class MgmtTreeManagerImpl implements MgmtTreeManager {

    private static String NODE_URI_DELIMETER = "/";
    private static String ROOT_NODE_PATH_DELIMETER = ".";

    private MgmtTree tree;

    public MgmtTreeManagerImpl(MgmtTree tree) {
        this.tree = tree;
    }

    @Override
    public boolean addNode(Node node, String path) {
        URIParser.validateUri(path);
        String absolutePath = path + NODE_URI_DELIMETER + node.getNodeName();
        if (isExistingNode(absolutePath)) {
            throw new DMNodeException("A node already exists in the given path.");
        }
        if (node == null) {
            throw new DMException("Node cannot be null.");
        } else {
            node.setPath(path);
            this.tree.getTreeMap().get(path).addNode(node);
            this.tree.getTreeMap().put(absolutePath,node);
            return true;
        }
    }

    @Override
    public boolean removeNode(String path) {
        URIParser.validateUri(path);
        if (!isExistingNode(path)) {
            throw new DMNodeException("No such node exists.");
        } else {
            Node parentNode = this.tree.getTreeMap().get(URIParser.getParentPath(path));
            parentNode.removeNode(URIParser.getNodeName(path));
            return true;
        }
    }

    @Override
    public Node getNode(String path) {
        if (isExistingNode(path)) {
            return this.tree.getTreeMap().get(path);
        } else {
            throw new DMNodeException("Node doesn't exist.");
        }
    }

    @Override
    public boolean replaceNodeValue(String path, String value) {
        return false;
    }

    @Override
    public int nodeCount() {
        return this.tree.getTreeMap().size();
    }

    @Override
    public boolean isExistingNode(String path) {
        if (this.tree.getTreeMap().containsKey(path)) {
            return true;
        }
        return false;
    }

    /**
     * This recursive method generates absolute paths of each node in a given Node list
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
