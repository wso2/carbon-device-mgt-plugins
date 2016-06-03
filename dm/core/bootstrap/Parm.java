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

/**
 * This class represents the 'parm' sub-element of a 'characteristic' element
 */
public class Parm implements Serializable {

    private static final long serialVersionUID = 9845243756234759L;
    private String name;
    private String value;

    public Parm() {}

    public Parm(String name, String value)
    {
        this.name = name;
        setValue(value);
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return this.value;
    }

    public void setValue(String value)
    {
        this.value = (value == null ? "" : value);
    }

    public boolean equals(Object obj)
    {
        if ((obj == null) || (!(obj instanceof Parm))) {
            return false;
        }
        return getName().equals(((Parm)obj).getName());
    }

    public int hashCode()
    {
        return getName().hashCode();
    }

}
