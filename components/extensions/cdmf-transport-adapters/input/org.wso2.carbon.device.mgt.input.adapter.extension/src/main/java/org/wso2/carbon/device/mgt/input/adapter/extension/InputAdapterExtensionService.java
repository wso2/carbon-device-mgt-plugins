package org.wso2.carbon.device.mgt.input.adapter.extension;

/**
 * This hold the input adapter extension service.
 */
public interface InputAdapterExtensionService {

    /**
     * return content validator for the given type.
     * @param type type of the content validator
     * @return content validator for the given type.
     */
    ContentValidator getContentValidator(String type);

    /**
     * return default content validator for the given type.
     * @return default content validator for the given type.
     */
    ContentValidator getDefaultContentValidator();

    /**
     * return content transformer for the given type.
     * @param type of the content transfomer
     * @return content transformer for the given type.
     */
    ContentTransformer getContentTransformer(String type);

    /**
     * return default content transformer for the given type.
     * @return default content transformer for the given type.
     */
    ContentTransformer getDefaultContentTransformer();

}
