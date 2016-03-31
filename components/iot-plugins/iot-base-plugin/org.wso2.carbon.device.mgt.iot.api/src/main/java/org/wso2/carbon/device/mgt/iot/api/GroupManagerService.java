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

package org.wso2.carbon.device.mgt.iot.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupUser;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.iot.util.APIUtil;
import org.wso2.carbon.device.mgt.iot.util.ResponsePayload;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

public class GroupManagerService {

    private static final String DEFAULT_ADMIN_ROLE = "admin";
    private static final String DEFAULT_OPERATOR_ROLE = "invoke-device-operations";
    private static final String DEFAULT_STATS_MONITOR_ROLE = "view-statistics";
    private static final String DEFAULT_VIEW_POLICIES = "view-policies";
    private static final String DEFAULT_MANAGE_POLICIES = "mange-policies";
    private static final String DEFAULT_VIEW_EVENTS = "view-events";
    private static final String[] DEFAULT_ADMIN_PERMISSIONS = {"/permission/device-mgt/admin/groups",
                                                               "/permission/device-mgt/user/groups"};
    private static final String[] DEFAULT_OPERATOR_PERMISSIONS = {"/permission/device-mgt/user/groups/device_operation"};
    private static final String[] DEFAULT_STATS_MONITOR_PERMISSIONS = {"/permission/device-mgt/user/groups/device_monitor"};
    private static final String[] DEFAULT_MANAGE_POLICIES_PERMISSIONS = {"/permission/device-mgt/user/groups/device_policies/add"};
    private static final String[] DEFAULT_VIEW_POLICIES_PERMISSIONS = {"/permission/device-mgt/user/groups/device_policies/view"};
    private static final String[] DEFAULT_VIEW_EVENTS_PERMISSIONS = {"/permission/device-mgt/user/groups/device_events"};

    private static Log log = LogFactory.getLog(GroupManagerService.class);

