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

import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.MgmtTree;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.Node;

import java.util.ArrayList;

/**
 * The interface for Management Tree Manager
 */
public interface MgmtTreeManager {

    /**
     * Adds a node to a given path
     *
     * @param tree The Management Tree
     * @param node The Node to be added
     * @param path The absolute path of the parent node
     * @return The status of the adding operation
     */
    boolean addNode(MgmtTree tree, Node node, String path);

    /**
     * Adds an array of nodes to the root of the tree
     *
     * @param tree  The Management Tree
     * @param nodes The array of nodes to be added
     * @return The status of the adding operation
     */
    boolean addNodes(MgmtTree tree, Node[] nodes);

    /**
     * Adds a list of nodes to the root of the tree
     *
     * @param tree  The Management Tree
     * @param nodes The node list to be added
     * @return The status of the adding operation
     */
    boolean addNodes(MgmtTree tree, ArrayList<Node> nodes);

    /**
     * Removes a node in a given path
     *
     * @param tree The Management Tree
     * @param path The absolute path of the node to be removed
     * @return Status of the remove operation
     */
    boolean removeNode(MgmtTree tree, String path);

    /**
     * Gets a node in a given path
     *
     * @param tree The management tree
     * @param path The absolute path of the node to be retrieved
     * @return The requested node
     */
    Node getNode(MgmtTree tree, String path);

    /**
     * Replaces data of a given node
     *
     * @param tree  The management tree
     * @param path  The absolute path of the node
     * @param value New data
     * @return The status of the replace operation
     */
    boolean replaceNodeValue(MgmtTree tree, String path, String value);

    /**
     * Counts the number of attached nodes in the tree
     *
     * @param tree The Management Tree
     * @return Number of nodes attached
     */
    int nodeCount(MgmtTree tree);

}
