package org.wso2.carbon.iot.android.sense.data.publisher;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This hold the definition of the stream that android sense is publishing to.
 */
public class Event {

    private String owner;
    private String deviceId;
    private String type;
    private int battery;
    private double gps[] = new double[]{0, 0}; //lat,long
    private float accelerometer[] = new float[]{0, 0, 0}; //x,y,z
    private float magnetic[] = new float[]{0, 0, 0};; //x,y,z
    private float gyroscope[] = new float[]{0, 0, 0};; //x,y,z
    private int batteryTemperature;
    private int batteryStatus;
    /**
     * LOW or OK
     */
    private String batteryState;
    private float light;
    private float pressure;
    private float proximity;
    private float gravity[] = new float[]{0, 0, 0};;
    private float rotation[] = new float[]{0, 0, 0};;
    private String wordSessionId;
    private String word;
    private String wordStatus;
    private long timestamp;
    private static float speed;
    private String turn;
    public static final float SPEED_LIMIT = 60;
    private int beaconMajor;
    private int beaconMinor;
    private int beaconUuid;
    private String beaconProximity;
    private String callNumber;
    private String callType;
    private long callStartTime;
    private long callEndTime;

    /**
     * State of the phone display.
     * Possible values are:
     * on
     * off
     * unknown
     */
    private String screenState;

    private boolean audioPlaying;
    private boolean headsetOn;
    private int musicVolume;
    private int activityType;
    private int confidence;
    private String smsNumber;
    private String packageName;
    private String action;

