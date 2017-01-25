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

package org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.exception.AuthorizationException;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class acts as a access point to retrieve config parameters used within the authorization.
 * this configuration is read from broker.xml
 */
public class AuthorizationConfiguration {
    /**
     * Reserved Prefixes that activate different processing logic.
     */
    private static final String LIST_TYPE = "LIST_";
    private static final QName SECURE_VAULT_QNAME = new QName("http://org.wso2.securevault/configuration"
            , "secretAlias");
    /**
     * Common Error states
     */
    private static final String GENERIC_CONFIGURATION_PARSE_ERROR = "Error occurred when trying to parse " +
            "configuration value {0}.";
    private static final String NO_CHILD_FOR_KEY_IN_PROPERTY = "There was no child at the given key {0} for the " +
            "parent property {1}.";
    private static final String PROPERTY_NOT_A_LIST = "The input property {0} does not contain a list of child " +
            "properties.";
    /**
     * location to broker.xml
     */
    private static final String ROOT_CONFIG_FILE_PATH = System.getProperty(ServerConstants.CARBON_HOME)
            + "/repository/conf/";
    /**
     * File name of the main configuration file.
     */
    private static final String ROOT_CONFIG_FILE_NAME = "broker.xml";
    private static Log log = LogFactory.getLog(AuthorizationConfigurationManager.class);
    private static CompositeConfiguration compositeConfiguration;
    /**
     * This hashmap is used to maintain any properties that were read from broker.xml
     */
    private static ConcurrentHashMap<String, String> propertyList;

    public static void initialize() throws AuthorizationException {
        String brokerConfigFilePath = ROOT_CONFIG_FILE_PATH + ROOT_CONFIG_FILE_NAME;
        if (log.isDebugEnabled()) {
            log.debug("Configuration located at : " + brokerConfigFilePath);
        }
        try {
            compositeConfiguration = new CompositeConfiguration();
            compositeConfiguration.setDelimiterParsingDisabled(true);
            XMLConfiguration rootConfiguration = new XMLConfiguration();
            rootConfiguration.setDelimiterParsingDisabled(true);
            rootConfiguration.setFileName(brokerConfigFilePath);
            rootConfiguration.setExpressionEngine(new XPathExpressionEngine());
            rootConfiguration.load();
            readConfigurationFromFile(brokerConfigFilePath);
            compositeConfiguration.addConfiguration(rootConfiguration);
        } catch (FileNotFoundException e) {
            String error = "Error occurred when trying to read the configuration file : " + brokerConfigFilePath;
            log.error(error, e);
            throw new AuthorizationException(error, e);
        } catch (JaxenException e) {
            String error = "Error occurred when trying to process file : " + brokerConfigFilePath;
            log.error(error, e);
            throw new AuthorizationException(error, e);
        } catch (XMLStreamException e) {
            String error = "Error occurred when trying to process file : " + brokerConfigFilePath;
            log.error(error, e);
            throw new AuthorizationException(error, e);
        } catch (ConfigurationException e) {
            String error = "Error occurred when trying to process file :" + brokerConfigFilePath;
            log.error(error, e);
            throw new AuthorizationException(error, e);
        }
    }


    private static void readConfigurationFromFile(String filePath) throws FileNotFoundException, JaxenException
            , XMLStreamException {
        propertyList = new ConcurrentHashMap();
        StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(new FileInputStream(new File(filePath)));
        OMElement dom = stAXOMBuilder.getDocumentElement();
        SecretResolver secretResolver = SecretResolverFactory.create(dom, false);
        AXIOMXPath xpathExpression = new AXIOMXPath("//*[@*[local-name() = \'secretAlias\']]");
        List nodeList = xpathExpression.selectNodes(dom);
        String propertyKey;
        String propertyValue;
        for (Iterator i$ = nodeList.iterator(); i$.hasNext(); propertyList.put(propertyKey, propertyValue)) {
            Object o = i$.next();
            propertyKey = ((OMElement) o).getAttributeValue(SECURE_VAULT_QNAME);
            propertyValue = "";
            if (secretResolver != null && secretResolver.isInitialized()) {
                if (secretResolver.isTokenProtected(propertyKey)) {
                    propertyValue = secretResolver.resolve(propertyKey);
                }
            } else {
                log.warn("Error while reading properties form file");
            }
        }

    }

