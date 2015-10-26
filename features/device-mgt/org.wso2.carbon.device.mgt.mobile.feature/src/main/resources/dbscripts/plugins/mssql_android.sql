-- -----------------------------------------------------
-- Table `AD_DEVICE`
-- -----------------------------------------------------
CREATE  TABLE AD_DEVICE (
  [ANDROID_DEVICE_ID] VARCHAR(45) NOT NULL,
  [GCM_TOKEN] VARCHAR(45) NULL DEFAULT NULL,
  [DEVICE_INFO] VARCHAR(8000) NULL DEFAULT NULL,
  [IMEI] VARCHAR(45) NULL DEFAULT NULL,
  [IMSI] VARCHAR(45) NULL DEFAULT NULL,
  [OS_VERSION] VARCHAR(45) NULL DEFAULT NULL,
  [DEVICE_MODEL] VARCHAR(45) NULL DEFAULT NULL,
  [VENDOR] VARCHAR(45) NULL DEFAULT NULL,
  [LATITUDE] VARCHAR(45) NULL DEFAULT NULL,
  [LONGITUDE] VARCHAR(45) NULL DEFAULT NULL,
  [SERIAL] VARCHAR(45) NULL DEFAULT NULL,
  [MAC_ADDRESS] VARCHAR(45) NULL DEFAULT NULL,
  [DEVICE_NAME] VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY ([ANDROID_DEVICE_ID]));

-- -----------------------------------------------------
-- Table `AD_FEATURE`
-- -----------------------------------------------------
CREATE TABLE AD_FEATURE (
  [ID] INT NOT NULL IDENTITY,
  [CODE] VARCHAR(45) NOT NULL,
  [NAME] VARCHAR(100) NULL,
  [DESCRIPTION] VARCHAR(200) NULL,
  PRIMARY KEY ([ID]));

-- -----------------------------------------------------
-- TODO remove this later
-- -----------------------------------------------------
INSERT INTO AD_FEATURE (CODE, NAME, DESCRIPTION)
VALUES
('DEVICE_LOCK', 'Device Lock', 'Lock the device'),
('DEVICE_LOCATION', 'Location', 'Request coordinates of device location'),
('WIFI', 'Wifi', 'Setting up wifi configuration'),
('CAMERA', 'Camera', 'Enable or disable camera'),
('EMAIL', 'Email', 'Configure email settings'),
('DEVICE_MUTE', 'Mute', 'Enable mute in the device'),
('DEVICE_INFO', 'Device Info', 'Request device information'),
('ENTERPRISE_WIPE', 'Enterprise Wipe', 'Remove enterprise applications'),
('CLEAR_PASSWORD', 'Clear Password', 'Clear current password'),
('WIPE_DATA', 'Wipe Data', 'Factory reset the device'),
('APPLICATION_LIST', 'Application List', 'Request list of current installed applications'),
('CHANGE_LOCK_CODE', 'Change Lock-code', 'Change current lock code'),
('INSTALL_APPLICATION', 'Install App', 'Install Enterprise or Market application'),
('UNINSTALL_APPLICATION', 'Uninstall App', 'Uninstall application'),
('BLACKLIST_APPLICATIONS', 'Blacklist app', 'Blacklist applications'),
('ENCRYPT_STORAGE', 'Encrypt storage', 'Encrypt storage'),
('DEVICE_RING', 'Ring', 'Ring the device'),
('PASSCODE_POLICY', 'Password Policy', 'Set passcode policy'),
('NOTIFICATION', 'Message', 'Send message');