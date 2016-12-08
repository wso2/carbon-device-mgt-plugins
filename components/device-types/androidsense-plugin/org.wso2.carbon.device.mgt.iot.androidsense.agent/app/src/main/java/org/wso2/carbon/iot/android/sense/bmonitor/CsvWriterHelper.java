/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.iot.android.sense.bmonitor;

public class CsvWriterHelper {
    private static final String QUOTE = "\"";

    public static String addStuff(final Integer text) {
        return QUOTE + text + QUOTE + ",";
    }

    public static String addStuff(final Long text) {
        return QUOTE + text + QUOTE + ",";
    }

    public static String addStuff(final boolean value) {
        return QUOTE + value + QUOTE + ",";
    }

    public static String addStuff(String text) {
        if (text == null) {
            text = "<blank>";
        }
        text = text.replace(QUOTE, "'");

        return QUOTE + text.trim() + QUOTE + ",";
    }
}