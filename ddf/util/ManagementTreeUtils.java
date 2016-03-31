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

package org.wso2.carbon.mdm.services.android.omadm.ddf.util;

import org.wso2.carbon.mdm.services.android.omadm.ddf.ManagementTree;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.Node;

import java.util.Map;

/**
 * This class includes various utilities for Management Trees
 */
public class ManagementTreeUtils {

    /**
     * Prints all the node names with their paths
     * @param mgmtTree Management Tree object
     */
    public static void printTree(ManagementTree mgmtTree) {
        printNodes(mgmtTree.getChildNodes());
    }

    /**
     * Prints node names and their absolute paths of a givel node list
     * @param treeNodes Node list
     */
    public static void printNodes(Map<String, Node> treeNodes) {
        Node[] nodes = treeNodes.values().toArray((new Node[0]));
        String tempStr = "";
        for (Node node : nodes) {
            System.out.println(node.getPath());
            node.setPath(tempStr);
            if (node.getSubNodes() != null) {
                printNodes(node.getSubNodes());
            }
        }
    }

}
