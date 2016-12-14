/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.mobile.windows.api.services.authbst;

import io.swagger.annotations.*;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsDeviceEnrolmentException;
import org.wso2.carbon.device.mgt.mobile.windows.api.services.authbst.beans.Credentials;

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Interface for handling authentication request comes via MDM login page.
 */

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "Windows Binary security token provider"),
                                @ExtensionProperty(name = "context",
                                        value = "/api/device-mgt/windows/v1.0/federated"),
                        })
                }
        ),
        tags = {
                @Tag(name = "devicemgt_windows", description = "")
        }
)
@Api(value = "Windows BST Management",
        description = "This carries all the resources related to Windows Binary security token management.")
@WebService
@Path("/bst")
@Produces({"application/json", "application/xml"})
@Consumes({"application/json", "application/xml"})
public interface BSTProvider {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/authentication")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Getting Binary security token.",
            notes = "Using this API to fetch Binary security token to call window enrollment and policy endpoints.",
            tags = "BST Provider",
            authorizations = {
                    @Authorization(
                            value = "permission",
                            scopes = {@AuthorizationScope(scope = "/device-mgt/devices/enroll/windows",
                                    description = "Getting Binary security token for Windows enrollment " +
                                            "and policy endpoints.")}
                    )
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Ok. \n Successfully fetching the binary security token.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The URL of the added policy."),
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests.")
                            }),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n The source can be retrieved from the URL specified in the location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error."),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The format of the requested entity was not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while fetching Binary security token.")
            })
    Response getBST(Credentials credentials)
            throws WindowsDeviceEnrolmentException;
}
