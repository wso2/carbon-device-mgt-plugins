/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.wso2.carbon.iot.android.sense.speech.detector.util;

/**
 * This defines the data structure of the word data.
 */
public class WordData {
    /**
     * timestamp for all the occurences
     */
    private long timestamp;
    private int occurences;
    private String word;
    private String sessionId;

    public WordData(String sessionId, String word, int occurences) {
        this.timestamp = System.currentTimeMillis() / 1000;
        this.occurences = occurences;
        this.word = word;
        this.sessionId = sessionId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getOccurences() {
        return occurences;
    }

    public String getWord() {
        return word;
    }

    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param occurences for the word and then add the timestamp for each occurences.
     */
    public void addOccurences(int occurences) {
        this.occurences = this.occurences + occurences;
        this.timestamp = System.currentTimeMillis() / 1000;
    }


}
