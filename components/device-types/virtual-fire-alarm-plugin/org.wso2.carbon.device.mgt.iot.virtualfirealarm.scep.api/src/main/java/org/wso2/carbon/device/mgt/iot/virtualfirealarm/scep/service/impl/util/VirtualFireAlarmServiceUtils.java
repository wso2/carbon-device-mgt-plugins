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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.scep.service.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.scep.service.impl.exception.VirtualFireAlarmException;

import java.lang.*;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 *
 */
public class VirtualFireAlarmServiceUtils {
    private static final Log log = LogFactory.getLog(VirtualFireAlarmServiceUtils.class);

    /**
     *
     * @return
     * @throws VirtualFireAlarmException
     */
    public static CertificateManagementService getCertificateManagementService() throws VirtualFireAlarmException {

        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        CertificateManagementService certificateManagementService = (CertificateManagementService)
                ctx.getOSGiService(CertificateManagementService.class, null);

        if (certificateManagementService == null) {
            String msg = "EnrollmentService is not initialized";
            log.error(msg);
            throw new VirtualFireAlarmException(msg);
        }

        return certificateManagementService;
    }

    /**
     *
     * @param deviceId
     * @return
     * @throws VirtualFireAlarmException
     */
    public static PublicKey getDevicePublicKey(String deviceId) throws VirtualFireAlarmException {
        PublicKey clientPublicKey;
        String alias = "";

        try {
            alias += deviceId.hashCode();

            CertificateManagementService certificateManagementService =
                    VirtualFireAlarmServiceUtils.getCertificateManagementService();
            X509Certificate clientCertificate = (X509Certificate) certificateManagementService.getCertificateByAlias(
                    alias);
            clientPublicKey = clientCertificate.getPublicKey();

        } catch (VirtualFireAlarmException e) {
            String errorMsg = "Could not retrieve CertificateManagementService from the runtime.";
            if(log.isDebugEnabled()){
                log.debug(errorMsg);
            }
            throw new VirtualFireAlarmException(errorMsg, e);
        } catch (KeystoreException e) {
            String errorMsg;
            if (e.getMessage().contains("NULL_CERT")) {
                errorMsg = "The Device-View page might have been accessed prior to the device being started.";
                if(log.isDebugEnabled()){
                    log.debug(errorMsg);
                }
                throw new VirtualFireAlarmException(errorMsg, e);
            } else {
                errorMsg = "An error occurred whilst trying to retrieve certificate for deviceId [" + deviceId +
                        "] with alias: [" + alias + "]";
                if(log.isDebugEnabled()){
                    log.debug(errorMsg);
                }
                throw new VirtualFireAlarmException(errorMsg, e);
            }
        }
        return clientPublicKey;
    }

}
