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

package org.wso2.carbon.mdm.services.android.services.operationmgt;

import io.swagger.annotations.*;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.mdm.services.android.bean.wrapper.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Android Device Operation REST-API implementation.
 */

@Api(value = "OperationMgtService", description = "Android Device Operation REST-API implementation.")
public interface OperationMgtService {
    String ACCEPT = "Accept";

    @PUT
    @Path("{id}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Getting Pending Android Device Operations",
            responseContainer = "List",
            notes = "The Android agent communicates with the server to get the operations that are queued up " +
                    "at the server end for a given device using this REST API",
            response = Operation.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of pending operations"),
            @ApiResponse(code = 500, message = "Issue in retrieving operation management service instance")
    })
    Response getPendingOperations(
            @ApiParam(name = "acceptHeader", value = "Accept Header") @HeaderParam(ACCEPT) String acceptHeader,
            @ApiParam(name = "id", value = "DeviceIdentifier") @PathParam("id") String id,
            @ApiParam(name = "resultOperations", value = "Device Operation Status")
                    List<? extends Operation> resultOperations);

    @POST
    @Path("lock")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a Device Lock on Android Devices",
            notes = "Using this API you have the option of hard locking an Android device, where the Administrator " +
                    "permanently locks the device or screen locking an Android device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response configureDeviceLock(@ApiParam(name = "acceptHeader", value = "Accept Header")
                             @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "cameraBeanWrapper",
            value = "Device lock configurations with device IDs") DeviceLockBeanWrapper deviceLockBeanWrapper);

    @POST
    @Path("unlock")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a Device Unlock on Android Devices",
            responseContainer = "List",
            notes = "Using this API you have the option of unlocking an Android device, where the Administrator " +
                    "unlocks the device",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response configureDeviceUnlock(@ApiParam(name = "acceptHeader", value = "Accept Header")
                                 @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "deviceIDs", value =
            "DeviceIds to be enable device unlock operation")
                                         List<String> deviceIDs);


    @POST
    @Path("location")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Requesting Location Coordinates of Android Devices",
            responseContainer = "List",
            notes = "Request location coordinates of Android devices",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Requested Device Coordinates"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")})
    Response getDeviceLocation(@ApiParam(name = "acceptHeader", value = "Accept Header")
                               @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "deviceIDs",
            value = "DeviceIDs to be requested to get device location")
                                       List<String> deviceIDs);

    @POST
    @Path("clear-password")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Clearing the Password on Android Devices",
            notes = "Clear the password on Android devices"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response removePassword(@ApiParam(name = "acceptHeader", value = "Accept Header")
                            @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "deviceIDs",
            value = "DeviceIds to be requested to remove password")
                                    List<String> deviceIDs);

    @POST
    @Path("camera")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Enabling or Disabling the Camera on Android Devices",
            notes = "Enable or disable the camera on Android devices"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in creating a new camera instance")
    })
    Response configureCamera(@ApiParam(name = "acceptHeader", value = "Accept Header")
                             @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "cameraBeanWrapper",
            value = "Camera enable/disable configurations with device IDs") CameraBeanWrapper cameraBeanWrapper);

    @POST
    @Path("device-info")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Requesting Information from Android Devices",
            notes = "Using this REST API you are able to request for Android device details. Once this REST API is" +
                    " executed it will be in the Android operation queue until the device calls the server to retrieve " +
                    "the list of operations that needs to be executed on the device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Device Information"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response getDeviceInformation(@ApiParam(name = "acceptHeader", value = "Accept Header")
                                  @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "deviceIds",
            value = "Device IDs to be requested to get device information")
                                          List<String> deviceIDs);

    @POST
    @Path("enterprise-wipe")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Enterprise Wiping Android Devices",
            notes = "Enterprise wipe is the process of deleting enterprise related data on a device while keeping the " +
                    "personal data intact. You are able to enterprise wipe Android devices using this REST API"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance"
            )})
    Response wipeDevice(@ApiParam(name = "acceptHeader", value = "Accept Header")
                        @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "deviceIDs",
            value = "Device IDs to be requested to done enterprise-wipe")
                                List<String> deviceIDs);

    @POST
    @Path("wipe-data")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Factory Resetting an Android Device",
            notes = "Factory rest or erases all the data stored in the Android devices " +
                    "to restore them back to the original system"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")})
    Response wipeData(@ApiParam(name = "acceptHeader", value = "Accept Header")
                      @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "wipeDataBeanWrapper",
            value = "Configurations and DeviceIds to be need to done wipe-data")
                              WipeDataBeanWrapper wipeDataBeanWrapper);

    @POST
    @Path("application-list")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Requesting the List of Installed Applications on Android Devices",
            notes = "Using this REST API the server requests for the list of applications that are installed on" +
                    " the Android devices. Once this REST API is executed it will be in the Android operation queue " +
                    "until the device calls the server to retrieve the list of operations that needs to be executed " +
                    "on the device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of applications for specific deviceIdentifier"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response getApplications(@ApiParam(name = "acceptHeader", value = "Accept Header")
                             @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "deviceIDs",
            value = "Device Ids to be need to get applications which already installed")
                                     List<String> deviceIDs);

    @POST
    @Path("ring-device")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Ringing Android Devices",
            notes = "Ring Android devices"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response ringDevice(@ApiParam(name = "acceptHeader", value = "Accept Header")
                        @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "deviceIDs",
            value = "Device Ids needs to be ring") List<String> deviceIDs);

    @POST
    @Path("reboot-device")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Rebooting Android Devices",
            notes = "Reboot Android devices"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response rebootDevice(@ApiParam(name = "acceptHeader", value = "Accept Header")
                        @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "deviceIDs",
            value = "Device Ids needs to be rebooted") List<String> deviceIDs);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Muting Android Devices",
            notes = "Mute Android devices"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    @Path("mute")
    Response muteDevice(@ApiParam(name = "acceptHeader", value = "Accept Header")
                        @HeaderParam(ACCEPT) String acceptHeader,
                        @ApiParam(name = "deviceIDs", value = "DeviceIDs need to be muted") List<String> deviceIDs);

    @POST
    @Path("install-application")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Installing an Application on Android Devices",
            notes = "Install an application on an Android device. If the device you are installing the application" +
                    " on has the WSO2 system service installed, the application installation will happen in silent " +
                    "mode, else the device user's consent will be required"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response installApplication(@ApiParam(name = "acceptHeader", value = "Accept Header")
                                @HeaderParam(ACCEPT) String acceptHeader,
                                @ApiParam(name = "applicationInstallationBeanWrapper",
                                        value = "Properties of installed apps and device IDs")
                                        ApplicationInstallationBeanWrapper applicationInstallationBeanWrapper);

    @POST
    @Path("update-application")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Updating an Application on Android Devices",
            notes = "Update an application on an Android device. If the device you are updating the application" +
                    " has the WSO2 system service installed, the application update will happen in silent " +
                    "mode, else the device user's consent will be required"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response updateApplication(@ApiParam(name = "acceptHeader", value = "Accept Header")
                                @HeaderParam(ACCEPT) String acceptHeader,
                                @ApiParam(name = "applicationUpdateBeanWrapper",
                                        value = "Properties of updated apps and device IDs")
                                        ApplicationUpdateBeanWrapper applicationUpdateBeanWrapper);

    @POST
    @Path("uninstall-application")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Uninstalling an Application from Android Devices",
            notes = "Uninstall an application from Android devices"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response uninstallApplication(@ApiParam(name = "acceptHeader", value = "Accept Header")
                                  @HeaderParam(ACCEPT) String acceptHeader,
                                  @ApiParam(name = "applicationUninstallationBeanWrapper",
                                          value = "applicationUninstallationConfigs and Device Ids")
                                          ApplicationUninstallationBeanWrapper applicationUninstallationBeanWrapper);

    @POST
    @Path("blacklist-applications")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Get BlackListed Applications",
            notes = "Getting BlackListed Applications"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response blacklistApplications(@ApiParam(name = "acceptHeader", value = "Accept Header")
                                   @HeaderParam(ACCEPT) String acceptHeader,
                                   @ApiParam(name = "blacklistApplicationsBeanWrapper",
                                           value = "BlacklistApplications Configuration and DeviceIds")
                                           BlacklistApplicationsBeanWrapper blacklistApplicationsBeanWrapper);

    @POST
    @Path("upgrade-firmware")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Upgrading device firmware",
            notes = "Device firmware upgrade"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response upgradeFirmware(@ApiParam(name = "acceptHeader", value = "Accept Header")
                                   @HeaderParam(ACCEPT) String acceptHeader,
                                   @ApiParam(name = "upgradeFirmwareBeanWrapper",
                                           value = "Firmware upgrade configuration and DeviceIds")
                                           UpgradeFirmwareBeanWrapper upgradeFirmwareBeanWrapper);

    @POST
    @Path("vpn")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Configuring VPN on Android devices",
            notes = "Configure VPN on Android devices"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response configureVPN(@ApiParam(name = "acceptHeader", value = "Accept Header")
                             @HeaderParam(ACCEPT) String acceptHeader,
                             @ApiParam(name = "vpnBeanWrapper",
                                     value = "VPN configuration and DeviceIds")
                                     VpnBeanWrapper vpnBeanWrapper);

    @POST
    @Path("notification")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Sending a Notification to Android Devices",
            notes = "Send a notification to Android devices"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response sendNotification(@ApiParam(name = "acceptHeader", value = "Accept Header")
                              @HeaderParam(ACCEPT) String acceptHeader,
                              @ApiParam(name = "notificationBeanWrapper",
                                      value = "Notification Configurations and device Ids")
                                      NotificationBeanWrapper notificationBeanWrapper);

    @POST
    @Path("wifi")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Configuring Wi-Fi on Android Devices",
            notes = "Configure Wi-Fi on Android devices"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response configureWifi(@ApiParam(name = "acceptHeader", value = "Accept Header")
                           @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "wifiBeanWrapper",
            value = "WifiConfigurations and Device Ids") WifiBeanWrapper wifiBeanWrapper);

    @POST
    @Path("encrypt")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Encrypting Storage on Android Devices",
            notes = "Encrypt the data stored on Android devices"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response encryptStorage(@ApiParam(name = "acceptHeader", value = "Accept Header")
                            @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "encryptionBeanWrapper",
            value = "Configurations and deviceIds need to be done data encryption")
                                    EncryptionBeanWrapper encryptionBeanWrapper);

    @POST
    @Path("change-lock-code")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Changing the Lock Code on Android Devices",
            notes = "Change the lock code on Android devices"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response changeLockCode(@ApiParam(name = "acceptHeader", value = "Accept Header")
                            @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "lockCodeBeanWrapper",
            value = "Configurations and device Ids need to be done change lock code")
                                    LockCodeBeanWrapper lockCodeBeanWrapper);

    @POST
    @Path("password-policy")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Setting a Passcode Policy on Android Devices",
            notes = "Set a password policy on Android devices"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "created"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response setPasswordPolicy(@ApiParam(name = "acceptHeader", value = "Accept Header")
                               @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "passwordPolicyBeanWrapper",
            value = "Password Policy Configurations and Device Ids")
                                       PasswordPolicyBeanWrapper passwordPolicyBeanWrapper);

    @POST
    @Path("webclip")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Setting a Web Clip on Android Devices",
            notes = "Set a web clip on Android devices. A web clip is used to add a bookmark to a web application"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Accepted"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response setWebClip(@ApiParam(name = "acceptHeader", value = "Accept Header")
                        @HeaderParam(ACCEPT) String acceptHeader, @ApiParam(name = "webClipBeanWrapper",
            value = "Configurations to need set web clip on device and device Ids")
                                WebClipBeanWrapper webClipBeanWrapper);

    @POST
    @Path("disenroll")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Un-Register The Device from the EMM server",
            notes = "unregister the given device"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Accepted"),
            @ApiResponse(code = 500, message = "Issue in retrieving device management service instance")
    })
    Response setDisenrollment(@ApiParam(name = "acceptHeader", value = "Accept Header")
                              @HeaderParam(ACCEPT) String acceptHeader,
                              @ApiParam(name = "disenrollmentBeanWrapper", value = "Dis-enrollment configurations")
                                      DisenrollmentBeanWrapper disenrollmentBeanWrapper);
}
