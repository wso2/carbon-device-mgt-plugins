/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import org.eclipse.californium.core.CoapClient;
import org.wso2.carbon.device.mgt.input.adapter.coap.util.COAPEventAdapterConstants;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;

import java.util.Map;

public class COAPEventAdapter implements InputEventAdapter {

    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;

    public COAPEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                            Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init(InputEventAdapterListener inputEventAdapterListener) throws InputEventAdapterException {

    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException, InputEventAdapterRuntimeException, ConnectionUnavailableException {


    }

    @Override
    public void connect() {

        String endpoint = eventAdapterConfiguration.getName();
        String payload = "";
        CoapClient coapClient = new CoapClient(COAPEventAdapterConstants.COAP, "localhost", COAPEventAdapterConstants.DEFAULT_COAP_PORT, COAPEventAdapterConstants.RESOURCE_DIRECTORY+"?ep="+endpoint); //schema, host,port,path
//        coapClient.post(new CoapHandler() {
//            @Override
//            public void onLoad(CoapResponse response) {
//
//            }
//
//            @Override
//            public void onError() {
//
//            }
//        },payload, C);C
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isEventDuplicatedInCluster() {
        return false;
    }

    @Override
    public boolean isPolling() {
        return false;
    }
}
