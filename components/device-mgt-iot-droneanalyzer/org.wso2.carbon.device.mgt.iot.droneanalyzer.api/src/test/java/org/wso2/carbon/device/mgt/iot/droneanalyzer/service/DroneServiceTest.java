package org.wso2.carbon.device.mgt.iot.droneanalyzer.service;

import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.WebClient;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.wso2.carbon.device.mgt.common.Device;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Iterator;


public class DroneServiceTest {

    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneServiceTest.class);
    ApplicationContext context;
    WebClient client;

    @Before
    public void init(){
        context = new ClassPathXmlApplicationContext("spring-cxf-client.xml");
        client = context.getBean("droneClient", WebClient.class);
    }
    //@Test
    public void registerDevice(){
        client.path("manager/device/register").accept(MediaType.APPLICATION_JSON_TYPE);
        client.query("deviceId", "device7");
        client.query("name", "dronetypeOne");
        client.query("owner", "DroneOwner");
        Response res = client.put(null);
        log.info("Response status :"+ res.getStatus());
        System.out.println("Response status :"+ res.getStatus());
    }

    //@Test
    public void removeDevice(){
        client.path("manager/device/remove/").accept(MediaType.APPLICATION_JSON_TYPE);
        client.path("device7");
        Response res = client.delete();
        log.info("Response status :"+ res.getStatus());
    }

    //@Test
    public void updateDevice(){
        client.path("manager/device/update/").accept(MediaType.APPLICATION_JSON_TYPE);
        client.path("device2");
        client.query("name", "ARDrone");
        Response res = client.post(null);
        log.info("Response status :"+ res.getStatus());
    }

    //@Test
    public void getDevice(){
        client.path("manager/device/").accept(MediaType.APPLICATION_JSON_TYPE);
        client.path("device2");
        Device res = client.get(Device.class);
        log.info("Device name :"+ res.getName());
        log.info("Device type :"+ res.getType());
    }

    //@Test
    public void getDroneDevices(){
        client.path("manager/devices/").accept(MediaType.APPLICATION_JSON_TYPE);
        client.path("DroneOwner");
        Collection<? extends Device> res = client.getCollection(Device.class);
        Iterator<? extends Device> iterator = res.iterator();
        while (iterator.hasNext()) {
            Device device = iterator.next();
            log.info("Device name :" + device.getName());
            log.info("Device type :"+ device.getType());
            iterator.remove();
        }
    }

    //@Test
    public void downloadSketch(){
        client.path("manager/devices/");
        client.path("type1");
        client.path("download").accept(MediaType.APPLICATION_OCTET_STREAM);
        Response res = client.get();
        log.info(res.getStatus());
     }

    //@Test
    public void droneController(){
        client.path("controller/send_command");
        client.query("owner", "DroneOwner");
        client.query("deviceId", "device2");
        client.query("action", "takeoff");
        client.query("speed", 5);
        client.query("duration", 56);
        client.accept(MediaType.APPLICATION_JSON);
        Response res = client.post(null);
        System.out.println(res.getStatus());
    }

    public void generateSketchLink(){
        client.path("manager/devices/");
        client.path("type1");
        client.path("download").accept(MediaType.APPLICATION_OCTET_STREAM);
        Response res = client.get();
        log.info(res.getStatus());

    }



}
