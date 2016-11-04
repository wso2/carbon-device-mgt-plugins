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
package org.wso2.carbon.device.mgt.output.adapter.xmpp.util;


public final class XMPPEventAdapterConstants {

    private XMPPEventAdapterConstants() {
    }

    public static final String ADAPTER_TYPE_XMPP = "xmpp";

    //dynamic properties
    public static final String ADAPTER_CONF_JID = "jid";
    public static final String ADAPTER_CONF_JID_HINT = "jid.hint";
    public static final String ADAPTER_CONF_SUBJECT = "subject";
    public static final String ADAPTER_CONF_SUBJECT_HINT = "subject.hint";
    public static final String ADAPTER_CONF_MESSAGETYPE = "messageType";
    public static final String ADAPTER_CONF_MESSAGETYPE_HINT = "messageType.hint";

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
    public static final int DEFAULT_XMPP_PORT = 5222;
    public static final int DEFAULT_TIMEOUT_INTERVAL = 5000;

    //global properties
    public static final int DEFAULT_MIN_THREAD_POOL_SIZE = 8;
    public static final int DEFAULT_MAX_THREAD_POOL_SIZE = 100;
    public static final int DEFAULT_EXECUTOR_JOB_QUEUE_SIZE = 2000;
    public static final long DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS = 20000;
    public static final String ADAPTER_MIN_THREAD_POOL_SIZE_NAME = "minThread";
    public static final String ADAPTER_MAX_THREAD_POOL_SIZE_NAME = "maxThread";
    public static final String ADAPTER_KEEP_ALIVE_TIME_NAME = "keepAliveTimeInMillis";
    public static final String ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME = "jobQueueSize";

    public static final class MessageType {
        public static final String NORMAL = "normal";
        public static final String CHAT = "chat";
        public static final String GROUP_CHAT = "groupchat";
        public static final String HEADLINE = "headline";
        public static final String ERROR = "error";

    }

}
