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

package org.wso2.carbon.device.mgt.input.adapter.extension.transformer;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentTransformer;

import java.util.Map;

/**
 * This holds the default implementation of ContentTransformer
 */
public class MQTTContentTransformer implements ContentTransformer {
    private static final String MQTT_CONTENT_TRANSFORMER = "device-meta-transformer";
    private static final String TOPIC = "topic";
    private static String JSON_ARRAY_START_CHAR = "[";

    private static final Log log = LogFactory.getLog(MQTTContentTransformer.class);

    @Override
    public String getType() {
        return MQTT_CONTENT_TRANSFORMER;
    }

    @Override
    public Object transform(Object messagePayload, Map<String, Object> dynamicProperties) {
        String topic = (String) dynamicProperties.get(TOPIC);
        String topics[] = topic.split("/");
        String deviceId = topics[2];
        String deviceType = topics[1];
        String message = (String) messagePayload;
        try {
            if (message.startsWith(JSON_ARRAY_START_CHAR)) {
                return processMultipleEvents(message, deviceId, deviceType);
            } else {
                return processSingleEvent(message, deviceId, deviceType);
            }
        } catch (ParseException e) {
            log.error("Invalid input " + message, e);
            return false;
        }
    }

    private String processSingleEvent(String msg, String deviceIdFromTopic, String deviceIdJsonPath)
            throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("deviceId", deviceIdFromTopic);
        JSONObject eventObject = new JSONObject();
        eventObject.put("payloadData", parser.parse(msg));
        eventObject.put("metaData", jsonObject);
        JSONObject event = new JSONObject();
        event.put("event", eventObject);
        return event.toJSONString();
    }

    private String processMultipleEvents(String msg, String deviceIdFromTopic, String deviceIdJsonPath)
            throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = (JSONArray) jsonParser.parse(msg);
        JSONArray eventsArray = new JSONArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            eventsArray.add(i, processSingleEvent(jsonArray.get(i).toString(), deviceIdFromTopic, deviceIdJsonPath));
        }
        return eventsArray.toJSONString();
    }
}
