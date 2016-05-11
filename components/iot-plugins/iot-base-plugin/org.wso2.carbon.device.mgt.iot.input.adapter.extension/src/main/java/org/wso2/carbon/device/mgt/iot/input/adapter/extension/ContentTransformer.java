package org.wso2.carbon.device.mgt.iot.input.adapter.extension;

import java.util.Map;

/**
 * Content Transformer will be triggered through input adapters
 */
public interface ContentTransformer {

    /**
     * This is used to transform the receiver content
     * @param message message to be format
     * @param dynamicProperties related to transport.
     * @return transformed message
     */
    Object transform(Object message, Map<String, String> dynamicProperties);
}
