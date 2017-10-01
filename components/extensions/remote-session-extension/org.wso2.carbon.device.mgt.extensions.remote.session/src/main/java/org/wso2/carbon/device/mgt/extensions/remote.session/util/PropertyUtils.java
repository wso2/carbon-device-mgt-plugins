/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.device.mgt.extensions.remote.session.util;

import org.wso2.carbon.device.mgt.extensions.remote.session.exception.RemoteSessionManagementException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for reading web socket url parameters
 */
public class PropertyUtils {

    /**
     * Replace URL with placeholders with properties
     * @param urlWithPlaceholders URL
     * @return replaced url
     * @throws RemoteSessionManagementException
     */
    public static String replaceProperty(String urlWithPlaceholders) throws RemoteSessionManagementException {
        String regex = "\\$\\{(.*?)\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matchPattern = pattern.matcher(urlWithPlaceholders);
        while (matchPattern.find()) {
            String sysPropertyName = matchPattern.group(1);
            String sysPropertyValue = System.getProperty(sysPropertyName);
            if (sysPropertyValue != null && !sysPropertyName.isEmpty()) {
                urlWithPlaceholders = urlWithPlaceholders.replaceAll("\\$\\{(" + sysPropertyName + ")\\}", sysPropertyValue);
            } else {
                throw new RemoteSessionManagementException("System property - " + sysPropertyName
                        + " is not defined, hence cannot resolve : " + urlWithPlaceholders);
            }
        }
        return urlWithPlaceholders;
    }
}
