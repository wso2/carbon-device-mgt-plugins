/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.input.adapter.extension;

/**
 * This is the return type of the ContentValidator.
 */
public class ContentInfo {
	/**
	 * true if the content is valid if not when false then content will not be published.
	 */
	private boolean isValidContent;
	/**
	 * msgText to be returned. eg: if the content is encrypted then we can decrypt the content and then validate and
	 * return it.
	 */
	private Object message;

	public ContentInfo(boolean isValidContent, Object message) {
		this.isValidContent = isValidContent;
		this.message = message;
	}

	public boolean isValidContent() {
		return isValidContent;
	}

	public void setIsValidContent(boolean isValidContent) {
		this.isValidContent = isValidContent;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}
}
