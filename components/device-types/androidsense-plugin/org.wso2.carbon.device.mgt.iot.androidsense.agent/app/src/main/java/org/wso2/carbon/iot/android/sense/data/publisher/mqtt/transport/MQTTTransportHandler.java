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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.iot.android.sense.data.publisher.mqtt.transport;
import android.content.Context;
import android.util.Log;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.iot.android.sense.constants.SenseConstants;
import org.wso2.carbon.iot.android.sense.util.LocalRegistry;

import java.nio.charset.StandardCharsets;

/**
 * This is an abstract class that implements the "TransportHandler" interface. The interface is an abstraction for
 * the core functionality with regards to device-server communication regardless of the Transport protocol. This
 * specific class contains the MQTT-Transport specific implementations. The class implements utility methods for the
 * case of a MQTT communication. However, this "abstract class", even-though it implements the "TransportHandler"
 * interface, does not contain the logic relevant to the interface methods. The specific functionality of the
 * interface methods are intended to be implemented by the concrete class that extends this abstract class and
 * utilizes the MQTT specific functionality (ideally a device API writer who would like to communicate to the device
 * via MQTT Protocol).
 * <p/>
 * This class contains the Device-Management specific implementation for all the MQTT functionality. This includes
 * connecting to a MQTT Broker & subscribing to the appropriate MQTT-topic, action plan upon losing connection or
 * successfully delivering a message to the broker and upon receiving a MQTT message. Makes use of the 'Paho-MQTT'
 * library provided by Eclipse Org.
 */
public abstract class MQTTTransportHandler implements MqttCallback, TransportHandler<MqttMessage> {
    private static final String TAG = "MQTTTransportHandler";

    private MqttClient client;
    private String clientId;
    private MqttConnectOptions options;     // options to be set to the client-connection.
    // topic to which a will-message is automatically published by the broker upon the device losing its connection.
    private String clientWillTopic;

    protected String mqttBrokerEndPoint;
    protected int timeoutInterval;          // interval to use for reconnection attempts etc.
    protected String subscribeTopic;

    // Quality of Service Levels for MQTT Subscription and Publishing.
    public static final int QoS_0 = 0;      // At-Most Once
    @SuppressWarnings("unused")
    public static final int QoS_1 = 1;      // At-Least Once
    public static final int QoS_2 = 2;      // Exactly Once

    public static final int DEFAULT_MQTT_QUALITY_OF_SERVICE = QoS_0;
    // Prefix to the Will-Topic to which a message is published if client loses its connection.
    private static final String DISCONNECTION_WILL_TOPIC_PREFIX = "Disconnection/";
    // Will-Message of the client to be published if connection is lost.
    private static final String DISCONNECTION_WILL_MSG = "Lost-Connection";
    /**
     * Constructor for the MQTTTransportHandler which takes in the owner, type of the device and the MQTT Broker URL
     * and the topic to subscribe.
     * @param context activity context.
     */
    protected MQTTTransportHandler(Context context) {
        String username = LocalRegistry.getUsername(context);
        String deviceId = LocalRegistry.getDeviceId(context);
        this.clientId = deviceId + ":" + SenseConstants.DEVICE_TYPE;
        this.subscribeTopic = LocalRegistry.getTenantDomain(context)+ "/" + SenseConstants.DEVICE_TYPE + "/" +
                deviceId + "/command/#";
        this.clientWillTopic = DISCONNECTION_WILL_TOPIC_PREFIX + SenseConstants.DEVICE_TYPE;
        this.mqttBrokerEndPoint = LocalRegistry.getMqttEndpoint(context);
        this.timeoutInterval = DEFAULT_TIMEOUT_INTERVAL;
        this.initMQTTClient();
        setUsernameAndPassword(LocalRegistry.getAccessToken(context), "");
    }

