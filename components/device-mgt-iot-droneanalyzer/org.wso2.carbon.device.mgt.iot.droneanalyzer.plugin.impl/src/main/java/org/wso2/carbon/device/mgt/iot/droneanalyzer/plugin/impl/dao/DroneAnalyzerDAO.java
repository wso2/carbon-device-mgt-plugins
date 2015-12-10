package org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.impl.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.impl.dao.impl.DroneAnalyzerDeviceDAOImpl;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.dao.IotDeviceDAO;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.dao.IotDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.dao.IotDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.dao.IotDeviceManagementDAOFactoryInterface;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by geesara on 12/9/15.
 */
public class DroneAnalyzerDAO extends IotDeviceManagementDAOFactory implements IotDeviceManagementDAOFactoryInterface {

    private static final Log log = LogFactory.getLog(DroneAnalyzerDAO.class);
    static DataSource dataSource;
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();

    public DroneAnalyzerDAO() {
        initFireAlarmDAO();
    }

    public static void initFireAlarmDAO() {
        dataSource = getDataSourceMap().get(DroneConstants.DEVICE_TYPE);
    }

    @Override
    public IotDeviceDAO getIotDeviceDAO() {
        return new DroneAnalyzerDeviceDAOImpl();
    }

    public static void beginTransaction() throws IotDeviceManagementDAOException {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            throw new IotDeviceManagementDAOException("Error occurred while retrieving datasource connection", e);
        }
    }

    public static Connection getConnection() throws IotDeviceManagementDAOException {

        if (currentConnection != null && currentConnection.get() == null) {
            try {
                currentConnection.set(dataSource.getConnection());
            } catch (SQLException e) {
                throw new IotDeviceManagementDAOException("Error occurred while retrieving data source connection", e);
            }
        }
        //log.info("------>");
        return currentConnection.get();
    }

    public static void commitTransaction() throws IotDeviceManagementDAOException {
        try {
            Connection conn = currentConnection.get();
            if (conn != null) {
                conn.commit();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Datasource connection associated with the current thread is null, hence commit "
                            + "has not been attempted");
                }
            }
        } catch (SQLException e) {
            throw new IotDeviceManagementDAOException("Error occurred while committing the transaction", e);
        } finally {
            closeConnection();
        }
    }

    public static void closeConnection() throws IotDeviceManagementDAOException {

        Connection con = currentConnection.get();
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("Error occurred while close the connection");
            }
        }
        currentConnection.remove();
    }

    public static void rollbackTransaction() throws IotDeviceManagementDAOException {
        try {
            Connection conn = currentConnection.get();
            if (conn != null) {
                conn.rollback();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Datasource connection associated with the current thread is null, hence rollback "
                            + "has not been attempted");
                }
            }
        } catch (SQLException e) {
            throw new IotDeviceManagementDAOException("Error occurred while rollback the transaction", e);
        } finally {
            closeConnection();
        }
    }
}
