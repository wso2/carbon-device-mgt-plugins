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

package org.wso2.carbon.mdm.services.android.omadm.dmtree.beans;

import org.wso2.carbon.mdm.services.android.omadm.dmtree.exceptions.DMNodeException;
import org.wso2.carbon.mdm.services.android.omadm.dmtree.exceptions.DMNodePropertyException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The basic building block of the DMTree
 */
public class DMNode implements Serializable {

    // The name by which the Node is addressed in the Management Tree - Mandatory
    private String name;
    // Path to the node
    private String path;
    // Value of the node
    private String value;
    // Runtime properties of the node
    private RTProperty rtProperty;
    // List of sub nodes
    private ArrayList<DMNode> subNodes = new ArrayList();

    public DMNode() {}

    public DMNode(String name, String path, String value, RTProperty rtProperty, ArrayList<DMNode> subNodes) {
        this.name = name;
        this.path = path;
        this.value = value;

        setRtProperty(rtProperty);

        if (subNodes == null) {
            subNodes = new ArrayList<>();
        }
        setSubNodes(subNodes);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if ((value != null) && (!this.subNodes.isEmpty())) {
            throw new DMNodePropertyException("Cannot add a leaf node into an internal node");
        }
        this.value = value;
    }

    public RTProperty getRtProperty() {
        return rtProperty;
    }

    public void setRtProperty(RTProperty rtProperty) {
        this.rtProperty = rtProperty;
    }

    public ArrayList<DMNode> getSubNodes() {
        return subNodes;
    }

    public void setSubNodes(ArrayList<DMNode> subNodes) {

        if (subNodes != null)
        {
            checkLeafNode();
            for (DMNode node : subNodes) {
                if (!(node instanceof DMNode)) {
                    throw new DMNodePropertyException("Invalid nodes found in the array");
                }
            }
            this.subNodes = subNodes;
        }
        else
        {
            this.subNodes.clear();
        }
    }

    public void addSubNodes(ArrayList<DMNode> dmNodes)
    {
        if (dmNodes == null) {
            return;
        }
        checkLeafNode();
        for (DMNode node : dmNodes) {
            if (!(node instanceof DMNode)) {
                throw new IllegalArgumentException("The nodes in the array list are not valid");
            }
        }
        this.subNodes.addAll(dmNodes);
    }

    public void addSubNodes(DMNode[] dmNodes)
    {
        if (dmNodes != null)
        {
            checkLeafNode();
            this.subNodes.addAll(Arrays.asList(dmNodes));
        }
    }

    public void addNode(DMNode dmNode)
    {
        checkLeafNode();
        this.subNodes.add(dmNode);
    }

    /**
     * This method checks whether there's a value set to the node and if so, breaks the recursion
     */
    private void checkLeafNode()
    {
        if (this.value != null) {
            throw new DMNodeException("Cannot add a leaf node into an internal node");
        }
    }
}
