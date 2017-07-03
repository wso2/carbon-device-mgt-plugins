import json
import urllib
import iotUtils
import requests


applicationKey = None
refreshToken = None
filename = "deviceConfig.properties"


class RefreshToken():

    def post(self, url, payload, appKey):
        headers = { 'Authorization' : 'Basic ' + appKey, 'Content-Type' : 'application/x-www-form-urlencoded' }
        baseUrl = iotUtils.HTTP_EP + url
        response = requests.post(baseUrl, params=payload, headers=headers);
        return response

    def read_server_conf(self):
        with open(filename, 'r') as outfile:
            conf_file = outfile.readlines()

        return conf_file

    def updateFile(self, response):
        newRefreshToken = response['refresh_token']
        newAccessToken = response['access_token']

        with open(filename, 'r+') as f:
            lines = f.readlines()
            f.seek(0)
            f.truncate()
            for line in lines:
                if line.__contains__("auth-token="):
                    line = "auth-token=" + newAccessToken + "\n"
                if line.__contains__("refresh-token="):
                    line = "refresh-token=" + newRefreshToken + "\n"
                f.write(line)


    def updateTokens(self,):
        global applicationKey
        global refreshToken
        refreshToken = iotUtils.REFRESH_TOKEN
        applicationKey = iotUtils.APPLICATION_KEY

        params = urllib.urlencode({"grant_type": "refresh_token", "refresh_token": refreshToken,
                                   "scope": "Enroll device"})
        data = self.post("/token", params, applicationKey)
        response = json.loads(data)
        self.updateFile(response)
        return response