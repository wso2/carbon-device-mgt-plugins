/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.mobile.impl.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.impl.MobileDeviceDAOImpl;
import org.wso2.carbon.device.mgt.mobile.dto.MobileDevice;
import org.wso2.carbon.device.mgt.mobile.impl.common.DBTypes;
import org.wso2.carbon.device.mgt.mobile.impl.common.TestDBConfiguration;
import org.wso2.carbon.device.mgt.mobile.impl.dao.util.MobileDatabaseUtils;

import java.sql.*;
import java.util.List;

/**
 *
 * Class for holding unit-tests related to MobileDeviceDAO class.
 *
 */

public class MobileDeviceDAOTestSuite {

	private static final Log log = LogFactory.getLog(MobileDeviceDAOTestSuite.class);
	public static final String TEST_MOBILE_DEVICE_ID = "ABCD";
	public static final String TEST_MOBILE_IMEI = "2412421412";
	public static final String TEST_MOBILE_IMSI = "325235235";
	public static final String TEST_MOBILE_MODEL = "S5";
	public static final String TEST_MOBILE_VENDOR = "samsung";
	public static final String TEST_MOBILE_UPDATED_VENDOR = "sony";
	public static final String TEST_MOBILE_PUSH_TOKEN = "2414";
	public static final String TEST_MOBILE_OS_VERSION = "5.0.0";
	public static final String TEST_MOBILE_LATITUDE = "6.93N";
	public static final String TEST_MOBILE_LONGITUDE = "80.60E";
	public static final String TEST_MOBILE_TOKEN = "2412K2HKHK24K12H4";
	public static final String TEST_MOBILE_SERIAL = "24124IIH4I2K4";
	public static final String TEST_MOBILE_CHALLENGE = "ASFASFSAFASFATWTWQTTQWTWQTQWTQWTWQT";
	public static final String TEST_MOBILE_UNLOCK_TOKEN = "FAFWQUWFUQWYWQYRWQURYUURUWQUWRUWRUWE";
	private TestDBConfiguration testDBConfiguration;
	private MobileDeviceDAOImpl mblDeviceDAO;

	@BeforeClass
	@Parameters("dbType")
	public void setUpDB(String dbTypeStr) throws Exception {

		DBTypes dbType = DBTypes.valueOf(dbTypeStr);
		testDBConfiguration = MobileDatabaseUtils.getTestDBConfiguration(dbType);

		switch (dbType) {
			case H2:
				MobileDatabaseUtils.createH2DB(testDBConfiguration);
				DataSource testDataSource = new org.apache.tomcat.jdbc.pool.DataSource();
				PoolProperties properties = new PoolProperties();
				properties.setUrl(testDBConfiguration.getConnectionURL());
				properties.setDriverClassName(testDBConfiguration.getDriverClassName());
				properties.setUsername(testDBConfiguration.getUsername());
				properties.setPassword(testDBConfiguration.getPassword());
				testDataSource.setPoolProperties(properties);
				mblDeviceDAO = new MobileDeviceDAOImpl(testDataSource);
			default:
		}
	}

