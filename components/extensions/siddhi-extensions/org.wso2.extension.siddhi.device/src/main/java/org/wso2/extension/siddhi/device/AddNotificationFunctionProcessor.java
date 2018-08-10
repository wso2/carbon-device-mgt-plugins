/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.extension.siddhi.device;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementService;
import org.wso2.extension.siddhi.device.utils.DeviceUtils;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.ArrayList;
import java.util.List;

public class AddNotificationFunctionProcessor extends StreamFunctionProcessor {

    private static final Log log = LogFactory.getLog(AddNotificationFunctionProcessor.class);

    /**
     * The init method of the StreamProcessor, this method will be called before other methods
     *
     * @param abstractDefinition           the incoming stream definition
     * @param attributeExpressionExecutors the executors of each function parameters
     * @param executionPlanContext         the context of the execution plan
     * @return the additional output attributes introduced by the function
     */
    @Override
    protected List<Attribute> init(AbstractDefinition abstractDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors,
                                   ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 3) {
            throw new ExecutionPlanValidationException(
                    "Invalid no of arguments passed to device:addNotification() function, required 3 but found " +
                            attributeExpressionExecutors.length);
        }
        if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException(
                    "Invalid parameter type found for the first argument (deviceIdentifier) of device:addNotification() " +
                            "function, required " + Attribute.Type.STRING + " as deviceIdentifier, but found " +
                            attributeExpressionExecutors[0].getReturnType().toString());
        }
        if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException(
                    "Invalid parameter type found for the second argument (deviceType) of device:addNotification() " +
                            "function, required " + Attribute.Type.STRING + " as deviceType, but found " +
                            attributeExpressionExecutors[1].getReturnType().toString());
        }
        if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanValidationException(
                    "Invalid parameter type found for the third argument (description) of device:addNotification() " +
                            "function, required " + Attribute.Type.STRING + " as description, but found " +
                            attributeExpressionExecutors[2].getReturnType().toString());
        }
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("notified", Attribute.Type.BOOL));
        return attributes;
    }

    /**
     * The process method of the StreamFunction, used when more than one function parameters are provided
     *
     * @param data the data values for the function parameters
     * @return the data for additional output attributes introduced by the function
     */
    @Override
    protected Object[] process(Object[] data) {
        if (data[0] == null || data[1] == null || data[2] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to device:addNotification() function. " +
                    "Neither of any three arguments cannot be null");
        }
        boolean isNotified = false;
        String deviceId = (String) data[0];
        String deviceType = (String) data[1];
        String description = (String) data[2];
        Notification notification = new Notification();
        notification.setStatus(Notification.Status.NEW.name());
        notification.setDeviceIdentifier(deviceId);
        notification.setDeviceType(deviceType);
        notification.setDescription(description);

        NotificationManagementService notificationManagementService = DeviceUtils.getNotificationManagementService();
        try {
            isNotified = notificationManagementService.addNotification(new DeviceIdentifier(deviceId, deviceType), notification);
        } catch (NotificationManagementException e) {
            log.error("Error occurred while adding notification for " + deviceType + " device with id " + deviceId, e);
        }

        return new Object[]{isNotified};
    }

    /**
     * The process method of the StreamFunction, used when zero or one function parameter is provided
     *
     * @param data null if the function parameter count is zero or runtime data value of the function parameter
     * @return the data for additional output attribute introduced by the function
     */
    @Override
    protected Object[] process(Object data) {
        return new Object[0];
    }

    /**
     * This will be called only once and this can be used to acquire
     * required resources for the processing element.
     * This will be called after initializing the system and before
     * starting to process the events.
     */
    @Override
    public void start() {

    }

    /**
     * This will be called only once and this can be used to release
     * the acquired resources for processing.
     * This will be called before shutting down the system.
     */
    @Override
    public void stop() {

    }

    /**
     * Used to collect the serializable state of the processing element, that need to be
     * persisted for the reconstructing the element to the same state on a different point of time
     *
     * @return stateful objects of the processing element as an array
     */
    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    /**
     * Used to restore serialized state of the processing element, for reconstructing
     * the element to the same state as if was on a previous point of time.
     *
     * @param objects the stateful objects of the element as an array on
     *                the same order provided by currentState().
     */
    @Override
    public void restoreState(Object[] objects) {
        //Since there's no need to maintain a state, nothing needs to be done here.
    }
}
