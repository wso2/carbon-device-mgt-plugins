/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.application.extension.constants.ApiApplicationConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.iot.virtualfirealarm.plugin.xmpp.XmppConfig;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This is used to create a zip file that includes the necessary configuration required for the agent.
 */
public class ZipUtil {

    private static final Log log = LogFactory.getLog(ZipUtil.class);
    private static final String HTTPS_PORT_PROPERTY = "httpsPort";
    private static final String HTTP_PORT_PROPERTY = "httpPort";

    private static final String LOCALHOST = "localhost";
    private static final String HTTPS_PROTOCOL_APPENDER = "https://";
    private static final String HTTP_PROTOCOL_APPENDER = "http://";
    private static final String CONFIG_TYPE = "general";
    private static final String DEFAULT_MQTT_ENDPOINT = "tcp://localhost:1886";
    public static final String HOST_NAME = "HostName";

    public ZipArchive createZipFile(String owner, String deviceType, String deviceId, String deviceName,
                                    String apiApplicationKey, String token, String refreshToken)
            throws DeviceManagementException {

        String sketchFolder = "repository" + File.separator + "resources" + File.separator + "sketches";
        String archivesPath =
                CarbonUtils.getCarbonHome() + File.separator + sketchFolder + File.separator + "archives" +
                        File.separator + deviceId;
        String templateSketchPath = sketchFolder + File.separator + deviceType;
        String iotServerIP;

        try {
            iotServerIP = getServerUrl();
            String httpsServerPort = System.getProperty(HTTPS_PORT_PROPERTY);
            String httpServerPort = System.getProperty(HTTP_PORT_PROPERTY);
            String httpsServerEP = HTTPS_PROTOCOL_APPENDER + iotServerIP + ":" + httpsServerPort;
            String httpServerEP = HTTP_PROTOCOL_APPENDER + iotServerIP + ":" + httpServerPort;
            String mqttEndpoint = DEFAULT_MQTT_ENDPOINT;
            if (mqttEndpoint.contains(LOCALHOST)) {
                mqttEndpoint = mqttEndpoint.replace(LOCALHOST, iotServerIP);
            }

            String xmppEndpoint = "";
            if (XmppConfig.getInstance().isEnabled()) {
                xmppEndpoint = XmppConfig.getInstance().getHost() + ":" + XmppConfig.getInstance().getPort();
                if (xmppEndpoint.contains(LOCALHOST)) {
                    xmppEndpoint = xmppEndpoint.replace(LOCALHOST, iotServerIP);
                }
            }
            PlatformConfiguration configuration = APIUtil.getTenantConfigurationManagementService().getConfiguration(
                    CONFIG_TYPE);
            if (configuration != null && configuration.getConfiguration() != null && configuration
                    .getConfiguration().size() > 0) {
                List<ConfigurationEntry> configurations = configuration.getConfiguration();
                for (ConfigurationEntry configurationEntry : configurations) {
                    switch (configurationEntry.getName()) {
                        case VirtualFireAlarmUtilConstants.VIRTUAL_FIREALARM_HTTPS_EP:
                            httpsServerEP = (String)configurationEntry.getValue();
                            break;
                        case VirtualFireAlarmUtilConstants.VIRTUAL_FIREALARM_HTTP_EP:
                            httpServerEP = (String)configurationEntry.getValue();
                            break;
                        case VirtualFireAlarmUtilConstants.VIRTUAL_FIREALARM_MQTT_EP:
                            mqttEndpoint = (String)configurationEntry.getValue();
                            break;
                        case VirtualFireAlarmUtilConstants.VIRTUAL_FIREALARM_XMPP_EP:
                            xmppEndpoint = (String)configurationEntry.getValue();
                            break;
                    }
                }
            }
            String base64EncodedApplicationKey = getBase64EncodedAPIAppKey(apiApplicationKey).trim();

            Map<String, String> contextParams = new HashMap<>();
            contextParams.put(VirtualFireAlarmUtilConstants.TENANT_DOMAIN, APIUtil.getTenantDomainOftheUser());
            contextParams.put(VirtualFireAlarmUtilConstants.DEVICE_OWNER, owner);
            contextParams.put(VirtualFireAlarmUtilConstants.DEVICE_ID, deviceId);
            contextParams.put(VirtualFireAlarmUtilConstants.DEVICE_NAME, deviceName);
            contextParams.put(VirtualFireAlarmUtilConstants.HTTPS_EP, httpsServerEP);
            contextParams.put(VirtualFireAlarmUtilConstants.HTTP_EP, httpServerEP);
            contextParams.put(VirtualFireAlarmUtilConstants.APIM_EP, httpServerEP);
            contextParams.put(VirtualFireAlarmUtilConstants.MQTT_EP, mqttEndpoint);
            contextParams.put(VirtualFireAlarmUtilConstants.XMPP_EP, "XMPP:" + xmppEndpoint);
            contextParams.put(VirtualFireAlarmUtilConstants.API_APPLICATION_KEY, base64EncodedApplicationKey);
            contextParams.put(VirtualFireAlarmUtilConstants.DEVICE_TOKEN, token);
            contextParams.put(VirtualFireAlarmUtilConstants.DEVICE_REFRESH_TOKEN, refreshToken);
            contextParams.put(VirtualFireAlarmUtilConstants.SERVER_NAME, XmppConfig.getInstance().getServerName() == null
                    ? "" : XmppConfig.getInstance().getServerName());
            contextParams.put(VirtualFireAlarmUtilConstants.SERVER_JID, XmppConfig.getInstance().getJid() == null
                    ? "" : XmppConfig.getInstance().getJid());

            ZipArchive zipFile;
            zipFile = getSketchArchive(archivesPath, templateSketchPath, contextParams, deviceName);
            return zipFile;
        } catch (IOException e) {
            throw new DeviceManagementException("Zip File Creation Failed", e);
        } catch (ConfigurationManagementException e) {
            throw new DeviceManagementException("Failed to retrieve configuration", e);
        }
    }

