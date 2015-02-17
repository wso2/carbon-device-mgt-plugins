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

package org.wso2.carbon.device.mgt.mobile.impl.dao.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.impl.common.DBTypes;
import org.wso2.carbon.device.mgt.mobile.impl.common.TestDBConfiguration;
import org.wso2.carbon.device.mgt.mobile.impl.common.TestDBConfigurations;
import org.wso2.carbon.device.mgt.mobile.util.MobileDeviceManagementUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.sql.*;

/**
 * This class provides the utility methods needed for DAO related test executions.
 */
public class MobileDatabaseUtils {

	private static final Log log = LogFactory.getLog(MobileDatabaseUtils.class);
	public static final String TEST_RESOURCES_DB_CONFIG_FILE =
			"src/test/resources/testdbconfig.xml";

	public static void cleanupResources(Connection conn, Statement stmt, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.warn("Error occurred while closing result set", e);
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				log.warn("Error occurred while closing prepared statement", e);
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.warn("Error occurred while closing database connection", e);
			}
		}
	}

	public static TestDBConfiguration getTestDBConfiguration(DBTypes dbType) throws
	                                                                   MobileDeviceManagementDAOException,
	                                                                   DeviceManagementException {
		File deviceMgtConfig = new File(TEST_RESOURCES_DB_CONFIG_FILE);
		Document doc;
		TestDBConfiguration testDBConfiguration = null;
		TestDBConfigurations testDBConfigurations;

		doc = MobileDeviceManagementUtil.convertToDocument(deviceMgtConfig);
		JAXBContext testDBContext;

		try {
			testDBContext = JAXBContext.newInstance(TestDBConfigurations.class);
			Unmarshaller unmarshaller = testDBContext.createUnmarshaller();
			testDBConfigurations = (TestDBConfigurations) unmarshaller.unmarshal(doc);
		} catch (JAXBException e) {
			throw new MobileDeviceManagementDAOException("Error parsing test db configurations", e);
		}

		for (TestDBConfiguration testDBConfiguration1 : testDBConfigurations.getDbTypesList()) {
			testDBConfiguration = testDBConfiguration1;
			if (testDBConfiguration.getType().equals(dbType.toString())) {
				break;
			}
		}

		return testDBConfiguration;
	}

	public static void createH2DB(TestDBConfiguration testDBConf) throws Exception {
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName(testDBConf.getDriverClassName());
			conn = DriverManager.getConnection(testDBConf.getConnectionURL());
			stmt = conn.createStatement();
			stmt.executeUpdate("RUNSCRIPT FROM './src/test/resources/sql/CreateH2TestDB.sql'");
		} finally {
			cleanupResources(conn, stmt, null);
		}
	}
}
