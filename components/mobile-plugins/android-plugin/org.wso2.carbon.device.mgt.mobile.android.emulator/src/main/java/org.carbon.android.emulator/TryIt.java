/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.carbon.android.emulator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class creates an Android TryIt Emulator to be used as virtual device to connect to WSO2 IOT Cloud
 * or Product-iot.
 */
public class TryIt {
    private String osSuffix;
    private String androidSdkHome;
    private String userHome;
    private String workingDirectory;
    private String adbLocation;                // location of executable file abd
    private String emulatorLocation;           // location of executable file emulator
    private File sdkConfigFile;              // file in which SDK location is written

    /**
     * This method gets the system specific variables.
     */
    private TryIt() {
        osSuffix = System.getProperty(Constants.OS_NAME_PROPERTY);
        if (osSuffix == null) {
            sysPropertyError(Constants.OS_NAME_PROPERTY, "OS Name");
        } else {
            osSuffix = osSuffix.toLowerCase();
        }
        userHome = System.getProperty(Constants.USER_HOME_PROPERTY);
        if (userHome == null) {
            sysPropertyError(Constants.USER_HOME_PROPERTY, "Home Directory");
        }
        workingDirectory = System.getProperty(Constants.USER_DIRECTORY_PROPERTY);
        if (workingDirectory == null) {
            sysPropertyError(Constants.USER_DIRECTORY_PROPERTY, "Current Working Directory");
        }
        if (osSuffix.contains(Constants.WINDOWS_OS)) {
            osSuffix = Constants.WINDOWS_OS;
        }
        if (osSuffix.contains(Constants.MAC)) {
            osSuffix = Constants.MAC_OS;
        }
        System.out.println("Detected OS " + osSuffix);
    }

    /**
     * This method creates an android virtual device.
     *
     * @param args commandline arguments.
     */
    public static void main(String[] args) {
        TryIt tryIt = new TryIt();
        tryIt.setAndroidSDK();
        tryIt.checkBuildTools();
        tryIt.startAVD();
        tryIt.checkEmulatorBoot();
        String[] agents = tryIt.checkForAgent();
        System.out.println("Starting Agent ...");
        tryIt.startPackage(agents);
        ProcessBuilder startShellProcessBuilder = new ProcessBuilder(tryIt.adbLocation, "shell");
        try {
            startShellProcessBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            startShellProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process startShell = startShellProcessBuilder.start();
            System.out.println("Connected to device shell");
            startShell.waitFor();
        } catch (IOException e) {
            tryIt.handleException("Unable to start the shell", e);
        } catch (InterruptedException ignored) {
            //
        }
        System.out.println("\nGood Bye!");
    }


