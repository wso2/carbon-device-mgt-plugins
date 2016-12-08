/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.scep.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.dto.SCEPResponse;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.scep.service.impl.exception.VirtualFireAlarmException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.scep.service.impl.util.scep.ContentType;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.scep.service.impl.util.scep.SCEPOperation;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.scep.service.impl.util.VirtualFireAlarmServiceUtils;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@SuppressWarnings("Non-Annoted WebService")
public class VirtualFireAlarmScepServerImpl implements VirtualFireAlarmScepServer {

    private static Log log = LogFactory.getLog(VirtualFireAlarmScepServerImpl.class);

    @GET
    @Path("scep")
    public Response scepRequest(@QueryParam("operation") String operation, @QueryParam("message") String message) {
        if (log.isDebugEnabled()) {
            log.debug("Invoking SCEP operation " + operation);
        }
        if (SCEPOperation.GET_CA_CERT.getValue().equals(operation)) {
            if (log.isDebugEnabled()) {
                log.debug("Invoking GetCACert");
            }
            try {
                CertificateManagementService certificateManagementService =
                        VirtualFireAlarmServiceUtils.getCertificateManagementService();
                SCEPResponse scepResponse = certificateManagementService.getCACertSCEP();
                Response.ResponseBuilder responseBuilder;
                switch (scepResponse.getResultCriteria()) {
                    case CA_CERT_FAILED:
                        log.error("CA cert failed");
                        responseBuilder = Response.serverError();
                        break;
                    case CA_CERT_RECEIVED:
                        if (log.isDebugEnabled()) {
                            log.debug("CA certificate received in GetCACert");
                        }
                        responseBuilder = Response.ok(scepResponse.getEncodedResponse(),
                                                      ContentType.X_X509_CA_CERT);
                        break;
                    case CA_RA_CERT_RECEIVED:
                        if (log.isDebugEnabled()) {
                            log.debug("CA and RA certificates received in GetCACert");
                        }
                        responseBuilder = Response.ok(scepResponse.getEncodedResponse(),
                                                      ContentType.X_X509_CA_RA_CERT);
                        break;
                    default:
                        log.error("Invalid SCEP request");
                        responseBuilder = Response.serverError();
                        break;
                }

                return responseBuilder.build();
            } catch (VirtualFireAlarmException e) {
                log.error("Error occurred while enrolling the VirtualFireAlarm device", e);
            } catch (KeystoreException e) {
                log.error("Keystore error occurred while enrolling the VirtualFireAlarm device", e);
            }

        } else if (SCEPOperation.GET_CA_CAPS.getValue().equals(operation)) {

            if (log.isDebugEnabled()) {
                log.debug("Invoking GetCACaps");
            }
            try {
                CertificateManagementService certificateManagementService = VirtualFireAlarmServiceUtils.
                        getCertificateManagementService();
                byte caCaps[] = certificateManagementService.getCACapsSCEP();

                return Response.ok(caCaps, MediaType.TEXT_PLAIN).build();

            } catch (VirtualFireAlarmException e) {
                log.error("Error occurred while enrolling the device", e);
            }
        } else {
            log.error("Invalid SCEP operation " + operation);
        }
        return Response.serverError().build();
    }

    @POST
    @Path("scep")
    public Response scepRequestPost(@QueryParam("operation") String operation, InputStream inputStream) {
        if (log.isDebugEnabled()) {
            log.debug("Invoking SCEP operation " + operation);
        }
        if (SCEPOperation.PKI_OPERATION.getValue().equals(operation)) {
            if (log.isDebugEnabled()) {
                log.debug("Invoking PKIOperation");
            }
            try {
                CertificateManagementService certificateManagementService = VirtualFireAlarmServiceUtils.
                        getCertificateManagementService();
                byte pkiMessage[] = certificateManagementService.getPKIMessageSCEP(inputStream);
                return Response.ok(pkiMessage, ContentType.X_PKI_MESSAGE).build();
            } catch (VirtualFireAlarmException e) {
                log.error("Error occurred while enrolling the device", e);
            } catch (KeystoreException e) {
                log.error("Keystore error occurred while enrolling the device", e);
            }
        }
        return Response.serverError().build();
    }

}
