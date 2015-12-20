#!/usr/bin/env python

"""
/**
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
**/
"""

import time, commands
import ConfigParser, os
import random
import running_mode

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#			HOST_NAME(IP) of the Device
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
global HOST_NAME
HOST_NAME = "0.0.0.0"
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

global LAST_TEMP
LAST_TEMP = 25  # The Last read temperature value from the DHT sensor. Kept globally
# Updated by the temperature reading thread
# Associate pin 23 to TRIG
TEMPERATURE_READING_INTERVAL_REAL_MODE = 3
TEMPERATURE_READING_INTERVAL_VIRTUAL_MODE = 60
TEMP_PIN = 4
TEMP_SENSOR_TYPE = 11

BULB_PIN = 11  # The GPIO Pin# in RPi to which the LED is connected
HTTP_SERVER_PORT = 80 # http server port which is listning on
global GPIO

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Device specific info when pushing data to server
#       Read from a file "deviceConfig.properties" in the same folder level
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
configParser = ConfigParser.RawConfigParser()
configFilePath = os.path.join(os.path.dirname(__file__), '../deviceConfig.properties')
configParser.read(configFilePath)

DEVICE_OWNER = configParser.get('Device-Configurations', 'owner')
DEVICE_ID = configParser.get('Device-Configurations', 'deviceId')
MQTT_EP = configParser.get('Device-Configurations', 'mqtt-ep')
XMPP_EP = configParser.get('Device-Configurations', 'xmpp-ep')
AUTH_TOKEN = configParser.get('Device-Configurations', 'auth-token')
CONTROLLER_CONTEXT = configParser.get('Device-Configurations', 'controller-context')
DEVICE_INFO = '{"owner":"' + DEVICE_OWNER + '","deviceId":"' + DEVICE_ID + '","reply":'
HTTPS_EP = configParser.get('Device-Configurations', 'https-ep')
HTTP_EP = configParser.get('Device-Configurations', 'http-ep')
DEVICE_IP = '"{ip}","value":'
DEVICE_DATA = '"{temperature}"'  # '"{temperature}:{load}:OFF"'


### ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Method used to switch ON/OFF the LED attached to RPi
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def switchBulb(state):
    print "Requested Switch State: " + state

    if running_mode.RUNNING_MODE == "N":
        import RPi.GPIO as GPIO
        if state == "ON":
            GPIO.output(BULB_PIN, True)
            print "BULB Switched ON"
        elif state == "OFF":
            GPIO.output(BULB_PIN, False)
            print "BULB Switched OFF"
    else:
        if state == "ON":
            print "BULB Switched ON"
        elif state == "OFF":
            print "BULB Switched OFF"
    print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       This method generate a random temperature value
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def generateRandomTemperatureAndHumidityValues():
    return [random.randint(15, 40),random.randint(15, 40)]
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


#      If an agent runs on a real setup GPIO needs to be imported
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def initGPIOModule():
    if running_mode.RUNNING_MODE == 'N':
        import RPi.GPIO as GPIO
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Get the wlan0 interface via which the RPi is connected 
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def getDeviceIP():
    rPi_IP = commands.getoutput("ip route list | grep 'src '").split()
    rPi_IP = rPi_IP[rPi_IP.index('src') + 1]
    if len(rPi_IP) <= 16:
        print "------------------------------------------------------------------------------------"
        print "IOT_UTILS: IP Address of RaspberryPi: " + rPi_IP
        print "------------------------------------------------------------------------------------"
        return rPi_IP
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Get the port which http server is listening on
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def getHTTPServerPort():
    return HTTP_SERVER_PORT
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Set the GPIO pin modes for the ones to be read
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def setUpGPIOPins():
    import RPi.GPIO as GPIO
    try:
        GPIO.setwarnings(False)
        GPIO.setmode(GPIO.BOARD)
    except Exception as e:
        print "IOT_UTILS: Exception at 'GPIO.setmode'"
        print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
        pass

    GPIO.setup(BULB_PIN, GPIO.OUT)
    GPIO.output(BULB_PIN, False)
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The Main method of the server script
#           This method is invoked from RaspberryStats.py on a new thread
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def main():
    global HOST_NAME
    HOST_NAME = getDeviceIP()
    if running_mode.RUNNING_MODE == 'N':
        setUpGPIOPins()


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

if __name__ == '__main__':
    main()
