package org.wso2.carbon.device.mgt.iot.digitaldisplay.api.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.api.websocket.DigitalDisplayWebSocketServerEndPoint;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.constants.DigitalDisplayConstants;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.transport.mqtt.MQTTTransportHandler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

public class DigitalDisplayMqttCommunicationHandler extends MQTTTransportHandler {

    private static Log log = LogFactory.getLog(DigitalDisplayMqttCommunicationHandler.class);

    private static final String subscribeTopic =
            "wso2" + File.separator + "iot" + File.separator + "+" + File.separator +
                    DigitalDisplayConstants.DEVICE_TYPE + File.separator + "+" + File.separator +
                    "digital_display_publisher";

    private static String iotServerSubscriber = UUID.randomUUID().toString().substring(0, 5);

    private ScheduledFuture<?> dataPushServiceHandler;

    private Map<String, String[]> schreenshots = new HashMap<>();

    private DigitalDisplayMqttCommunicationHandler() {
        super(iotServerSubscriber, DigitalDisplayConstants.DEVICE_TYPE,
                MqttConfig.getInstance().getMqttQueueEndpoint(), subscribeTopic);
    }

    public ScheduledFuture<?> getDataPushServiceHandler() {
        return dataPushServiceHandler;
    }

    @Override
    public void connect() {
        Runnable connect = new Runnable() {
            @Override
            public void run() {
                while (!isConnected()) {
                    try {
                        connectToQueue();
                        subscribeToQueue();

                    } catch (TransportHandlerException e) {
                        log.warn("Connection/Subscription to MQTT Broker at: " +
                                mqttBrokerEndPoint + " failed");

                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException ex) {
                            log.error("MQTT-Subscriber: Thread Sleep Interrupt Exception");
                        }
                    }
                }

                log.info("Connected..");

            }
        };

        Thread connectorThread = new Thread(connect);
        connectorThread.setDaemon(true);
        connectorThread.start();

    }

    @Override
    public void processIncomingMessage(MqttMessage message, String... messageParams) {

        String topic = messageParams[0];

        String ownerAndId = topic.replace("wso2" + File.separator + "iot" + File.separator, "");
        ownerAndId = ownerAndId.replace(File.separator + DigitalDisplayConstants.DEVICE_TYPE + File.separator, ":");
        ownerAndId = ownerAndId.replace(File.separator + "digital_display_publisher", "");

        String owner = ownerAndId.split(":")[0];
        String deviceId = ownerAndId.split(":")[1];

        String[] messageData = message.toString().split("::");
        log.info("Received MQTT message for: {OWNER-" + owner + "} & {DEVICE.ID-" + deviceId + "}");

        String token = messageData[0];
        if (messageData.length == 2) {
            String responseMessage = messageData[1];
            DigitalDisplayWebSocketServerEndPoint.sendMessage(token, new StringBuilder(responseMessage));
        } else if (messageData.length == 3) {
            String tag = messageData[1];
            if (tag.equals("token")) {
                String response = messageData[2];

                JSONObject schreenshot = new JSONObject(response);
                String pic_id = schreenshot.getString("pic_id");
                String data = schreenshot.getString("data");
                int pos = schreenshot.getInt("pos");
                int length = schreenshot.getInt("size");
                createScreenShot(token, pic_id, pos, length ,data);

            }

        }
    }

    private void createScreenShot(String token, String pic_id, int pos, int length, String data) {

        String[] screenshot = schreenshots.get(pic_id);

        if (screenshot == null) {
            screenshot = new String[length + 2];
            screenshot[length + 1] = "0";
            schreenshots.put(pic_id, screenshot);
        }
        if (!screenshot[length + 1].equals(String.valueOf(length + 1))) {
            screenshot[pos] = data;
            screenshot[length + 1] = String.valueOf(Integer.parseInt(screenshot[length + 1]) + 1);

            if(screenshot[length + 1].equals(String.valueOf(length + 1))){
                StringBuilder displayScreenShot = new StringBuilder("");

                for(int i = 0 ; i < (screenshot.length - 1) ; i++){
                    displayScreenShot.append(screenshot[i]);
                }
                System.out.println(screenshot[length+1]);
                schreenshots.remove(pic_id);
                DigitalDisplayWebSocketServerEndPoint.sendMessage(token, displayScreenShot);
            }
        }
    }

    public void publishToDigitalDisplay(String topic, String payLoad, int qos, boolean retained)
            throws TransportHandlerException {
        log.info(topic + " " + payLoad);
        publishToQueue(topic, payLoad, qos, retained);
    }

    @Override
    public void disconnect() {
        Runnable stopConnection = new Runnable() {
            public void run() {
                while (isConnected()) {
                    try {
                        dataPushServiceHandler.cancel(true);
                        closeConnection();

                    } catch (MqttException e) {
                        if (log.isDebugEnabled()) {
                            log.warn("Unable to 'STOP' MQTT connection at broker at: " +
                                    mqttBrokerEndPoint);
                        }

                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException e1) {
                            log.error("MQTT-Terminator: Thread Sleep Interrupt Exception");
                        }
                    }
                }
            }
        };

        Thread terminatorThread = new Thread(stopConnection);
        terminatorThread.setDaemon(true);
        terminatorThread.start();
    }


    @Override
    public void publishDeviceData() throws TransportHandlerException {

    }

    @Override
    public void publishDeviceData(MqttMessage publishData) throws TransportHandlerException {

    }

    @Override
    public void publishDeviceData(String... publishData) throws TransportHandlerException {

    }

    @Override
    public void processIncomingMessage() {

    }

    @Override
    public void processIncomingMessage(MqttMessage message) throws TransportHandlerException {

    }

}
