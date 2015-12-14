/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.droneanalyzer.service.trasformer;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.wso2.carbon.device.mgt.iot.droneanalyzer.plugin.constants.MessageConfig;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class MessageTransformer {

    private Log log = LogFactory.getLog(MessageTransformer.class);

    private Queue<String> sharedQueue;
    private String current_message = "";
    ObjectMapper mapper;
    boolean isOld = true;
    private String inbound_message_format = "{\"quatanium_val\":[%d, %d, %d, %d],\"accelerometer\":[%d, %d, %d]," +
            "\"gyroscope\":[%d, %d, %d],\"magnetometer\":[%d, %d, %d],\"basicParam\":[%d, %d, %d]}";
    private String outbound_message_format = "{\"quatanium_val\":[%f, %f, %f, %f],\"accelerometer\":[%f, %f, %f]," +
            "\"gyroscope\":[%f, %f, %f],\"magnetometer\":[%f, %f, %f],\"basicParam\":[%f, %f, %f]}";

    public MessageTransformer(){
        mapper = new ObjectMapper();
        sharedQueue = new LinkedList<String>();
    }

    public void messageTranslater(String inbound_message){
        JsonNode actualObj = null;
        String outbound_message;
        try {
            actualObj = mapper.readTree(inbound_message);
            ArrayNode quatannim_vals = (ArrayNode) actualObj.path(MessageConfig.IN_QUATANNIM_VAL);
            ArrayNode accelerometer_vals = (ArrayNode) actualObj.path(MessageConfig.IN_ACCELETOMETER_VAL);
            ArrayNode gyroscope_vals = (ArrayNode) actualObj.path(MessageConfig.IN_GYROSCOPE_VAL);
            ArrayNode magnetometer_vals = (ArrayNode) actualObj.path(MessageConfig.IN_GYROSCOPE_VAL);
            ArrayNode basicParam_vals = (ArrayNode) actualObj.path(MessageConfig.IN_BASIC_PARAM_VAL);
            outbound_message = String.format(outbound_message_format, sTd(quatannim_vals.get(0)), sTd(quatannim_vals.get(1)),
                    sTd(quatannim_vals.get(2)), sTd(quatannim_vals.get(3)), sTd(accelerometer_vals.get(0)),
                    sTd(accelerometer_vals.get(1)), sTd(accelerometer_vals.get(2)), sTd(gyroscope_vals.get(0)),
                    sTd(gyroscope_vals.get(1)), sTd(gyroscope_vals.get(2)), sTd(magnetometer_vals.get(0)),
                    sTd(magnetometer_vals.get(1)), sTd(magnetometer_vals.get(2)), sTd(basicParam_vals.get(0)),
                    sTd(basicParam_vals.get(1)), sTd(basicParam_vals.get(2)));

            synchronized(current_message){
                isOld = false;
                current_message = outbound_message;
            }
            sharedQueue.add(outbound_message);
        } catch (IOException e) {
            log.error(e.getMessage()+",\n"+ e);
        }catch (Exception e) {
            log.error(e.getMessage()+",\n"+ e);
        }
    }

    private double sTd(JsonNode s)
    {
        return Double.parseDouble(s.toString());
    }

    public String getMessage() {
        if(sharedQueue.isEmpty()){
            return null;
        }
        else{
            return sharedQueue.remove();
        }
    }

    public String getCurrentMessage(){
        synchronized(current_message) {
            current_message = isOld == false ? current_message : null;
            isOld = true;
        }
        return current_message;
    }

    public boolean isCurrentMessageEmpty()
    {
            return isOld;
    }

    public boolean isEmptyQueue(){
        return sharedQueue.isEmpty();
    }

}
