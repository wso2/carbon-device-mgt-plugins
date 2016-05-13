package org.wso2.carbon.iot.android.sense.data.publisher;

import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.iot.android.sense.event.streams.Location.LocationData;
import android.util.Log;


/**
 * This hold the definition of the stream that android sense is publishing to.
 */
public class Event {

    private String owner;
    private String deviceId;
    private String type;
    private int battery;
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

    private static final String TAG = Event.class.getName();
    LocationData  gpsLoc;

    private int getBattery() {
        return this.battery;
    }

    public void setBattery(int battery) {
        this.type = "battery";
        this.battery = battery;
    }

    private double[] getGps() {

        return gps != null ? this.gps : new double[]{gps[0],gps[1]};
    }

    public void setGps(double[] gps) {
        this.type = "gps";
        this.gps = gps;
    }

    private float[] getAccelerometer() {
        return this.accelerometer != null ? this.accelerometer : new float[]{0, 0, 0};
    }

    public void setAccelerometer(float[] accelerometer) {
        this.type = "accelerometer";
        this.accelerometer = accelerometer;
    }

    private float[] getMagnetic() {
        return this.magnetic != null ? this.magnetic : new float[]{0, 0, 0};
    }

    public void setMagnetic(float[] magnetic) {
        this.type = "magnetic";
        this.magnetic = magnetic;
    }

    private float[] getGyroscope() {
        return this.gyroscope != null ? this.gyroscope : new float[]{0, 0, 0};
    }

    public void setGyroscope(float[] gyroscope) {
        this.type = "gyroscope";
        this.gyroscope = gyroscope;
    }

    public float getLight() {
        return this.light;
    }

    public void setLight(float light) {
        this.type = "light";
        this.light = light;
    }

    public float getPressure() {
        return this.pressure;
    }

    public void setPressure(float pressure) {
        this.type = "pressure";
        this.pressure = pressure;
    }

    public float getProximity() {
        return this.proximity;
    }

    public void setProximity(float proximity) {
        this.type = "proximity";
        this.proximity = proximity;
    }

    private float[] getGravity() {
        return this.gravity != null ? this.gravity : new float[]{0, 0, 0};
    }

    public void setGravity(float gravity[]) {
        this.type = "gravity";
        this.gravity = gravity;
    }

    private float[] getRotation() {
        return this.rotation != null ? this.rotation : new float[]{0, 0, 0};
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
        return this.word != null ? this.word : "";
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
        //gps & accelerometer

        if (gps !=null && accelerometer !=null) {
            jsonPayloadData.put("gps_lat", gps[0]);
            jsonPayloadData.put("gps_long", gps[1]);

            jsonPayloadData.put("accelerometer_x", accelerometer[0]);
            jsonPayloadData.put("accelerometer_y", accelerometer[1]);
            jsonPayloadData.put("accelerometer_z", accelerometer[2]);
        }


        //magnetic
        //events = getMagnetic();
        if (magnetic !=null) {
            jsonPayloadData.put("magnetic_x", magnetic[0]);
            jsonPayloadData.put("magnetic_y", magnetic[1]);
            jsonPayloadData.put("magnetic_z", magnetic[2]);
        }
        //gyroscope
        //events = getGyroscope();

        if (gyroscope != null) {
            jsonPayloadData.put("gyroscope_x", gyroscope[0]);
            jsonPayloadData.put("gyroscope_y", gyroscope[1]);
            jsonPayloadData.put("gyroscope_z", gyroscope[2]);
        }


        jsonPayloadData.put("light", getLight());

        jsonPayloadData.put("pressure", getPressure());
        jsonPayloadData.put("proximity", getProximity());
        //gravity
        //events = getGravity();
        if (gravity!=null) {
            jsonPayloadData.put("gravity_x", gravity[0]);
            jsonPayloadData.put("gravity_y", gravity[1]);
            jsonPayloadData.put("gravity_z", gravity[2]);
        }
        //rotation
        //events = getRotation();
        if (rotation!=null) {
            jsonPayloadData.put("rotation_x", rotation[0]);
            jsonPayloadData.put("rotation_y", rotation[1]);
            jsonPayloadData.put("rotation_z", rotation[2]);
        }
        //word
        jsonPayloadData.put("word", getWord());
        jsonPayloadData.put("word_sessionId", getWordSessionId());
        jsonPayloadData.put("word_status", getWordStatus());



        jsonEvent.put("payloadData", jsonPayloadData);

        return jsonEvent;
    }

}
