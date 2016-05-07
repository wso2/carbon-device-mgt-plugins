package org.wso2.carbon.device.mgt.iot.input.adapter;

import java.util.Map;

public interface ContentTransformer {

    String transform(String message, Map<String, String> dynamicProperties);
}
