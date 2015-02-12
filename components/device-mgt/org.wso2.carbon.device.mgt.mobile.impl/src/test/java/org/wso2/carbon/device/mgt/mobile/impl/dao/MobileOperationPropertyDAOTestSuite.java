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
import org.wso2.carbon.device.mgt.mobile.dao.impl.MobileOperationDAOImpl;
import org.wso2.carbon.device.mgt.mobile.dao.impl.MobileOperationPropertyDAOImpl;
import org.wso2.carbon.device.mgt.mobile.dto.MobileOperation;
import org.wso2.carbon.device.mgt.mobile.dto.MobileOperationProperty;
import org.wso2.carbon.device.mgt.mobile.impl.common.DBTypes;
import org.wso2.carbon.device.mgt.mobile.impl.common.TestDBConfiguration;
import org.wso2.carbon.device.mgt.mobile.impl.dao.util.MobileDatabaseUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MobileOperationPropertyDAOTestSuite {

	private static final Log log = LogFactory.getLog(MobileOperationPropertyDAOTestSuite.class);
	public static final String TEST_MBL_OPR_FEATURE_CODE = "LOCK";
	public static final long TEST_MBL_OPR_CREATED_DATE = new java.util.Date().getTime();
	public static final String TEST_MBL_OPR_PROPERTY_SSID = "SSID";
	public static final String TEST_MBL_OPR_PROPERTY_SSID_VALUE = "wso2";
	public static final String TEST_MBL_OPR_PROPERTY_PWD = "PASSWORD";
	public static final String TEST_MBL_OPR_PROPERTY_PWD_VALUE = "wso2";
	public static final String TEST_MBL_OPR_PROPERTY_PWD_UPDATED_VALUE = "wso2mdm";
	private int mblOperationId;
	private TestDBConfiguration testDBConfiguration;
	private MobileOperationPropertyDAOImpl mobileOperationPropertyDAO;
	private MobileOperationDAOImpl mblOperationDAO;

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
				mobileOperationPropertyDAO = new MobileOperationPropertyDAOImpl(testDataSource);
				mblOperationDAO = new MobileOperationDAOImpl(testDataSource);
			default:
		}
	}

	@Test
	public void addMobileOperationPropertyTest()
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		MobileOperation mblOperation = new MobileOperation();
		MobileOperationProperty operationProperty = new MobileOperationProperty();
		List<MobileOperationProperty> properties = new ArrayList<MobileOperationProperty>();
		//Add a new Operation to the database
		MobileOperation testMblOperation = new MobileOperation();
		mblOperation.setFeatureCode(TEST_MBL_OPR_FEATURE_CODE);
		mblOperation.setCreatedDate(TEST_MBL_OPR_CREATED_DATE);
		mblOperationId = mblOperationDAO.addMobileOperation(mblOperation);
		//Add property1
		operationProperty.setOperationId(mblOperationId);
		operationProperty.setProperty(TEST_MBL_OPR_PROPERTY_SSID);
		operationProperty.setValue(TEST_MBL_OPR_PROPERTY_SSID_VALUE);
		boolean status1 = mobileOperationPropertyDAO.addMobileOperationProperty(operationProperty);

		//add property2
		operationProperty = new MobileOperationProperty();
		operationProperty.setOperationId(mblOperationId);
		operationProperty.setProperty(TEST_MBL_OPR_PROPERTY_PWD);
		operationProperty.setValue(TEST_MBL_OPR_PROPERTY_PWD_VALUE);
		boolean status2 = mobileOperationPropertyDAO.addMobileOperationProperty(operationProperty);

		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String selectDBQuery =
					"SELECT OPERATION_ID, PROPERTY, VALUE FROM MBL_OPERATION_PROPERTY WHERE OPERATION_ID = ?";
			preparedStatement = conn.prepareStatement(selectDBQuery);
			preparedStatement.setInt(1, mblOperationId);
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				operationProperty = new MobileOperationProperty();
				operationProperty.setOperationId(resultSet.getInt(1));
				operationProperty.setProperty(resultSet.getString(2));
				operationProperty.setValue(resultSet.getString(3));
				properties.add(operationProperty);
			}

		} catch (SQLException e) {
			String msg = "Error in retrieving Mobile Operation Property data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(status1, "MobileOperationProperty1 has added ");
		Assert.assertTrue(status2, "MobileOperationProperty2 has added ");
		Assert.assertTrue(properties.size() == 2, "MobileOperationProperties have retrieved ");

		for (MobileOperationProperty mobileOperationProperty : properties) {
			Assert.assertNotNull(mobileOperationProperty.getProperty(),
			                     "MobileOperationProperty property has persisted ");
			Assert.assertNotNull(mobileOperationProperty.getValue(),
			                     "MobileOperationProperty value has persisted ");
		}
	}

	@Test(dependsOnMethods = { "addMobileOperationPropertyTest" })
	public void getMobileOperationPropertyTest()
			throws MobileDeviceManagementDAOException {
		MobileOperationProperty mobileOperationProperty = mobileOperationPropertyDAO
				.getMobileOperationProperty(mblOperationId, TEST_MBL_OPR_PROPERTY_PWD);
		Assert.assertEquals(mblOperationId, mobileOperationProperty.getOperationId(),
		                    "MobileOperationProperty operation-id has retrieved ");
		Assert.assertEquals(TEST_MBL_OPR_PROPERTY_PWD, mobileOperationProperty.getProperty(),
		                    "MobileOperationProperty property has retrieved ");
		Assert.assertEquals(TEST_MBL_OPR_PROPERTY_PWD_VALUE, mobileOperationProperty.getValue(),
		                    "MobileOperationProperty property-value has retrieved ");
	}

	@Test(dependsOnMethods = { "addMobileOperationPropertyTest" })
	public void getAllMobileOperationPropertiesOfOperationTest()
			throws MobileDeviceManagementDAOException {
		List<MobileOperationProperty> mobileOperationProperties = mobileOperationPropertyDAO
				.getAllMobileOperationPropertiesOfOperation(mblOperationId);
		Assert.assertTrue(mobileOperationProperties.size() == 2,
		                  "MobileOperationProperties of operation has retrieved");
	}

	@Test(dependsOnMethods = { "addMobileOperationPropertyTest", "getMobileOperationPropertyTest",
	                           "getAllMobileOperationPropertiesOfOperationTest" })
	public void updateMobileOperationPropertyTest()
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		MobileOperationProperty mblOperationProperty = new MobileOperationProperty();
		MobileOperationProperty testMblOperationProperty = new MobileOperationProperty();
		mblOperationProperty.setOperationId(mblOperationId);
		mblOperationProperty.setProperty(TEST_MBL_OPR_PROPERTY_PWD);
		mblOperationProperty.setValue(TEST_MBL_OPR_PROPERTY_PWD_UPDATED_VALUE);

		boolean status =
				mobileOperationPropertyDAO.updateMobileOperationProperty(mblOperationProperty);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String selectDBQuery =
					"SELECT OPERATION_ID, PROPERTY, VALUE FROM MBL_OPERATION_PROPERTY WHERE" +
					" OPERATION_ID = ? AND PROPERTY = ?";
			preparedStatement = conn.prepareStatement(selectDBQuery);
			preparedStatement.setInt(1, mblOperationId);
			preparedStatement.setString(2, TEST_MBL_OPR_PROPERTY_PWD);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				testMblOperationProperty.setOperationId(resultSet.getInt(1));
				testMblOperationProperty.setProperty(resultSet.getString(2));
				testMblOperationProperty.setValue(resultSet.getString(3));
			}
		} catch (SQLException e) {
			String msg = "Error in retrieving MobileOperationProperty data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(status, "MobileOperationProperty has updated ");
		Assert.assertEquals(TEST_MBL_OPR_PROPERTY_PWD_UPDATED_VALUE,
		                    testMblOperationProperty.getValue(),
		                    "MobileOperationProperty value has updated ");
	}

	@Test(dependsOnMethods = { "addMobileOperationPropertyTest", "getMobileOperationPropertyTest",
	                           "getAllMobileOperationPropertiesOfOperationTest",
	                           "updateMobileOperationPropertyTest" })
	public void deleteMobileOperationPropertiesOfOperationTest()
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		boolean status =
				mobileOperationPropertyDAO.deleteMobileOperationProperties(mblOperationId);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String selectDBQuery =
					"SELECT OPERATION_ID, PROPERTY, VALUE FROM MBL_OPERATION_PROPERTY WHERE" +
					" OPERATION_ID = ?";
			preparedStatement = conn.prepareStatement(selectDBQuery);
			preparedStatement.setInt(1, mblOperationId);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				status = false;
			}
		} catch (SQLException e) {
			String msg = "Error in retrieving MobileOperationProperty data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(status, "MobileOperationProperty has deleted ");
	}
}
