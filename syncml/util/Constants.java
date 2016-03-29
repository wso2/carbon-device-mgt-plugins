/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.mdm.services.android.omadm.syncml.util;

/**
 * Constant values used in syncml generator.
 */
public class Constants {

    public static final String APPLICATION_SYNCML = "application/vnd.syncml.dm+xml;charset=utf-8";
    public static final String EXECUTE = "Exec";
    public static final String ATOMIC = "Atomic";
    public static final String ADD = "Add";
    public static final String COMMAND_ID = "CmdID";
    public static final String GET = "Get";
    public static final String DELETE = "Delete";
    public static final String ITEM = "Item";
    public static final String SOURCE = "Source";
    public static final String LOC_URI = "LocURI";
    public static final String LOC_NAME = "LocName";
    public static final String MESSAGE_REFERENCE = "MsgRef";
    public static final String COMMAND_REFERENCE = "CmdRef";
    public static final String COMMAND = "Cmd";
    public static final String TARGET_REFERENCE = "TargetRef";
    public static final String DATA = "Data";
    public static final String STATUS = "Status";
    public static final String SYNC_BODY = "SyncBody";
    public static final String SYNC_HDR = "SyncHdr";
    public static final String VER_DTD = "VerDTD";
    public static final String VER_PROTOCOL = "VerProto";
    public static final String SESSION_ID = "SessionID";
    public static final String MESSAGE_ID = "MsgID";
    public static final String TARGET = "Target";
    public static final String VER_DTD_VALUE = "1.2";
    public static final String VER_PROTOCOL_VALUE = "DM/1.2";
    public static final String ALERT = "Alert";
    public static final String FINAL = "Final";
    public static final String REPLACE = "Replace";
    public static final String META = "Meta";
    public static final String CREDENTIAL = "Cred";
    public static final String FORMAT = "Format";
    public static final String TYPE = "Type";
    public static final String NEXTNONCE = "NextNonce";
    public static final String CHALLENGE = "chal";
    public static final String META_NAMESPACE = "syncml:metinf";
    public static final String XMLNS = "xmlns";
    public static final String RESULTS = "Results";
    public static final String CRED_FORMAT = "b64";
    public static final String CRED_TYPE = "syncml:auth-md5";
    public static final String SEQUENCE = "Sequence";
    public static final String META_FORMAT_INT = "int";
    public static final String META_FORMAT_CHARACTER = "chr";

    /**
     * SynclML response codes
     */
    public final class SyncMLResponseCodes {
        public static final String AUTHENTICATION_ACCEPTED = "212";
        public static final String ACCEPTED = "200";
        public static final String ACCEPTED_FOR_PROCESSING = "202";
        public static final String PIN_NOTFOUND = "405";
        public static final String CREDENTIALS_MISSING = "407";
        public static final String INVALID_CREDENTIALS = "401";
        public static final String LOCKRESET_NOTIFICATION = "Error occurred in Device Lock" +
                " Operation. " + "Please trigger lock-reset operation.";
    }

    /**
     * SynclML parser related constants.
     */
    public final class SyncML {
        private SyncML() {
            throw new AssertionError();
        }

        public static final String SYNCML_SOURCE = "Source";
        public static final String SYNCML_DATA = "Data";
        public static final String SYNCML_CMD = "Cmd";
        public static final String SYNCML_CHAL = "ChallengeTag";
        public static final String SYNCML_CMD_ID = "CmdID";
        public static final String SYNCML_CMD_REF = "CmdRef";
        public static final String SYNCML_MESSAGE_REF = "MsgRef";
        public static final String SYNCML_LOCATION_URI = "LocURI";
        public static final String SYNCML_TARGET_REF = "TargetRef";
    }

    /**
     * Configuration Service Provider (CSP) URIs
     */
    public final class ConfigurationServiceProviders {
        private ConfigurationServiceProviders() {
            throw new AssertionError();
        }

        public static final String DEVICE_ID = "./DevInfo/DevId";
        public static final String MANUFACTURER = "./DevInfo/Man";
        public static final String DEVICE_MODEL = "./DevInfo/Mod";
        public static final String DEVICE_MODEL_VERSION = "./DevInfo/DmV";
        public static final String LANGUAGE = "./DevInfo/Lang";
        public static final String IMSI = "./DevInfo/Ext/Android/IMSI";
        public static final String IMEI = "./DevInfo/Ext/Android/IMEI";
        public static final String SOFTWARE_VERSION = "./DevDetail/SwV";
        public static final String VENDOR = "./DevDetail/OEM";
        public static final String MAC_ADDRESS = "./DevDetail/Ext/WLANMACAddress";
        public static final String DEVICE_NAME = "./DevDetail/Ext/Android/DeviceName";
        public static final String CHANNEL_URI = "./Vendor/MSFT/DMClient/Provider/MobiCDMServer/Push/ChannelURI";
        public static final String LOCK_PIN = "./Vendor/MSFT/RemoteLock/NewPINValue";
        public static final String LOCK_RESET = "./Vendor/MSFT/RemoteLock/LockAndResetPIN";
        public static final String CAMERA = "./Vendor/MSFT/PolicyManager/My/Camera/AllowCamera";
        public static final String CAMERA_STATUS = "./Vendor/MSFT/PolicyManager/Device/Camera/AllowCamera";
        public static final String ENCRYPT_STORAGE_STATUS = "./Vendor/MSFT/PolicyManager/Device/Security/RequireDeviceEncryption";
        public static final String DEVICE_PASSWORD_STATUS = "./Vendor/MSFT/PolicyManager/Device/DeviceLock/DevicePasswordEnabled";
        public static final String DEVICE_PASSCODE_DELETE = "./Vendor/MSFT/PolicyManager/My/DeviceLock";
        public static final String LONGITUDE = "./Vendor/MSFT/RemoteFind/Location/Longitude";
        public static final String LATITUDE = "./Vendor/MSFT/RemoteFind/Location/Latitude";
    }

