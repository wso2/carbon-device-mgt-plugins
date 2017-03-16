package org.wso2.carbon.device.mgt.input.adapter.extension;

import org.wso2.carbon.device.mgt.input.adapter.extension.internal.InputAdapterServiceDataHolder;

/**
 * This hold the input adapter extension service implementation.
 */
public class InputAdapterExtensionServiceImpl implements InputAdapterExtensionService {
    private static final String DEFAULT = "default";


    @Override
    public ContentValidator getContentValidator(String type) {
        return InputAdapterServiceDataHolder.getInstance().getContentValidatorMap().get(type);
    }

    @Override
    public ContentValidator getDefaultContentValidator() {
        return InputAdapterServiceDataHolder.getInstance().getContentValidatorMap().get(DEFAULT);
    }

    @Override
    public ContentTransformer getContentTransformer(String type) {
        return InputAdapterServiceDataHolder.getInstance().getContentTransformerMap().get(type);
    }

    @Override
    public ContentTransformer getDefaultContentTransformer() {
        return InputAdapterServiceDataHolder.getInstance().getContentTransformerMap().get(DEFAULT);
    }
}
