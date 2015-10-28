-- -----------------------------------------------------
-- Table `AD_DEVICE`
-- -----------------------------------------------------
CREATE TABLE AD_DEVICE (
  DEVICE_ID VARCHAR(45) NOT NULL ,
  DEVICE_INFO VARCHAR(500) DEFAULT NULL,
  GCM_TOKEN VARCHAR(45) DEFAULT NULL,
  IMEI VARCHAR(45) DEFAULT NULL,
  IMSI VARCHAR(45) DEFAULT NULL,
  OS_VERSION VARCHAR(45) DEFAULT NULL,
  DEVICE_MODEL VARCHAR(45) DEFAULT NULL,
  VENDOR VARCHAR(45) DEFAULT NULL,
  LATITUDE VARCHAR(45) DEFAULT NULL,
  LONGITUDE VARCHAR(45) DEFAULT NULL,
  SERIAL VARCHAR(45) DEFAULT NULL,
  MAC_ADDRESS VARCHAR(45) DEFAULT NULL,
  DEVICE_NAME VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (DEVICE_ID)
);
/

-- -----------------------------------------------------
-- Table `AD_FEATURE`
-- -----------------------------------------------------
CREATE TABLE AD_FEATURE (
  ID INT NOT NULL,
  CODE VARCHAR(45) NOT NULL,
  NAME VARCHAR(100) NOT NULL,
  DESCRIPTION VARCHAR(200) DEFAULT NULL,
  PRIMARY KEY (ID)
);
/

-- -----------------------------------------------------
-- Sequence `AD_FEATURE_ID_INC_SEQ`
-- -----------------------------------------------------
CREATE SEQUENCE AD_FEATURE_ID_INC_SEQ START WITH 1 INCREMENT BY 1 NOCACHE;
/

-- -----------------------------------------------------
-- Trigger `AD_FEATURE_ID_INC_TRIG`
-- -----------------------------------------------------
CREATE OR REPLACE TRIGGER AD_FEATURE_ID_INC_TRIG
BEFORE INSERT ON AD_FEATURE
FOR EACH ROW
BEGIN
    SELECT AD_FEATURE_ID_INC_SEQ.NEXTVAL INTO :NEW.ID FROM DUAL;
END;
/
