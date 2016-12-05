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
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicRequestLine;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.proxy.HttpTranslator;
import org.eclipse.californium.proxy.TranslationException;
import org.eclipse.californium.tools.resources.RDNodeResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * resource that add at the end of a path which starts from a NodeResource
 */

public class EndResource extends TagResource {

	private CoAP.Code resourceCode; // an EndResource can handle only one request type [GET/POST/PUT/DELETE]

	public EndResource(String name, boolean visible, RDNodeResource parentNode, String resourceCode) {

		super(name, visible, parentNode);
		if (resourceCode != null)
			setResourceCode(resourceCode);
		else
			setResourceCode("GET"); //default value code of an end resource is 'GET'

	}

	@Override public void handleRequest(Exchange exchange) {
		CoAP.Code code = exchange.getRequest().getCode();

		// TODO the post method is used to extrace auth information since GET method restrict payload

		if (code.equals(CoAP.Code.POST) && resourceCode.equals(CoAP.Code.GET))
			handleGET(new CoapExchange(exchange, this));

		/*above if condition must be removed when a necessary alternative is found*/

		else if (code.equals(resourceCode)) {
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
			}
		} else
			exchange.sendResponse(new Response(CoAP.ResponseCode.METHOD_NOT_ALLOWED));

	}

	@Override
	public void handleGET(CoapExchange exchange) {

		Request request = exchange.advanced().getRequest();
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		HttpRequest httpRequest = null;

		//set http URI by removing 'rd' part in the reousrce path
		URL proxyUri=getHttpURI(request);
		if (request.getOptions().getProxyUri() == null)
			request.getOptions().setProxyUri(proxyUri.toString());

		//get hosts
		HttpHost httpHost = new HttpHost(proxyUri.getHost(), proxyUri.getPort());

		// TODO the post method is used to extrace auth information since GET method restrict payload
		if (request.getCode().equals(CoAP.Code.POST)) {

			//create GET requestline and http request
			RequestLine requestLine = new BasicRequestLine("GET", proxyUri.toString(), HttpVersion.HTTP_1_1);
			httpRequest = new BasicHttpEntityEnclosingRequest(requestLine);

			//set default headers
			Header[] headers = HttpTranslator.getHttpHeaders(request.getOptions().asSortedList());
			for (Header header : headers) {
				httpRequest.addHeader(header);
			}

			//set payload and additional headers
			httpRequest = setHttpPayload(httpRequest, request);

		}
		else {
			try {
				httpRequest = HttpTranslator.getHttpRequest(request);
			} catch (TranslationException e) {
				e.printStackTrace();
			}
		}

		Response coapResponse = null;
		try {
			HttpResponse httpResponse = NodeResource.HTTP_CLIENT.execute(httpHost, httpRequest);
			coapResponse = HttpTranslator.getCoapResponse(httpResponse, request);
		} catch (TranslationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		exchange.respond(coapResponse);
	}


	@Override
	public void handlePOST(CoapExchange exchange) {

		Request request = exchange.advanced().getRequest();
		request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
		HttpRequest httpRequest = null;

		//set http URI by removing 'rd' part in the reousrce path
		URL proxyUri=getHttpURI(request);
		if(request.getOptions().getProxyUri()==null)
			request.getOptions().setProxyUri(proxyUri.toString());

		//get hosts
		HttpHost httpHost = new HttpHost(proxyUri.getHost(), proxyUri.getPort());

		RequestLine requestLine = new BasicRequestLine("POST", proxyUri.toString(), HttpVersion.HTTP_1_1);
		httpRequest = new BasicHttpEntityEnclosingRequest(requestLine);

		//set default headers
		Header[] headers = HttpTranslator.getHttpHeaders(request.getOptions().asSortedList());
		for (Header header : headers) {
			httpRequest.addHeader(header);
		}

		//set payload and additional headers
		httpRequest = setHttpPayload(httpRequest, request);

		Response coapResponse = null;
		try {
			HttpResponse httpResponse = NodeResource.HTTP_CLIENT.execute(httpHost, httpRequest);
			coapResponse = HttpTranslator.getCoapResponse(httpResponse, request);
		} catch (TranslationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		exchange.respond(coapResponse);

	}

	/**
	 * @param httpRequest
	 * @param request     - coap request to be translated
	 * @return - the modified HTTP request with extra headers and entity
	 */
	public HttpRequest setHttpPayload(HttpRequest httpRequest, Request request) {

		//handle json
		if (request.getOptions().getContentFormat() == MediaTypeRegistry.APPLICATION_JSON) {
			ContentType contentType = null;
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> payloadMap = new HashMap<String, Object>();
			try {
				payloadMap = mapper.readValue(request.getPayloadString(), new TypeReference<Map<String, Object>>() {

				});
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (payloadMap.containsKey("header")) {
				Map headerMap = (Map) payloadMap.get("header");
				Iterator iterator = headerMap.keySet().iterator();
				String key;
				String value;
				while (iterator.hasNext()) {
					key = (String) iterator.next();
					value = (String) headerMap.get(key);
					httpRequest.addHeader(setHeader(key, value));
					try {
						if (key.equalsIgnoreCase("Content-Type"))
							contentType = ContentType.parse(value);
					} catch (ParseException e) {
						contentType = ContentType.TEXT_PLAIN;
					}
				}

			}
			if (payloadMap.containsKey("body") && !resourceCode.equals(CoAP.Code.GET)) {
				String value = (String) payloadMap.get("body");
				request.setPayload(value);
			} else
				request.setPayload("");

			HttpEntity httpEntity = null;
			if (contentType != null) {
				httpEntity = new StringEntity(request.getPayloadString(), contentType);
			} else if (request.getPayload().length != 0) {
				httpEntity = new ByteArrayEntity(request.getPayload());
			}

			if (httpEntity != null)
				((BasicHttpEntityEnclosingRequest) httpRequest).setEntity(httpEntity);

		}
		return httpRequest;
	}

/*	public HttpRequest setPayload(HttpRequest request, String payload) {

		final Pattern HEADER_PATTERN = Pattern.compile("[H|h]eader\\{(.*?)\\}"); //e.g. Header{Autherization:"Bearer 785ht9t9t"}
		final String HEADER_TITLE = "Header";
		final Pattern PARAM_PATTERN=Pattern.compile("[P|p]aram\\{(.*?)\\}");
		final String PARAM_TITLE="Param";

		Scanner scanner = new Scanner(payload);
		String line;
		while (scanner.hasNext())//can be used to set other parameters in payload
		{
			if ((line = scanner.findInLine(HEADER_PATTERN)) != null) {
				line = line
						.substring(HEADER_TITLE.length() + 1, line.length() - 1); //trim Header{ & }
				Scanner headerScanner = new Scanner(line);
				String attribute = null;
				while (headerScanner.findWithinHorizon(LinkFormat.DELIMITER, 1) == null
						&& (attribute = headerScanner.findInLine(LinkFormat.WORD)) != null) {
					if (headerScanner.findWithinHorizon(":", 0) != null) {
						String value = null;
						if ((value = headerScanner.findInLine(LinkFormat.QUOTED_STRING)) != null) {
							value = value.substring(1, value.length() - 1); // trim " "
						} else {
							value = headerScanner.next();

						}

						request.addHeader(setHeader(attribute, value));

					}
				}
			}
			if((line = scanner.findInLine(PARAM_PATTERN)) != null)
			{
				line = line.substring(PARAM_TITLE.length() + 1, line.length() - 1); //trim Param{ & }
				Scanner paramScanner = new Scanner(line);
				String attribute = null;
				while (paramScanner.findWithinHorizon(LinkFormat.DELIMITER, 1) == null
						&& (attribute = paramScanner.findInLine(LinkFormat.WORD)) != null) {
					if (paramScanner.findWithinHorizon("=", 0) != null) {
						String value = null;
						if ((value = paramScanner.findInLine(LinkFormat.QUOTED_STRING)) != null) {
							value = value.substring(1, value.length() - 1); // trim " "
						} else {
							value = paramScanner.next();

						}

					}
				}


			}

		}

		return request;
	}
*/
	/**
	 *
	 * @param attribute - header title attribute
	 * @param value - value of the heade attribute
	 * @return - Http header with the attribute and title
	 */
	public Header setHeader(String attribute, String value) {
		Header header = null;

		//header
		final String AUTHORIZATION_HEADER = "Authorization"; //can be used to set other header parameters [only authorization atm]

		//header values
		final Pattern AUTHORIZATION_PATTERN = Pattern.compile("[B|b]earer\\s");

		if (attribute.equalsIgnoreCase(AUTHORIZATION_HEADER)) {
			if (AUTHORIZATION_PATTERN.matcher(value).find()) {
				header = new BasicHeader(AUTHORIZATION_HEADER, value);
			}
		} else
			header = new BasicHeader(attribute,value);

		return header;
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

		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
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
		this.resourceCode = CoAP.Code.valueOf(resourceCode);
	}

}
