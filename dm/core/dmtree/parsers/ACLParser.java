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

package org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.parsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class handles the parsing of ACL Properties in Management Nodes
 */
public final class ACLParser {

    /**
     * Constants for parsing ACL Strings
     */
    private static final String ACCESS_TYPE_DELIMITER = "&";
    private static final String SERVER_ID_DELIMITER = "+";
    private static final String WILDCARD_CHAR = "*";
    private static final String ACCESS_TYPE_VALUE_DELIMITER = "=";
    private static final String WHITESPACE_CHAR = "\\s";

    /**
     * Validates ACL Strings
     * @param acl - The ACL string
     * @return - 'True' if valid, or else 'False'
     */
    public static Boolean validateACL(String acl) {

        if (acl == null || acl.isEmpty()) {
            return false;
        } else {
            String[] accessLevels = acl.split(ACCESS_TYPE_DELIMITER);
            //String[] accessLevelValues = acl.split("");

            for (String str : accessLevels) {
                if (str.contains(ACCESS_TYPE_DELIMITER) || str.contains(WHITESPACE_CHAR)) {
                    return false;
                }
            }
            //TODO : More validation scenarios should be added
            return false;
        }
    }

    /**
     * Extracts all the available access types in an ACL
     * @param acl - The ACL string
     * @return - A list of available Access Type values
     */
    public List<String> getAvailableAccessTypes(String acl) {

        String[] accessLevels = acl.split(ACCESS_TYPE_DELIMITER);
        List<String> accessTypes = new ArrayList<>();

        for (String str : accessLevels) {
            String[] accessValues = str.split(ACCESS_TYPE_VALUE_DELIMITER);
            accessTypes.add(accessValues[0]);
        }
        return accessTypes;
    }

    /**
     * Finds all the access types granted for a given Server ID
     * @param acl - The ACL string
     * @param serverId - Server ID
     * @return - A list of available access types for a given Server ID
     */
    public List<String> getServerAccessTypes(String acl, String serverId) {

        String[] accessLevels = acl.split(ACCESS_TYPE_DELIMITER);
        List<String> accessTypes = new ArrayList<>();

        for (String str : accessLevels) {
            if (str.contains(serverId) || str.contains(WILDCARD_CHAR)) {
                String[] tempStr = str.split(ACCESS_TYPE_VALUE_DELIMITER);
                accessTypes.add(tempStr[0]);
            }
        }
        return accessTypes;
    }

    /**
     * Finds all the server IDs specified to a given access type
     * @param acl - The ACL string
     * @param accessType - Access Type
     * @return - A list of server IDs specified to the access type
     */
    public List<String> getAccessTypeServerIDs(String acl, String accessType) {

        String[] accessLevels = acl.split(ACCESS_TYPE_DELIMITER);
        List<String> serverIDs = new ArrayList<>();

        for (String levelStr : accessLevels) {
            if (levelStr.contains(accessType)) {
                String[] tempStr = levelStr.split(ACCESS_TYPE_VALUE_DELIMITER);
                serverIDs.addAll(Arrays.asList(tempStr[1]));
            }
        }
        return serverIDs;
    }

    /**
     * Checks whether a server has access to a specific access type
     * @param acl - The ACL string
     * @param accessType - Access Type
     * @param serverId - Server ID
     * @return - 'True' if the server has access, or else 'False'
     */
    public Boolean hasAccess(String acl, String accessType, String serverId) {

        String[] accessLevels = acl.split(ACCESS_TYPE_DELIMITER);

        for (String str : accessLevels) {
            if (str.contains(accessType) && (str.contains(serverId)) || str.
                    contains(WILDCARD_CHAR)) {
                return true;
            }
        }
        return false;
    }

}
