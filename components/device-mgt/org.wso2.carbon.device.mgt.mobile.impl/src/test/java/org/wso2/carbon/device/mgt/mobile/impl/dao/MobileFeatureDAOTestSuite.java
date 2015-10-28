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
import org.wso2.carbon.device.mgt.mobile.dao.impl.MobileFeatureDAOImpl;
import org.wso2.carbon.device.mgt.mobile.dto.MobileFeature;
import org.wso2.carbon.device.mgt.mobile.impl.common.DBTypes;
import org.wso2.carbon.device.mgt.mobile.impl.common.TestDBConfiguration;
import org.wso2.carbon.device.mgt.mobile.impl.dao.util.MobileDatabaseUtils;

import java.sql.*;
import java.util.List;

/**
 *
 * Class for holding unit-tests related to MobileFeatureDAO class.
 *
 */
public class MobileFeatureDAOTestSuite {

	private static final Log log = LogFactory.getLog(MobileFeatureDAOTestSuite.class);
	public static final String MBL_FEATURE_NAME = "Camera";
	private static final String MBL_FEATURE_CODE = "500A";
	public static final String MBL_FEATURE_DESCRIPTION = "Camera enable or disable";
	public static final String MBL_FEATURE_DEVICE_TYPE = "Android";
	public static final String MBL_FEATURE_UPDATED_CODE = "501B";
	private TestDBConfiguration testDBConfiguration;
	private MobileFeatureDAOImpl mblFeatureDAO;
	private int mblFeatureId;

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
				mblFeatureDAO = new MobileFeatureDAOImpl(testDataSource);
			default:
		}
	}

	@Test
	public void addMobileFeatureTest()
			throws MobileDeviceManagementDAOException {

		Connection conn = null;
		PreparedStatement preparedStatement = null;
		MobileFeature mobileFeature = new MobileFeature();
		MobileFeature testMblFeature = new MobileFeature();
		mobileFeature.setCode(MBL_FEATURE_CODE);
		mobileFeature.setDescription(MBL_FEATURE_DESCRIPTION);
		mobileFeature.setName(MBL_FEATURE_NAME);
		mobileFeature.setDeviceType(MBL_FEATURE_DEVICE_TYPE);
		mblFeatureDAO.addFeature(mobileFeature);

		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String query =
					"SELECT ID, CODE, NAME, DESCRIPTION, DEVICE_TYPE FROM AD_FEATURE WHERE CODE = ?";
			preparedStatement = conn.prepareStatement(query);
			preparedStatement.setString(1, MBL_FEATURE_CODE);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				testMblFeature.setId(resultSet.getInt(1));
				testMblFeature.setCode(resultSet.getString(2));
				testMblFeature.setName(resultSet.getString(3));
				testMblFeature.setDescription(resultSet.getString(4));
				testMblFeature.setDeviceType(resultSet.getString(5));
			}
		} catch (SQLException e) {
			String msg = "Error in retrieving Mobile Feature data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		mblFeatureId = testMblFeature.getId();
		Assert.assertTrue(mblFeatureId > 0, "MobileFeature has added ");
		Assert.assertEquals(MBL_FEATURE_CODE, testMblFeature.getCode(),
		                    "MobileFeature code has persisted ");
		Assert.assertEquals(MBL_FEATURE_NAME, testMblFeature.getName(),
		                    "MobileFeature name has persisted ");
		Assert.assertEquals(MBL_FEATURE_DESCRIPTION, testMblFeature.getDescription(),
		                    "MobileFeature description has persisted ");
		Assert.assertEquals(MBL_FEATURE_DEVICE_TYPE, testMblFeature.getDeviceType(),
		                    "MobileFeature device-type has persisted ");
	}

	@Test(dependsOnMethods = { "addMobileFeatureTest" })
	public void getMobileFeatureByCodeTest()
			throws MobileDeviceManagementDAOException {

		MobileFeature mobileFeature = mblFeatureDAO.getFeatureByCode(MBL_FEATURE_CODE);
		Assert.assertEquals(MBL_FEATURE_CODE, mobileFeature.getCode(),
		                    "MobileFeature code has retrieved ");
		Assert.assertEquals(MBL_FEATURE_NAME, mobileFeature.getName(),
		                    "MobileFeature name has retrieved ");
		Assert.assertEquals(MBL_FEATURE_DESCRIPTION, mobileFeature.getDescription(),
		                    "MobileFeature description has retrieved ");
	}

	@Test(dependsOnMethods = { "addMobileFeatureTest" })
	public void getMobileFeatureByIdTest()
			throws MobileDeviceManagementDAOException {

		MobileFeature mobileFeature = mblFeatureDAO.getFeatureById(mblFeatureId);
		Assert.assertEquals(MBL_FEATURE_CODE, mobileFeature.getCode(),
		                    "MobileFeature code has retrieved ");
		Assert.assertEquals(MBL_FEATURE_NAME, mobileFeature.getName(),
		                    "MobileFeature name has retrieved ");
		Assert.assertEquals(MBL_FEATURE_DESCRIPTION, mobileFeature.getDescription(),
		                    "MobileFeature description has retrieved ");
	}

	@Test(dependsOnMethods = { "addMobileFeatureTest" })
	public void getAllMobileFeaturesTest()
			throws MobileDeviceManagementDAOException {

		List<MobileFeature> mobileFeatures = mblFeatureDAO.getAllFeatures();
		Assert.assertNotNull(mobileFeatures, "MobileFeature list is not null");
		Assert.assertTrue(mobileFeatures.size() > 0, "MobileFeature list has 1 MobileFeature");
	}

	@Test(dependsOnMethods = { "addMobileFeatureTest", "getMobileFeatureByCodeTest",
	                           "getMobileFeatureByIdTest", "getAllMobileFeaturesTest" })
	public void updateMobileFeatureTest()
			throws MobileDeviceManagementDAOException {

		Connection conn = null;
		PreparedStatement stmt = null;

		MobileFeature mobileFeature = new MobileFeature();
		MobileFeature testMblFeature = new MobileFeature();
		mobileFeature.setCode(MBL_FEATURE_UPDATED_CODE);
		mobileFeature.setDescription(MBL_FEATURE_DESCRIPTION);
		mobileFeature.setName(MBL_FEATURE_NAME);
		mobileFeature.setId(mblFeatureId);
		boolean updated = mblFeatureDAO.updateFeature(mobileFeature);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String query =
					"SELECT ID, CODE, NAME, DESCRIPTION FROM AD_FEATURE WHERE CODE = ?";
			stmt = conn.prepareStatement(query);
			stmt.setString(1, MBL_FEATURE_UPDATED_CODE);
			ResultSet resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				testMblFeature.setId(resultSet.getInt(1));
				testMblFeature.setCode(resultSet.getString(2));
				testMblFeature.setName(resultSet.getString(3));
				testMblFeature.setDescription(resultSet.getString(4));
			}
		} catch (SQLException e) {
			String msg = "Error in updating Mobile Feature data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, stmt, null);
		}
		Assert.assertTrue(updated, "MobileFeature has updated");
		Assert.assertEquals(MBL_FEATURE_UPDATED_CODE, testMblFeature.getCode(),
		                    "MobileFeature data has updated ");
	}

	@Test(dependsOnMethods = { "addMobileFeatureTest", "getMobileFeatureByCodeTest",
	                           "getMobileFeatureByIdTest", "getAllMobileFeaturesTest",
	                           "updateMobileFeatureTest" })
	public void deleteMobileFeatureByIdTest()
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement stmt = null;

		boolean status = mblFeatureDAO.deleteFeatureById(mblFeatureId);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String query = "SELECT ID, CODE FROM AD_FEATURE WHERE ID = ?";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, mblFeatureId);
			ResultSet resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				status = false;
			}
		} catch (SQLException e) {
			String msg = "Error in deleting Mobile Feature data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, stmt, null);
		}
		Assert.assertTrue(status, "MobileFeature has deleted ");
	}

	@Test(dependsOnMethods = { "addMobileFeatureTest", "getMobileFeatureByCodeTest",
	                           "getMobileFeatureByIdTest", "getAllMobileFeaturesTest",
	                           "updateMobileFeatureTest", "deleteMobileFeatureByIdTest" })
	public void deleteMobileFeatureByCodeTest()
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		MobileFeature mobileFeature = new MobileFeature();
		mobileFeature.setCode(MBL_FEATURE_CODE);
		mobileFeature.setDescription(MBL_FEATURE_DESCRIPTION);
		mobileFeature.setName(MBL_FEATURE_NAME);
		mobileFeature.setDeviceType(MBL_FEATURE_DEVICE_TYPE);
		mblFeatureDAO.addFeature(mobileFeature);
		boolean status = mblFeatureDAO.deleteFeatureByCode(MBL_FEATURE_CODE);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String query = "SELECT ID, CODE FROM AD_FEATURE WHERE CODE = ?";
			preparedStatement = conn.prepareStatement(query);
			preparedStatement.setString(1, MBL_FEATURE_CODE);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				status = false;
			}
		} catch (SQLException e) {
			String msg = "Error in deleting Mobile Feature data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(status, "MobileFeature has deleted ");
	}
}
