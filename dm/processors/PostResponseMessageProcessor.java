package org.wso2.carbon.mdm.services.android.omadm.dm.processors;

import org.wso2.carbon.mdm.services.android.omadm.ddf.MgmtTreeManager;
import org.wso2.carbon.mdm.services.android.omadm.ddf.impl.MgmtTreeManagerImpl;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.MgmtTree;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.Node;
import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.parsers.URIParser;
import org.wso2.carbon.mdm.services.android.omadm.dm.dao.DeviceMODao;
import org.wso2.carbon.mdm.services.android.omadm.syncml.beans.*;
import org.wso2.carbon.mdm.services.android.omadm.syncml.util.SyncMLConstants;
import org.wso2.carbon.mdm.services.android.omadm.syncml.util.SyncMLStatusCodes;
import org.wso2.carbon.mdm.services.android.omadm.util.SyncMLAlertCodeUtils;

import java.util.List;

/**
 * This class handles the update and roll back scenarios of OMADM management
 * sessions. The need of having such an entity is to keep track of the sent Tree
 * manipulation commands and update them according to the response from the other end.
 */
public class PostResponseMessageProcessor {

    private SyncMLDocument responseDocument;
    private SyncMLBody requestBody;
    private String deviceId;

    private DeviceMODao moDao = DeviceMODao.getInstance();

    public PostResponseMessageProcessor(SyncMLDocument responseDocument, SyncMLBody requestBody, String deviceId) {
        this.responseDocument = responseDocument;
        this.requestBody = requestBody;
        this.deviceId = deviceId;
    }

    public void processMessage() {
        commitManagementTreeChanges();
    }

    /**
     * This method updates the Management Tree based on the status codes for the previous request
     */
    private void commitManagementTreeChanges() {
        List<StatusTag> statuses = responseDocument.getBody().getStatus();

        for (StatusTag status : statuses) {
            processStatusBlock(status);
        }
    }

    private void processStatusBlock(StatusTag status) {
        switch (status.getCommand()) {
            case SyncMLConstants.ADD : {
                if (SyncMLAlertCodeUtils.isSuccessCode(status.getData())) {
                    processAddOperation(requestBody.getAdd());
                }
                break;
            }
            case SyncMLConstants.REPLACE : {
                if (SyncMLAlertCodeUtils.isSuccessCode(status.getData())) {
                    processReplaceOperation(requestBody.getReplace());
                }
                break;
            }
            case SyncMLConstants.DELETE : // process delete commands
                break;
            case SyncMLConstants.SEQUENCE : // process sequence command
                break;
            case SyncMLConstants.ATOMIC : // process atomic commands
                break;
        }
    }

    private void processAddOperation(AddTag add) {
        List<ItemTag> items = add.getItems();
        MetaTag commonMeta = null;

        if (add.getMeta() != null) {
            commonMeta = add.getMeta();
        }

        for (ItemTag item : items) {
            String locURI = item.getTarget().getLocURI();
            MgmtTree tree = moDao.getMO(URIParser.getDMTreeName(locURI),
                    responseDocument.getHeader().getSource().getLocURI());

            if (tree != null) {
                Node node = new Node();
                if (commonMeta != null) {
                    if (commonMeta.getFormat() != null) {
                        node.setFormat(commonMeta.getFormat());
                    }
                    if (commonMeta.getSize() != null) {
                        node.setSize(commonMeta.getSize());
                    }
                    if (commonMeta.getType() != null) {
                        node.setType(commonMeta.getType());
                    }
                } else {
                    if (item.getMeta() != null) {
                        if (item.getMeta().getFormat() != null) {
                            node.setFormat(item.getMeta().getFormat());
                        }
                        if (item.getMeta().getSize() != null) {
                            node.setSize(item.getMeta().getFormat());
                        }
                        if (item.getMeta().getType() != null) {
                            node.setType(item.getMeta().getType());
                        }
                    }
                }
                node.setValue(item.getData());
                node.setNodeName(URIParser.getNodeName(locURI));
                MgmtTreeManager treeManager = new MgmtTreeManagerImpl(tree);

                if (!treeManager.isExistingNode(locURI)) {
                    treeManager.addNode(node, locURI);
                }
            }
        }
    }

    private void processReplaceOperation(ReplaceTag replace) {
        List<ItemTag> items = replace.getItems();

        for (ItemTag item : items) {
            String locURI = item.getSource().getLocURI();
            MgmtTree tree = moDao.getMO(URIParser.getDMTreeName(locURI),
                    responseDocument.getHeader().getSource().getLocURI());

            if (tree != null) {
                MgmtTreeManager treeManager = new MgmtTreeManagerImpl(tree);
                treeManager.replaceNodeDetails(locURI, item);
            }
        }

    }

}
