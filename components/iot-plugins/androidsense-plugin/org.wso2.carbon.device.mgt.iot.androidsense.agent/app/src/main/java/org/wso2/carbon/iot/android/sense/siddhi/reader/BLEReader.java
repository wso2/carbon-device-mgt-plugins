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

package org.wso2.carbon.iot.android.sense.siddhi.reader;

import org.wso2.carbon.iot.android.sense.siddhi.dto.BLE;
import org.wso2.carbon.iot.android.sense.siddhi.eventprocessor.wrapper.SidhdhiWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BLEReader implements Runnable{

    private ReadWriteLock rwlock = new ReentrantReadWriteLock();
    List<BLE> bleData = new ArrayList<BLE>();
    public volatile Thread bleReader;
    private static final int INTERVAL = 2000;

    public void run(){
        Thread thisThread = Thread.currentThread();
        while (bleReader == thisThread) {
            write();
            try {
                thisThread.sleep(INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop(){
        bleReader = null;
    }

    public void start(){
        bleReader = new Thread(this);
        bleReader.start();
    }

    public void write()
    {
        rwlock.writeLock().lock();
        try {
            bleData.add(new BLE(123, "loc_1"));
            bleData.add(new BLE(123, "loc_2"));
            bleData.add(new BLE(123, "loc_3"));
            SidhdhiWrapper.setBleData(bleData);
        } finally {
            rwlock.writeLock().unlock();
        }
    }
}
