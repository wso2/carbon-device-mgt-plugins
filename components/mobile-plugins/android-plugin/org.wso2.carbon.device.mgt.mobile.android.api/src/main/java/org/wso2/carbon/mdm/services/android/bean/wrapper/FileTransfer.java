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
import org.wso2.carbon.mdm.services.android.bean.AndroidOperation;

import java.io.Serializable;


@ApiModel(value = "FileTransfer",
        //todo
        description = "This class carries all information related to file transfer operation.")
public class FileTransfer extends AndroidOperation implements Serializable {
//todo
    @ApiModelProperty(name = "file-location", value = "FTP URL of file", required = true)
    private String location;

    @ApiModelProperty(name = "ftp-username", value = "FTP User name", required = true)
    private String userName;

    @ApiModelProperty(name = "ftp-password", value = "FTP password", required = true)
    private String password;

    public String getFielLocation() {
        return location;
    }

    public void setFileLocation(String location) {
        this.location = location;
    }

    public String getFtpUserName() {
        return userName;
    }

    public void setFtpUserName(String userName) {
        this.userName = userName;
    }

    public String getFtpPassword() {
        return password;
    }

    public void setFtpPassword(String password) {
        this.password = password;
    }


}
