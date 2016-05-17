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

package org.wso2.carbon.mdm.mobileservices.windows.services.authbst.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This bean class is for credentials coming from wab page at federated authentication step.
 */
@ApiModel(value = "Credentials", description = "This class carries all information related to Credentials.")
@XmlRootElement(name = "credentials")
@XmlAccessorType(XmlAccessType.FIELD)
public class Credentials {

    @ApiModelProperty(name = "username", value = "User name", required = true)
    @XmlElement(required = true, name = "username")
    private String username;

    @ApiModelProperty(name = "email", value = "Email Address of the user.", required = true)
    @XmlElement(required = true, name = "email")
    private String email;

    @ApiModelProperty(name = "password", value = "password of the user.", required = true)
    @XmlElement(required = true, name = "password")
    private String password;

    @ApiModelProperty(name = "ownership", value = "ownership of the user.(BYOD/COPE).", required = true)
    @XmlElement(required = true, name = "ownership")
    private String ownership;

    @ApiModelProperty(name = "usertoken", value = "User Token.", required = true)
    @XmlElement(required = true, name = "token")
    private String usertoken;

    public Credentials() {

    }

    public String getUsertoken() {
        return usertoken;
    }

    public void setUsertoken(String usertoken) {
        this.usertoken = usertoken;
    }

    public String getOwnership() {
        return ownership;
    }

    public void setOwnership(String ownership) {
        this.ownership = ownership;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
