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

package org.wso2.carbon.mdm.services.android.omadm.dm.core.bootstrap;

import org.apache.commons.collections.map.ListOrderedMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the 'DMAcc' management object
 */
public class DMAcc implements Serializable {

    private Map<String, DMAccount> dmAccounts;

    public DMAcc() {}

    public DMAcc(DMAccount dmAccount, String accountName)
    {
        this.dmAccounts = new HashMap<>();
        this.dmAccounts.put(accountName, dmAccount);
    }

    public void addDMAccount(DMAccount dmAccount, String accountName)
    {
        this.dmAccounts.put(accountName, dmAccount);
    }

    public DMAccount getDMAccount(String accountName)
    {
        return this.dmAccounts.get(accountName);
    }

    public void renameDMAccount(String oldName, String newName)
    {
        if (this.dmAccounts == null) {
            return;
        }

        String str = null;
        ListOrderedMap localListOrderedMap = new ListOrderedMap();
        for (String currentKey : dmAccounts.keySet())
        {
            if (currentKey.equalsIgnoreCase(oldName)) {
                localListOrderedMap.put(newName, this.dmAccounts.get(oldName));
            } else {
                localListOrderedMap.put(str, this.dmAccounts.get(currentKey));
            }
        }
        this.dmAccounts = localListOrderedMap;
    }

    public Map<String, DMAccount> getDMAccounts()
    {
        return this.dmAccounts;
    }

    public void setDMAccounts(Map<String, DMAccount> dmAccounts)
    {
        this.dmAccounts = dmAccounts;
    }

    public int numberOfDMAccounts()
    {
        return this.dmAccounts.size();
    }

}
