/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.andes.extensions.device.mgt.jaxrs.util;

import org.wso2.carbon.andes.extensions.device.mgt.jaxrs.beans.*;

public class RequestValidationUtil {
    public static void validatePaginationParameters(int offset, int limit) {
        if (offset < 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request parameter offset is s " +
                            "negative value.").build());
        }
        if (limit < 0) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request parameter limit is a " +
                            "negative value.").build());
        }
        if (limit > 100) {
            throw new InputValidationException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(400l).setMessage("Request parameter limit should" +
                            " be less than or equal to 100.").build());
        }

    }

}
