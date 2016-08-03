/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.mdm.services.android.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.mdm.services.android.bean.ErrorResponse;
import org.wso2.carbon.mdm.services.android.bean.wrapper.AndroidApplication;
import org.wso2.carbon.mdm.services.android.bean.wrapper.AndroidDevice;
import org.wso2.carbon.mdm.services.android.exception.UnexpectedServerErrorException;
import org.wso2.carbon.mdm.services.android.services.DeviceManagementService;
import org.wso2.carbon.mdm.services.android.util.AndroidAPIUtils;
import org.wso2.carbon.mdm.services.android.util.AndroidConstants;
import org.wso2.carbon.mdm.services.android.util.AndroidDeviceUtils;
import org.wso2.carbon.mdm.services.android.util.Message;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceManagementServiceImpl implements DeviceManagementService {

    private static final String OPERATION_ERROR_STATUS = "ERROR";
    private static final Log log = LogFactory.getLog(DeviceManagementServiceImpl.class);

    @PUT
    @Path("/{id}/applications")
    @Override
    public Response updateApplicationList(@PathParam("id")
                                          @NotNull
                                          @Size(min = 2, max = 45)
                                          @Pattern(regexp = "^[A-Za-z0-9]*$")
                                          String id, List<AndroidApplication> androidApplications) {
        Application application;
        List<Application> applications = new ArrayList<>();
        for (AndroidApplication androidApplication : androidApplications) {
            application = new Application();
            application.setPlatform(androidApplication.getPlatform());
            application.setCategory(androidApplication.getCategory());
            application.setName(androidApplication.getName());
            application.setLocationUrl(androidApplication.getLocationUrl());
            application.setImageUrl(androidApplication.getImageUrl());
            application.setVersion(androidApplication.getVersion());
            application.setType(androidApplication.getType());
            application.setAppProperties(androidApplication.getAppProperties());
            application.setApplicationIdentifier(androidApplication.getApplicationIdentifier());
            application.setMemoryUsage(androidApplication.getMemoryUsage());
            applications.add(application);
        }
        Message responseMessage = new Message();
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(id);
        deviceIdentifier.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
        try {
            AndroidAPIUtils.getApplicationManagerService().
                    updateApplicationListInstalledInDevice(deviceIdentifier, applications);
            responseMessage.setResponseMessage("Device information has modified successfully.");
            return Response.status(Response.Status.ACCEPTED).entity(responseMessage).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while modifying the application list.";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
    }

    @PUT
    @Path("/{id}/pending-operations")
    @Override
    public Response getPendingOperations(@PathParam("id") String id,
                                         @HeaderParam("If-Modified-Since") String ifModifiedSince,
                                         List<? extends Operation> resultOperations) {
        if (id == null || id.isEmpty()) {
            String msg = "Device identifier is null or empty, hence returning device not found";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        DeviceIdentifier deviceIdentifier = AndroidAPIUtils.convertToDeviceIdentifierObject(id);
        try {
            if (!AndroidDeviceUtils.isValidDeviceIdentifier(deviceIdentifier)) {
                String msg = "Device not found for identifier '" + id + "'";
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            if (log.isDebugEnabled()) {
                log.debug("Invoking Android pending operations:" + id);
            }
            if (resultOperations != null && !resultOperations.isEmpty()) {
                updateOperations(id, resultOperations);
            }
        } catch (OperationManagementException e) {
            String msg = "Issue in retrieving operation management service instance";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        } catch (PolicyComplianceException e) {
            String msg = "Issue in updating Monitoring operation";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        } catch (DeviceManagementException e) {
            String msg = "Issue in retrieving device management service instance";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        } catch (ApplicationManagementException e) {
            String msg = "Issue in retrieving application management service instance";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        } catch (NotificationManagementException e) {
            String msg = "Issue in retrieving Notification management service instance";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }

        List<? extends Operation> pendingOperations;
        try {
            pendingOperations = AndroidAPIUtils.getPendingOperations(deviceIdentifier);
        } catch (OperationManagementException e) {
            String msg = "Issue in retrieving operation management service instance";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
        return Response.status(Response.Status.CREATED).entity(pendingOperations).build();
    }

    private void updateOperations(String deviceId, List<? extends Operation> operations)
            throws OperationManagementException, PolicyComplianceException,
            ApplicationManagementException, NotificationManagementException, DeviceManagementException {
        for (org.wso2.carbon.device.mgt.common.operation.mgt.Operation operation : operations) {
            AndroidAPIUtils.updateOperation(deviceId, operation);
            if (OPERATION_ERROR_STATUS.equals(operation.getStatus().toString())) {
                org.wso2.carbon.device.mgt.common.notification.mgt.Notification notification = new
                        org.wso2.carbon.device.mgt.common.notification.mgt.Notification();
                DeviceIdentifier id = new DeviceIdentifier();
                id.setId(deviceId);
                id.setType(AndroidConstants.DEVICE_TYPE_ANDROID);
                String deviceName = AndroidAPIUtils.getDeviceManagementService().getDevice(id).getName();
                notification.setOperationId(operation.getId());
                notification.setStatus(org.wso2.carbon.device.mgt.common.notification.mgt.Notification.
                        Status.NEW.toString());
                notification.setDescription("Operation " + operation.getCode() + " failed to execute on device " +
                        deviceName + ". Device ID : " + deviceId);
                AndroidAPIUtils.getNotificationManagementService().addNotification(id, notification);
            }
            if (log.isDebugEnabled()) {
                log.debug("Updating operation '" + operation.toString() + "'");
            }
        }
    }

    @POST
    @Override
    public Response enrollDevice(@Valid AndroidDevice androidDevice) {
        try {
            Device device = new Device();
            device.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
            device.setEnrolmentInfo(androidDevice.getEnrolmentInfo());
            device.getEnrolmentInfo().setOwner(AndroidAPIUtils.getAuthenticatedUser());
            device.setDeviceInfo(androidDevice.getDeviceInfo());
            device.setDeviceIdentifier(androidDevice.getDeviceIdentifier());
            device.setDescription(androidDevice.getDescription());
            device.setName(androidDevice.getName());
            device.setFeatures(androidDevice.getFeatures());
            device.setProperties(androidDevice.getProperties());

            boolean status = AndroidAPIUtils.getDeviceManagementService().enrollDevice(device);

            PolicyManagerService policyManagerService = AndroidAPIUtils.getPolicyManagerService();
            policyManagerService.getEffectivePolicy(new DeviceIdentifier(androidDevice.getDeviceIdentifier(), device.getType()));
            if (status) {
                return Response.status(Response.Status.OK).entity("Android device, which carries the id '" +
                        androidDevice.getDeviceIdentifier() + "' has successfully been enrolled").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to enroll '" +
                        device.getType() + "' device, which carries the id '" +
                        androidDevice.getDeviceIdentifier() + "'").build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while enrolling the android, which carries the id '" +
                    androidDevice.getDeviceIdentifier() + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while enforcing default enrollment policy upon android " +
                    "', which carries the id '" +
                    androidDevice.getDeviceIdentifier() + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
    }

    @GET
    @Path("/{id}/status")
    @Override
    public Response isEnrolled(@PathParam("id") String id, @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        boolean result;
        DeviceIdentifier deviceIdentifier = AndroidAPIUtils.convertToDeviceIdentifierObject(id);
        try {
            result = AndroidAPIUtils.getDeviceManagementService().isEnrolled(deviceIdentifier);
            if (result) {
                return Response.status(Response.Status.OK).entity("Android device that carries the id '" +
                        id + "' is enrolled").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("No Android device is found upon the id '" +
                        id + "'").build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while checking enrollment status of the device.";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
    }

    @PUT
    @Path("/{id}")
    @Override
    public Response modifyEnrollment(@PathParam("id") String id, @Valid AndroidDevice androidDevice) {
        Device device = new Device();
        device.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
        device.setEnrolmentInfo(androidDevice.getEnrolmentInfo());
        device.getEnrolmentInfo().setOwner(AndroidAPIUtils.getAuthenticatedUser());
        device.setDeviceInfo(androidDevice.getDeviceInfo());
        device.setDeviceIdentifier(androidDevice.getDeviceIdentifier());
        device.setDescription(androidDevice.getDescription());
        device.setName(androidDevice.getName());
        device.setFeatures(androidDevice.getFeatures());
        device.setProperties(androidDevice.getProperties());
        boolean result;
        try {
            device.setType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID);
            result = AndroidAPIUtils.getDeviceManagementService().modifyEnrollment(device);
            if (result) {
                return Response.status(Response.Status.ACCEPTED).entity("Enrollment of Android device that " +
                        "carries the id '" + id + "' has successfully updated").build();
            } else {
                return Response.status(Response.Status.NOT_MODIFIED).entity("Enrollment of Android device that " +
                        "carries the id '" + id + "' has not been updated").build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while modifying enrollment of the Android device that carries the id '" +
                    id + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
    }

    @DELETE
    @Path("/{id}")
    @Override
    public Response disEnrollDevice(@PathParam("id") String id) {
        boolean result;
        DeviceIdentifier deviceIdentifier = AndroidAPIUtils.convertToDeviceIdentifierObject(id);
        try {
            result = AndroidAPIUtils.getDeviceManagementService().disenrollDevice(deviceIdentifier);
            if (result) {
                return Response.status(Response.Status.OK).entity("Android device that carries id '" + id +
                        "' has successfully dis-enrolled").build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Android device that carries id '" + id +
                        "' has not been dis-enrolled").build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while dis-enrolling the Android device that carries the id '" + id + "'";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
    }

}
