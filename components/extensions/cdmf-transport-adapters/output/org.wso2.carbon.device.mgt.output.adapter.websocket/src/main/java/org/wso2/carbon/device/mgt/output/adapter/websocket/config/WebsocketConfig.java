/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.output.adapter.websocket.config;

import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.output.adapter.websocket.util.WebsocketUtils;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * This class represents the configuration that are needed for scopes to permission map.
 */
public class WebsocketConfig {

    private static WebsocketConfig config = new WebsocketConfig();
    private WebsocketValidationConfigs websocketValidationConfigs;

    private static final String WEBSOCKET_VALIDATION_CONFIG_PATH =
            CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + "websocket-validation.xml";

    private WebsocketConfig() {
    }

    public static WebsocketConfig getInstance() {
        return config;
    }

    public void init() throws WebsocketValidationConfigurationFailedException {
        try {
            File deviceMgtConfig = new File(WEBSOCKET_VALIDATION_CONFIG_PATH);
            Document doc = WebsocketUtils.convertToDocument(deviceMgtConfig);

            /* Un-marshaling DeviceMGtScope configuration */
            JAXBContext ctx = JAXBContext.newInstance(WebsocketValidationConfigs.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            //unmarshaller.setSchema(getSchema());
            websocketValidationConfigs = (WebsocketValidationConfigs) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new WebsocketValidationConfigurationFailedException("Error occurred while un-marshalling Websocket" +
                                                                          " Config", e);
        }
    }

    public WebsocketValidationConfigs getWebsocketValidationConfigs() {
        return websocketValidationConfigs;
    }

    public void setWebsocketValidationConfigs(WebsocketValidationConfigs websocketValidationConfigs) {
        websocketValidationConfigs = websocketValidationConfigs;
    }
}
