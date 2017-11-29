/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.execution.json;

import org.json.JSONArray;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

/**
 * getArray(elements..)
 * Returns json array of elements as a string
 * Accept Type(s): (STRING|INT|DOUBLE|FLOAT|OBJECT ..)
 * Return Type(s): (STRING)
 */
public class GetArrayFunctionExtension extends FunctionExecutor {

    private Attribute.Type returnType = Attribute.Type.STRING;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
                        ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length <= 0) {
            throw new ExecutionPlanValidationException(
                    "Invalid no of arguments passed to json:getArray() function," + " required one or more, but found "
                    + attributeExpressionExecutors.length);
        }
        Attribute.Type inputType = attributeExpressionExecutors[0].getReturnType();
        for (int i = 1; i < attributeExpressionExecutors.length; i++) {
            if (attributeExpressionExecutors[0].getReturnType() != inputType) {
                throw new ExecutionPlanValidationException(
                        "Parameter types are inconsistent. All parameters should be same");
            }
        }
    }

    @Override
    protected Object execute(Object[] data) {

        JSONArray jsonArray = new JSONArray();
        for (Object obj : data) {
            jsonArray.put(obj);
        }
        return jsonArray.toString();
    }

    @Override
    protected Object execute(Object data) {
        return execute(new Object[]{data});
    }

    @Override
    public void start() {
        //Nothing to start
    }

    @Override
    public void stop() {
        //Nothing to stop
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

    @Override
    public Object[] currentState() {
        return null;    //No need to maintain a state.
    }

    @Override
    public void restoreState(Object[] state) {
        //Since there's no need to maintain a state, nothing needs to be done here.
    }
}


