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

destination="/usr/local/src/RaspberryAgent"
currentDir=$PWD
if [ ! -d "$destination" ]
then
    mkdir $destination
fi
sudo cp $currentDir/deviceConfig.properties $currentDir/src
sudo cp -r $currentDir/src $destination
sudo chmod +x $destination/src/RaspberryAgent.py
sudo update-rc.d -f RaspberryService.sh remove
sudo cp $currentDir/RaspberryService.sh /etc/init.d
sudo chmod +x /etc/init.d/RaspberryService.sh
sudo update-rc.d RaspberryService.sh defaults
sudo service RaspberryService.sh start
