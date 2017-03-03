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

package org.wso2.carbon.device.mgt.iot.raspberrypi.service.impl.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    private static final String LOCALHOST = "localhost";
    private static final String HTTPS_PROTOCOL_URL = "https://${iot.gateway.host}:${iot.gateway.https.port}";
    private static final String HTTP_PROTOCOL_URL = "http://${iot.gateway.host}:${iot.gateway.http.port}";
    private static final String CONFIG_TYPE = "general";
    private static final String DEFAULT_MQTT_ENDPOINT = "tcp://${mqtt.broker.host}:${mqtt.broker.port}";

    public ZipArchive createZipFile(String owner, String tenantDomain, String deviceType,
                                    String deviceId, String deviceName, String token,
                                    String refreshToken) throws DeviceManagementException {

        String sketchFolder = "repository" + File.separator + "resources" + File.separator + "sketches";
        String templateSketchPath = sketchFolder + File.separator + deviceType;
        String iotServerIP;

        try {
            iotServerIP = getServerUrl();
            String httpsServerEP = Utils.replaceSystemProperty(HTTPS_PROTOCOL_URL);
            String httpServerEP = Utils.replaceSystemProperty(HTTP_PROTOCOL_URL);
            String mqttEndpoint = Utils.replaceSystemProperty(DEFAULT_MQTT_ENDPOINT);
            if (mqttEndpoint.contains(LOCALHOST)) {
                mqttEndpoint = mqttEndpoint.replace(LOCALHOST, iotServerIP);
                httpsServerEP = httpsServerEP.replace(LOCALHOST, iotServerIP);
                httpServerEP = httpServerEP.replace(LOCALHOST, iotServerIP);
            }
            PlatformConfiguration configuration = APIUtil.getTenantConfigurationManagementService().getConfiguration(
                    CONFIG_TYPE);
            if (configuration != null && configuration.getConfiguration() != null && configuration
                    .getConfiguration().size() > 0) {
                List<ConfigurationEntry> configurations = configuration.getConfiguration();
                for (ConfigurationEntry configurationEntry : configurations) {
                    switch (configurationEntry.getName()) {
                        case "RASPBERRYPI_HTTPS_EP":
                            httpsServerEP = (String)configurationEntry.getValue();
                            break;
                        case "RASPBERRYPI_HTTP_EP":
                            httpServerEP = (String)configurationEntry.getValue();
                            break;
                        case "RASPBERRYPI_MQTT_EP":
                            mqttEndpoint = (String)configurationEntry.getValue();
                            break;
                    }
                }
            }

            Map<String, String> contextParams = new HashMap<>();
            contextParams.put("SERVER_NAME", APIUtil.getTenantDomainOftheUser());
            contextParams.put("DEVICE_OWNER", owner);
            contextParams.put("DEVICE_ID", deviceId);
            contextParams.put("DEVICE_NAME", deviceName);
            contextParams.put("HTTPS_EP", httpsServerEP);
            contextParams.put("HTTP_EP", httpServerEP);
            contextParams.put("APIM_EP", httpsServerEP);
            contextParams.put("MQTT_EP", mqttEndpoint);
            contextParams.put("DEVICE_TOKEN", token);
            contextParams.put("DEVICE_REFRESH_TOKEN", refreshToken);

            ZipArchive zipFile;
            zipFile = getSketchArchive(templateSketchPath, contextParams, deviceName);
            return zipFile;
        } catch (IOException e) {
            throw new DeviceManagementException("Zip File Creation Failed", e);
        } catch (ConfigurationManagementException e) {
            throw new DeviceManagementException("Failed to retrieve configuration", e);
        }
    }

    private static String getServerUrl() {
        try {
            return org.apache.axis2.util.Utils.getIpAddress();
        } catch (SocketException e) {
            log.warn("Failed retrieving the hostname, therefore set to localhost", e);
            return "localhost";
        }
    }

    private ZipArchive getSketchArchive(String templateSketchPath, Map contextParams
            , String zipFileName)
            throws DeviceManagementException, IOException {
        String sketchPath = CarbonUtils.getCarbonHome() + File.separator + templateSketchPath;
        zipFileName = zipFileName + ".zip";
        try {
            Map<String, List<String>> properties = getProperties(sketchPath + File.separator + "sketch" + ".properties");
            List<String> templateFiles = properties.get("templates");
            List<TemplateFile> processTemplateFiles = new ArrayList<>();

            for (String templateFile : templateFiles) {
                TemplateFile tFile = new TemplateFile();
                tFile.setContent(parseTemplate(templateSketchPath + File.separator + templateFile, contextParams));
                tFile.setFileName(templateFile);
                processTemplateFiles.add(tFile);
            }

            templateFiles.add("sketch.properties");         // ommit copying the props file

            byte[] zip =  createZipArchive(templateSketchPath, processTemplateFiles);
            return new ZipArchive(zipFileName, zip);
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

    private static String parseTemplate(String srcFile, Map contextParams) throws IOException {
        //read from file
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(srcFile);
            String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8.toString());
            Iterator iterator = contextParams.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                content = content.replaceAll("\\$\\{" + mapEntry.getKey() + "\\}", mapEntry.getValue().toString());
            }
            return content;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private static byte[] createZipArchive(String srcFolder, List<TemplateFile> processTemplateFiles) throws IOException {
        ZipOutputStream out = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            out = new ZipOutputStream(new BufferedOutputStream(baos));
            File subDir = new File(srcFolder);
            String subdirList[] = subDir.list();
            if (subdirList == null) {
                log.warn("The sub directory " + subDir.getAbsolutePath() + " is empty");
                return null;
            }
            for (String sd : subdirList) {
                // get a list of files from current directory
                File f = new File(srcFolder + File.separator + sd);
                if (f.isDirectory()) {
                    String files[] = f.list();

                    if (files == null) {
                        log.warn("The current directory " + f.getAbsolutePath() + " is empty. Has no files");
                        return null;
                    }

                    for (int i = 0; i < files.length; i++) {
                        boolean fileAdded = false;
                        for (TemplateFile templateFile : processTemplateFiles) {
                            if (files[i].equals(templateFile.getFileName())) {
                                ZipEntry entry = new ZipEntry(templateFile.getFileName());
                                out.putNextEntry(entry);
                                out.write(templateFile.getContent().getBytes());
                                out.closeEntry();
                                fileAdded = true;
                                break;
                            } else if (f.getName().equals("sketch.properties")) {
                                fileAdded = true;
                                break;
                            }
                        }
                        if (fileAdded) {
                            continue;
                        }
                        ZipEntry entry = new ZipEntry(sd + File.separator + files[i]);
                        out.putNextEntry(entry);
                        out.write(IOUtils.toByteArray(new FileInputStream(srcFolder + File.separator + sd
                                                                                  + File.separator + files[i])));
                        out.closeEntry();

                    }
                } else //it is just a file
                {
                    boolean fileAdded = false;
                    for (TemplateFile templateFile : processTemplateFiles) {
                        if (f.getName().equals(templateFile.getFileName())) {
                            ZipEntry entry = new ZipEntry(templateFile.getFileName());
                            out.putNextEntry(entry);
                            out.write(templateFile.getContent().getBytes());
                            out.closeEntry();
                            fileAdded = true;
                            break;
                        } else if (f.getName().equals("sketch.properties")) {
                            fileAdded = true;
                            break;
                        }
                    }
                    if (fileAdded) {
                        continue;
                    }
                    ZipEntry entry = new ZipEntry(sd);
                    out.putNextEntry(entry);
                    out.write(IOUtils.toByteArray(new FileInputStream(f)));
                    out.closeEntry();
                }
            }
            out.finish();
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return baos.toByteArray();
    }

    public class TemplateFile {
        private String content;
        private String fileName;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }
}