    /**
     * SynclML Tag related constants.
     */
    public final class SyncMLTags {
        private SyncMLTags() {
            throw new AssertionError();
        }

        public static final String EXECUTE = "Exec";
        public static final String ATOMIC = "Atomic";
        public static final String ADD = "Add";
        public static final String COMMAND_ID = "CmdID";
        public static final String GET = "Get";
        public static final String DELETE = "Delete";
        public static final String ITEM = "Item";
        public static final String SOURCE = "Source";
        public static final String LOC_URI = "LocURI";
        public static final String LOC_NAME = "LocName";
        public static final String MESSAGE_REFERENCE = "MsgRef";
        public static final String COMMAND_REFERENCE = "CmdRef";
        public static final String COMMAND = "Cmd";
        public static final String TARGET_REFERENCE = "TargetRef";
        public static final String DATA = "Data";
        public static final String STATUS = "Status";
        public static final String SYNC_BODY = "SyncBody";
        public static final String SYNC_HDR = "SyncHdr";
        public static final String VER_DTD = "VerDTD";
        public static final String VER_PROTOCOL = "VerProto";
        public static final String SESSION_ID = "SessionID";
        public static final String MESSAGE_ID = "MsgID";
        public static final String TARGET = "Target";
        public static final String VER_DTD_VALUE = "1.2";
        public static final String VER_PROTOCOL_VALUE = "DM/1.2";
        public static final String ALERT = "Alert";
        public static final String FINAL = "Final";
        public static final String REPLACE = "Replace";
        public static final String META = "Meta";
        public static final String CREDENTIAL = "Cred";
        public static final String FORMAT = "Format";
        public static final String TYPE = "Type";
        public static final String NEXTNONCE = "NextNonce";
        public static final String CHALLENGE = "chal";
        public static final String META_NAMESPACE = "syncml:metinf";
        public static final String XMLNS = "xmlns";
        public static final String RESULTS = "Results";
        public static final String CRED_FORMAT = "b64";
        public static final String CRED_TYPE = "syncml:auth-md5";
        public static final String SEQUENCE = "Sequence";
        public static final String META_FORMAT_INT = "int";
        public static final String META_FORMAT_CHARACTER = "chr";
    }

    /**
     * General SynclML builder related constants.
     */
    public final class SyncMLGeneralInfo {
        private SyncMLGeneralInfo() {
            throw new AssertionError();
        }

        public static final String PROVIDER_ID = "MobiCDMServer";
        public static final String SERVER_SECRET = "dummy";
        public static final String INITIAL_NONCE = "ZHVtbXk=";
        public static final String DISENROLL_ALERT_DATA = "1226";
        public static final String INITIAL_ALERT_DATA = "1201";
        public static final int EMPTY = 0;
        public static final String SYNCML_ROOT_ELEMENT_NAME = "SyncML";
        public static final String XMLNS_SYNCML = "SYNCML:SYNCML1.2";
        public static final String UTF_8 = "UTF-8";
        public static final String MD5 = "MD5";
        public static final String YES = "yes";
    }

    /**
     * SyncmML message related constants.
     */
    public final class SyncmlMessageCodes {
        public static final int replaceCommandId = 300;
        public static final int elementCommandId = 75;
        public static final int atomicCommandId = 400;
        public static final int addCommandId = 90;
    }

    /**
     * Device Operation codes.
     */
    public final class OperationCodes {
        private OperationCodes() {
            throw new AssertionError();
        }

        public static final String DEVICE_LOCK = "DEVICE_LOCK";
        public static final String DISENROLL = "DISENROLL";
        public static final String DEVICE_RING = "DEVICE_RING";
        public static final String WIPE_DATA = "WIPE_DATA";
        public static final String ENCRYPT_STORAGE = "ENCRYPT_STORAGE";
        public static final String LOCK_RESET = "LOCK_RESET";
        public static final String PIN_CODE = "LOCK_PIN";
        public static final String CAMERA = "CAMERA";
        public static final String PASSCODE_POLICY = "PASSCODE_POLICY";
        public static final String PASSWORD_EXPIRE = "PASSWORD_EXPIRE";
        public static final String PASSWORD_HISTORY = "PASSWORD_HISTORY";
        public static final String MAX_PASSWORD_INACTIVE_TIME = "MAX_PASSWORD_INACTIVE_TIME";
        public static final String MIN_PASSWORD_COMPLEX_CHARACTERS = "MIN_PASSWORD_COMPLEX_CHARACTERS";
        public static final String ALPHANUMERIC_PASSWORD = "ALPHANUMERIC_PASSWORD";
        public static final String SIMPLE_PASSWORD = "SIMPLE_PASSWORD";
        public static final String MIN_PASSWORD_LENGTH = "MIN_PASSWORD_LENGTH";
        public static final String DEVICE_PASSWORD_ENABLE = "DEVICE_PASSWORD_ENABLE";
        public static final String PASSWORD_MAX_FAIL_ATTEMPTS = "PASSWORD_MAX_FAIL_ATTEMPTS";
        public static final String MONITOR = "MONITOR";
        public static final String CAMERA_STATUS = "CAMERA_STATUS";
        public static final String POLICY_BUNDLE = "POLICY_BUNDLE";
        public static final String ENCRYPT_STORAGE_STATUS = "ENCRYPT_STORAGE_STATUS";
        public static final String DEVICE_PASSWORD_STATUS = "DEVICE_PASSWORD_STATUS";
        public static final String DEVICE_PASSCODE_DELETE = "DEVICE_PASSCODE_DELETE";
    }
}
