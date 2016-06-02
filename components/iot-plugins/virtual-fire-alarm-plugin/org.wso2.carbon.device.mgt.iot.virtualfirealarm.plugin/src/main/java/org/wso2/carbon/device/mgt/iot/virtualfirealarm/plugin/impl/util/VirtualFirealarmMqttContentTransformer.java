package org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.impl.util;

import org.json.JSONObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.iot.input.adapter.extension.ContentTransformer;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.exception.VirtualFirealarmDeviceMgtPluginException;

import java.security.PublicKey;
import java.util.Map;

public class VirtualFirealarmMqttContentTransformer implements ContentTransformer {

    @Override
    public Object transform(Object message, Map<String, String> dynamicProperties) {
        String topic = dynamicProperties.get("topic");
        String[] topicParams = topic.split("/");
        String tenantDomain = topicParams[0];
        String deviceId = topicParams[2];
        JSONObject jsonPayload = new JSONObject((String) message);
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            ctx.setTenantDomain(tenantDomain, true);
            Long serialNo = (Long) jsonPayload.get(VirtualFireAlarmConstants.JSON_SERIAL_KEY);
            // the hash-code of the deviceId is used as the alias for device certificates during SCEP enrollment.
            // hence, the same is used here to fetch the device-specific-certificate from the key store.
            PublicKey clientPublicKey = VirtualFireAlarmUtils.getDevicePublicKey("" + serialNo.hashCode());

            // the MQTT-messages from VirtualFireAlarm devices are in the form {"Msg":<MESSAGE>, "Sig":<SIGNATURE>}
            String actualMessage = VirtualFireAlarmUtils.extractMessageFromPayload((String) message, clientPublicKey);
            return deviceId + "," + actualMessage;
        } catch (VirtualFirealarmDeviceMgtPluginException e) {
            return "";
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
