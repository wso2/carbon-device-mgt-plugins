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

package org.wso2.carbon.mdm.mobileservices.windows.services.syncml.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Bean for device lockOperationUpdate screen passcode policy.
 */
@ApiModel(value = "PasscodePolicy",
        description = "This class carries all information related to Passcode Policy.")
public class PasscodePolicy extends BasicOperation {

    @ApiModelProperty(name = "maxFailedAttempts", value = "No of Max fail attempts.", required = true)
    private int maxFailedAttempts;
    @ApiModelProperty(name = "minLength", value = "Minimum length of the Passcode.", required = true)
    private int minLength;
    @ApiModelProperty(name = "pinHistory",
            value = "The number of passwords Windows 10 Mobile remembers in the password history.", required = true)
    private int pinHistory;
    @ApiModelProperty(name = "minComplexChars",
            value = "The number of password element types (in other words, uppercase letters, lowercase letters, " +
                    "numbers, or punctuation) required to create strong passwords.", required = true)
    private int minComplexChars;
    @ApiModelProperty(name = "maxPINAgeInDays",
            value = "Number of Maximum days to expire the passcode.", required = true)
    private int maxPINAgeInDays;
    @ApiModelProperty(name = "requireAlphanumeric",
            value = "An integer value that specifies the complexity of the password or PIN allowed.", required = true)
    private boolean requireAlphanumeric;
    @ApiModelProperty(name = "allowSimple", value = "Set boolean value to allow simple password.", required = true)
    private boolean allowSimple;
    @ApiModelProperty(name = "enablePassword", value = "Set boolean value enable password.", required = true)
    private boolean enablePassword;
    @ApiModelProperty(name = "maxInactiveTime", value = "Maximum Inactive time.", required = true)
    private int maxInactiveTime;

    public int getMaxInactiveTime() {
        return maxInactiveTime;
    }

    public void setMaxInactiveTime(int maxInactiveTime) {
        this.maxInactiveTime = maxInactiveTime;
    }

    public boolean isEnablePassword() {
        return enablePassword;
    }

    public void setEnablePassword(boolean enablePassword) {
        this.enablePassword = enablePassword;
    }

    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public void setMaxFailedAttempts(int maxFailedAttempts) {
        this.maxFailedAttempts = maxFailedAttempts;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getPinHistory() {
        return pinHistory;
    }

    public void setPinHistory(int pinHistory) {
        this.pinHistory = pinHistory;
    }

    public int getMinComplexChars() {
        return minComplexChars;
    }

    public void setMinComplexChars(int minComplexChars) {
        this.minComplexChars = minComplexChars;
    }

    public int getMaxPINAgeInDays() {
        return maxPINAgeInDays;
    }

    public void setMaxPINAgeInDays(int maxPINAgeInDays) {
        this.maxPINAgeInDays = maxPINAgeInDays;
    }

    public boolean isRequireAlphanumeric() {
        return requireAlphanumeric;
    }

    public void setRequireAlphanumeric(boolean requireAlphanumeric) {
        this.requireAlphanumeric = requireAlphanumeric;
    }

    public boolean isAllowSimple() {
        return allowSimple;
    }

    public void setAllowSimple(boolean allowSimple) {
        this.allowSimple = allowSimple;
    }
}
