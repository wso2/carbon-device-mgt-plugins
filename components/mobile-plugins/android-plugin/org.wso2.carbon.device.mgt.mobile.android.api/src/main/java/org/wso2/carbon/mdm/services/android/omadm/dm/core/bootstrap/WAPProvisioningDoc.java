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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the WAP Provisioning Document
 */
public class WAPProvisioningDoc implements Serializable {

    private static final long serialVersionUID = 483274682437479L;
    private ArrayList<Characteristic> characteristics = null;
    private String version = null;

    public WAPProvisioningDoc()
    {
        this.characteristics = new ArrayList();
    }

    public WAPProvisioningDoc(String paramString)
    {
        this.version = paramString;
        this.characteristics = new ArrayList();
    }

    public String getVersion()
    {
        return this.version;
    }

    public void setVersion(String paramString)
    {
        this.version = paramString;
    }

    public void addCharacteristic(Characteristic characteristic)
    {
        this.characteristics.add(characteristic);
    }

    public void addCharacteristicList(List<Characteristic> characteristicList)
    {
        this.characteristics.addAll(characteristicList);
    }

    public ArrayList<Characteristic> getCharacteristics()
    {
        return this.characteristics;
    }

}
