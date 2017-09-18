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

package org.wso2.carbon.mdm.services.android.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import java.io.Serializable;

/**
 * This class represents the information of setting up password policy.
 */
@ApiModel(value = "PasscodePolicy", description = "This class represents the information of setting up "
		+ "password policy")
public class PasscodePolicy extends AndroidOperation implements Serializable {

	@ApiModelProperty(name = "maxFailedAttempts", value = "The maximum number of times the end-user can enter"
			+ " his/her device passcode incorrectly. EMM will take different courses of action based on the"
			+ " OS when  the failed attempts exceed the maximum failed attempts.  Android devices will be "
			+ "automatically reset to the original factory settings", required = true)

    @Max(10)
	private int maxFailedAttempts;
	@ApiModelProperty(name = "minLength", value = "The minimum number of alphanumerical values that the "
			+ "end-user can enter as his/her passcode", required = true)
	@Max(15)
	private int minLength;
	@ApiModelProperty(name = "pinHistory", value = "The end-user will not be allowed to reuse a passcode that"
			+ " he/she previously entered until he/she exceeds the set pin history length", required = true)
	@Max(50)
	private int pinHistory;
	@ApiModelProperty(name = "minComplexChars", value = "The minimum number of special characters that the "
			+ "end-user will have to enter in his/her passcode", required = true)
	@Max(5)
	private int minComplexChars;
	@ApiModelProperty(name = "maxPINAgeInDays", value = "The number of days after which the device owner has"
			+ " to change his/her passcode", required = true)
	@Max(730)
	private int maxPINAgeInDays;
	@ApiModelProperty(name = "requireAlphanumeric", value = "Whether or not it is mandatory for the end-user"
			+ " to have a mix of digits and characters in his/her passcode", required = true)
	private boolean requireAlphanumeric;
	@ApiModelProperty(name = "allowSimple", value = "If this field is set to 'true', the device owner will be"
			+ " able to have a simple passcode and the following criteria in the passcode policy will not be"
			+ " applicable:\n"
			+ "Minimum length\n" + "Minimum complex characters", required = true)
	private boolean allowSimple;

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
