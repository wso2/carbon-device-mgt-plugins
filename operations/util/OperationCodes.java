package org.wso2.carbon.mdm.services.android.omadm.operations.util;

/**
 * OMADM Operation CSPs
 */
public class OperationCodes {

        public static final String DEVICE_LOCK = "DEVICE_LOCK";
        public static final String DISENROLL = "DISENROLL";
        public static final String DEVICE_RING = "DEVICE_RING";
        public static final String WIPE_DATA = "WIPE_DATA";
        public static final String ENCRYPT_STORAGE = "ENCRYPT_STORAGE";
        public static final String LOCK_RESET = "LOCK_RESET";
        public static final String DEVICE_MUTE = "DEVICE_MUTE";
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

    public enum Info {
        CAMERA("./DevConfig/Camera"),
        CAMERA_STATUS("./DevConfig/Camera"),
        DEV_ID("./DevInfo/DevId"),
        MANUFACTURER("./DevInfo/Man"),
        DEVICE_MODEL("./DevInfo/Mod"),
        DM_VERSION("./DevInfo/DmV"),
        LANGUAGE("./DevInfo/Lang"),
        IMSI("./DevInfo/Ext/Identity/IMSI"),
        IMEI("./DevInfo/Ext/Identity/IMEI"),
        SOFTWARE_VERSION("./DevDetail/SwV"),
        VENDOR("./DevDetail/OEM"),
        MAC_ADDRESS("./DevDetail/Ext/WLANMACAddress"),
        RESOLUTION("./DevDetail/Ext/Microsoft/Resolution"),
        DEVICE_NAME("./DevDetail/Ext/Microsoft/DeviceName"),
        LOCK_PIN("./Vendor/MSFT/RemoteLock/NewPINValue"),
        LOCK_RESET("./Vendor/MSFT/RemoteLock/LockAndResetPIN"),
        LONGITUDE("./DevInfo/Ext/Location/Longitude"),
        ENCRYPT_STORAGE_STATUS("./Vendor/MSFT/PolicyManager/Device/Security/RequireDeviceEncryption"),
        DEVICE_PASSCODE_STATUS("./Vendor/MSFT/PolicyManager/My/DeviceLock/DevicePasswordEnabled"),
        LATITUDE("./DevInfo/Ext/Location/Latitude");

        private final String code;

        Info(String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }

    }

    public enum Command {
        DEVICE_RING("./DevCommand/Ring"),
        DEVICE_LOCK("./DevCommand/Lock"),
        DEVICE_WIPE("./DevCommand/Wipe"),
        DEVICE_MUTE("./DevCommand/Mute"),
        LOCK_RESET("./DevCommand/Lock/Reset"),
        DISENROLL("./Vendor/MSFT/DMClient/Unenroll"),
        CAMERA("./Vendor/MSFT/PolicyManager/My/Camera/AllowCamera"),
        ENCRYPT_STORAGE("./Vendor/MSFT/PolicyManager/My/Security/RequireDeviceEncryption"),
        CAMERA_STATUS("./Vendor/MSFT/PolicyManager/Device/Camera/AllowCamera"),
        ENCRYPT_STORAGE_STATUS("./Vendor/MSFT/PolicyManager/Device/Security/RequireDeviceEncryption"),
        DEVICE_PASSWORD_ENABLE("./Vendor/MSFT/PolicyManager/My/DeviceLock/DevicePasswordEnabled"),
        DEVICE_PASSCODE_DELETE("./Vendor/MSFT/PolicyManager/My/DeviceLock");

        private final String code;

        Command(String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }

    }

    public enum Configure {
        WIFI("./Vendor/MSFT/WiFi/Profile/MyNetwork/WlanXml"),
        CAMERA("./DevConfig/Camera"),
        CAMERA_STATUS("./DevConfig/Camera"),
        ENCRYPT_STORAGE("./Vendor/MSFT/PolicyManager/My/Security/RequireDeviceEncryption"),
        ENCRYPT_STORAGE_STATUS("./Vendor/MSFT/PolicyManager/Device/Security/RequireDeviceEncryption"),
        PASSWORD_MAX_FAIL_ATTEMPTS("./Vendor/MSFT/PolicyManager/My/DeviceLock/MaxDevicePasswordFailedAttempts"),
        DEVICE_PASSWORD_ENABLE("./Vendor/MSFT/PolicyManager/My/DeviceLock/DevicePasswordEnabled"),
        SIMPLE_PASSWORD("./Vendor/MSFT/PolicyManager/My/DeviceLock/AllowSimpleDevicePassword"),
        MIN_PASSWORD_LENGTH("./Vendor/MSFT/PolicyManager/My/DeviceLock/MinDevicePasswordLength"),
        Alphanumeric_PASSWORD("./Vendor/MSFT/PolicyManager/My/DeviceLock/AlphanumericDevicePasswordRequired"),
        PASSWORD_EXPIRE("./Vendor/MSFT/PolicyManager/My/DeviceLock/DevicePasswordExpiration"),
        PASSWORD_HISTORY("./Vendor/MSFT/PolicyManager/My/DeviceLock/DevicePasswordHistory"),
        MAX_PASSWORD_INACTIVE_TIME("./Vendor/MSFT/PolicyManager/My/DeviceLock/MaxInactivityTimeDeviceLock"),
        MIN_PASSWORD_COMPLEX_CHARACTERS("./Vendor/MSFT/PolicyManager/My/DeviceLock/MinDevicePasswordComplexCharacters");

        private final String code;

        Configure(String code) {
            this.code = code;
        }

        public String getCode() {
            return this.code;
        }
    }

    /**
     * Policy Configuration related constants.
     */
    public final class PolicyConfigProperties {
        private PolicyConfigProperties() {
            throw new AssertionError();
        }

        public static final String POLICY_ENABLE = "enabled";
        public static final String ENCRYPTED_ENABLE = "encrypted";
        public static final String ENABLE_PASSWORD = "enablePassword";
    }
}
