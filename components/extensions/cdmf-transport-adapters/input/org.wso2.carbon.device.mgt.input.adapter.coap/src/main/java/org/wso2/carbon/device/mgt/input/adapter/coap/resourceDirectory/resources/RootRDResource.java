/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.device.mgt.input.adapter.coap.resourceDirectory.resources;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.tools.resources.KeyValuePair;
import org.eclipse.californium.tools.resources.RDNodeResource;
import org.eclipse.californium.tools.resources.RDResource;

import java.util.List;

/**
 * Modified RD resource
 */

public class RootRDResource extends RDResource {

    public RootRDResource()
    {
        super();
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        // get name and lifetime from option query
        String endpointName = "";
        String domain = "local";
        RDNodeResource resource = null;

        CoAP.ResponseCode responseCode;

        LOGGER.info("Registration request from "+exchange.getSourceAddress().getHostName()+":"+exchange.getSourcePort());

        List<String> query = exchange.getRequestOptions().getUriQuery();
        for (String q : query) {

            KeyValuePair kvp = KeyValuePair.parse(q);

            if (LinkFormat.END_POINT.equals(kvp.getName()) && !kvp.isFlag()) {
                endpointName = kvp.getValue();
            }

            if (LinkFormat.DOMAIN.equals(kvp.getName()) && !kvp.isFlag()) {
                domain = kvp.getValue();
            }
        }

        // mandatory variables
        if (endpointName.isEmpty()) {
            LOGGER.info("Missing Endpoint Name for "+exchange.getSourceAddress().getHostName()+":"+exchange.getSourcePort());
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Missing Endpoint Name (?ep)");
            return;
        }

        // find already registered EP
        for (Resource node : getChildren()) {
            if (((NodeResource) node).getEndpointName().equals(endpointName) && ((NodeResource) node).getDomain().equals(domain)) {
                resource = (NodeResource) node;
            }
        }

        if (resource==null) {

            // uncomment to use random resource names instead of registered Endpoint Name
			/*
			String randomName;
			do {
				randomName = Integer.toString((int) (Math.random() * 10000));
			} while (getChild(randomName) != null);
			*/

            resource = new NodeResource(endpointName, domain);
            add(resource);

            responseCode = CoAP.ResponseCode.CREATED;
        } else {
            responseCode = CoAP.ResponseCode.CHANGED;
        }

        // set parameters of resource or abort on failure
        if (!resource.setParameters(exchange.advanced().getRequest())) {
            resource.delete();
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
            return;
        }

        LOGGER.info("Adding new endpoint: "+resource.getContext());

        // inform client about the location of the new resource
        exchange.setLocationPath(resource.getURI());

        // complete the request
        exchange.respond(responseCode);
    }


}
