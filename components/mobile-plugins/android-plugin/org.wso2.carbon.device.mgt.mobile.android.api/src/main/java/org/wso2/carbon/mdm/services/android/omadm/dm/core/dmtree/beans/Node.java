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

import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.dfproperties.DFProperties;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.rtproperties.RTProperties;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.exceptions.DMNodeException;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Node in the Management Tree
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "nodeName",
        "path",
        "rtProperties",
        "dfProperties",
        "nodes",
        "value"
})
@XmlRootElement(name = "Node")
public class Node {

    private static String NODE_URI_DELIMETER = "/";
    public static final String TYPE_DEFAULT_VALUE = "text/plain";
    public static final String FORMAT_DEFAULT_VALUE = "chr";

    @XmlElement(name = "NodeName", required = true)
    private String nodeName;
    @XmlElement(name = "Path")
    private String path;
    @XmlElement(name = "RTProperties")
    private RTProperties rtProperties;
    @XmlElement(name = "DFProperties")
    private DFProperties dfProperties;
    @XmlElement(name = "Node")
    private List<Node> nodes = new ArrayList<>();
    @XmlElement(name = "Value")
    private String value;
    @XmlTransient
    private String type = TYPE_DEFAULT_VALUE;
    @XmlTransient
    private String format = FORMAT_DEFAULT_VALUE;
    @XmlTransient
    private String size;

    public Node() {
    }

    public Node(String nodeName, List<Node> nodes, RTProperties rtProperties) {
        this.nodeName = nodeName;
        this.nodes = nodes;
        this.rtProperties = rtProperties;
    }

    public Node(String nodeName, RTProperties rtProperties, String value) {
        this.nodeName = nodeName;
        this.rtProperties = rtProperties;
        this.value = value;
    }

    public Node(String nodeName, DFProperties dfProperties, List<Node> nodes) {
        this.nodeName = nodeName;
        this.dfProperties = dfProperties;
        this.nodes = nodes;
    }

    public Node(String nodeName, DFProperties dfProperties, String value) {
        this.nodeName = nodeName;
        this.dfProperties = dfProperties;
        this.value = value;
    }

    public void checkForValue() {
        if (this.value != null) {
            throw new DMNodeException("Cannot add nodes to a leaf node");
        }
    }

    public void checkForSubNodes() {
        if (this.nodes != null) {
            throw new DMNodeException("Cannot set a value to an interior node");
        }
    }

    public int getChildNodeCount() {
        return this.nodes.size();
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

    public String getNodeName() {
        return nodeName;
    }

    public String getPath() {
        return path;
    }

    public RTProperties getRtProperties() {
        return rtProperties;
    }

    public DFProperties getDfProperties() {
        return dfProperties;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public String getValue() {
        return value;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setRtProperties(RTProperties rtProperties) {
        this.rtProperties = rtProperties;
    }

    public void setDfProperties(DFProperties dfProperties) {
        this.dfProperties = dfProperties;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
