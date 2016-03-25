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

import java.io.Serializable;

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

}
