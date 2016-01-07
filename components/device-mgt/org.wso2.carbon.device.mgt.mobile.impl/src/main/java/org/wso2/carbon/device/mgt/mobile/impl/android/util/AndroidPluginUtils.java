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

package org.wso2.carbon.device.mgt.mobile.impl.android.util;

import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.mobile.impl.android.AndroidDeviceManagementService;

/**
 * Contains utility methods used by Android plugin.
 */
public class AndroidPluginUtils {

    public static License getDefaultLicense() {
        License license = new License();
        license.setName(AndroidDeviceManagementService.DEVICE_TYPE_ANDROID);
        license.setLanguage("en_US");
        license.setVersion("1.0.0");
        license.setText("This End User License Agreement (\"Agreement\") is a legal agreement between you (\"You\") " +
                "and WSO2, Inc., regarding the enrollment of Your personal mobile device (\"Device\") in SoR's " +
                "mobile device management program, and the loading to and removal from Your Device and Your use " +
                "of certain applications and any associated software and user documentation, whether provided in " +
                "\"online\" or electronic format, used in connection with the operation of or provision of services " +
                "to WSO2, Inc.,  BY SELECTING \"I ACCEPT\" DURING INSTALLATION, YOU ARE ENROLLING YOUR DEVICE, AND " +
                "THEREBY AUTHORIZING SOR OR ITS AGENTS TO INSTALL, UPDATE AND REMOVE THE APPS FROM YOUR DEVICE AS " +
                "DESCRIBED IN THIS AGREEMENT.  YOU ARE ALSO EXPLICITLY ACKNOWLEDGING AND AGREEING THAT (1) THIS IS " +
                "A BINDING CONTRACT AND (2) YOU HAVE READ AND AGREE TO THE TERMS OF THIS AGREEMENT.\n" +
                "\n" +
                "IF YOU DO NOT ACCEPT THESE TERMS, DO NOT ENROLL YOUR DEVICE AND DO NOT PROCEED ANY FURTHER.\n" +
                "\n" +
                "You agree that: (1) You understand and agree to be bound by the terms and conditions contained " +
                "in this Agreement, and (2) You are at least 21 years old and have the legal capacity to enter " +
                "into this Agreement as defined by the laws of Your jurisdiction.  SoR shall have the right, " +
                "without prior notice, to terminate or suspend (i) this Agreement, (ii) the enrollment of Your " +
                "Device, or (iii) the functioning of the Apps in the event of a violation of this Agreement or " +
                "the cessation of Your relationship with SoR (including termination of Your employment if You are " +
                "an employee or expiration or termination of Your applicable franchise or supply agreement if You " +
                "are a franchisee of or supplier to the WSO2 WSO2, Inc., system).  SoR expressly reserves all " +
                "rights not expressly granted herein.");
        return license;
    }

}
