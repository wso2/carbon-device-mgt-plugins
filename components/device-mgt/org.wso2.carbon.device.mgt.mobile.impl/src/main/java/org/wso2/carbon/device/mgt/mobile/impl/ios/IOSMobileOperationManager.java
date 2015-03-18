/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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
package org.wso2.carbon.device.mgt.mobile.impl.ios;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.mobile.AbstractMobileOperationManager;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.mobile.dao.MobileDeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.mobile.dto.MobileDeviceOperationMapping;
import org.wso2.carbon.device.mgt.mobile.dto.MobileOperation;
import org.wso2.carbon.device.mgt.mobile.dto.MobileOperationProperty;
import org.wso2.carbon.device.mgt.mobile.util.MobileDeviceManagementUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IOSMobileOperationManager extends AbstractMobileOperationManager  {

    private static final Log log = LogFactory.getLog(IOSMobileOperationManager.class);
    @Override
    public List<Operation> getOperations(DeviceIdentifier deviceIdentifier) throws OperationManagementException {
        return null;
    }

    @Override
    public boolean addOperation(Operation operation, List<DeviceIdentifier> devices)
            throws OperationManagementException {

        boolean status = false;

        try {
            MobileDeviceOperationMapping mobileDeviceOperationMapping;
            MobileOperation mobileOperation = MobileDeviceManagementUtil.convertToMobileOperation(operation);
            int operationId = MobileDeviceManagementDAOFactory.getMobileOperationDAO().
                    addMobileOperation(mobileOperation);

            if (operationId > 0) {

                for (MobileOperationProperty operationProperty : mobileOperation.getProperties()) {
                    operationProperty.setOperationId(operationId);
                    status = MobileDeviceManagementDAOFactory.getMobileOperationPropertyDAO().
                            addMobileOperationProperty(operationProperty);
                }

                for (DeviceIdentifier deviceIdentifier : devices) {
                    mobileDeviceOperationMapping = new MobileDeviceOperationMapping();
                    mobileDeviceOperationMapping.setOperationId(operationId);
                    mobileDeviceOperationMapping.setDeviceId(deviceIdentifier.getId());
                    mobileDeviceOperationMapping.setStatus(MobileDeviceOperationMapping.Status.NEW);
                    status = MobileDeviceManagementDAOFactory.getMobileDeviceOperationDAO().
                            addMobileDeviceOperationMapping(mobileDeviceOperationMapping);
                }

            }
        } catch (MobileDeviceManagementDAOException e) {
            String msg = String.format("Error while adding operation %s to iOS device", operation.getCode());
            log.error(msg, e);
            throw new OperationManagementException(msg, e);
        }

        return status;
    }

    @Override
    public List<Operation> getPendingOperations(DeviceIdentifier deviceIdentifier) throws OperationManagementException {

        List<Operation> operations = new ArrayList<Operation>();
        List<MobileDeviceOperationMapping> mobileDeviceOperationMappings;
        List<MobileOperationProperty> operationProperties ;
        MobileOperation mobileOperation;

        try {
            mobileDeviceOperationMappings = MobileDeviceManagementDAOFactory.getMobileDeviceOperationDAO().
                    getAllPendingOperationMappingsOfMobileDevice(deviceIdentifier.getId());

            for (MobileDeviceOperationMapping operation : mobileDeviceOperationMappings) {

                mobileOperation = MobileDeviceManagementDAOFactory.getMobileOperationDAO().
                        getMobileOperation(operation.getOperationId());
                operationProperties = MobileDeviceManagementDAOFactory.getMobileOperationPropertyDAO().
                        getAllMobileOperationPropertiesOfOperation(operation.getOperationId());
                mobileOperation.setProperties(operationProperties);
                operations.add(MobileDeviceManagementUtil.convertMobileOperationToOperation(mobileOperation));
                operation.setStatus(MobileDeviceOperationMapping.Status.INPROGRESS);
                operation.setSentDate(new Date().getTime());

                MobileDeviceManagementDAOFactory.getMobileDeviceOperationDAO().
                        updateMobileDeviceOperationMappingToInProgress(operation.getDeviceId(),
                                operation.getOperationId());
            }
        } catch (MobileDeviceManagementDAOException e) {
            String msg = "Error occurred when retrieving pending operations in iOS device " + deviceIdentifier.getId();
            log.error(msg, e);
            throw new OperationManagementException(msg, e);
        }
        return operations;
    }

    @Override
    public List<Feature> getFeatures(String type) throws FeatureManagementException {
        return null;
    }

}
