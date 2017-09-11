/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mdm.services.android.bean.wrapper;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.mdm.services.android.bean.FileTransfer;

import java.util.List;

/**
 * This class is used to wrap the File Transfer bean with devices.
 */
@ApiModel(value = "FileTransferBeanWrapper",
        description = "FileTransfer related Information.")
public class FileTransferBeanWrapper {

    @ApiModelProperty(name = "deviceIDs", value = "Device id list of the operation to be executed.", required = true)
    private List<String> deviceIDs;

    @ApiModelProperty(name = "upload", value = "This is an inbound file transfer or out bound file transfer respective to the device.", required = true)
    private boolean upload;

    @ApiModelProperty(name = "operation", value = "Information of the File Transfer Operation.", required = true)
    private FileTransfer operation;

    public List<String> getDeviceIDs() {
        return deviceIDs;
    }

    public void setDeviceIDs(List<String> deviceIDs) {
        this.deviceIDs = deviceIDs;
    }

    public FileTransfer getOperation() {
        return operation;
    }

    public void setOperation(FileTransfer operation) {
        this.operation = operation;
    }

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }
}
