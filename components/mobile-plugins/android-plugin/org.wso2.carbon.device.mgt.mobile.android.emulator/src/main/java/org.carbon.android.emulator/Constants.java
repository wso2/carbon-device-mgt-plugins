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

package org.carbon.android.emulator;

import java.io.File;

/**
 * This class has the constant strings used and the system properties.
 */
class Constants {
    static final String OS_NAME_PROPERTY = "os.name";
    static final String USER_HOME_PROPERTY = "user.home";
    static final String USER_DIRECTORY_PROPERTY = "user.dir";
    static final String MAC_OS = "macosx";
    static final String MAC = "mac";
    static final String WINDOWS_OS = "windows";
    static final String WINDOWS_EXTENSION_EXE = ".exe";
    static final String WINDOWS_EXTENSION_BAT = ".bat";
    static final String NAME = "name=";

    static final String SDK_TOOLS_URL = "sdk.tools.url";
    static final String PLATFORM_TOOLS_URL = "platform.tools.url";
    static final String BUILD_TOOL_URL = "build.tools.url";
    static final String PLATFORM_URL = "platform.url";
    static final String SYSTEM_IMAGE_URL = "sys.img.url";
    static final String HAXM_URL = "haxm.url";

    static final String DOWNLOADED_BUILD_TOOL_NAME = "downloaded.build.tool.name"; //"android-7.1.1";
    static final String BUILD_TOOLS_VERSION = "build.tool.version"; //"25.0.2";
    static final String DOWNLOADED_PLATFORM_NAME = "downloaded.platform.name"; //"android-6.0";
    static final String TARGET_VERSION =  "target.version"; //"android-23";
    static final String OS_TARGET = "os.target";//"x86";


    static final String WSO2_AVD_NAME = "WSO2_AVD";
    static final String APK_LOCATION = File.separator + "resources" + File.separator + "android-agent.apk";
    static final String WSO2_CONFIG_LOCATION = File.separator + "resources" + File.separator + "config.ini";

    static final String MAC_HAXM_EXTENSION = ".sh";
    static final String MAC_DARWIN = "darwin";

//    class AndroidCommands{
//        static final String LIST_AVD = "-list-avds";
//    }
}
