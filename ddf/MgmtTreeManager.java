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
     * @param node The Node to be added
     * @param path The absolute path of the parent node
     * @return The status of the adding operation
     */
    boolean addNode(Node node, String path);

    /**
     * Removes a node in a given path
     *
     * @param path The absolute path of the node to be removed
     * @return Status of the remove operation
     */
    boolean removeNode(String path);

    /**
     * Gets a node in a given path by traversing through each
     * and every node of the given tree
     *
     * @param path The absolute path of the node to be retrieved
     * @return The requested node
     */
    Node getNode(String path);

    /**
     * Replaces data of a given node
     *
     * @param path  The absolute path of the node
     * @param value New data
     * @return The status of the replace operation
     */
    boolean replaceNodeValue(String path, String value);

    /**
     * Counts the number of attached nodes in the tree
     *
     * @return Number of nodes attached
     */
    int nodeCount();

    /**
     * Checks for a node's existence in the Management Tree
     *
     * @param path Path of the node
     * @return True if exists or else false
     */
    boolean isExistingNode(String path);

}
