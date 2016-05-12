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
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.exceptions.DMNodeException;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.exceptions.DMTreeOperationException;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.parsers.URIParser;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.ItemTag;
import org.wso2.carbon.mdm.services.android.omadm.syncml.util.SyncMLStatusCodes;

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
    public String addNode(Node node, String path) throws DMTreeOperationException {
        // Note that the path here is the path of the parent node
        URIParser.validateUri(path);
        String absolutePath = URIParser.getPath(path);
        node.setPath(absolutePath);
        getParentNode(node).getNodes().add(node);
        return SyncMLStatusCodes.SUCCESS.getCode();
    }

    @Override
    public String removeNode(String path) throws DMTreeOperationException {
        Node node = getNode(path);

        if (node.getDfProperties().getAccessType().getDelete() != null) {
            getNode(URIParser.getParentPath(path)).getNodes().remove(node);
            return SyncMLStatusCodes.SUCCESS.getCode();
        } else {
            return SyncMLStatusCodes.NOT_MODIFIED.getCode();
        }
    }

    @Override
    public Node getNode(String path) throws DMNodeException {
        Node node;
        //Validate the URI
        URIParser.validateUri(path);

        String[] pathArr = URIParser.nodeNames(path);
        node = searchNode(pathArr, tree.getNodes(), 0);
        return node;
    }

    @Override
    public String replaceNodeDetails(String path, ItemTag item) {
        Node node = getNode(path);
        if (node == null) {
            return SyncMLStatusCodes.NOT_FOUND.getCode();
        }
        // TODO: Need to check for ACL permissions

        if (item.getMeta() != null) {
            if (item.getMeta().getFormat() != null) {
                node.setFormat(item.getMeta().getFormat());
            }
        }

        if (item.getMeta() != null) {
            if (item.getMeta().getType() != null) {
                node.setType(item.getMeta().getType());
            }
        }

        if (item.getMeta() != null) {
            if (item.getData() != null) {
                node.setValue(item.getData());
            }
        }

        return SyncMLStatusCodes.SUCCESS.getCode();
    }

    @Override
    public int nodeCount() {
        return this.tree.getNodes().size();
    }

    @Override
    public boolean isExistingNode(String path) {
        if (getNode(path) != null) {
            return true;
        }
        return false;
    }

    public Node getParentNode(Node node) {
        return getNode(node.getPath());
    }

    /**
     * This is a recursive method which traverses through a nested node structure
     *
     * @param pathArr The path segments to the node as an array of node names
     * @param nodes   The node list to be traversed
     * @param index   Current search position of the node list
     * @return Requested node if found or else null
     */
    private Node searchNode(String[] pathArr, List<Node> nodes, int index) {
        int N = index;
        Node targetNode = null;
        for (Node node : nodes) {
            if (node.getNodeName().equalsIgnoreCase(pathArr[N + 1])) {
                if (N + 2 == pathArr.length) {
                    targetNode = node;
                } else {
                    targetNode = searchNode(pathArr, node.getNodes(), ++N);
                }
            }
        }
        return targetNode;
    }

    /**
     * This recursive method generates absolute paths of each node in a given Node list
     *
     * @param treeNodes     Node list
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
