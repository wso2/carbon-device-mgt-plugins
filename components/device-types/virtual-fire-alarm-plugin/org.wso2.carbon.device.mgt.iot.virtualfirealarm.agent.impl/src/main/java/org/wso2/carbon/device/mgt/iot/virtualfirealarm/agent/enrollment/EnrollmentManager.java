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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.enrollment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.jscep.client.Client;
import org.jscep.client.ClientException;
import org.jscep.client.EnrollmentResponse;
import org.jscep.client.verification.CertificateVerifier;
import org.jscep.client.verification.OptimisticCertificateVerifier;
import org.jscep.transaction.TransactionException;
import org.jscep.transport.response.Capabilities;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.core.AgentConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.core.AgentManager;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.exception.AgentCoreOperationException;
import sun.security.x509.X509CertImpl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * This class controls the entire SCEP enrolment process of the client. It is a singleton for any single client which
 * has the agent code running in it. The main functionality of this class includes generating a Private-Public Key
 * Pair for the enrollment flow, creating the Certificate-Sign-Request using the generated Public-Key to send to the
 * SEP server, Contacting the SCEP server to receive the Signed Certificate and requesting for the server's public
 * key for encrypting the payloads.
 * The provider for all Cryptographic functions used in this class are "BouncyCastle" and the Asymmetric-Key pair
 * algorithm used is "RSA" with a key size of 2048. The signature algorithm used is "SHA1withRSA".
 * This class also holds the "SCEPUrl" (Server Url read from the configs file), the Private-Public Keys of the
 * client, Signed SCEP certificate and the server's public certificate.
 */

//TODO: Need to save cert and keys to file after initial enrollment...
public class EnrollmentManager {
    private static final Log log = LogFactory.getLog(EnrollmentManager.class);
    private static EnrollmentManager enrollmentManager;

    private static final String KEY_PAIR_ALGORITHM = "RSA";
    private static final String PROVIDER = "BC";
    private static final String SIGNATURE_ALG = "SHA1withRSA";
    private static final String CERT_IS_CA_EXTENSION = "is_ca";
    private static final int KEY_SIZE = 2048;

    // Seed to our PRNG. Make sure this is initialised randomly, NOT LIKE THIS
    private static final byte[] SEED = ")(*&^%$#@!".getBytes();
    private static final int CERT_VALIDITY = 730;

    // URL of our SCEP server
    private String SCEPUrl;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private PublicKey serverPublicKey;
    private X509Certificate SCEPCertificate;
    private boolean isEnrolled = false;


    /**
     * Constructor of the EnrollmentManager. Initializes the SCEPUrl as read from the configuration file by the
     * AgentManager.
     */
    private EnrollmentManager() {
        this.SCEPUrl = AgentManager.getInstance().getEnrollmentEP();
        //setEnrollmentStatus();
    }

    /**
     * Method to return the current singleton instance of the EnrollmentManager.
     *
     * @return the current singleton instance if available and if not initializes a new instance and returns it.
     */
    public static EnrollmentManager getInstance() {
        if (enrollmentManager == null) {
            enrollmentManager = new EnrollmentManager();
        }
        return enrollmentManager;
    }


