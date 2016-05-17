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

package org.wso2.carbon.mdm.mobileservices.windows.common.util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO class to hold the information of authenticated user.
 */
@ApiModel(value = "ApplicationInstallation",
        description = "This class carries all information related to install application")

public class AuthenticationInfo {
    @ApiModelProperty(name = "message", value = "Authentication info message.", required = true)
    private String message;
    @ApiModelProperty(name = "username", value = "Username of the enrolled user.", required = true)
    private String username;
    @ApiModelProperty(name = "tenantDomain", value = "Enrolled user's tenant domain.", required = true)
    private String tenantDomain;
    @ApiModelProperty(name = "tenantId", value = "Enrolled user's tenant ID)", required = true)
    private int tenantId = -1;

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

}
