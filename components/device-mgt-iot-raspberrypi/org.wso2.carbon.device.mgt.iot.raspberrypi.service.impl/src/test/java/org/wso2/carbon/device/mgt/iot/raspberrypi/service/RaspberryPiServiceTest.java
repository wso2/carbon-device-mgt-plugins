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
package org.wso2.carbon.device.mgt.iot.raspberrypi.service;

import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.WebClient;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RaspberryPiServiceTest {
    private static org.apache.commons.logging.Log log = LogFactory.getLog(RaspberryPiServiceTest.class);
    ApplicationContext context;
    WebClient client;

    @BeforeClass
    public void init(){
        context = new ClassPathXmlApplicationContext("spring-cxf-client.xml");
        client = context.getBean("droneClient", WebClient.class);
    }

    //@Test
    public void register(){
        client.path("controller/register").accept(MediaType.APPLICATION_JSON_TYPE);
        client.path("admin/s1ee19tz5bn4/10.111.68.190/9763");
        Response res = client.post(null);
        log.info("Response status :"+ res.getStatus());
        System.out.println("Response status :"+ res.getStatus());
    }
    //@Test
    public void SensorRecord(){
        client.path("controller/readtemperature").accept(MediaType.APPLICATION_JSON_TYPE);
        client.header("owner","admin");
        client.header("deviceId","deviceId");
        client.header("protocol","HTTP_PROTOCOL");
        Response res = client.get();
        log.info("Response status :"+ res.getStatus());
        System.out.println("Response status :"+ res.getStatus());
    }

    //@Test
    public void switchBulb(){
        client.path("controller/bulb");
        client.header("owner","admin");
        client.header("deviceId","1iv9uchjd5lq0");
        client.header("protocol","HTTP");
        client.path("ON");
        Response res = client.post(null);
        log.info("Response status :"+ res.getStatus());
        System.out.println("Response status :"+ res.getStatus());
    }
}
