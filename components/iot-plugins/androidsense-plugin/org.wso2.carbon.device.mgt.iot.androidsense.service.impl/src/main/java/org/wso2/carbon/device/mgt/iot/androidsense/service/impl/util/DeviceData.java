package org.wso2.carbon.device.mgt.iot.androidsense.service.impl.util;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceData {
    @XmlElement(required = true) public String owner;
    @XmlElement(required = true) public String deviceId;
    @XmlElement public SensorData[] values;
}
