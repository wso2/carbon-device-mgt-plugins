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

package org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.dfproperties;

import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.dfproperties.accesstype.AccessType;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.dfproperties.casesense.CaseSense;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.dfproperties.dfformat.DFFormat;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.dfproperties.dftype.DFType;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.dfproperties.occurrence.Occurrence;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.dfproperties.scope.Scope;

import javax.xml.bind.annotation.*;

/**
 * This class represents the 'DFProperties' element in a node
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "accessType",
        "defaultValue",
        "description",
        "dfFormat",
        "occurrence",
        "scope",
        "dfTitle",
        "dfType",
        "caseSense"
})
@XmlRootElement(name = "DFProperties")
public class DFProperties {

    @XmlElement(name = "AccessType", required = true)
    protected AccessType accessType;
    @XmlElement(name = "DefaultValue")
    protected String defaultValue;
    @XmlElement(name = "Description")
    protected String description;
    @XmlElement(name = "DFFormat", required = true)
    protected DFFormat dfFormat;
    @XmlElement(name = "Occurrence")
    protected Occurrence occurrence;
    @XmlElement(name = "Scope")
    protected Scope scope;
    @XmlElement(name = "DFTitle")
    protected String dfTitle;
    @XmlElement(name = "DFType", required = true)
    protected DFType dfType;
    @XmlElement(name = "CaseSense")
    protected CaseSense caseSense;

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DFFormat getDfFormat() {
        return dfFormat;
    }

    public void setDfFormat(DFFormat dfFormat) {
        this.dfFormat = dfFormat;
    }

    public Occurrence getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(Occurrence occurrence) {
        this.occurrence = occurrence;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public String getDfTitle() {
        return dfTitle;
    }

    public void setDfTitle(String dfTitle) {
        this.dfTitle = dfTitle;
    }

    public DFType getDfType() {
        return dfType;
    }

    public void setDfType(DFType dfType) {
        this.dfType = dfType;
    }

    public CaseSense getCaseSense() {
        return caseSense;
    }

    public void setCaseSense(CaseSense caseSense) {
        this.caseSense = caseSense;
    }
}