    private String getBase64EncodedAPIAppKey(String apiAppCredentialsAsJSONString) {

        JSONObject jsonObject = new JSONObject(apiAppCredentialsAsJSONString);
        String consumerKey = jsonObject.get(ApiApplicationConstants.OAUTH_CLIENT_ID).toString();
        String consumerSecret = jsonObject.get(ApiApplicationConstants.OAUTH_CLIENT_SECRET).toString();
        String stringToEncode = consumerKey + ":" + consumerSecret;
        return Base64.encodeBase64String(stringToEncode.getBytes());
    }

    public static String getServerUrl() {
        String hostName = ServerConfiguration.getInstance().getFirstProperty(HOST_NAME);
        try {
            if (hostName == null) {
                hostName = NetworkUtils.getLocalHostname();
            }
        } catch (SocketException e) {
            hostName = "localhost";
            log.warn("Failed retrieving the hostname, therefore set to localhost", e);
        }
        return hostName;
    }

    public static ZipArchive getSketchArchive(String archivesPath, String templateSketchPath, Map contextParams
            , String zipFileName)
            throws DeviceManagementException, IOException {
        String sketchPath = CarbonUtils.getCarbonHome() + File.separator + templateSketchPath;
        FileUtils.deleteDirectory(new File(archivesPath));//clear directory
        FileUtils.deleteDirectory(new File(archivesPath + ".zip"));//clear zip
        if (!new File(archivesPath).mkdirs()) { //new dir
            String message = "Could not create directory at path: " + archivesPath;
            log.error(message);
            throw new DeviceManagementException(message);
        }
        zipFileName = zipFileName + ".zip";
        try {
            Map<String, List<String>> properties = getProperties(sketchPath + File.separator + "sketch" + ".properties");
            List<String> templateFiles = properties.get("templates");

            for (String templateFile : templateFiles) {
                parseTemplate(templateSketchPath + File.separator + templateFile, archivesPath + File.separator + templateFile,
                              contextParams);
            }

            templateFiles.add("sketch.properties");         // ommit copying the props file
            copyFolder(new File(sketchPath), new File(archivesPath), templateFiles);
            createZipArchive(archivesPath);
            FileUtils.deleteDirectory(new File(archivesPath));
            File zip = new File(archivesPath + ".zip");
            return new org.wso2.carbon.device.mgt.iot.virtualfirealarm.service.impl.util.ZipArchive(zipFileName, zip);
        } catch (IOException ex) {
            throw new DeviceManagementException(
                    "Error occurred when trying to read property " + "file sketch.properties", ex);
        }
    }

