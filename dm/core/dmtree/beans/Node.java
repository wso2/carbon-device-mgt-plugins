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

import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.adapters.NodeMapAdapter;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.constants.FormatProperty;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.exceptions.DMNodeException;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.exceptions.DMNodePropertyException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a Node in the Management Tree
 */
@XmlRootElement(name = "Node")
@XmlAccessorType(XmlAccessType.FIELD)
public class Node {

    // The name by which the Node is addressed in the Management Tree - Mandatory
    @XmlElement(name = "NodeName")
    private String name;
    // Path to the node
    @XmlElement(name = "Path")
    private String path;
    // Value of the node
    @XmlElement(name = "Path")
    private String value;
    // Runtime properties of the node
    private RTProperty rtProperty = new RTProperty();
    //DF properties of the node
    @XmlElement(name = "DFProperties")
    private DFProperty dfProperty = new DFProperty();
    // List of sub nodes
    @XmlElement(name = "Nodes")
    @XmlJavaTypeAdapter(NodeMapAdapter.class)
    private Map<String, Node> subNodes;

    public Node() {}

    public Node(String name, String path, String value, RTProperty rtProperty) {
        this.name = name;
        this.path = path;
        this.value = value;
        this.rtProperty = rtProperty;
        if (subNodes == null) {
            subNodes = new HashMap<>();
        }
    }

    public Node(String name, String value, RTProperty rtProperty, Map<String, Node> subNodes) {
        this.name = name;
        this.value = value;
        this.rtProperty = rtProperty;
        this.subNodes = subNodes;
    }

    public void setValue(String value, FormatProperty format) {
        if (subNodes != null) {
            throw new DMNodeException("Cannot set a value to an Interior node");
        }
        if (format == FormatProperty.NODE) {
            throw new DMNodePropertyException("Cannot set format 'node' to a leaf node");
        }
        this.value = value;
        this.rtProperty.setFormat(format);
        this.rtProperty.incrementVerNo();
        this.rtProperty.updateTimeStamp();
    }

    public void setRtProperty(RTProperty rtProperty) {
        this.rtProperty = rtProperty;
    }

    public void setDfProperty(DFProperty dfProperty) {
        this.dfProperty = dfProperty;
    }

    public boolean addNode(Node node) {
        if (value != null) {
            throw new DMNodeException("Cannot add nodes to a leaf node");
        }
        if (subNodes == null) {
            subNodes = new HashMap<>();
        }
        subNodes.put(node.getName(), node);
        return true;
    }

    public boolean addNodes(Node[] nodes) {
        for (Node node : nodes) {
            subNodes.put(node.getName(), node);
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getValue() {
        return value;
    }

    public RTProperty getRtProperty() {
        return rtProperty;
    }

    public DFProperty getDfProperty() {
        return dfProperty;
    }

    public Map<String, Node> getSubNodes() {
        return subNodes;
    }

    public Node[] getSubNodesArray() {
        return subNodes.values().toArray((new Node[0]));
    }

    public void setName(String name) {
        if (name != null) {
            this.name = name;
        } else {
            throw new DMNodePropertyException("Node name cannot be null");
        }
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
