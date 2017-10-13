package org.wso2.carbon.mdm.services.android.mocks;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementService;

import java.util.List;

public class NotificationManagementServiceMock implements NotificationManagementService {
    @Override
    public boolean addNotification(DeviceIdentifier deviceIdentifier, Notification notification)
            throws NotificationManagementException {
        return false;
    }

    @Override
    public boolean updateNotification(Notification notification) throws NotificationManagementException {
        return false;
    }

    @Override
    public boolean updateNotificationStatus(int i, Notification.Status status) throws NotificationManagementException {
        return false;
    }

    @Override
    public List<Notification> getAllNotifications() throws NotificationManagementException {
        return null;
    }

    @Override
    public Notification getNotification(int i) throws NotificationManagementException {
        return null;
    }

    @Override
    public PaginationResult getAllNotifications(PaginationRequest paginationRequest)
            throws NotificationManagementException {
        return null;
    }

    @Override
    public List<Notification> getNotificationsByStatus(Notification.Status status)
            throws NotificationManagementException {
        return null;
    }

    @Override
    public PaginationResult getNotificationsByStatus(Notification.Status status, PaginationRequest paginationRequest)
            throws NotificationManagementException {
        return null;
    }
}
