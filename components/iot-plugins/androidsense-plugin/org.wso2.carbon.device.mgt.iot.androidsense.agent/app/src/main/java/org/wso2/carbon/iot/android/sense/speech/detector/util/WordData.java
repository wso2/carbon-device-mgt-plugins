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
