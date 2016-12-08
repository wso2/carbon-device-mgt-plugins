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

package org.wso2.carbon.device.mgt.output.adapter.websocket.endpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authentication.AuthenticationInfo;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authentication.Authenticator;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authorization.Authorizer;
import org.wso2.carbon.device.mgt.output.adapter.websocket.endpoint.constants.Constants;
import org.wso2.carbon.device.mgt.output.adapter.websocket.endpoint.util.ServiceHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Connect to web socket with Super tenant
 */

@ServerEndpoint(value = "/{streamname}/{version}", configurator = HttpSessionConfigurator.class)
public class SuperTenantSubscriptionEndpoint extends SubscriptionEndpoint {

	private static final Log log = LogFactory.getLog(SuperTenantSubscriptionEndpoint.class);

	/**
	 * Web socket onOpen - When client sends a message
	 *
	 * @param session    - Users registered session.
	 * @param streamName - StreamName extracted from the ws url.
	 * @param version    -  Version extracted from the ws url.
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config, @PathParam("streamname") String streamName,
					   @PathParam("version") String version) {
		if (log.isDebugEnabled()) {
			log.debug("WebSocket opened, for Session id: " + session.getId() + ", for the Stream:" + streamName);
		}
		Map<String, List<String>> httpHeaders;
		httpHeaders = (Map<String, List<String>>) config.getUserProperties().get(Constants.HTTP_HEADERS);
		Authenticator authenticator = ServiceHolder.getWebsocketValidationService().getAuthenticator();
		AuthenticationInfo authenticationInfo = authenticator.isAuthenticated(httpHeaders);
		if (authenticationInfo != null && authenticationInfo.isAuthenticated()) {
			Authorizer authorizer = ServiceHolder.getWebsocketValidationService().getAuthorizer();
			boolean isAuthorized = authorizer.isAuthorized(authenticationInfo, session, streamName);
			if (isAuthorized) {
				try {
					PrivilegedCarbonContext.startTenantFlow();
					PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
							MultitenantConstants.SUPER_TENANT_ID);
					ServiceHolder.getInstance().getUiOutputCallbackControllerService().subscribeWebsocket(streamName,
																										  version,
																										  session);
				} finally {
					PrivilegedCarbonContext.endTenantFlow();
				}
			} else {
				log.info("Failed to authorize the connection for the stream : "+ streamName+" , version : "+ version);
			}
		} else {
			try {
				session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Unauthorized Access"));
			} catch (IOException e) {
				log.error("Failed to disconnect the unauthorized client.", e);
			}
		}
	}

	/**
	 * Web socket onMessage - When client sens a message
	 *
	 * @param session    - Users registered session.
	 * @param message    - Status code for web-socket close.
	 * @param streamName - StreamName extracted from the ws url.
	 */
	@OnMessage
	public void onMessage(Session session, String message, @PathParam("streamname") String streamName) {
		if (log.isDebugEnabled()) {
			log.debug("Received and dropped message from client. Message: " + message + ", " +
							  "for Session id: " + session.getId() + ", for the Stream:" + streamName);
		}
	}

	/**
	 * Web socket onClose - Remove the registered sessions
	 *
	 * @param session    - Users registered session.
	 * @param reason     - Status code for web-socket close.
	 * @param streamName - StreamName extracted from the ws url.
	 * @param version    - Version extracted from the ws url.
	 */
	@OnClose
	public void onClose(Session session, CloseReason reason, @PathParam("streamname") String streamName,
						@PathParam("version") String version) {
		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
			super.onClose(session, reason, streamName, version);
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}

	/**
	 * Web socket onError - Remove the registered sessions
	 *
	 * @param session    - Users registered session.
	 * @param throwable  - Status code for web-socket close.
	 * @param streamName - StreamName extracted from the ws url.
	 * @param version    - Version extracted from the ws url.
	 */
	@OnError
	public void onError(Session session, Throwable throwable, @PathParam("streamname") String streamName,
						@PathParam("version") String version) {
		try {
			PrivilegedCarbonContext.startTenantFlow();
			PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
			super.onError(session, throwable, streamName, version);
		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}
	}

}