    public void setEnrollmentStatus() {
        KeyStore keyStore;

        try {
            keyStore = KeyStore.getInstance(AgentConstants.DEVICE_KEYSTORE_TYPE);
            keyStore.load(new FileInputStream(AgentConstants.DEVICE_KEYSTORE),
                          AgentConstants.DEVICE_KEYSTORE_PASSWORD.toCharArray());

            this.isEnrolled = (keyStore.containsAlias(AgentConstants.DEVICE_CERT_ALIAS) &&
                    keyStore.containsAlias(AgentConstants.DEVICE_PRIVATE_KEY_ALIAS) &&
                    keyStore.containsAlias(AgentConstants.SERVER_CA_CERT_ALIAS));

        } catch (KeyStoreException e) {
            log.error(AgentConstants.LOG_APPENDER + "An error occurred whilst accessing the device KeyStore '" +
                              AgentConstants.DEVICE_KEYSTORE + "' with keystore type [" +
                              AgentConstants.DEVICE_KEYSTORE_TYPE + "] to ensure enrollment status.");
            log.error(AgentConstants.LOG_APPENDER + e);
            log.warn(AgentConstants.LOG_APPENDER + "Device will be re-enrolled.");
            return;
        } catch (CertificateException | NoSuchAlgorithmException e) {
            log.error(AgentConstants.LOG_APPENDER + "An error occurred whilst trying to [load] the device KeyStore '" +
                              AgentConstants.DEVICE_KEYSTORE + "'.");
            log.error(AgentConstants.LOG_APPENDER + e);
            log.warn(AgentConstants.LOG_APPENDER + "Device will be re-enrolled.");
            return;
        } catch (IOException e) {
            log.error(AgentConstants.LOG_APPENDER +
                              "An error occurred whilst trying to load input stream with the keystore file: " +
                              AgentConstants.DEVICE_KEYSTORE);
            log.error(AgentConstants.LOG_APPENDER + e);
            log.warn(AgentConstants.LOG_APPENDER + "Device will be re-enrolled.");
            return;
        }

        try {
            if (this.isEnrolled) {
                this.SCEPCertificate = (X509Certificate) keyStore.getCertificate(AgentConstants.DEVICE_CERT_ALIAS);
                this.privateKey = (PrivateKey) keyStore.getKey(AgentConstants.DEVICE_PRIVATE_KEY_ALIAS,
                                                               AgentConstants.DEVICE_KEYSTORE_PASSWORD.toCharArray());
                this.publicKey = SCEPCertificate.getPublicKey();

                X509Certificate serverCACert = (X509Certificate) keyStore.getCertificate(
                        AgentConstants.SERVER_CA_CERT_ALIAS);
                this.serverPublicKey = serverCACert.getPublicKey();
                log.info(AgentConstants.LOG_APPENDER +
                                 "Device has already been enrolled. Hence, loaded certificate information from device" +
                                 " trust-store.");
            }
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            log.error(AgentConstants.LOG_APPENDER + "An error occurred whilst accessing the device KeyStore '" +
                              AgentConstants.DEVICE_KEYSTORE + "' to ensure enrollment status.");
            log.error(AgentConstants.LOG_APPENDER + e);
            log.warn(AgentConstants.LOG_APPENDER + "Device will be re-enrolled.");
            this.isEnrolled = false;
        }
    }

    /**
     * Method to control the entire enrollment flow. This method calls the method to create the Private-Public Key
     * Pair, calls the specific method to generate the Certificate-Sign-Request, creates a one time self signed
     * certificate to present to the SCEP server with the initial CSR, calls the specific method to connect to the
     * SCEP Server and to get the SCEP Certificate and also calls the method that requests the SCEP Server for its
     * PublicKey for future payload encryption.
     *
     * @throws AgentCoreOperationException if the private method generateCertSignRequest() fails with an error or if
     *                                     there is an error creating a self-sign certificate to present to the
     *                                     server (whilst trying to get the CSR signed)
     */
    public void beginEnrollmentFlow() throws AgentCoreOperationException {
        Security.addProvider(new BouncyCastleProvider());

        KeyPair keyPair = generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();

        if (log.isDebugEnabled()) {
            log.info(AgentConstants.LOG_APPENDER + "DevicePrivateKey:\n[\n" + privateKey + "\n]\n");
            log.info(AgentConstants.LOG_APPENDER + "DevicePublicKey:\n[\n" + publicKey + "\n]\n");
        }

        PKCS10CertificationRequest certSignRequest = generateCertSignRequest();

        /**
         *  -----------------------------------------------------------------------------------------------
         *  Generate an ephemeral self-signed certificate. This is needed to present to the CA in the SCEP request.
         *  In the future, add proper EKU and attributes in the request. The CA does NOT have to honour any of this.
         *  -----------------------------------------------------------------------------------------------
         */
        X500Name issuer = new X500Name("CN=Temporary Issuer");
        BigInteger serial = new BigInteger(32, new SecureRandom());
        Date fromDate = new Date();
        Date toDate = new Date(System.currentTimeMillis() + (CERT_VALIDITY * 86400000L));

        // Build the self-signed cert using BC, sign it with our private key (self-signed)
        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(issuer, serial, fromDate, toDate,
                                                                            certSignRequest.getSubject(),
                                                                            certSignRequest.getSubjectPublicKeyInfo());
        ContentSigner sigGen;
        X509Certificate tmpCert;

        try {
            sigGen = new JcaContentSignerBuilder(SIGNATURE_ALG).setProvider(PROVIDER).build(keyPair.getPrivate());
            tmpCert = new JcaX509CertificateConverter().setProvider(PROVIDER).getCertificate(certBuilder.build(sigGen));
        } catch (OperatorCreationException e) {
            String errorMsg = "Error occurred whilst creating a ContentSigner for the Temp-Self-Signed Certificate.";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        } catch (CertificateException e) {
            String errorMsg = "Error occurred whilst trying to create Temp-Self-Signed Certificate.";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        }
        /**
         *  -----------------------------------------------------------------------------------------------
         */

        this.SCEPCertificate = getSignedCertificateFromServer(tmpCert, certSignRequest);
        this.serverPublicKey = initPublicKeyOfServer();

        storeCertificateToStore(AgentConstants.DEVICE_CERT_ALIAS, SCEPCertificate);
        storeKeyToKeyStore(AgentConstants.DEVICE_PRIVATE_KEY_ALIAS, this.privateKey, SCEPCertificate);

        if (log.isDebugEnabled()) {
            log.info(AgentConstants.LOG_APPENDER +
                             "SCEPCertificate, DevicePrivateKey, ServerPublicKey was saved to device keystore [" +
                             AgentConstants.DEVICE_KEYSTORE + "]");
            log.info(AgentConstants.LOG_APPENDER + "TemporaryCertPublicKey:\n[\n" + tmpCert.getPublicKey() + "\n]\n");
            log.info(AgentConstants.LOG_APPENDER + "ServerPublicKey:\n[\n" + serverPublicKey + "\n]\n");
        }
    }

