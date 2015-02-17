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

package org.wso2.carbon.device.mgt.mobile.dao;

import org.wso2.carbon.device.mgt.mobile.dto.MobileDeviceOperationMapping;

import java.util.List;

/**
 * This class represents the mapping between mobile device and operations.
 */
public interface MobileDeviceOperationMappingDAO {

	/**
	 * Adds a new mobile device operation mapping to the table.
	 *
	 * @param mblDeviceOperationMapping MobileDeviceOperationMapping object that holds data related
	 *                                  to the MobileDeviceOperationMapping to be inserted.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean addMobileDeviceOperationMapping(MobileDeviceOperationMapping mblDeviceOperationMapping)
			throws MobileDeviceManagementDAOException;

	/**
	 * Updates a MobileDeviceOperationMapping in MobileDeviceOperationMapping table.
	 *
	 * @param mblDeviceOperation MobileDeviceOperationMapping object that holds data has to be updated.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean updateMobileDeviceOperationMapping(MobileDeviceOperationMapping mblDeviceOperation)
			throws MobileDeviceManagementDAOException;

	/**
	 * Updates a MobileDeviceOperationMapping to In-Progress state in MobileDeviceOperationMapping
	 * table.
	 *
	 * @param mblDeviceId MobileDevice id of the mappings to be updated.
	 * @param operationId Operation id of the mapping to be updated.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean updateMobileDeviceOperationMappingToInProgress(String mblDeviceId, int operationId)
			throws MobileDeviceManagementDAOException;

	/**
	 * Updates a MobileDeviceOperationMapping to completed state in MobileDeviceOperationMapping
	 * table.
	 *
	 * @param mblDeviceId MobileDevice id of the mappings to be updated.
	 * @param operationId Operation id of the mapping to be updated.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean updateMobileDeviceOperationMappingToCompleted(String mblDeviceId, int operationId)
			throws MobileDeviceManagementDAOException;

	/**
	 * Delete a given MobileDeviceOperationMapping from MobileDeviceOperationMapping table.
	 *
	 * @param mblDeviceId MobileDevice id of the mappings to be deleted.
	 * @param operationId Operation id of the mapping to be deleted.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean deleteMobileDeviceOperationMapping(String mblDeviceId, int operationId)
			throws MobileDeviceManagementDAOException;

	/**
	 * Retrieves a given MobileDeviceOperationMapping object from the MobileDeviceOperationMapping
	 * table.
	 *
	 * @param mblDeviceId Device id of the mapping to be retrieved.
	 * @param operationId Operation id of the mapping to be retrieved.
	 * @return MobileDeviceOperation object that holds data of the device operation mapping
	 * represented by deviceId and operationId.
	 * @throws MobileDeviceManagementDAOException
	 */
	MobileDeviceOperationMapping getMobileDeviceOperationMapping(String mblDeviceId, int operationId)
			throws MobileDeviceManagementDAOException;

	/**
	 * Retrieves all the of MobileDeviceOperationMappings relevant to a given mobile device.
	 *
	 * @param mblDeviceId MobileDevice id of the mappings to be retrieved.
	 * @return MobileDeviceOperationMapping object list.
	 * @throws MobileDeviceManagementDAOException
	 */
	List<MobileDeviceOperationMapping> getAllMobileDeviceOperationMappingsOfDevice(String mblDeviceId)
			throws MobileDeviceManagementDAOException;

	/**
	 * Retrieves all the pending MobileDeviceOperationMappings of a mobile device.
	 *
	 * @param mblDeviceId MobileDevice id of the mappings to be retrieved.
	 * @return MobileDeviceOperationMapping object list.
	 * @throws MobileDeviceManagementDAOException
	 */
	List<MobileDeviceOperationMapping> getAllPendingOperationMappingsOfMobileDevice(String mblDeviceId)
			throws MobileDeviceManagementDAOException;
}
