package org.wso2.carbon.device.mgt.iot.input.adapter.extension;

import java.util.Map;

/**
 * This holds the default implementation of content validator interface.
 */
public class DefaultContentValidator implements ContentValidator {

    @Override
    public ContentInfo validate(Object message, Map<String, String> params, Map<String, String> dynamicParams) {
        return new ContentInfo(true, message);
    }

}
