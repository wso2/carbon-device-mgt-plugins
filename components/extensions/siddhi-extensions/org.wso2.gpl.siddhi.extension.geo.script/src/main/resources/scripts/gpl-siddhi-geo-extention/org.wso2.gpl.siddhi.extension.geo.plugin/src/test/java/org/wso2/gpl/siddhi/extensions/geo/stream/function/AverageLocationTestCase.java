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

package org.wso2.gpl.siddhi.extensions.geo.stream.function;

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


public class AverageLocationTestCase {
    static final Logger log = Logger.getLogger(AverageLocationTestCase.class);
    private volatile int count;
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count = 0;
        eventArrived = false;
    }

    @Test
    public void testAverageLocationWithSameWeightsTestCase() throws InterruptedException {
        log.info("testAverageLocation with same weight TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(
                "@config(async = 'true') " +
                        "define stream cleanedStream (locationRecorder string, latitude double, longitude double, " +
                        "beaconProximity string, uuid string, weight double, timestamp long); " +
                        "@info(name = 'query1') " +
                        "from cleanedStream#geo:locationApproximate(locationRecorder, latitude, longitude, " +
                        "beaconProximity, uuid, weight, timestamp) " +
                        "select * " +
                        "insert into dataOut;");

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(6.876657000000001, event.getData(7));
                        Assert.assertEquals(79.897648, event.getData(8));
                        eventArrived = true;
                    } else if (count == 2) {
                        Assert.assertEquals(6.797727042508542, event.getData(7));
                        Assert.assertEquals(80.13557409252783, event.getData(8));
                        eventArrived = true;
                    } else if (count == 3) {
                        Assert.assertEquals(6.853572272662002, event.getData(7));
                        Assert.assertEquals(true, 80.34826512892124 == (Double) event.getData(8) || 80.34826512892126 == (Double) event.getData(8));
                        eventArrived = true;
                    } else if (count == 4) {
                        Assert.assertEquals(true, 8.026326160526303 == (Double) event.getData(7) || 8.0263261605263 == (Double) event.getData(7));
                        Assert.assertEquals(80.42794459517538, event.getData(8));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cleanedStream");
        executionPlanRuntime.start();
        //nugegods, ratnapura, nuwara eliya, vavniya --> thbuttegama
        inputHandler.send(new Object[]{"person1", 6.876657, 79.897648, "ENTER", "uuid1", 20.0d, 1452583935l});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"person1", 6.718681, 80.373422, "ENTER", "uuid2", 20.0d, 1452583937l});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"person1", 6.964981, 80.773796, "ENTER", "uuid3", 20.0d, 1452583939l});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"person1", 8.729925, 80.475966, "ENTER", "uuid4", 100.0d, 1452583941l});
        Thread.sleep(100);

        Assert.assertEquals(4, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testAverageLocationWithDifferentWeightsTestCase2() throws InterruptedException {
        log.info("testAverageLocation with different weights TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(
                "@config(async = 'true') " +
                        "define stream cleanedStream (locationRecorder string, latitude double, longitude double, " +
                        "beaconProximity string, uuid string, weight double, timestamp long); " +
                        "@info(name = 'query1') " +
                        "from cleanedStream#geo:locationApproximate(locationRecorder, latitude, longitude, " +
                        "beaconProximity, uuid, weight, timestamp) " +
                        "select * " +
                        "insert into dataOut;");

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count++;
                    if (count == 1) {
                        Assert.assertEquals(6.876657000000001, event.getData(7));
                        Assert.assertEquals(79.897648, event.getData(8));
                        eventArrived = true;
                    } else if (count == 2) {
                        Assert.assertEquals(6.797727042508542, event.getData(7));
                        Assert.assertEquals(80.13557409252783, event.getData(8));
                        eventArrived = true;
                    } else if (count == 3) {
                        Assert.assertEquals(6.853572272662002, event.getData(7));
                        Assert.assertEquals(true, 80.34826512892124 == (Double) event.getData(8) || 80.34826512892126 == (Double) event.getData(8));
                        eventArrived = true;
                    } else if (count == 4) {
                        Assert.assertEquals(7.322639705655454, event.getData(7));
                        Assert.assertEquals(80.38008364787895, event.getData(8));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cleanedStream");
        executionPlanRuntime.start();
        //nugegods, ratnapura, nuwara eliya, vavniya --> kegalle
        inputHandler.send(new Object[]{"person1", 6.876657, 79.897648, "ENTER", "uuid1", 20.0d, 1452583935l});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"person1", 6.718681, 80.373422, "ENTER", "uuid2", 20.0d, 1452583937l});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"person1", 6.964981, 80.773796, "ENTER", "uuid3", 20.0d, 1452583939l});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"person1", 8.729925, 80.475966, "ENTER", "uuid4", 20.0d, 1452583941l});
        Thread.sleep(100);

        Assert.assertEquals(4, count);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
}
