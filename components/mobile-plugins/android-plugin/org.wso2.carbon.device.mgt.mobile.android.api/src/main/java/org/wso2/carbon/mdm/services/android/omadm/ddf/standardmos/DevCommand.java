package org.wso2.carbon.mdm.services.android.omadm.ddf.standardmos;

import org.wso2.carbon.mdm.services.android.omadm.ddf.util.DDFCommonUtils;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.MgmtTree;

/**
 * A Vendor-specific custom MO designed to represent command operations of a device
 */
public class DevCommand {
    // Path to the DevInfo DDF file
    public static final String DEV_COMMAND_DDF_PATH = "OMA-SUP-MO_DM_DevCommand.xml";

    private static MgmtTree mgmtTree = DDFCommonUtils.generateTree(DEV_COMMAND_DDF_PATH);
    private static DevCommand devCommand = new DevCommand();

    private DevCommand() {
    }

    public static DevCommand getInstance() {
        if (mgmtTree != null) {
            devCommand.setMgmtTree(mgmtTree);
        }
        return devCommand;
    }

    public MgmtTree getMgmtTree() {
        return mgmtTree;
    }

    public static void setMgmtTree(MgmtTree mgmtTree) {
        DevCommand.mgmtTree = mgmtTree;
    }
}
