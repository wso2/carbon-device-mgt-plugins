/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function onRequest(context) {
    var log = new Log("license-box-agent-unit");
    log.debug("calling license-box-agent-unit backend js");

    var viewModel = {};
    var deviceModule = require("/app/modules/business-controllers/device.js")["deviceModule"];
    var mdmProps = require("/app/modules/conf-reader/main.js")["conf"];

    var license = deviceModule.getLicense(context.unit.params["platform"], context.unit.params["languageCode"]);
    if (license) {
        viewModel.license = license;
    } else {
        viewModel.license = "ERROR: Unable to retrieve License Text.";
    }

    var companyProps = session.get("COMPANY_DETAILS");
    if (!companyProps) {
        viewModel.companyName = mdmProps.generalConfig.companyName;
    } else {
        viewModel.companyName = companyProps.companyName;
    }
    return viewModel;
}