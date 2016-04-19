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

package org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.type;

import javax.xml.bind.annotation.*;

/**
 * The 'MIME' property of the 'DFType' property
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "value"
})
@XmlRootElement(name = "MIME")
public class MIME extends AbstractTypeProperty {

    @XmlValue
    protected String value;

    public String getvalue() {
        return value;
    }

    public void setvalue(String value) {
        this.value = value;
    }
}
