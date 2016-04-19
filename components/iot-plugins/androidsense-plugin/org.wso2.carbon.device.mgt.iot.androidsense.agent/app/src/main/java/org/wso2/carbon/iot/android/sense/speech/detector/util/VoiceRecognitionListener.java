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

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import org.wso2.carbon.iot.android.sense.speech.detector.IVoiceControl;
import java.util.ArrayList;

/**
 * This triggers android voice recognition listener.
 */
public class VoiceRecognitionListener implements RecognitionListener {
    private static VoiceRecognitionListener instance = null;

    IVoiceControl listener; // This is your running activity (we will initialize it later)

    public static VoiceRecognitionListener getInstance() {
        if (instance == null) {
            instance = new VoiceRecognitionListener();
        }
        return instance;
    }

    private VoiceRecognitionListener() { }

    public void setListener(IVoiceControl listener) {
        this.listener = listener;
    }

    public void processVoiceCommands(String... voiceCommands) {
        listener.processVoiceCommands(voiceCommands);
    }

    // This method will be executed when voice commands were found
    public void onResults(Bundle data) {
        ArrayList<String> matches = data.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String[] commands = new String[matches.size()];
        commands = matches.toArray(commands);
        processVoiceCommands(commands);
    }

    // User starts speaking
    public void onBeginningOfSpeech() {
        System.out.println("Starting to listen");
    }

    public void onBufferReceived(byte[] buffer) { }

    // User finished speaking
    public void onEndOfSpeech() {
        System.out.println("Waiting for result...");
    }

    // If the user said nothing the service will be restarted
    public void onError(int error) {
        if (listener != null) {
            listener.restartListeningService();
        }
    }
    public void onEvent(int eventType, Bundle params) { }

    public void onPartialResults(Bundle partialResults) { }

    public void onReadyForSpeech(Bundle params) { }

    public void onRmsChanged(float rmsdB) { }
}
