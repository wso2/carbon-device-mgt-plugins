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

package org.wso2.carbon.device.mgt.iot.input.adapter.http.util;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.device.mgt.iot.input.adapter.extension.ContentInfo;
import org.wso2.carbon.device.mgt.iot.input.adapter.extension.ContentValidator;

import java.util.List;
import java.util.Map;

public class HTTPContentValidator implements ContentValidator {
	private static final Log log = LogFactory.getLog(HTTPContentValidator.class);
	private static String JSON_ARRAY_START_CHAR = "[";
	private static String CDMF_SCOPE_PREFIX = "cdmf";
	private static String CDMF_SCOPE_SEPERATOR = "/";

	@Override
	public ContentInfo validate(Object msgPayload, Map<String, Object> dynamicParams) {
		String deviceId = (String) dynamicParams.get("deviceId");
		String deviceType = (String) dynamicParams.get("deviceType");
		String msg = (String) msgPayload;
		String deviceIdJsonPath = HTTPEventAdapterConstants.DEVICE_ID_JSON_PATH;
		boolean status;
		if (status = isValidDevice(deviceId, deviceType, dynamicParams)) {
			if (msg.startsWith(JSON_ARRAY_START_CHAR)) {
				status = processMultipleEvents(msg, deviceId, deviceIdJsonPath);
			} else {
				status = processSingleEvent(msg, deviceId, deviceIdJsonPath);
			}
		}
		return new ContentInfo(status, msg);
	}

	private boolean processSingleEvent(String msg, String deviceIdFromTopic, String deviceIdJsonPath) {
		Object res = JsonPath.read(msg, deviceIdJsonPath);
		String deviceIdFromContent = (res != null) ? res.toString() : "";
		if (deviceIdFromContent.equals(deviceIdFromTopic)) {
			return true;
		}
		return false;
	}

	private boolean processMultipleEvents(String msg, String deviceIdFromTopic, String deviceIdJsonPath) {
		try {
			JSONParser jsonParser = new JSONParser();
			JSONArray jsonArray = (JSONArray) jsonParser.parse(msg);
			boolean status = false;
			for (int i = 0; i < jsonArray.size(); i++) {
				status = processSingleEvent(jsonArray.get(i).toString(), deviceIdFromTopic, deviceIdJsonPath);
				if (!status) {
					return status;
				}
			}
			return status;
		} catch (ParseException e) {
			log.error("Invalid input " + msg, e);
			return false;
		}
	}

	private boolean isValidDevice(String deviceId, String deviceType, Map<String, Object> dynamicParams) {
		List<String> scopes = (List<String>) dynamicParams.get(HTTPEventAdapterConstants.SCOPE_TAG);
		if (scopes != null) {
			for (String scope : scopes) {
				if (scope.startsWith(CDMF_SCOPE_PREFIX)) {
					String deviceIdInfo[] = scope.split(CDMF_SCOPE_SEPERATOR);
					if (deviceIdInfo.length == 3) {
						if (deviceId.equals(deviceIdInfo[2]) && deviceType.equals(deviceIdInfo[1])) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