    private void storeCertificateToStore(String alias, Certificate certificate) {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(AgentConstants.DEVICE_KEYSTORE_TYPE);
            keyStore.load(new FileInputStream(AgentConstants.DEVICE_KEYSTORE),
                          AgentConstants.DEVICE_KEYSTORE_PASSWORD.toCharArray());

            keyStore.setCertificateEntry(alias, certificate);
            keyStore.store(new FileOutputStream(AgentConstants.DEVICE_KEYSTORE),
                           AgentConstants.DEVICE_KEYSTORE_PASSWORD.toCharArray());

        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            log.error(AgentConstants.LOG_APPENDER +
                              "An error occurred whilst trying to store the Certificate received from the SCEP " +
                              "Enrollment.");
            log.error(AgentConstants.LOG_APPENDER + e);
            log.warn(AgentConstants.LOG_APPENDER +
                             "SCEP Certificate was not stored in the keystore; " +
                             "Hence the device will be re-enrolled during next restart.");
        }
    }


    private void storeKeyToKeyStore(String alias, Key cryptoKey, Certificate certInCertChain) {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(AgentConstants.DEVICE_KEYSTORE_TYPE);
            keyStore.load(new FileInputStream(AgentConstants.DEVICE_KEYSTORE),
                          AgentConstants.DEVICE_KEYSTORE_PASSWORD.toCharArray());

            Certificate[] certChain = new Certificate[1];
            certChain[0] = certInCertChain;

            keyStore.setKeyEntry(alias, cryptoKey, AgentConstants.DEVICE_KEYSTORE_PASSWORD.toCharArray(), certChain);
            keyStore.store(new FileOutputStream(AgentConstants.DEVICE_KEYSTORE),
                           AgentConstants.DEVICE_KEYSTORE_PASSWORD.toCharArray());

        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            log.error(AgentConstants.LOG_APPENDER +
                              "An error occurred whilst trying to store the key with alias " +
                              "[" + alias + "] in the device keystore.");
            log.error(AgentConstants.LOG_APPENDER + e);
            log.warn(AgentConstants.LOG_APPENDER +
                             "Key [" + alias + "] was not stored in the keystore; " +
                             "Hence the device will be re-enrolled during next restart.");
        }
    }

    /**
     * This method creates the Public-Private Key pair for the current client.
     *
     * @return the generated KeyPair object
     * @throws AgentCoreOperationException when the given Security Provider does not exist or the Algorithmn used to
     *                                     generate the key pair is invalid.
     */
    private KeyPair generateKeyPair() throws AgentCoreOperationException {

        // Generate key pair
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM, PROVIDER);
            keyPairGenerator.initialize(KEY_SIZE, new SecureRandom(SEED));
        } catch (NoSuchAlgorithmException e) {
            String errorMsg = "Algorithm [" + KEY_PAIR_ALGORITHM + "] provided for KeyPairGenerator is invalid.";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        } catch (NoSuchProviderException e) {
            String errorMsg = "Provider [" + PROVIDER + "] provided for KeyPairGenerator does not exist.";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        }

        return keyPairGenerator.genKeyPair();
    }


    /**
     * This method creates the PKCS10 Certificate Sign Request which is to be sent to the SCEP Server using the
     * generated PublicKey of the client. The certificate parameters used here are the ones from the AgentManager
     * which are the values read from the configurations file.
     *
     * @return the PKCS10CertificationRequest object created using the client specific configs and the generated
     * PublicKey
     * @throws AgentCoreOperationException if an error occurs when creating a content signer to sign the CSR.
     */
    private PKCS10CertificationRequest generateCertSignRequest() throws AgentCoreOperationException {
        // Build the CN for the cert that's being requested.
        X500NameBuilder nameBld = new X500NameBuilder(BCStyle.INSTANCE);
        nameBld.addRDN(BCStyle.CN, AgentManager.getInstance().getAgentConfigs().getTenantDomain());
        nameBld.addRDN(BCStyle.O, AgentManager.getInstance().getAgentConfigs().getDeviceOwner());
        nameBld.addRDN(BCStyle.OU, AgentManager.getInstance().getAgentConfigs().getDeviceOwner());
        nameBld.addRDN(BCStyle.UNIQUE_IDENTIFIER, AgentManager.getInstance().getAgentConfigs().getDeviceId());
        nameBld.addRDN(BCStyle.SERIALNUMBER, AgentManager.getInstance().getAgentConfigs().getDeviceId());
        X500Name principal = nameBld.build();

        JcaContentSignerBuilder contentSignerBuilder = new JcaContentSignerBuilder(SIGNATURE_ALG).setProvider(PROVIDER);
        ContentSigner contentSigner;

        try {
            contentSigner = contentSignerBuilder.build(this.privateKey);
        } catch (OperatorCreationException e) {
            String errorMsg = "Could not create content signer with private key.";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        }

        // Generate the certificate signing request (csr = PKCS10)
        PKCS10CertificationRequestBuilder reqBuilder = new JcaPKCS10CertificationRequestBuilder(principal,
                                                                                                this.publicKey);
        return reqBuilder.build(contentSigner);
    }


    /**
     * This method connects to the SCEP Server to fetch the signed SCEP Certificate.
     *
     * @param tempCert        the temporary self-signed certificate of the client required for the initial CSR
     *                        request against the SCEP Server.
     * @param certSignRequest the PKCS10 Certificate-Sign-Request that is to be sent to the SCEP Server.
     * @return the SCEP-Certificate for the client signed by the SCEP-Server.
     * @throws AgentCoreOperationException if the SCEPUrl is invalid or if the flow of sending the CSR and getting
     *                                     the signed certificate fails or if the signed certificate cannot be
     *                                     retrieved from the reply from the server.
     */
    private X509Certificate getSignedCertificateFromServer(X509Certificate tempCert,
                                                           PKCS10CertificationRequest certSignRequest)
            throws AgentCoreOperationException {

        X509Certificate signedSCEPCertificate = null;
        URL url;
        EnrollmentResponse enrolResponse;
        CertStore certStore;

        try {
            // The URL where we are going to request our cert from
            url = new URL(this.SCEPUrl);

            /*  // This is called when we get the certificate for our CSR signed by CA
                // Implement this handler to check the CA cert in prod. We can do cert pinning here
            CallbackHandler cb = new CallbackHandler() {
                @Override
                public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated
                    methods, choose Tools | Templates.
                }
            };*/

            // Implement verification of the CA cert. VERIFY the CA
            CertificateVerifier ocv = new OptimisticCertificateVerifier();

            // Instantiate our SCEP client
            Client scepClient = new Client(url, ocv);

            // Submit our cert for signing. SCEP server should allow the client to specify
            // the SCEP CA to issue the request against, if there are multiple CAs
            enrolResponse = scepClient.enrol(tempCert, this.privateKey, certSignRequest);

            // Verify we got what we want, and just print out the cert.
            certStore = enrolResponse.getCertStore();

            for (Certificate x509Certificate : certStore.getCertificates(null)) {
                if (log.isDebugEnabled()) {
                    log.debug(x509Certificate.toString());
                }
                signedSCEPCertificate = (X509Certificate) x509Certificate;
            }

        } catch (MalformedURLException ex) {
            String errorMsg = "Could not create valid URL from given SCEP URI: " + SCEPUrl;
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, ex);
        } catch (TransactionException | ClientException e) {
            String errorMsg = "Enrollment process to SCEP Server at: " + SCEPUrl + " failed.";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        } catch (CertStoreException e) {
            String errorMsg = "Could not retrieve [Signed-Certificate] from the response message from SCEP-Server.";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        }

        return signedSCEPCertificate;
    }


    /**
     * Gets the Public Key of the SCEP-Server and initializes it for later use. This method contacts the SCEP Server
     * and fetches its CA Cert and extracts the Public Key of the server from the received reply.
     *
     * @return the public key of the SCEP Server which is to be used to encrypt pyloads.
     * @throws AgentCoreOperationException if the SCEPUrl is invalid or if the flow of sending the CSR and getting
     *                                     the signed certificate fails or if the signed certificate cannot be
     *                                     retrieved from the reply from the server.
     */
    private PublicKey initPublicKeyOfServer() throws AgentCoreOperationException {
        URL url;
        CertStore certStore;
        PublicKey serverCertPublicKey = null;

        try {
            // The URL where we are going to request our cert from
            url = new URL(this.SCEPUrl);

            /*  // This is called when we get the certificate for our CSR signed by CA
                // Implement this handler to check the CA cert in prod. We can do cert pinning here
                CallbackHandler cb = new CallbackHandler() {
                    @Override
                    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated
                        methods, choose Tools | Templates.
                }
            };*/

            // Implement verification of the CA cert. VERIFY the CA
            CertificateVerifier ocv = new OptimisticCertificateVerifier();

            // Instantiate our SCEP client
            Client scepClient = new Client(url, ocv);

            // Get the CA capabilities. Should return SHA1withRSA for strongest hash and sig. Returns MD5.
            if (log.isDebugEnabled()) {
                Capabilities cap = scepClient.getCaCapabilities();
                log.debug(String.format(
                        "\nStrongestCipher: %s,\nStrongestMessageDigest: %s,\nStrongestSignatureAlgorithm: %s," +
                                "\nIsRenewalSupported: %s,\nIsRolloverSupported: %s",
                        cap.getStrongestCipher(), cap.getStrongestMessageDigest(), cap.getStrongestSignatureAlgorithm(),
                        cap.isRenewalSupported(), cap.isRolloverSupported()));
            }

            certStore = scepClient.getCaCertificate();

            for (Certificate cert : certStore.getCertificates(null)) {
                if (cert instanceof X509Certificate) {
                    if (log.isDebugEnabled()) {
                        log.debug(((X509Certificate) cert).getIssuerDN().getName());
                    }

                    // I have chosen the CA cert based on its BasicConstraintExtension "is_ca" being set to "true"
                    // This is because the returned keystore may contain many certificates including RAs.
                    if (((Boolean) ((X509CertImpl) cert).getBasicConstraintsExtension().get(CERT_IS_CA_EXTENSION))) {
                        serverCertPublicKey = cert.getPublicKey();
                        storeCertificateToStore(AgentConstants.SERVER_CA_CERT_ALIAS, cert);
                    }
                }
            }

        } catch (MalformedURLException ex) {
            String errorMsg = "Could not create valid URL from given SCEP URI: " + SCEPUrl;
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, ex);
        } catch (ClientException e) {
            String errorMsg = "Could not retrieve [Server-Certificate] from the SCEP-Server.";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        } catch (CertStoreException e) {
            String errorMsg = "Could not retrieve [Server-Certificates] from the response message from SCEP-Server.";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Error occurred whilst trying to get property ['is_ca'] from the retreived Certificates";
            log.error(errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        }

        return serverCertPublicKey;
    }

    /**
     * Gets the Public-Key of the client.
     *
     * @return the public key of the client.
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Gets the Private-Key of the client.
     *
     * @return the private key of the client.
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * Gets the SCEP-Certificate of the client.
     *
     * @return the SCEP Certificate of the client.
     */
    public X509Certificate getSCEPCertificate() {
        return SCEPCertificate;
    }

    /**
     * Gets the Public-Key of the Server.
     *
     * @return the pubic key of the server.
     */
    public PublicKey getServerPublicKey() {
        return serverPublicKey;
    }

    /**
     * Checks whether the device has already been enrolled with the SCEP Server.
     *
     * @return the enrollment status; 'TRUE' if already enrolled else 'FALSE'.
     */
    public boolean isEnrolled() {
        return isEnrolled;
    }
}
