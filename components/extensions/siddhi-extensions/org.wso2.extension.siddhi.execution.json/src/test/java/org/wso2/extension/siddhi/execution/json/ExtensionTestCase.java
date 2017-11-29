/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.execution.json;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.extension.siddhi.execution.json.test.util.SiddhiTestHelper;

import java.util.concurrent.atomic.AtomicInteger;

public class ExtensionTestCase {
    static final Logger log = Logger.getLogger(ExtensionTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @BeforeClass
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testGetPropertyFunctionExtension() throws InterruptedException {
        log.info("GetPropertyFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (payload string, id string, volume long);";
        String query = ("@info(name = 'query1') from inputStream select id, json:getProperty(payload, 'latitude') "
                + "as latitude insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        Assert.assertEquals("1.5", event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        Assert.assertEquals("67.5", event.getData(1));
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        Assert.assertEquals("7.5", event.getData(1));
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"{'latitude' : 1.5, 'longitude' : 78.5}","IBM",100l});
        inputHandler.send(new Object[]{"{'latitude' : 67.5, 'longitude' : 34.9}","WSO2", 200l});
        inputHandler.send(new Object[]{"{'latitude' : 7.5, 'longitude' : 44.9}", "XYZ", 200l});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        Assert.assertEquals(3, count.get());
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test(dependsOnMethods = {"testGetPropertyFunctionExtension"})
    public void testGetArrayFunctionExtension() throws InterruptedException {
        count.set(0);
        eventArrived = false;
        log.info("GetArrayFunctionExtension TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (arg1 string, arg2 string);";
        String query = ("@info(name = 'query1') from inputStream select json:getArray(arg1, arg2) "
                        + "as array insert into outputStream;");

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        eventArrived = true;
                    }
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"Arg1","Arg2"});
        SiddhiTestHelper.waitForEvents(100, 1, count, 60000);
        Assert.assertEquals(1, count.get());
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
}
