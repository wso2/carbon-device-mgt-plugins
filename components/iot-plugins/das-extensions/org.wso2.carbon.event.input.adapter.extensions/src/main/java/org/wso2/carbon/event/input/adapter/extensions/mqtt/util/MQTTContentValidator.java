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

package org.wso2.carbon.event.input.adapter.extensions.mqtt.util;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adapter.extensions.ContentInfo;
import org.wso2.carbon.event.input.adapter.extensions.ContentValidator;
import org.wso2.carbon.event.input.adapter.extensions.mqtt.Constants;

import java.util.Map;

public class MQTTContentValidator implements ContentValidator {
	private static final Log log = LogFactory.getLog(MQTTContentValidator.class);

	@Override
	public ContentInfo validate(Map<String, String> params) {
		String topic = params.get(Constants.TOPIC);
		String topics[] = topic.split("/");

		String msg = params.get(Constants.PAYLOAD);
		String deviceIdJsonPath = params.get(Constants.DEVICE_ID_JSON_PATH);
		String deviceIdInTopicHierarchyLevel = params.get(Constants.DEVICE_ID_TOPIC_HIERARCHY_INDEX);
		int deviceIdInTopicHierarchyLevelIndex = 0;
		if (deviceIdInTopicHierarchyLevel != null && !deviceIdInTopicHierarchyLevel.isEmpty()) {
			deviceIdInTopicHierarchyLevelIndex = Integer.parseInt(deviceIdInTopicHierarchyLevel);
		}
		String deviceIdFromTopic = topics[deviceIdInTopicHierarchyLevelIndex];
		Object res = JsonPath.read(msg, deviceIdJsonPath);
		String deviceIdFromContent = (res != null) ? res.toString() : "";
		if (deviceIdFromContent.equals(deviceIdFromTopic)) {
			return new ContentInfo(true, msg);
		}
		return new ContentInfo(false, msg);
	}
}
