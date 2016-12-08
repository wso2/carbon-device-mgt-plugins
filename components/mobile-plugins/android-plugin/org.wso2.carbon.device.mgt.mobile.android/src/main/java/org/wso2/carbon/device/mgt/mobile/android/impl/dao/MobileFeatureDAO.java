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

package org.wso2.carbon.device.mgt.mobile.android.impl.dao;

import org.wso2.carbon.device.mgt.mobile.android.impl.dto.MobileFeature;

import java.util.List;

/**
 * This class represents the key operations associated with persisting mobile feature related
 * information.
 */
public interface MobileFeatureDAO {

	/**
	 * Adds a new MobileFeature to Mobile-Feature table.
	 *
	 * @param mobileFeature MobileFeature object that holds data related to the feature to be inserted.
	 * @return boolean status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean addFeature(MobileFeature mobileFeature) throws MobileDeviceManagementDAOException;

	/**
	 * Adda a list of MobileFeatures to Mobile-Feature table.
	 *
	 * @param mobileFeatures List of MobileFeature objects.
	 * @return boolean status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean addFeatures(List<MobileFeature> mobileFeatures) throws MobileDeviceManagementDAOException;

	/**
	 * Updates a MobileFeature in Mobile-Feature table.
	 *
	 * @param mobileFeature MobileFeature object that holds data has to be updated.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean updateFeature(MobileFeature mobileFeature) throws MobileDeviceManagementDAOException;

	/**
	 * Deletes a MobileFeature from Mobile-Feature table when the feature id is given.
	 *
	 * @param mblFeatureId MobileFeature id of the MobileFeature to be deleted.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean deleteFeatureById(int mblFeatureId) throws MobileDeviceManagementDAOException;

	/**
	 * Deletes a MobileFeature from Mobile-Feature table when the feature code is given.
	 *
	 * @param mblFeatureCode MobileFeature code of the feature to be deleted.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean deleteFeatureByCode(String mblFeatureCode) throws MobileDeviceManagementDAOException;

	/**
	 * Retrieves a given MobileFeature from Mobile-Feature table when the feature id is given.
	 *
	 * @param mblFeatureId Feature id of the feature to be retrieved.
	 * @return MobileFeature object that holds data of the feature represented by featureId.
	 * @throws MobileDeviceManagementDAOException
	 */
	MobileFeature getFeatureById(int mblFeatureId) throws MobileDeviceManagementDAOException;

	/**
	 * Retrieves a given MobileFeature from Mobile-Feature table when the feature code is given.
	 *
	 * @param mblFeatureCode Feature code of the feature to be retrieved.
	 * @return MobileFeature object that holds data of the feature represented by featureCode.
	 * @throws MobileDeviceManagementDAOException
	 */
	MobileFeature getFeatureByCode(String mblFeatureCode) throws MobileDeviceManagementDAOException;

	/**
	 * Retrieves all MobileFeatures of a MobileDevice type from Mobile-Feature table.
	 *
	 * @param deviceType MobileDevice type of the MobileFeatures to be retrieved
	 * @return MobileFeature object list.
	 * @throws MobileDeviceManagementDAOException
	 */
	List<MobileFeature> getFeatureByDeviceType(String deviceType) throws MobileDeviceManagementDAOException;

	/**
	 * Retrieve all the MobileFeatures from Mobile-Feature table.
	 *
	 * @return MobileFeature object list.
	 * @throws MobileDeviceManagementDAOException
	 */
	List<MobileFeature> getAllFeatures() throws MobileDeviceManagementDAOException;
}
