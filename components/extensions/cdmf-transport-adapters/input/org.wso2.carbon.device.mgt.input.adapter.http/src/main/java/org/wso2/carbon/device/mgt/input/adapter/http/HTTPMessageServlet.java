/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.input.adapter.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.input.adapter.http.oauth.OAuthAuthenticator;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentInfo;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentTransformer;
import org.wso2.carbon.device.mgt.input.adapter.extension.DefaultContentTransformer;
import org.wso2.carbon.device.mgt.input.adapter.extension.DefaultContentValidator;
import org.wso2.carbon.device.mgt.input.adapter.http.exception.HTTPContentInitializationException;
import org.wso2.carbon.device.mgt.input.adapter.http.jwt.JWTAuthenticator;
import org.wso2.carbon.device.mgt.input.adapter.http.util.AuthenticationInfo;
import org.wso2.carbon.device.mgt.input.adapter.http.util.HTTPEventAdapterConstants;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentValidator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * This will act as the event reciver.
 */
public class HTTPMessageServlet extends HttpServlet {

	private static final String AUTH_MESSAGE_STORE_AUTHENTICATION_INFO = "AUTH_MESSAGE_STORE_AUTHENTICATION_INFO";
	private static final String AUTH_FAILURE_RESPONSE = "_AUTH_FAILURE_";

	private static Log log = LogFactory.getLog(HTTPMessageServlet.class);

	private static ContentValidator contentValidator;
	private static ContentTransformer contentTransformer;
	private InputEventAdapterListener eventAdaptorListener;
	private int tenantId;
	private String exposedTransports;
	private static JWTAuthenticator jwtAuthenticator;
	private static OAuthAuthenticator oAuthAuthenticator;

	public HTTPMessageServlet(InputEventAdapterListener eventAdaptorListener, int tenantId,
							  InputEventAdapterConfiguration eventAdapterConfiguration,
                              Map<String, String> globalProperties) {
		this.eventAdaptorListener = eventAdaptorListener;
		this.tenantId = tenantId;
		this.exposedTransports = eventAdapterConfiguration.getProperties().get(
				HTTPEventAdapterConstants.EXPOSED_TRANSPORTS);

		String className = eventAdapterConfiguration.getProperties().get(
				HTTPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME);
		if (HTTPEventAdapterConstants.DEFAULT.equals(className)) {
			contentValidator = new DefaultContentValidator();
		} else {
			try {
				Class<? extends ContentValidator> contentValidatorClass = Class.forName(className)
						.asSubclass(ContentValidator.class);
				contentValidator = contentValidatorClass.newInstance();
			} catch (ClassNotFoundException e) {
				throw new HTTPContentInitializationException(
						"Unable to find the class validator: " + className, e);
			} catch (InstantiationException e) {
				throw new HTTPContentInitializationException(
						"Unable to create an instance of :" + className, e);
			} catch (IllegalAccessException e) {
				throw new HTTPContentInitializationException("Access of the instance in not allowed.", e);
			}
		}

		String contentTransformerClassName = eventAdapterConfiguration.getProperties().get(
				HTTPEventAdapterConstants.ADAPTER_CONF_CONTENT_TRANSFORMER_CLASSNAME);
		if (contentTransformerClassName != null && contentTransformerClassName.equals(HTTPEventAdapterConstants.DEFAULT)) {
			contentTransformer = new DefaultContentTransformer();
		} else if (contentTransformerClassName != null && !contentTransformerClassName.isEmpty()) {
			try {
				Class<? extends ContentTransformer> contentTransformerClass = Class.forName(contentTransformerClassName)
						.asSubclass(ContentTransformer.class);
				contentTransformer = contentTransformerClass.newInstance();
			} catch (ClassNotFoundException e) {
				throw new HTTPContentInitializationException(
						"Unable to find the class transformer: " + contentTransformerClassName, e);
			} catch (InstantiationException e) {
				throw new HTTPContentInitializationException(
						"Unable to create an instance of :" + contentTransformerClassName, e);
			} catch (IllegalAccessException e) {
				throw new HTTPContentInitializationException("Access of the instance in not allowed.", e);
			}
		}

		jwtAuthenticator = new JWTAuthenticator();
		oAuthAuthenticator = new OAuthAuthenticator(globalProperties);
	}

