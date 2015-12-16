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
    
    client.fastrprint(F("GET "));
    client.fastrprint(SERVICE_EPOINT);
    client.fastrprint(F("readcontrols/"));
    client.fastrprint(DEVICE_ID);
    client.fastrprint(F("?owner="));
    client.fastrprint(DEVICE_OWNER);
    client.fastrprint(F(" HTTP/1.1")); client.fastrprint(F("\n"));
    client.fastrprint(host.c_str()); client.fastrprint(F("\n"));
    client.println();
    
    delay(1000);
    
    
    while (client.available()) {
        char response = client.read();
        responseMsg += response;
        
    }
    int index = responseMsg.lastIndexOf(":");
    int newLine = responseMsg.lastIndexOf("\n");
    subStrn = responseMsg.substring(index + 1);
    responseMsg = responseMsg.substring(newLine + 1, index);
    if(DEBUG) {
        Serial.print(responseMsg);
        Serial.println();
        Serial.println("-------------------------------");
    }
    
    if (subStrn.equals("ON")) {
        Serial.println("ITS ON");
        //digitalWrite(13, HIGH);
        digitalWrite(6, HIGH);
    } else if (subStrn.equals("OFF")){
        
        Serial.println("ITS OFF");
        //digitalWrite(13, LOW);
        digitalWrite(6, LOW);
        
    }
    
}



