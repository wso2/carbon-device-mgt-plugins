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

package org.wso2.carbon.device.mgt.mobile.windows.api.services.enrollment.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Message;
import org.w3c.dom.*;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementServiceImpl;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.PluginConstants;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.beans.CacheEntry;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.CertificateGenerationException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.SyncmlMessageFormatException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WAPProvisioningException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WindowsDeviceEnrolmentException;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.util.DeviceUtil;
import org.wso2.carbon.device.mgt.mobile.windows.api.common.util.WindowsAPIUtils;
import org.wso2.carbon.device.mgt.mobile.windows.api.operations.util.SyncmlCredentialUtil;
import org.wso2.carbon.device.mgt.mobile.windows.api.services.enrollment.EnrollmentService;
import org.wso2.carbon.device.mgt.mobile.windows.api.services.enrollment.beans.*;
import org.wso2.carbon.device.mgt.mobile.windows.api.services.syncml.beans.WindowsDevice;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingType;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;


/**
 * Implementation class of Windows10 Enrollment process.
 */
@WebService(endpointInterface = PluginConstants.ENROLLMENT_SERVICE_ENDPOINT,
        targetNamespace = PluginConstants.DEVICE_ENROLLMENT_SERVICE_TARGET_NAMESPACE)
@Addressing(enabled = true, required = true)
@BindingType(value = SOAPBinding.SOAP12HTTP_BINDING)
public class EnrollmentServiceImpl implements EnrollmentService {
    private static Log log = LogFactory.getLog(EnrollmentServiceImpl.class);
    private X509Certificate rootCACertificate;
    private String pollingFrequency;
    private String provisioningURL;
    private String domain;

    @Resource
    private WebServiceContext context;

    @Override
    public void requestSecurityToken(String tokenType, String requestType, String binarySecurityToken,
                                     AdditionalContext additionalContext,
                                     Holder<RequestSecurityTokenResponse> response)
            throws WindowsDeviceEnrolmentException, UnsupportedEncodingException, WAPProvisioningException {

        String headerBinarySecurityToken = null;
        String headerTo = null;
        String encodedWap;
        List<Header> headers = getHeaders();
        for (Header headerElement : headers != null ? headers : null) {
            String nodeName = headerElement.getName().getLocalPart();
            if (PluginConstants.SECURITY.equals(nodeName)) {
                Element element = (Element) headerElement.getObject();
                headerBinarySecurityToken = element.getFirstChild().getFirstChild().getTextContent();
            }
            if (PluginConstants.TO.equals(nodeName)) {
                Element toElement = (Element) headerElement.getObject();
                headerTo = toElement.getFirstChild().getTextContent();
            }
        }
        try {
            enrollDevice(additionalContext, headerBinarySecurityToken);
        } catch (DeviceManagementException e) {
            throw new WindowsDeviceEnrolmentException("Error occurred while enrolling the device.");
        } catch (PolicyManagementException e) {
            throw new WindowsDeviceEnrolmentException("Error occurred while enforcing windows policies.");
        }
        String[] splitEmail = headerTo.split("(/ENROLLMENTSERVER)");
        String email = splitEmail[PluginConstants.CertificateEnrolment.EMAIL_SEGMENT];

        String[] splitDomain = email.split("(EnterpriseEnrollment.)");
        domain = splitDomain[PluginConstants.CertificateEnrolment.DOMAIN_SEGMENT];
        provisioningURL = PluginConstants.CertificateEnrolment.ENROLL_SUBDOMAIN + domain +
                PluginConstants.CertificateEnrolment.SYNCML_PROVISIONING_WIN10_SERVICE_URL;

        List<ConfigurationEntry> tenantConfigurations;
        try {
            if ((tenantConfigurations = WindowsAPIUtils.getTenantConfigurationData()) != null) {
                for (ConfigurationEntry configurationEntry : tenantConfigurations) {
                    if ((PluginConstants.TenantConfigProperties.NOTIFIER_FREQUENCY.equals(
                            configurationEntry.getName()))) {
                        pollingFrequency = configurationEntry.getValue().toString();
                    } else {
                        pollingFrequency = PluginConstants.TenantConfigProperties.DEFAULT_FREQUENCY;
                    }
                }
            } else {
                pollingFrequency = PluginConstants.TenantConfigProperties.DEFAULT_FREQUENCY;
                String msg = "Tenant configurations are not initialized yet.";
                log.error(msg);
            }
            ServletContext ctx = (ServletContext) context.getMessageContext().
                    get(MessageContext.SERVLET_CONTEXT);
            File wapProvisioningFile = (File) ctx.getAttribute(PluginConstants.CONTEXT_WAP_PROVISIONING_FILE);
            if (log.isDebugEnabled()) {
                log.debug("Received CSR from Device:" + binarySecurityToken);
            }

            String wapProvisioningFilePath = wapProvisioningFile.getPath();
            RequestSecurityTokenResponse requestSecurityTokenResponse = new RequestSecurityTokenResponse();
            requestSecurityTokenResponse.setTokenType(PluginConstants.CertificateEnrolment.TOKEN_TYPE);

            encodedWap = prepareWapProvisioningXML(binarySecurityToken, wapProvisioningFilePath,
                    headerBinarySecurityToken);
            RequestedSecurityToken requestedSecurityToken = new RequestedSecurityToken();
            BinarySecurityToken binarySecToken = new BinarySecurityToken();
            binarySecToken.setValueType(PluginConstants.CertificateEnrolment.VALUE_TYPE);
            binarySecToken.setEncodingType(PluginConstants.CertificateEnrolment.ENCODING_TYPE);
            binarySecToken.setToken(encodedWap);
            requestedSecurityToken.setBinarySecurityToken(binarySecToken);
            requestSecurityTokenResponse.setRequestedSecurityToken(requestedSecurityToken);
            requestSecurityTokenResponse.setRequestID(PluginConstants.CertificateEnrolment.REQUEST_ID);
            response.value = requestSecurityTokenResponse;
        } catch (CertificateGenerationException e) {
            String msg = "Problem occurred while generating certificate.";
            log.error(msg, e);
            throw new WindowsDeviceEnrolmentException(msg, e);
        } catch (WAPProvisioningException e) {
            String msg = "Problem occurred while generating wap-provisioning file.";
            log.error(msg, e);
            throw new WindowsDeviceEnrolmentException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting tenant configurations.";
            log.error(msg);
            throw new WindowsDeviceEnrolmentException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();

        }
    }

