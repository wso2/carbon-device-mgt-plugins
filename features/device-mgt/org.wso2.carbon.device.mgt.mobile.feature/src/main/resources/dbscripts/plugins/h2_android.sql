
-- -----------------------------------------------------
-- Table `AD_DEVICE`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `AD_DEVICE` (
  `ANDROID_DEVICE_ID` VARCHAR(45) NOT NULL ,
  `GCM_TOKEN` VARCHAR(45) NULL DEFAULT NULL ,
  `DEVICE_INFO` VARCHAR(500) NULL DEFAULT NULL ,
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
  PRIMARY KEY (`ANDROID_DEVICE_ID`) );

-- -----------------------------------------------------
-- Table `AD_FEATURE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `AD_FEATURE` (
  `ID` INT NOT NULL AUTO_INCREMENT ,
  `CODE` VARCHAR(45) NOT NULL,
  `NAME` VARCHAR(100) NULL ,
  `DESCRIPTION` VARCHAR(200) NULL ,
  PRIMARY KEY (`ID`) );


-- -----------------------------------------------------
-- TODO remove this later
-- -----------------------------------------------------

INSERT INTO AD_FEATURE (CODE, NAME, DESCRIPTION)
VALUES
('DEVICE_LOCK', 'DEVICE_LOCK', 'Lock the device'),
('DEVICE_LOCATION', 'DEVICE_LOCATION', 'Request coordinates of device location'),
('WIFI', 'WIFI', 'Setting up wifi configuration'),
('CAMERA', 'CAMERA', 'Enable or disable camera'),
('EMAIL', 'EMAIL', 'Configure email settings'),
('DEVICE_MUTE', 'DEVICE_MUTE', 'Enable mute in the device'),
('PASSWORD_POLICY', 'PASSWORD_POLICY', 'Set up password policy'),
('DEVICE_INFO', 'DEVICE_INFO', 'Request device information'),
('ENTERPRISE_WIPE', 'ENTERPRISE_WIPE', 'Remove enterprise applications'),
('CLEAR_PASSWORD', 'CLEAR_PASSWORD', 'Clear current password'),
('WIPE_DATA', 'WIPE_DATA', 'Factory reset the device'),
('APPLICATION_LIST', 'APPLICATION_LIST', 'Request list of current installed applications'),
('CHANGE_LOCK_CODE', 'CHANGE_LOCK_CODE', 'Change current lock code'),
('INSTALL_APPLICATION', 'INSTALL_APPLICATION', 'Install Enterprise or Market application'),
('UNINSTALL_APPLICATION', 'UNINSTALL_APPLICATION', 'Uninstall application'),
('BLACKLIST_APPLICATIONS', 'BLACKLIST_APPLICATIONS', 'Blacklist applications'),
('ENCRYPT_STORAGE', 'ENCRYPT_STORAGE', 'Encrypt storage'),
('DEVICE_RING', 'DEVICE_RING', 'Ring the device'),
('PASSCODE_POLICY', 'PASSCODE_POLICY', 'Set passcode policy'),
('NOTIFICATION', 'NOTIFICATION', 'Send notification');
