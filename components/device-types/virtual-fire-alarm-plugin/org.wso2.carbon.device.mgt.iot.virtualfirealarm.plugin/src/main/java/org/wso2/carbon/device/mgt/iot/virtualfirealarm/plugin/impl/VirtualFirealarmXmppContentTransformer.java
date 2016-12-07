package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl;

import org.json.JSONObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentTransformer;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.exception.VirtualFirealarmDeviceMgtPluginException;

import java.security.PublicKey;
import java.util.Map;

public class VirtualFirealarmXmppContentTransformer implements ContentTransformer {

    @Override
    public Object transform(Object message, Map<String, Object> dynamicProperties) {
        String from = (String) dynamicProperties.get("from");
        String subject = (String) dynamicProperties.get("subject");

        int indexOfAt = from.indexOf("@");
        int indexOfSlash = from.indexOf("/");

        if (indexOfAt != -1 && indexOfSlash != -1) {
            String deviceId = from.substring(0, indexOfAt);
            JSONObject jsonPayload = new JSONObject((String) message);
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                ctx.setTenantDomain(subject, true);
                Integer serialNo = (Integer) jsonPayload.get(VirtualFireAlarmConstants.JSON_SERIAL_KEY);
                // the hash-code of the deviceId is used as the alias for device certificates during SCEP enrollment.
                // hence, the same is used here to fetch the device-specific-certificate from the key store.
                PublicKey clientPublicKey = VirtualFireAlarmUtils.getDevicePublicKey("" + serialNo);

                // the MQTT-messages from VirtualFireAlarm devices are in the form {"Msg":<MESSAGE>, "Sig":<SIGNATURE>}
                String actualMessage = VirtualFireAlarmUtils.extractMessageFromPayload((String) message,
                                                                                       clientPublicKey);
                return deviceId + "," + actualMessage;
            } catch (VirtualFirealarmDeviceMgtPluginException e) {
                return "";
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return "";
    }
}
