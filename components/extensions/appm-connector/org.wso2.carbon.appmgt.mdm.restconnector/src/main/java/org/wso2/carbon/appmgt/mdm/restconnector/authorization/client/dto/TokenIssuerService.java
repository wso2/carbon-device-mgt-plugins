/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.appmgt.mdm.restconnector.authorization.client.dto;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * This hold the api definition that is used as a contract with netflix feign.
 */
@Path("/token")
public interface TokenIssuerService {

    /**
     * Get a token for password grant type.
     * @param grant Token grant type
     * @param username Username
     * @param password Password
     * @return {@link AccessTokenInfo} object
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    AccessTokenInfo getToken(@QueryParam("grant_type") String grant, @QueryParam("username") String username,
                             @QueryParam("password") String password);

    /**
     * Get a token for refresh grant type.
     * @param grant Token grant type
     * @param refreshToken Refresh token
     * @return {@link AccessTokenInfo} object
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    AccessTokenInfo getToken(@QueryParam("grant_type") String grant, @QueryParam("refreshToken") String refreshToken);
}
