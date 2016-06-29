/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.iot.android.sense.siddhi.eventprocessor.wrapper;

import org.wso2.carbon.iot.android.sense.siddhi.dto.BLE;
import org.wso2.carbon.iot.android.sense.siddhi.eventprocessor.core.SidhdhiQueryExecutor;
import org.wso2.carbon.iot.android.sense.siddhi.reader.BLEReader;

import java.util.List;
import java.util.Map;


public class SidhdhiWrapper {

    private static List<BLE> bleData;

    public static List<BLE> getBleData() {
        return bleData;
    }

    public static void setBleData(List<BLE> bleData) {
        SidhdhiWrapper.bleData = bleData;
    }

    public static void main(String args[]){
        String query = "@Import('iot.sample.input:1.0.0')\n" +
                "define stream dataIn (id int, timestamp long, location string);\n" +
                "\n" +
                "@Export('iot.sample.output:1.0.0')\n" +
                "define stream dataOut (action string, timestamp long);\n" +
                "\n" +
                "from every e1=dataIn[location=='loc_1'] -> e2=dataIn[location=='loc_2'] -> e3=dataIn[location=='loc_3']\n" +
                "select 'x' as action, e3.timestamp\n" +
                "insert into dataOut;";

        BLEReader blEReader = new BLEReader();
        blEReader.start();

        SidhdhiQueryExecutor queryExecutor = new SidhdhiQueryExecutor(query);
        queryExecutor.start();
    }

}
