-- -----------------------------------------------------
-- Table `IOS_FEATURE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `IOS_FEATURE` (
  `FEATURE_ID` INT NOT NULL AUTO_INCREMENT,
  `CODE` VARCHAR(45) NOT NULL,
  `NAME` VARCHAR(100) NULL,
  `DESCRIPTION` VARCHAR(200) NULL,
  PRIMARY KEY (`FEATURE_ID`) );


-- -----------------------------------------------------
-- Table `IOS_DEVICE`
-- -----------------------------------------------------

  CREATE  TABLE IF NOT EXISTS `IOS_DEVICE` (
  `MOBILE_DEVICE_ID` VARCHAR(45) NOT NULL,
  `APNS_PUSH_TOKEN` VARCHAR(100) NULL DEFAULT NULL,
  `MAGIC_TOKEN` VARCHAR(100) NULL DEFAULT NULL,
  `MDM_TOKEN` VARCHAR(100) NULL DEFAULT NULL,
  `UNLOCK_TOKEN` VARCHAR(2000) NULL DEFAULT NULL,
  `CHALLENGE_TOKEN` VARCHAR(45) NULL DEFAULT NULL,
  `DEVICE_INFO` VARCHAR(8000) NULL DEFAULT NULL,
  `SERIAL` VARCHAR(45) NULL DEFAULT NULL,
  `PRODUCT` VARCHAR(45) NULL DEFAULT NULL,
  `IMEI` VARCHAR(45) NULL DEFAULT NULL,
  `VERSION` VARCHAR(45) NULL DEFAULT NULL,
  `MAC_ADDRESS` VARCHAR(45) NULL DEFAULT NULL,
  `DEVICE_NAME` VARCHAR(100) NULL DEFAULT NULL,
  `ICCID` VARCHAR(45) NULL DEFAULT NULL,
  `LATITUDE` VARCHAR(45) NULL DEFAULT NULL,
  `LONGITUDE` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`MOBILE_DEVICE_ID`) );


-- -----------------------------------------------------
-- TODO remove this later
-- -----------------------------------------------------

INSERT INTO IOS_FEATURE (CODE, NAME, DESCRIPTION)
VALUES
('INSTALL_ENTERPRISE_APPLICATION', 'INSTALL_ENTERPRISE_APPLICATION', 'Enterprise application installation'),
 ('INSTALL_STORE_APPLICATION', 'INSTALL_STORE_APPLICATION', 'Store application installation'),  
('REMOVE_APPLICATION', 'REMOVE_APPLICATION', 'Application removal'),
 ('DEVICE_LOCK', 'DEVICE_LOCK', 'Lock device'),
 ('CELLULAR', 'CELLULAR', 'Cellular operation'),
 ('APN', 'APN', 'APN operation'),
 ('RESTRICTION', 'RESTRICTION', 'Restriction operation'),
 ('WIFI', 'WIFI', 'WIFI operation'),
 ('DEVICE_INFO', 'DEVICE_INFO', 'Device info operation'),  
('AIR_PLAY', 'AIR_PLAY', 'AirPlay operation'),  
('LOCATION', 'LOCATION', 'Fetch location'),  
('ALARM', 'ALARM', 'Alarm device'),
('APPLICATION_LIST', 'APPLICATION_LIST', 'Application list'),
('PROFILE_LIST', 'PROFILE_LIST', 'Profile list'),
('REMOVE_PROFILE', 'REMOVE_PROFILE', 'Remove profile'),
('CLEAR_PASSCODE', 'CLEAR_PASSCODE', 'Clear passcode'),
('CALDAV', 'CALDAV', 'CalDav'),
('CALENDAR_SUBSCRIPTION', 'CALENDAR_SUBSCRIPTION', 'Calendar subscription'),
('PASSCODE_POLICY', 'PASSCODE_POLICY', 'Passcode policy'),
('EMAIL', 'EMAIL', 'Email operation'),
('LDAP', 'LDAP', 'LDAP operation'),
('WEB_CLIP', 'WEB_CLIP', 'Web clip operation'),
('VPN', 'VPN', 'VPN operation'),
('PER_APP_VPN', 'PER_APP_VPN', 'Per app VPN operation'),
('APP_TO_PER_APP_VPN_MAPPING', 'APP_TO_PER_APP_VPN_MAPPING', 'App to per app VPN mapping operation'),
('ENTERPRISE_WIPE', 'ENTERPRISE_WIPE', 'Enterprise wipe operation');
