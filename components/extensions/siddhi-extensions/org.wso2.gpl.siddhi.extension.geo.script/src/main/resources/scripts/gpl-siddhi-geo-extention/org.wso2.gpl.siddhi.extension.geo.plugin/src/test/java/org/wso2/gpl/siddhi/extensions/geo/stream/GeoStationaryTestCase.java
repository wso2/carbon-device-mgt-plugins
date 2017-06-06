/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.gpl.siddhi.extensions.geo.stream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.gpl.siddhi.extensions.geo.GeoTestCase;

public class GeoStationaryTestCase extends GeoTestCase {
    private static Logger logger = Logger.getLogger(GeoStationaryTestCase.class);
    @Test
     public void testStationary() throws Exception {
        logger.info("TestStationary");

        data.clear();
        expectedResult.clear();
        eventCount = 0;

        data.add(new Object[]{"km-4354", 0d,  0d});
        data.add(new Object[]{"km-4354", 1d,  1d});
        data.add(new Object[]{"km-4354", 1d, 1.5d});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", 1d, 1.75d});
        data.add(new Object[]{"km-4354", 1d, 2.5d});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", 1d, 2.3d});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", 1d, 2.2d});
        data.add(new Object[]{"km-4354", 1d, 2.6d});
        data.add(new Object[]{"km-4354", 1d, 3.6d});
        expectedResult.add(false);

        String executionPlan = "@config(async = 'true') define stream dataIn (id string, longitude double, latitude double);"
                + "@info(name = 'query1') from dataIn#geo:stationary(id,longitude,latitude, 110574.61087757687) " +
                "select stationary \n" +
                "insert into dataOut";

        long start = System.currentTimeMillis();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        long end = System.currentTimeMillis();
        logger.info(String.format("Time to create ExecutionPlanRunTime: [%f sec]", ((end - start) / 1000f)));
        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                for (Event event : inEvents) {
                    Boolean isWithin = (Boolean) event.getData(0);
                    Assert.assertEquals(expectedResult.get(eventCount++), isWithin);
                }
            }
        });
        executionPlanRuntime.start();
        generateEvents(executionPlanRuntime);
        Thread.sleep(1000);
        Assert.assertEquals(expectedResult.size(), eventCount);
    }
}