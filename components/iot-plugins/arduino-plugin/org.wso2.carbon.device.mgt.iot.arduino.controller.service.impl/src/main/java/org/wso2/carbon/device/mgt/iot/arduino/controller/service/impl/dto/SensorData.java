package org.wso2.carbon.device.mgt.iot.arduino.controller.service.impl.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
/**
 * This stores sensor event data for the device type.
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
