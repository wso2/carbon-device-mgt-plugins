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

import time
import BaseHTTPServer
import iotUtils
import running_mode
import os
import subprocess
import re

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Class that handles HTTP GET requests for operations on the RPi
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class OnRequestListener(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_GET(request):
        # """Respond to a GET request."""

        if not processURLPath(request.path):
            return

        print request.path.split("/")[1].upper()
        resource = request.path.split("/")[1].upper()
        state = request.path.split("/")[2].upper()
        print "HTTP_SERVER: Resource - " + resource

        if resource == "TEMPERATURE":
            request.send_response(200)
            request.send_header('Content-Type', 'application/json')
            request.send_header('Authorization', 'Bearer ' + iotUtils.AUTH_TOKEN)
            request.end_headers()
            request.wfile.write(iotUtils.LAST_TEMP)

        elif resource == "BULB":
            iotUtils.switchBulb(state)
            print "HTTP_SERVER: Requested Switch State - " + state

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Check the URL string of the request and validate
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def processURLPath(path):
    if path.count("/") != 2 and not "favicon" in path:
        print "HTTP_SERVER: Invalid URL String: " + path
        return False

    resource = path.split("/")[1]

    if not iequal("BULB", resource) and not iequal("TEMPERATURE", resource):
        if not "favicon" in resource:
            print "HTTP_SERVER: Invalid resource - " + resource + " to execute operation"
        return False

    return True
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Case-Insensitive check on whether two string are similar
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def iequal(a, b):
    try:
        return a.upper() == b.upper()
    except AttributeError:
        return a == b

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The Main method of the server script
#			This method is invoked from RaspberryStats.py on a new thread
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def main():
    HOST_NAME = iotUtils.getDeviceIP()
    HTTP_SERVER_PORT = iotUtils.getHTTPServerPort()
    server_class = BaseHTTPServer.HTTPServer

    while True:
        try:
            httpd = server_class((HOST_NAME, HTTP_SERVER_PORT), OnRequestListener)
            print "HTTP_SERVER: " + time.asctime(), "Server Starts - %s:%s" % (HOST_NAME, HTTP_SERVER_PORT)

            httpd.serve_forever()
        except (KeyboardInterrupt, Exception) as e:
            print "HTTP_SERVER: Exception in HttpServerThread (either KeyboardInterrupt or Other)"
            print ("HTTP_SERVER: " + str(e))

            if running_mode.RUNNING_MODE == "N":
                iotUtils.switchBulb("OFF")
            else :
                iotUtils.switchBulb("OFF")
                httpd.server_close()
            print "HTTP_SERVER: " + time.asctime(), "Server Stops - %s:%s" % (HOST_NAME, HTTP_SERVER_PORT)
            print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
            pass
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

if __name__ == '__main__':
    main()
