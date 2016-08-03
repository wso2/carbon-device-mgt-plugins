/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.output.adapter.ui.util;

import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.iot.output.adapter.ui.config.WebsocketValidationConfigurationFailedException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * This class holds util methods used by OAuth extension bundle.
 */
public class WebsocketUtils {

    public static Document convertToDocument(File file) throws WebsocketValidationConfigurationFailedException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new WebsocketValidationConfigurationFailedException("Error occurred while parsing file, while converting " +
                                                                          "to a org.w3c.dom.Document", e);
        }
    }

}
