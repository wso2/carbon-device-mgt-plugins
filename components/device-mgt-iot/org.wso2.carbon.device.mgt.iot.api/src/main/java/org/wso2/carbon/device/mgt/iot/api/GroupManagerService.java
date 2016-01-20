/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.group.common.DeviceGroup;
import org.wso2.carbon.device.mgt.group.common.GroupManagementException;
import org.wso2.carbon.device.mgt.group.common.GroupUser;
import org.wso2.carbon.device.mgt.group.core.providers.GroupManagementServiceProvider;
import org.wso2.carbon.device.mgt.iot.AbstractManagerService;

import javax.jws.WebService;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@WebService
public class GroupManagerService extends AbstractManagerService {

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
    @Context  //injected response proxy supporting multiple threads
    private HttpServletResponse response;

    @Path("/groups")
    @POST
    @Produces("application/json")
    public boolean createGroup(@FormParam("name") String name, @FormParam("userName") String userName,
                               @FormParam("description") String description) {
        DeviceGroup group = new DeviceGroup();
        group.setName(name);
        group.setDescription(description);
        group.setOwner(userName);
        group.setDateOfCreation(new Date().getTime());
        group.setDateOfLastUpdate(new Date().getTime());
        boolean isAdded = false;
        try {
            GroupManagementServiceProvider groupManagementService = this.getServiceProvider(GroupManagementServiceProvider.class);
            int groupId = groupManagementService.createGroup(group, DEFAULT_ADMIN_ROLE,
                                                             DEFAULT_ADMIN_PERMISSIONS);
            response.setStatus(Response.Status.OK.getStatusCode());
            isAdded = (groupId > 0) && groupManagementService.addSharing(userName, groupId, DEFAULT_OPERATOR_ROLE,
                                                                         DEFAULT_OPERATOR_PERMISSIONS);
            groupManagementService.addSharing(userName, groupId, DEFAULT_STATS_MONITOR_ROLE,
                                              DEFAULT_STATS_MONITOR_PERMISSIONS);
            groupManagementService.addSharing(userName, groupId, DEFAULT_VIEW_POLICIES,
                                              DEFAULT_VIEW_POLICIES_PERMISSIONS);
            groupManagementService.addSharing(userName, groupId, DEFAULT_MANAGE_POLICIES,
                                              DEFAULT_MANAGE_POLICIES_PERMISSIONS);
            groupManagementService.addSharing(userName, groupId, DEFAULT_VIEW_EVENTS,
                                              DEFAULT_VIEW_EVENTS_PERMISSIONS);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isAdded;
    }

    @Path("/groups/{groupId}")
    @PUT
    @Produces("application/json")
    public boolean updateGroup(@PathParam("groupId") int groupId, @FormParam("name") String name,
                               @FormParam("userName") String userName, @FormParam("description") String description) {
        if (!isAuthorized(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/modify")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            return false;
        }
        try {
            GroupManagementServiceProvider groupManagementService = this.getServiceProvider(GroupManagementServiceProvider.class);
            DeviceGroup group = groupManagementService.getGroup(groupId);
            group.setName(name);
            group.setDescription(description);
            group.setOwner(userName);
            group.setDateOfLastUpdate(new Date().getTime());
            response.setStatus(Response.Status.OK.getStatusCode());
            groupManagementService.updateGroup(group);
            return true;
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
            return false;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/groups/{groupId}")
    @DELETE
    @Produces("application/json")
    public boolean deleteGroup(@PathParam("groupId") int groupId, @QueryParam("userName") String userName) {
        if (!isAuthorized(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/delete")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            return false;
        }
        boolean isDeleted = false;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            isDeleted = this.getServiceProvider(GroupManagementServiceProvider.class).deleteGroup(
                    groupId);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isDeleted;
    }

    @Path("/groups/{groupId}")
    @GET
    @Produces("application/json")
    public DeviceGroup getGroup(@PathParam("groupId") int groupId) {
        DeviceGroup deviceGroup = null;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            deviceGroup = this.getServiceProvider(GroupManagementServiceProvider.class).getGroup(groupId);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return deviceGroup;
    }

    //get groups for autocomplete
    @Path("/groups/search")
    @GET
    @Produces("application/json")
    public DeviceGroup[] findGroups(@QueryParam("groupName") String groupName, @QueryParam("userName") String userName) {
        DeviceGroup[] deviceGroups = null;
        try {
            List<DeviceGroup> groups = this.getServiceProvider(GroupManagementServiceProvider.class).findGroups(groupName, userName);
            deviceGroups = new DeviceGroup[groups.size()];
            response.setStatus(Response.Status.OK.getStatusCode());
            groups.toArray(deviceGroups);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return deviceGroups;
    }

    //get groups by an specific permision
    @Path("/groups")
    @GET
    @Produces("application/json")
    public DeviceGroup[] getGroups(@QueryParam("userName") String userName,
                                   @QueryParam("permission") String permission) {
        DeviceGroup[] deviceGroups = null;
        try {
            GroupManagementServiceProvider groupManagementService = this.getServiceProvider(GroupManagementServiceProvider.class);
            List<DeviceGroup> groups;
            if(permission != null){
                groups = groupManagementService.getGroups(userName, permission);
            }else{
                groups = groupManagementService.getGroups(userName);
            }
            deviceGroups = new DeviceGroup[groups.size()];
            response.setStatus(Response.Status.OK.getStatusCode());
            groups.toArray(deviceGroups);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return deviceGroups;
    }

    @Path("/groups/count")
    @GET
    @Produces("application/json")
    public int getGroupCount(@QueryParam("userName") String userName) {
        int count = -1;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            count = this.getServiceProvider(GroupManagementServiceProvider.class).getGroupCount(
                    userName);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return count;
    }

    @Path("/groups/{groupId}/share")
    @POST
    @Produces("application/json")
    public boolean shareGroup(@FormParam("userName") String userName, @FormParam("shareUser") String shareUser,
                              @PathParam("groupId") int groupId, @FormParam("roleName") String sharingRole) {
        if (!isAuthorized(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/share")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            return false;
        }
        boolean isShared = false;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            isShared = this.getServiceProvider(GroupManagementServiceProvider.class).shareGroup(
                    shareUser, groupId, sharingRole);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isShared;
    }

    @Path("/groups/{groupId}/unshare")
    @POST
    @Produces("application/json")
    public boolean unShareGroup(@FormParam("userName") String userName, @FormParam("unShareUser") String unShareUser,
                                @PathParam("groupId") int groupId, @FormParam("roleName") String sharingRole) {
        if (!isAuthorized(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/share")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            return false;
        }
        boolean isUnShared = false;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            isUnShared = this.getServiceProvider(GroupManagementServiceProvider.class).unShareGroup(
                    unShareUser, groupId, sharingRole);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isUnShared;
    }

    //add sharing permissions
    @Path("/groups/{groupId}/share/roles/{roleName}/permissions")
    @POST
    @Produces("application/json")
    public boolean addSharing(@QueryParam("userName") String userName, @PathParam("groupId") int groupId,
                              @PathParam("roleName") String roleName, @FormParam("permissions") String[] permissions) {
        if (!isAuthorized(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/share")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            return false;
        }
        boolean isAdded = false;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            isAdded = this.getServiceProvider(GroupManagementServiceProvider.class).addSharing(
                    userName, groupId, roleName, permissions);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isAdded;
    }

    //remove sharing permissions
    @Path("/groups/{groupId}/share/roles/{roleName}/permissions")
    @DELETE
    @Produces("application/json")
    public boolean removeSharing(@QueryParam("userName") String userName, @PathParam("groupId") int groupId,
                                 @PathParam("roleName") String roleName) {
        if (!isAuthorized(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/share")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
        }
        boolean isRemoved = false;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            isRemoved = this.getServiceProvider(GroupManagementServiceProvider.class).removeSharing(groupId, roleName);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isRemoved;
    }

    @Path("/groups/{groupId}/share/roles")
    @GET
    @Produces("application/json")
    public String[] getRoles(@PathParam("groupId") int groupId, @QueryParam("userName") String userName) {
        String[] rolesArray = null;
        try {
            List<String> roles = null;
            if(userName != null && !userName.isEmpty()) {
                roles = this.getServiceProvider(GroupManagementServiceProvider.class).getRoles(
                        userName, groupId);
            }else {
                roles = this.getServiceProvider(GroupManagementServiceProvider.class).getRoles(groupId);
            }
            rolesArray = new String[roles.size()];
            response.setStatus(Response.Status.OK.getStatusCode());
            roles.toArray(rolesArray);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return rolesArray;
    }

    @Path("/groups/{groupId}/users")
    @GET
    @Produces("application/json")
    public GroupUser[] getUsers(@PathParam("groupId") int groupId) {
        GroupUser[] usersArray = null;
        try {
            List<GroupUser> users = this.getServiceProvider(GroupManagementServiceProvider.class).getUsers(groupId);
            usersArray = new GroupUser[users.size()];
            response.setStatus(Response.Status.OK.getStatusCode());
            users.toArray(usersArray);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return usersArray;
    }

    @Path("/groups/{groupId}/devices/all")
    @GET
    @Produces("application/json")
    public Device[] getDevices(@PathParam("groupId") int groupId) {
        Device[] deviceArray = null;
        try {
            List<Device> devices = this.getServiceProvider(GroupManagementServiceProvider.class).getDevices(groupId);
            deviceArray = new Device[devices.size()];
            response.setStatus(Response.Status.OK.getStatusCode());
            devices.toArray(deviceArray);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return deviceArray;
    }

    @Path("/groups/{groupId}/devices/count")
    @GET
    @Produces("application/json")
    public int getDeviceCount(@PathParam("groupId") int groupId) {
        try {
            return this.getServiceProvider(GroupManagementServiceProvider.class).getDeviceCount(groupId);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
            return -1;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/groups/{groupId}/devices")
    @GET
    @Produces("application/json")
    public PaginationResult getDevices(@PathParam("groupId") int groupId,
                                       @QueryParam("index") int index,
                                       @QueryParam("limit") int limit) {
        try {
            return this.getServiceProvider(GroupManagementServiceProvider.class).getDevices(groupId, index, limit);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
            return null;
        } finally {
            this.endTenantFlow();
        }
    }

    @Path("/groups/{groupId}/devices/{deviceType}/{deviceId}")
    @PUT
    @Produces("application/json")
    public boolean addDevice(@PathParam("groupId") int groupId, @PathParam("deviceId") String deviceId,
                             @PathParam("deviceType") String deviceType, @FormParam("userName") String userName) {
        if (!isAuthorized(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/add_devices")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            return false;
        }
        boolean isAdded = false;
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            response.setStatus(Response.Status.OK.getStatusCode());
            isAdded = this.getServiceProvider(GroupManagementServiceProvider.class).addDevice(deviceIdentifier, groupId);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isAdded;
    }

    @Path("/groups/{groupId}/devices/{deviceType}/{deviceId}")
    @DELETE
    @Produces("application/json")
    public boolean removeDevice(@PathParam("groupId") int groupId, @PathParam("deviceId") String deviceId,
                                @PathParam("deviceType") String deviceType) {
        if (!isAuthorized(getCurrentUserName(), groupId, "/permission/device-mgt/admin/groups/remove_devices")){
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            return false;
        }
        boolean isRemoved = false;
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            response.setStatus(Response.Status.OK.getStatusCode());
            isRemoved = this.getServiceProvider(GroupManagementServiceProvider.class).removeDevice(deviceIdentifier, groupId);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isRemoved;
    }

    @Path("/groups/{groupId}/users/{userName}/permissions")
    @GET
    @Produces("application/json")
    public String[] getPermissions(@PathParam("userName") String userName, @PathParam("groupId") int groupId) {
        String[] permissions = null;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            permissions = this.getServiceProvider(GroupManagementServiceProvider.class).getPermissions(userName, groupId);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return permissions;
    }

    @Path("/groups/{groupId}/users/{userName}/authorized")
    @GET
    @Produces("application/json")
    public boolean isAuthorized(@PathParam("userName") String userName, @PathParam("groupId") int groupId,
                                @QueryParam("permission") String permission){
        boolean isAuthorized = false;
        try {
            response.setStatus(Response.Status.OK.getStatusCode());
            isAuthorized = this.getServiceProvider(GroupManagementServiceProvider.class).isAuthorized(userName, groupId, permission);
        } catch (GroupManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e.getErrorMessage(), e);
        } finally {
            this.endTenantFlow();
        }
        return isAuthorized;
    }

}