    /**
     * Method used to Convert the Document object into a String.
     *
     * @param document - Wap provisioning XML document
     * @return - String representation of wap provisioning XML document
     * @throws TransformerException
     */
    private String convertDocumentToString(Document document) throws TransformerException {
        DOMSource DOMSource = new DOMSource(document);
        StringWriter stringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(stringWriter);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(DOMSource, streamResult);

        return stringWriter.toString();
    }

    /**
     * This method prepares the wap-provisioning file by including relevant certificates etc.
     *
     * @param binarySecurityToken     - CSR from device
     * @param wapProvisioningFilePath - File path of wap-provisioning file
     * @return - base64 encoded final wap-provisioning file as a String
     * @throws CertificateGenerationException
     * @throws org.wso2.carbon.device.mgt.mobile.windows.api.common.exceptions.WAPProvisioningException
     */
    private String prepareWapProvisioningXML(String binarySecurityToken, String wapProvisioningFilePath,
                                             String headerBst) throws CertificateGenerationException,
            WAPProvisioningException,
            WindowsDeviceEnrolmentException {
        String rootCertEncodedString;
        String signedCertEncodedString;
        X509Certificate signedCertificate;
        String provisioningXmlString;

        CertificateManagementServiceImpl certMgtServiceImpl = CertificateManagementServiceImpl.getInstance();
        Base64 base64Encoder = new Base64();
        try {
            rootCACertificate = (X509Certificate) certMgtServiceImpl.getCACertificate();
            rootCertEncodedString = base64Encoder.encodeAsString(rootCACertificate.getEncoded());


            signedCertificate = certMgtServiceImpl.getSignedCertificateFromCSR(binarySecurityToken);
            signedCertEncodedString = base64Encoder.encodeAsString(signedCertificate.getEncoded());

            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;

            builder = domFactory.newDocumentBuilder();
            Document document = builder.parse(wapProvisioningFilePath);
            NodeList wapParm = document.getElementsByTagName(PluginConstants.CertificateEnrolment.PARM);
            Node caCertificatePosition = wapParm.item(PluginConstants.CertificateEnrolment.CA_CERTIFICATE_POSITION);

            //Adding SHA1 CA certificate finger print to wap-provisioning xml.
            caCertificatePosition.getParentNode().getAttributes().getNamedItem(PluginConstants.
                    CertificateEnrolment.TYPE).setTextContent(String.valueOf(
                    DigestUtils.sha1Hex(rootCACertificate.getEncoded())).toUpperCase());
            //Adding encoded CA certificate to wap-provisioning file after removing new line
            // characters.
            NamedNodeMap rootCertAttributes = caCertificatePosition.getAttributes();
            Node rootCertNode =
                    rootCertAttributes.getNamedItem(PluginConstants.CertificateEnrolment.VALUE);
            rootCertEncodedString = rootCertEncodedString.replaceAll("\n", "");
            rootCertNode.setTextContent(rootCertEncodedString);

            if (log.isDebugEnabled()) {
                log.debug("Root certificate: " + rootCertEncodedString);
            }

            Node signedCertificatePosition = wapParm.item(PluginConstants.CertificateEnrolment.
                    SIGNED_CERTIFICATE_POSITION);

            //Adding SHA1 signed certificate finger print to wap-provisioning xml.
            signedCertificatePosition.getParentNode().getAttributes().getNamedItem(PluginConstants.
                    CertificateEnrolment.TYPE).setTextContent(String.valueOf(
                    DigestUtils.sha1Hex(signedCertificate.getEncoded())).toUpperCase());

            //Adding encoded signed certificate to wap-provisioning file after removing new line
            // characters.
            NamedNodeMap clientCertAttributes = signedCertificatePosition.getAttributes();
            Node clientEncodedNode =
                    clientCertAttributes.getNamedItem(PluginConstants.CertificateEnrolment.VALUE);
            signedCertEncodedString = signedCertEncodedString.replaceAll("\n", "");

            clientEncodedNode.setTextContent(signedCertEncodedString);
            if (log.isDebugEnabled()) {
                log.debug("Signed certificate: " + signedCertEncodedString);
            }

            //Adding domainName to wap-provisioning xml.
            Node domainPosition = wapParm.item(PluginConstants.CertificateEnrolment.DOMAIN_POSITION);
            NamedNodeMap domainAttribute = domainPosition.getAttributes();
            Node domainNode = domainAttribute.getNamedItem(PluginConstants.CertificateEnrolment.VALUE);
            domainNode.setTextContent(domain);

            //Adding Next provisioning service URL to wap-provisioning xml.
            Node syncmlServicePosition = wapParm.item(PluginConstants.CertificateEnrolment.
                    SYNCML_PROVISIONING_ADDR_POSITION);
            NamedNodeMap syncmlServiceAttribute = syncmlServicePosition.getAttributes();
            Node syncmlServiceNode = syncmlServiceAttribute.getNamedItem(PluginConstants.CertificateEnrolment.VALUE);
            syncmlServiceNode.setTextContent(provisioningURL);

            // Adding user name auth token to wap-provisioning xml.
            Node userNameAuthPosition = wapParm.item(PluginConstants.CertificateEnrolment.APPAUTH_USERNAME_POSITION);
            NamedNodeMap appServerAttribute = userNameAuthPosition.getAttributes();
            Node authNameNode = appServerAttribute.getNamedItem(PluginConstants.CertificateEnrolment.VALUE);
            String userName = getRequestedUser(headerBst);
            //CacheEntry cacheEntry = (CacheEntry) DeviceUtil.getCacheEntry(headerBst);
            // String userName = cacheEntry.getUsername();
            authNameNode.setTextContent(userName);
            DeviceUtil.removeToken(headerBst);
            String password = DeviceUtil.generateRandomToken();
            Node passwordAuthPosition = wapParm.item(PluginConstants.CertificateEnrolment.APPAUTH_PASSWORD_POSITION);
            NamedNodeMap appSrvPasswordAttribute = passwordAuthPosition.getAttributes();
            Node authPasswordNode = appSrvPasswordAttribute.getNamedItem(PluginConstants.CertificateEnrolment.VALUE);
            authPasswordNode.setTextContent(password);
            String requestSecurityTokenResponse = SyncmlCredentialUtil.generateRST(userName, password);
            DeviceUtil.persistChallengeToken(requestSecurityTokenResponse, null, userName);

            // Get device polling frequency from the tenant Configurations.
            Node numberOfFirstRetries = wapParm.item(PluginConstants.CertificateEnrolment.POLLING_FREQUENCY_POSITION);
            NamedNodeMap pollingAttributes = numberOfFirstRetries.getAttributes();
            Node pollValue = pollingAttributes.getNamedItem(PluginConstants.CertificateEnrolment.VALUE);
            pollValue.setTextContent(pollingFrequency);
            provisioningXmlString = convertDocumentToString(document);

        } catch (ParserConfigurationException e) {
            throw new WAPProvisioningException("Problem occurred while creating configuration request", e);
        } catch (CertificateEncodingException e) {
            throw new WindowsDeviceEnrolmentException("Error occurred while encoding certificates.", e);
        } catch (SAXException e) {
            throw new WAPProvisioningException("Error occurred while parsing wap-provisioning.xml file.", e);
        } catch (TransformerException e) {
            throw new WAPProvisioningException("Error occurred while transforming wap-provisioning.xml file.", e);
        } catch (IOException e) {
            throw new WAPProvisioningException("Error occurred while getting wap-provisioning.xml file.", e);
        } catch (SyncmlMessageFormatException e) {
            throw new WindowsDeviceEnrolmentException("Error occurred while generating password hash value.", e);
        } catch (KeystoreException e) {
            throw new CertificateGenerationException("CA certificate cannot be generated.", e);
        }
        return base64Encoder.encodeAsString(provisioningXmlString.getBytes());
    }

