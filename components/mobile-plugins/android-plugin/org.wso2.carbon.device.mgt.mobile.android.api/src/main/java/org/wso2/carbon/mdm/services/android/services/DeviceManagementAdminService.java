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
package org.wso2.carbon.mdm.services.android.services;

import io.swagger.annotations.*;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.mdm.services.android.bean.wrapper.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/admin/devices")
@Api(value = "Android Device Management Administrative Service", description = "Device management related admin APIs.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceManagementAdminService {

    @POST
    @Path("/lock-devices")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adds a Device Lock on Android Devices",
            notes = "Using this API you have the option of hard locking an Android device, where the Administrator " +
                    "permanently locks the device or screen locking an Android device",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created \n Lock operation has successfully been scheduled",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(code = 500, message = "Internal Server Error. \n Error occurred when adding operation.")
    })
    Response configureDeviceLock(
            @ApiParam(name = "deviceLockBeanWrapper",
                    value = "Device lock configurations with device IDs") DeviceLockBeanWrapper deviceLockBeanWrapper);

    @POST
    @Path("/unlock-devices")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a Device Unlock on Android Devices",
            responseContainer = "List",
            notes = "Using this API you have the option of unlocking an Android device, where the Administrator " +
                    "unlocks the device",
            response = Activity.class,
            tags = "Android Device Management Administrative Service")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added unlock.", response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n Device identifier list is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response configureDeviceUnlock(
            @ApiParam(name = "deviceIDs", value = "DeviceIds to be enable device unlock operation")
            List<String> deviceIDs);


    @POST
    @Path("/location")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Requesting Location Coordinates of Android Devices",
            responseContainer = "List",
            notes = "Request location coordinates of Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Requested Device Coordinates", response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n Device identifier list is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")})
    Response getDeviceLocation(
            @ApiParam(name = "deviceIDs", value = "DeviceIDs to be requested to get device location")
            List<String> deviceIDs);

    @POST
    @Path("/clear-password")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Clearing the Password on Android Devices",
            notes = "Clear the password on Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully cleared password.", response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n Device identifier list is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response removePassword(
            @ApiParam(name = "deviceIDs",
                    value = "DeviceIds to be requested to remove password") List<String> deviceIDs);

    @POST
    @Path("/control-camera")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Enabling or Disabling the Camera on Android Devices",
            notes = "Enable or disable the camera on Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Camera operation performed successfully.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n Invalid request or validation error."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Issue in creating a new camera instance")
    })
    Response configureCamera(
            @ApiParam(name = "cameraBeanWrapper", value = "Camera enable/disable configurations with device IDs")
            CameraBeanWrapper cameraBeanWrapper);

    @POST
    @Path("/info")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Requesting Information from Android Devices",
            notes = "Using this REST API you are able to request for Android device details. Once this REST API is" +
                    " executed it will be in the Android operation queue until the device calls the server to retrieve " +
                    "the list of operations that needs to be executed on the device",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Device information request operation added.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n Device identifier list is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response getDeviceInformation(
            @ApiParam(name = "deviceIds", value = "Device IDs to be requested to get device information")
            List<String> deviceIDs);

    @POST
    @Path("/enterprise-wipe")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Enterprise Wiping Android Devices",
            notes = "Enterprise wipe is the process of deleting enterprise related data on a device while keeping the " +
                    "personal data intact. You are able to enterprise wipe Android devices using this REST API",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Enterprise wipe operation added.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n Device identifier list is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation."
            )})
    Response wipeDevice(@ApiParam(name = "deviceIDs", value = "Device IDs to be requested to do enterprise-wipe")
                        List<String> deviceIDs);

    @POST
    @Path("/wipe")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Factory Resetting an Android Device",
            notes = "Factory rest or erases all the data stored in the Android devices " +
                    "to restore them back to the original system",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Added wipe operation", response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n WipeData bean is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")})
    Response wipeData(
            @ApiParam(name = "wipeDataBeanWrapper", value = "Configurations and DeviceIds needed to do wipe-data")
            WipeDataBeanWrapper wipeDataBeanWrapper);

    @POST
    @Path("/applications")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Requesting the List of Installed Applications on Android Devices",
            notes = "Using this REST API the server requests for the list of applications that are installed on" +
                    " the Android devices. Once this REST API is executed it will be in the Android operation queue " +
                    "until the device calls the server to retrieve the list of operations that needs to be executed " +
                    "on the device",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n List of applications for specific deviceIdentifier",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n Device identifier list is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response getApplications(
            @ApiParam(name = "deviceIDs", value = "Device Ids needed to get applications that are already installed")
            List<String> deviceIDs);

    @POST
    @Path("/ring")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Ringing Android Devices",
            notes = "Ring Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added ring operation.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n Device identifier list is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response ringDevice(
            @ApiParam(name = "deviceIDs", value = "Device Ids needed for ring") List<String> deviceIDs);

    @POST
    @Path("/reboot")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Rebooting Android Devices",
            notes = "Reboot Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added reboot operation.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n Device identifier list is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response rebootDevice(
            @ApiParam(name = "deviceIDs", value = "Device Ids needed for reboot.") List<String> deviceIDs);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Muting Android Devices",
            notes = "Mute Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added  mute operation.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n Device identifier list is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    @Path("/mute")
    Response muteDevice(
            @ApiParam(name = "deviceIDs", value = "DeviceIDs need to be muted") List<String> deviceIDs);

    @POST
    @Path("/install-application")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Installing an Application on Android Devices",
            notes = "Install an application on an Android device. If the device you are installing the application" +
                    " on has the WSO2 system service installed, the application installation will happen in silent " +
                    "mode, else the device user's consent will be required",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added  install application operation.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n applicationInstallationBeanWrapper is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response installApplication(
            @ApiParam(name = "applicationInstallationBeanWrapper", value = "Properties of installed apps and device IDs")
            ApplicationInstallationBeanWrapper applicationInstallationBeanWrapper);

    @POST
    @Path("/update-application")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Updating an Application on Android Devices",
            notes = "Update an application on an Android device. If the device you are updating the application" +
                    " has the WSO2 system service installed, the application update will happen in silent " +
                    "mode, else the device user's consent will be required",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added update application operation.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n applicationInstallationBeanWrapper is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response updateApplication(
            @ApiParam(name = "applicationUpdateBeanWrapper", value = "Properties of updated apps and device IDs")
            ApplicationUpdateBeanWrapper applicationUpdateBeanWrapper);

    @POST
    @Path("/uninstall-application")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Uninstalling an Application from Android Devices",
            notes = "Uninstall an application from Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added uninstall application operation.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n applicationInstallationBeanWrapper is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response uninstallApplication(
            @ApiParam(name = "applicationUninstallationBeanWrapper",
                    value = "applicationUninstallationConfigs and Device Ids")
            ApplicationUninstallationBeanWrapper applicationUninstallationBeanWrapper);

    @POST
    @Path("/blacklist-applications")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Get BlackListed Applications",
            notes = "Getting BlackListed Applications",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added blacklist application operation.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n blacklistApplicationsBeanWrapper is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response blacklistApplications(
            @ApiParam(name = "blacklistApplicationsBeanWrapper", value = "BlacklistApplications " +
                    "Configuration and DeviceIds")
            BlacklistApplicationsBeanWrapper blacklistApplicationsBeanWrapper);

    @POST
    @Path("/upgrade-firmware")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Upgrading device firmware",
            notes = "Device firmware upgrade",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added firmware upgrade  operation.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n upgradeFirmwareBeanWrapper is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response upgradeFirmware(
            @ApiParam(name = "upgradeFirmwareBeanWrapper",
                    value = "Firmware upgrade configuration and DeviceIds")
            UpgradeFirmwareBeanWrapper upgradeFirmwareBeanWrapper);

    @POST
    @Path("/configure-vpn")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Configuring VPN on Android devices",
            notes = "Configure VPN on Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added configure vpn operation.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n vpnBeanWrapper is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response configureVPN(
            @ApiParam(name = "vpnBeanWrapper",
                    value = "VPN configuration and DeviceIds")
            VpnBeanWrapper vpnBeanWrapper);

    @POST
    @Path("/send-notification")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Sending a Notification to Android Devices",
            notes = "Send a notification to Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added send notification operation.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n notificationBeanWrapper is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response sendNotification(
            @ApiParam(name = "notificationBeanWrapper",
                    value = "Notification Configurations and device Ids")
            NotificationBeanWrapper notificationBeanWrapper);

    @POST
    @Path("/configure-wifi")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Configuring Wi-Fi on Android Devices",
            notes = "Configure Wi-Fi on Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added configure wifi operation.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n WifiConfigurations is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response configureWifi(
            @ApiParam(name = "wifiBeanWrapper",
                    value = "WifiConfigurations and Device Ids") WifiBeanWrapper wifiBeanWrapper);

    @POST
    @Path("/encrypt-storage")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Encrypting Storage on Android Devices",
            notes = "Encrypt the data stored on Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added encrypt storage operation.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n encryptionBeanWrapper is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response encryptStorage(
            @ApiParam(name = "encryptionBeanWrapper",
                    value = "Configurations and deviceIds need to be done data encryption")
            EncryptionBeanWrapper encryptionBeanWrapper);

    @POST
    @Path("/change-lock-code")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Changing the Lock Code on Android Devices",
            notes = "Change the lock code on Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added change lock code operation.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n lockCodeBeanWrapper is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response changeLockCode(
            @ApiParam(name = "lockCodeBeanWrapper",
                    value = "Configurations and device Ids need to be done change lock code")
            LockCodeBeanWrapper lockCodeBeanWrapper);

    @POST
    @Path("/set-password-policy")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Setting a Passcode Policy on Android Devices",
            notes = "Set a password policy on Android devices",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created  \n Successfully added set passcode policy operation.",
                    response = Activity.class),
            @ApiResponse(code = 400, message = "Bad Request. \n passwordPolicyBeanWrapper is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response setPasswordPolicy(
            @ApiParam(name = "passwordPolicyBeanWrapper",
                    value = "Password Policy Configurations and Device Ids")
            PasswordPolicyBeanWrapper passwordPolicyBeanWrapper);

    @POST
    @Path("set-webclip")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Setting a Web Clip on Android Devices",
            notes = "Set a web clip on Android devices. A web clip is used to add a bookmark to a web application",
            response = Activity.class,
            tags = "Android Device Management Administrative Service"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created \n Successfully added web clip operation."),
            @ApiResponse(code = 400, message = "Bad Request. \n webClipBeanWrapper is empty."),
            @ApiResponse(code = 500, message = "Internal Server Error.  \n Error occurred when adding operation.")
    })
    Response setWebClip(
            @ApiParam(name = "webClipBeanWrapper",
                    value = "Configurations to need set web clip on device and device Ids")
            WebClipBeanWrapper webClipBeanWrapper);


}
