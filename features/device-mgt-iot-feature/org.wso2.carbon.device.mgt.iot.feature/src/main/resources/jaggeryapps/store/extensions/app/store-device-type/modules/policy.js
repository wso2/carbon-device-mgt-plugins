/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var policyModule = function () {
    var log = new Log("modules/policy.js");

    var constants = require("constants.js");
    var utility = require("utility.js").utility;

    var server = require('store').server;
    var carbonUser = server.current(session);

    var carbonModule = require('carbon');
    var hostname = utility.getIoTServerConfig("IoTMgtHost");
    var carbonHttpsServletTransport = "https://" + hostname + ":9443";

    var carbonServer = new carbonModule.server.Server({
        tenanted: true,
        url: carbonHttpsServletTransport + '/admin'
    });

    var deviceModule = require("device.js").deviceModule;

    var publicMethods = {};
    var privateMethods = {};

    publicMethods.addPolicy = function (policyName, deviceType, policyDefinition, policyDescription,
                                        deviceId) {
        log.info("adding " + policyName);
        if (policyName && deviceType) {

            var options = {system: true};

            var resource = {
                name: policyName,
                mediaType: 'text/plain',
                content: policyDefinition,
                description: policyDescription,
                properties: {owner: carbonUser.username}
            };

            var policyId = 0;

            if (carbonUser) {
                options.tenantId = carbonUser.tenantId;
                var registry = new carbonModule.registry.Registry(carbonServer, options);
                log.info("########### Policy name : " + policyName);
                log.info("########### Policy type : " + deviceType);
                log.info("########### Policy Declaration : " + policyDefinition);
                log.info("########### Policy policyDescription: " + policyDescription);
                var queName = "";
                if (deviceId) {
                    registry.put(constants.POLICY_REGISTRY_PATH + deviceType + "/" + deviceId + "/" + policyName, resource);
                    queName = "wso2/iot/" + carbonUser.username + "/" + deviceType + "/" + deviceId;
                    policyId = registry.get(constants.POLICY_REGISTRY_PATH + deviceType + "/" + deviceId + "/" + policyName, resource).uuid;
                } else {
                    registry.put(constants.POLICY_REGISTRY_PATH + deviceType + "/" + policyName, resource);
                    queName = "wso2/iot/" + carbonUser.username + "/" + deviceType;
                    policyId = registry.get(constants.POLICY_REGISTRY_PATH + deviceType + "/" + policyName, resource).uuid;
                }
            }

            var policyJSON = {
                "id": policyId,
                "type": "POLICY",
                "priority": "1",
                "reference": {
                    "deviceId": "456",
                    "deviceType": deviceType
                },
                "language": "siddhi",
                "content": policyDefinition
            };

            var mqttsenderClass = Packages.org.wso2.device.mgt.mqtt.policy.push.MqttPush;
            var mqttsender = new mqttsenderClass();
            log.info("Queue : " + queName);

            var result = mqttsender.pushToMQTT(queName, stringify(policyJSON), "tcp://192.168.67.21:1883", "Raspberry-Policy-sender");

            mqttsender = null;

            return true;

        } else {
            return false;
        }
    };

    publicMethods.getPolicies = function () {
        var options = {system: true};

        var policies = [];

        if (carbonUser) {
            options.tenantId = carbonUser.tenantId;
            var registry = new carbonModule.registry.Registry(carbonServer, options);
            var allPolicies = registry.get(constants.POLICY_REGISTRY_PATH);

            if (allPolicies) {

                //loop through all device types
                for (var i = 0; i < allPolicies.content.length; i++) {
                    var deviceType = allPolicies.content[i].replace(constants.POLICY_REGISTRY_PATH, "");
                    var deviceTypePolicies = registry.get(allPolicies.content[i]);

                    //loop through policies
                    for (var j = 0; j < deviceTypePolicies.content.length; j++) {
                        var deviceTypePolicy = registry.get(deviceTypePolicies.content[j]);

                        for (var k = 0; k < deviceTypePolicy.content.length; k++) {
                            var policy = registry.get(deviceTypePolicy.content[k]);
                            var owner = registry.properties(deviceTypePolicy.content[k]).owner;
                            var deviceId = deviceTypePolicy.id.replace(deviceTypePolicies.id + "/", "");

                            var policyObj = {
                                "id": policy.uuid,                         // Identifier of the policy.
                                //"priorityId": 1,                 // Priority of the policies. This will be used only for simple evaluation.
                                //"profile": {},                   // Profile
                                "policyName": policy.name,  // Name of the policy.
                                "updated": policy.updated.time,
                                "deviceType": deviceType,
                                "owner": owner
                                //"generic": true,                 // If true, this should be applied to all related device.
                                //"roles": {},                     // Roles which this policy should be applied.
                                //"ownershipType": {},             // Ownership type (COPE, BYOD, CPE)
                                //"devices": {},                   // Individual devices this policy should be applied
                                //"users": {},                     // Individual users this policy should be applied
                                //"Compliance": {},
                                //"policyCriterias": {},
                                //"startTime": 283468236,          // Start time to apply the policy.
                                //"endTime": 283468236,            // After this time policy will not be applied
                                //"startDate": "",                 // Start date to apply the policy
                                //"endDate": "",                   // After this date policy will not be applied.
                                //"tenantId": -1234,
                                //"profileId": 1
                            };

                            if (deviceId != 'undefined') {
                                policyObj.device = deviceModule.getDevice(deviceType, deviceId);
                            }

                            policies.push(policyObj);
                        }
                    }
                }//end of policy loop
            }//end of device type policy loop
        }

        return policies;

    };

    publicMethods.removePolicy = function (name, deviceType) {
        var options = {system: true};
        var bool = false;

        if (carbonUser) {
            options.tenantId = carbonUser.tenantId;
            var registry = new carbonModule.registry.Registry(carbonServer, options);
            log.info("########### Policy name : " + name);
            log.info("########### Policy type : " + deviceType);
            try {
                registry.remove(constants.POLICY_REGISTRY_PATH + deviceType + "/" + name);
                bool = true;
            } catch (err) {
                log.error("Error while trying to remove policy :" + name, err);
            }
        }

        return bool;
    };

    return publicMethods;
}();