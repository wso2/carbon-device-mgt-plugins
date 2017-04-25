package org.wso2.carbon.device.mgt.mobile.windows.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dao.WindowsDAOFactory;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dao.WindowsEnrollmentTokenDAO;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dao.impl.WindowsEnrollmentTokenDAOImpl;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dto.MobileCacheEntry;

public class WindowsTokenServiceImpl implements WindowsTokenService {

    private static final Log log = LogFactory.getLog(WindowsTokenServiceImpl.class);
    private static WindowsEnrollmentTokenDAO windowsEnrollmentTokenDAO;

    public WindowsTokenServiceImpl() {
        WindowsDAOFactory windowsDAOFactory = new WindowsDAOFactory();
        windowsEnrollmentTokenDAO = new WindowsEnrollmentTokenDAOImpl();
    }

    @Override
    public void saveCacheToken(MobileCacheEntry entry) throws MobileDeviceManagementDAOException {
        try {
            WindowsDAOFactory.beginTransaction();
            windowsEnrollmentTokenDAO.addCacheToken(entry);
            WindowsDAOFactory.commitTransaction();
        }  finally {
            WindowsDAOFactory.closeConnection();
        }
    }

    @Override
    public void updateCacheToken(MobileCacheEntry entry) throws MobileDeviceManagementDAOException {
        try {
            WindowsDAOFactory.beginTransaction();
            windowsEnrollmentTokenDAO.updateCacheToken(entry);
            WindowsDAOFactory.commitTransaction();
        } finally {
            WindowsDAOFactory.closeConnection();
        }
    }

    @Override
    public MobileCacheEntry getCacheToken(String token) throws MobileDeviceManagementDAOException {
        MobileCacheEntry cacheEntry = null;
        try {
            WindowsDAOFactory.beginTransaction();
            cacheEntry = windowsEnrollmentTokenDAO.getCacheToken(token);
            WindowsDAOFactory.commitTransaction();
        } finally {
            WindowsDAOFactory.closeConnection();
        }
        return cacheEntry;
    }

    @Override public MobileCacheEntry getCacheTokenFromDeviceId(String deviceId)
            throws MobileDeviceManagementDAOException {
        MobileCacheEntry cacheEntry = null;
        try {
            WindowsDAOFactory.beginTransaction();
            cacheEntry = windowsEnrollmentTokenDAO.getCacheTokenFromDeviceId(deviceId);
            WindowsDAOFactory.commitTransaction();
        } finally {
            WindowsDAOFactory.closeConnection();
        }
        return cacheEntry;
    }

    @Override
    public void removeCacheToken(String token) throws MobileDeviceManagementDAOException {
        try {
            WindowsDAOFactory.beginTransaction();
            windowsEnrollmentTokenDAO.deleteCacheToken(token);
            WindowsDAOFactory.commitTransaction();
        } finally {
            WindowsDAOFactory.closeConnection();
        }
    }
}