	@Override
	protected void doPost(HttpServletRequest req,
						  HttpServletResponse res) throws IOException {

		String data = this.inputStreamToString(req.getInputStream());
		if (data == null) {
			log.warn("Event Object is empty/null");
			return;
		}
		AuthenticationInfo authenticationInfo = null;
		if (exposedTransports.equalsIgnoreCase(HTTPEventAdapterConstants.HTTPS)) {
			if (!req.isSecure()) {
				res.setStatus(403);
				log.error("Only Secured endpoint is enabled for requests");
				return;
			} else {
				authenticationInfo = this.checkAuthentication(req);
				int tenantId = authenticationInfo != null ? authenticationInfo.getTenantId() : -1;
				if (tenantId == -1) {
					res.getOutputStream().write(AUTH_FAILURE_RESPONSE.getBytes());
					res.setStatus(401);
					log.error("Authentication failed for the request");
					return;
				} else if (tenantId != this.tenantId) {
					res.getOutputStream().write(AUTH_FAILURE_RESPONSE.getBytes());
					res.setStatus(401);
					log.error("Authentication failed for the request");
					return;
				}
			}
		} else if (exposedTransports.equalsIgnoreCase(HTTPEventAdapterConstants.HTTP)) {
			if (req.isSecure()) {
				res.setStatus(403);
				log.error("Only unsecured endpoint is enabled for requests");
				return;
			}
		} else {
			authenticationInfo = this.checkAuthentication(req);
			int tenantId = authenticationInfo != null ? authenticationInfo.getTenantId() : -1;
			if (tenantId == -1) {
				res.getOutputStream().write(AUTH_FAILURE_RESPONSE.getBytes());
				res.setStatus(401);
				log.error("Authentication failed for the request");
				return;
			} else if (tenantId != this.tenantId) {
				res.getOutputStream().write(AUTH_FAILURE_RESPONSE.getBytes());
				res.setStatus(401);
				log.error("Authentication failed for the request");
				return;
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Message : " + data);
		}

		if (authenticationInfo != null) {
			Map<String, Object> paramMap = new HashMap<>();
			Enumeration<String> reqParameterNames = req.getParameterNames();
			while (reqParameterNames.hasMoreElements()) {
				String paramterName = reqParameterNames.nextElement();
				paramMap.put(paramterName, req.getParameter(paramterName));
			}
			paramMap.put(HTTPEventAdapterConstants.USERNAME_TAG, authenticationInfo.getUsername());
			paramMap.put(HTTPEventAdapterConstants.TENANT_DOMAIN_TAG, authenticationInfo.getTenantDomain());
			paramMap.put(HTTPEventAdapterConstants.SCOPE_TAG, authenticationInfo.getScopes());
			if (contentValidator != null && contentTransformer != null) {
				data = (String) contentTransformer.transform(data, paramMap);
				ContentInfo contentInfo = contentValidator.validate(data, paramMap);
				if (contentInfo != null && contentInfo.isValidContent()) {
					HTTPEventAdapter.executorService.submit(new HTTPRequestProcessor(eventAdaptorListener,
																					 (String) contentInfo.getMessage(), tenantId));
				}
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest req,
						 HttpServletResponse res) throws IOException {
		doPost(req, res);
	}

	public class HTTPRequestProcessor implements Runnable {

		private InputEventAdapterListener inputEventAdapterListener;
		private String payload;
		private int tenantId;

		public HTTPRequestProcessor(InputEventAdapterListener inputEventAdapterListener, String payload, int tenantId) {
			this.inputEventAdapterListener = inputEventAdapterListener;
			this.payload = payload;
			this.tenantId = tenantId;
		}

		public void run() {
			try {
				PrivilegedCarbonContext.startTenantFlow();
				PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
				if (log.isDebugEnabled()) {
					log.debug("Event received in HTTP Event Adapter - " + payload);
				}
				if (payload != null) {
					inputEventAdapterListener.onEvent(payload);
				} else {
					log.warn("Dropping the empty/null event received through http adapter");
				}
			} catch (Exception e) {
				log.error("Error while parsing http request for processing: " + e.getMessage(), e);
			} finally {
				PrivilegedCarbonContext.endTenantFlow();
			}
		}
	}

	private AuthenticationInfo checkAuthentication(HttpServletRequest req) {
		AuthenticationInfo authenticationInfo = (AuthenticationInfo) req.getSession().getAttribute(
				AUTH_MESSAGE_STORE_AUTHENTICATION_INFO);
		if (authenticationInfo != null) {
			return authenticationInfo;
		}
		if (jwtAuthenticator.isJWTHeaderExist(req)) {
			authenticationInfo = jwtAuthenticator.authenticate(req);
		} else {
			authenticationInfo = oAuthAuthenticator.authenticate(req);
		}
		if (authenticationInfo != null) {
			boolean success = authenticationInfo.isAuthenticated();
			if (success) {
				req.getSession().setAttribute(AUTH_MESSAGE_STORE_AUTHENTICATION_INFO, authenticationInfo);
			}
		}
		return authenticationInfo;
	}

	private String inputStreamToString(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int i;
		while ((i = in.read(buff)) > 0) {
			out.write(buff, 0, i);
		}
		out.close();
		return out.toString();
	}

}
