/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.mobile.impl.windows.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.mobile.dao.*;
import org.wso2.carbon.device.mgt.mobile.impl.windows.dao.impl.WindowsDeviceDAOImpl;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class WindowsDAOFactory extends MobileDeviceManagementDAOFactory
        implements MobileDeviceManagementDAOFactoryInterface {

    private static final Log log = LogFactory.getLog(WindowsDAOFactory.class);
    protected static DataSource dataSource;
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();

    public WindowsDAOFactory() {
        this.dataSource = getDataSourceMap().get(DeviceManagementConstants.MobileDeviceTypes
                .MOBILE_DEVICE_TYPE_WINDOWS);
    }

    @Override
    public MobileDeviceDAO getMobileDeviceDAO() {
        return new WindowsDeviceDAOImpl();
    }

    @Override
    public MobileOperationDAO getMobileOperationDAO() {
        return null;
    }

    @Override
    public MobileOperationPropertyDAO getMobileOperationPropertyDAO() {
        return null;
    }

    @Override
    public MobileDeviceOperationMappingDAO getMobileDeviceOperationDAO() {
        return null;
    }

    @Override
    public MobileFeatureDAO getMobileFeatureDao() {
        return null;
    }

    @Override
    public MobileFeaturePropertyDAO getFeaturePropertyDAO() {
        return null;
    }

    public static void beginTransaction() throws MobileDeviceManagementDAOException {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            throw new MobileDeviceManagementDAOException("Error occurred while retrieving datasource connection", e);
        }
    }

    public static Connection getConnection() throws MobileDeviceManagementDAOException {
        if (currentConnection.get() == null) {
            try {
                currentConnection.set(dataSource.getConnection());
            } catch (SQLException e) {
                throw new MobileDeviceManagementDAOException("Error occurred while retrieving data source connection",
                        e);
            }
        }
        return currentConnection.get();
    }

    public static void commitTransaction() {
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
            log.error("Error occurred while committing the transaction", e);
        } finally {
            closeConnection();
        }
    }

    public static void closeConnection() {

        Connection con = currentConnection.get();
        try {
            con.close();
        } catch (SQLException e) {
            log.error("Error occurred while close the connection");
        }
        currentConnection.remove();
    }

    public static void rollbackTransaction() {
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
            log.warn("Error occurred while roll-backing the transaction", e);

        } finally {
            closeConnection();
        }
    }
}
