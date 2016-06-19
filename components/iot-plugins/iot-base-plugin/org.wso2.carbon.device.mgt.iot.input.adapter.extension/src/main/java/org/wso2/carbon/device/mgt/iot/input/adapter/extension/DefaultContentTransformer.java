package org.wso2.carbon.device.mgt.iot.input.adapter.extension;

import java.util.Map;

/**
 * This holds the default implementation of ContentTransformer
 */
public class DefaultContentTransformer implements ContentTransformer{

    @Override
    public Object transform(Object message, Map<String, Object> dynamicProperties) {
        return message;
    }
}
