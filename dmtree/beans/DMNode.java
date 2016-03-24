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

import org.wso2.carbon.mdm.services.android.omadm.dmtree.beans.constants.FormatProperty;

/**
 * The basic building block of the DMTree
 */
public class DMNode {

    // The Access Control List - Mandatory
    private String acl;
    // The name by which the Node is addressed in the Management Tree - Mandatory
    private String name;
    // Data format of the  node - Mandatory
    private FormatProperty format;
    // Size of the node in bytes. Client is responsible to update this
    private int size;
    // A human readable alphanumeric string containing info about the node - Optional
    private String title;
    // The type of the node - Mandatory
    private String type;
    // Version number, automatically incremented at each modification - Optional
    private short verNo;
    /**
     * Time stamp, date and time of last change
     * UTC based, [ISO8601] basic format. e.g. 20010711T163817Z means July 11,
     * 2001 at 16 hours, 38 minutes and 17 seconds
     * Optional
     */
    private String tStamp;

}
