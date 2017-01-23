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

package org.wso2.carbon.device.mgt.mobile.windows.api.services.enrollment;

import io.swagger.annotations.*;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.PluginConstants;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WAPProvisioningException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsDeviceEnrolmentException;
import org.wso2.carbon.device.mgt.mobile.windows.api.services.enrollment.beans.AdditionalContext;
import org.wso2.carbon.device.mgt.mobile.windows.api.services.enrollment.beans.RequestSecurityTokenResponse;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ws.rs.POST;
import javax.xml.ws.BindingType;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.soap.SOAPBinding;
import java.io.UnsupportedEncodingException;

@WebService(targetNamespace = PluginConstants.DEVICE_ENROLLMENT_SERVICE_TARGET_NAMESPACE, name = "enrollment")
@BindingType(value = SOAPBinding.SOAP12HTTP_BINDING)
public interface EnrollmentService {

    @RequestWrapper(localName = "RequestSecurityToken", targetNamespace = PluginConstants
            .WS_TRUST_TARGET_NAMESPACE)
    @WebMethod(operationName = "RequestSecurityToken")
    @ResponseWrapper(localName = "RequestSecurityTokenResponseCollection", targetNamespace =
            PluginConstants.WS_TRUST_TARGET_NAMESPACE)
    @POST
    @ApiOperation(
            httpMethod = "POST",
            value = "Signing the certificate signing request(CSR) and provide request security token response.",
            notes = "Using this API to fetching more information to enroll the Device and " +
                    "getting pending operations.",
            tags = "Windows 10 Device Enrollment Service.",
            authorizations = {
                    @Authorization(
                            value = "permission",
                            scopes = {@AuthorizationScope(
                                    scope = "/device-mgt/devices/enroll/windows",
                                    description = "Signing the certificate signing request(CSR) " +
                                            "and provide request security token response")}
                    )
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "Ok.Successfully signed the CSR.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "URL of the activity instance that refers to the scheduled operation."),
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
                    message = "Unsupported media type. \n The format of the requested entity was not supported.\n"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while Signing the CSR.")
    })
    void requestSecurityToken(
            @WebParam(name = "TokenType", targetNamespace = PluginConstants.WS_TRUST_TARGET_NAMESPACE)
            String tokenType,
            @WebParam(name = "RequestType", targetNamespace = PluginConstants.WS_TRUST_TARGET_NAMESPACE)
            String requestType,
            @WebParam(name = "BinarySecurityToken", targetNamespace = PluginConstants
                    .WS_SECURITY_TARGET_NAMESPACE)
            String binarySecurityToken,
            @WebParam(name = "AdditionalContext", targetNamespace = PluginConstants
                    .SOAP_AUTHORIZATION_TARGET_NAMESPACE)
            AdditionalContext additionalContext,
            @WebParam(mode = WebParam.Mode.OUT, name = "RequestSecurityTokenResponse",
                    targetNamespace = PluginConstants.WS_TRUST_TARGET_NAMESPACE)
            javax.xml.ws.Holder<RequestSecurityTokenResponse> response) throws
            WindowsDeviceEnrolmentException, UnsupportedEncodingException,
            WAPProvisioningException;
}
