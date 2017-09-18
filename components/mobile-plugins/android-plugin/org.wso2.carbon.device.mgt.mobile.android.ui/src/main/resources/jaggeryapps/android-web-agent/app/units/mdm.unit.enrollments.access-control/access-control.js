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
    var log = new Log("enrollment-access-control-unit backend js");
    log.debug("calling enrollment-access-control-unit");

    var mdmProps = require("/app/modules/conf-reader/main.js")["conf"];
    var UAParser = require("/app/modules/ua-parser.min.js")["UAParser"];

    var parser = new UAParser();
    var userAgent = request.getHeader("User-Agent");
    parser.setUA(userAgent);
    parser.getResult();
    var userAgentPlatform = parser.getOS()["name"];

    if (userAgentPlatform != context.unit.params["allowedPlatform"]) {
        // if userAgentPlatform is not allowed
        log.error("platform not allowed");
        response.sendRedirect(context.app.context + "/enrollments/error/unintentional-request");
    } else {
        var lastPage = context.unit.params["lastPage"];
        var nextPage = context.unit.params["nextPage"];
        var currentPage = context.unit.params["currentPage"];
        // if userAgentPlatform is allowed,
        // restricting unordered intermediate page access
        if (lastPage && currentPage && nextPage) {
            // meaning it's not first page, but a middle page
            if (!session.get("lastAccessedPage")) {
                // meaning a middle page is accessed at first
                response.sendRedirect(context.app.context + "/enrollments/error/unintentional-request");
            } else if (!(session.get("lastAccessedPage") == currentPage) &&
                !(session.get("lastAccessedPage") == lastPage) &&
                !(session.get("lastAccessedPage") == nextPage)) {
                response.sendRedirect(context.app.context + "/enrollments/error/unintentional-request");
            } else if (currentPage) {
                // if currentPage is set, update lastAccessedPage as currentPage
                session.put("lastAccessedPage", currentPage);
            }
        } else if (lastPage && currentPage && !nextPage) {
            // meaning it's not first page, not a middle page, but the last page in wizard
            if (!session.get("lastAccessedPage")) {
                // this means the last page is accessed at first
                response.sendRedirect(context.app.context + "/enrollments/error/unintentional-request");
            } else if (!(session.get("lastAccessedPage") == currentPage) &&
                !(session.get("lastAccessedPage") == lastPage)) {
                response.sendRedirect(context.app.context + "/enrollments/error/unintentional-request");
            } else if (currentPage) {
                // if currentPage is set, update lastAccessedPage as currentPage
                session.put("lastAccessedPage", currentPage);
            }
        } else if (currentPage) {
            // meaning it's the first page
            // if currentPage is set, update lastAccessedPage as currentPage
            session.put("lastAccessedPage", currentPage);
        }
    }

    if (log.isDebugEnabled()) {
        log.debug("last-accessed-page = " + session.get("lastAccessedPage") +
            " : " + "session-id = " + session.getId());
    }
    return context;
}