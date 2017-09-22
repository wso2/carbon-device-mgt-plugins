/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.andes.extensions.device.mgt.jaxrs.beans;

import java.util.Date;

/**
 * This class holds required parameters for a querying a paginated device response.
 */
public class TopicPaginationRequest {

    private int startIndex;
    private int rowCount;
    private Date since;
    private String topic_name;
    private int remaining_messages;
    private String active;
    private String durable;
    private String subscriber_name;
    private String identifier;

    public TopicPaginationRequest(int start, int rowCount) {
        this.startIndex = start;
        this.rowCount = rowCount;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public String getTopic_name() {
        return topic_name;
    }

    public void setTopic_name(String topic_name) {
        this.topic_name = topic_name;
    }

    public int getRemaining_messages() {
        return remaining_messages;
    }

    public void setRemaining_messages(int remaining_messages) {
        this.remaining_messages = remaining_messages;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getDurable() {
        return durable;
    }

    public void setDurable(String durable) {
        this.durable = durable;
    }

    public String getSubscriber_name() {
        return subscriber_name;
    }

    public void setSubscriber_name(String subscriber_name) {
        this.subscriber_name = subscriber_name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
