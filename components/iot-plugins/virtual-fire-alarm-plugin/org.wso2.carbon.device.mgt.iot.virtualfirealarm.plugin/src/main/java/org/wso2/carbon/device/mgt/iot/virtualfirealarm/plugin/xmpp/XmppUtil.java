package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.xmpp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.exception.VirtualFirealarmDeviceMgtPluginException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * This holds the utility class related to XMPP.
 */
public class XmppUtil {
    private static final Log log = LogFactory.getLog(XmppUtil.class);

    public static void createXMPPAccountForDeviceType() {
        if (!XmppConfig.getInstance().isEnabled()) {
            return;
        }
        XmppServerClient xmppServerClient = new XmppServerClient();
        try {
            XmppAccount xmppAccount = new XmppAccount();
            xmppAccount.setAccountName(XmppConfig.getInstance().getVirtualFirealarmAdminJID());
            xmppAccount.setUsername(XmppConfig.getInstance().getVirtualFirealarmAdminUsername());
            xmppAccount.setPassword(XmppConfig.getInstance().getVirtualFirealarmAdminPassword());
            xmppAccount.setEmail("");
            xmppServerClient.createAccount(xmppAccount);
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            String errorMsg = "An error was encountered whilst trying to create Server XMPP account for device-type - "
                    + VirtualFireAlarmConstants.DEVICE_TYPE;
            log.error(errorMsg, e);
        }
    }
}
