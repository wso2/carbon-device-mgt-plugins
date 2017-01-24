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
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.tools.resources.RDNodeResource;

import java.util.LinkedList;

/**
 * A path containing DynamicReousces must have at least one DyamicParentResource to handle the requests.
 * the DynamicParent retrieves any ambiguous path in it's way and direct it to correct places if exist.
 * even a path may have many DynamicParentResources, the requests are handled by the Parent of the first DynamicResource in a path.
 */
public class DynamicParentResource extends TagResource {

	public DynamicParentResource(String name, boolean visible, RDNodeResource parentNode) {
		super(name, visible, parentNode);
	}

	/**
	 * Get existing resource and set it as a DynamicParentResource
	 * @param resource - the Tag resource that will be converted into a Dynamic parent
	 */
	public DynamicParentResource(TagResource resource)
	{
		//create a new dynamic parent resource identical to the tagresource
		super(resource.getName(),resource.isVisible(),resource.getParentNode());

		Resource parent = resource.getParent();

		//add new reource into the tree and set it's attribute values
		this.setPath(resource.getPath());
		for(Resource child:resource.getChildren())
		{
			this.add(child);
		}
		for(String key:resource.getAttributes().getAttributeKeySet())
		{
			this.getAttributes().addAttribute(key);
			for(String value:resource.getAttributes().getAttributeValues(key))
			{
				this.getAttributes().addAttribute(key,value);
			}
		}

		//delete the old tag resource
		resource.delete();

		//add new identical dynamic resource
		parent.add(this);

	}


	@Override
	public void handleRequest(Exchange exchange) {

		Request request = exchange.getRequest();
		LinkedList<String> path = new LinkedList<>(request.getOptions().getUriPath());

		//remove the first part of path until the current dynamic parent
		for (String param : this.getPath().substring(1).split("/")) {
			if(param.equalsIgnoreCase(path.getFirst()))
				path.removeFirst();
			else
				exchange.sendResponse(new Response(CoAP.ResponseCode.NOT_FOUND));
		}

		Resource current = this;
		if (current.getName().equals(path.removeFirst())) {
			current = searchDynamicPath(path, current);
			if (current != null)
				current.handleRequest(exchange);
			else{
				exchange.sendResponse(new Response(CoAP.ResponseCode.NOT_FOUND));
			}

		}
		else
		{
			exchange.sendResponse(new Response(CoAP.ResponseCode.NOT_FOUND));
		}



	}

	/**
	 * Recursive method to search for a matching path consists of dynamic parameters [DynamicResource]
	 * @param path    - path of next resource to the destination
	 * @param current - parent resource
	 * @return        - if there is an exact match of dynamic path the end resource of the path is returned; else null.
	 */
	public Resource searchDynamicPath(LinkedList<String> path, Resource current) {

		Resource resource = null;

		if (!path.isEmpty() && !current.getChildren().isEmpty()) {

			if (current.getChild(path.getFirst()) != null) //if a static resource and matches the name
			{
				current = current.getChild(path.removeFirst());
				Resource check = searchDynamicPath(path, current);
				if (check != null)
					resource = check;

			} else {

				// for each child resource check if it is a dynamic resource with matching parameter type

				String param = path.removeFirst();
				for (Resource child : current.getChildren()) {

					if((child instanceof DynamicResource && ((DynamicResource) child).isParamType(param)) || (child instanceof DynamicEndResource && ((DynamicEndResource) child).isParamType(param)))
					{

						Resource check = searchDynamicPath(path, child);
						if (check != null)
							resource = check;
					}
				}


			}
		} else if (path.isEmpty() && current.getChildren().isEmpty()) {
			return current;
		}


		return resource;

	}



}
