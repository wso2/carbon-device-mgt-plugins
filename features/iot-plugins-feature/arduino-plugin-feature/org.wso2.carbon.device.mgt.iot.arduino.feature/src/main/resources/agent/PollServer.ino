/**
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
**/

#include "ArduinoBoardSketch.h"
void readControls() {
    //  String responseMsg;

    Serial.println("Started..");
    
    client.fastrprint(F("GET "));
    client.fastrprint(IOT_SERVICE_EPOINT);
    client.fastrprint(F(" HTTP/1.1"));
    client.fastrprint(F("\n"));
    client.fastrprint(host.c_str());client.fastrprint(F("\n")); 
    client.fastrprint(F("Authorization: Bearer ")); client.fastrprint(F(DEVICE_TOKEN)); client.fastrprint(F("\n"));
    client.fastrprint(F("Content-Type: application/json")); client.fastrprint(F("\n"));
    client.fastrprint(F("Accept: application/json")); client.fastrprint(F("\n"));
    client.fastrprint(F("\n"));
    client.fastrprint(F("protocol: HTTP\n"));

    client.println();
    
    if(DEBUG) {
        Serial.print("GET ");
        Serial.print(IOT_SERVICE_EPOINT);
        Serial.print(" HTTP/1.1"); Serial.println();
        Serial.print(host); Serial.println();
        Serial.print("Content-Type: application/json"); Serial.println();
        Serial.println();
    }
    
    delay(1000);
    
    while (client.available()) {
        char response = client.read();
        if(DEBUG) Serial.print(response);
        responseMsg += response;
        
    }
    Serial.println();
    Serial.println("Ended..");
    int index = responseMsg.lastIndexOf(":");
    int newLine = responseMsg.lastIndexOf("\n");
    subStrn = responseMsg.substring(index + 1);
    responseMsg = responseMsg.substring(newLine + 1, index);
    
    if(DEBUG) {
        Serial.print("Polling Response: ");
        Serial.print(responseMsg);
        Serial.println();
        Serial.println("-------------------------------");
    }
    
    if (subStrn.equals("ON")) {
        Serial.println("ITS ON");
        digitalWrite(13, HIGH);
        digitalWrite(6, HIGH);
    } else if (subStrn.equals("OFF")){
        
        Serial.println("ITS OFF");
        digitalWrite(13, LOW);
        digitalWrite(6, LOW);
        
    }
    
}


