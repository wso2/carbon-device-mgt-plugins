-- -----------------------------------------------------
-- Table `WINDOWS_FEATURE`
-- -----------------------------------------------------
CREATE TABLE WINDOWS_FEATURE (
  FEATURE_ID NUMBER(10) NOT NULL,
  CODE VARCHAR2(45) NOT NULL,
  NAME VARCHAR2(100) NULL,
  DESCRIPTION VARCHAR2(200) NULL,
  PRIMARY KEY (FEATURE_ID))
 ;

-- Generate ID using sequence and trigger
CREATE SEQUENCE WINDOWS_FEATURE_seq START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE TRIGGER WINDOWS_FEATURE_seq_tr
 BEFORE INSERT ON WINDOWS_FEATURE FOR EACH ROW
 WHEN (NEW.FEATURE_ID IS NULL)
BEGIN
 SELECT WINDOWS_FEATURE_seq.NEXTVAL INTO :NEW.FEATURE_ID FROM DUAL;
END;
/

-- -----------------------------------------------------
-- Table `WINDOWS_DEVICE`
-- -----------------------------------------------------
CREATE  TABLE WINDOWS_DEVICE (
  MOBILE_DEVICE_ID VARCHAR2(45) NOT NULL,
  CHANNEL_URI VARCHAR2(100) DEFAULT NULL NULL,
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
  PRIMARY KEY (MOBILE_DEVICE_ID));

-- -----------------------------------------------------
-- TODO remove this later
-- -----------------------------------------------------
INSERT INTO WINDOWS_FEATURE (CODE, NAME, DESCRIPTION)
  SELECT 'INSTALL_ENTERPRISE_APPLICATION', 'Install Enterprise App', 'Install Enterprise App' FROM dual UNION ALL
 ('INSTALL_STORE_APPLICATION', 'Install Public App', 'Install Public App'),  
('REMOVE_APPLICATION', 'Uninstall App', 'Uninstall App'),
 ('DEVICE_LOCK', 'Device Lock', 'Device Lock'),
 ('CELLULAR', 'Cellular', 'Cellular'),
 ('APN', 'APN', 'APN'),
 ('RESTRICTION', 'Restrictions', 'Restrictions operation'),
 ('WIFI', 'Wifi', 'Wifi'),
 ('DEVICE_INFO', 'Device Info', 'Device info operation'),  
('AIR_PLAY', 'Air Play', 'AirPlay operation'),  
('LOCATION', 'Location', 'Fetch location'),  
('ALARM', 'Alarm', 'Alarm device'),
('APPLICATION_LIST', 'Application list', 'Application list'),
('PROFILE_LIST', 'Profile List', 'Profile list'),
('REMOVE_PROFILE', 'Remove Profile', 'Remove profile'),
('CLEAR_PASSCODE', 'Clear Passcode', 'Clear passcode'),
('CALDAV', 'CalDev', 'Setup CalDav'),
('CALENDAR_SUBSCRIPTION', 'Calendar Subscriptions', 'Calendar subscription'),
('PASSCODE_POLICY', 'Passcode Policy', 'Passcode policy'),
('EMAIL', 'Email', 'Email operation'),
('LDAP', 'LDAP', 'LDAP operation'),
('WEB_CLIP', 'Web Clip', 'Web clip operation'),
('VPN', 'VPN', 'VPN operation'),
('PER_APP_VPN', 'Per App VPN', 'Per app VPN operation'),
('APP_TO_PER_APP_VPN_MAPPING', 'VPN App mapping', 'App to per app VPN mapping operation'),
('ENTERPRISE_WIPE', 'Enterprise Wipe', 'Enterprise wipe operation'),
('APP_LOCK', 'App lock', 'App lock operation');