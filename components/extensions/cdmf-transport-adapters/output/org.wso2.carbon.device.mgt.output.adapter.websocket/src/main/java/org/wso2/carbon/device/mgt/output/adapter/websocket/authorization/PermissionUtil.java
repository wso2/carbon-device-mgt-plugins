/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.output.adapter.websocket.authorization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.device.mgt.output.adapter.websocket.internal.WebsocketEventAdaptorServiceDataHolder;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.StringTokenizer;

/**
 * Utility class which holds necessary utility methods required for persisting permissions in
 * registry.
 */
public class PermissionUtil {

    public static final String PERMISSION_PROPERTY_NAME = "name";
    private static Log log = LogFactory.getLog(DeviceAuthorizer.class);

    public static void putPermission(String permission) {
        try {
            StringTokenizer tokenizer = new StringTokenizer(permission, "/");
            String lastToken = "", currentToken, tempPath;
            while (tokenizer.hasMoreTokens()) {
                currentToken = tokenizer.nextToken();
                tempPath = lastToken + "/" + currentToken;
                if (!checkResourceExists(tempPath)) {
                    createRegistryCollection(tempPath, currentToken);

                }
                lastToken = tempPath;
            }
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Failed to creation permission in registry" + permission, e);
        }
    }

    public static void createRegistryCollection(String path, String resourceName)
            throws org.wso2.carbon.registry.api.RegistryException {
        Resource resource = getGovernanceRegistry().newCollection();
        resource.addProperty(PERMISSION_PROPERTY_NAME, resourceName);
        getGovernanceRegistry().beginTransaction();
        getGovernanceRegistry().put(path, resource);
        getGovernanceRegistry().commitTransaction();
    }

    public static boolean checkResourceExists(String path)
            throws RegistryException {
        return getGovernanceRegistry().resourceExists(path);
    }

    public static Registry getGovernanceRegistry() throws RegistryException {
        return WebsocketEventAdaptorServiceDataHolder.getRegistryService()
                    .getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
    }

}
