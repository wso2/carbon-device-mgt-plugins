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

#!/bin/sh

### BEGIN INIT INFO
# Provides:          RaspberryService
# Required-Start:    $remote_fs $syslog $network
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: WSO2-IOT RPi Service
# Description:       RPi Service used to Publish RPi Stats to the WSO2 Device Cloud
### END INIT INFO

PATH=/sbin:/bin:/usr/sbin:/usr/bin:/usr/local/sbin:/usr/local/bin
# Change the next 3 lines to suit where you install your script and what you want to call it
DESC="This service is used to publish events from the Raspberry Pi to the WSO2 Device Cloud"
NAME=RaspberryStats

DIR=/usr/local/src/RaspberryAgent/src/
DAEMON=$DIR/RaspberryAgent.py
DAEMON_NAME=$NAME
SCRIPTNAME=RaspberryService.sh

# The process ID of the script when it runs is stored here:
PIDFILE=/var/run/$DAEMON_NAME.pid

# Add any command line options for your daemon here
DAEMON_OPTS="-l /usr/local/src/RaspberryAgent/RaspberryStats.log -m N -i 60"

# This next line determines what user the script runs as.
# Root generally not recommended but necessary if you are using the Raspberry Pi GPIO from Python.
DAEMON_USER=root   #pi

# Load the VERBOSE setting and other rcS variables
. /lib/init/vars.sh

# Define LSB log_* functions.
# Depend on lsb-base (>= 3.2-14) to ensure that this file is present
# and status_of_proc is working.
. /lib/lsb/init-functions

do_start () {
    log_daemon_msg "Starting system $DAEMON_NAME daemon"
    start-stop-daemon --start --background --pidfile $PIDFILE --make-pidfile --user $DAEMON_USER --chuid $DAEMON_USER --startas $DAEMON -- $DAEMON_OPTS
    log_end_msg $?
}
do_stop () {
    log_daemon_msg "Stopping system $DAEMON_NAME daemon"
    start-stop-daemon --stop --pidfile $PIDFILE --retry 10
    log_end_msg $?
}

case "$1" in

    start|stop)
        do_${1}
        ;;

    restart|reload|force-reload)
        do_stop
        do_start
        ;;

    status)
        status_of_proc "$DAEMON_NAME" "$DAEMON" && exit 0 || exit $?
        ;;

    getdeviceid)
        $DIR/getMac.sh && exit 0 || exit $?
        ;;

    *)
        #echo Usage: /etc/init.d/$DAEMON_NAME {start|stop|restart|status}"
	echo "Usage: /etc/init.d/$SCRIPTNAME {start|stop|restart|status}"
	exit 1
        ;;

esac
exit 0
