/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.iot.android.sense.event.streams.audio;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import org.wso2.carbon.iot.android.sense.event.streams.DataReader;
import org.wso2.carbon.iot.android.sense.util.SenseDataHolder;

import java.util.Date;

public class AudioDataReader extends DataReader {

    private static final String TAG = AudioDataReader.class.getName();
    private Context context;

    public AudioDataReader(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        Log.d(TAG, "Running AudioDataReader");
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        AudioData audioData = new AudioData();
        audioData.setTimestamp(new Date().getTime());
        audioData.setPlaying(manager.isMusicActive());
        audioData.setHeadsetOn(manager.isWiredHeadsetOn());
        audioData.setMusicVolume(manager.getStreamVolume(AudioManager.STREAM_MUSIC));
        SenseDataHolder.getAudioDataHolder().add(audioData);
    }
}
