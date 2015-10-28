-- -----------------------------------------------------
-- Table `AD_DEVICE`
-- -----------------------------------------------------
CREATE  TABLE AD_DEVICE (
  ANDROID_DEVICE_ID VARCHAR2(45) NOT NULL,
  GCM_TOKEN VARCHAR2(45) DEFAULT NULL NULL,
  DEVICE_INFO VARCHAR2(8000) DEFAULT NULL NULL,
  IMEI VARCHAR2(45) DEFAULT NULL NULL,
  IMSI VARCHAR2(45) DEFAULT NULL NULL,
  OS_VERSION VARCHAR2(45) DEFAULT NULL NULL,
  DEVICE_MODEL VARCHAR2(45) DEFAULT NULL NULL,
  VENDOR VARCHAR2(45) DEFAULT NULL NULL,
  LATITUDE VARCHAR2(45) DEFAULT NULL NULL,
  LONGITUDE VARCHAR2(45) DEFAULT NULL NULL,
  SERIAL VARCHAR2(45) DEFAULT NULL NULL,
  MAC_ADDRESS VARCHAR2(45) DEFAULT NULL NULL,
  DEVICE_NAME VARCHAR2(100) DEFAULT NULL NULL,
  PRIMARY KEY (ANDROID_DEVICE_ID));

-- -----------------------------------------------------
-- Table `AD_FEATURE`
-- -----------------------------------------------------
CREATE TABLE AD_FEATURE (
  ID NUMBER(10) NOT NULL,
  CODE VARCHAR2(45) NOT NULL,
  NAME VARCHAR2(100) NULL,
  DESCRIPTION VARCHAR2(200) NULL,
  PRIMARY KEY (ID));

-- Generate ID using sequence and trigger
CREATE SEQUENCE AD_FEATURE_seq START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER AD_FEATURE_seq_tr
 BEFORE INSERT ON AD_FEATURE FOR EACH ROW
 WHEN (NEW.ID IS NULL)
BEGIN
 SELECT AD_FEATURE_seq.NEXTVAL INTO :NEW.ID FROM DUAL;
END;
/

-- -----------------------------------------------------
-- TODO remove this later
-- -----------------------------------------------------
INSERT INTO AD_FEATURE (CODE, NAME, DESCRIPTION)
  SELECT 'DEVICE_LOCK', 'Device Lock', 'Lock the device' FROM dual UNION ALL
  SELECT 'DEVICE_LOCATION', 'Location', 'Request coordinates of device location' FROM dual UNION ALL
  SELECT 'WIFI', 'Wifi', 'Setting up wifi configuration' FROM dual UNION ALL
  SELECT 'CAMERA', 'Camera', 'Enable or disable camera' FROM dual UNION ALL
  SELECT 'EMAIL', 'Email', 'Configure email settings' FROM dual UNION ALL
  SELECT 'DEVICE_MUTE', 'Mute', 'Enable mute in the device' FROM dual UNION ALL
  SELECT 'DEVICE_INFO', 'Device Info', 'Request device information' FROM dual UNION ALL
  SELECT 'ENTERPRISE_WIPE', 'Enterprise Wipe', 'Remove enterprise applications' FROM dual UNION ALL
  SELECT 'CLEAR_PASSWORD', 'Clear Password', 'Clear current password' FROM dual UNION ALL
  SELECT 'WIPE_DATA', 'Wipe Data', 'Factory reset the device' FROM dual UNION ALL
  SELECT 'APPLICATION_LIST', 'Application List', 'Request list of current installed applications' FROM dual UNION ALL
  SELECT 'CHANGE_LOCK_CODE', 'Change Lock-code', 'Change current lock code' FROM dual UNION ALL
  SELECT 'INSTALL_APPLICATION', 'Install App', 'Install Enterprise or Market application' FROM dual UNION ALL
  SELECT 'UNINSTALL_APPLICATION', 'Uninstall App', 'Uninstall application' FROM dual UNION ALL
  SELECT 'BLACKLIST_APPLICATIONS', 'Blacklist app', 'Blacklist applications' FROM dual UNION ALL
  SELECT 'ENCRYPT_STORAGE', 'Encrypt storage', 'Encrypt storage' FROM dual UNION ALL
  SELECT 'DEVICE_RING', 'Ring', 'Ring the device' FROM dual UNION ALL
  SELECT 'PASSCODE_POLICY', 'Password Policy', 'Set passcode policy' FROM dual UNION ALL
  SELECT 'NOTIFICATION', 'Message', 'Send message' FROM dual;
