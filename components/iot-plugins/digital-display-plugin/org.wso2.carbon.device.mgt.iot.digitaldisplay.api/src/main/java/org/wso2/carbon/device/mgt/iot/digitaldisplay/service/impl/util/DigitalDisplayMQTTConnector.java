package org.wso2.carbon.device.mgt.iot.digitaldisplay.service.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.service.impl.model.ScreenShotModel;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.service.impl.websocket.DigitalDisplayWebSocketServerEndPoint;
import org.wso2.carbon.device.mgt.iot.digitaldisplay.plugin.constants.DigitalDisplayConstants;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.transport.mqtt.MQTTTransportHandler;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

@SuppressWarnings("no JAX-WS annotation")
public class DigitalDisplayMQTTConnector extends MQTTTransportHandler {

    private static Log log = LogFactory.getLog(DigitalDisplayMQTTConnector.class);
    private static final String MQTT_TOPIC_APPENDER = "wso2/iot";
    private static final String subscribeTopic =
            MQTT_TOPIC_APPENDER + "/" + DigitalDisplayConstants.DEVICE_TYPE + "/+/digital_display_publisher";
    private static final String KEY_TYPE = "PRODUCTION";
    private static final String EMPTY_STRING = "";//change name to default password

    private static String iotServerSubscriber = UUID.randomUUID().toString().substring(0, 5);

    private ScheduledFuture<?> dataPushServiceHandler;

    private Map<String, ScreenShotModel> screenshots = new HashMap<>();

    private DigitalDisplayMQTTConnector() {
        super(iotServerSubscriber, DigitalDisplayConstants.DEVICE_TYPE,
              MqttConfig.getInstance().getMqttQueueEndpoint(), subscribeTopic);
    }

    @Override
    public void connect() {
        Runnable connector = new Runnable() {
            public void run() {
                while (!isConnected()) {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                            DigitalDisplayConstants.DEVICE_TYPE_PROVIDER_DOMAIN, true);
                    try {
                        String applicationUsername = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                                .getRealmConfiguration().getAdminUserName();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(applicationUsername);
                        APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
                        String[] tags = {DigitalDisplayConstants.DEVICE_TYPE};
                        ApiApplicationKey apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
                                DigitalDisplayConstants.DEVICE_TYPE, tags, KEY_TYPE, applicationUsername, true);
                        JWTClient jwtClient = APIUtil.getJWTClientManagerService().getJWTClient();
                        String scopes = "device_type_" + DigitalDisplayConstants.DEVICE_TYPE + " device_mqtt_connector";
                        AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(apiApplicationKey.getConsumerKey(),
                            apiApplicationKey.getConsumerSecret(), applicationUsername, scopes);
                        //create token
                        String accessToken = accessTokenInfo.getAccessToken();
                        setUsernameAndPassword(accessToken, EMPTY_STRING);
                        connectToQueue();
                        subscribeToQueue();
                    } catch (TransportHandlerException e) {
                        log.error("Connection/Subscription to MQTT Broker at: " + mqttBrokerEndPoint + " failed", e);
                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException ex) {
                            log.error("MQTT-Connector: Thread Sleep Interrupt Exception.", ex);
                        }
                    } catch (JWTClientException e) {
                        log.error("Failed to retrieve token from JWT Client.", e);
                        return;
                    } catch (UserStoreException e) {
                        log.error("Failed to retrieve the user.", e);
                        return;
                    } catch (APIManagerException e) {
                        log.error("Failed to create an application and generate keys.", e);
                        return;
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            }
        };

        Thread connectorThread = new Thread(connector);
        connectorThread.start();
    }

    @Override
    public void processIncomingMessage(MqttMessage message, String... messageParams) {
        //Add thread pull executor
        String topic = messageParams[0];
        String[] topicParams = topic.split("/");
        String owner = topicParams[2];
        String deviceId = topicParams[4];
        String[] messageData = message.toString().split("::");

        if (log.isDebugEnabled()) {
            log.debug("Received MQTT message for: [OWNER-" + owner + "] & [DEVICE.ID-" + deviceId + "]");
        }

        String sessionId = messageData[0];
        if (messageData.length == 2) {
            String responseMessage = messageData[1];
            DigitalDisplayWebSocketServerEndPoint.sendMessage(sessionId, new StringBuilder(responseMessage));
        } else if (messageData.length == 3) {
            String response = messageData[2];
            JSONObject schreenShot = new JSONObject(response);
            String picId = schreenShot.getString("pic_id");
            String data = schreenShot.getString("data");
            int pos = schreenShot.getInt("pos");
            int length = schreenShot.getInt("size");
            createScreenShot(sessionId, picId, pos, length, data);
        }
    }

    private void createScreenShot(String sessionId, String picId, int pos, int length, String data) {
        ScreenShotModel screenShotModel = screenshots.get(picId);
        if (screenShotModel == null) {
            screenShotModel = new ScreenShotModel();
            screenShotModel.setScreenShotData(new String[length + 1]);
            screenShotModel.setLength(0);
            screenshots.put(picId, screenShotModel);
        }
        if (screenShotModel.getLength() <= length) {
            screenShotModel.getScreenShotData()[pos] = data;
            screenShotModel.setLength(screenShotModel.getLength() + 1);
            if (screenShotModel.getLength() == (length + 1)) {
                StringBuilder displayScreenShot = new StringBuilder("Screenshot||");
                for (String screenshot : screenShotModel.getScreenShotData()) {
                    displayScreenShot.append(screenshot);
                }
                screenshots.remove(picId);
                DigitalDisplayWebSocketServerEndPoint.sendMessage(sessionId, displayScreenShot);
            }
        }
    }

    public void publishToDigitalDisplay(String topic, String payLoad, int qos, boolean retained)
            throws TransportHandlerException {
        if (log.isDebugEnabled()) {
            log.debug("Publishing message [" + payLoad + "to topic [" + topic + "].");
        }
        publishToQueue(topic, payLoad, qos, retained);
    }

    @Override
    public void disconnect() {
        Runnable stopConnection = new Runnable() {
            public void run() {
                while (isConnected()) {
                    try {
                        closeConnection();
                    } catch (MqttException e) {
                        if (log.isDebugEnabled()) {
                            log.warn("Unable to 'STOP' MQTT connection at broker at: " + mqttBrokerEndPoint);
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
