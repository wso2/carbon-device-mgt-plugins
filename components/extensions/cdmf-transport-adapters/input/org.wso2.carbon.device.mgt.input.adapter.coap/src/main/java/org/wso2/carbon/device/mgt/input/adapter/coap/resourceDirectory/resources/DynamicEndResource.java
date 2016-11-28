/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.device.mgt.input.adapter.coap.resourceDirectory.resources;

import org.eclipse.californium.tools.resources.RDNodeResource;

public class DynamicEndResource extends EndResource {

	public final DynamicResource.DataType paramType;

	public DynamicEndResource(String name, boolean visible, RDNodeResource parentNode, String resourceCode) {

		this(name, visible, parentNode, resourceCode,getDataType(name));
	}


	public DynamicEndResource(String name, boolean visible, RDNodeResource parentNode,String resourceCode, DynamicResource.DataType paramType) {
		super(getResourceName(name), visible, parentNode,resourceCode);
		this.paramType = paramType;
	}

	public boolean isParamType(String name) {
		if (paramType.equals(DynamicResource.DataType.INTEGER)) {
			if (!name.matches("^-?\\d+$"))
				return false;
		}

		return true;
	}

	public static DynamicResource.DataType getDataType(String queryValue) {
		return DynamicResource.getDataType(queryValue);
	}

	public static String getResourceName(String queryValue) {

		return DynamicResource.getResourceName(queryValue);
	}

}
