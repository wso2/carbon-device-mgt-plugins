-- -----------------------------------------------------
-- Table `WINDOWS_FEATURE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `WINDOWS_FEATURE` (
  `FEATURE_ID` INT NOT NULL AUTO_INCREMENT,
  `CODE` VARCHAR(45) NOT NULL,
  `NAME` VARCHAR(100) NULL,
  `DESCRIPTION` VARCHAR(200) NULL,
  PRIMARY KEY (`FEATURE_ID`) );


-- -----------------------------------------------------
-- Table `WINDOWS_DEVICE`
-- -----------------------------------------------------

  CREATE  TABLE IF NOT EXISTS `WINDOWS_DEVICE` (
  `MOBILE_DEVICE_ID` VARCHAR(45) NOT NULL,
  `CHANNEL_URI` VARCHAR(100) NULL DEFAULT NULL,
  `DEVICE_INFO` VARCHAR(8000) NULL DEFAULT NULL ,
  `IMEI` VARCHAR(45) NULL DEFAULT NULL ,
  `IMSI` VARCHAR(45) NULL DEFAULT NULL ,
  `OS_VERSION` VARCHAR(45) NULL DEFAULT NULL ,
  `DEVICE_MODEL` VARCHAR(45) NULL DEFAULT NULL ,
  `VENDOR` VARCHAR(45) NULL DEFAULT NULL ,
  `LATITUDE` VARCHAR(45) NULL DEFAULT NULL,
  `LONGITUDE` VARCHAR(45) NULL DEFAULT NULL,
  `SERIAL` VARCHAR(45) NULL DEFAULT NULL,
  `MAC_ADDRESS` VARCHAR(45) NULL DEFAULT NULL,
  `DEVICE_NAME` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`MOBILE_DEVICE_ID`) );


-- -----------------------------------------------------
-- TODO remove this later
-- -----------------------------------------------------

INSERT INTO WINDOWS_FEATURE (CODE, NAME, DESCRIPTION)
VALUES
('INSTALL_ENTERPRISE_APPLICATION', 'Install Enterprise App', 'Install Enterprise App'),
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
