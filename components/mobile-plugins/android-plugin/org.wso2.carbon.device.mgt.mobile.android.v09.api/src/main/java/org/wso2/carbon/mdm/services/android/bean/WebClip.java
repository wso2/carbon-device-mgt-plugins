/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.io.Serializable;

/**
 * This class represents the information of setting up webclip.
 */
@ApiModel(value = "WebClip", description = "This class represents the information of setting up webclip")
public class WebClip extends AndroidOperation implements Serializable {

	@ApiModelProperty(name = "identity", value = "The URL of the application", required = true)
	private String identity;
	@ApiModelProperty(name = "title", value = "The name of the web application", required = true)
	private String title;
	@ApiModelProperty(name = "type", value = "The type of the operation. Following are the possible operation"
			+ " types: install and uninstall. If the operation type is install, the web clip is added, and "
			+ "if the operation type is uninstall, the existing web clip is removed", required = true)
	private String type;

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
