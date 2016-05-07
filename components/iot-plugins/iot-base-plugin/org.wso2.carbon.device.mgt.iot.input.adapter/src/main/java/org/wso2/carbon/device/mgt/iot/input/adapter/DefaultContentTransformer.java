package org.wso2.carbon.device.mgt.iot.input.adapter;

import java.util.Map;

/**
 * This holds the default implementation of ContentTransformer
 */
public class DefaultContentTransformer implements ContentTransformer{

    @Override
    public String transform(String message, Map<String, String> dynamicProperties) {
        return message;
    }
}
