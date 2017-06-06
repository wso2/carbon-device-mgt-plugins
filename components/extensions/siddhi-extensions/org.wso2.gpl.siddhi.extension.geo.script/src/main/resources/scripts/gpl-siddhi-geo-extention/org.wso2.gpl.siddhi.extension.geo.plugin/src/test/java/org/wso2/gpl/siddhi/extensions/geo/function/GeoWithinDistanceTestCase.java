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

package org.wso2.gpl.siddhi.extensions.geo.function;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.gpl.siddhi.extensions.geo.GeoTestCase;

public class GeoWithinDistanceTestCase extends GeoTestCase {
    private static Logger logger = Logger.getLogger(GeoWithinDistanceTestCase.class);

    @Test
    public void testGeometry() throws Exception {
        logger.info("TestGeometry");

        data.clear();
        expectedResult.clear();
        eventCount = 0;

        data.add(new Object[]{"{'type':'Polygon','coordinates':[[[0.5, 0.5],[0.5, 1.5],[1.5, 1.5],[1.5, 0.5],[0.5, 0.5]]]}"});
        expectedResult.add(true);
        data.add(new Object[]{"{'type':'Circle','coordinates':[-1, -1], 'radius':110575}"});
        expectedResult.add(true);
        data.add(new Object[]{"{'type':'Point','coordinates':[3, 0]}"});
        expectedResult.add(false);
        data.add(new Object[]{"{'type':'Polygon','coordinates':[[[3, 3],[3, 2],[2, 2],[2, 3],[3, 3]]]}"});
        expectedResult.add(false);

        String executionPlan = "@config(async = 'true') define stream dataIn (geometry string);"
                + "@info(name = 'query1') from dataIn" +
                " select geo:withinDistance(geometry, \"{'type':'Polygon','coordinates':[[[0, 0],[0, 1],[1, 1],[1, 0],[0, 0]]]}\", 110574.61087757687) as intersects \n" +
                " insert into dataOut";

        long start = System.currentTimeMillis();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);
        long end = System.currentTimeMillis();
        logger.info(String.format("Time to create ExecutionPlanRunTime: [%f sec]", ((end - start) / 1000f)));
        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                for (Event event : inEvents) {
                    logger.info(event);
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