    /**
     * Using this method, you can access a singular property of a child.
     * example,
     * <authorizer class="org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.
     * DeviceAccessBasedMQTTAuthorizer">
     * <property name="connectionPermission">/permission/admin/device-mgt/user</property>
     * <property name="adminPermission">/permission/admin/device-mgt/admin</property>
     * <property name="MQTTSubscriberScopeIdentifier">mqtt-subscriber</property>
     * <property name="MQTTPublisherScopeIdentifier">mqtt-subscriber</property>
     * <property name="devicemgtScopeIdentifier">device-mgt</property>
     * </authorizer> scenario.
     *
     * @param configurationProperty relevant enum value (e.g.- above scenario -> org.wso2.carbon.andes.extensions
     *                              .device.mgt.mqtt.authorization.config.TRANSPORT_MQTT_AUTHORIZATION_PROPERTIES)
     * @param key                   key of the child of whom you seek the value (e.g. above scenario -> "property list")
     */
    static <T> T readValueOfChildByKey(MQTTConfiguration configurationProperty, String key) {

        String constructedKey = configurationProperty.get().getKeyInFile().replace("{key}",
                key);
        try {
            return (T) deriveValidConfigurationValue(constructedKey,
                    configurationProperty.get().getDataType(),
                    configurationProperty.get().getDefaultValue());
        } catch (ConfigurationException e) {
            log.error(MessageFormat.format(NO_CHILD_FOR_KEY_IN_PROPERTY, key, configurationProperty), e);
            return null;
        }
    }


    /**
     * Use this method when you need to acquire a list of properties of same group.
     *
     * @param configurationProperty relevant enum value (e.g.- org.wso2.carbon.andes.extensions
     *                              .device.mgt.mqtt.authorization.config.LIST_TRANSPORT_MQTT_AUTHORIZATION_PROPERTIES)
     * @return String list of required property values
     */
    static List<String> readValueList(MQTTConfiguration configurationProperty) {

        if (configurationProperty.toString().startsWith(LIST_TYPE)) {
            return Arrays.asList(compositeConfiguration.getStringArray(configurationProperty.get().getKeyInFile()));
        } else {
            log.error(MessageFormat.format(PROPERTY_NOT_A_LIST, configurationProperty));
            return new ArrayList<>();
        }
    }


    /**
     * Given the data type and the value read from a config, this returns the parsed value
     * of the property.
     *
     * @param key          The Key to the property being read (n xpath format as contained in file.)
     * @param dataType     Expected data type of the property
     * @param defaultValue This parameter should NEVER be null since we assign a default value to
     *                     every config property.
     * @param <T>          Expected data type of the property
     * @return Value of config in the expected data type.
     * @throws ConfigurationException if there are any configuration issues
     */
    private static <T> T deriveValidConfigurationValue(String key, Class<T> dataType,
                                                       String defaultValue) throws ConfigurationException {
        if (log.isDebugEnabled()) {
            log.debug("Reading configuration value " + key);
        }
        String readValue = compositeConfiguration.getString(key);
        String validValue = defaultValue;
        if (StringUtils.isBlank(readValue)) {
            log.warn("Error when trying to read property : " + key + ". Switching to " + "default value : " +
                    defaultValue);
        } else {
            validValue = overrideWithDecryptedValue(key, readValue);
        }
        if (log.isDebugEnabled()) {
            log.debug("Valid value read for andes configuration property " + key + " is : " + validValue);
        }
        try {
            return dataType.getConstructor(String.class).newInstance(validValue);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new ConfigurationException(MessageFormat.format(GENERIC_CONFIGURATION_PARSE_ERROR, key), e);
        }
    }

    /**
     * If the property is contained in the propertyList, replace the raw value with that value.
     *
     * @param keyInFile xpath expression used to extract the value from file.
     * @param rawValue  The value read from the file without any processing.
     * @return the value with corresponding to actual value.
     */
    private static String overrideWithDecryptedValue(String keyInFile, String rawValue) {
        if (!StringUtils.isBlank(keyInFile)) {
            String key = keyInFile.replaceAll("/", ".");
            if (propertyList.containsKey(key)) {
                return propertyList.get(key);
            }
        }
        return rawValue;
    }
}
