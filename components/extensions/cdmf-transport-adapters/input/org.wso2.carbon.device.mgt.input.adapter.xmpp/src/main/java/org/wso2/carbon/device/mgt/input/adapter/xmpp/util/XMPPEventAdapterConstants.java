/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.input.adapter.xmpp.util;


/**
 * This holds the constants related to xmpp event adapter.
 */
public class XMPPEventAdapterConstants {

    public static final String ADAPTER_TYPE_XMPP = "xmpp";

    //static properties
    public static final String ADAPTER_CONF_HOST = "host";
    public static final String ADAPTER_CONF_HOST_HINT = "host.hint";
    public static final String ADAPTER_CONF_PORT = "port";
    public static final String ADAPTER_CONF_PORT_HINT = "port.hint";
    public static final String ADAPTER_CONF_USERNAME = "username";
    public static final String ADAPTER_CONF_USERNAME_HINT = "username.hint";
    public static final String ADAPTER_CONF_PASSWORD = "password";
    public static final String ADAPTER_CONF_PASSWORD_HINT = "password.hint";
    public static final String ADAPTER_CONF_RESOURCE = "resource";
    public static final String ADAPTER_CONF_RESOURCE_HINT = "resource.hint";
    public static final String ADAPTER_CONF_TIMEOUT_INTERVAL = "timeoutInterval";
    public static final String ADAPTER_CONF_TIMEOUT_INTERVAL_HINT = "timeoutInterval.hint";
    public static final String ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME = "contentValidator";
    public static final String ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME_HINT = "contentValidator.hint";
    public static final String ADAPTER_CONF_CONTENT_TRANSFORMER_CLASSNAME = "contentTransformer";
    public static final String ADAPTER_CONF_CONTENT_TRANSFORMER_CLASSNAME_HINT = "contentTransformer.hint";
    public static final String ADAPTER_CONF_RECIEVER_JID = "jid";
    public static final String ADAPTER_CONF_RECIEVER_JID_HINT = "jid.hint";
    public static final int DEFAULT_XMPP_PORT = 5222;
    public static final int DEFAULT_TIMEOUT_INTERVAL = 5000;

    public static int INITIAL_RECONNECTION_DURATION = 4000;
    public static final int RECONNECTION_PROGRESS_FACTOR = 2;

    public static final String DEFAULT = "default";
    public static final String FROM_KEY = "from";
    public static final String SUBJECT_KEY = "subject";
}
