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

package org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.adapters;

import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.Node;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The XML adapter class for node maps
 */
public class NodeMapAdapter extends XmlAdapter<NodeMapAdapter.AdaptedHashMap, HashMap<String, Node>> {

    @Override
    public HashMap<String, Node> unmarshal(AdaptedHashMap v) throws Exception {
        HashMap<String, Node> nodeMap = new HashMap<String, Node>();
        for (Node node : v.nodes) {
            nodeMap.put(node.getName(), node);
        }
        return nodeMap;
    }

    @Override
    public AdaptedHashMap marshal(HashMap<String, Node> v) throws Exception {
        AdaptedHashMap adaptedHashMap = new AdaptedHashMap();
        for(Map.Entry<String, Node> entry : v.entrySet()) {
            adaptedHashMap.nodes.add(entry.getValue());
        }
        return adaptedHashMap;
    }

    public class AdaptedHashMap {
        @XmlElement(name = "Node")
        public List<Node> nodes = new ArrayList<>();
    }
}
