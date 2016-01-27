#!/bin/bash  

for P in paho-mqtt lxml; do
    dpkg -s "$P" >/dev/null 2>&1 && {
        echo "$P is installed."
    } || {
        sudo pip install paho-mqtt 
	sudo apt-get install python-lxml
    }
done

unzip DigitalDisplay
cp ./deviceConfig.properties ./DigitalDisplay




