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

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.agent.transport.TransportHandlerException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TransportUtils {
	private static final Log log = LogFactory.getLog(TransportUtils.class);

	public static final int MIN_PORT_NUMBER = 9000;
	public static final int MAX_PORT_NUMBER = 11000;

	/**
	 * Given a server endpoint as a String, this method splits it into Protocol, Host and Port
	 *
	 * @param ipString a network endpoint in the format - '<PROTOCOL>://<HOST>:<PORT>'
	 * @return a map with keys "Protocol", "Host" & "Port" for the related values from the ipString
	 * @throws TransportHandlerException
	 */
	public static Map<String, String> getHostAndPort(String ipString)
			throws TransportHandlerException {
		Map<String, String> ipPortMap = new HashMap<String, String>();
		String[] ipPortArray = ipString.split(":");

		if (ipPortArray.length != 3) {
			String errorMsg =
					"The IP String - '" + ipString +
							"' is invalid. It needs to be in format '<PROTOCOL>://<HOST>:<PORT>'.";
			log.info(errorMsg);
			throw new TransportHandlerException(errorMsg);
		}

		ipPortMap.put("Protocol", ipPortArray[0]);
		ipPortMap.put("Host", ipPortArray[1].replace("/", ""));
		ipPortMap.put("Port", ipPortArray[2]);
		return ipPortMap;
	}

	/**
	 * This method validates whether a specific IP Address is of IPv4 type
	 *
	 * @param ipAddress the IP Address which needs to be validated
	 * @return true if it is of IPv4 type and false otherwise
	 */
	public static boolean validateIPv4(String ipAddress) {
		try {
			if (ipAddress == null || ipAddress.isEmpty()) {
				return false;
			}

			String[] parts = ipAddress.split("\\.");
			if (parts.length != 4) {
				return false;
			}

			for (String s : parts) {
				int i = Integer.parseInt(s);
				if ((i < 0) || (i > 255)) {
					return false;
				}
			}
			return !ipAddress.endsWith(".");

		} catch (NumberFormatException nfe) {
			log.warn("The IP Address: " + ipAddress + " could not " +
					         "be validated against IPv4-style");
			return false;
		}
	}


	public static Map<String, String> getInterfaceIPMap() throws TransportHandlerException {

		Map<String, String> interfaceToIPMap = new HashMap<String, String>();
		Enumeration<NetworkInterface> networkInterfaces;
		String networkInterfaceName = "";
		String ipAddress;

		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException exception) {
			String errorMsg =
					"Error encountered whilst trying to get the list of network-interfaces";
			log.error(errorMsg);
			throw new TransportHandlerException(errorMsg, exception);
		}

		try {
			for (; networkInterfaces.hasMoreElements(); ) {
				networkInterfaceName = networkInterfaces.nextElement().getName();

				if (log.isDebugEnabled()) {
					log.debug("Network Interface: " + networkInterfaceName);
					log.debug("------------------------------------------");
				}

				Enumeration<InetAddress> interfaceIPAddresses = NetworkInterface.getByName(
						networkInterfaceName).getInetAddresses();

				for (; interfaceIPAddresses.hasMoreElements(); ) {
					ipAddress = interfaceIPAddresses.nextElement().getHostAddress();

					if (log.isDebugEnabled()) {
						log.debug("IP Address: " + ipAddress);
					}

					if (TransportUtils.validateIPv4(ipAddress)) {
						interfaceToIPMap.put(networkInterfaceName, ipAddress);
					}
				}

				if (log.isDebugEnabled()) {
					log.debug("------------------------------------------");
				}
			}
		} catch (SocketException exception) {
			String errorMsg =
					"Error encountered whilst trying to get the IP Addresses of the network " +
							"interface: " + networkInterfaceName;
			log.error(errorMsg);
			throw new TransportHandlerException(errorMsg, exception);
		}

		return interfaceToIPMap;
	}


	/**
	 * Attempts to find a free port between the MIN_PORT_NUMBER(9000) and MAX_PORT_NUMBER(11000).
	 * Tries 'RANDOMLY picked' port numbers between this range up-until "randomAttempts" number of
	 * times. If still fails, then tries each port in descending order from the MAX_PORT_NUMBER
	 * whilst skipping already attempted ones via random selection.
	 *
	 * @param randomAttempts no of times to TEST port numbers picked randomly over the given range
	 * @return an available/free port
	 */
	public static synchronized int getAvailablePort(int randomAttempts) {
		ArrayList<Integer> failedPorts = new ArrayList<Integer>(randomAttempts);

		Random randomNum = new Random();
		int randomPort = MAX_PORT_NUMBER;

		while (randomAttempts > 0) {
			randomPort = randomNum.nextInt(MAX_PORT_NUMBER - MIN_PORT_NUMBER) + MIN_PORT_NUMBER;

			if (checkIfPortAvailable(randomPort)) {
				return randomPort;
			}
			failedPorts.add(randomPort);
			randomAttempts--;
		}

		randomPort = MAX_PORT_NUMBER;

		while (true) {
			if (!failedPorts.contains(randomPort) && checkIfPortAvailable(randomPort)) {
				return randomPort;
			}
			randomPort--;
		}
	}


	private static boolean checkIfPortAvailable(int port) {
		ServerSocket tcpSocket = null;
		DatagramSocket udpSocket = null;

		try {
			tcpSocket = new ServerSocket(port);
			tcpSocket.setReuseAddress(true);

			udpSocket = new DatagramSocket(port);
			udpSocket.setReuseAddress(true);
			return true;
		} catch (IOException ex) {
			// denotes the port is in use
		} finally {
			if (tcpSocket != null) {
				try {
					tcpSocket.close();
				} catch (IOException e) {
	                    /* not to be thrown */
				}
			}

			if (udpSocket != null) {
				udpSocket.close();
			}
		}

		return false;
	}


	/**
	 * This is a utility method that creates and returns a HTTP connection object.
	 *
	 * @param urlString the URL pattern to which the connection needs to be created
	 * @return an HTTPConnection object which cn be used to send HTTP requests
	 * @throws TransportHandlerException if errors occur when creating the HTTP connection with
	 *                                       the given URL string
	 */
	public static HttpURLConnection getHttpConnection(String urlString) throws
	                                                                    TransportHandlerException {
		URL connectionUrl;
		HttpURLConnection httpConnection;

		try {
			connectionUrl = new URL(urlString);
			httpConnection = (HttpURLConnection) connectionUrl.openConnection();
		} catch (MalformedURLException e) {
			String errorMsg = "Error occured whilst trying to form HTTP-URL from string: " + urlString;
			log.error(errorMsg);
			throw new TransportHandlerException(errorMsg, e);
		} catch (IOException exception) {
			String errorMsg = "Error occured whilst trying to open a connection to: " + urlString;
			log.error(errorMsg);
			throw new TransportHandlerException(errorMsg, exception);
		}
		return httpConnection;
	}

	/**
	 * This is a utility method that reads and returns the response from a HTTP connection
	 *
	 * @param httpConnection the connection from which a response is expected
	 * @return the response (as a string) from the given HTTP connection
	 * @throws TransportHandlerException if any errors occur whilst reading the response from
	 *                                       the connection stream
	 */
	public static String readResponseFromHttpRequest(HttpURLConnection httpConnection)
			throws TransportHandlerException {
		BufferedReader bufferedReader;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(
					httpConnection.getInputStream(), StandardCharsets.UTF_8));
		} catch (IOException exception) {
			String errorMsg = "There is an issue with connecting the reader to the input stream at: " +
					httpConnection.getURL();
			log.error(errorMsg);
			throw new TransportHandlerException(errorMsg, exception);
		}

		String responseLine;
		StringBuilder completeResponse = new StringBuilder();

		try {
			while ((responseLine = bufferedReader.readLine()) != null) {
				completeResponse.append(responseLine);
			}
		} catch (IOException exception) {
			String errorMsg = "Error occured whilst trying read from the connection stream at: " +
					httpConnection.getURL();
			log.error(errorMsg);
			throw new TransportHandlerException(errorMsg, exception);
		}
		try {
			bufferedReader.close();
		} catch (IOException exception) {
			log.error("Could not succesfully close the bufferedReader to the connection at: " + httpConnection.getURL());
		}
		return completeResponse.toString();
	}

}
