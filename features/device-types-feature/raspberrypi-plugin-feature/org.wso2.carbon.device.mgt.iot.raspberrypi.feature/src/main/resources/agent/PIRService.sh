#!/bin/sh

### BEGIN INIT INFO
# Provides:          PIRService
# Required-Start:    $remote_fs $syslog $network
# Required-Stop:     $remote_fs $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: WSO2-IOT RPi Service
# Description:       RPi Service used to Publish RPi Stats to the WSO2 Device Cloud
### END INIT INFO

PATH=/sbin:/bin:/usr/sbin:/usr/bin:/usr/local/sbin:/usr/local/bin
DESC="This service is used to publish events from the Raspberry Pi PIR to the WSO2 Device Cloud"
NAME=RaspberryPIRStats

DIR=/usr/local/src/RaspberryAgent/
DAEMON=$DIR/pir_sensor-1.0-SNAPSHOT-jar-with-dependencies.jar
DAEMON_NAME=$NAME
SCRIPTNAME=PIRService.sh

# The process ID of the script when it runs is stored here:
PIDFILE=/var/run/$DAEMON_NAME.pid

# Add any command line options for your daemon here
#DAEMON_OPTS="-l /usr/local/src/RaspberryAgent/RaspberryStats.log -m N -i 60"

# This next line determines what user the script runs as.
DAEMON_USER=root   #pi

# Load the VERBOSE setting and other rcS variables
. /lib/init/vars.sh

# Define LSB log_* functions.
# Depend on lsb-base (>= 3.2-14) to ensure that this file is present
# and status_of_proc is working.
. /lib/lsb/init-functions

do_start () {
    log_daemon_msg "Starting system $DAEMON_NAME daemon"
    start-stop-daemon --start --background --pidfile $PIDFILE --make-pidfile --user $DAEMON_USER --chuid $DAEMON_USER --startas $DAEMON
    java -jar $DAEMON &
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
