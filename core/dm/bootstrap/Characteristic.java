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

package org.wso2.carbon.mdm.services.android.omadm.core.dm.bootstrap;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class represents the 'characteristic' element in a Bootstrap file
 */
public class Characteristic implements Serializable {

    private static final long serialVersionUID = 12435448852938456L;
    // Bootstrap data
    public static final String TYPE_PXLOGICAL = "PXLOGICAL";
    public static final String TYPE_PXPHYSICAL = "PXPHYSICAL";
    public static final String TYPE_PXAUTHINFO = "PXAUTHINFO";
    public static final String TYPE_PORT = "PORT";
    public static final String TYPE_NAPDEF = "NAPDEF";
    public static final String TYPE_NAPAUTHINFO = "NAPAUTHINFO";
    public static final String TYPE_VALIDITY = "VALIDITY";
    public static final String TYPE_BOOTSTRAP = "BOOTSTRAP";
    public static final String TYPE_CLIENTIDENTITY = "CLIENTIDENTITY";
    public static final String TYPE_VENDORCONFIG = "VENDORCONFIG";
    public static final String TYPE_APPLICATION = "APPLICATION";
    public static final String TYPE_APPADDR = "APPADDR";
    public static final String TYPE_APPAUTH = "APPAUTH";
    public static final String TYPE_RESOURCE = "RESOURCE";
    public static final String TYPE_ACCESS = "ACCESS";

    private ArrayList<Serializable> children = new ArrayList();
    private String type;

    public Characteristic() {}

    public Characteristic(String type)
    {
        this();
        this.type = type;
    }

    public void add(Characteristic characteristic)
    {
        this.children.add(characteristic);
    }

    public void add(Parm parm)
    {
        this.children.add(parm);
    }

    public String getType()
    {
        return this.type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public ArrayList<Serializable> getChildren()
    {
        return this.children;
    }

    public void removeParm(String parmName)
    {
        Parm parm = null;
        if (this.children != null)
        {
            for (Object obj : children)
            {
                if ((obj instanceof Parm))
                {
                    parm = (Parm)obj;
                    if (parmName.equalsIgnoreCase(parm.getName()))
                    {
                        this.children.remove(parm);
                        return;
                    }
                }
            }
        }
    }

    public void removeCharacteristic(String characName)
    {
        Characteristic characteristic = null;
        ArrayList tempArray = new ArrayList();
        if (this.children != null)
        {
            for (Object obj : children)
            {
                if ((obj instanceof Characteristic))
                {
                    characteristic = (Characteristic)obj;
                    if (characName.equalsIgnoreCase(characteristic.getType())) {
                        tempArray.add(characteristic);
                    }
                }
            }
        }
        this.children.removeAll(tempArray);
    }

    public Parm getParm(String parmName)
    {
        Parm parm = null;
        if (this.children != null)
        {
            for (Object obj : children)
            {
                if ((obj instanceof Parm))
                {
                    parm = (Parm)obj;
                    if (parmName.equalsIgnoreCase(parm.getName())) {
                        return parm;
                    }
                }
            }
        }
        return null;
    }

    public int setParm(String parmName, String parmValue, int index)
    {
        Parm parm = null;
        int i = 0;
        if (this.children != null)
        {
            for (Object obj : children)
            {
                if ((obj instanceof Parm))
                {
                    parm = (Parm)obj;
                    if (parmName.equalsIgnoreCase(parm.getName()))
                    {
                        parm.setValue(parmValue);
                        return i;
                    }
                }
                i++;
            }
        }
        parm = new Parm(parmName, parmValue);
        this.children.add(index, parm);
        return index;
    }

    public Characteristic getCharacteristic(String characName)
    {
        Characteristic characteristic = null;
        if (this.children != null)
        {
            for (Object obj : children)
            {
                if ((obj instanceof Characteristic))
                {
                    characteristic = (Characteristic)obj;
                    if (characName.equalsIgnoreCase(characteristic.getType())) {
                        return characteristic;
                    }
                }
            }
        }
        return null;
    }

}
