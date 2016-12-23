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
package org.wso2.carbon.device.mgt.input.adapter.coap.resourceDirectory.network;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.observe.ObserveManager;
import org.eclipse.californium.core.observe.ObserveRelation;
import org.eclipse.californium.core.observe.ObservingEndpoint;
import org.eclipse.californium.core.server.MessageDeliverer;
import org.eclipse.californium.core.server.resources.Resource;
import org.wso2.carbon.device.mgt.input.adapter.coap.resourceDirectory.resources.DynamicParentResource;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A strategy for delivering inbound CoAP messages to an appropriate processor.
 *
 * Implementations should try to deliver incoming CoAP requests to a published
 * resource matching the request's URI. If no such resource exists, it checks for a Dynamic Resource Parent to the not-matching Resource
 * if no such Resource found an incoming CoAP response
 * message should be delivered to its corresponding outbound request.
 */

public class DynamicMessageDeliverer implements MessageDeliverer{

	private static final Logger LOGGER = Logger.getLogger(DynamicMessageDeliverer.class.getCanonicalName());

	private final Resource root;

	private final ObserveManager observeManager = new ObserveManager();

	public DynamicMessageDeliverer(Resource root) {
		this.root = root;
	}

	/** from <code>ServerMessageDeliverer</code> class*/
	@Override
	public void deliverRequest(final Exchange exchange) {
		Request request = exchange.getRequest();
		List<String> path = request.getOptions().getUriPath();
		final Resource resource = findResource(path);
		if (resource != null) {
			checkForObserveOption(exchange, resource);

			// Get the executor and let it process the request
			Executor executor = resource.getExecutor();
			if (executor != null) {
				exchange.setCustomExecutor();
				executor.execute(new Runnable() {
					public void run() {
						resource.handleRequest(exchange);
					} });
			} else {
				resource.handleRequest(exchange);
			}
		} else {
			LOGGER.log(Level.INFO, "Did not find resource {0} requested by {1}:{2}",
					new Object[]{path, request.getSource(), request.getSourcePort()});
			exchange.sendResponse(new Response(CoAP.ResponseCode.NOT_FOUND));
		}
	}

	/** from <code>ServerMessageDeliverer</code> class*/
	@Override
	public void deliverResponse(Exchange exchange, Response response) {
		if (response == null) {
			throw new NullPointerException("Response must not be null");
		} else if (exchange == null) {
			throw new NullPointerException("Exchange must not be null");
		} else if (exchange.getRequest() == null) {
			throw new IllegalArgumentException("Exchange does not contain request");
		} else {
			exchange.getRequest().setResponse(response);
		}
	}

	/**
	 * Searches in the resource tree for the specified path. A parent resource
	 * may accept requests to subresources, e.g., to allow addresses with
	 * wildcards like <code>coap://example.com:5683/devices/*</code>
	 *
	 * Modification - allow path parameter access using dynamic resources
	 * e.g., <code>coap://example.com:5683/devices/{deviceId}/stats</code>
	 *
	 * @param list the path as list of resource names
	 * @return the resource or dynamic resource or null if not found
	 */
	private Resource findResource(final List<String> list) {
		LinkedList<String> path = new LinkedList<String>(list);
		Resource current = root;
		Resource last = null;
		while (!path.isEmpty() && current != null) {
			last=current;
			String name = path.removeFirst();
			current = current.getChild(name);
		}

        //if the exact path doesn't match check if it has a dynamic resource
		if(current==null && last instanceof DynamicParentResource)
		{
			return last;
		}

		return current;
	}

	/** from <code>ServerMessageDeliverer</code> class*/
	private void checkForObserveOption(final Exchange exchange, final Resource resource) {
		Request request = exchange.getRequest();
		if (request.getCode() != CoAP.Code.GET) {
			return;
		}

		InetSocketAddress source = new InetSocketAddress(request.getSource(), request.getSourcePort());

		if (request.getOptions().hasObserve() && resource.isObservable()) {

			if (request.getOptions().getObserve()==0) {
				// Requests wants to observe and resource allows it :-)
				LOGGER.log(Level.FINER,
						"Initiate an observe relation between {0}:{1} and resource {2}",
						new Object[]{request.getSource(), request.getSourcePort(), resource.getURI()});
				ObservingEndpoint remote = observeManager.findObservingEndpoint(source);
				ObserveRelation relation = new ObserveRelation(remote, resource, exchange);
				remote.addObserveRelation(relation);
				exchange.setRelation(relation);
				// all that's left is to add the relation to the resource which
				// the resource must do itself if the response is successful

			} else if (request.getOptions().getObserve() == 1) {
				// Observe defines 1 for canceling
				ObserveRelation relation = observeManager.getRelation(source, request.getToken());
				if (relation != null) {
					relation.cancel();
				}
			}
		}
	}
}
