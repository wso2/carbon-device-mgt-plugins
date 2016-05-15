package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.util;

import org.wso2.carbon.event.input.adapter.core.InputEventAdapterSubscription;

public class VirtualFirealarmEventAdapterSubscription implements InputEventAdapterSubscription {

    @Override
    public void onEvent(Object o) {
        String msg = (String) o;
        if (msg != null && !msg.isEmpty()) {
            String[] messages = (msg).split(",");
            String deviceId = messages[0];
            String actualMessage = messages[1];
            if (actualMessage.contains("PUBLISHER")) {
                float temperature = Float.parseFloat(actualMessage.split(":")[2]);
                VirtualFireAlarmUtils.publishToDAS(deviceId, temperature);
            } else {
                float temperature = Float.parseFloat(actualMessage.split(":")[1]);
                VirtualFireAlarmUtils.publishToDAS(deviceId, temperature);
            }
        }
    }
}
