/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
var pageDecorators = {};
(function (pageDecorators) {
    var log = new Log();
    var isActivatedAsset = function (assetType, tenantId) {
        var app = require('rxt').app;
        var activatedAssets = app.getUIActivatedAssets(tenantId); //ctx.tenantConfigs.assets;
        //return true;
        if (!activatedAssets) {
            throw 'Unable to load all activated assets for current tenant: ' + tenatId + '.Make sure that the assets property is present in the tenant config';
        }
        for (var index in activatedAssets) {
            if (activatedAssets[index] == assetType) {
                return true;
            }
        }
        return false;
    };
    pageDecorators.navigationBar = function (ctx, page, utils) {
        var carbonUser = server.current(session);
        page.navigationBar = {};
        var links = {
            "users": [],
            "policies": [],
            "profiles": [],
            "devices": [],
            "device": [],
            "groups": [],
            "store": [],
            "dashboard": [],
            "analytics": [],
            "events": []
        };

        var deviceMgtLink = {
            title: "Go back to My Devices",
            icon: "fw-left-arrow",
            url: "/store/pages/devices"
        };

        var groupMgtLink = {
            title: "Go back to Groups",
            icon: "fw-left-arrow",
            url: "/store/pages/groups"
        };

        var storeLink = {
            title: "Go back to Store",
            icon: "fw-left-arrow",
            url: "/store"
        };

        var uri = request.getRequestURI();
        var uriMatcher = new URIMatcher(String(uri));

        links.profiles.push(deviceMgtLink);
        links.store.push(deviceMgtLink);
        links.store.push(storeLink);
        links.users.push(deviceMgtLink);
        links.device.push(deviceMgtLink);

        var groupId = request.getParameter("groupId");
        if (groupId) {
            links.analytics.push(groupMgtLink);
            links.devices.push(groupMgtLink);
        } else {
            links.analytics.push(deviceMgtLink);
        }

        links.devices.push({
            title: "Enroll Device",
            icon: "fw-add",
            url: "/store/assets/deviceType/list"
        });
        links.devices.push({
            title: "Add Group",
            icon: "fw-add",
            url: "/store/pages/groups/add"
        });
        links.devices.push({
            title: "Add Policy",
            icon: "fw-add",
            url: "/store/pages/policies/add"
        });


        links.groups.push(deviceMgtLink);
        if (uriMatcher.match("/{context}/pages/groups/add")) {
            links.groups.push({
                title: "Go back to Groups",
                icon: "fw-left-arrow",
                url: "/store/pages/groups"
            });
        } else {
            links.groups.push({
                title: "Add Group",
                icon: "fw-add",
                url: "/store/pages/groups/add"
            });
        }

        links.events.push(deviceMgtLink);

        links.policies.push(deviceMgtLink);
        if (uriMatcher.match("/{context}/pages/policies/add")) {
            links.policies.push({
                title: "Go back to Policies",
                icon: "fw-left-arrow",
                url: "/store/pages/policies"
            });
        } else {
            links.policies.push({
                title: "Add Policy",
                icon: "fw-add",
                url: "/store/pages/policies/add"
            });
        }

        //log.info("context.meta.pageName " + page.meta.pageName);
        page.navigationBar.currentActions = links[page.meta.pageName];
        //log.info("page.navigationBar.currentActions ");
        return page;
    };
    var assetManager = function (ctx) {
        var rxt = require('rxt');
        var type = ctx.assetType;
        var am = rxt.asset.createUserAssetManager(ctx.session, type);
        return am;
    };
}(pageDecorators));