    /**
     * This method get the soap request header contents.
     *
     * @return List of SOAP headers.
     */
    private List<Header> getHeaders() {
        MessageContext messageContext = context.getMessageContext();
        if (messageContext == null || !(messageContext instanceof WrappedMessageContext)) {
            return null;
        }
        Message message = ((WrappedMessageContext) messageContext).getWrappedMessage();
        return CastUtils.cast((List<?>) message.get(Header.HEADER_LIST));
    }

    /**
     * This method to getting RSTR requested user from the Cache.
     *
     * @param bst Binary Security token which has given from BST Endpoint.
     * @return User for given token.
     */
    private String getRequestedUser(String bst) {
        CacheEntry cacheEntry = (CacheEntry) DeviceUtil.getCacheEntry(bst);
        String userName = cacheEntry.getUsername();
        return userName;
    }

    /**
     * This Method to generate windows device.
     *
     * @param windowsDevice Requested Device with properties.
     * @return Value added Device.
     */
    private Device generateDevice(WindowsDevice windowsDevice) {

        Device generatedDevice = new Device();

        Device.Property OSVersionProperty = new Device.Property();
        OSVersionProperty.setName(PluginConstants.SyncML.OS_VERSION);
        OSVersionProperty.setValue(windowsDevice.getOsVersion());

        Device.Property IMSEIProperty = new Device.Property();
        IMSEIProperty.setName(PluginConstants.SyncML.IMSI);
        IMSEIProperty.setValue(windowsDevice.getImsi());

        Device.Property IMEIProperty = new Device.Property();
        IMEIProperty.setName(PluginConstants.SyncML.IMEI);
        IMEIProperty.setValue(windowsDevice.getImei());

        List<Device.Property> propertyList = new ArrayList<>();
        propertyList.add(OSVersionProperty);
        propertyList.add(IMSEIProperty);
        propertyList.add(IMEIProperty);

        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setOwner(windowsDevice.getUser());
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
        enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);

