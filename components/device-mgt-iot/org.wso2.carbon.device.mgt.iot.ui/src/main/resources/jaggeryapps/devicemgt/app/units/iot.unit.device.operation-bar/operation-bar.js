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

function onRequest(context) {
    var log = new Log("operation.js");
    var operationModule = require("/app/modules/operation.js").operationModule;
    var device = context.unit.params.device;
    var controlOperations;
    try {
        controlOperations = operationModule.getControlOperations(device.type);
    }catch(e){
        log.error("Control operation loading failed.");
        controlOperations = null;
    }
    return {"control_operations": controlOperations, "device": device};
}