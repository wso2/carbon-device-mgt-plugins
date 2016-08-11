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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.device.mgt.iot.devicetype.config.CertificateKeystoreConfig;
import org.wso2.carbon.device.mgt.iot.devicetype.config.DeviceManagementConfiguration;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.exception.VirtualFirealarmDeviceMgtPluginException;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.internal.VirtualFirealarmManagementDataHolder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class VirtualFirealarmSecurityManager {
    private static final Log log = LogFactory.getLog(VirtualFirealarmSecurityManager.class);

    private static PrivateKey serverPrivateKey;
    private static CertificateKeystoreConfig certificateKeystoreConfig;
    private static final String SIGNATURE_ALG = "SHA1withRSA";
    private static final String CIPHER_PADDING = "RSA/ECB/PKCS1Padding";

    private VirtualFirealarmSecurityManager() {

    }

    private static CertificateKeystoreConfig getCertKeyStoreConfig() {
        if (certificateKeystoreConfig == null) {
            DeviceManagementConfiguration deviceManagementConfiguration = VirtualFirealarmManagementDataHolder.getInstance().
                    getDeviceTypeConfigService().getConfiguration(
                    VirtualFireAlarmConstants.DEVICE_TYPE,
                    VirtualFireAlarmConstants.DEVICE_TYPE_PROVIDER_DOMAIN);
            certificateKeystoreConfig = deviceManagementConfiguration.getCertificateKeystoreConfig();
        }
        return certificateKeystoreConfig;
    }

    public static void initVerificationManager() {
        serverPrivateKey = retrievePrivateKey();
    }

    public static PrivateKey retrievePrivateKey() {
        PrivateKey privateKey = null;
        InputStream inputStream = null;
        KeyStore keyStore;
        CertificateKeystoreConfig certificateKeystoreConfig = getCertKeyStoreConfig();
        try {
            keyStore = KeyStore.getInstance(certificateKeystoreConfig.getCertificateKeystoreType());
            inputStream = new FileInputStream(certificateKeystoreConfig.getCertificateKeystoreLocation());

            keyStore.load(inputStream, certificateKeystoreConfig.getCertificateKeystorePassword().toCharArray());

            privateKey = (PrivateKey) (keyStore.getKey(certificateKeystoreConfig.getCACertAlias(),
                                                       certificateKeystoreConfig.getCAPrivateKeyPassword().toCharArray()));

        } catch (KeyStoreException e) {
            String errorMsg = "Could not load KeyStore of given type in [certificate-config.xml] file." ;
            log.error(errorMsg, e);
        } catch (FileNotFoundException e) {
            String errorMsg = "KeyStore file could not be loaded from path given in [certificate-config.xml] file.";
            log.error(errorMsg, e);
        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found when loading KeyStore";
            log.error(errorMsg, e);
        } catch (CertificateException e) {
            String errorMsg = "CertificateException when loading KeyStore";
            log.error(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Input output issue occurred when loading KeyStore";
            log.error(errorMsg, e);
        } catch (UnrecoverableKeyException e) {
            String errorMsg = "Key is unrecoverable when retrieving CA private key";
            log.error(errorMsg, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("Error closing KeyStore input stream", e);
            }
        }

        return privateKey;
    }

    public static PrivateKey getServerPrivateKey() {
        return serverPrivateKey;
    }

    public static String encryptMessage(String message, Key encryptionKey) throws
                                                                           VirtualFirealarmDeviceMgtPluginException {
        Cipher encrypter;
        byte[] cipherData;

        try {
            encrypter = Cipher.getInstance(CIPHER_PADDING);
            encrypter.init(Cipher.ENCRYPT_MODE, encryptionKey);
            cipherData = encrypter.doFinal(message.getBytes(StandardCharsets.UTF_8));

        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found exception occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        } catch (NoSuchPaddingException e) {
            String errorMsg = "No Padding error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        } catch (InvalidKeyException e) {
            String errorMsg = "InvalidKey exception occurred for encryptionKey \n[\n" + encryptionKey + "\n]\n";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        } catch (BadPaddingException e) {
            String errorMsg = "Bad Padding error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        } catch (IllegalBlockSizeException e) {
            String errorMsg = "Illegal blockSize error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        }

        return Base64.encodeBase64String(cipherData);
    }

    public static String signMessage(String encryptedData, PrivateKey signatureKey) throws VirtualFirealarmDeviceMgtPluginException {

        Signature signature;
        String signedEncodedString;

        try {
            signature = Signature.getInstance(SIGNATURE_ALG);
            signature.initSign(signatureKey);
            signature.update(Base64.decodeBase64(encryptedData));

            byte[] signatureBytes = signature.sign();
            signedEncodedString = Base64.encodeBase64String(signatureBytes);

        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found exception occurred for Signature instance of [" + SIGNATURE_ALG + "]";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        } catch (SignatureException e) {
            String errorMsg = "Signature exception occurred for Signature instance of [" + SIGNATURE_ALG + "]";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        } catch (InvalidKeyException e) {
            String errorMsg = "InvalidKey exception occurred for signatureKey \n[\n" + signatureKey + "\n]\n";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        }

        return signedEncodedString;
    }

    public static boolean verifySignature(String data, String signedData, PublicKey verificationKey)
            throws VirtualFirealarmDeviceMgtPluginException {

        Signature signature;
        boolean verified;

        try {
            signature = Signature.getInstance(SIGNATURE_ALG);
            signature.initVerify(verificationKey);
            signature.update(Base64.decodeBase64(data));

            verified = signature.verify(Base64.decodeBase64(signedData));

        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found exception occurred for Signature instance of [" + SIGNATURE_ALG + "]";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        } catch (SignatureException e) {
            String errorMsg = "Signature exception occurred for Signature instance of [" + SIGNATURE_ALG + "]";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        } catch (InvalidKeyException e) {
            String errorMsg = "InvalidKey exception occurred for signatureKey \n[\n" + verificationKey + "\n]\n";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        }

        return verified;
    }

    public static String decryptMessage(String encryptedMessage, Key decryptKey) throws VirtualFirealarmDeviceMgtPluginException {

        Cipher decrypter;
        String decryptedMessage;

        try {

            decrypter = Cipher.getInstance(CIPHER_PADDING);
            decrypter.init(Cipher.DECRYPT_MODE, decryptKey);
            decryptedMessage = new String(decrypter.doFinal(Base64.decodeBase64(encryptedMessage)), StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found exception occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        } catch (NoSuchPaddingException e) {
            String errorMsg = "No Padding error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        } catch (InvalidKeyException e) {
            String errorMsg = "InvalidKey exception occurred for encryptionKey \n[\n" + decryptKey + "\n]\n";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        } catch (BadPaddingException e) {
            String errorMsg = "Bad Padding error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        } catch (IllegalBlockSizeException e) {
            String errorMsg = "Illegal blockSize error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new VirtualFirealarmDeviceMgtPluginException(errorMsg, e);
        }

        return decryptedMessage;
    }


}