    /**
     * Constructor for the MQTTTransportHandler which takes in the owner, type of the device and the MQTT Broker URL
     * and the topic to subscribe. Additionally this constructor takes in the reconnection-time interval between
     * successive attempts to connect to the broker.
     *
     * @param deviceOwner        the owner of the device.
     * @param deviceType         the CDMF Device-Type of the device.
     * @param mqttBrokerEndPoint the IP/URL of the MQTT broker endpoint.
     * @param subscribeTopic     the MQTT topic to which the client is to be subscribed
     * @param intervalInMillis   the time interval in MILLI-SECONDS between attempts to connect to the broker.
     */
    protected MQTTTransportHandler(String deviceOwner, String deviceType,
                                   String mqttBrokerEndPoint, String subscribeTopic, int intervalInMillis) {
        this.clientId = deviceOwner + ":" + deviceType;
        this.subscribeTopic = subscribeTopic;
        this.clientWillTopic = DISCONNECTION_WILL_TOPIC_PREFIX + deviceType;
        this.mqttBrokerEndPoint = mqttBrokerEndPoint;
        this.timeoutInterval = intervalInMillis;
        this.initMQTTClient();
    }


    /**
     * Initializes the MQTT-Client. Creates a client using the given MQTT-broker endpoint and the clientId (which is
     * constructed by a concatenation of [deviceOwner]:[deviceType]). Also sets the client's options parameter with
     * the clientWillTopic (in-case of connection failure) and other info. Also sets the callback to this current class.
     */
    private void initMQTTClient() {
        try {
            client = new MqttClient(this.mqttBrokerEndPoint, clientId, null);
            Log.i(TAG, "MQTT client was created with ClientID : " + clientId);
        } catch (MqttException ex) {
            String errorMsg = "Initializing the MQTT Client failed.";
            Log.e(TAG, errorMsg, ex);
        }

        options = new MqttConnectOptions();
        options.setKeepAliveInterval(120);              // set the keep alive interval to 120 seconds by default.
        options.setCleanSession(true);                  // sets clean session to true by default.
        setDisconnectionWillForClient(QoS_2, true);     // sets default will-topic & msg with QoS 2 and retained true.
        client.setCallback(this);                       // callback for MQTT events are set to `this` object.
    }

    /**
     * @param qos        the Quality of Service at which the last-will-message is to be published.
     * @param isRetained indicate whether to retain the last-will-message.
     * @see MQTTTransportHandler#setDisconnectionWillForClient(String, String, int, boolean). Uses the default values
     * for Will-Topic and Will-Message.
     */
    protected void setDisconnectionWillForClient(int qos, boolean isRetained) {
        this.setDisconnectionWillForClient(clientWillTopic, DISCONNECTION_WILL_MSG, qos, isRetained);
    }

    /**
     * Sets the [Will] option in the default options-set of the MQTT Client. A will-topic, will-message is parsed
     * along with the QoS and the retained flag. When the client loses its connection to the broker, the broker
     * publishes the will-message to the will-topic, to itself.
     *
     * @param willTopic  the topic to which the last will message is to be published when client exists ungracefully.
     * @param willMsg    the message to be published upon client's ungraceful exit from the broker.
     * @param qos        the Quality of Service at which the last-will-message is to be published.
     * @param isRetained indicate whether to retain the last-will-message.
     */
    protected void setDisconnectionWillForClient(String willTopic, String willMsg, int qos, boolean isRetained) {
        this.options.setWill(willTopic, willMsg.getBytes(StandardCharsets.UTF_8), qos, isRetained);
    }

    /**
     * Sets the [Clean-Session] option in the default options-set of the MQTT Client. It is set to `true` by default.
     *
     * @param setCleanSession `true` indicates that the session details can be cleared/cleaned upon disconnection,
     *                        `false` indicates that the session details are to be persisted if the client disconnects.
     */
    @SuppressWarnings("unused")
    protected void setClientCleanSession(boolean setCleanSession) {
        this.options.setCleanSession(setCleanSession);
    }

    /**
     * Sets the [Username] & [Password] options in the default options-set of the MQTT Client. By default these
     * values are not set.
     *
     * @param username the username to be used by the client to connect to the broker.
     * @param password the password to be used by the client to connect to the broker.
     */
    @SuppressWarnings("unused")
    protected void setUsernameAndPassword(String username, String password) {
        this.options.setUserName(username);
        this.options.setPassword(password.toCharArray());
    }

