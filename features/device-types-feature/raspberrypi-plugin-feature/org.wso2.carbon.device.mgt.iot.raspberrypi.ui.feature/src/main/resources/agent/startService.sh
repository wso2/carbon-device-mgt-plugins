#"""
#/**
#* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#*
#* WSO2 Inc. licenses this file to you under the Apache License,
#* Version 2.0 (the "License"); you may not use this file except
#* in compliance with the License.
#* You may obtain a copy of the License at
#*
#* http://www.apache.org/licenses/LICENSE-2.0
#*
#* Unless required by applicable law or agreed to in writing,
#* software distributed under the License is distributed on an
#* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#* KIND, either express or implied. See the License for the
#* specific language governing permissions and limitations
#* under the License.
#**/
#"""

#!/usr/bin/env bash

echo "----------------------------------------------------------------"
echo "|		          WSO2 IOT Sample				                  "
echo "|		            RaspiAgent				                      "
echo "|	              ----------------				                  "
echo "|           ....initializing service-start-script	              "
echo "----------------------------------------------------------------"

destination="/usr/local/src/RaspberryAgent"
currentDir=$PWD
if [ ! -d "$destination" ]
then
    mkdir $destination
fi
# installing dependencies
echo ===Installing Dependencies
sudo apt-get update
sudo apt-get install python-pip
sudo pip install paho-mqtt

#installing ada
sudo apt-get install build-essential python-dev
unzip Adafruit_Python_DHT
sudo python Adafruit_Python_DHT/setup.py install

sudo cp $currentDir/deviceConfig.properties $currentDir/src
sudo cp -r $currentDir/src $destination
sudo chmod 755 $destination/src/RaspberryAgent.py
sudo update-rc.d -f RaspberryService.sh remove
sudo cp $currentDir/RaspberryService.sh /etc/init.d
sudo chmod 755 /etc/init.d/RaspberryService.sh
sudo update-rc.d RaspberryService.sh defaults
sudo service RaspberryService.sh start
