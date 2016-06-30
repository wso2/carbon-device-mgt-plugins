/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.input.adapter.http.oauth;

import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.wso2.carbon.device.mgt.iot.input.adapter.http.internal.InputAdapterServiceDataHolder;
import org.wso2.carbon.device.mgt.iot.input.adapter.http.util.AuthenticationInfo;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import java.rmi.RemoteException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authenticate use oauth validator
 */
public class OAuthAuthenticator {
    private static String cookie;
    private GenericObjectPool stubs;

    private static final Pattern PATTERN = Pattern.compile("[B|b]earer\\s");
    private static final String TOKEN_TYPE = "bearer";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static Log log = LogFactory.getLog(OAuthAuthenticator.class);

    public OAuthAuthenticator(InputEventAdapterConfiguration eventAdapterConfiguration) {
        this.stubs = new GenericObjectPool(new OAuthTokenValidaterStubFactory(eventAdapterConfiguration));
    }

    public AuthenticationInfo authenticate(HttpServletRequest req) {
        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        String bearerToken = getBearerToken(req);
        if (bearerToken == null) {
            return authenticationInfo;
        }
        try {
            authenticationInfo = validateToken(bearerToken);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("checkAuthentication() fail: " + e.getMessage(), e);
            }
        }
        return authenticationInfo;
    }

    private String getBearerToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader != null) {
            Matcher matcher = PATTERN.matcher(authorizationHeader);
            if (matcher.find()) {
                authorizationHeader = authorizationHeader.substring(matcher.end());
            }
        }
        return authorizationHeader;
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
            throws RemoteException, UserStoreException {
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
            RealmService realmService = InputAdapterServiceDataHolder.getRealmService();
            int tenantId = realmService.getTenantManager().getTenantId(authenticationInfo.getTenantDomain());
            authenticationInfo.setTenantId(tenantId);
            authenticationInfo.setScopes(tokenValidationResponse.getScope());
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
     * This method gets a string accessToken and validates it
     *
     * @param token which need to be validated.
     * @return AuthenticationInfo with the validated results.
     */
    private AuthenticationInfo validateToken(String token) {
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
        authenticationInfo.setTenantId(-1);
        return authenticationInfo;
    }
}
