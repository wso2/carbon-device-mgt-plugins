package org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
/**
 * This stores sensor event data for android sense.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorData {

    @XmlElement public Long time;
    @XmlElement public String key;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @XmlElement public String value;

}
