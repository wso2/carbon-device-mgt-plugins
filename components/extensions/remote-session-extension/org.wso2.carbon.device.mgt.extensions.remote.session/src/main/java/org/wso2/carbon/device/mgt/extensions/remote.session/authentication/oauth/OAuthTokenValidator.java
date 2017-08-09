/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package org.wso2.carbon.device.mgt.extensions.remote.session.authentication.oauth;

import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.wso2.carbon.device.mgt.extensions.remote.session.authentication.AuthenticationInfo;
import org.wso2.carbon.device.mgt.extensions.remote.session.constants.RemoteSessionConstants;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;


import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This acts as a contract point for OAuth token validation.
 */
public class OAuthTokenValidator {

	private static String cookie;
	private GenericObjectPool stubs;
	private static Log log = LogFactory.getLog(OAuthTokenValidator.class);
	private static final String COOKIE_KEY_VALUE_SEPERATOR = "=";
	private static final String COOKIE_KEYPAIR_SEPERATOR = ";";
	private static final String COOKIE = "cookie";
	private static final String TOKEN_TYPE = "bearer";
	private static final String TOKEN_IDENTIFIER = "websocketToken";
	private static final String QUERY_STRING_SEPERATOR = "&";
	private static final String QUERY_KEY_VALUE_SEPERATOR = "=";
	private static final String QUERY_STRING = "queryString";
	private static OAuthTokenValidator oAuthTokenValidator;


	public OAuthTokenValidator(Map<String, String> globalProperties) {
		this.stubs = new GenericObjectPool(new OAuthTokenValidatorStubFactory(globalProperties));
	}

	/**
	 * This method gets a string accessToken and validates it
	 * @param webSocketConnectionProperties WebSocket connection information including http headers
	 * @return AuthenticationInfo with the validated results.
	 */
	public AuthenticationInfo validateToken(Map<String, List<String>> webSocketConnectionProperties) {
		String token = getTokenFromSession(webSocketConnectionProperties);
		if (token == null) {
			AuthenticationInfo authenticationInfo = new AuthenticationInfo();
			authenticationInfo.setAuthenticated(false);
			return authenticationInfo;
		}
		OAuth2TokenValidationServiceStub tokenValidationServiceStub = null;
		try {
			Object stub = this.stubs.borrowObject();
			if (stub != null) {
				tokenValidationServiceStub = (OAuth2TokenValidationServiceStub) stub;
				if (cookie != null) {
					tokenValidationServiceStub._getServiceClient().getOptions().setProperty(
							HTTPConstants.COOKIE_STRING, cookie);
				}
				return getAuthenticationInfo(token, tokenValidationServiceStub);
			} else {
				log.warn("Stub initialization failed.");
			}
		} catch (RemoteException e) {
			log.error("Error on connecting with the validation endpoint.", e);
		} catch (Exception e) {
			log.error("Error occurred in borrowing an validation stub from the pool.", e);

		} finally {
			try {
				if (tokenValidationServiceStub != null) {
					this.stubs.returnObject(tokenValidationServiceStub);
				}
			} catch (Exception e) {
				log.warn("Error occurred while returning the object back to the oauth token validation service " +
								 "stub pool.", e);
			}
		}
		AuthenticationInfo authenticationInfo = new AuthenticationInfo();
		authenticationInfo.setAuthenticated(false);
		return authenticationInfo;
	}

	/**
	 * This creates an AuthenticationInfo object that is used for authorization. This method will validate the token
	 * and
	 * sets the required parameters to the object.
	 *
	 * @param token                      that needs to be validated.
	 * @param tokenValidationServiceStub stub that is used to call the external service.
	 * @return AuthenticationInfo This contains the information related to authenticated client.
	 * @throws RemoteException that triggers when failing to call the external service..
	 */
	private AuthenticationInfo getAuthenticationInfo(String token,
													 OAuth2TokenValidationServiceStub tokenValidationServiceStub)
			throws RemoteException {
		AuthenticationInfo authenticationInfo = new AuthenticationInfo();
		OAuth2TokenValidationRequestDTO validationRequest = new OAuth2TokenValidationRequestDTO();
		OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessToken =
				new OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
		accessToken.setTokenType(TOKEN_TYPE);
		accessToken.setIdentifier(token);
		validationRequest.setAccessToken(accessToken);
		boolean authenticated;
		OAuth2TokenValidationResponseDTO tokenValidationResponse;
		tokenValidationResponse = tokenValidationServiceStub.validate(validationRequest);
		if (tokenValidationResponse == null) {
			authenticationInfo.setAuthenticated(false);
			return authenticationInfo;
		}
		authenticated = tokenValidationResponse.getValid();
		if (authenticated) {
			String authorizedUser = tokenValidationResponse.getAuthorizedUser();
			String username = MultitenantUtils.getTenantAwareUsername(authorizedUser);
			String tenantDomain = MultitenantUtils.getTenantDomain(authorizedUser);
			authenticationInfo.setUsername(username);
			authenticationInfo.setTenantDomain(tenantDomain);
			String scopes[] = tokenValidationResponse.getScope();
			if (scopes != null) {
				Map<String, Object> properties = new HashMap<>();
				properties.put(RemoteSessionConstants.SCOPE_IDENTIFIER, scopes);
				authenticationInfo.setProperties(properties);
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Token validation failed for token: " + token);
			}
		}
		ServiceContext serviceContext = tokenValidationServiceStub._getServiceClient()
				.getLastOperationContext().getServiceContext();
		cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
		authenticationInfo.setAuthenticated(authenticated);
		return authenticationInfo;
	}

	/**
	 * Retrieving the token from the http header
	 * @param webSocketConnectionProperties WebSocket connection information including http headers
	 * @return retrieved token
	 */
	private String getToken(Map<String, List<String>> webSocketConnectionProperties) {
		String cookieString = webSocketConnectionProperties.get(COOKIE).get(0);
 		String[] properties = cookieString.split(COOKIE_KEYPAIR_SEPERATOR);
 		String token;
		for (String keyValuePair: properties) {
            if(TOKEN_IDENTIFIER.equals((keyValuePair.split(COOKIE_KEY_VALUE_SEPERATOR)[0]).trim())){
                token = (keyValuePair.split(COOKIE_KEY_VALUE_SEPERATOR)[1]).trim();
				return token;
            }
        }
		log.error("WebSocket token should be specified in cookie");
		return null;
	}

	/**
	 * Retrieving the token from the http session
	 * @param webSocketConnectionProperties WebSocket connection information including http headers
	 * @return retrieved token
	 */
	private String getTokenFromSession(Map<String, List<String>> webSocketConnectionProperties) {
		String queryString = webSocketConnectionProperties.get(QUERY_STRING).get(0);
		if (queryString != null) {
			String[] allQueryParamPairs = queryString.split(QUERY_STRING_SEPERATOR);
			for (String keyValuePair : allQueryParamPairs) {
				String[] queryParamPair = keyValuePair.split(QUERY_KEY_VALUE_SEPERATOR);
				if (queryParamPair.length != 2) {
					log.warn("Invalid query string [" + queryString + "] passed in.");
					break;
				}
				if (queryParamPair[0].equals(TOKEN_IDENTIFIER)) {
					return queryParamPair[1];
				}
			}
		}
		return null;
	}
}
