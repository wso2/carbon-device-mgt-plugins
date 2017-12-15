/*
 * Copyright (c) 2016, WSO2 Inc. (http:www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mdm.services.android.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
@ApiModel(value = "DeviceLock",
        description = "This class carries all information related to device lock operation.")
public class DeviceLock extends AndroidOperation implements Serializable {

    @ApiModelProperty(name = "message", value = "Pop up message of the lock operation.", required = false)
    private String message;
    @ApiModelProperty(name = "isHardLockEnabled", value = "Hard lock enable status of the Device", required = true)
    private boolean isHardLockEnabled;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isHardLockEnabled() {
        return isHardLockEnabled;
    }

    public void setHardLockEnabled(boolean hardLockEnabled) {
        isHardLockEnabled = hardLockEnabled;
    }
}