    private int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.type = "battery";
        this.battery = battery;
    }

    public int getBatteryTemperature() {
        return batteryTemperature;
    }

    public void setBatteryTemperature(int batteryTemperature) {
        this.type = "battery";  // Type is battery
        this.batteryTemperature = batteryTemperature;
    }

    public int getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(int batteryStatus) {
        this.type = "battery";  // Type is battery
        this.batteryStatus = batteryStatus;
    }

    public String getBatteryState() {
        return batteryState != null ? batteryState : "";
    }

    public void setBatteryState(String batteryState) {
        this.type = "battery";  // Type is battery
        this.batteryState = batteryState;
    }

    private double[] getGps() {
        return gps;
    }

    public void setGps(double[] gps) {
        this.type = "gps";
        this.gps = gps;
    }

    private float[] getAccelerometer() {
        return accelerometer;
    }

    public void setAccelerometer(float[] accelerometer) {
        this.type = "accelerometer";
        this.accelerometer = accelerometer;
    }

    private float[] getMagnetic() {
        return magnetic;
    }

    public void setMagnetic(float[] magnetic) {
        this.type = "magnetic";
        this.magnetic = magnetic;
    }

    private float[] getGyroscope() {
        return gyroscope;
    }

    public void setGyroscope(float[] gyroscope) {
        this.type = "gyroscope";
        this.gyroscope = gyroscope;
    }

    public float getLight() {
        return light;
    }

    public void setLight(float light) {
        this.type = "light";
        this.light = light;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.type = "pressure";
        this.pressure = pressure;
    }

    public float getProximity() {
        return proximity;
    }

    public void setProximity(float proximity) {
        this.type = "proximity";
        this.proximity = proximity;
    }

    private float[] getGravity() {
        return gravity;
    }

    public void setGravity(float gravity[]) {
        this.type = "gravity";
        this.gravity = gravity;
    }

    private float[] getRotation() {
        return rotation;
    }

    public void setRotation(float rotation[]) {
        this.type = "rotation";
        this.rotation = rotation;
    }

    public void setSmsNumber(String smsNumber) {
        this.type = "sms";
        this.smsNumber = smsNumber;
    }

    public String getSmsNumber() {
        return smsNumber;
    }

    private String getWordSessionId() {
        return wordSessionId != null ? wordSessionId : "";
    }

    public void setWordSessionId(String wordSessionId) {
        this.wordSessionId = wordSessionId;
    }

    private String getWord() {
        return word != null ? word : "";
    }

    public void setWord(String word) {
        this.type = "word";
        this.word = word;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.type = "application";
        this.packageName = packageName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.type = "application";
        this.action = action;
    }

    private long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    private String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    private String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getWordStatus() {
        return wordStatus != null ? wordStatus : "";
    }

    public void setWordStatus(String wordStatus) {
        this.wordStatus = wordStatus;
    }

    public void setSpeed(float speed) {
        this.type = "speed";
        this.speed = speed;
    }

    public float getSpeed() {
        this.type = "speed";
        if (Float.isInfinite(speed) || Float.isNaN(speed)) {
            return -1.0f;
        }
        return speed;
    }

    public void setTurns(String turn) {

        this.type = "turn";
        this.turn = turn;
    }

    public String getTurns() {

        if (turn == null || turn.isEmpty() || turn.equals("null")) {
            turn = "No Turns";
        }
        return turn;
    }

    public void setBeaconMajor(int beaconMajor) {
        this.type = "beaconMajor";
        this.beaconMajor = beaconMajor;
    }

    public int getBeaconMajor() {
        this.type = "beaconMajor";
        return beaconMajor;
    }

    public void setBeaconMinor(int beaconMinor) {
        this.type = "beaconMinor";
        this.beaconMinor = beaconMinor;
    }

    public int getBeaconMinor() {
        this.type = "beaconMinor";
        return beaconMinor;
    }

    public void setBeaconUuid(int beaconUuid) {
        this.type = "beaconUuid";
        this.beaconUuid = beaconUuid;
    }

    public int getBeaconUuid() {
        this.type = "beaconUuid";
        return beaconUuid;
    }

    public void setBeaconProximity(String beaconProximity) {
        this.type = "beaconProximity";
        this.beaconProximity = beaconProximity;
    }

    public String getBeaconProximity() {
        return beaconProximity != null ? beaconProximity : "";
    }

    public String getCallNumber() {
        return callNumber != null ? callNumber : "";
    }

    public void setCallNumber(String callNumber) {
        this.type = "call";
        this.callNumber = callNumber;
    }

    public String getCallType() {
        return callType != null ? callType : "";
    }

    public void setCallType(String callType) {
        this.type = "call";
        this.callType = callType;
    }

    public long getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(long callStartTime) {
        this.type = "call";
        this.callStartTime = callStartTime;
    }

    public long getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(long callEndTime) {
        this.type = "call";
        this.callEndTime = callEndTime;
    }

    public String getScreenState() {
        return screenState != null ? screenState : "";
    }

    public void setScreenState(String screenState) {
        this.type = "screen";
        this.screenState = screenState;
    }

    public boolean isAudioPlaying() {
        return audioPlaying;
    }

    public void setAudioPlaying(boolean audioPlaying) {
        this.type = "audio";
        this.audioPlaying = audioPlaying;
    }

    public boolean isHeadsetOn() {
        return headsetOn;
    }

    public void setHeadsetOn(boolean headsetOn) {
        this.type = "audio";
        this.headsetOn = headsetOn;
    }

    public int getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(int musicVolume) {
        this.type = "audio";
        this.musicVolume = musicVolume;
    }

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.type = "activity";
        this.activityType = activityType;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        // Do not set type here since it can be used for various types.
        // However, now it is being used by activity only
        this.confidence = confidence;
    }

    public JSONObject getEvent() throws JSONException {
        JSONObject jsonEvent = new JSONObject();
        JSONObject jsonMetaData = new JSONObject();
        jsonMetaData.put("owner", getOwner());
        jsonMetaData.put("deviceId", getDeviceId());
        jsonMetaData.put("type", type);
        jsonMetaData.put("timestamp", getTimestamp());
        jsonEvent.put("metaData", jsonMetaData);

        JSONObject jsonPayloadData = new JSONObject();
        // battery
        jsonPayloadData.put("battery", getBattery());
        jsonPayloadData.put("battery_state", getBatteryState());
        jsonPayloadData.put("battery_status", getBatteryStatus());
        jsonPayloadData.put("battery_temperature", getBatteryTemperature());


        //gps
        double gpsEvents[] = getGps();
        jsonPayloadData.put("gps_lat", gpsEvents[0]);
        jsonPayloadData.put("gps_long", gpsEvents[1]);
        //accelerometer
        float events[] = getAccelerometer();
        jsonPayloadData.put("accelerometer_x", events[0]);
        jsonPayloadData.put("accelerometer_y", events[1]);
        jsonPayloadData.put("accelerometer_z", events[2]);

        //speed
        //if (getSpeed()>SPEED_LIMIT) {
        jsonPayloadData.put("speed_limit", getSpeed());
        //}

        //Beacon Data
        jsonPayloadData.put("beacon_major", getBeaconMajor());
        jsonPayloadData.put("beacon_minor", getBeaconMinor());
        jsonPayloadData.put("beacon_proximity", getBeaconProximity());
        jsonPayloadData.put("beacon_uuid", getBeaconUuid());

        //turn
        jsonPayloadData.put("turn_way", getTurns());
        //magnetic
        events = getMagnetic();
        jsonPayloadData.put("magnetic_x", events[0]);
        jsonPayloadData.put("magnetic_y", events[1]);
        jsonPayloadData.put("magnetic_z", events[2]);
        //gyroscope
        events = getGyroscope();
        jsonPayloadData.put("gyroscope_x", events[0]);
        jsonPayloadData.put("gyroscope_y", events[1]);
        jsonPayloadData.put("gyroscope_z", events[2]);

        jsonPayloadData.put("light", getLight());
        jsonPayloadData.put("pressure", getPressure());
        jsonPayloadData.put("proximity", getProximity());
        //gravity
        events = getGravity();
        jsonPayloadData.put("gravity_x", events[0]);
        jsonPayloadData.put("gravity_y", events[1]);
        jsonPayloadData.put("gravity_z", events[2]);
        //rotation
        events = getRotation();
        jsonPayloadData.put("rotation_x", events[0]);
        jsonPayloadData.put("rotation_y", events[1]);
        jsonPayloadData.put("rotation_z", events[2]);
        //word
        jsonPayloadData.put("word", getWord());
        jsonPayloadData.put("word_sessionId", getWordSessionId());
        jsonPayloadData.put("word_status", getWordStatus());

        // call
        jsonPayloadData.put("call_number", getCallNumber());
        jsonPayloadData.put("call_type", getCallType());
        jsonPayloadData.put("call_start_time", getCallStartTime());
        jsonPayloadData.put("call_end_time", getCallEndTime());

        // screen
        jsonPayloadData.put("screen_state", getScreenState());

        // headset
        jsonPayloadData.put("audio_playing", isAudioPlaying());
        jsonPayloadData.put("headset_on", isHeadsetOn());
        jsonPayloadData.put("music_volume", getMusicVolume());

        jsonPayloadData.put("activity_type", getActivityType());
        jsonPayloadData.put("confidence", getConfidence());

        jsonPayloadData.put("sms_number", getSmsNumber());

        jsonPayloadData.put("application_name", getPackageName());
        jsonPayloadData.put("action", getAction());


        jsonEvent.put("payloadData", jsonPayloadData);

        return jsonEvent;
    }

}