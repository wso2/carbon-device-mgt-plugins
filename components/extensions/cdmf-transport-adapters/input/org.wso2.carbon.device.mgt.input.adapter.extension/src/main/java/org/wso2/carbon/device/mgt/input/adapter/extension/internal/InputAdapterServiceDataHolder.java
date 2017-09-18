/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.device.mgt.input.adapter.extension.internal;

import org.wso2.carbon.device.mgt.input.adapter.extension.ContentTransformer;
import org.wso2.carbon.device.mgt.input.adapter.extension.ContentValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * common place to hold some OSGI service references.
 */
public class InputAdapterServiceDataHolder {

    private static InputAdapterServiceDataHolder inputAdapterServiceDataHolder = new InputAdapterServiceDataHolder();
    private static Map<String, ContentValidator> contentValidatorMap = new HashMap<>();
    private static Map<String, ContentTransformer> contentTransformerMap = new HashMap<>();

	private InputAdapterServiceDataHolder() {
	}

    public static InputAdapterServiceDataHolder getInstance() {
        return  inputAdapterServiceDataHolder;
    }

    public Map<String, ContentValidator> getContentValidatorMap() {
        return contentValidatorMap;
    }

    public void addContentValidator(ContentValidator contentValidator) {
        InputAdapterServiceDataHolder.contentValidatorMap.put(contentValidator.getType(), contentValidator);
    }

    public Map<String, ContentTransformer> getContentTransformerMap() {
        return contentTransformerMap;
    }

    public void addContentTransformer(ContentTransformer contentTransformer) {
        InputAdapterServiceDataHolder.contentTransformerMap.put(contentTransformer.getType(), contentTransformer);
    }
}
