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

package org.wso2.carbon.device.mgt.mobile.windows.impl.dao;
import org.wso2.carbon.device.mgt.mobile.windows.impl.dto.MobileDevice;

import java.util.List;

/**
 * This class represents the key operations associated with persisting mobile-device related
 * information.
 */
public interface MobileDeviceDAO {

	/**
	 * Fetches a MobileDevice from MDM database.
	 *
	 * @param mblDeviceId Id of the Mobile-Device.
	 * @return MobileDevice corresponding to given device-id.
	 * @throws MobileDeviceManagementDAOException
	 */
	MobileDevice getMobileDevice(String mblDeviceId) throws MobileDeviceManagementDAOException;

	/**
	 * Adds a new MobileDevice to the MDM database.
	 *
	 * @param mobileDevice MobileDevice to be added.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean addMobileDevice(MobileDevice mobileDevice) throws MobileDeviceManagementDAOException;

	/**
	 * Updates MobileDevice information in MDM database.
	 *
	 * @param mobileDevice MobileDevice to be updated.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean updateMobileDevice(MobileDevice mobileDevice) throws MobileDeviceManagementDAOException;

	/**
	 * Deletes a given MobileDevice from MDM database.
	 *
	 * @param mblDeviceId Id of MobileDevice to be deleted.
	 * @return The status of the operation.
	 * @throws MobileDeviceManagementDAOException
	 */
	boolean deleteMobileDevice(String mblDeviceId) throws MobileDeviceManagementDAOException;

	/**
	 * Fetches all MobileDevices from MDM database.
	 *
	 * @return List of MobileDevices.
	 * @throws MobileDeviceManagementDAOException
	 */
	List<MobileDevice> getAllMobileDevices() throws MobileDeviceManagementDAOException;

}
