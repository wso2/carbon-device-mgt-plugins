/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.transport;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

/**
 * This is a utility class which contains methods common to the communication process of a client and the server. The
 * methods include encryption/decryption of payloads and signing/verification of payloads received and to be sent.
 */
public class CommunicationUtils {
    private static final Log log = LogFactory.getLog(TransportUtils.class);

    // The Signature Algorithm used.
    private static final String SHA_512 = "SHA-512";
    // The Encryption Algorithm and the Padding used.
    private static final String CIPHER_PADDING = "RSA/ECB/PKCS1Padding";


    /**
     * Encrypts the message with the key that's passed in.
     *
     * @param message       the message to be encrypted.
     * @param encryptionKey the key to use for the encryption of the message.
     * @return the encrypted message in String format.
     * @throws TransportHandlerException if an error occurs with the encryption flow which can be due to Padding
     *                                     issues, encryption key being invalid or the algorithm used is unrecognizable.
     */
    public static String encryptMessage(String message, Key encryptionKey) throws TransportHandlerException {
        Cipher encrypter;
        byte[] cipherData;

        try {
            encrypter = Cipher.getInstance(CIPHER_PADDING);
            encrypter.init(Cipher.ENCRYPT_MODE, encryptionKey);
            cipherData = encrypter.doFinal(message.getBytes(StandardCharsets.UTF_8));

        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found exception occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        } catch (NoSuchPaddingException e) {
            String errorMsg = "No Padding error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        } catch (InvalidKeyException e) {
            String errorMsg = "InvalidKey exception occurred for encryptionKey \n[\n" + encryptionKey + "\n]\n";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        } catch (BadPaddingException e) {
            String errorMsg = "Bad Padding error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        } catch (IllegalBlockSizeException e) {
            String errorMsg = "Illegal blockSize error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        }

        return Base64.encodeBase64String(cipherData);
    }

///TODO:: Exception needs to change according to the common package
    /**
     * Signed a given message using the PrivateKey that's passes in.
     *
     * @param message      the message to be signed. Ideally some encrypted payload.
     * @param signatureKey the PrivateKey with which the message is to be signed.
     * @return the Base64Encoded String of the signed payload.
     * @throws TransportHandlerException if some error occurs with the signing process which may be related to the
     *                                     signature algorithm used or the key used for signing.
     */
    public static String signMessage(String message, PrivateKey signatureKey) throws TransportHandlerException {

        Signature signature;
        String signedEncodedString;

        try {
            signature = Signature.getInstance(SHA_512);
            signature.initSign(signatureKey);
            signature.update(Base64.decodeBase64(message));

            byte[] signatureBytes = signature.sign();
            signedEncodedString = Base64.encodeBase64String(signatureBytes);

        } catch (NoSuchAlgorithmException e) {
            String errorMsg =
                    "Algorithm not found exception occurred for Signature instance of [" + SHA_512 + "]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        } catch (SignatureException e) {
            String errorMsg = "Signature exception occurred for Signature instance of [" + SHA_512 + "]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        } catch (InvalidKeyException e) {
            String errorMsg = "InvalidKey exception occurred for signatureKey \n[\n" + signatureKey + "\n]\n";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        }

        return signedEncodedString;
    }


    /**
     * Verifies some signed-data against the a Public-Key to ensure that it was produced by the holder of the
     * corresponding Private Key.
     *
     * @param data            the actual payoad which was signed by some Private Key.
     * @param signedData      the signed data produced by signing the payload using a Private Key.
     * @param verificationKey the corresponding Public Key which is an exact pair of the Private-Key with we expect
     *                        the data to be signed by.
     * @return true if the signed data verifies to be signed by the corresponding Private Key.
     * @throws TransportHandlerException if some error occurs with the verification process which may be related to
     *                                     the signature algorithm used or the key used for signing.
     */
    public static boolean verifySignature(String data, String signedData, PublicKey verificationKey)
            throws TransportHandlerException {

        Signature signature;
        boolean verified;

        try {
            signature = Signature.getInstance(SHA_512);
            signature.initVerify(verificationKey);
            signature.update(Base64.decodeBase64(data));

            verified = signature.verify(Base64.decodeBase64(signedData));

        } catch (NoSuchAlgorithmException e) {
            String errorMsg =
                    "Algorithm not found exception occurred for Signature instance of [" + SHA_512 + "]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        } catch (SignatureException e) {
            String errorMsg = "Signature exception occurred for Signature instance of [" + SHA_512 + "]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        } catch (InvalidKeyException e) {
            String errorMsg = "InvalidKey exception occurred for signatureKey \n[\n" + verificationKey + "\n]\n";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        }

        return verified;
    }


    /**
     * Encrypts the message with the key that's passed in.
     *
     * @param encryptedMessage the encrypted message that is supposed to be decrypted.
     * @param decryptKey       the key to use in the decryption process.
     * @return the decrypted message in String format.
     * @throws TransportHandlerException if an error occurs with the encryption flow which can be due to Padding
     *                                     issues, encryption key being invalid or the algorithm used is unrecognizable.
     */
    public static String decryptMessage(String encryptedMessage, Key decryptKey) throws TransportHandlerException {

        Cipher decrypter;
        String decryptedMessage;

        try {

            decrypter = Cipher.getInstance(CIPHER_PADDING);
            decrypter.init(Cipher.DECRYPT_MODE, decryptKey);
            decryptedMessage = new String(decrypter.doFinal(Base64.decodeBase64(encryptedMessage)),
                                          StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm not found exception occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        } catch (NoSuchPaddingException e) {
            String errorMsg = "No Padding error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        } catch (InvalidKeyException e) {
            String errorMsg = "InvalidKey exception occurred for encryptionKey \n[\n" + decryptKey + "\n]\n";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        } catch (BadPaddingException e) {
            String errorMsg = "Bad Padding error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        } catch (IllegalBlockSizeException e) {
            String errorMsg = "Illegal blockSize error occurred for Cipher instance of [" + CIPHER_PADDING + "]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        }

        return decryptedMessage;
    }
}