        generatedDevice.setEnrolmentInfo(enrolmentInfo);
        generatedDevice.setDeviceIdentifier(windowsDevice.getDeviceId());
        generatedDevice.setProperties(propertyList);
        generatedDevice.setType(windowsDevice.getDeviceType());
        generatedDevice.setName(windowsDevice.getDeviceName());

        return generatedDevice;
    }

    /**
     * This method to enroll windows10 Device.
     *
     * @param requestContextItems       Context values to enroll the device.
     * @param headerBinarySecurityToken SOAP request header value to identify the user.
     * @throws DeviceManagementException Exception occurs while enrolling the Device.
     * @throws PolicyManagementException Exception occurs while getting effective policies.
     */
    private void enrollDevice(AdditionalContext requestContextItems, String headerBinarySecurityToken)
            throws DeviceManagementException, PolicyManagementException {
        WindowsDevice windowsDevice = new WindowsDevice();
        windowsDevice.setDeviceType(DeviceManagementConstants.MobileDeviceTypes.
                MOBILE_DEVICE_TYPE_WINDOWS);
        windowsDevice.setUser(getRequestedUser(headerBinarySecurityToken));
        List<ContextItem> contextItems = requestContextItems.getcontextitem();
        for (int x = 0; x < contextItems.size(); x++) {
            switch (x) {
                case PluginConstants.WindowsEnrollmentProperties.WIN_DEVICE_NAME:
                    windowsDevice.setDeviceName(contextItems.get(x).getValue());
                case PluginConstants.WindowsEnrollmentProperties.WIN_DEVICE_IMEI:
                    windowsDevice.setImei(contextItems.get(x).getValue());
                case PluginConstants.WindowsEnrollmentProperties.WIN_DEVICE_ID:
                    windowsDevice.setDeviceId(contextItems.get(x).getValue());
                case PluginConstants.WindowsEnrollmentProperties.WIN_DEVICE_VERSION:
                    windowsDevice.setOsVersion(contextItems.get(x).getValue());
            }
        }
        Device device = generateDevice(windowsDevice);
        WindowsAPIUtils.getDeviceManagementService().enrollDevice(device);
        PolicyManagerService policyManagerService = WindowsAPIUtils.getPolicyManagerService();
        policyManagerService.getEffectivePolicy(new DeviceIdentifier(windowsDevice.getDeviceId(), device.getType()));

    }
}

