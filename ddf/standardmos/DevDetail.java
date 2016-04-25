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

package org.wso2.carbon.mdm.services.android.omadm.ddf.standardmos;

import org.wso2.carbon.mdm.services.android.omadm.ddf.util.DDFCommonUtils;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.MgmtTree;

/**
 * This class represents the standard 'DevInfo' Management Object
 */
public class DevDetail {

    // Name of the Management Object
    public static final String DEVDETAIL = "DevDetail";
    // Maximum depth of the management tree supported by the device
    public static final String URI_MAX_DEPTH = "/URI/MaxDepth";
    // Maximum total length of any URI used to address a node or node property
    public static final String URI_MAX_TOT_LEN = "/URI/MaxToLen";
    // Maximum total length of any URI segment in a URI used to address a node or node property
    public static final String URI_MAX_SEG_LEN = "/URI/MaxSegLen";
    // Device type
    public static final String DEV_TYP = "/DevTyp";
    // Original Equipment Manufacturer of the device
    public static final String OEM = "/OEM";
    // Firmware version of the device
    public static final String FWV = "/FwV";
    // Software version of the device
    public static final String SWV = "/SwV";
    // Hardware version of the device
    public static final String HWV = "/HwV";
    // Indicates whether the device supports the OMA DM Large Object Handling specification
    public static final String LRG_OBJ = "/LrgObj";
    // Path to the DevDetail DDF file
    public static final String DEV_DETAIL_DDF_PATH = "org/wso2/carbon/mdm/services/android/omadm/ddf/" +
            "resources/OMA-SUP-MO_DM_DevDetail-V1_2-20070209-A.xml";

    private MgmtTree mgmtTree = null;

    public DevDetail() {
        this.mgmtTree = DDFCommonUtils.generateTree(DEV_DETAIL_DDF_PATH);
        if (mgmtTree != null) {
            this.mgmtTree.setName(DEVDETAIL);
        }
    }

    public MgmtTree getTree() {
        if (this.mgmtTree != null) {
            return this.mgmtTree;
        } else {
            this.mgmtTree = DDFCommonUtils.generateTree(DEV_DETAIL_DDF_PATH);
            this.mgmtTree.setName(DEVDETAIL);
            return this.mgmtTree;
        }
    }

}
