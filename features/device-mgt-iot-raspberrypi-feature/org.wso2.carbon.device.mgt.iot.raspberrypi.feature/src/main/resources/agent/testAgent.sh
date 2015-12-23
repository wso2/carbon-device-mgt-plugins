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

#!/bin/bash

echo "----------------------------------------------------------------"
echo "|		WSO2 IOT Sample				"
echo "|		  RaspiAgent				"
echo "|	       ----------------				"
echo "|    ....initializing startup-script	"
echo "----------------------------------------------------------------"

currentDir=$PWD

while true; do
    read -p "Do you wish to run 'apt-get update' and continue? [Yes/No] " yn
    case $yn in
        [Yy]* ) sudo apt-get update;
				break;;
        [Nn]* ) echo "Continuing without apt-get update...";
				break;;
        * ) echo "Please answer yes or no.";
    esac
done

if [ $? -ne 0 ]; then
    echo "apt-get update failed.... Some dependencies may not get installed"
    echo "If an already installed version of the package exists, try running:"
    echo "----------------------------------------------------------------"
    echo "sudo -i"
    echo "cd /var/lib/dpkg/info"
    echo "rm -rf wso2-raspi-alarm*"
    echo "dpkg --remove --force-remove-reinstreq wso2-raspi-agent"
    echo "exit"
    echo "----------------------------------------------------------------"
    echo "Retry Installation...."
    break;
fi

for f in ./deviceConfig.properties; do
    ## Check if the glob gets expanded to existing files.
    ## If not, f here will be exactly the pattern above
    ## and the exists test will evaluate to false.
    if [ -e "$f" ]; then
    	echo "Configuration file found......"
    else
    	echo "'deviceConfig.properties' file does not exist in current path. \nExiting installation...";
    	exit;
    fi
    ## This is all we needed to know, so we can break after the first iteration
    break
done

cd $currentDir
git clone git://git.eclipse.org/gitroot/paho/org.eclipse.paho.mqtt.python.git
cd org.eclipse.paho.mqtt.python
sudo python setup.py install

cd $currentDir

sudo apt-get install python-pip
sudo pip install sleekxmpp
sudo pip install pyasn1 pyasn1-modules

while true; do
    read -p "Whats the time-interval (in seconds) between successive Data-Pushes to the WSO2-DC (ex: '60' indicates 1 minute) > " input
    read -p "Are you want to run this as a virtual agent? (Yes/No) " mode

    if [ $input -eq $input 2>/dev/null ]
    then
        echo "Setting data-push interval to $input seconds."
    else
        echo "Input needs to be an integer indicating the number seconds between successive data-pushes."
    fi
    case $mode in
        [Yy]* )  mode="Y"
                 echo "----------------------------------------------------------"
                 echo "              This will run as a virtual agent            "
                 echo "----------------------------------------------------------"
				 break;;
        [Nn]* )  mode="N"
                 echo "----------------------------------------------------------"
                 echo "              This will run as a real agent               "
                 echo "----------------------------------------------------------"
				 break;;
        * ) echo "Please answer yes or no.";
    esac
done
cp deviceConfig.properties ./src
chmod +x ./src/RaspberryAgent.py
./src/RaspberryAgent.py -i $input -m $mode

if [ $? -ne 0 ]; then
	echo "Could not start the service..."
	exit;
fi


echo "--------------------------------------------------------------------------"
echo "|			Successfully Started		"
echo "|		   --------------------------		"