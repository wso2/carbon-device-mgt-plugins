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

    // Path to the DevDetail DDF file
    public static final String DEV_DETAIL_DDF_PATH = "OMA-SUP-MO_DM_DevDetail-V1_2-20070209-A.xml";

    private static MgmtTree mgmtTree = DDFCommonUtils.generateTree(DEV_DETAIL_DDF_PATH);
    private static DevDetail devDetail = new DevDetail();

    public DevDetail() {
    }

    public static DevDetail getInstance() {
        if (mgmtTree != null) {
            devDetail.setMgmtTree(mgmtTree);
        }
        return devDetail;
    }

    public MgmtTree getMgmtTree() {
        return mgmtTree;
    }

    private static void setMgmtTree(MgmtTree mgmtTree) {
        DevDetail.mgmtTree = mgmtTree;
    }

}
