/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mdm.services.android.omadm.ddf.util;

import org.wso2.carbon.mdm.services.android.omadm.dm.core.dmtree.beans.MgmtTree;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * This class contains various utilities related to the DDF Framework
 */
public class DDFCommonUtils {

    /**
     * This method generates a Management Tree from a given DDF file
     *
     * @param path - Path to the file
     * @return - Generated Management Tree
     */
    public static MgmtTree generateTree(String path) {
        JAXBContext jaxbContext;
        Unmarshaller jaxbUnmarshaller;
        MgmtTree mgmtTree = null;
        try {
            jaxbContext = JAXBContext.newInstance(MgmtTree.class);
            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            mgmtTree = (MgmtTree) jaxbUnmarshaller.unmarshal(new File(path));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return mgmtTree;
    }

}
