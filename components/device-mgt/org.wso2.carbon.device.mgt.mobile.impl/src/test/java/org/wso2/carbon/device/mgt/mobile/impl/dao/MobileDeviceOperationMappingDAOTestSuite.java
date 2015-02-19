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
import org.wso2.carbon.device.mgt.mobile.dao.impl.MobileDeviceOperationMappingDAOImpl;
import org.wso2.carbon.device.mgt.mobile.dao.impl.MobileOperationDAOImpl;
import org.wso2.carbon.device.mgt.mobile.dto.MobileDevice;
import org.wso2.carbon.device.mgt.mobile.dto.MobileDeviceOperationMapping;
import org.wso2.carbon.device.mgt.mobile.dto.MobileOperation;
import org.wso2.carbon.device.mgt.mobile.impl.common.DBTypes;
import org.wso2.carbon.device.mgt.mobile.impl.common.TestDBConfiguration;
import org.wso2.carbon.device.mgt.mobile.impl.dao.util.MobileDatabaseUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Class for holding unit-tests related to MobileDeviceOperationMappingDAO class.
 *
 */
public class MobileDeviceOperationMappingDAOTestSuite {

	private static final Log log =
			LogFactory.getLog(MobileDeviceOperationMappingDAOTestSuite.class);
	public static final String TEST_MOBILE_DEVICE_ID = "ABCD";
	public static final String TEST_MOBILE_IMEI = "2412421412";
	public static final String TEST_MOBILE_IMSI = "325235235";
	public static final String TEST_MOBILE_MODEL = "S5";
	public static final String TEST_MOBILE_VENDOR = "samsung";
	public static final String TEST_MOBILE_REG_ID = "2414";
	public static final String TEST_MOBILE_OS_VERSION = "5.0.0";
	public static final String TEST_MOBILE_LATITUDE = "6.93N";
	public static final String TEST_MOBILE_LONGITUDE = "80.60E";
	public static final String TEST_MOBILE_TOKEN = "2412K2HKHK24K12H4";
	public static final String TEST_MOBILE_SERIAL = "24124IIH4I2K4";
	public static final String TEST_MOBILE_CHALLENGE = "ASFASFSAFASFATWTWQTTQWTWQTQWTQWTWQT";
	public static final String TEST_MOBILE_UNLOCK_TOKEN = "FAFWQUWFUQWYWQYRWQURYUURUWQUWRUWRUWE";
	public static final String TEST_MBL_OPR_FEATURE_CODE1 = "LOCK";
	public static final String TEST_MBL_OPR_FEATURE_CODE2 = "WIPE";
	public static final long TEST_MBL_OPR_CREATED_DATE = new java.util.Date().getTime();

