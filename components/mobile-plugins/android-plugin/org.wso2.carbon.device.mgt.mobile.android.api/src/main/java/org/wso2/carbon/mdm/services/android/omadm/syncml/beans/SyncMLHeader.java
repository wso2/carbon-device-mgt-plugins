/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mdm.services.android.omadm.syncml.beans;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.mdm.services.android.omadm.syncml.util.SyncMLConstants;

/**
 * Represents the header details of a syncml.
 */
public class SyncMLHeader {
    private int sessionId = -1;
    private int MsgID = -1;
    private TargetTag target;
    private SourceTag source;
    private CredentialTag credential;
    private String hexadecimalSessionId;

    public String getHexadecimalSessionId() {
        return hexadecimalSessionId;
    }

    public void setHexadecimalSessionId(String hexSessionId) {
        this.hexadecimalSessionId = hexSessionId;
    }

    public CredentialTag getCredential() {
        return credential;
    }

    public void setCredential(CredentialTag credential) {
        this.credential = credential;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getMsgID() {
        return MsgID;
    }

    public void setMsgID(int msgID) {
        this.MsgID = msgID;
    }

    public TargetTag getTarget() {
        return target;
    }

    public void setTarget(TargetTag target) {
        this.target = target;
    }

    public SourceTag getSource() {
        return source;
    }

    public void setSource(SourceTag source) {
        this.source = source;
    }

    public void buildSyncmlHeaderElement(Document doc, Element rootElement) {
        Element syncHdr = doc.createElement(SyncMLConstants.SYNC_HDR);
        rootElement.appendChild(syncHdr);
        Element verDTD = doc.createElement(SyncMLConstants.VER_DTD);
        verDTD.appendChild(doc.createTextNode(SyncMLConstants.VER_DTD_VALUE));
        syncHdr.appendChild(verDTD);

        Element verProtocol = doc.createElement(SyncMLConstants.VER_PROTOCOL);
        verProtocol.appendChild(doc.createTextNode(SyncMLConstants.VER_PROTOCOL_VALUE));
        syncHdr.appendChild(verProtocol);
        if (getHexadecimalSessionId() != null) {
            Element sessionId = doc.createElement(SyncMLConstants.SESSION_ID);
            sessionId.appendChild(doc.createTextNode(getHexadecimalSessionId()));
            syncHdr.appendChild(sessionId);
        }
        if (getMsgID() != -1) {
            Element msgId = doc.createElement(SyncMLConstants.MESSAGE_ID);
            msgId.appendChild(doc.createTextNode(String.valueOf(getMsgID())));
            syncHdr.appendChild(msgId);
        }
        if (getTarget() != null) {
            getTarget().buildTargetElement(doc, syncHdr);
        }
        if (getSource() != null) {
            getSource().buildSourceElement(doc, syncHdr);
        }
        if (getCredential() != null) {
            getCredential().buildCredentialElement(doc, syncHdr);
        }
    }
}
