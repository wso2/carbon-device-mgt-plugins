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
package org.wso2.carbon.device.mgt.input.adapter.coap.resourceDirectory;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.tools.resources.RDLookUpTopResource;
import org.eclipse.californium.tools.resources.RDResource;
import org.eclipse.californium.tools.resources.RDTagTopResource;
import org.wso2.carbon.device.mgt.input.adapter.coap.resourceDirectory.network.DynamicMessageDeliverer;
import org.wso2.carbon.device.mgt.input.adapter.coap.resourceDirectory.resources.RootRDResource;

public class ResourceDirectory extends CoapServer {

    private static RDResource rdResource;
    //http client for each endpoint
    public static final HttpClient HTTP_CLIENT = HttpClientBuilder.create().build();

    public ResourceDirectory() {
        super();
        init();
    }

    public ResourceDirectory(int... ports) {
        super(ports);
        init();
    }

    public ResourceDirectory(NetworkConfig config, int... ports) {
        super(config, ports);
        init();
    }

    public void init() {

        try {
            //the messages with dynamic resources are delivered by DynamicMessageDeliverer
            this.setMessageDeliverer(new DynamicMessageDeliverer(this.getRoot()));
            //directory adds ResourceDiirectory resource, ResourseDirectoryLookup resource and Tag resource
            rdResource = new RootRDResource();
            this.add(rdResource);
            this.add(new RDLookUpTopResource(rdResource));
            this.add(new RDTagTopResource(rdResource));
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

    }


}
