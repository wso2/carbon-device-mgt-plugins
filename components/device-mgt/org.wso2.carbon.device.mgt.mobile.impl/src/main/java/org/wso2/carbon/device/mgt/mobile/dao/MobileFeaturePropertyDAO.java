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

import org.wso2.carbon.device.mgt.mobile.dto.MobileFeatureProperty;

import java.util.List;

/**
 * This class represents the key operations associated with persisting mobile feature property
 * related information.
 */
public interface MobileFeaturePropertyDAO {

	/**
	 * Add a new MobileFeatureProperty to MobileFeatureProperty table.
	 *
	 * @param mblFeatureProperty MobileFeatureProperty object that holds data related to the feature
	 *                                 property to be inserted.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean addMobileFeatureProperty(MobileFeatureProperty mblFeatureProperty)
			throws MobileDeviceManagementDAOException;

	/**
	 * Updates a MobileFeatureProperty in the MobileFeatureProperty table.
	 *
	 * @param mblFeatureProperty MobileFeatureProperty object that holds data has to be updated.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean updateMobileFeatureProperty(MobileFeatureProperty mblFeatureProperty)
			throws MobileDeviceManagementDAOException;

	/**
	 * Deletes a given MobileFeatureProperty from MobileFeatureProperty table.
	 *
	 * @param property Property of the MobileFeatureProperty to be deleted.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean deleteMobileFeatureProperty(String property) throws MobileDeviceManagementDAOException;

	/**
	 * Deletes MobileFeatureProperties of a given feature from MobileFeatureProperty table.
	 *
	 * @param mblFeatureId Feature-id of the MobileFeature corresponding properties should be deleted.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean deleteMobileFeaturePropertiesOfFeature(Integer mblFeatureId)
			throws MobileDeviceManagementDAOException;

	/**
	 * Retrieves a given MobileFeatureProperty from MobileFeatureProperty table.
	 *
	 * @param property Property of the feature property to be retrieved.
	 * @return MobileFeatureProperty object that holds data of the feature property represented by
	 * property.
	 * @throws MobileDeviceManagementDAOException
	 */
	MobileFeatureProperty getMobileFeatureProperty(String property)
			throws MobileDeviceManagementDAOException;

	/**
	 * Retrieves a list of MobileFeatureProperties corresponds to a given feature id from
	 * MobileFeatureProperty table.
	 *
	 * @param mblFeatureId feature id of the MobileFeatureProperties to be retrieved.
	 * @return List of MobileFeatureProperty objects that holds data of the MobileFeatureProperties
	 * represented by featureId.
	 * @throws MobileDeviceManagementDAOException
	 */
	List<MobileFeatureProperty> getFeaturePropertiesOfFeature(Integer mblFeatureId)
			throws MobileDeviceManagementDAOException;

}
