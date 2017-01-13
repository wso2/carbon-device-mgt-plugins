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

package org.wso2.carbon.device.mgt.mobile.windows.api.services.discovery;

import io.swagger.annotations.*;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.PluginConstants;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsDeviceEnrolmentException;
import org.wso2.carbon.device.mgt.mobile.windows.api.services.discovery.beans.DiscoveryRequest;
import org.wso2.carbon.device.mgt.mobile.windows.api.services.discovery.beans.DiscoveryResponse;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.BindingType;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.soap.SOAPBinding;

/**
 * Interface for Discovery service related operations.
 */

@WebService(targetNamespace = PluginConstants.DISCOVERY_SERVICE_TARGET_NAMESPACE,
        name = "IDiscoveryService")
@BindingType(value = SOAPBinding.SOAP12HTTP_BINDING)
public interface DiscoveryService {
    @POST
    @ApiOperation(
            httpMethod = "POST",
            value = "Discovering the server Enrollment policy and Enrollment service Endpoints.",
            notes = "Using this API to discover the Enrollment policy,Enrollment service and " +
                    "federated login page server endpoints in the server. ",
            response = DiscoveryResponse.class,
            tags = "Windows Device Enrollment.",
            authorizations = {
                    @Authorization(
                            value = "permission",
                            scopes = {@AuthorizationScope(
                                    scope = "/device-mgt/devices/enroll/windows",
                                    description = "Discover the service endpoints.")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "Ok. \n Accepted and getting server endpoints.",
                    response = DiscoveryResponse.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified. \n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n The source can be retrieved from the URLr.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.\n"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while fetching the server endpoints.")
    })
    @RequestWrapper(localName = "Discover", targetNamespace = PluginConstants.DISCOVERY_SERVICE_TARGET_NAMESPACE)
    @WebMethod(operationName = "Discover")
    @ResponseWrapper(localName = "DiscoverResponse", targetNamespace = PluginConstants.DISCOVERY_SERVICE_TARGET_NAMESPACE)
    void discover(
            @WebParam(name = "request", targetNamespace = PluginConstants.DISCOVERY_SERVICE_TARGET_NAMESPACE)
            @ApiParam(
                    name = "DiscoveryRequest",
                    value = "Discovery service SOAP request.")
            DiscoveryRequest request,
            @WebParam(mode = WebParam.Mode.OUT, name = "DiscoverResult",
                    targetNamespace = PluginConstants.DISCOVERY_SERVICE_TARGET_NAMESPACE)
            javax.xml.ws.Holder<DiscoveryResponse> response
    ) throws WindowsDeviceEnrolmentException;

}