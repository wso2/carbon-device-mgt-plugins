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
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.gpl.siddhi.extensions.geo.GeoTestCase;

public class GeoWithinTestCase extends GeoTestCase {
    private static Logger logger = Logger.getLogger(GeoWithinTestCase.class);

    private volatile int count;
    private volatile boolean eventArrived;

    @Test
    public void testPoint() throws Exception {
        logger.info("TestPoint");

        data.clear();
        expectedResult.clear();
        eventCount = 0;
        data.add(new Object[]{"km-4354", 0.5d, 0.5d});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", 2d, 2d});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", -0.5d, 1.5d});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", 0.5d, 1.25d});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", 0.75d, 0.5d});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", 3.5d, 0.5d});
        expectedResult.add(false);

        String executionPlan = "@config(async = 'true') define stream dataIn (id string, longitude double, latitude double);"
                + "@info(name = 'query1') from dataIn " +
                "select geo:within(longitude,latitude,\"{'type':'Polygon','coordinates':[[[0,0],[0,2],[1,2],[1,0],[0,0]]]}\") as notify \n" +
                " \n" +
                "insert into dataOut";

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

    @Test
    public void testPoint2() throws Exception {
        logger.info("TestPoint2");
        data.clear();
        expectedResult.clear();
        eventCount = 0;
        data.add(new Object[]{"km-4354", 0.75d, 1d, "{'type':'Polygon','coordinates':[[[0.5, 0.5],[0.5,1.5],[0.75,1.5],[0.75,0.5],[0.5,0.5]]]}"});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", 1d, 1d, "{'type': 'Circle', 'radius': 110575, 'coordinates':[1.5, 1.5]}"});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", 3d, 3d, "{'type': 'Circle', 'radius': 110575, 'coordinates':[0.5, 1.5]}"});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", 0.6d, 1.0d, "{'type':'MultiPolygon','coordinates':[[[[0.5, 0.5],[0.5,1.5],[0.75,1.5],[0.75,0.5],[0.5,0.5]]], [[[1, 1],[1,2],[2,2],[2,1],[1,1]]]]}"});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", 1.5d, 1.5d, "{'type':'MultiPolygon','coordinates':[[[[0.5, 0.5],[0.5,1.5],[0.75,1.5],[0.75,0.5],[0.5,0.5]]], [[[1, 1],[1,2],[2,2],[2,1],[1,1]]]]}"});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", -0.5d, 1.5d, "{'type':'MultiPolygon','coordinates':[[[[0.5, 0.5],[0.5,1.5],[0.75,1.5],[0.75,0.5],[0.5,0.5]]], [[[1, 1],[1,2],[2,2],[2,1],[1,1]]]]}"});
        expectedResult.add(false);

        String executionPlan = "@config(async = 'true') define stream dataIn (id string, longitude double, latitude double, geometry string);"
                + "@info(name = 'query1') from dataIn " +
                "select geo:within(longitude, latitude, geometry) as notify \n" +
                " \n" +
                "insert into dataOut";

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

    @Test
    public void testGeometry() throws Exception {
        logger.info("TestGeometry");

        data.clear();
        expectedResult.clear();
        eventCount = 0;
        data.add(new Object[]{"km-4354", "{'type':'Polygon','coordinates':[[[0.5, 0.5],[0.5,1.5],[0.75,1.5],[0.75,0.5],[0.5,0.5]]]}"});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", "{'type': 'Circle', 'radius': 110575, 'coordinates':[1.5, 1.5]}"});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", "{'type': 'Circle', 'radius': 110575, 'coordinates':[0.5, 1.5]}"});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", "{'type':'Point', 'coordinates':[-1,1]}"});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", "{'type':'MultiPolygon','coordinates':[[[[0.5, 0.5],[0.5,1.5],[0.75,1.5],[0.75,0.5],[0.5,0.5]]], [[[1, 1],[1,2],[2,2],[2,1],[1,1]]]]}"});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", "{'type':'Point', 'coordinates':[1,1]}"});
        expectedResult.add(true);

        String executionPlan = "@config(async = 'true') define stream dataIn (id string, geometry string);"
                + "@info(name = 'query1') from dataIn " +
                "select geo:within(geometry,\"{'type':'Polygon','coordinates':[[[0,0],[0,4],[3,4],[3,0],[0,0]]]}\") as notify \n" +
                " \n" +
                "insert into dataOut";

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

    @Test
    public void testGeometry2() throws Exception {
        logger.info("TestGeometry");

        data.clear();
        expectedResult.clear();
        eventCount = 0;
        data.add(new Object[]{"km-4354", "{'type':'Point', 'coordinates':[1.0, 1.5]}", "{'type':'Polygon','coordinates':[[[0.5, 0.5],[0.5,1.5],[0.75,1.5],[0.75,0.5],[0.5,0.5]]]}"});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", "{'type':'Point', 'coordinates':[1.5, 1.0]}", "{'type': 'Circle', 'radius': 110575, 'coordinates':[1.5, 1.5]}"});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", "{'type': 'Circle', 'radius': 10, 'coordinates':[0.5, 1.5]}", "{'type': 'Circle', 'radius': 110575, 'coordinates':[0.5, 1.5]}"});
        expectedResult.add(true);
        data.add(new Object[]{"km-4354", "{'type': 'Circle', 'radius': 20, 'coordinates':[0.5, 1.5]}", "{'type': 'Circle', 'radius': 10, 'coordinates':[0.5, 1.5]}"});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", "{'type':'Point', 'coordinates':[-0.5, 1.0]}", "{'type':'MultiPolygon','coordinates':[[[[0.5, 0.5],[0.5,1.5],[0.75,1.5],[0.75,0.5],[0.5,0.5]]], [[[1, 1],[1,2],[2,2],[2,1],[1,1]]]]}"});
        expectedResult.add(false);
        data.add(new Object[]{"km-4354", "{'type':'Polygon','coordinates':[[[0.5, 0.5],[0.5, -0.5],[-0.5, -0.5],[-0.5, 0.5], [0.5, 0.5]]]}", "{'type': 'Circle', 'radius': 110575, 'coordinates':[0, 0]}"});
        expectedResult.add(true);

        String executionPlan = "@config(async = 'true') define stream dataIn (id string, geometry string, otherGeometry string);"
                + "@info(name = 'query1') from dataIn " +
                "select geo:within(geometry,otherGeometry) as notify \n" +
                " \n" +
                "insert into dataOut";

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

    @Test
    public void testGeometry3() throws Exception {
        logger.info("TestGeometryJoin");

        count = 0;
        eventArrived = false;
        String executionPlan = "" +
                "define stream dataIn (id string, latitude double, longitude double); " +
                "define stream dataToTable (id string, geometry string); " +
                "" +
                "define table dataTable (id string, geometry string); " +
                "" +
                "from dataToTable " +
                "insert into dataTable; " +
                "" +
                "@info(name = 'query1') " +
                "from dataIn join  dataTable " +
                "on geo:within(dataIn.latitude, dataIn.longitude, dataTable.geometry) " +
                "select dataIn.id as dataInID, dataTable.id as dataTableID " +
                "insert into dataOut; ";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

        InputHandler dataIn = executionPlanRuntime.getInputHandler("dataIn");
        InputHandler dataToTable = executionPlanRuntime.getInputHandler("dataToTable");

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    eventArrived = true;
                    count++;
                }
            }
        });
        executionPlanRuntime.start();
        Thread.sleep(1000);

        dataToTable.send(new Object[]{"1", "{'type': 'Circle', 'radius': 110575, 'coordinates':[1.5, 1.5]}"});
        dataToTable.send(new Object[]{"2", "{'type': 'Circle', 'radius': 110575, 'coordinates':[12.5, 1.5]}"});
        Thread.sleep(1000);
        dataIn.send(new Object[]{"3", 1.5, 1.0});
        dataIn.send(new Object[]{"4", 7.5, 1.0});
        Thread.sleep(1000);
        executionPlanRuntime.shutdown();
        Assert.assertEquals(1, count);
        Assert.assertEquals(true, eventArrived);
    }
}