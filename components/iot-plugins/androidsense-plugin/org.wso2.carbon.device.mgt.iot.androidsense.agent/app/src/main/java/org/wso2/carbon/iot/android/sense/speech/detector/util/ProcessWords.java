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

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.EditText;
import org.apache.commons.codec.language.Soundex;
import org.wso2.carbon.iot.android.sense.event.streams.Location.LocationData;
import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.tartarus.snowball.ext.englishStemmer;
import agent.sense.android.iot.carbon.wso2.org.wso2_senseagent.R;

/**
 * This class process the words form required words with the recongnized words to check whether it matches with the
 * certain threshold.
 */
public class ProcessWords extends AsyncTask<String, Void, String> {
    private static volatile double threshold = 80;
    private static volatile Map<String, WordData> wordDataMap = new ConcurrentHashMap<>();
    private static String sessionId = "default";
    private static Soundex soundex = new Soundex();
    Activity activity;

    public ProcessWords(Activity activity) {
        this.activity = activity;
    }

    /**
     * Add the list of words which are used for reference.
     * @param wordlist that needs to be looked upon in the speech
     */
    public static void addWords(List<String> wordlist) {
        for (String word : wordlist) {
            if (!wordDataMap.keySet().contains(word) && !word.isEmpty()) {
                wordDataMap.put(word, new WordData(sessionId, word, 0));
            }
        }
    }

    /**
     * Process the recognized word list.
     * @param voiceCommands word lists.
     */
    private void processTexts(String... voiceCommands) {
        for (String requiredWord : wordDataMap.keySet()) {
            int maxOccurunce = 0;
            for (String command : voiceCommands) {
                int occurence = 0;
                for (String word : command.split(" ")) {
                    if (StringSimilarity.similarity(requiredWord, word) > threshold) {
                        occurence++;
                        continue;
                    }
                    if (soundex.encode(requiredWord).equals(soundex.encode(word))) {
                        occurence++;
                        continue;
                    }
                    if (StringSimilarity.similarity(requiredWord, stem(word)) > threshold) {
                        occurence++;
                        continue;
                    }
                }
                if (maxOccurunce < occurence) {
                    maxOccurunce = occurence;
                }
            }
            if (maxOccurunce > 0) {
                WordData wordData = wordDataMap.get(requiredWord);
                wordData.addOccurences(maxOccurunce);
                wordDataMap.put(requiredWord, wordData);
            }
        }
    }

    /**
     * Check for distance between the referenced and recognized words.
     * @param params
     * @return
     */
    @Override
    protected String doInBackground(String... params) {
        processTexts(params);
        publishProgress();
        return "";
    }

    /**
     * update it in the UI.
     * @param values words list.
     */
    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        EditText content = (EditText) activity.findViewById(R.id.command);
        String text = "";
        for (String key : ProcessWords.wordDataMap.keySet()) {
            text = text + key + " - " + ProcessWords.wordDataMap.get(key).getOccurences() + "\n";
        }
        content.setText(text);
        EditText thresholdText = (EditText) activity.findViewById(R.id.editThreashold);
        thresholdText.setText("" + threshold);
    }

    /**
     * set the threshold to determine the distance.
     * @param threshold to determine the distance.
     */
    public static synchronized void setThreshold(int threshold) {
        ProcessWords.threshold = threshold;
    }

    /**
     *
     * @param sessionId for each listening session.
     */
    public static synchronized void setSessionId(String sessionId) {
        ProcessWords.sessionId = sessionId;
    }

    /**
     *
     * retrieve sessionId for each listening session.
     */
    public static synchronized String getSessionId() {
        return ProcessWords.sessionId;
    }

    /**
     * @param word that is used for refrerence.
     */
    public static synchronized void addWord(String word) {
        if (!wordDataMap.keySet().contains(word) && !word.isEmpty()) {
            wordDataMap.put(word, new WordData(sessionId, word, 0));
        }
    }

    /**
     *
     * @param word that needs to be removed from the reference list.
     */
    public static synchronized void removeWord(String word) {
        cleanAndPushToWordMap();
        wordDataMap.remove(word);
    }

    /**
     * clean in memory content and publish it to the server.
     */
    public static synchronized void cleanAndPushToWordMap() {
        for (String word : wordDataMap.keySet()) {
            WordData wordData = wordDataMap.get(word);
            SenseDataHolder.getWordDataHolder().add(wordData);
            wordDataMap.put(word, new WordData(sessionId, word, 0));
        }
    }

    /**
     * apply porter stem algorithm
     * @param word to be stemmed.
     * @return
     */
    private static String stem(String word)
    {
        englishStemmer stemmer = new englishStemmer();
        stemmer.setCurrent(word);
        boolean result = stemmer.stem();
        if (!result)
        {
            return word;
        }
        return stemmer.getCurrent();
    }


}