    private static Map<String, List<String>> getProperties(String propertyFilePath) throws IOException {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(propertyFilePath);

            // load a properties file
            prop.load(input);
            Map<String, List<String>> properties = new HashMap<String, List<String>>();

            String templates = prop.getProperty("templates");
            List<String> list = new ArrayList<String>(Arrays.asList(templates.split(",")));
            properties.put("templates", list);

            final String filename = prop.getProperty("zipfilename");
            list = new ArrayList<String>() {{
                add(filename);
            }};
            properties.put("zipfilename", list);
            return properties;

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    log.error("Failed closing connection", e);
                }
            }
        }
    }

    private static void parseTemplate(String srcFile, String dstFile, Map contextParams) throws IOException {
        //read from file
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(srcFile);
            outputStream = new FileOutputStream(dstFile);
            String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8.toString());
            Iterator iterator = contextParams.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                content = content.replaceAll("\\$\\{" + mapEntry.getKey() + "\\}", mapEntry.getValue().toString());
            }
            IOUtils.write(content, outputStream, StandardCharsets.UTF_8.toString());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    private static void copyFolder(File src, File dest, List<String> excludeFileNames) throws IOException {

        if (src.isDirectory()) {
            //if directory not exists, create it
            if (!dest.exists() && !dest.mkdirs()) {
                String message = "Could not create directory at path: " + dest;
                log.error(message);
                throw new IOException(message);
            }
            //list all the directory contents
            String files[] = src.list();

            if (files == null) {
                log.warn("There are no files insides the directory " + src.getAbsolutePath());
                return;
            }

            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyFolder(srcFile, destFile, excludeFileNames);
            }

        } else {
            for (String fileName : excludeFileNames) {
                if (src.getName().equals(fileName)) {
                    return;
                }
            }
            //if file, then copy it
            //Use bytes stream to support all file types
            InputStream in = null;
            OutputStream out = null;

            try {
                in = new FileInputStream(src);
                out = new FileOutputStream(dest);

                byte[] buffer = new byte[1024];

                int length;
                //copy the file content in bytes
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
    }

    private static boolean createZipArchive(String srcFolder) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = null;

        try {
            final int BUFFER = 2048;
            FileOutputStream dest = new FileOutputStream(new File(srcFolder + ".zip"));
            out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];
            File subDir = new File(srcFolder);
            String subdirList[] = subDir.list();
            if (subdirList == null) {
                log.warn("The sub directory " + subDir.getAbsolutePath() + " is empty");
                return false;
            }
            for (String sd : subdirList) {
                // get a list of files from current directory
                File f = new File(srcFolder + "/" + sd);
                if (f.isDirectory()) {
                    String files[] = f.list();

                    if (files == null) {
                        log.warn("The current directory " + f.getAbsolutePath() + " is empty. Has no files");
                        return false;
                    }

                    for (int i = 0; i < files.length; i++) {
                        FileInputStream fi = new FileInputStream(srcFolder + "/" + sd + "/" + files[i]);
                        origin = new BufferedInputStream(fi, BUFFER);
                        ZipEntry entry = new ZipEntry(sd + "/" + files[i]);
                        out.putNextEntry(entry);
                        int count;
                        while ((count = origin.read(data, 0, BUFFER)) != -1) {
                            out.write(data, 0, count);
                            out.flush();
                        }

                    }
                } else //it is just a file
                {
                    FileInputStream fi = new FileInputStream(f);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(sd);
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                        out.flush();
                    }
                }
            }
            out.flush();
        } finally {
            if (origin != null) {
                origin.close();
            }
            if (out != null) {
                out.close();
            }
        }
        return true;
    }
}
