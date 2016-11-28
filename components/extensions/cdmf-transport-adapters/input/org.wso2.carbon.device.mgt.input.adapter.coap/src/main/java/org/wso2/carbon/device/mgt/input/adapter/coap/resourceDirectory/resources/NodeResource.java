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


import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.LinkFormat;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.core.server.resources.ResourceAttributes;
import org.eclipse.californium.tools.resources.RDNodeResource;

import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Endpoint resource which directly links to RD (Resource Directory)
 */

public class NodeResource extends RDNodeResource {

    private static final Logger LOGGER = Logger.getLogger(NodeResource.class.getCanonicalName());

    //parameter constants
    public static final String URI_TEMPLATE = "ut";

	//dynamic patterns
	public Pattern dynamicResourcePattern= Pattern.compile("\\{(\\w+;?\\w+)\\}");

    //http client for each endpoint
    public static final HttpClient HTTP_CLIENT= HttpClientBuilder.create().build();

    public NodeResource(String ep, String domain) {
        super(ep, domain);
	}


    @Override
    public CoapResource addResource(String path, ResourceAttributes attributes) {
        Scanner scanner = new Scanner(path);
        scanner.useDelimiter("/");
        String next = "";
        boolean resourceExist = false;
		boolean dynamicPath = false; //define if the current path contains dynamic parent (hence dynamic resources)
        Resource resource = this; // It's the resource that represents the endpoint

        CoapResource subResource = null;
        while (scanner.hasNext()) {
            resourceExist = false;
            next = scanner.next();
            for (Resource res : resource.getChildren()) {
                if (res.getName().equals(next)) {
                    subResource = (CoapResource) res;
                    resourceExist = true;
					if(subResource instanceof DynamicParentResource)
						dynamicPath=true;
                }
            }
            if (!resourceExist) {

				if(next.matches(dynamicResourcePattern.pattern())) //if the resource is dynamic
				{
					//FIXME - end resource must be changed into a tag resource when adding a child resoruce under it
					if(!dynamicPath) //if the new subResource has no dynamic parent in it's path
					{
						resource= new DynamicParentResource((TagResource) resource);
					}

					if (scanner.hasNext())//if the resource is not the end resource
						subResource = new DynamicResource(next, true, this);
					else
						subResource = addEndResource(next,attributes,true);

				}
            	else
				{
					if (scanner.hasNext())//if the resource is not the end resource
						subResource = new TagResource(next, true, this);
					else
						subResource = addEndResource(next, attributes,false);
				}

                resource.add(subResource);
            }
            resource = subResource;
        }
        subResource.setPath(resource.getPath());
        subResource.setName(next);
        scanner.close();
        return subResource;
    }

	/**
	 * @param name
	 * @param attributes
	 * @param isDynamic check if the resource is a dynamic resource or not
	 * @return
	 */
    public CoapResource addEndResource(String name, ResourceAttributes attributes,boolean isDynamic) {

    	CoapResource endResource;
		String resourceCode =null;

        if (attributes.containsAttribute(LinkFormat.INTERFACE_DESCRIPTION))
            resourceCode=attributes.getAttributeValues(LinkFormat.INTERFACE_DESCRIPTION).get(0);

		if(isDynamic)
			endResource = new DynamicEndResource(name, true, this, resourceCode);
		else
			endResource = new EndResource(name, true, this, resourceCode);
		return endResource;
    }




}
