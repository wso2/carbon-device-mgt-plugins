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

import org.wso2.carbon.device.mgt.mobile.dto.MobileOperation;

/**
 * This class represents the key operations associated with persisting mobile operation related
 * information.
 */
public interface MobileOperationDAO {

	/**
	 * Adds a new Mobile operation to the MobileOperation table.
	 * @param mblOperation MobileOperation object that holds data related to the operation to be
	 *                        inserted.
	 * @return The id of the inserted record, if the insertion was unsuccessful -1 is returned.
	 * @throws MobileDeviceManagementDAOException
	 */
	int addMobileOperation(MobileOperation mblOperation) throws MobileDeviceManagementDAOException;

	/**
	 * Updates a Mobile operation in the MobileOperation table.
	 * @param mblOperation MobileOperation object that holds data has to be updated.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean updateMobileOperation(MobileOperation mblOperation) throws MobileDeviceManagementDAOException;

	/**
	 * Deletes a given MobileOperation from MobileOperation table.
	 * @param mblOperationId Operation code of the MobileOperation to be deleted.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean deleteMobileOperation(int mblOperationId) throws MobileDeviceManagementDAOException;

	/**
	 * Retrieve a MobileOperation from MobileOperation table.
	 * @param mblOperationId Operation id of the MobileOperation to be retrieved.
	 * @return MobileOperation object that holds data of MobileOperation represented by operationId.
	 * @throws MobileDeviceManagementDAOException
	 */
	MobileOperation getMobileOperation(int mblOperationId) throws MobileDeviceManagementDAOException;

}
