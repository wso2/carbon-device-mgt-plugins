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

package org.wso2.carbon.mdm.services.android.omadm.dm.dao;

import org.wso2.carbon.mdm.services.android.omadm.ddf.constants.StandardMOConstants;
import org.wso2.carbon.mdm.services.android.omadm.ddf.standardmos.StandardMOFactory;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.MgmtTree;

/**
 * This is a mock DAO class written to mimic an RDBMS
 */
public class DeviceMODao {

    public MgmtTree getMO(String name, String deviceId) {

        MgmtTree tree = null;
        switch (name) {
            case StandardMOConstants.DEV_INFO:
                tree = StandardMOFactory.getDevInfoTree().getMgmtTree();
            break;
            case StandardMOConstants.DEV_DETAIL:
                tree = StandardMOFactory.getDevDetailTree().getMgmtTree();
            break;
        }
        return tree;
    }
}
