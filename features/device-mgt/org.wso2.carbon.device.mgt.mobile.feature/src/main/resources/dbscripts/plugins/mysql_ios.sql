-- -----------------------------------------------------
-- Table `IOS_FEATURE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `IOS_FEATURE` (
  `FEATURE_ID` INT NOT NULL AUTO_INCREMENT,
  `CODE` VARCHAR(45) NOT NULL,
  `NAME` VARCHAR(100) NULL,
  `DESCRIPTION` VARCHAR(200) NULL,
  PRIMARY KEY (`FEATURE_ID`) )
  ENGINE = InnoDB;


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
  PRIMARY KEY (`MOBILE_DEVICE_ID`) )
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- TODO remove this later
-- -----------------------------------------------------

INSERT INTO IOS_FEATURE (CODE, NAME, DESCRIPTION) VALUES
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
  ('APP_LOCK', 'App lock', 'App lock operation'),
  ('GET_RESTRICTIONS', 'Get restrictions', 'Get restrictions operation');
