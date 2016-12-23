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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.input.adapter.coap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.californium.core.CoapServer;
import org.wso2.carbon.device.mgt.input.adapter.coap.resourceDirectory.ResourceDirectory;
import org.wso2.carbon.device.mgt.input.adapter.coap.util.COAPEventAdapterConstants;
import org.wso2.carbon.event.input.adapter.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * The CoAp event adapter factory class to create a CoAP input adapter and start the RD server
 */
public class COAPEventAdapterFactory extends InputEventAdapterFactory {

    private int coapPort;
    private CoapServer directory;
    private static final Log log = LogFactory.getLog(COAPEventAdapterFactory.class);

    public COAPEventAdapterFactory() {
        this.coapPort = COAPEventAdapterConstants.DEFAULT_COAP_PORT;
        directoryStartup();
    }

    @Override
    public String getType() {
        return COAPEventAdapterConstants.ADAPTER_TYPE_COAP;
    }

    @Override
    public List<String> getSupportedMessageFormats() {

        List<String> supportInputMessageTypes = new ArrayList<>();
        supportInputMessageTypes.add(MessageType.JSON);
        supportInputMessageTypes.add(MessageType.TEXT);
        return supportInputMessageTypes;
    }

    @Override
    public List<Property> getPropertyList() {
        return  new ArrayList<>();

    }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration inputEventAdapterConfiguration, Map<String, String> map) {
        return new COAPEventAdapter(inputEventAdapterConfiguration, map);
    }


    private void directoryStartup() {
        //coap directory server startup in coap port
        FutureTask<CoapServer> startup = new FutureTask<CoapServer>(new Callable() {
            @Override
            public CoapServer call() throws Exception {
                CoapServer directory = new ResourceDirectory(coapPort);
                directory.start();
                return directory;
            }
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(startup);

        try {
            if (startup.isDone()) {
                this.directory = startup.get();
                executor.shutdown();
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
        }
    }

    public CoapServer getDirectory() {
        return directory;
    }


}
