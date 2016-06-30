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

package org.wso2.carbon.iot.android.sense.siddhi.eventprocessor.core;

import org.wso2.carbon.iot.android.sense.siddhi.dto.BLE;
import org.wso2.carbon.iot.android.sense.siddhi.eventprocessor.wrapper.SidhdhiWrapper;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class SidhdhiQueryExecutor implements Runnable {

    protected String query = null;
    private static SiddhiManager siddhiManager = new SiddhiManager();
    private static final int INTERVAL = 3000;

    public volatile Thread sidhdiThread;
    private ReadWriteLock rwlock = new ReentrantReadWriteLock();

    //TODO have another array initialized so that a list of callbacks can be stored, String[] callbackList
    public SidhdhiQueryExecutor(String query){
        this.query = query;
    }

    public void run(){
        //TODO the array of callbacks needs to be passed to invoke
        //TODO what is retruned should be a map of callbacks and outputs
        ExecutionPlan executionPlan = new ExecutionPlan().invoke();

        Thread thisThread = Thread.currentThread();

        while (sidhdiThread == thisThread) {
            InputHandler inputHandler = executionPlan.getInputHandler();

            //Sending events to Siddhi
            try {
                List<BLE> bleReadings = read();
                for(BLE ble : bleReadings){
                    System.out.println("Publishing data...");
                    inputHandler.send(new Object[]{ble.getId(), ble.getTimeStamp(), ble.getLocation()});
                }

                thisThread.sleep(INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public List<BLE> read()
    {
        List<BLE> bleData;
        rwlock.readLock().lock();
        try {
            //TODO Reading BLE VALUES
            bleData = SidhdhiWrapper.getBleData();
        } finally {
            rwlock.readLock().unlock();
        }
        return bleData;
    }

    public void stop(){
        sidhdiThread = null;
    }

    public void start(){
        sidhdiThread = new Thread(this);
        sidhdiThread.start();
    }

     private class ExecutionPlan {

        private InputHandler inputHandler;

        public InputHandler getInputHandler() {
            return inputHandler;
        }

        //TODO should expect an array of callbacks
        public ExecutionPlan invoke() {

            //Generating runtime
            ExecutionPlanRuntime runtime  = siddhiManager.createExecutionPlanRuntime(query);



            //TODO logic needs to be revised so that array of callbacks are processed
            runtime.addCallback("dataOut", new StreamCallback() {
                @Override
                public void receive(Event[] events) {
                    System.out.println("Location Match event initiated.");
                    if (events.length > 0) {
                        //TODO Configure Event here!
                        System.out.println("Firing location match event...");
                    }
                }
            });


            //Retrieving InputHandler to push events into Siddhi
            inputHandler = runtime.getInputHandler("dataIn");

            //Starting event processing
            runtime.start();
            System.out.println("Execution Plan Started!");
            return this;
        }
    }

}
