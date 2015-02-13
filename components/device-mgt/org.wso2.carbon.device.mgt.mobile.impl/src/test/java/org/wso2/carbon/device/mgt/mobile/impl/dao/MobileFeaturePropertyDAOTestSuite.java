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
import org.wso2.carbon.device.mgt.mobile.dao.impl.MobileFeaturePropertyDAOImpl;
import org.wso2.carbon.device.mgt.mobile.dto.MobileFeature;
import org.wso2.carbon.device.mgt.mobile.dto.MobileFeatureProperty;
import org.wso2.carbon.device.mgt.mobile.impl.common.DBTypes;
import org.wso2.carbon.device.mgt.mobile.impl.common.TestDBConfiguration;
import org.wso2.carbon.device.mgt.mobile.impl.dao.util.MobileDatabaseUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Class for holding unit-tests related to MobileFeaturePropertyDAO class.
 *
 */
public class MobileFeaturePropertyDAOTestSuite {
	private static final Log log = LogFactory.getLog(MobileFeaturePropertyDAOTestSuite.class);
	public static final String MBL_FEATURE_NAME = "WIFI";
	private static final String MBL_FEATURE_CODE = "500A";
	public static final String MBL_FEATURE_DESCRIPTION = "Wifi config";
	public static final String MBL_FEATURE_DEVICE_TYPE = "Android";
	public static final String MBL_FEATURE_PROP_1 = "SSID";
	public static final String MBL_FEATURE_PROP_2 = "PASSWORD";
	private TestDBConfiguration testDBConfiguration;
	private MobileFeatureDAOImpl mblFeatureDAO;
	private MobileFeaturePropertyDAOImpl mobileFeaturePropertyDAO;
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
				mobileFeaturePropertyDAO = new MobileFeaturePropertyDAOImpl(testDataSource);
			default:
		}
	}

	@Test
	public void addMobileFeaturePropertyTest()
			throws MobileDeviceManagementDAOException {

		Connection conn = null;
		PreparedStatement preparedStatement = null;
		List<MobileFeatureProperty> propertyList = new ArrayList<MobileFeatureProperty>();
		//Add a new MobileFeature to the database
		MobileFeature mobileFeature = new MobileFeature();
		mobileFeature.setCode(MBL_FEATURE_CODE);
		mobileFeature.setDescription(MBL_FEATURE_DESCRIPTION);
		mobileFeature.setName(MBL_FEATURE_NAME);
		mobileFeature.setDeviceType(MBL_FEATURE_DEVICE_TYPE);
		mblFeatureId = mblFeatureDAO.addMobileFeature(mobileFeature);

		//Add 1st property to the feature
		MobileFeatureProperty mobileFeatureProperty = new MobileFeatureProperty();
		mobileFeatureProperty.setFeatureID(mblFeatureId);
		mobileFeatureProperty.setProperty(MBL_FEATURE_PROP_1);
		boolean status1 = mobileFeaturePropertyDAO.addMobileFeatureProperty(mobileFeatureProperty);

		//Add 2nd property to the feature
		mobileFeatureProperty.setProperty(MBL_FEATURE_PROP_2);
		boolean status2 = mobileFeaturePropertyDAO.addMobileFeatureProperty(mobileFeatureProperty);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String query =
					"SELECT FEATURE_ID, PROPERTY FROM MBL_FEATURE_PROPERTY WHERE FEATURE_ID = ?";
			preparedStatement = conn.prepareStatement(query);
			preparedStatement.setInt(1, mblFeatureId);
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				mobileFeatureProperty = new MobileFeatureProperty();
				mobileFeatureProperty.setFeatureID(resultSet.getInt(1));
				mobileFeatureProperty.setProperty(resultSet.getString(2));
				propertyList.add(mobileFeatureProperty);
			}
		} catch (SQLException e) {
			String msg = "Error in retrieving Mobile Feature data ";
			log.error(msg, e);
			throw new MobileDeviceManagementDAOException(msg, e);
		} finally {
			MobileDatabaseUtils.cleanupResources(conn, preparedStatement, null);
		}
		Assert.assertTrue(status1, "MobileFeatureProperty1 has added ");
		Assert.assertTrue(status2, "MobileFeatureProperty2 has added ");
		Assert.assertTrue(propertyList.size() == 2, "MobileFeatureProperties have retrieved ");

		for (MobileFeatureProperty mblFeatureProperty : propertyList) {
			Assert.assertNotNull(mblFeatureProperty.getProperty(),
			                     "MobileFeatureProperty property has persisted ");
			Assert.assertNotNull(mblFeatureProperty.getFeatureID(),
			                     "MobileFeatureProperty feature-id has persisted ");
		}

	}

	@Test(dependsOnMethods = { "addMobileFeaturePropertyTest" })
	public void getMobileFeaturePropertyTest()
			throws MobileDeviceManagementDAOException {
		MobileFeatureProperty mobileFeatureProperty =
				mobileFeaturePropertyDAO.getMobileFeatureProperty(MBL_FEATURE_PROP_1);
		Assert.assertNotNull(mobileFeatureProperty, "MobileFeatureProperty has retrieved ");
		Assert.assertEquals(MBL_FEATURE_PROP_1, mobileFeatureProperty.getProperty(),
		                    "MobileFeatureProperty property has retrieved ");
		Assert.assertTrue(mblFeatureId == mobileFeatureProperty.getFeatureID(),
		                  "MobileFeatureProperty featureId has retrieved ");
	}

	@Test(dependsOnMethods = { "addMobileFeaturePropertyTest" })
	public void getFeaturePropertyOfFeatureTest()
			throws MobileDeviceManagementDAOException {
		List<MobileFeatureProperty> mobileFeatureProperties =
				mobileFeaturePropertyDAO.getFeaturePropertiesOfFeature(mblFeatureId);
		Assert.assertNotNull(mobileFeatureProperties, "MobileFeatureProperty list has retrieved ");
		Assert.assertTrue(mobileFeatureProperties.size() == 2,
		                  "MobileFeatureProperties have fetched ");
		for (MobileFeatureProperty mblFeatureProperty : mobileFeatureProperties) {
			Assert.assertNotNull(mblFeatureProperty.getProperty(),
			                     "MobileFeatureProperty property has fetched ");
			Assert.assertNotNull(mblFeatureProperty.getFeatureID(),
			                     "MobileFeatureProperty feature-id has fetched ");
		}
	}

	@Test(dependsOnMethods = { "addMobileFeaturePropertyTest", "getMobileFeaturePropertyTest",
	                           "getFeaturePropertyOfFeatureTest" }, expectedExceptions = MobileDeviceManagementDAOException.class)
	public void updateMobileFeaturePropertyTest() throws MobileDeviceManagementDAOException {
		//Update 1st property to a non-exist feature
		MobileFeatureProperty mobileFeatureProperty = new MobileFeatureProperty();
		mobileFeatureProperty.setFeatureID(2);
		mobileFeatureProperty.setProperty(MBL_FEATURE_PROP_1);
		mobileFeaturePropertyDAO.updateMobileFeatureProperty(mobileFeatureProperty);
	}

	@Test(dependsOnMethods = { "addMobileFeaturePropertyTest", "getMobileFeaturePropertyTest",
	                           "getFeaturePropertyOfFeatureTest" })
	public void deleteMobileFeaturePropertyTest()
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		boolean status =
				mobileFeaturePropertyDAO.deleteMobileFeatureProperty(MBL_FEATURE_PROP_2);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String query =
					"SELECT PROPERTY, FEATURE_ID FROM MBL_FEATURE_PROPERTY WHERE PROPERTY = ?";
			preparedStatement = conn.prepareStatement(query);
			preparedStatement.setString(1, MBL_FEATURE_PROP_2);
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
		Assert.assertTrue(status, "MobileFeatureProperty has deleted ");
	}

	@Test(dependsOnMethods = { "addMobileFeaturePropertyTest", "getMobileFeaturePropertyTest",
	                           "getFeaturePropertyOfFeatureTest" , "updateMobileFeaturePropertyTest",
	                           "deleteMobileFeaturePropertyTest"})
	public void deleteMobileFeaturePropertiesOfFeatureTest()
			throws MobileDeviceManagementDAOException {
		Connection conn = null;
		PreparedStatement preparedStatement = null;
		boolean status =
				mobileFeaturePropertyDAO.deleteMobileFeaturePropertiesOfFeature(mblFeatureId);
		try {
			conn = DriverManager.getConnection(testDBConfiguration.getConnectionURL());
			String query =
					"SELECT PROPERTY, FEATURE_ID FROM MBL_FEATURE_PROPERTY WHERE FEATURE_ID = ?";
			preparedStatement = conn.prepareStatement(query);
			preparedStatement.setInt(1, mblFeatureId);
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
		Assert.assertTrue(status, "MobileFeatureProperties has deleted ");
	}

}
