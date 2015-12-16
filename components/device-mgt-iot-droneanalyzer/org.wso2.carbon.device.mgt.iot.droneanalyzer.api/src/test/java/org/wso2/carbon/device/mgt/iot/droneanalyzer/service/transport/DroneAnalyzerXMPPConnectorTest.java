package org.wso2.carbon.device.mgt.iot.droneanalyzer.service.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;

import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Created by geesara on 12/10/15.
 */
public class DroneAnalyzerXMPPConnectorTest {
    private static Log log = LogFactory.getLog(DroneAnalyzerXMPPConnectorTest.class);
    public DroneAnalyzerXMPPConnector droneAnalyzerXMPPConnector;

    @BeforeClass
    public void setup(){
        //droneAnalyzerXMPPConnector = new DroneAnalyzerXMPPConnector();
        //droneAnalyzerXMPPConnector.initConnector();
    }

    //@Test
    public void login(){
       // droneAnalyzerXMPPConnector.connectAndLogin();
       // log.info("ip address "+XmppConfig.getInstance().getXmppServerIP());
        //log.info("path "+ CarbonUtils.getCarbonConfigDirPath());
       // log.info("path "+ CarbonUtils.getCarbonHome());
        //System.out.println(System.getProperty("carbon.home"));
        System.out.println(System.getenv("CARBON_HOME"));

    }
}
