package org.wso2.carbon.mdm.services.android.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "NotifierFrequency",
        description = "This class represents notification frequency configuration.")
public class NotifierFrequency extends AndroidOperation implements Serializable {

    @ApiModelProperty(name = "value", value = "Notification polling frequency", required = true)
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