    /**
     * Connects to the MQTT-Broker at the endpoint specified in the constructor to this class using default the
     * MQTT-options.
     *
     * @throws TransportHandlerException in the event of 'Connecting to' the MQTT broker fails.
     */
    protected void connectToQueue() throws TransportHandlerException {
        this.connectToQueue(options);
    }

    /**
     * Connects to the MQTT-Broker at the endpoint specified in the constructor to this class using the MQTT-Options
     * passed.
     *
     * @param options options to be used by the client for this connection. (username, password, clean-session, etc)
     * @throws TransportHandlerException in the event of 'Connecting to' the MQTT broker fails.
     */
    protected void connectToQueue(MqttConnectOptions options) throws TransportHandlerException {
        try {
            client.connect(options);
            Log.d(TAG, "MQTT Client connected to queue at: " + this.mqttBrokerEndPoint);
        } catch (MqttException ex) {
            String errorMsg = "MQTT Exception occured whilst connecting to queue at [" + this.mqttBrokerEndPoint + "]";
            Log.e(TAG, errorMsg);
            throw new TransportHandlerException(errorMsg, ex);
        }
    }

    /**
     * @throws TransportHandlerException in the event of 'Subscribing to' the MQTT broker fails.
     * @see MQTTTransportHandler#subscribeToQueue(int). Uses default QoS of 1.
     */
    protected void subscribeToQueue() throws TransportHandlerException {
        this.subscribeToQueue(QoS_0);
    }

    /**
     * Subscribes to the MQTT-Topic specified in the constructor to this class.
     *
     * @throws TransportHandlerException in the event of 'Subscribing to' the MQTT broker fails.
     */
    protected void subscribeToQueue(int qos) throws TransportHandlerException {
        try {
            client.subscribe(subscribeTopic, qos);
            Log.d(TAG, "Client [" + clientId + "] subscribed to topic: " + subscribeTopic);
        } catch (MqttException ex) {
            String errorMsg = "MQTT Exception occurred whilst client [" + clientId + "] tried to subscribe to " +
                    "topic: [" + subscribeTopic + "]";
            Log.e(TAG, errorMsg);
            throw new TransportHandlerException(errorMsg, ex);
        }
    }

    /**
     * @param topic   the topic to which the message is to be published.
     * @param payLoad the message (payload) of the MQTT publish action.
     * @see MQTTTransportHandler#publishToQueue(String, String, int, boolean)
     */
    @SuppressWarnings("unused")
    protected void publishToQueue(String topic, String payLoad) throws TransportHandlerException {
        publishToQueue(topic, payLoad, DEFAULT_MQTT_QUALITY_OF_SERVICE, false);
    }

    /**
     * @param topic   the topic to which the message is to be published.
     * @param message the message (payload) of the MQTT publish action as a `MQTTMessage`.
     * @throws TransportHandlerException if any error occurs whilst trying to publish to the MQTT Queue.
     * @see MQTTTransportHandler#publishToQueue(String, String, int, boolean)
     */
    protected void publishToQueue(String topic, MqttMessage message) throws TransportHandlerException {
        try {
            client.publish(topic, message);
            Log.d(TAG, "Message: " + message.toString() + " to MQTT topic [" + topic + "] published successfully");
        } catch (MqttException ex) {
            String errorMsg = "MQTT Client Error whilst client [" + clientId + "] tried to publish to queue at " +
                    "[" + mqttBrokerEndPoint + "] under topic [" + topic + "]";
            Log.e(TAG, errorMsg);
            throw new TransportHandlerException(errorMsg, ex);
        }
    }

