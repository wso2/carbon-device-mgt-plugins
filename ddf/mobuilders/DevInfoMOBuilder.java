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

package org.wso2.carbon.mdm.services.android.omadm.ddf.mobuilders;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mdm.services.android.omadm.ddf.ManagementTree;
import org.wso2.carbon.mdm.services.android.omadm.ddf.standardmos.DevInfo;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.DFProperty;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.Node;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.RTProperty;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.constants.DFPropertyAccessType;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.constants.DFPropertyCaseSense;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.constants.DFPropertyScope;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.constants.FormatProperty;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.exceptions.DMException;

import java.io.File;

/**
 * This class builds the standard 'DevInfo' management object
 */
public class DevInfoMOBuilder {

    private static Log log = LogFactory.getLog(DevInfoMOBuilder.class);

    // The unique management object identifier defined by OMA
    public static final String MO_IDENTIFIER = "urn:oma:mo:oma-dm-devinfo:1.0";

    private ManagementTree mgmtTree;
    private DevInfo devInfo;

    public DevInfoMOBuilder() {
    }

    public DevInfoMOBuilder(DevInfo devInfo) {
        if (devInfo == null) {
            throw new DMException("DevInfo cannot be null");
        }
        this.devInfo = devInfo;
    }

    public ManagementTree buildTree() {
        //TODO : Build the Management Object from pre-defined values
        return this.mgmtTree;
    }

    public ManagementTree buildTree(File ddf) {
        //TODO : Build the Management Object from a XML DDF file
        return this.mgmtTree;
    }
}
