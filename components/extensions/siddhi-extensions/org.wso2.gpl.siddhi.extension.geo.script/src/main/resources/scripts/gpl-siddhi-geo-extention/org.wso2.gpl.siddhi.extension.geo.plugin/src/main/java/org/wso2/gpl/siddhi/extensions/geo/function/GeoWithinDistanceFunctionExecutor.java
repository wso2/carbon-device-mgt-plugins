/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.gpl.siddhi.extensions.geo.function;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.gpl.siddhi.extensions.geo.internal.util.WithinDistanceOperation;
import org.wso2.siddhi.query.api.definition.Attribute;

public class GeoWithinDistanceFunctionExecutor extends AbstractGeoOperationExecutor {
    public GeoWithinDistanceFunctionExecutor() {
        this.geoOperation = new WithinDistanceOperation();
    }
    /**
     * The initialization method for FunctionExecutor, this method will be called before the other methods
     *
     * @param attributeExpressionExecutors are the executors of each function parameters
     * @param executionPlanContext         the context of the execution plan
     */
    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        this.geoOperation.init(attributeExpressionExecutors, 0, attributeExpressionExecutors.length);
        if (attributeExpressionExecutors[attributeExpressionExecutors.length - 1].getReturnType() != Attribute.Type.DOUBLE) {
            throw new ExecutionPlanCreationException("Last argument should be a double");
        }
    }
}