    /**
     * This method downloads the artifacts from remote url.
     *
     * @param remotePath - remote url
     * @param localPath  - local path to download
     */
    private void downloadArtifacts(String remotePath, String localPath) {
        BufferedInputStream in = null;
        FileOutputStream out = null;
        long startingTime = System.currentTimeMillis();

        try {
            URL url = new URL(remotePath);
            URLConnection conn = url.openConnection();
            int size = conn.getContentLength();
            in = new BufferedInputStream(url.openStream());
            out = new FileOutputStream(localPath);
            byte data[] = new byte[1024];
            int count;
            double sumCount = 0.0;

            while ((count = in.read(data, 0, 1024)) != -1) {
                out.write(data, 0, count);
                sumCount += count;
                if ((size > 0 && (System.currentTimeMillis() - startingTime > 5000))
                        || (sumCount / size * 100.0) == 100) {
                    System.out.println("Downloading: "
                            + new DecimalFormat("#.##").format((sumCount / size * 100.0)) + " %");
                    startingTime = System.currentTimeMillis();
                }
            }
        } catch (MalformedURLException e) {
            System.out.println("Error in download URL of " + localPath);
            System.out.println("URL provided " + remotePath);
        } catch (IOException e) {
            if (!new File(localPath).delete()) {
                System.out.println("Delete " + localPath + " and try again");
            }
            handleException("Downloading " + localPath + " failed.", e);
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException ignored) {
                    //
                }
            if (out != null)
                try {
                    out.close();
                } catch (IOException ignored) {
                    //
                }
        }
    }

    /**
     * This method is called when then is an error in getting system properties
     *
     * @param property -property type
     * @param hint     - property name
     */
    private void sysPropertyError(String property, String hint) {
        System.out.println("Unable to get the " + property + " property of your system (" + hint + ")");
        System.exit(1);
    }

    /**
     * This method validates the Android SDK location provided by the user and write it to the file
     * sdkConfigFile.
     */
    private void setSDKPath() {
        System.out.println("Please provide android SDK location : ");
        String response = new Scanner(System.in, StandardCharsets.UTF_8.toString()).next();
        String emulatorLocationPath = response + File.separator + "tools" + File.separator + "emulator";
        if (osSuffix.equals(Constants.WINDOWS_OS)) {
            emulatorLocationPath += Constants.WINDOWS_EXTENSION_BAT;
        }
        if (new File(emulatorLocationPath).exists()) {
            androidSdkHome = response;
            writeToSdkConfigFile(response);
        } else {
            System.out.println("Invalid SDK location");
            setSDKPath();
        }
    }

    /**
     * This method writes the SDK location to a file sdkConfigFile for future use.
     *
     * @param string - SDK location.
     */
    private void writeToSdkConfigFile(String string) {
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(sdkConfigFile), StandardCharsets.UTF_8);
            writer.write(string);
        } catch (IOException e) {
            System.out.println("Writing to " + sdkConfigFile.toString() + " failed.");
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ignored) {
                //
            }
        }
    }


    /**
     * This method creates a folder named android-sdk and downloads the minimum tools for SDK
     * and write the sdk-location to the file sdkConfigFile.
     */
    private void getAndroidSDK() {
        String androidSdkFolderName = "android-sdk";
        if (!new File(workingDirectory + File.separator + androidSdkFolderName).exists()) {
            if (!new File(workingDirectory + File.separator + androidSdkFolderName).mkdir()) {
                System.out.println("Unable to make folder named " + androidSdkFolderName + " in " + workingDirectory);
                System.exit(1);
            }
        }
        androidSdkHome = workingDirectory + File.separator + androidSdkFolderName;
        getTools(System.getProperty(Constants.SDK_TOOLS_URL), "_Android-sdk-tools.zip");
        getTools(System.getProperty(Constants.PLATFORM_TOOLS_URL), "_Android-platform-tools.zip");
        writeToSdkConfigFile(androidSdkHome);
    }

    /**
     * This method downloads and extracts the tools.
     *
     * @param url        - the URL to download from.
     * @param folderName - the folder name where to download.
     */
    private void getTools(String url, String folderName) {
        System.out.println("Downloading " + folderName);
        downloadArtifacts(url, androidSdkHome + File.separator + folderName);
        System.out.println("Configuring " + folderName);
        extractFolder(androidSdkHome + File.separator + folderName);
    }

    /**
     * This method starts the AVD specified by the user.
     */
    private void startAVD() {
        String wso2AvdLocation = userHome + File.separator + ".android" + File.separator + "avd" + File.separator
                + Constants.WSO2_AVD_NAME + ".avd";
        checkForPlatform();
        checkForSystemImages();
        if (!new File(wso2AvdLocation).isDirectory()) {
            Scanner read = new Scanner(System.in, StandardCharsets.UTF_8.toString());
            System.out.print("Do you want to create WSO2_AVD with default configs (Y/n)?: ");
            if (read.next().toLowerCase().matches("y")) {
                createAVD();
                return;
            }
        }
        System.out.println("+----------------------------------------------------------------+");
        System.out.println("|                        WSO2 Android TryIt                      |");
        System.out.println("+----------------------------------------------------------------+");

        emulatorLocation = androidSdkHome + File.separator + "tools" + File.separator + "emulator";
        if (osSuffix.equals(Constants.WINDOWS_OS)) {
            emulatorLocation += Constants.WINDOWS_EXTENSION_EXE;
        }
        setExecutePermission(emulatorLocation);
        listAVDs();
    }

    /**
     * This method gets the available AVDs' name from the system.
     */
    private void listAVDs() {
        ArrayList<String> devices = new ArrayList<>();
        BufferedReader reader = null;
        try {
            ProcessBuilder listAVDsProcessBuilder = new ProcessBuilder(emulatorLocation, "-list-avds");
            Process listAVDsProcess = listAVDsProcessBuilder.start();
            reader = new BufferedReader(new InputStreamReader(listAVDsProcess.getInputStream(),
                    StandardCharsets.UTF_8.toString()));
            String readLine;
            while ((readLine = reader.readLine()) != null) {
                devices.add(readLine);
            }
            selectAVD(devices);
        } catch (IOException e) {
            handleException("Unable to list the available AVDs", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ignored) {
                // exception in finally block
            }
        }
    }

    /**
     * This method makes the thread wait.
     *
     * @param milliSec -time to wait for
     */
    private void delay(long milliSec) {
        try {
            Thread.sleep(milliSec);
        } catch (InterruptedException ignored) {
            // interruption in main thread
        }
    }

    /**
     * This method enables the user to select an AVD form available AVDs.
     *
     * @param devices - list of available AVDs.
     */
    private void selectAVD(ArrayList<String> devices) {
        if (devices.size() == 0) {
            System.out.println("No AVDs available in the system ");
            startAVD();
        } else if (devices.size() == 1) {
            runEmulator(devices.get(0));
        } else {
            System.out.println("\nAvailable AVDs in the system\n");
            int count = 1;
            for (String device : devices) {
                System.out.println(count + ") " + device);
                count++;
            }
            System.out.print("\nEnter AVD number to start (eg: 1) :");
            Scanner read = new Scanner(System.in, StandardCharsets.UTF_8.toString());
            int avdNo = read.nextInt();
            runEmulator(devices.get(--avdNo));
        }
    }

    /**
     * This method creates WSO2_AVD with the specific configurations.
     */
    private void createAVD() {
        String avdManagerPath = androidSdkHome + File.separator + "tools" + File.separator + "bin"
                + File.separator + "avdmanager";
        String androidPath = androidSdkHome + File.separator + "tools" + File.separator + "android";
        if (osSuffix.equals(Constants.WINDOWS_OS)) {
            avdManagerPath += Constants.WINDOWS_EXTENSION_BAT;
            androidPath += Constants.WINDOWS_EXTENSION_BAT;
        }
        setExecutePermission(androidPath);
        System.out.println("Creating a new AVD device");
        try {
            if (new File(avdManagerPath).exists()) {
                setExecutePermission(avdManagerPath);
                ProcessBuilder createAvdProcessBuilder = new ProcessBuilder(avdManagerPath, "create", "avd", "-k",
                        "system-images;android-23;default;x86", "-n", Constants.WSO2_AVD_NAME);
                createAvdProcessBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
                createAvdProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                Process createAvdProcess = createAvdProcessBuilder.start();
                createAvdProcess.waitFor();
            } else {
                ProcessBuilder createAvd = new ProcessBuilder(androidPath, "create", "avd", "-n",
                        Constants.WSO2_AVD_NAME, "-t", "android-23");
                createAvd.redirectInput(ProcessBuilder.Redirect.INHERIT);
                createAvd.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                Process createAvdProcess = createAvd.start();
                createAvdProcess.waitFor();
            }
        } catch (IOException e) {
            handleException("Unable to create " + Constants.WSO2_AVD_NAME, e);
        } catch (InterruptedException ignored) {
            // interruption in main thread
        }
        copyDefaultWSO2Configs();
        startAVD();
    }

    /**
     * This method replaces the default configurations provided in the resources to the WSoO2 AVD created
     */
    private void copyDefaultWSO2Configs() {
        String configFileLocation = workingDirectory + Constants.WSO2_CONFIG_LOCATION;
        String wso2ConfigFile = userHome + File.separator + ".android" + File.separator + "avd" + File.separator
                + Constants.WSO2_AVD_NAME + ".avd" + File.separator + "config.ini";
        try {
            Files.copy(Paths.get(configFileLocation), Paths.get(wso2ConfigFile), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            System.out.println("WARN : Failed to have WSO2 default AVD configurations");
        }
    }

    /**
     * This method runs the Android Emulator for the name specified by deviceId.
     *
     * @param deviceId String name of the device.
     */
    private void runEmulator(String deviceId) {
        if (osSuffix.equals(Constants.MAC_OS) || osSuffix.equals(Constants.WINDOWS_OS)) {
            installHAXM();
        }
        System.out.println("Starting : " + deviceId);
        startEmulator(deviceId);
        checkCacheImg(deviceId);
    }

    /**
     * This method checks for the availability of android build tools in SDK location to run the AVD.
     */
    private void checkBuildTools() {
        File buildTools = new File(androidSdkHome + File.separator + "build-tools"
                + File.separator + System.getProperty(Constants.BUILD_TOOLS_VERSION));
        if (!buildTools.exists()) {
            getTools(System.getProperty(Constants.BUILD_TOOL_URL), "_Android-build-tool.zip");
            File buildTool = new File(androidSdkHome + File.separator
                    + System.getProperty(Constants.DOWNLOADED_BUILD_TOOL_NAME));
            if (!new File(androidSdkHome + File.separator + "build-tools").exists()
                    && !new File(androidSdkHome + File.separator + "build-tools").mkdir()) {
                makeDirectoryError("build-tools", androidSdkHome);
            }
            buildTool.renameTo(new File(androidSdkHome + File.separator + "build-tools"
                    + File.separator + System.getProperty(Constants.BUILD_TOOLS_VERSION)));
        }
    }

    /**
     * This method make sure whether the directory can be created.
     *
     * @param name     - name of the folder to be made
     * @param location - location to make folder
     */
    private void makeDirectoryError(String name, String location) {
        System.out.println("Unable to make folder named " + name + " in " + location);
        System.exit(1);
    }

    /**
     * This method halts the system until the emulator is fully booted
     * if boot process is not completed successfully, rest of the tasks won't be continued.
     */
    private void checkEmulatorBoot() {
        BufferedReader reader = null;
        String readLine;
        Boolean sysBootComplete = false;
        do {
            ProcessBuilder systemBoot = new ProcessBuilder(adbLocation, "shell", "getprop",
                    "sys.boot_completed");
            try {
                Process systemBootProcess = systemBoot.start();
                systemBootProcess.waitFor();
                reader = new BufferedReader(new InputStreamReader(systemBootProcess.getInputStream(),
                        StandardCharsets.UTF_8));
                while ((readLine = reader.readLine()) != null) {
                    // if boot process is success the process gives 1 as output
                    if (readLine.contains("1")) {
                        sysBootComplete = true;
                    }
                }
                System.out.print(".");
                delay(1000);
            } catch (IOException e) {
                System.out.println("WARN : Unable to check boot process");
            } catch (InterruptedException ignored) {
                //interruption in main thread
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ignored) {
                }
            }
        } while (!sysBootComplete);
        System.out.println();
    }

    /**
     * This method gets the Android SDK location if available and sets the SDK path else downloads the SDK.
     */
    private void setAndroidSDK() {
        sdkConfigFile = new File("sdkConfigLocation");
        if (!(sdkConfigFile.exists() && !sdkConfigFile.isDirectory())) {
            //TODO
            Scanner read = new Scanner(System.in, StandardCharsets.UTF_8.toString());
            System.out.print("Do you have an Android SDK installed on your computer (y/N) ? : ");
            String response = read.next().toLowerCase();
            if (response.matches("y")) {
                setSDKPath();
            } else {
                getAndroidSDK();
            }
        } else {
            Scanner scanner = null;
            try {
                scanner = new Scanner(sdkConfigFile, StandardCharsets.UTF_8.toString());
                androidSdkHome = scanner.useDelimiter("\\Z").next();
            } catch (FileNotFoundException ignored) {
                //
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
            }
        }
        adbLocation = androidSdkHome + File.separator + "platform-tools" + File.separator + "adb";
        if (osSuffix.equals(Constants.WINDOWS_OS)) {
            adbLocation += Constants.WINDOWS_EXTENSION_EXE;
        }
        setExecutePermission(adbLocation);
    }

    /**
     * this method prints the exception and terminate the program.
     *
     * @param message -exception method to be printed
     * @param ex      - exception caught
     */
    private void handleException(String message, Exception ex) {
        System.out.println(message);
        ex.printStackTrace();
        System.exit(1);
    }

    /**
     * This method check for the android agent in the specified AVD and installs it if not available.
     *
     * @return package name and act name.
     */
    private String[] checkForAgent() {
        String pkg = null;
        String activity = null;
        String readLine;
        BufferedReader reader = null;
        String apkFileLocation = workingDirectory + Constants.APK_LOCATION;
        String aaptLocation = androidSdkHome + File.separator + "build-tools" + File.separator
                + System.getProperty(Constants.BUILD_TOOLS_VERSION) + File.separator + "aapt";
        if (osSuffix.equals(Constants.WINDOWS_OS)) {
            aaptLocation += Constants.WINDOWS_EXTENSION_EXE;
        }
        setExecutePermission(aaptLocation);
        ProcessBuilder badgingApkFileProcessBuilder = new ProcessBuilder(aaptLocation, "d", "badging",
                apkFileLocation);
        try {
            Process badgingApkFileProcess = badgingApkFileProcessBuilder.start();
            reader = new BufferedReader(new InputStreamReader(badgingApkFileProcess.getInputStream(),
                    StandardCharsets.UTF_8));
            while ((readLine = reader.readLine()) != null) {
                if (readLine.contains("package")) {
                    Pattern pattern = Pattern.compile("'(.*?)'");
                    Matcher matcher = pattern.matcher(readLine);
                    if (matcher.find()) {
                        pkg = matcher.group(1);
                    }
                }
                if (readLine.contains("launchable-activity")) {
                    Pattern pattern = Pattern.compile("'(.*?)'");
                    Matcher matcher = pattern.matcher(readLine);
                    if (matcher.find()) {
                        activity = matcher.group(1);
                    }
                }
            }
        } catch (IOException ignored) {
            System.out.println("WARN : Failed to get the available packages");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    //
                }
            }
        }
        if (!checkForPackage(pkg)) {
            installAgent();
        }
        return new String[]{pkg, activity};
    }

    /**
     * This method check whether the package is available in the AVD.
     *
     * @param pkg - name og package to check for.
     * @return - available or not.
     */
    private boolean checkForPackage(String pkg) {
        String readLine;
        BufferedReader reader = null;
        Boolean hasAgent = false;
        ProcessBuilder listPackages = new ProcessBuilder(adbLocation, "shell", "pm", "list", "packages");
        try {
            Process listPackagesProcess = listPackages.start();
            listPackagesProcess.waitFor();
            reader = new BufferedReader(new InputStreamReader(listPackagesProcess.getInputStream(),
                    StandardCharsets.UTF_8));
            while ((readLine = reader.readLine()) != null) {
                if (readLine.contains("package:" + pkg)) {
                    hasAgent = true;
                }
            }
        } catch (IOException | InterruptedException ignored) {
            System.out.println("WARN : Failed to check the available packages, agent will be installed");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ignored) {
                //
            }
        }
        return hasAgent;
    }

    /**
     * This method installs the Android Agent ( WSO2 iot agent ).
     */
    private void installAgent() {
        String androidAgentLocation = workingDirectory + Constants.APK_LOCATION;
        System.out.println("Installing agent ...");
        ProcessBuilder installAgentProcessBuilder = new ProcessBuilder(adbLocation, "install",
                androidAgentLocation);
        try {
            Process installAgentProcess = installAgentProcessBuilder.start();
            installAgentProcess.waitFor();
        } catch (Exception e) {
            System.out.println("WSO2 Agent installation failed");
            //TODO
            Scanner read = new Scanner(System.in, StandardCharsets.UTF_8.toString());
            System.out.println("Do you want to install agent again (Y/N) ? ");
            if (read.next().toLowerCase().matches("y")) {
                installAgent();
            }
        }
    }

    /**
     * This method starts the package (wso2.iot.agent).
     *
     * @param agents package name and launchable activity name.
     */
    private void startPackage(String[] agents) {
        String pkg = agents[0];
        String activity = agents[1];
        ProcessBuilder pkgStartProcessBuilder = new ProcessBuilder(adbLocation, "shell", "am", "start",
                "-n", pkg + "/" + activity);
        try {
            Process pkgStartProcess = pkgStartProcessBuilder.start();
            pkgStartProcess.waitFor();
        } catch (InterruptedException ignored) {
            //
        } catch (IOException e) {
            handleException("Unable to start WSO2 package", e);
        }
    }

    /**
     * This method checks for the availability of Android Platform in SDK and if not available downloads it.
     */
    private void checkForPlatform() {
        File platform = new File(androidSdkHome + File.separator + "platforms" + File.separator
                + System.getProperty(Constants.TARGET_VERSION));
        if (!platform.isDirectory()) {
            getTools(System.getProperty(Constants.PLATFORM_URL), "_Android-platforms.zip");
            if (!new File(androidSdkHome + File.separator + "platforms").exists()
                    && !new File(androidSdkHome + File.separator + "platforms").mkdir()) {
                makeDirectoryError("platforms", androidSdkHome);
            }
            //noinspection ResultOfMethodCallIgnored
            new File(androidSdkHome + File.separator + System.getProperty(Constants.DOWNLOADED_PLATFORM_NAME)).
                    renameTo(new File(androidSdkHome + File.separator + "platforms"
                            + File.separator + System.getProperty(Constants.TARGET_VERSION)));
        }
    }

    /**
     * This method checks for the system images in the Android SDK and downloads if not available.
     */
    private void checkForSystemImages() {
        File systemImages = new File(androidSdkHome + File.separator + "system-images"
                + File.separator + System.getProperty(Constants.TARGET_VERSION) + File.separator + "default");

        if (!systemImages.isDirectory()) {
            getTools(System.getProperty(Constants.SYSTEM_IMAGE_URL), "_sys-images.zip");
            //noinspection ResultOfMethodCallIgnored
            new File(androidSdkHome + File.separator + "system-images" + File.separator
                    + System.getProperty(Constants.TARGET_VERSION) + File.separator + "default").mkdirs();
            //noinspection ResultOfMethodCallIgnored
            new File(androidSdkHome + File.separator + System.getProperty(Constants.OS_TARGET))
                    .renameTo(new File(androidSdkHome + File.separator + "system-images" + File.separator
                            + System.getProperty(Constants.TARGET_VERSION) + File.separator + "default"
                            + File.separator + System.getProperty(Constants.OS_TARGET)));
        }
    }

    /**
     * This method install Hardware_Accelerated Execution_Manager in mac and windows os.
     */
    private void installHAXM() {
        String haxmLocation = androidSdkHome + File.separator + "extras" + File.separator + "intel"
                + File.separator + "Hardware_Accelerated_Execution_Manager";

        if (!new File(haxmLocation).isDirectory()) {
            //System.out.println("Downloading intel HAXM...");
            if (!new File(haxmLocation).mkdirs()) {
                makeDirectoryError(haxmLocation, androidSdkHome);
            }
            String folderName = "_haxm.zip";
            getTools(System.getProperty(Constants.HAXM_URL), haxmLocation + File.separator
                    + folderName);
            String haxmInstaller = haxmLocation + File.separator + "silent_install";
            if (osSuffix.equals(Constants.WINDOWS_OS)) {
                haxmInstaller += Constants.WINDOWS_EXTENSION_BAT;
            } else {
                haxmInstaller += Constants.MAC_HAXM_EXTENSION;
            }
            setExecutePermission(haxmInstaller);

            ProcessBuilder processBuilder = new ProcessBuilder(haxmInstaller, "-m", "2048", "-log",
                    workingDirectory + File.separator + "haxmSilentRun.log");
            processBuilder.directory(new File(haxmLocation));
            processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            try {
                Process process = processBuilder.start();
                process.waitFor();
            } catch (IOException | InterruptedException e) {
                System.out.println("HAXM installation failed, install HAXM and try again");
            }
            System.out.println("Please restart your machine and run again.");
            System.exit(0);
        }
    }

    /**
     * This method starts the Android emulator for specific device name.
     *
     * @param deviceId - name of the device to start the emulator.
     */
    private void startEmulator(String deviceId) {
        String qemuSystemFileLocation = androidSdkHome + File.separator + "tools" + File.separator
                + "qemu" + File.separator;

        switch (osSuffix) {
            case Constants.MAC_OS:
                qemuSystemFileLocation += Constants.MAC_DARWIN + "-x86_64" + File.separator + "qemu-system-i386";
                break;
            case Constants.WINDOWS_OS:
                qemuSystemFileLocation += osSuffix + "-x86_64" + File.separator + "qemu-system-i386.exe";
                break;
            default:
                qemuSystemFileLocation += osSuffix + "-x86_64" + File.separator + "qemu-system-i386";
        }
        setExecutePermission(qemuSystemFileLocation);

        qemuSystemFileLocation = androidSdkHome + File.separator + "emulator" + File.separator
                + "qemu" + File.separator;

        switch (osSuffix) {
            case Constants.MAC_OS:
                qemuSystemFileLocation += Constants.MAC_DARWIN + "-x86_64" + File.separator + "qemu-system-i386";
                break;
            case Constants.WINDOWS_OS:
                qemuSystemFileLocation += osSuffix + "-x86_64" + File.separator + "qemu-system-i386.exe";
                break;
            default:
                qemuSystemFileLocation += osSuffix + "-x86_64" + File.separator + "qemu-system-i386";
        }

        killServer();
        setExecutePermission(qemuSystemFileLocation);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new TryItEmulator(deviceId, emulatorLocation));
    }

    /**
     * This method ensures device properly starts.
     */
    private void killServer() {
        ProcessBuilder processBuilderKillServer = new ProcessBuilder(adbLocation, "kill-server");
        Process processKillServer = null;
        try {
            processKillServer = processBuilderKillServer.start();
        } catch (IOException ignored) {
            System.out.println("If the device doesn't start properly, stop running the script and restart again");
        }
        try {
            if (processKillServer != null) {
                processKillServer.waitFor();
            }
        } catch (InterruptedException ignored) {
            System.out.println("If the device doesn't start properly, stop running the script and restart again");
        }
    }

    /**
     * This method halts the system the cache.img file is created for the particular AVD started.
     *
     * @param deviceId - name of the AVD.
     */
    private void checkCacheImg(String deviceId) {
        File cacheImg = new File(userHome + File.separator + ".android"
                + File.separator + "avd" + File.separator + deviceId + ".avd" + File.separator + "cache.img");
        while (!cacheImg.exists()) {
            System.out.print(".");
            delay(1000);
        }
        System.out.println();
    }

    /**
     * This method sets the executable permission for the specified file,
     * if the files are not the executable, the process cannot be continued.
     *
     * @param fileName name of the file to set execution permission.
     */
    private void setExecutePermission(String fileName) {
        if (new File((fileName)).exists()) {
            if (!new File(fileName).canExecute()) {
                if (!new File(fileName).setExecutable(true)) {
                    System.out.println("Unable to set the execute permission of : " + fileName);
                    System.out.println("Please set the executable permission for file "
                            + new File(fileName).getAbsolutePath() + " to continue");
                    System.exit(1);      // if can't execute, unable to proceed
                }
            }
        }
    }

    /**
     * This method extracts the zip folder.
     *
     * @param zipFile -Name of zip to extract
     */
    private void extractFolder(String zipFile) {
        int BUFFER = 2048;
        File file = new File(zipFile);
        ZipFile zip;
        try {
            zip = new ZipFile(file);
            String newPath = zipFile.substring(0, zipFile.lastIndexOf(File.separator));
            new File(newPath).mkdirs();
            Enumeration zipFileEntries = zip.entries();
            while (zipFileEntries.hasMoreElements()) {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                File destFile = new File(newPath, currentEntry);
                File destinationParent = destFile.getParentFile();
                if (destinationParent == null) {
                    destFile.mkdirs();
                    continue;
                } else {
                    //noinspection ResultOfMethodCallIgnored
                    destinationParent.mkdirs();
                }
                if (!entry.isDirectory()) {
                    BufferedInputStream is;
                    try {
                        is = new BufferedInputStream(zip.getInputStream(entry));
                        int currentByte;
                        // establish buffer for writing file
                        byte data[] = new byte[BUFFER];
                        // write the current file to disk
                        FileOutputStream fos = new FileOutputStream(destFile);
                        BufferedOutputStream dest = new BufferedOutputStream(fos,
                                BUFFER);
                        // read and write until last byte is encountered
                        while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, currentByte);
                        }
                        dest.flush();
                        dest.close();
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(0);
                    }
                }
            }
            zip.close();
        } catch (IOException e) {
            handleException("Extraction of " + zipFile + " failed", e);
        }
        if (!new File(zipFile).delete()) {
            System.out.println("Downloaded zip : " + zipFile + " - not deleted");
        }
    }
}
