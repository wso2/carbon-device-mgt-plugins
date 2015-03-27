/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.mobile.impl.ios.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.mobile.impl.ios.dao.impl.FeatureDAOImpl;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class FeatureManagementDAOFactory {

    private static DataSource dataSource;
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();
    private static final Log log = LogFactory.getLog(FeatureManagementDAOFactory.class);

    public static FeatureDAO getFeatureDAO() {
        return new FeatureDAOImpl();
    }

    public static void init(DataSource dtSource) {
        dataSource = dtSource;
    }

    public static void beginTransaction() throws FeatureManagementDAOException {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            throw new FeatureManagementDAOException("Error occurred while retrieving datasource connection", e);
        }
    }

    public static Connection getConnection() {
        return currentConnection.get();
    }

    public static void commitTransaction() throws FeatureManagementDAOException {
        try {
            Connection conn = currentConnection.get();
            if (conn != null) {
                conn.commit();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Datasource connection associated with the current thread is null, hence commit " +
                            "has not been attempted");
                }
            }
        } catch (SQLException e) {
            throw new FeatureManagementDAOException("Error occurred while committing the transaction", e);
        }
    }

    public static void rollbackTransaction() throws FeatureManagementDAOException {
        try {
            Connection conn = currentConnection.get();
            if (conn != null) {
                conn.rollback();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Datasource connection associated with the current thread is null, hence rollback " +
                            "has not been attempted");
                }
            }
        } catch (SQLException e) {
            throw new FeatureManagementDAOException("Error occurred while rollbacking the transaction", e);
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

}
