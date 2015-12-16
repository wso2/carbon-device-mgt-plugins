package org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.impl.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.dao.IotDeviceManagementDAOException;

/**
 * Created by geesara on 12/9/15.
 */
public class DroneAnalyzerDeviceDAOImplTest {
    DroneAnalyzerDeviceDAOImpl dao;
    @Before
    public void init(){
        dao = new DroneAnalyzerDeviceDAOImpl();
    }

    //@Test
    public void testDBConnenction(){
        try {
            dao.deleteIotDevice("device1");
        } catch (IotDeviceManagementDAOException e) {
            e.printStackTrace();
        }
    }
}
