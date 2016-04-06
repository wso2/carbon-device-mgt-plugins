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

package org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans;

import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.constants.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Runtime Properties of a Node
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DFProperty {

    @XmlElement(name = "AccessType")
    private DFPropertyAccessType accessType;
    @XmlElement(name = "DefaultValue")
    private String defaultValue;
    @XmlElement(name = "Description")
    private String description;
    @XmlElement(name = "DFFormat")
    private FormatProperty format;
    @XmlElement(name = "Occurrence")
    private DFPropertyOccurrence occurrence;
    @XmlElement(name = "Scope")
    private DFPropertyScope scope;
    @XmlElement(name = "DFTitle")
    private String dfTitle;
    @XmlElement(name = "DFType")
    private String dfType;
    @XmlElement(name = "CaseSense")
    private DFPropertyCaseSense caseSense;

    public DFProperty() {
    }

    public DFProperty(DFPropertyAccessType accessType, String defaultValue, String description,
                      FormatProperty format, DFPropertyOccurrence occurrence, DFPropertyScope scope,
                      String dfTitle, String dfType, DFPropertyCaseSense caseSense) {
        this.accessType = accessType;
        this.defaultValue = defaultValue;
        this.description = description;
        this.format = format;
        this.occurrence = occurrence;
        this.scope = scope;
        this.dfTitle = dfTitle;
        this.dfType = dfType;
        this.caseSense = caseSense;
    }

    public DFPropertyAccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(DFPropertyAccessType accessType) {
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

    public FormatProperty getFormat() {
        return format;
    }

    public void setFormat(FormatProperty format) {
        this.format = format;
    }

    public DFPropertyOccurrence getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(DFPropertyOccurrence occurrence) {
        this.occurrence = occurrence;
    }

    public DFPropertyScope getScope() {
        return scope;
    }

    public void setScope(DFPropertyScope scope) {
        this.scope = scope;
    }

    public String getDfTitle() {
        return dfTitle;
    }

    public void setDfTitle(String dfTitle) {
        this.dfTitle = dfTitle;
    }

    public String getDfType() {
        return dfType;
    }

    public void setDfType(String dfType) {
        this.dfType = dfType;
    }

    public DFPropertyCaseSense getCaseSense() {
        return caseSense;
    }

    public void setCaseSense(DFPropertyCaseSense caseSense) {
        this.caseSense = caseSense;
    }
}
