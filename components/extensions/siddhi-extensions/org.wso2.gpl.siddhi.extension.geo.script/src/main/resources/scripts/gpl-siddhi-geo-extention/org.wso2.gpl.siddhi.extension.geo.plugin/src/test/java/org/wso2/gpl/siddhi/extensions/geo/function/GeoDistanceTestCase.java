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

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;


public class GeoDistanceTestCase {
    static final Logger log = Logger.getLogger(GeoDistanceTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testGeoDistanceTestCase() throws InterruptedException {
        log.info("testGeoDistance TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(
                "@config(async = 'true') " +
                "define stream cleanedStream (latitude double, longitude double, prevLatitude double, " +
                                            "prevLongitude double); " +
                "@info(name = 'query1') " +
                "from cleanedStream " +
                "select geo:distance(latitude, longitude, prevLatitude, prevLongitude) as distance " +
                "insert into dataOut;");

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(2322119.848252557, event.getData(0));
                        eventArrived = true;
                    } else if (count == 2) {
                        Assert.assertEquals(871946.8734223971, event.getData(0));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cleanedStream");
        executionPlanRuntime.start();
        //getting distance near equator
        inputHandler.send(new Object[]{8.116553, 77.523679, 9.850047, 98.597177});
        Thread.sleep(500);
        //getting distance away from equator
        inputHandler.send(new Object[]{54.432063, 19.669778, 59.971487, 29.958951});
        Thread.sleep(100);

        Assert.assertEquals(2, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
}
