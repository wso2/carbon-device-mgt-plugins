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

import org.apache.http.*;
import org.apache.http.message.BasicHeader;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.eclipse.californium.core.coap.*;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.proxy.HttpTranslator;
import org.eclipse.californium.proxy.TranslationException;
import org.eclipse.californium.tools.resources.RDNodeResource;
import org.wso2.carbon.device.mgt.input.adapter.coap.resourceDirectory.ResourceDirectory;
import org.wso2.carbon.device.mgt.input.adapter.coap.resourceDirectory.coap.MappingRDProperties;
import org.wso2.carbon.device.mgt.input.adapter.coap.resourceDirectory.coap.OtherOptionNumberRegistry;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This resource added at the end of a path which starts from a NodeResource which handles all the RESTful requests.
 *
 * If the incoming request Code (GET/POST/PUT/DELETE) is equal to the Resource's registered Code, the class direct the request to the needed method handler
 * if not, a Method Not Allowed message is sent.
 */

public class EndResource extends TagResource {

	public static final Properties HTTP_OTHER_PROPERTIES = new MappingRDProperties("OtherProxy.properties");
	private CoAP.Code resourceCode; // an EndResource can handle only one request type [GET/POST/PUT/DELETE]
	private static final Logger LOGGER = Logger.getLogger(EndResource.class.getCanonicalName());

	public EndResource(String name, boolean visible, RDNodeResource parentNode, String resourceCode) {

		super(name, visible, parentNode);
		setResourceCode(resourceCode);
	}

	public EndResource(String name, boolean visible, RDNodeResource parentNode) {
		super(name, visible, parentNode);
		this.resourceCode = null;
	}

	/**
	 * check for the code and direct it to the needed method
	 */
	@Override public void handleRequest(Exchange exchange) {

		CoAP.Code code = exchange.getRequest().getCode();

		/* TODO - The attributes should be validated and set before constructing the EndResource*/
		if (this.resourceCode == null) {
			if (getAttributes().containsAttribute(LinkFormat.INTERFACE_DESCRIPTION))
				setResourceCode(getAttributes().getAttributeValues(LinkFormat.INTERFACE_DESCRIPTION).get(0));
		}

		if (code.equals(resourceCode)) {
			switch (code) {
			case GET:
				handleGET(new CoapExchange(exchange, this));
				break;
			case POST:
				handlePOST(new CoapExchange(exchange, this));
				break;
			case PUT:
				handlePUT(new CoapExchange(exchange, this));
				break;
			case DELETE:
				handleDELETE(new CoapExchange(exchange, this));
				break;
			default:
				exchange.sendResponse(new Response(CoAP.ResponseCode.METHOD_NOT_ALLOWED));
			}
		} else
			exchange.sendResponse(new Response(CoAP.ResponseCode.METHOD_NOT_ALLOWED));

	}

	@Override
	public void handleGET(CoapExchange exchange) {

		exchange.respond(this.handleAsHttp(exchange.advanced().getRequest()));
	}

	@Override
	public void handlePOST(CoapExchange exchange) {

		exchange.respond(this.handleAsHttp(exchange.advanced().getRequest()));
	}

	/**
	 * This method convert the CoAP request into a HTTP request and send to the relevant HTTP endpoints
	 * @param request - CoAP Request message
	 * @return - Http Response from the HTTP endpoint.
	 */
	public Response handleAsHttp(Request request)
	{
		Response response=null;

		//set http URI by removing 'rd' part in the reousrce path
		URL proxyUri = getHttpURI(request);
		if (request.getOptions().getProxyUri() == null)
			request.getOptions().setProxyUri(proxyUri.toString());

		//get hosts
		HttpHost httpHost = new HttpHost(proxyUri.getHost(), proxyUri.getPort(), proxyUri.getProtocol());

		try {

			//translate the coap request to http request
			HttpRequest httpRequest=HttpTranslator.getHttpRequest(request);

			//other option values mapped to http headers
			List<Header> headers= setOtherOptionHeaders(request.getOptions());
			ContentType contentType=null;
			for(Header header:headers) {
				if((header.getName().equals(OtherOptionNumberRegistry.Names.Content_Type))) {
					contentType = ContentType.parse(header.getValue());
					httpRequest.removeHeaders("content-type");
				}
				httpRequest.addHeader(header);
			}
			
			//set the http payload according to the other option content-type
			if(contentType!=null && !request.getPayloadString().isEmpty()) {
				HttpEntity httpEntity = new StringEntity(request.getPayloadString(),contentType);
				((BasicHttpEntityEnclosingRequest) httpRequest).setEntity(httpEntity);
			}

			//send the request from a static CLIENT and get the http response
			HttpResponse httpResponse=ResourceDirectory.HTTP_CLIENT.execute(httpHost,httpRequest);
			//translate the http response into coap response
			response=HttpTranslator.getCoapResponse(httpResponse, request);
		} catch (TranslationException | IOException e) {
			LOGGER.log(Level.WARNING,e.getMessage());
		}
		return response;
	}

	/**
	 * set headers for other option set
	 * @param options coap option set
	 * @return http converted header list
	 */
	private List<Header> setOtherOptionHeaders(OptionSet options) {

		List<Header> headers=new ArrayList<>();
		for(Option option:options.asSortedList())
		{

			String headerName = HTTP_OTHER_PROPERTIES.getProperty("coap.message.option." + option.getNumber());
			String optionValue;
			if (headerName != null && !headerName.isEmpty()) {
				// format the value
				if (OtherOptionNumberRegistry.getFormatByNr(option.getNumber()) == OptionNumberRegistry.optionFormats.STRING) {
					optionValue = option.getStringValue();
				} else if (OtherOptionNumberRegistry.getFormatByNr(option.getNumber()) == OptionNumberRegistry.optionFormats.INTEGER) {
					optionValue = Integer.toString(option.getIntegerValue());
				} else if (OtherOptionNumberRegistry.getFormatByNr(option.getNumber()) == OptionNumberRegistry.optionFormats.OPAQUE) {
					optionValue = new String(option.getValue());
				} else {
					// if the option is not formattable, skip it
					continue;
				}

				headers.add(new BasicHeader(headerName, optionValue));
			}

		}

		return headers;
	}

	/**
	 *
	 * @param request - coap request
	 * @return Http URL with removed RD resources and added query params
	 */
	public URL getHttpURI(Request request)
	{
		URL proxyUri=null;
		try {
			URI coapUri=new URI(request.getURI());
			String path=coapUri.getPath();
			if (request.getOptions().getProxyUri() == null) {
				proxyUri = new URL(this.getParentNode().getContext() + path
						.substring(this.getParentNode().getParent().getName().length() + 1) + (
						coapUri.getQuery() != null ? "?" + coapUri.getQuery():""));

			} else
				proxyUri = new URL(request.getOptions().getProxyUri());

		} catch (URISyntaxException | MalformedURLException e) {
			LOGGER.log(Level.WARNING,e.getMessage());
		}
		return proxyUri;
	}

	/**
	 * getters & setters
	 */

	public CoAP.Code getResourceCode() {
		return resourceCode;
	}

	public void setResourceCode(String resourceCode) {
		/*TODO - the validation must be moved to setting the attributes*/
		try {
			this.resourceCode = CoAP.Code.valueOf(resourceCode);
		} catch (IllegalArgumentException e) {
			this.resourceCode = CoAP.Code.GET; //default value code of an end resource is 'GET'
		}
	}

}
