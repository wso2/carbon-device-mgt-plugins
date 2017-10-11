/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.extension.siddhi.devicegroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.extension.siddhi.devicegroup.utils.DeviceGroupUtils;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;


/**
 * isDeviceInGroup(deviceId , groupId)
 * Returns true if device belongs to group, otherwise false.
 * Accept Type(s): (STRING, INTEGER)
 * Return Type(s): (BOOL)
 */
public class IsDeviceInGroupFunctionExecutor extends FunctionExecutor {

    private static Log log = LogFactory.getLog(IsDeviceInGroupFunctionExecutor.class);
    private Attribute.Type returnType = Attribute.Type.BOOL;

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors,
            ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 3) {
            throw new ExecutionPlanValidationException(
                    "Invalid no of arguments passed to group:isDeviceInGroup() function, required 3, but found "
                            + attributeExpressionExecutors.length);
        }
        if (attributeExpressionExecutors[0].getReturnType()!= Attribute.Type.INT) {
            throw new ExecutionPlanValidationException(
                    "Invalid parameter type found for the first argument (group id) of group:isDeviceInGroup() " +
                    "function, required " + Attribute.Type.INT + ", but found " +
                    attributeExpressionExecutors[0].getReturnType().toString());
        }
        if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException(
                    "Invalid parameter type found for the second argument (device id) of group:isDeviceInGroup() " +
                    "function, required " + Attribute.Type.STRING + ", but found " +
                    attributeExpressionExecutors[1].getReturnType().toString());
        }
        if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException(
                    "Invalid parameter type found for the third argument (device type) of group:isDeviceInGroup() " +
                    "function, required " + Attribute.Type.STRING + ", but found " +
                    attributeExpressionExecutors[2].getReturnType().toString());
        }
    }

    @Override
    protected Object execute(Object[] data) {
        if (data[0] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to group:isDeviceInGroup() function. " +
                                                    "First argument cannot be null");
        }
        if (data[1] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to group:isDeviceInGroup() function. " +
                                                    "Second argument cannot be null");
        }
        if (data[2] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to group:isDeviceInGroup() function. " +
                                                    "Third argument cannot be null");
        }
        Integer groupId = (Integer) data[0];
        String deviceId = (String) data[1];
        String deviceType = (String) data[2];

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
        GroupManagementProviderService groupManagementService = DeviceGroupUtils.getGroupManagementProviderService();
        try {
            return groupManagementService.isDeviceMappedToGroup(groupId, deviceIdentifier);
        } catch (GroupManagementException e) {
            log.error("Error occurred while checking device is belonging to group.", e);
        }
        return false;
    }

    @Override
    protected Object execute(Object data) {
        return null;  //Since the getProperty function takes in 2 parameters, this method does not get called. Hence,not implemented.
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


