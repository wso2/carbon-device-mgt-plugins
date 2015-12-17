#!/bin/bash

echo "----------------------------------------------------------------"
echo "|		WSO2 IOT Sample				"
echo "|		  RaspiAlarm				"
echo "|	       ----------------				"
echo "|    ....initializing startup-script	"
echo "----------------------------------------------------------------"

currentDir=$PWD

cd /var/lib/dpkg/info
sudo rm -rf wso2-raspi-alarm*
dpkg --remove --force-remove-reinstreq wso2-raspi-alarm

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
    echo "dpkg --remove --force-remove-reinstreq wso2-raspi-alarm"
    echo "exit"
    echo "----------------------------------------------------------------"
    echo "Retry Installation...."
    break;
fi


for f in ./deviceConfigs.cfg; do
    ## Check if the glob gets expanded to existing files.
    ## If not, f here will be exactly the pattern above
    ## and the exists test will evaluate to false.
    # [ -e "$f" ] && echo "'wso2-raspi-alarm_1.0_armhf.deb' file found and installing" || echo "'wso2-raspi-alarm_1.0_armhf.deb' file does not exist in current path"; exit;
    if [ -e "$f" ]; then
    	echo "Configuration file found......"
    else
    	echo "'deviceConfigs.cfg' file does not exist in current path. \nExiting installation..."; 
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

#sudo apt-get install python-pip
sudo pip install sleekxmpp
sudo pip install pyasn1 pyasn1-modules


echo "Running the RaspberryAgent service...."
# sudo service RaspberryService.sh start

while true; do
    read -p "Whats the time-interval (in seconds) between successive Data-Pushes to the WSO2-DC (ex: '60' indicates 1 minute) > " input

    if [ $input -eq $input 2>/dev/null ]
    then
        echo "Setting data-push interval to $input seconds."
        break;
    else
        echo "Input needs to be an integer indicating the number seconds between successive data-pushes."
    fi
done

sudo nohup ./RaspberryStats.py -i $input > /dev/null 2>&1 &

if [ $? -ne 0 ]; then
	echo "Could not start the service..."
	exit;
fi


echo "--------------------------------------------------------------------------"
echo "|			Successfully Started		"
echo "---------------------------------------------------------------------------"
