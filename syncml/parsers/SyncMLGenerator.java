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

package org.wso2.carbon.mdm.services.android.omadm.syncml.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.SyncmlBody;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.SyncmlDocument;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.SyncmlHeader;
import org.wso2.carbon.mdm.services.android.syncml.beans.*;
import org.wso2.carbon.mdm.services.android.omadm.syncml.exceptions.SyncMLSyntaxException;
import org.wso2.carbon.mdm.services.android.omadm.syncml.util.Constants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * Generates the response syncml xml file that should be sent to the Device.
 */
public class SyncMLGenerator {

    public static String generatePayload(SyncmlDocument syncmlDocument) throws SyncMLSyntaxException {
        Document doc = generateDocument();
        Element rootElement = createRootElement(doc);
        SyncmlHeader header = syncmlDocument.getHeader();
        header.buildSyncmlHeaderElement(doc, rootElement);
        SyncmlBody body = syncmlDocument.getBody();
        body.buildBodyElement(doc, rootElement);
        return transformDocument(doc);
    }

    private static Document generateDocument() throws SyncMLSyntaxException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = documentFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new SyncMLSyntaxException("Error occurred while generating a new document of syncml", e);
        }
        return docBuilder.newDocument();
    }

    private static Element createRootElement(Document document) {
        Element rootElement = document.createElementNS(Constants.SyncMLGeneralInfo.XMLNS_SYNCML,
                Constants.SyncMLGeneralInfo.SYNCML_ROOT_ELEMENT_NAME);
        document.appendChild(rootElement);
        return rootElement;
    }

    private static String transformDocument(Document document) throws SyncMLSyntaxException {
        DOMSource domSource = new DOMSource(document);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new SyncMLSyntaxException("Error occurred while retrieving a new transformer", e);
        }
        transformer.setOutputProperty(OutputKeys.ENCODING, Constants.SyncMLGeneralInfo.UTF_8);
        transformer.setOutputProperty(OutputKeys.INDENT, Constants.SyncMLGeneralInfo.YES);

        StringWriter stringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(stringWriter);
        try {
            transformer.transform(domSource, streamResult);
        } catch (TransformerException e) {
            throw new SyncMLSyntaxException("Error occurred while transforming document to a string", e);
        }
        return stringWriter.toString();
    }
}

