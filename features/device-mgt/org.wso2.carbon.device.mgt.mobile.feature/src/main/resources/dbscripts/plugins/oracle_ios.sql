-- -----------------------------------------------------
-- Table `IOS_FEATURE`
-- -----------------------------------------------------
CREATE TABLE IOS_FEATURE (
  FEATURE_ID NUMBER(10) NOT NULL,
  CODE VARCHAR2(45) NOT NULL,
  NAME VARCHAR2(100) NULL,
  DESCRIPTION VARCHAR2(200) NULL,
  PRIMARY KEY (FEATURE_ID) )
;

-- Generate ID using sequence and trigger
CREATE SEQUENCE IOS_FEATURE_seq START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER IOS_FEATURE_seq_tr
BEFORE INSERT ON IOS_FEATURE FOR EACH ROW
WHEN (NEW.FEATURE_ID IS NULL)
  BEGIN
    SELECT IOS_FEATURE_seq.NEXTVAL INTO :NEW.FEATURE_ID FROM DUAL;
  END;
/


-- -----------------------------------------------------
-- Table `IOS_DEVICE`
-- -----------------------------------------------------
CREATE  TABLE IOS_DEVICE (
  MOBILE_DEVICE_ID VARCHAR2(45) NOT NULL,
  APNS_PUSH_TOKEN VARCHAR2(100) DEFAULT NULL NULL,
  MAGIC_TOKEN VARCHAR2(100) DEFAULT NULL NULL,
  MDM_TOKEN VARCHAR2(100) DEFAULT NULL NULL,
  UNLOCK_TOKEN VARCHAR2(2000) DEFAULT NULL NULL,
  CHALLENGE_TOKEN VARCHAR2(45) DEFAULT NULL NULL,
  DEVICE_INFO VARCHAR2(8000) DEFAULT NULL NULL,
  SERIAL VARCHAR2(45) DEFAULT NULL NULL,
  PRODUCT VARCHAR2(45) DEFAULT NULL NULL,
  IMEI VARCHAR2(45) DEFAULT NULL NULL,
  VERSION VARCHAR2(45) DEFAULT NULL NULL,
  MAC_ADDRESS VARCHAR2(45) DEFAULT NULL NULL,
  DEVICE_NAME VARCHAR2(100) DEFAULT NULL NULL,
  ICCID VARCHAR2(45) DEFAULT NULL NULL,
  LATITUDE VARCHAR2(45) DEFAULT NULL NULL,
  LONGITUDE VARCHAR2(45) DEFAULT NULL NULL,
  PRIMARY KEY (MOBILE_DEVICE_ID) )
;


-- -----------------------------------------------------
-- TODO remove this later
-- -----------------------------------------------------

INSERT INTO IOS_FEATURE (CODE, NAME, DESCRIPTION)
  SELECT 'INSTALL_ENTERPRISE_APPLICATION', 'Install Enterprise App', 'Install Enterprise App' FROM dual UNION ALL
  SELECT 'INSTALL_STORE_APPLICATION', 'Install Public App', 'Install Public App' FROM dual UNION ALL
  SELECT 'REMOVE_APPLICATION', 'Uninstall App', 'Uninstall App' FROM dual UNION ALL
  SELECT 'DEVICE_LOCK', 'Device Lock', 'Device Lock' FROM dual UNION ALL
  SELECT 'CELLULAR', 'Cellular', 'Cellular' FROM dual UNION ALL
  SELECT 'APN', 'APN', 'APN' FROM dual UNION ALL
  SELECT 'RESTRICTION', 'Restrictions', 'Restrictions operation' FROM dual UNION ALL
  SELECT 'WIFI', 'Wifi', 'Wifi' FROM dual UNION ALL
  SELECT 'DEVICE_INFO', 'Device Info', 'Device info operation' FROM dual UNION ALL
  SELECT 'AIR_PLAY', 'Air Play', 'AirPlay operation' FROM dual UNION ALL
  SELECT 'LOCATION', 'Location', 'Fetch location' FROM dual UNION ALL
  SELECT 'ALARM', 'Alarm', 'Alarm device' FROM dual UNION ALL
  SELECT 'APPLICATION_LIST', 'Application list', 'Application list' FROM dual UNION ALL
  SELECT 'PROFILE_LIST', 'Profile List', 'Profile list' FROM dual UNION ALL
  SELECT 'REMOVE_PROFILE', 'Remove Profile', 'Remove profile' FROM dual UNION ALL
  SELECT 'CLEAR_PASSCODE', 'Clear Passcode', 'Clear passcode' FROM dual UNION ALL
  SELECT 'CALDAV', 'CalDev', 'Setup CalDav' FROM dual UNION ALL
  SELECT 'CALENDAR_SUBSCRIPTION', 'Calendar Subscriptions', 'Calendar subscription' FROM dual UNION ALL
  SELECT 'PASSCODE_POLICY', 'Passcode Policy', 'Passcode policy' FROM dual UNION ALL
  SELECT 'EMAIL', 'Email', 'Email operation' FROM dual UNION ALL
  SELECT 'LDAP', 'LDAP', 'LDAP operation' FROM dual UNION ALL
  SELECT 'WEB_CLIP', 'Web Clip', 'Web clip operation' FROM dual UNION ALL
  SELECT 'VPN', 'VPN', 'VPN operation' FROM dual UNION ALL
  SELECT 'PER_APP_VPN', 'Per App VPN', 'Per app VPN operation' FROM dual UNION ALL
  SELECT 'APP_TO_PER_APP_VPN_MAPPING', 'VPN App mapping', 'App to per app VPN mapping operation' FROM dual UNION ALL
  SELECT 'ENTERPRISE_WIPE', 'Enterprise Wipe', 'Enterprise wipe operation' FROM dual UNION ALL
  SELECT 'APP_LOCK', 'App lock', 'App lock operation' FROM dual UNION ALL
  SELECT 'GET_RESTRICTIONS', 'Get restrictions', 'Get restrictions operation' FROM dual;
