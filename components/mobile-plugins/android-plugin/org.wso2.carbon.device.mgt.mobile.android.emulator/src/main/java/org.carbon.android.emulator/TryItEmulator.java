/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.carbon.android.emulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * This class starts the Emulator with the name ID specified and log the output to emulator.log.
 */
public class TryItEmulator implements Runnable {
    private String deviceId;                    // name of the AVD to start
    private String emulatorLocation;            // location of the executable file emulator

    TryItEmulator(String id, String emulator) {
        deviceId = id;
        emulatorLocation = emulator;
    }

    public void run() {
        String readLine;
        BufferedReader reader = null;
        Writer writer = null;
        ProcessBuilder processBuilder = new ProcessBuilder(emulatorLocation, "-avd", deviceId);
        try {
            Process process = processBuilder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
                    StandardCharsets.UTF_8));
            writer = new OutputStreamWriter(new FileOutputStream(new File("emulator.log")),
                    StandardCharsets.UTF_8);
            while ((readLine = reader.readLine()) != null) {
                writer.append(readLine);
                writer.append(readLine);
            }
        } catch (IOException e) {
            System.out.println("Error in starting " + deviceId);
            e.printStackTrace();
        } finally {
                try {
                    if (reader != null) reader.close();
                    if (writer != null) writer.close();
                } catch (IOException ignored) {
                    //
            }
        }
    }
}