    @Path("/groups")
    @POST
    @Produces("application/json")
    public Response createGroup(@FormParam("groupName") String groupName,
                                @FormParam("userName") String username,
                                @FormParam("description") String description) {
        DeviceGroup group = new DeviceGroup();
        group.setName(username);
        group.setDescription(description);
        group.setOwner(username);
        group.setDateOfCreation(new Date().getTime());
        group.setDateOfLastUpdate(new Date().getTime());
        boolean isAdded = false;
        try {
            GroupManagementProviderService groupManagementService = APIUtil.getGroupManagementProviderService();
            int groupId = groupManagementService.createGroup(group, DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
            if (groupId == -2) {
                ResponsePayload responsePayload = new ResponsePayload();
                responsePayload.setStatusCode(HttpStatus.SC_CONFLICT);
                responsePayload.setMessageFromServer("Group name is already exists.");
                responsePayload.setResponseContent("CONFLICT");
                return Response.status(HttpStatus.SC_CONFLICT).entity(responsePayload).build();
            } else {
                isAdded = (groupId > 0) && groupManagementService.addGroupSharingRole(username, groupId,
                                                                                      DEFAULT_OPERATOR_ROLE,
                                                                                      DEFAULT_OPERATOR_PERMISSIONS);
                groupManagementService.addGroupSharingRole(username, groupId, DEFAULT_STATS_MONITOR_ROLE,
                                                           DEFAULT_STATS_MONITOR_PERMISSIONS);
                groupManagementService.addGroupSharingRole(username, groupId, DEFAULT_VIEW_POLICIES,
                                                           DEFAULT_VIEW_POLICIES_PERMISSIONS);
                groupManagementService.addGroupSharingRole(username, groupId, DEFAULT_MANAGE_POLICIES,
                                                           DEFAULT_MANAGE_POLICIES_PERMISSIONS);
                groupManagementService.addGroupSharingRole(username, groupId, DEFAULT_VIEW_EVENTS,
                                                           DEFAULT_VIEW_EVENTS_PERMISSIONS);
                ResponsePayload responsePayload = new ResponsePayload();
                responsePayload.setStatusCode(HttpStatus.SC_OK);
                return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();
            }
        } catch (GroupManagementException e) {
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Path("/groups/{groupId}")
    @PUT
    @Produces("application/json")
    public Response updateGroup(@PathParam("groupId") int groupId, @FormParam("groupName") String groupName,
                                @FormParam("userName") String userName,
                                @FormParam("description") String description) {
        try {
            GroupManagementProviderService groupManagementService = APIUtil.getGroupManagementProviderService();
            DeviceGroup group = groupManagementService.getGroup(groupId);
            group.setName(groupName);
            group.setDescription(description);
            group.setOwner(userName);
            group.setDateOfLastUpdate(new Date().getTime());
            Response.status(Response.Status.OK.getStatusCode());
            groupManagementService.updateGroup(group);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (GroupManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("/groups/{groupId}")
    @DELETE
    @Produces("application/json")
    public Response deleteGroup(@PathParam("groupId") int groupId, @QueryParam("userName") String userName) {

        if (!checkAuthorize(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/delete")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            boolean isDeleted = APIUtil.getGroupManagementProviderService().deleteGroup(
                    groupId);
            if (isDeleted) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("/groups/{groupId}")
    @GET
    @Produces("application/json")
    public Response getGroup(@PathParam("groupId") int groupId) {
        try {
            DeviceGroup deviceGroup = APIUtil.getGroupManagementProviderService().getGroup(
                    groupId);
            if (deviceGroup != null) {
                return Response.status(Response.Status.OK).entity(deviceGroup).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("/groups/search")
    @GET
    @Produces("application/json")
    public Response findGroups(@QueryParam("groupName") String groupName,
                               @QueryParam("userName") String userName) {
        try {
            List<DeviceGroup> groups = APIUtil.getGroupManagementProviderService()
                    .findInGroups(groupName, userName);
            DeviceGroup[] deviceGroups = new DeviceGroup[groups.size()];
            groups.toArray(deviceGroups);
            return Response.status(Response.Status.OK).entity(deviceGroups).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("/groups")
    @GET
    @Produces("application/json")
    public Response getGroups(@QueryParam("userName") String userName,
                              @QueryParam("permission") String permission) {
        try {
            GroupManagementProviderService groupManagementService = APIUtil.getGroupManagementProviderService();
            List<DeviceGroup> groups;
            if (permission != null) {
                groups = groupManagementService.getGroups(userName, permission);
            } else {
                groups = groupManagementService.getGroups(userName);
            }
            DeviceGroup[] deviceGroups = new DeviceGroup[groups.size()];
            groups.toArray(deviceGroups);
            return Response.status(Response.Status.OK).entity(deviceGroups).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("/groups/count")
    @GET
    @Produces("application/json")
    public Response getGroupCount(@QueryParam("userName") String userName) {
        try {
            int count = APIUtil.getGroupManagementProviderService().getGroupCount(userName);
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("/groups/{groupId}/share")
    @PUT
    @Produces("application/json")
    public Response shareGroup(@FormParam("userName") String userName,
                               @FormParam("shareUser") String shareUser, @PathParam("groupId") int groupId,
                               @FormParam("roleName") String sharingRole) {
        if (!checkAuthorize(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/share")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            boolean isShared = APIUtil.getGroupManagementProviderService().shareGroup(
                    shareUser, groupId, sharingRole);
            if (isShared) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("/groups/{groupId}/unshare")
    @PUT
    @Produces("application/json")
    public Response unShareGroup(@FormParam("userName") String userName,
                                 @FormParam("unShareUser") String unShareUser,
                                 @PathParam("groupId") int groupId,
                                 @FormParam("roleName") String sharingRole) {
        if (!checkAuthorize(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/share")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            boolean isUnShared = APIUtil.getGroupManagementProviderService().unshareGroup(
                    unShareUser, groupId, sharingRole);
            if (isUnShared) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("/groups/{groupId}/share/roles/{roleName}/permissions")
    @PUT
    @Produces("application/json")
    public Response addSharing(@QueryParam("userName") String userName, @PathParam("groupId") int groupId,
                               @PathParam("roleName") String roleName,
                               @FormParam("permissions") String[] permissions) {
        if (!checkAuthorize(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/share")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            boolean isAdded = APIUtil.getGroupManagementProviderService().addGroupSharingRole(
                    userName, groupId, roleName, permissions);
            if (isAdded) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/groups/{groupId}/share/roles/{roleName}/permissions")
    @Produces("application/json")
    public Response removeSharing(@QueryParam("userName") String userName, @PathParam("groupId") int groupId,
                                  @PathParam("roleName") String roleName) {
        if (!checkAuthorize(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/share")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            boolean isRemoved = APIUtil.getGroupManagementProviderService().removeGroupSharingRole(
                    groupId, roleName);
            if (isRemoved) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/groups/{groupId}/share/roles")
    @Produces("application/json")
    public Response getRoles(@PathParam("groupId") int groupId, @QueryParam("userName") String userName) {
        try {
            List<String> roles;
            if (userName != null && !userName.isEmpty()) {
                roles = APIUtil.getGroupManagementProviderService().getRoles(userName,
                                                                                               groupId);
            } else {
                roles = APIUtil.getGroupManagementProviderService().getRoles(groupId);
            }
            String[] rolesArray = new String[roles.size()];
            roles.toArray(rolesArray);
            return Response.status(Response.Status.OK).entity(rolesArray).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/groups/{groupId}/users")
    @Produces("application/json")
    public Response getUsers(@PathParam("groupId") int groupId) {
        try {
            List<GroupUser> users = APIUtil.getGroupManagementProviderService().getUsers(
                    groupId);
            GroupUser[] usersArray = new GroupUser[users.size()];
            users.toArray(usersArray);
            return Response.status(Response.Status.OK).entity(usersArray).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/groups/{groupId}/devices/all")
    @Produces("application/json")
    public Response getDevices(@PathParam("groupId") int groupId) {
        try {
            List<Device> devices = APIUtil.getGroupManagementProviderService().getDevices(
                    groupId);
            Device[] deviceArray = new Device[devices.size()];
            devices.toArray(deviceArray);
            return Response.status(Response.Status.OK).entity(deviceArray).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/groups/{groupId}/devices/count")
    @Produces("application/json")
    public Response getDeviceCount(@PathParam("groupId") int groupId) {
        try {
            int count = APIUtil.getGroupManagementProviderService().getDeviceCount(groupId);
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("/groups/{groupId}/devices/{deviceType}/{deviceId}")
    @Produces("application/json")
    public Response addDevice(@PathParam("groupId") int groupId, @PathParam("deviceId") String deviceId,
                              @PathParam("deviceType") String deviceType,
                              @FormParam("userName") String userName) {
        if (!checkAuthorize(getCurrentUserName(), groupId,
                            "/permission/device-mgt/admin/groups/add_devices")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            boolean isAdded = APIUtil.getGroupManagementProviderService().addDevice(
                    deviceIdentifier, groupId);
            if (isAdded) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/groups/{groupId}/devices/{deviceType}/{deviceId}")
    @Produces("application/json")
    public Response removeDevice(@PathParam("groupId") int groupId, @PathParam("deviceId") String deviceId,
                                 @PathParam("deviceType") String deviceType) {
        if (!checkAuthorize(getCurrentUserName(), groupId,
                            "/permission/device-mgt/admin/groups/remove_devices")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            boolean isRemoved = APIUtil.getGroupManagementProviderService().removeDevice(
                    deviceIdentifier, groupId);
            if (isRemoved) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/groups/{groupId}/users/{userName}/permissions")
    @Produces("application/json")
    public Response getPermissions(@PathParam("userName") String userName,
                                   @PathParam("groupId") int groupId) {
        try {
            String[] permissions = APIUtil.getGroupManagementProviderService()
                    .getPermissions(userName, groupId);
            return Response.status(Response.Status.OK).entity(permissions).build();
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/groups/{groupId}/users/{userName}/authorized")
    @Produces("application/json")
    public Response isAuthorized(@PathParam("userName") String userName, @PathParam("groupId") int groupId,
                                 @QueryParam("permission") String permission) {
        boolean isAuthorized = checkAuthorize(userName, groupId, permission);
        if (isAuthorized) {
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean checkAuthorize(String userName, int groupId, String permission) {
        try {
            return APIUtil.getGroupManagementProviderService().isAuthorized(userName, groupId, permission);
        } catch (GroupManagementException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private String getCurrentUserName() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
    }

}