	@Test
	public void addMobileDeviceTest()
			throws MobileDeviceManagementDAOException {

		Connection conn = null;
		PreparedStatement preparedStatement = null;
		MobileDevice mobileDevice = new MobileDevice();
		MobileDevice testMblDevice = new MobileDevice();
		mobileDevice.setMobileDeviceId(TEST_MOBILE_DEVICE_ID);
		mobileDevice.setImei(TEST_MOBILE_IMEI);
		mobileDevice.setImsi(TEST_MOBILE_IMSI);
		mobileDevice.setModel(TEST_MOBILE_MODEL);
		mobileDevice.setVendor(TEST_MOBILE_VENDOR);
		mobileDevice.setPushToken(TEST_MOBILE_PUSH_TOKEN);
		mobileDevice.setOsVersion(TEST_MOBILE_OS_VERSION);
		mobileDevice.setLatitude(TEST_MOBILE_LATITUDE);
		mobileDevice.setLongitude(TEST_MOBILE_LONGITUDE);
		mobileDevice.setToken(TEST_MOBILE_TOKEN);
		mobileDevice.setSerial(TEST_MOBILE_SERIAL);
		mobileDevice.setChallenge(TEST_MOBILE_CHALLENGE);
		mobileDevice.setUnlockToken(TEST_MOBILE_UNLOCK_TOKEN);

		boolean added = mblDeviceDAO.addMobileDevice(mobileDevice);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String selectDBQuery =
					"SELECT MOBILE_DEVICE_ID, PUSH_TOKEN, IMEI, IMSI, OS_VERSION,DEVICE_MODEL, VENDOR, " +
					"LATITUDE, LONGITUDE, CHALLENGE, SERIAL, TOKEN, UNLOCK_TOKEN FROM MBL_DEVICE " +
					"WHERE MOBILE_DEVICE_ID = ?";
			preparedStatement = conn.prepareStatement(selectDBQuery);
			preparedStatement.setString(1, TEST_MOBILE_DEVICE_ID);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				testMblDevice.setMobileDeviceId(resultSet.getString(1));
				testMblDevice.setPushToken(resultSet.getString(2));
				testMblDevice.setImei(resultSet.getString(3));
				testMblDevice.setImsi(resultSet.getString(4));
				testMblDevice.setOsVersion(resultSet.getString(5));
				testMblDevice.setModel(resultSet.getString(6));
				testMblDevice.setVendor(resultSet.getString(7));
				testMblDevice.setLatitude(resultSet.getString(8));
				testMblDevice.setLongitude(resultSet.getString(9));
				testMblDevice.setChallenge(resultSet.getString(10));
				testMblDevice.setSerial(resultSet.getString(11));
				testMblDevice.setToken(resultSet.getString(12));
				testMblDevice.setUnlockToken(resultSet.getString(13));
			}
		} catch (SQLException e) {
			String msg = "Error in retrieving Mobile Device data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(added, "MobileDevice has added");
		Assert.assertEquals(TEST_MOBILE_DEVICE_ID, testMblDevice.getMobileDeviceId(),
		                    "MobileDevice id has persisted ");
		Assert.assertEquals(TEST_MOBILE_IMEI, testMblDevice.getImei(),
		                    "MobileDevice IMEI has persisted ");
		Assert.assertEquals(TEST_MOBILE_IMSI, testMblDevice.getImsi(),
		                    "MobileDevice IMSI has persisted ");
		Assert.assertEquals(TEST_MOBILE_LATITUDE, testMblDevice.getLatitude(),
		                    "MobileDevice latitude has persisted ");
		Assert.assertEquals(TEST_MOBILE_LONGITUDE, testMblDevice.getLongitude(),
		                    "MobileDevice longitude has persisted ");
		Assert.assertEquals(TEST_MOBILE_MODEL, testMblDevice.getModel(),
		                    "MobileDevice model has persisted ");
		Assert.assertEquals(TEST_MOBILE_OS_VERSION, testMblDevice.getOsVersion(),
		                    "MobileDevice os-version has persisted ");
		Assert.assertEquals(TEST_MOBILE_PUSH_TOKEN, testMblDevice.getPushToken(),
		                    "MobileDevice reg-id has persisted ");
		Assert.assertEquals(TEST_MOBILE_VENDOR, testMblDevice.getVendor(),
		                    "MobileDevice vendor has persisted ");
		Assert.assertEquals(TEST_MOBILE_CHALLENGE, testMblDevice.getChallenge(),
		                    "MobileDevice challenge has persisted ");
		Assert.assertEquals(TEST_MOBILE_SERIAL, testMblDevice.getSerial(),
		                    "MobileDevice serial has persisted");
		Assert.assertEquals(TEST_MOBILE_UNLOCK_TOKEN, testMblDevice.getUnlockToken(),
		                    "MobileDevice unlock-token has persisted");
		Assert.assertEquals(TEST_MOBILE_TOKEN, testMblDevice.getToken(),
		                    "MobileDevice token has persisted");
	}

	@Test(dependsOnMethods = { "addMobileDeviceTest" })
	public void getMobileDeviceTest()
			throws MobileDeviceManagementDAOException {
		MobileDevice testMblDevice = mblDeviceDAO.getMobileDevice(TEST_MOBILE_DEVICE_ID);
		Assert.assertEquals(TEST_MOBILE_DEVICE_ID, testMblDevice.getMobileDeviceId(),
		                    "MobileDevice id has persisted ");
		Assert.assertEquals(TEST_MOBILE_IMEI, testMblDevice.getImei(),
		                    "MobileDevice IMEI has persisted ");
		Assert.assertEquals(TEST_MOBILE_IMSI, testMblDevice.getImsi(),
		                    "MobileDevice IMSI has persisted ");
		Assert.assertEquals(TEST_MOBILE_LATITUDE, testMblDevice.getLatitude(),
		                    "MobileDevice latitude has persisted ");
		Assert.assertEquals(TEST_MOBILE_LONGITUDE, testMblDevice.getLongitude(),
		                    "MobileDevice longitude has persisted ");
		Assert.assertEquals(TEST_MOBILE_MODEL, testMblDevice.getModel(),
		                    "MobileDevice model has persisted ");
		Assert.assertEquals(TEST_MOBILE_OS_VERSION, testMblDevice.getOsVersion(),
		                    "MobileDevice os-version has persisted ");
		Assert.assertEquals(TEST_MOBILE_PUSH_TOKEN, testMblDevice.getPushToken(),
		                    "MobileDevice reg-id has persisted ");
		Assert.assertEquals(TEST_MOBILE_VENDOR, testMblDevice.getVendor(),
		                    "MobileDevice vendor has persisted ");
		Assert.assertEquals(TEST_MOBILE_CHALLENGE, testMblDevice.getChallenge(),
		                    "MobileDevice challenge has persisted ");
		Assert.assertEquals(TEST_MOBILE_SERIAL, testMblDevice.getSerial(),
		                    "MobileDevice serial has persisted");
		Assert.assertEquals(TEST_MOBILE_UNLOCK_TOKEN, testMblDevice.getUnlockToken(),
		                    "MobileDevice unlock-token has persisted");
		Assert.assertEquals(TEST_MOBILE_TOKEN, testMblDevice.getToken(),
		                    "MobileDevice token has persisted");
	}

	@Test(dependsOnMethods = { "addMobileDeviceTest" })
	public void getAllMobileDevicesTest()
			throws MobileDeviceManagementDAOException {
		List<MobileDevice> mblDevices = mblDeviceDAO.getAllMobileDevices();
		Assert.assertNotNull(mblDevices, "MobileDevice list is not null");
		Assert.assertTrue(mblDevices.size() == 1, "MobileDevice list has 1 MobileDevice");
	}

	@Test(dependsOnMethods = { "addMobileDeviceTest", "getMobileDeviceTest",
			"getAllMobileDevicesTest" })
	public void updateMobileDeviceTest()
			throws MobileDeviceManagementDAOException {

		Connection conn = null;
		PreparedStatement preparedStatement = null;
		MobileDevice mobileDevice = new MobileDevice();
		MobileDevice testMblDevice = new MobileDevice();
		mobileDevice.setMobileDeviceId(TEST_MOBILE_DEVICE_ID);
		mobileDevice.setImei(TEST_MOBILE_IMEI);
		mobileDevice.setImsi(TEST_MOBILE_IMSI);
		mobileDevice.setModel(TEST_MOBILE_MODEL);
		mobileDevice.setVendor(TEST_MOBILE_UPDATED_VENDOR);
		mobileDevice.setPushToken(TEST_MOBILE_PUSH_TOKEN);
		mobileDevice.setOsVersion(TEST_MOBILE_OS_VERSION);
		mobileDevice.setLatitude(TEST_MOBILE_LATITUDE);
		mobileDevice.setLongitude(TEST_MOBILE_LONGITUDE);
		mobileDevice.setToken(TEST_MOBILE_TOKEN);
		mobileDevice.setSerial(TEST_MOBILE_SERIAL);
		mobileDevice.setChallenge(TEST_MOBILE_CHALLENGE);
		mobileDevice.setUnlockToken(TEST_MOBILE_UNLOCK_TOKEN);

		boolean updated = mblDeviceDAO.updateMobileDevice(mobileDevice);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String selectDBQuery =
					"SELECT MOBILE_DEVICE_ID, PUSH_TOKEN, IMEI, IMSI, OS_VERSION,DEVICE_MODEL, VENDOR, " +
					"LATITUDE, LONGITUDE, CHALLENGE, SERIAL, TOKEN, UNLOCK_TOKEN FROM MBL_DEVICE " +
					"WHERE MOBILE_DEVICE_ID = ?";
			preparedStatement = conn.prepareStatement(selectDBQuery);
			preparedStatement.setString(1, TEST_MOBILE_DEVICE_ID);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				testMblDevice.setMobileDeviceId(resultSet.getString(1));
				testMblDevice.setPushToken(resultSet.getString(2));
				testMblDevice.setImei(resultSet.getString(3));
				testMblDevice.setImsi(resultSet.getString(4));
				testMblDevice.setOsVersion(resultSet.getString(5));
				testMblDevice.setModel(resultSet.getString(6));
				testMblDevice.setVendor(resultSet.getString(7));
				testMblDevice.setLatitude(resultSet.getString(8));
				testMblDevice.setLongitude(resultSet.getString(9));
				testMblDevice.setChallenge(resultSet.getString(10));
				testMblDevice.setSerial(resultSet.getString(11));
				testMblDevice.setToken(resultSet.getString(12));
				testMblDevice.setUnlockToken(resultSet.getString(13));
			}
		} catch (SQLException e) {
			String msg = "Error in retrieving Mobile Device data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(updated, "MobileDevice has updated ");
		Assert.assertEquals(TEST_MOBILE_UPDATED_VENDOR, testMblDevice.getVendor(),
		                    "MobileDevice vendor has updated ");
	}

	@Test(dependsOnMethods = { "addMobileDeviceTest", "getMobileDeviceTest",
	                           "getAllMobileDevicesTest", "updateMobileDeviceTest" })
	public void deleteMobileDeviceTest()
			throws MobileDeviceManagementDAOException {

		Connection conn = null;
		PreparedStatement preparedStatement = null;
		boolean deleted = mblDeviceDAO.deleteMobileDevice(TEST_MOBILE_DEVICE_ID);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String selectDBQuery =
					"SELECT MOBILE_DEVICE_ID, PUSH_TOKEN, IMEI, IMSI, OS_VERSION,DEVICE_MODEL, VENDOR, " +
					"LATITUDE, LONGITUDE, CHALLENGE, SERIAL, TOKEN, UNLOCK_TOKEN FROM MBL_DEVICE " +
					"WHERE MOBILE_DEVICE_ID = ?";
			preparedStatement = conn.prepareStatement(selectDBQuery);
			preparedStatement.setString(1, TEST_MOBILE_DEVICE_ID);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				deleted = false;
			}
		} catch (SQLException e) {
			String msg = "Error in retrieving Mobile Device data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(deleted, "MobileDevice has deleted ");
	}
}