    /**
     * This method is used to publish messages to the MQTT-Endpoint to which this client is connected to. It is via
     * publishing to this broker that the messages are communicated to the device. This is an overloaded method with
     * different parameter combinations. This method invokes the publish method provided by the MQTT-Client library.
     *
     * @param topic    the topic to which the message is to be published.
     * @param payLoad  the message (payload) of the MQTT publish action.
     * @param qos      the Quality-of-Service of the current publish action.
     *                 Could be 0(At-most once), 1(At-least once) or 2(Exactly once)
     * @param retained indicate whether to retain the publish-message in the event of no subscribers.
     * @throws TransportHandlerException if any error occurs whilst trying to publish to the MQTT Queue.
     */
    protected void publishToQueue(String topic, String payLoad, int qos, boolean retained)
            throws TransportHandlerException {
        try {
            client.publish(topic, payLoad.getBytes(StandardCharsets.UTF_8), qos, retained);
            Log.d(TAG, "Message: " + payLoad + " to MQTT topic [" + topic + "] published successfully");

        } catch (MqttException ex) {
            String errorMsg = "MQTT Client Error whilst client [" + clientId + "] tried to publish to queue at " +
                    "[" + mqttBrokerEndPoint + "] under topic [" + topic + "]";
            Log.e(TAG, errorMsg);
            throw new TransportHandlerException(errorMsg, ex);
        }
    }

    /**
     * Checks whether the connection to the MQTT-Broker exists.
     *
     * @return `true` if the client is connected to the MQTT-Broker, else `false`.
     */
    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    /**
     * Callback method which is triggered once the MQTT client losers its connection to the broker. Spawns a new
     * thread that executes necessary actions to try and reconnect to the endpoint.
     *
     * @param throwable a Throwable Object containing the details as to why the failure occurred.
     */
    @Override
    public void connectionLost(Throwable throwable) {
        Log.w(TAG, "Connection for client: " + this.clientId + " to " + this.mqttBrokerEndPoint + " was lost." +
                    "\nThis was due to - " + throwable.getMessage());
        Thread reconnectThread = new Thread() {
            public void run() {
                connect();
            }
        };
        reconnectThread.start();
    }

    /**
     * Callback method which is triggered upon receiving a MQTT Message from the broker. Spawns a new thread that
     * executes any actions to be taken with the received message.
     *
     * @param topic       the MQTT-Topic to which the received message was published to and the client subscribed to.
     * @param mqttMessage the actual MQTT-Message that was received from the broker.
     */
    @Override
    public void messageArrived(final String topic, final MqttMessage mqttMessage) {
        Log.d(TAG, "Got an MQTT message '" + mqttMessage.toString() + "' for topic '" + topic + "'.");

        Thread messageProcessorThread = new Thread() {
            public void run() {
                try {
                    processIncomingMessage(mqttMessage, topic);
                } catch (TransportHandlerException e) {
                    Log.e(TAG, "An error occurred when trying to process received MQTT message [" + mqttMessage + "] " +
                            "for topic [" + topic + "].", e);
                }
            }
        };
        messageProcessorThread.start();
    }

    /**
     * Callback method which gets triggered upon successful completion of a message delivery to the broker.
     *
     * @param iMqttDeliveryToken the MQTT-DeliveryToken which includes the details about the specific message delivery.
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        String topic = iMqttDeliveryToken.getTopics()[0];
        String client = iMqttDeliveryToken.getClient().getClientId();

        try {
            if (iMqttDeliveryToken.isComplete()) {
                if (iMqttDeliveryToken.getMessage() != null) {
                    String message = iMqttDeliveryToken.getMessage().toString();
                    Log.d(TAG, "Message to client [" + client + "] under topic (" + topic +
                            ") was delivered successfully with the delivery message: '" + message + "'");
                } else {
                    Log.d(TAG, "Message to client [" + client + "] under topic (" + topic +
                            ") was delivered successfully.");
                }
            } else {
                Log.w(TAG, "FAILED: Delivery of MQTT message to [" + client + "] under topic [" + topic + "] failed.");
            }
        } catch (MqttException e) {
            Log.w(TAG, "Error occurred whilst trying to read the message from the MQTT delivery token.");
        }
    }

    /**
     * Closes the connection to the MQTT Broker.
     */
    public void closeConnection() throws MqttException {
        if (client != null && isConnected()) {
            client.disconnect();
        }
    }

    /**
     * Fetches the default options set for the MQTT Client
     *
     * @return the options that are currently set for the client.
     */
    public MqttConnectOptions getOptions() {
        return options;
    }
}