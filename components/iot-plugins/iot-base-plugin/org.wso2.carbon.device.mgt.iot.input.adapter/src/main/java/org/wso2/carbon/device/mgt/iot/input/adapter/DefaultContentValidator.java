package org.wso2.carbon.device.mgt.iot.input.adapter;

import java.util.Map;

public class DefaultContentValidator implements ContentValidator {
    @Override
    public ContentInfo validate(String msgPayload, Map<String, String> params, Map<String, String> dynamicPaarams) {
        return new ContentInfo(true, msgPayload);
    }

}
