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

import org.wso2.carbon.device.mgt.mobile.dto.MobileOperationProperty;

import java.util.List;

/**
 *
 * This class represents the key operations associated with persisting mobile operation property
 * related information.
 *
 */
public interface MobileOperationPropertyDAO {

	/**
	 * Add a new MobileOperationProperty to MobileOperationProperty table.
	 *
	 * @param mblOperationProperty MobileOperationProperty object that holds data related to the
	 *                              operation property to be inserted.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean addMobileOperationProperty(MobileOperationProperty mblOperationProperty)
			throws MobileDeviceManagementDAOException;

	/**
	 * Update a MobileOperationProperty in the MobileOperationProperty table.
	 *
	 * @param mblOperationProperty MobileOperationProperty object that holds data has to be updated.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean updateMobileOperationProperty(MobileOperationProperty mblOperationProperty)
			throws MobileDeviceManagementDAOException;

	/**
	 * Deletes MobileOperationProperties of a given operation id from the MobileOperationProperty
	 * table.
	 *
	 * @param mblOperationId Operation id of the MobileOperationProperty to be deleted.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean deleteMobileOperationProperties(int mblOperationId)
			throws MobileDeviceManagementDAOException;

	/**
	 * Retrieve a given MobileOperationProperty from MobileOperationProperty table.
	 *
	 * @param mblOperationId Operation id of the mapping to be retrieved.
	 * @param property    Property of the mapping to be retrieved.
	 * @return MobileOperationProperty object that holds data of the MobileOperationProperty
	 * represented by mblOperationId and property.
	 * @throws MobileDeviceManagementDAOException
	 */
	MobileOperationProperty getMobileOperationProperty(int mblOperationId, String property)
			throws MobileDeviceManagementDAOException;

	/**
	 * Retrieve all the MobileOperationProperties related to the a operation id from
	 * MobileOperationProperty table.
	 *
	 * @param mblOperationId Operation id of the MobileOperationProperty to be retrieved.
	 * @return List of MobileOperationProperty objects.
	 * @throws MobileDeviceManagementDAOException
	 */
	List<MobileOperationProperty> getAllMobileOperationPropertiesOfOperation(int mblOperationId)
			throws MobileDeviceManagementDAOException;
}
