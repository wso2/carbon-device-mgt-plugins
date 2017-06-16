/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mdm.services.android.util;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.mdm.services.android.bean.ErrorListItem;
import org.wso2.carbon.mdm.services.android.bean.ErrorResponse;
import org.wso2.carbon.mdm.services.android.exception.BadRequestException;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Util class for holding Android device related util methods.
 */
public class AndroidDeviceUtils {

    private static final String COMMA_SEPARATION_PATTERN = ", ";

    public DeviceIDHolder validateDeviceIdentifiers(List<String> deviceIDs) {

        List<String> errorDeviceIdList = new ArrayList<String>();
        List<DeviceIdentifier> validDeviceIDList = new ArrayList<DeviceIdentifier>();

        int deviceIDCounter = 0;
        for (String deviceID : deviceIDs) {

            deviceIDCounter++;

            if (deviceID == null || deviceID.isEmpty()) {
                errorDeviceIdList.add(String.format(AndroidConstants.DeviceConstants.DEVICE_ID_NOT_FOUND,
                        deviceIDCounter));
                continue;
            }

            try {
                DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                deviceIdentifier.setId(deviceID);
                deviceIdentifier.setType(DeviceManagementConstants.MobileDeviceTypes.
                        MOBILE_DEVICE_TYPE_ANDROID);

                if (isValidDeviceIdentifier(deviceIdentifier)) {
                    validDeviceIDList.add(deviceIdentifier);
                } else {
                    errorDeviceIdList.add(String.format(AndroidConstants.DeviceConstants.
                        DEVICE_ID_NOT_FOUND, deviceID));
                }
            } catch (DeviceManagementException e) {
                errorDeviceIdList.add(String.format(AndroidConstants.DeviceConstants.DEVICE_ID_SERVICE_NOT_FOUND,
                        deviceIDCounter));
            }
        }

        DeviceIDHolder deviceIDHolder = new DeviceIDHolder();
        deviceIDHolder.setValidDeviceIDList(validDeviceIDList);
        deviceIDHolder.setErrorDeviceIdList(errorDeviceIdList);

        return deviceIDHolder;
    }

    public String convertErrorMapIntoErrorMessage(List<String> errorDeviceIdList) {
        return StringUtils.join(errorDeviceIdList.iterator(), COMMA_SEPARATION_PATTERN);
    }

    public static boolean isValidDeviceIdentifier(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        Device device = AndroidAPIUtils.getDeviceManagementService().
                getDevice(deviceIdentifier, false);
        if (device == null || device.getDeviceIdentifier() == null ||
                device.getDeviceIdentifier().isEmpty() || device.getEnrolmentInfo() == null) {
            return false;
        } else if (EnrolmentInfo.Status.REMOVED.equals(device.getEnrolmentInfo().getStatus())) {
            return false;
        }
        return true;
    }

    /**
     * Returns a new BadRequestException
     *
     * @param description description of the exception
     * @return a new BadRequestException with the specified details as a response DTO
     */
    public static BadRequestException buildBadRequestException(String description) {
        ErrorResponse errorResponse = getErrorResponse(AndroidConstants.
                ErrorMessages.STATUS_BAD_REQUEST_MESSAGE_DEFAULT,400l, description);
        return new BadRequestException(errorResponse);
    }

    /**
     * Returns generic ErrorResponse.
     * @param message specific error message
     * @param code
     * @param description
     * @return generic Response with error specific details.
     */
    public static ErrorResponse getErrorResponse(String message, Long code, String description) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(code);
        errorResponse.setMoreInfo("");
        errorResponse.setMessage(message);
        errorResponse.setDescription(description);
        return errorResponse;
    }

    public static <T> ErrorResponse getConstraintViolationErrorDTO(Set<ConstraintViolation<T>> violations) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setDescription("Validation Error");
        errorResponse.setMessage("Bad Request");
        errorResponse.setCode(400l);
        errorResponse.setMoreInfo("");
        List<ErrorListItem> errorListItems = new ArrayList<>();
        for (ConstraintViolation violation : violations) {
            ErrorListItem errorListItemDTO = new ErrorListItem();
            errorListItemDTO.setCode(400 + "_" + violation.getPropertyPath());
            errorListItemDTO.setMessage(violation.getPropertyPath() + ": " + violation.getMessage());
            errorListItems.add(errorListItemDTO);
        }
        errorResponse.setErrorItems(errorListItems);
        return errorResponse;
    }

}
