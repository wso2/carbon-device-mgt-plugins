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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.transport;

/**
 * This interface consists of the core functionality related to the transport between any device and the server. The
 * interface is an abstraction, regardless of the underlying protocol used for the transport. Implementation of this
 * interface by any class that caters a specific protocol (ex: HTTP, XMPP, MQTT, CoAP) would ideally have methods
 * specific to the protocol used for communication and other methods that implement the logic related to the devices
 * using the protocol. The methods of the interface are identified as generic ones for implementing transport
 * protocols for device communication. The implementation can utilize the appropriate method signatures applicable for
 * intended protocol.
 *
 * @param <T> an object of the message type specific to the protocol implemented. To be set to 'String' if there
 *            isn't anything specific.
 */
public interface TransportHandler<T> {
    // a default timeout interval to be used for the protocol specific connections
    int DEFAULT_TIMEOUT_INTERVAL = 5000;      // millis ~ 5 sec

    /**
     * Implements the underlying connect mechanism specific to the protocol enabled by the interface. An object of a
     * class that implements this interface would call this method before any communication is started via the
     * intended protocol.
     */
    void connect();

    /**
     * Used to check whether a connection (via the implemented protocol) to the external-endpoint exists. Ideally
     * used to verify that the connection persists and to spawn a reconnection attempt if not.
     *
     * @return 'true' if connection is already made & exists, else 'false'.
     */
    boolean isConnected();

    /**
     * @throws TransportHandlerException in the event of any exceptions that occur whilst processing the message.
     * @see TransportHandler#processIncomingMessage(Object, String...)
     */
    void processIncomingMessage() throws TransportHandlerException;

    /**
     * @param message the message (of the type specific to the protocol) received from the device.
     * @throws TransportHandlerException
     * @see TransportHandler#processIncomingMessage(Object, String...)
     */
    void processIncomingMessage(T message) throws TransportHandlerException;

    /**
     * This is an overloaded method with three different method-signatures. This method is used to process any
     * incoming messages via the implemented protocol. It would ideally be invoked at a point where a message
     * received event is activated (Ex: `MessageArrived` callback in Eclipse-Paho-MQTT Client & `PacketListener`(s)
     * in XMPP).
     * <p/>
     *
     * @param message       the message (of the type specific to the protocol) received from the device.
     * @param messageParams one or more other parameters received as part-of & relevant-to the message (Ex: MQTT Topic).
     * @throws TransportHandlerException in the event of any exceptions that occur whilst processing the message.
     */
    void processIncomingMessage(T message, String... messageParams) throws TransportHandlerException;

    /**
     * @throws TransportHandlerException in the event of any exceptions that occur whilst sending the message.
     * @see TransportHandler#publishDeviceData(String...)
     */
    void publishDeviceData() throws TransportHandlerException;

    /**
     * @param publishData the message (of the type specific to the protocol) to be sent to the device.
     * @throws TransportHandlerException in the event of any exceptions that occur whilst sending the message.
     * @see TransportHandler#publishDeviceData(String...)
     */
    void publishDeviceData(T publishData) throws TransportHandlerException;

    /**
     * This is an overloaded method with three different method-signatures. This method is used to publish messages
     * to an external-endpoint/device via the implemented protocol. It could in itself call the (communicating)
     * external-endpoint or invoke any method provided by the protocol specific library.
     * <p/>
     *
     * @param publishData one or more parameters specific to the message and the data to be sent.
     * @throws TransportHandlerException in the event of any exceptions that occur whilst sending the message.
     */
    void publishDeviceData(String... publishData) throws TransportHandlerException;

    /**
     * Implements the underlying disconnect mechanism specific to the protocol enabled by the interface. An object of a
     * class that implements this interface would call this method upon completion of all communication. In the case of
     * the IoT-Server invoking this would only be required if the server shuts-down.
     */
    void disconnect();
}
