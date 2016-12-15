package org.wso2.carbon.device.mgt.mobile.windows.api.services.enrollment;

import org.wso2.carbon.device.mgt.mobile.windows.api.common.PluginConstants;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WAPProvisioningException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsDeviceEnrolmentException;
import org.wso2.carbon.device.mgt.mobile.windows.api.services.enrollment.beans.AdditionalContext;
import org.wso2.carbon.device.mgt.mobile.windows.api.services.enrollment.beans.RequestSecurityTokenResponse;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
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