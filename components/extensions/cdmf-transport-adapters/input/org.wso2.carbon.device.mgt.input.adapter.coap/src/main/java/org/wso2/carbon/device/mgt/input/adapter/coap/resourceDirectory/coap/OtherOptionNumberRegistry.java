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
package org.wso2.carbon.device.mgt.input.adapter.coap.resourceDirectory.coap;

import org.eclipse.californium.core.coap.OptionNumberRegistry;

public final class OtherOptionNumberRegistry {

	public static final int AUTHORIZATION = 65000;
	public static final int CONTENT_TYPE=65001;

	public static String toString(int optionNumber) {
		switch (optionNumber) {
		case 65000:
			return Names.Authorization;
		case 65001:
			return Names.Content_Type;
		default:
			return String.format("Unknown (%d)", optionNumber);
		}
	}

	public static OptionNumberRegistry.optionFormats getFormatByNr(int optionNumber) {
		switch(optionNumber) {
		case 65000:
		case 65001:
			return OptionNumberRegistry.optionFormats.STRING;
		default:
			return OptionNumberRegistry.optionFormats.UNKNOWN;
		}
	}

	public static int toNumber(String name) {
		if(Names.Authorization.equals(name)) return AUTHORIZATION;
		else if(Names.Content_Type.equals(name)) return CONTENT_TYPE;
		else return -1;
	}
	public static class Names {
		public static final String Content_Type="Content-Type";
		public static final String Authorization="Authorization";

	}

}
