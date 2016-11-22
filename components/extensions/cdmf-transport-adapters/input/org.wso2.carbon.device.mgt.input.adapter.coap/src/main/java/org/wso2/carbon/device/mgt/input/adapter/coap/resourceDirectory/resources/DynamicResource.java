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

/**
 * A Dynamic Resource is used to describe a changing parameter of a path annotation in a particular path.
 * Eg: <code>devices/{deviceId}/stats</code>
 * here, the <code>{deviceId}</code> is stored as a DynamicResource, which can get any value accoring to it's parameter type.
 */
public class DynamicResource extends TagResource {

	public enum DataType {INTEGER, STRING}

	public final DataType paramType;

	public DynamicResource(String name, boolean visible, RDNodeResource parentNode) {

		this(name, visible, parentNode, getDataType(name));
	}

	public DynamicResource(String name, boolean visible, RDNodeResource parentNode, DataType paramType) {
		super(getResourceName(name), visible, parentNode);
		this.paramType = paramType;
	}

	public boolean isParamType(String name) {
		if (paramType.equals(DataType.INTEGER)) {
			if (!name.matches("^-?\\d+$"))
				return false;
		}

		return true;
	}

	public static DataType getDataType(String queryValue) {
		if (queryValue.contains(";")) {
			String value = queryValue.split(";")[1];
			String type=value.substring(0,value.length()-2);
			try {
				DataType dataType = DataType.valueOf(type);
				return dataType;
			} catch (IllegalArgumentException e) {
				return DataType.STRING;
			}
		} else
			return DataType.STRING;
	}

	public static String getResourceName(String queryValue) {

		if (queryValue.contains(";")) {
			return queryValue.split(";")[0]+"}";
		}
		else
			return queryValue;
	}

}
