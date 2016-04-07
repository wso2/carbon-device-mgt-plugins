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

package org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.dfproperties.occurrence;

import javax.xml.bind.annotation.*;

/**
 * The 'Occurrence' property of the 'DFProperties' property
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Occurrence")
public class Occurrence {

    @XmlElements({
            @XmlElement(name = "One", required = true, type = One.class),
            @XmlElement(name = "ZeroOrOne", required = true, type = ZeroOrOne.class),
            @XmlElement(name = "ZeroOrMore", required = true, type = ZeroOrMore.class),
            @XmlElement(name = "OneOrMore", required = true, type = OneOrMore.class),
            @XmlElement(name = "ZeroOrN", required = true, type = ZeroOrN.class),
            @XmlElement(name = "OneOrN", required = true, type = OneOrN.class)
    })

    private AbstractOccurrenceProperty occurrence;

    public AbstractOccurrenceProperty getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(AbstractOccurrenceProperty occurrence) {
        this.occurrence = occurrence;
    }
}
