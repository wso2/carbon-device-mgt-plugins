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

package org.wso2.carbon.device.mgt.iot.input.adapter.http.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.device.mgt.iot.input.adapter.http.util.AuthenticationInfo;
import org.wso2.carbon.device.mgt.iot.input.adapter.internal.InputAdapterServiceDataHolder;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import javax.servlet.http.HttpServletRequest;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

/**
 * This authenticator authenticates HTTP requests using JWT header.
 */
public class JWTAuthenticator {

	private static final Log log = LogFactory.getLog(JWTAuthenticator.class);
	public static final String SIGNED_JWT_AUTH_USERNAME = "Username";
	private static final String JWT_ASSERTION_HEADER = "X-JWT-Assertion";

	public boolean isJWTHeaderExist(HttpServletRequest request) {
		String authorizationHeader = request.getHeader(JWT_ASSERTION_HEADER);
		if((authorizationHeader != null) && !authorizationHeader.isEmpty()){
			return true;
		}
		return false;
	}

	public AuthenticationInfo authenticate(HttpServletRequest request) {
	    AuthenticationInfo authenticationInfo = new AuthenticationInfo();
		//Get the filesystem keystore default primary certificate
		KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
		try {
			keyStoreManager.getDefaultPrimaryCertificate();
			String authorizationHeader = request.getHeader(HTTPConstants.HEADER_AUTHORIZATION);
			String headerData = decodeAuthorizationHeader(authorizationHeader);
			JWSVerifier verifier =
					new RSASSAVerifier((RSAPublicKey) keyStoreManager.getDefaultPublicKey());
			SignedJWT jwsObject = SignedJWT.parse(headerData);
			if (jwsObject.verify(verifier)) {
				String username = jwsObject.getJWTClaimsSet().getStringClaim(SIGNED_JWT_AUTH_USERNAME);
				String tenantDomain = MultitenantUtils.getTenantDomain(username);
				username = MultitenantUtils.getTenantAwareUsername(username);
				TenantManager tenantManager = InputAdapterServiceDataHolder.getRealmService().
                        getTenantManager();
				int tenantId = tenantManager.getTenantId(tenantDomain);
				if (tenantId == -1) {
					log.error("tenantDomain is not valid. username : " + username + ", tenantDomain " +
					          ": " + tenantDomain);
				} else {
                    UserStoreManager userStore = InputAdapterServiceDataHolder.getRealmService().
                            getTenantUserRealm(tenantId).getUserStoreManager();
                    if (userStore.isExistingUser(username)) {
                        authenticationInfo.setTenantId(tenantId);
                        authenticationInfo.setUsername(username);
                        authenticationInfo.setTenantDomain(tenantDomain);
						authenticationInfo.setAuthenticated(true);
                    }
                }
    		}
		} catch (UserStoreException e) {
			log.error("Error occurred while obtaining the user.", e);
		} catch (ParseException e) {
			log.error("Error occurred while parsing the JWT header.", e);
		} catch (JOSEException e) {
			log.error("Error occurred while verifying the JWT header.", e);
		} catch (Exception e) {
			log.error("Error occurred while verifying the JWT header.", e);
		}
		return authenticationInfo;
	}

	private String decodeAuthorizationHeader(String authorizationHeader) {

		if(authorizationHeader == null) {
			return null;
		}

		String[] splitValues = authorizationHeader.trim().split(" ");
		byte[] decodedBytes = Base64Utils.decode(splitValues[1].trim());
		if (decodedBytes != null) {
			return new String(decodedBytes);
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Error decoding authorization header.");
			}
			return null;
		}
	}

}
