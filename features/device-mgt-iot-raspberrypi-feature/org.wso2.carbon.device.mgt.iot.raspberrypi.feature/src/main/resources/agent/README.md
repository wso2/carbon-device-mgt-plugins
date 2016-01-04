# Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
# http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.


                                                --------------
                                                  testAgent.sh
                                                --------------
This script is used to run this service in a testing environment. It can be run on a real Raspberry Pi device or in a 
virtual environment.
If this runs on a real Raspberry Pi device, you need to install Adafruit Python DHT Sensor Library and RPi.GPIO Python Library.To install  Adafruit Python DHT Sensor Library, follow the
instructions given here https://github.com/adafruit/Adafruit_Python_DHT.
To install RPi.GPIO Python Library, sudo apt-get install rpi.gpio

To run: sudo ./testAgent.sh and follow the instructions.

                                                -------------------
                                                  startService.sh
                                                -------------------
After testing, this script can be used to deploy this application as a service on Raspberry Pi which will get loaded
during boot up process.
To run: sudo ./startService.sh
Note: You should have to provide following arguments in RaspberryService.sh as shown below.

DAEMON_OPTS="-l /usr/local/src/RaspberryAgent/RaspberryStats.log -m N -i 56"

-l ----> file to write log
-i ----> time interval between successive data pushes to the wso2 IoT Server
-m ----> weather is going to run on the real device or not

start service:
       service /etc/init.d/RaspberryService.sh start

stop service:
       service /etc/init.d/RaspberryService.sh stop
      
