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


import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.tools.resources.RDNodeResource;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Endpoint resource which directly links to RD (Resource Directory)
 * Register the path creating Resources specific to the register request.
 */

public class NodeResource extends RDNodeResource {

    private static final Logger LOGGER = Logger.getLogger(NodeResource.class.getCanonicalName());

	//dynamic patterns
	public Pattern dynamicResourcePattern= Pattern.compile("\\{(\\w+;?\\w+)\\}");

    public NodeResource(String ep, String domain) {
        super(ep, domain);
	}

	/**
	 * add a new resource to the node. E.g. the resource temperature or
	 * humidity. If the path is /readings/temp, temp will be a subResource
	 * of readings, which is a subResource of the node.
	 *
	 * As the overriding feature it checks for a Dynamic Resource Parent when adding a Dynamic Resource
	 * if no Dynamic Resource Parent found it change the parent Resource as the Dynamic Resource Parent
	 * before adding the the Dynamic Resource
	 *
	 * if the resource is the last resource of the path it is added as an End Resource or a Dynamic End Resource
	 * other wise the default resource type is Tag Resource
	 *
	 */
    @Override
    public CoapResource addNodeResource(String path) {
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
					//FIXME - end resource must be changed into a tag resource when adding a child resource under it
					try {
						if (!dynamicPath) //if the new subResource has no dynamic parent in it's path
							resource = new DynamicParentResource((TagResource) resource);

					} catch (ClassCastException e) {
						LOGGER.log(Level.INFO,e.getMessage());
					}

					if (scanner.hasNext())//if the resource is not the end resource
						subResource = new DynamicResource(next, true, this);
					else
						subResource = new DynamicEndResource(next, true, this);

				}
            	else
				{
					if (scanner.hasNext())//if the resource is not the end resource
						subResource = new TagResource(next, true, this);
					else
						subResource = new EndResource(next, true, this);
				}

                resource.add(subResource);
            }
            resource = subResource;
        }
		if (subResource != null) {
			subResource.setPath(resource.getPath());
			subResource.setName(next);
		}

        scanner.close();
        return subResource;
    }


}
