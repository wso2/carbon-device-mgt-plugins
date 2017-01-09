/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.device.mgt.output.adapter.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authentication.Authenticator;
import org.wso2.carbon.device.mgt.output.adapter.websocket.authorization.Authorizer;
import org.wso2.carbon.device.mgt.output.adapter.websocket.service.WebsocketValidationService;
import org.wso2.carbon.device.mgt.output.adapter.websocket.service.WebsocketValidationServiceImpl;
import org.wso2.carbon.device.mgt.output.adapter.websocket.util.WebsocketEventAdapterConstants;
import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.core.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The UI event adapter factory class to create a UI output adapter
 */
public class WebsocketEventAdapterFactory extends OutputEventAdapterFactory {

    private ResourceBundle resourceBundle = ResourceBundle.getBundle("org.wso2.carbon.device.mgt.output.adapter.websocket.i18n" +
            ".Resources", Locale.getDefault());
    private BundleContext bundleContext;
    private boolean isAuthInitialized = false;
    private static final Log log = LogFactory.getLog(WebsocketEventAdapter.class);

    public WebsocketEventAdapterFactory() {
    }

    @Override
    public String getType() {
        return WebsocketEventAdapterConstants.ADAPTER_TYPE_UI;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportedMessageFormats = new ArrayList<String>();
        supportedMessageFormats.add(MessageType.WSO2EVENT);
        supportedMessageFormats.add(MessageType.JSON);
        return supportedMessageFormats;
    }

    @Override
    public List<Property> getStaticPropertyList() {
        return null;
    }

    @Override
    public List<Property> getDynamicPropertyList() {
        return null;
    }

    @Override
    public String getUsageTips() {
        return resourceBundle.getString(WebsocketEventAdapterConstants.ADAPTER_USAGE_TIPS_PREFIX) + " "
                + resourceBundle.getString(WebsocketEventAdapterConstants.ADAPTER_USAGE_TIPS_POSTFIX);
    }

    @Override
    public OutputEventAdapter createEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                                                 Map<String, String> globalProperties) {
        if (!isAuthInitialized) {
            initializeAuthenticatorAndAuthorizor(globalProperties);
        }
        return new WebsocketEventAdapter(eventAdapterConfiguration, globalProperties);
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private void initializeAuthenticatorAndAuthorizor (Map<String, String> globalProperties) {
        if (!isAuthInitialized) {
            synchronized (WebsocketEventAdapterFactory.class) {
                if (!isAuthInitialized) {
                    try {
                        WebsocketValidationServiceImpl websocketValidationService =
                                new WebsocketValidationServiceImpl();
                        String authenticatorClassName = globalProperties.get(
                                WebsocketEventAdapterConstants.AUTHENTICATOR_CLASS);
                        String authorizerClassName = globalProperties.get(WebsocketEventAdapterConstants.AUTHORIZER_CLASS);
                        if (authenticatorClassName != null && !authenticatorClassName.isEmpty()) {
                            Class<? extends Authenticator> authenticatorClass = Class.forName(authenticatorClassName)
                                    .asSubclass(Authenticator.class);
                            Authenticator authenticator = authenticatorClass.newInstance();
                            authenticator.init(globalProperties);
                            websocketValidationService.setAuthenticator(authenticator);
                        }
                        if (authorizerClassName != null && !authorizerClassName.isEmpty()) {
                            Class<? extends Authorizer> authorizerClass = Class.forName(authorizerClassName)
                                    .asSubclass(Authorizer.class);
                            Authorizer authorizer = authorizerClass.newInstance();
                            authorizer.init(globalProperties);
                            websocketValidationService.setAuthorizer(authorizer);
                        }
                        bundleContext.registerService(
                                WebsocketValidationService.class.getName(), websocketValidationService, null);
                        isAuthInitialized = true;
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        log.error("Failed to initialize the class authentication and authorization given " +
                                          "in the websocket validation configuration.", e);
                    }
                }
            }
        }

    }

}
