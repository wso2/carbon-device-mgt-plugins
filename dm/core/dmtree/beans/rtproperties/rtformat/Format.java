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

package org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.rtproperties.rtformat;

import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.format.*;

import javax.xml.bind.annotation.*;

/**
 * The 'Format' property of 'RTProperty' property
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "Format")
public class Format {

    @XmlElements({
            @XmlElement(name = "b64", required = true, type = FormatB64.class),
            @XmlElement(name = "bin", required = true, type = FormatBin.class),
            @XmlElement(name = "bool", required = true, type = FormatBool.class),
            @XmlElement(name = "chr", required = true, type = FormatChr.class),
            @XmlElement(name = "int", required = true, type = FormatInt.class),
            @XmlElement(name = "null", required = true, type = FormatNull.class),
            @XmlElement(name = "xml", required = true, type = FormatXml.class),
            @XmlElement(name = "date", required = true, type = FormatDate.class),
            @XmlElement(name = "time", required = true, type = FormatTime.class),
            @XmlElement(name = "float", required = true, type = Float.class)
    })

    private AbstractFormatProperty format;

    public AbstractFormatProperty getFormat() {
        return format;
    }

    public void setFormat(AbstractFormatProperty format) {
        this.format = format;
    }
}
