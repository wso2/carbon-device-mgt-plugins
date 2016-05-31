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
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.mdm.services.android.bean.DeviceState;
import org.wso2.carbon.mdm.services.android.bean.ErrorResponse;
import org.wso2.carbon.mdm.services.android.bean.wrapper.EventBeanWrapper;
import org.wso2.carbon.mdm.services.android.exception.UnexpectedServerErrorException;
import org.wso2.carbon.mdm.services.android.services.EventReceiverService;
import org.wso2.carbon.mdm.services.android.util.AndroidAPIUtils;
import org.wso2.carbon.mdm.services.android.util.Message;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/events")
public class EventReceiverServiceImpl implements EventReceiverService {

    private static final String EVENT_STREAM_DEFINITION = "android_agent";
    private static final Log log = LogFactory.getLog(EventReceiverServiceImpl.class);

    @POST
    @Path("/publish")
    @Override
    public Response publishEvents(EventBeanWrapper eventBeanWrapper)  {
        if (log.isDebugEnabled()) {
            log.debug("Invoking Android device even logging.");
        }
        Message message = new Message();

        Object payload[] = {eventBeanWrapper.getDeviceIdentifier(), eventBeanWrapper.getPayload(),
                eventBeanWrapper.getType()};
        try {
            if (AndroidAPIUtils.getEventPublisherService().publishEvent(
                    EVENT_STREAM_DEFINITION, "1.0.0", new Object[0], new Object[0], payload)) {
                message.setResponseCode("Event is published successfully.");
                return Response.status(Response.Status.CREATED).entity(message).build();
            } else {
                message.setResponseCode("Error occurred while publishing the event.");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                        entity(message).build();
            }
        } catch (DataPublisherConfigurationException e) {
            String msg = "Error occurred while getting the Data publisher Service.";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
    }

    @GET
    @Override
    public Response retrieveAlerts(@QueryParam("id") String deviceId, @QueryParam("from") long from,
                                   @QueryParam("to") long to, @QueryParam("type") String type,
                                   @HeaderParam("If-Modified-Since") String ifModifiedSince) {

        if (from != 0l && to != 0l && deviceId != null) {
            return retrieveAlertFromDate(deviceId, from, to);
        } else if (deviceId != null && type != null) {
            return retrieveAlertByType(deviceId, type);
        } else if (deviceId != null) {
            return retrieveAlert(deviceId);
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Request must contain the device identifier. " +
                    "Optionally, both from and to value should be present to get alerts between times.").build();
        }
    }

    private Response retrieveAlert(String deviceId) {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving events for given device Identifier.");
        }
        Message message = new Message();
        String query = "deviceIdentifier:" + deviceId;
        List<DeviceState> deviceStates;
        try {
            deviceStates = AndroidAPIUtils.getAllEventsForDevice(EVENT_STREAM_DEFINITION, query);
            if (deviceStates == null) {
                message.setResponseCode("No any alerts are published for Device: " + deviceId + ".");
                return Response.status(Response.Status.NOT_FOUND).entity(message).build();
            } else {
                return Response.status(Response.Status.OK).entity(deviceStates).build();
            }
        } catch (AnalyticsException e) {
            String msg = "Error occurred while getting published events for specific device: " + deviceId + ".";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
    }

    private Response retrieveAlertFromDate(String deviceId, long from, long to) {
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        if (log.isDebugEnabled()) {
            log.debug("Retrieving events for given device Identifier and time period.");
        }
        Message message = new Message();

        String query = "deviceIdentifier:" + deviceId + " AND _timestamp: [" + fromDate + " TO " + toDate + "]";
        List<DeviceState> deviceStates;
        try {
            deviceStates = AndroidAPIUtils.getAllEventsForDevice(EVENT_STREAM_DEFINITION, query);
            if (deviceStates == null) {
                message.
                        setResponseCode("No any alerts are published on given date for given Device: " + deviceId + ".");
                return Response.status(Response.Status.NOT_FOUND).entity(message).build();

            } else {
                return Response.status(Response.Status.OK).entity(deviceStates).build();
            }
        } catch (AnalyticsException e) {
            String msg = "Error occurred while getting published events for specific " +
                    "Device: " + deviceId + " on given Date.";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
    }

    private Response retrieveAlertByType(String deviceId, String type) {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving events for given device identifier and type.");
        }
        Message message = new Message();
        String query = "deviceIdentifier:" + deviceId + " AND type:" + type;
        List<DeviceState> deviceStates;
        try {
            deviceStates = AndroidAPIUtils.getAllEventsForDevice(EVENT_STREAM_DEFINITION, query);
            if (deviceStates == null) {
                message.setResponseCode("No any alerts are published for given Device: " +
                        "" + deviceId + " on specific date.");
                return Response.status(Response.Status.NOT_FOUND).entity(message).build();

            } else {
                return Response.status(Response.Status.OK).entity(deviceStates).build();
            }
        } catch (AnalyticsException e) {
            String msg = "Error occurred while getting published events for specific " +
                    "Device: " + deviceId + "and given device Type.";
            log.error(msg, e);
            throw new UnexpectedServerErrorException(
                    new ErrorResponse.ErrorResponseBuilder().setCode(500l).setMessage(msg).build());
        }
    }

}
