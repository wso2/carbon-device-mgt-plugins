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
package org.wso2.carbon.iot.android.sense.speech.detector;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.wso2.carbon.iot.android.sense.constants.SenseConstants;
import org.wso2.carbon.iot.android.sense.realtimeviewer.ActivitySelectSensor;
import org.wso2.carbon.iot.android.sense.speech.detector.util.ListeningActivity;
import org.wso2.carbon.iot.android.sense.speech.detector.util.ProcessWords;
import org.wso2.carbon.iot.android.sense.speech.detector.util.VoiceRecognitionListener;
import org.wso2.carbon.iot.android.sense.speech.detector.util.WordData;
import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;

import agent.sense.android.iot.carbon.wso2.org.wso2_senseagent.R;

/**
 * This is main activity for word recognition.
 */
public class WordRecognitionActivity extends ListeningActivity {
    Button setThreasholdButton;
    Button addWordButton;
    Button removeWordButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_sense_main);
        context = getApplicationContext(); // Needs to be set

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        VoiceRecognitionListener.getInstance().setListener(this); // Here we set the current listener
        addListenerOnSetThreasholdButton();
        addListenerOnAddWordButton();
        addListenerOnRemoveWordButton();
        String sessionId = getIntent().getStringExtra("sessionId");
        ProcessWords.setSessionId(sessionId);
        FloatingActionButton fbtnSpeechRecongnizer = (FloatingActionButton) findViewById(R.id.sensorChange);
        fbtnSpeechRecongnizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WordData wordData = new WordData(ProcessWords.getSessionId(), SenseConstants.EVENT_LISTENER_FINISHED, 1);
                SenseDataHolder.getWordDataHolder().add(wordData);
                stopListening();
                Intent intent = new Intent(getApplicationContext(), ActivitySelectSensor.class);
                startActivity(intent);
            }
        });
        Long tsLong = System.currentTimeMillis() / 1000;
        WordData wordData = new WordData(sessionId, SenseConstants.EVENT_LISTENER_STARTED, 1);
        SenseDataHolder.getWordDataHolder().add(wordData);
        startListening(); // starts listening
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void processVoiceCommands(String... voiceCommands) {
        if(voiceCommands==null || voiceCommands.length==0){
            return;
        }
        ProcessWords processWords = new ProcessWords(this);
        processWords.execute(voiceCommands);
        restartListeningService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_deEnroll) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addListenerOnSetThreasholdButton() {
        setThreasholdButton = (Button) findViewById(R.id.setThreshold);
        setThreasholdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String thresholdString = ((EditText) findViewById(R.id.editThreashold)).getText().toString();
                try{
                    ProcessWords.setThreshold(Integer.parseInt(thresholdString));
                } catch (NumberFormatException e) {
                    Toast.makeText(WordRecognitionActivity.this, "Invalid Threshold - " + thresholdString, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void addListenerOnAddWordButton() {
        addWordButton = (Button) findViewById(R.id.addWord);
        addWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String word = ((EditText) findViewById(R.id.wordText)).getText().toString();
                ProcessWords.addWord(word);
                Toast.makeText(WordRecognitionActivity.this, word + " is added to the list", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addListenerOnRemoveWordButton() {
        removeWordButton = (Button) findViewById(R.id.removeWord);
        removeWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String word = ((EditText) findViewById(R.id.wordText)).getText().toString();
                Toast.makeText(WordRecognitionActivity.this, word + " is removed from the list", Toast.LENGTH_SHORT).show();
                ProcessWords.removeWord(word);
            }

        });
    }
}
