package org.wso2.carbon.iot.android.sense.data.publisher;

import org.json.JSONException;
import org.json.JSONObject;

public class Event {

    private String owner;
    private String deviceId;
    private String type;
    private float battery;
    private double gps[]; //lat,long
    private float accelerometer[]; //x,y,z
    private float magnetic[]; //x,y,z
    private float gyroscope[]; //x,y,z
    private float light;
    private float pressure;
    private float proximity;
    private float gravity[];
    private float rotation[];
    private String wordSessionId;
    private String word;
    private String wordStatus;
    private long timestamp;

    private float getBattery() {
        return battery;
    }

    public void setBattery(float battery) {
        this.type = "battery";
        this.battery = battery;
    }

    private double[] getGps() {
        return gps != null ? gps : new double[]{0, 0};
    }

    public void setGps(double[] gps) {
        this.type = "gps";
        this.gps = gps;
    }

    private float[] getAccelerometer() {
        return accelerometer != null ? accelerometer : new float[]{0, 0, 0};
    }

    public void setAccelerometer(float[] accelerometer) {
        this.type = "accelerometer";
        this.accelerometer = accelerometer;
    }

    private float[] getMagnetic() {
        return magnetic != null ? magnetic : new float[]{0, 0, 0};
    }

    public void setMagnetic(float[] magnetic) {
        this.type = "magnetic";
        this.magnetic = magnetic;
    }

    private float[] getGyroscope() {
        return gyroscope != null ? gyroscope : new float[]{0, 0, 0};
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
        return gravity != null ? gravity : new float[]{0, 0, 0};
    }

    public void setGravity(float gravity[]) {
        this.type = "gravity";
        this.gravity = gravity;
    }

    private float[] getRotation() {
        return rotation != null ? rotation : new float[]{0, 0, 0};
    }

    public void setRotation(float rotation[]) {
        this.type = "rotation";
        this.rotation = rotation;
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

    public JSONObject getEvent() throws JSONException {
        JSONObject jsonEvent = new JSONObject();
        JSONObject jsonMetaData = new JSONObject();
        jsonMetaData.put("owner", getOwner());
        jsonMetaData.put("deviceId", getDeviceId());
        jsonMetaData.put("type", type);
        jsonMetaData.put("timestamp", getTimestamp());
        jsonEvent.put("metaData", jsonMetaData);

        JSONObject jsonPayloadData = new JSONObject();
        jsonPayloadData.put("battery", getBattery());
        //gps
        double gpsEvents[] = getGps();
        jsonPayloadData.put("gps_lat", gpsEvents[0]);
        jsonPayloadData.put("gps_long", gpsEvents[1]);
        //acceleromter
        float events[] = getAccelerometer();
        jsonPayloadData.put("accelerometer_x", events[0]);
        jsonPayloadData.put("accelerometer_y", events[1]);
        jsonPayloadData.put("accelerometer_z", events[2]);
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

        jsonEvent.put("payloadData", jsonPayloadData);

        return jsonEvent;
    }

}