	private TestDBConfiguration testDBConfiguration;
	private MobileDeviceDAOImpl mblDeviceDAO;
	private MobileOperationDAOImpl mblOperationDAO;
	private MobileDeviceOperationMappingDAOImpl mblDeviceOperationMappingDAO;
	private int mblOperationId1;
	private int mblOperationId2;

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
				mblOperationDAO = new MobileOperationDAOImpl(testDataSource);
				mblDeviceOperationMappingDAO =
						new MobileDeviceOperationMappingDAOImpl(testDataSource);
			default:
		}
	}

	@Test
	public void addMobileDeviceOperationMappingTest()
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement preparedStatement = null;

		List<MobileDeviceOperationMapping> mblOperations =
				new ArrayList<MobileDeviceOperationMapping>();
		MobileDeviceOperationMapping mblDvOperationMapping =
				new MobileDeviceOperationMapping();
		//Add a new Device to the database
		MobileDevice mobileDevice = new MobileDevice();
		mobileDevice.setMobileDeviceId(TEST_MOBILE_DEVICE_ID);
		mobileDevice.setImei(TEST_MOBILE_IMEI);
		mobileDevice.setImsi(TEST_MOBILE_IMSI);
		mobileDevice.setModel(TEST_MOBILE_MODEL);
		mobileDevice.setVendor(TEST_MOBILE_VENDOR);
		mobileDevice.setPushToken(TEST_MOBILE_REG_ID);
		mobileDevice.setOsVersion(TEST_MOBILE_OS_VERSION);
		mobileDevice.setLatitude(TEST_MOBILE_LATITUDE);
		mobileDevice.setLongitude(TEST_MOBILE_LONGITUDE);
		mobileDevice.setToken(TEST_MOBILE_TOKEN);
		mobileDevice.setSerial(TEST_MOBILE_SERIAL);
		mobileDevice.setChallenge(TEST_MOBILE_CHALLENGE);
		mobileDevice.setUnlockToken(TEST_MOBILE_UNLOCK_TOKEN);
		mblDeviceDAO.addMobileDevice(mobileDevice);

		//Add an Operation to the db
		MobileOperation mblOperation = new MobileOperation();
		mblOperation.setFeatureCode(TEST_MBL_OPR_FEATURE_CODE1);
		mblOperation.setCreatedDate(TEST_MBL_OPR_CREATED_DATE);
		mblOperationId1 = mblOperationDAO.addMobileOperation(mblOperation);

		//Add a new Operation 2 to the db
		mblOperation.setFeatureCode(TEST_MBL_OPR_FEATURE_CODE2);
		mblOperation.setCreatedDate(TEST_MBL_OPR_CREATED_DATE);
		mblOperationId2 = mblOperationDAO.addMobileOperation(mblOperation);

		//Add a device-operation mapping 1 to the table
		mblDvOperationMapping.setDeviceId(TEST_MOBILE_DEVICE_ID);
		mblDvOperationMapping.setOperationId(mblOperationId1);
		mblDvOperationMapping.setStatus(MobileDeviceOperationMapping.Status.NEW);
		boolean status1 =
				mblDeviceOperationMappingDAO.addMobileDeviceOperationMapping(mblDvOperationMapping);

		//Add a device-operation mapping 2 to the table
		mblDvOperationMapping.setDeviceId(TEST_MOBILE_DEVICE_ID);
		mblDvOperationMapping.setOperationId(mblOperationId2);
		mblDvOperationMapping.setStatus(MobileDeviceOperationMapping.Status.NEW);
		boolean status2 =
				mblDeviceOperationMappingDAO.addMobileDeviceOperationMapping(mblDvOperationMapping);

		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String selectDBQuery =
					"SELECT DEVICE_ID, OPERATION_ID, SENT_DATE, RECEIVED_DATE, STATUS FROM " +
					"MBL_DEVICE_OPERATION_MAPPING WHERE DEVICE_ID = ?";
			preparedStatement = conn.prepareStatement(selectDBQuery);
			preparedStatement.setString(1, TEST_MOBILE_DEVICE_ID);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				mblDvOperationMapping = new MobileDeviceOperationMapping();
				mblDvOperationMapping.setDeviceId(resultSet.getString(1));
				mblDvOperationMapping.setOperationId(resultSet.getInt(2));
				mblDvOperationMapping.setSentDate(resultSet.getLong(3));
				mblDvOperationMapping.setReceivedDate(resultSet.getLong(4));
				mblDvOperationMapping.setStatus(resultSet.getString(5));
				mblOperations.add(mblDvOperationMapping);
			}

		} catch (SQLException e) {
			String msg = "Error in retrieving Mobile Operation Mappings data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(status1, "MobileOperationMapping1 has added ");
		Assert.assertTrue(status2, "MobileOperationMapping2 has added ");
		Assert.assertTrue(mblOperations.size() == 2, "MobileOperationMappings have retrieved ");

		for (MobileDeviceOperationMapping mapping : mblOperations) {
			Assert.assertEquals(TEST_MOBILE_DEVICE_ID, mapping.getDeviceId(),
			                    "MobileOperationMapping device id has persisted ");
			Assert.assertEquals(MobileDeviceOperationMapping.Status.NEW, mapping.getStatus(),
			                    "MobileOperationMapping status has persisted ");
			Assert.assertTrue(mapping.getOperationId() > 0,
			                  "MobileOperationMapping operation-id has persisted ");
			Assert.assertTrue(mapping.getSentDate() == 0,
			                  "MobileOperationMapping sent-date has fetched ");
			Assert.assertTrue(mapping.getReceivedDate() == 0,
			                  "MobileOperationMapping received-date has fetched ");
		}
	}

	@Test(dependsOnMethods = { "addMobileDeviceOperationMappingTest" })
	public void getMobileDeviceOperationMappingTest() throws MobileDeviceManagementDAOException {
		MobileDeviceOperationMapping mblOperationMapping =
				mblDeviceOperationMappingDAO.getMobileDeviceOperationMapping(
						TEST_MOBILE_DEVICE_ID, mblOperationId1);
		Assert.assertNotNull(mblOperationMapping, "MobileOperationMapping 1 has fetched ");
		Assert.assertEquals(TEST_MOBILE_DEVICE_ID, mblOperationMapping.getDeviceId(),
		                    "MobileOperationMapping device id has fetched ");
		Assert.assertEquals(mblOperationId1, mblOperationMapping.getOperationId(),
		                    "MobileOperationMapping device id has fetched ");
		Assert.assertEquals(MobileDeviceOperationMapping.Status.NEW,
		                    mblOperationMapping.getStatus(),
		                    "MobileOperationMapping status has fetched ");
		Assert.assertTrue(mblOperationMapping.getSentDate() == 0,
		                  "MobileOperationMapping sent-date has fetched ");
		Assert.assertTrue(mblOperationMapping.getReceivedDate() == 0,
		                  "MobileOperationMapping received-date has fetched ");
	}

	@Test(dependsOnMethods = { "addMobileDeviceOperationMappingTest" })
	public void getAllMobileDeviceOperationMappingsOfDeviceTest()
			throws MobileDeviceManagementDAOException {
		List<MobileDeviceOperationMapping> mblOperationMappings =
				mblDeviceOperationMappingDAO.getAllMobileDeviceOperationMappingsOfDevice(
						TEST_MOBILE_DEVICE_ID);
		Assert.assertNotNull(mblOperationMappings, "MobileOperationMappings have fetched ");
		Assert.assertTrue(mblOperationMappings.size() == 2,
		                  "All MobileOperationMappings have fetched ");
		for (MobileDeviceOperationMapping mblOperationMapping : mblOperationMappings) {
			Assert.assertEquals(TEST_MOBILE_DEVICE_ID, mblOperationMapping.getDeviceId(),
			                    "MobileOperationMapping device id has fetched ");
			Assert.assertTrue(mblOperationMapping.getOperationId() > 0,
			                  "MobileOperationMapping operation-id has fetched ");
			Assert.assertEquals(MobileDeviceOperationMapping.Status.NEW,
			                    mblOperationMapping.getStatus(),
			                    "MobileOperationMapping status has fetched ");
			Assert.assertTrue(mblOperationMapping.getSentDate() == 0,
			                  "MobileOperationMapping sent-date has fetched ");
			Assert.assertTrue(mblOperationMapping.getReceivedDate() == 0,
			                  "MobileOperationMapping received-date has fetched ");
		}
	}

	@Test(dependsOnMethods = { "addMobileDeviceOperationMappingTest",
	                           "getAllMobileDeviceOperationMappingsOfDeviceTest" })
	public void updateMobileDeviceOperationMappingToInProgressTest()
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		MobileDeviceOperationMapping mblOperationMapping = null;
		//Update device-operation to In-Progress state
		boolean status =
				mblDeviceOperationMappingDAO.updateMobileDeviceOperationMappingToInProgress(
						TEST_MOBILE_DEVICE_ID, mblOperationId1);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String selectDBQuery =
					"SELECT DEVICE_ID, OPERATION_ID, SENT_DATE, STATUS FROM " +
					"MBL_DEVICE_OPERATION_MAPPING WHERE DEVICE_ID = ? AND OPERATION_ID = ?";
			preparedStatement = conn.prepareStatement(selectDBQuery);
			preparedStatement.setString(1, TEST_MOBILE_DEVICE_ID);
			preparedStatement.setInt(2, mblOperationId1);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				mblOperationMapping = new MobileDeviceOperationMapping();
				mblOperationMapping.setDeviceId(resultSet.getString(1));
				mblOperationMapping.setOperationId(resultSet.getInt(2));
				mblOperationMapping.setSentDate(resultSet.getLong(3));
				mblOperationMapping.setStatus(resultSet.getString(4));
			}

		} catch (SQLException e) {
			String msg = "Error in retrieving Mobile Operation Mappings data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(status, "MobileOperationMapping 1 has updated ");
		Assert.assertNotNull(mblOperationMapping, "MobileOperationMappings have fetched ");
		Assert.assertEquals(MobileDeviceOperationMapping.Status.INPROGRESS,
		                    mblOperationMapping.getStatus(),
		                    "MobileOperationMapping status has updated ");
		Assert.assertTrue(mblOperationMapping.getSentDate() > 0,
		                  "MobileOperationMapping sent-date has updated ");
	}

	@Test(dependsOnMethods = { "addMobileDeviceOperationMappingTest",
	                           "getAllMobileDeviceOperationMappingsOfDeviceTest" })
	public void updateMobileDeviceOperationMappingToCompletedTest()
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		MobileDeviceOperationMapping mblOperationMapping = null;
		//Update device-operation to Completed state
		boolean status =
				mblDeviceOperationMappingDAO.updateMobileDeviceOperationMappingToCompleted(
						TEST_MOBILE_DEVICE_ID, mblOperationId1);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String selectDBQuery =
					"SELECT DEVICE_ID, OPERATION_ID, RECEIVED_DATE, STATUS FROM " +
					"MBL_DEVICE_OPERATION_MAPPING WHERE DEVICE_ID = ? AND OPERATION_ID = ?";
			preparedStatement = conn.prepareStatement(selectDBQuery);
			preparedStatement.setString(1, TEST_MOBILE_DEVICE_ID);
			preparedStatement.setInt(2, mblOperationId1);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				mblOperationMapping = new MobileDeviceOperationMapping();
				mblOperationMapping.setDeviceId(resultSet.getString(1));
				mblOperationMapping.setOperationId(resultSet.getInt(2));
				mblOperationMapping.setReceivedDate(resultSet.getLong(3));
				mblOperationMapping.setStatus(resultSet.getString(4));
			}

		} catch (SQLException e) {
			String msg = "Error in retrieving Mobile Operation Mappings data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(status, "MobileOperationMapping 1 has updated ");
		Assert.assertNotNull(mblOperationMapping, "MobileOperationMappings have fetched ");
		Assert.assertEquals(MobileDeviceOperationMapping.Status.COMPLETED,
		                    mblOperationMapping.getStatus(),
		                    "MobileOperationMapping status has updated ");
		Assert.assertTrue(mblOperationMapping.getReceivedDate() > 0,
		                  "MobileOperationMapping received-date has updated ");
	}

	@Test(dependsOnMethods = { "addMobileDeviceOperationMappingTest",
	                           "getAllMobileDeviceOperationMappingsOfDeviceTest",
	                           "updateMobileDeviceOperationMappingToInProgressTest",
	                           "updateMobileDeviceOperationMappingToCompletedTest" })
	public void updateMobileDeviceOperationMappingTest()
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		MobileDeviceOperationMapping mblOperationMapping = new MobileDeviceOperationMapping();
		long currentTime = new java.util.Date().getTime();
		//Update device-operation mapping 1
		mblOperationMapping.setDeviceId(TEST_MOBILE_DEVICE_ID);
		mblOperationMapping.setOperationId(mblOperationId1);
		mblOperationMapping.setStatus(MobileDeviceOperationMapping.Status.INPROGRESS);
		mblOperationMapping.setSentDate(currentTime);
		mblOperationMapping.setReceivedDate(currentTime);
		boolean status =
				mblDeviceOperationMappingDAO.updateMobileDeviceOperationMapping(
						mblOperationMapping);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String selectDBQuery =
					"SELECT DEVICE_ID, OPERATION_ID, RECEIVED_DATE, SENT_DATE, STATUS FROM " +
					"MBL_DEVICE_OPERATION_MAPPING WHERE DEVICE_ID = ? AND OPERATION_ID = ?";
			preparedStatement = conn.prepareStatement(selectDBQuery);
			preparedStatement.setString(1, TEST_MOBILE_DEVICE_ID);
			preparedStatement.setInt(2, mblOperationId1);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				mblOperationMapping = new MobileDeviceOperationMapping();
				mblOperationMapping.setDeviceId(resultSet.getString(1));
				mblOperationMapping.setOperationId(resultSet.getInt(2));
				mblOperationMapping.setReceivedDate(resultSet.getLong(3));
				mblOperationMapping.setSentDate(resultSet.getLong(4));
				mblOperationMapping.setStatus(resultSet.getString(5));
			}

		} catch (SQLException e) {
			String msg = "Error in retrieving Mobile Operation Mappings data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(status, "MobileOperationMapping 1 has updated ");
		Assert.assertNotNull(mblOperationMapping, "MobileOperationMappings have fetched ");
		Assert.assertEquals(MobileDeviceOperationMapping.Status.INPROGRESS,
		                    mblOperationMapping.getStatus(),
		                    "MobileOperationMapping status has updated ");
		Assert.assertTrue(mblOperationMapping.getReceivedDate() == currentTime,
		                  "MobileOperationMapping received-date has updated ");
		Assert.assertTrue(mblOperationMapping.getSentDate() == currentTime,
		                  "MobileOperationMapping sent-date has updated ");
	}

	@Test(dependsOnMethods = { "addMobileDeviceOperationMappingTest",
	                           "getAllMobileDeviceOperationMappingsOfDeviceTest",
	                           "updateMobileDeviceOperationMappingToInProgressTest" })
	public void getAllPendingOperationMappingsOfMobileDeviceTest()
			throws MobileDeviceManagementDAOException {
		List<MobileDeviceOperationMapping> mblOperationMappings =
				mblDeviceOperationMappingDAO.getAllPendingOperationMappingsOfMobileDevice(
						TEST_MOBILE_DEVICE_ID);
		Assert.assertNotNull(mblOperationMappings, "Pending MobileOperationMappings have fetched ");
		Assert.assertTrue(mblOperationMappings.size() == 1,
		                  "All MobileOperationMappings have fetched ");
		for (MobileDeviceOperationMapping mblOperationMapping : mblOperationMappings) {
			Assert.assertEquals(TEST_MOBILE_DEVICE_ID, mblOperationMapping.getDeviceId(),
			                    "MobileOperationMapping device id has fetched ");
			Assert.assertTrue(mblOperationMapping.getOperationId() == mblOperationId2,
			                  "MobileOperationMapping operation-id has fetched ");
			Assert.assertEquals(MobileDeviceOperationMapping.Status.NEW,
			                    mblOperationMapping.getStatus(),
			                    "MobileOperationMapping status has fetched ");
			Assert.assertTrue(mblOperationMapping.getSentDate() == 0,
			                  "MobileOperationMapping sent-date has fetched ");
			Assert.assertTrue(mblOperationMapping.getReceivedDate() == 0,
			                  "MobileOperationMapping received-date has fetched ");
		}
	}

	@Test(dependsOnMethods = { "addMobileDeviceOperationMappingTest",
	                           "getAllMobileDeviceOperationMappingsOfDeviceTest",
	                           "updateMobileDeviceOperationMappingToInProgressTest",
	                           "updateMobileDeviceOperationMappingToCompletedTest",
	                           "updateMobileDeviceOperationMappingTest" })
	public void deleteMobileDeviceOperationMappingTest()
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		boolean status =
				mblDeviceOperationMappingDAO.deleteMobileDeviceOperationMapping(
						TEST_MOBILE_DEVICE_ID, mblOperationId1);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String selectDBQuery =
					"SELECT DEVICE_ID, OPERATION_ID, RECEIVED_DATE, SENT_DATE, STATUS FROM " +
					"MBL_DEVICE_OPERATION_MAPPING WHERE DEVICE_ID = ? AND OPERATION_ID = ?";
			preparedStatement = conn.prepareStatement(selectDBQuery);
			preparedStatement.setString(1, TEST_MOBILE_DEVICE_ID);
			preparedStatement.setInt(2, mblOperationId1);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				status = false;
			}
		} catch (SQLException e) {
			String msg = "Error in retrieving MobileFeatureProperty data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(status, "MobileDeviceOperationMapping 1 has deleted ");
	}
}
