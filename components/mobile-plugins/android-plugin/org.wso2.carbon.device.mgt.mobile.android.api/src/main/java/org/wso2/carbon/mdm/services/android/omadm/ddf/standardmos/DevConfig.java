package org.wso2.carbon.mdm.services.android.omadm.ddf.standardmos;

import org.wso2.carbon.mdm.services.android.omadm.ddf.util.DDFCommonUtils;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.MgmtTree;

/**
 * A Vendor-specific custom MO designed to represent configuration operations of a device
 */
public class DevConfig {

    // Path to the DevInfo DDF file
    public static final String DEV_CONFIG_DDF_PATH = "OMA-SUP-MO_DM_DevConfig.xml";

    private static MgmtTree mgmtTree = DDFCommonUtils.generateTree(DEV_CONFIG_DDF_PATH);
    private static DevConfig devConfig = new DevConfig();

    private DevConfig() {
    }

    public static DevConfig getInstance() {
        if (mgmtTree != null) {
            devConfig.setMgmtTree(mgmtTree);
        }
        return devConfig;
    }

    public MgmtTree getMgmtTree() {
        return mgmtTree;
    }

    public static void setMgmtTree(MgmtTree mgmtTree) {
        DevConfig.mgmtTree = mgmtTree;
    }

}
