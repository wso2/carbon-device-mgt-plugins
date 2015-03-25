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
import org.wso2.carbon.device.mgt.mobile.dto.MobileOperation;
import org.wso2.carbon.device.mgt.mobile.impl.common.DBTypes;
import org.wso2.carbon.device.mgt.mobile.impl.common.TestDBConfiguration;
import org.wso2.carbon.device.mgt.mobile.impl.dao.util.MobileDatabaseUtils;

import java.sql.*;

/**
 * Class for holding unit-tests related to MobileOperationDAO class.
 */
public class MobileOperationDAOTestSuite {

	private static final Log log = LogFactory.getLog(MobileOperationDAOTestSuite.class);
	public static final String TEST_MBL_OPR_FEATURE_CODE = "LOCK";
	public static final String TEST_MBL_OPR_UPDATED_FEATURE_CODE = "MUTE";
	public static final long TEST_MBL_OPR_CREATED_DATE = new java.util.Date().getTime();
	private TestDBConfiguration testDBConfiguration;
	private MobileOperationDAOImpl mblOperationDAO;
	private int mblOperationId;

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
				mblOperationDAO = new MobileOperationDAOImpl(testDataSource);
			default:
		}
	}

	@Test
	public void addMobileOperationTest()
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		MobileOperation mblOperation = new MobileOperation();
		MobileOperation testMblOperation = new MobileOperation();
		mblOperation.setFeatureCode(TEST_MBL_OPR_FEATURE_CODE);
		mblOperation.setCreatedDate(TEST_MBL_OPR_CREATED_DATE);
		mblOperationId = mblOperationDAO.addMobileOperation(mblOperation);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String selectDBQuery =
					"SELECT OPERATION_ID, FEATURE_CODE, CREATED_DATE FROM AD_OPERATION WHERE OPERATION_ID = ?";
			preparedStatement = conn.prepareStatement(selectDBQuery);
			preparedStatement.setInt(1, mblOperationId);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				testMblOperation.setOperationId(resultSet.getInt(1));
				testMblOperation.setFeatureCode(resultSet.getString(2));
				testMblOperation.setCreatedDate(resultSet.getLong(3));
			}
		} catch (SQLException e) {
			String msg = "Error in retrieving Mobile Operation data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(mblOperationId > 0, "MobileOperation has added ");
		Assert.assertEquals(TEST_MBL_OPR_FEATURE_CODE, testMblOperation.getFeatureCode(),
		                    "MobileOperation feature code has persisted ");
		Assert.assertEquals(TEST_MBL_OPR_CREATED_DATE, testMblOperation.getCreatedDate(),
		                    "MobileOperation created-date has persisted ");
	}

	@Test(dependsOnMethods = { "addMobileOperationTest" })
	public void getMobileOperationTest()
			throws MobileDeviceManagementDAOException {

		MobileOperation mobileOperation = mblOperationDAO.getMobileOperation(mblOperationId);
		Assert.assertEquals(TEST_MBL_OPR_CREATED_DATE, mobileOperation.getCreatedDate(),
		                    "MobileOperation created-date has retrieved ");
		Assert.assertEquals(TEST_MBL_OPR_FEATURE_CODE, mobileOperation.getFeatureCode(),
		                    "MobileOperation feature-code has retrieved ");
	}

	@Test(dependsOnMethods = { "addMobileOperationTest", "getMobileOperationTest" })
	public void updateMobileOperationTest()
			throws MobileDeviceManagementDAOException {

		Connection conn = null;
		PreparedStatement preparedStatement = null;
		long updatedDate = new java.util.Date().getTime();
		MobileOperation mblOperation = new MobileOperation();
		MobileOperation testMblOperation = new MobileOperation();
		mblOperation.setFeatureCode(TEST_MBL_OPR_UPDATED_FEATURE_CODE);
		mblOperation.setCreatedDate(updatedDate);
		mblOperation.setOperationId(mblOperationId);
		boolean status = mblOperationDAO.updateMobileOperation(mblOperation);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String selectDBQuery =
					"SELECT OPERATION_ID, FEATURE_CODE, CREATED_DATE FROM AD_OPERATION WHERE OPERATION_ID = ?";
			preparedStatement = conn.prepareStatement(selectDBQuery);
			preparedStatement.setInt(1, mblOperationId);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				testMblOperation.setOperationId(resultSet.getInt(1));
				testMblOperation.setFeatureCode(resultSet.getString(2));
				testMblOperation.setCreatedDate(resultSet.getLong(3));
			}
		} catch (SQLException e) {
			String msg = "Error in retrieving Mobile Operation data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(status, "MobileOperation has updated ");
		Assert.assertEquals(TEST_MBL_OPR_UPDATED_FEATURE_CODE, testMblOperation.getFeatureCode(),
		                    "MobileOperation feature code has updated ");
		Assert.assertEquals(updatedDate, testMblOperation.getCreatedDate(),
		                    "MobileOperation created-date has updated ");
	}

	@Test(dependsOnMethods = { "addMobileOperationTest", "getMobileOperationTest",
	                           "updateMobileOperationTest" })
	public void deleteMobileDeviceTest()
			throws MobileDeviceManagementDAOException {

		Connection conn = null;
		PreparedStatement preparedStatement = null;
		boolean deleted = mblOperationDAO.deleteMobileOperation(mblOperationId);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String selectDBQuery =
					"SELECT OPERATION_ID, FEATURE_CODE, CREATED_DATE FROM AD_OPERATION WHERE OPERATION_ID = ?";
			preparedStatement = conn.prepareStatement(selectDBQuery);
			preparedStatement.setInt(1, mblOperationId);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				deleted = false;
			}
		} catch (SQLException e) {
			String msg = "Error in retrieving Mobile Operation data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(deleted, "MobileOperation has deleted ");
	}
}
