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
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private File sdkLocationFile;              // file in which SDK location is written
    private String adbLocation;                // location of executable file abd
    private String emulatorLocation;           // location of executable file emulator

    /**
     * This method gets the system specific variables.
     */
    private TryIt() {
        osSuffix = System.getProperty(Constants.OS_NAME_PROPERTY).toLowerCase();
        userHome = System.getProperty(Constants.USER_HOME_PROPERTY);
        workingDirectory = System.getProperty(Constants.USER_DIRECTORY_PROPERTY);

        if (osSuffix.contains(Constants.WINDOWS_OS)) {
            osSuffix = Constants.WINDOWS_OS;
        }
        if (osSuffix.contains("mac")) {
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

        try {
            tryIt.startAVD();
        } catch (IOException e) {
            tryIt.handleException("Unable to start AVD", e);
        }

        try {
            tryIt.checkEmulatorBoot();
        } catch (IOException e) {
            tryIt.handleException("Emulator boot process failure", e);
        }

        String[] agents = new String[2];

        try {
            agents = tryIt.checkForAgent();
        } catch (IOException ignored) {
            // can continue installing agent again
        }
        System.out.println("Starting Agent ...");
        try {
            tryIt.startPackage(agents);
        } catch (IOException e) {
            tryIt.handleException("Unable to start WSO2 package", e);
        }
        Process startShell = null;
        ProcessBuilder startShellProcessBuilder = new ProcessBuilder(tryIt.adbLocation, "shell");
        try {
            startShellProcessBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            startShellProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            startShell = startShellProcessBuilder.start();
        } catch (IOException ignored) {
            //can continue
        }
        System.out.println("Connected to device shell");
        try {
            if (startShell != null) {
                startShell.waitFor();
            }
        } catch (InterruptedException ignored) {
            // Interrupted if AVD is closed only.
        }
        System.out.println("Good Bye!");
    }

    /**
     * This method downloads the files.
     *
     * @param path       - the URL to download from.
     * @param folderName - the folder location to download the files to.
     */
    private void downloadArtifacts(String path, String folderName) {
        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        URL url = null;

        try {
            url = new URL(path);
        } catch (MalformedURLException e) {
            handleException("Downloading " + folderName + " failed.", e);
        }

        try {
            if (url != null) {
                rbc = Channels.newChannel(url.openStream());
            }
            fos = new FileOutputStream(folderName);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            if (!new File(folderName).delete()) {
                System.out.println("Delete " + folderName + " and try again");
            }
            handleException("Downloading " + folderName + " failed.", e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (rbc != null) {
                    rbc.close();
                }
            } catch (IOException ignored) {
                // File close exception ignored
            }
        }
    }


    /**
     * This method validates the Android SDK location provided by the user and write it to the file
     * sdkLocationFile.
     */
    private void setSDKPath() {
        System.out.println("Please provide android SDK location");
        String response = new Scanner(System.in, "UTF-8").next();
        String emulatorLocationPath = response + File.separator + "tools" + File.separator + "emulator";

        if (osSuffix.equals(Constants.WINDOWS_OS)) {                      // windows emulator location ends with .bat
            emulatorLocationPath += Constants.WINDOWS_EXTENSION_BAT;
        }

        if (new File(emulatorLocationPath).exists()) {
            Writer writer = null;
            try {
                writer = new OutputStreamWriter(new FileOutputStream(sdkLocationFile), StandardCharsets.UTF_8);
            } catch (FileNotFoundException e) {
                System.out.println("Unable to write the sdkLocation to file ");
            }
            try {
                if (writer != null) {
                    writer.write(response);
                }
            } catch (IOException e) {
                androidSdkHome = response;
                System.out.println("Unable to write the sdkLocation to file ");
                e.printStackTrace();
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException ignored) {
                    // writer close
                }
            }
        } else {
            System.out.println("Invalid SDK location");
            setSDKPath();
        }
    }


    /**
     * This method creates a folder named android-sdk and downloads the minimum tools for SDK
     * and write the sdk-location to the file sdkLocationFile.
     */
    private void getAndroidSDK() {
        String androidSdkFolderName = "android-sdk";

        //noinspection ResultOfMethodCallIgnored
        new File(workingDirectory + File.separator + androidSdkFolderName).mkdir();

        androidSdkHome = workingDirectory + File.separator + androidSdkFolderName;

        getTools(System.getProperty(Constants.SDK_TOOLS_URL), "_Android-sdk-tools.zip");

        getTools(System.getProperty(Constants.PLATFORM_TOOLS_URL), "_Android-platform-tools.zip");

        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(sdkLocationFile), StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            System.out.println("Unable to write the sdkLocation to file ");
        }
        try {
            if (writer != null) {
                writer.write(androidSdkHome);
            }
        } catch (IOException e) {
            System.out.println("Unable to write the sdkLocation to file ");
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ignored) {
                // writes close
            }
        }
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
     *
     * @throws IOException process throws  if an I/O error occurs.
     */
    private void startAVD() throws IOException {
        String wso2AvdLocation = userHome + File.separator + ".android" + File.separator + "avd" + File.separator
                + "WSO2_AVD.avd";

        checkForPlatform();
        checkForSystemImages();

        if (!new File(wso2AvdLocation).isDirectory()) {
            Scanner read = new Scanner(System.in, "UTF-8");
            System.out.println("Do you want to create WSO2_AVD with default configs (Y/n)?: ");
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

        ProcessBuilder listAVDsProcessBuilder = new ProcessBuilder(emulatorLocation, "-list-avds");
        Process listAVDsProcess = listAVDsProcessBuilder.start();

        ArrayList<String> devices = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(listAVDsProcess.getInputStream()
                    , StandardCharsets.UTF_8));
            String readLine;
            while ((readLine = reader.readLine()) != null) {
                devices.add(readLine);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (devices.size() == 0) {
            System.out.println("No AVDs available in the system ");
            startAVD();
        } else if (devices.size() == 1) {
            runEmulator(devices.get(0));
        } else {
            System.out.println("\nAvailable AVDs in the system\n");
            int count = 1;
            for (String device : devices) {
                System.out.println(count + " - - " + device);
                count++;
            }
            System.out.print("\nEnter AVD number to start (eg: 1) :");
            Scanner read = new Scanner(System.in, "UTF-8");
            int avdNo = read.nextInt();
            runEmulator(devices.get(--avdNo));
        }
    }

    /**
     * This method creates WSO2_AVD with the specific configurations.
     *
     * @throws IOException process throws  if an I/O error occurs.
     */
    private void createAVD() throws IOException {
        String avdManagerPath = androidSdkHome + File.separator + "tools" + File.separator + "bin"
                + File.separator + "avdmanager";
        String androidPath = androidSdkHome + File.separator + "tools" + File.separator + "android";
        String configFileLocation = workingDirectory + File.separator + "resources" + File.separator + "config.ini";
        String wso2ConfigFile = userHome + File.separator + ".android" + File.separator + "avd" + File.separator
                + "WSO2_AVD.avd" + File.separator + "config.ini";

        if (osSuffix.equals(Constants.WINDOWS_OS)) {
            avdManagerPath += Constants.WINDOWS_EXTENSION_BAT;
        }
        if (osSuffix.equals(Constants.WINDOWS_OS)) {
            androidPath += Constants.WINDOWS_EXTENSION_BAT;
        }
        setExecutePermission(androidPath);

        System.out.println("Creating a new AVD device");

        if (new File(avdManagerPath).exists()) {
            setExecutePermission(avdManagerPath);
            ProcessBuilder createAvdProcessBuilder = new ProcessBuilder(avdManagerPath, "create", "avd", "-k",
                    "system-images;android-23;default;x86", "-n", "WSO2_AVD");

            createAvdProcessBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            createAvdProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            Process createAvdProcess = createAvdProcessBuilder.start();

            try {
                createAvdProcess.waitFor();
            } catch (InterruptedException e) {
                handleException("Unable to create new AVD", e);
            }
        } else {
            ProcessBuilder createAvd = new ProcessBuilder(androidPath, "create", "avd", "-n", "WSO2_AVD",
                    "-t", "android-23");

            createAvd.redirectInput(ProcessBuilder.Redirect.INHERIT);
            createAvd.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            Process createAvdProcess = createAvd.start();

            try {
                createAvdProcess.waitFor();
            } catch (InterruptedException e) {
                handleException("Unable to create new AVD", e);
            }
        }
        Files.copy(Paths.get(configFileLocation), Paths.get(wso2ConfigFile), StandardCopyOption.REPLACE_EXISTING);
        startAVD();
    }

    /**
     * This method runs the Android Emulator for the name specified by deviceId.
     *
     * @param deviceId String name of the device.
     * @throws IOException process start throws  if an I/O error occurs.
     */
    private void runEmulator(String deviceId) throws IOException {

        // mac os and windows needs hardware_Accelerated_execution_Manager
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
                + File.separator + "25.0.2");

        if (!buildTools.exists()) {
            getTools(System.getProperty(Constants.BUILD_TOOL_URL), "_Android-build-tool.zip");

            File buildTool = new File(androidSdkHome + File.separator + "android-7.1.1");

            //noinspection ResultOfMethodCallIgnored
            new File(androidSdkHome + File.separator + "build-tools").mkdir();
            //noinspection ResultOfMethodCallIgnored
            buildTool.renameTo(new File(androidSdkHome + File.separator + "build-tools"
                    + File.separator + "25.0.2"));
        }
    }

    /**
     * This method halts the system until the emulator is fully booted
     * if boot process is not completed successfully, rest of the tasks won't be continued.
     *
     * @throws IOException process throws  if an I/O error occurs.
     */
    private void checkEmulatorBoot() throws IOException {
        BufferedReader reader;
        String readLine;
        Boolean sysBootComplete = false;

        do {
            ProcessBuilder systemBoot = new ProcessBuilder(adbLocation, "shell", "getprop",
                    "sys.boot_completed");
            Process systemBootProcess = systemBoot.start();
            try {
                systemBootProcess.waitFor();
            } catch (InterruptedException e) {
                handleException("System boot process interuppted", e);
            }
            reader = new BufferedReader(new InputStreamReader(systemBootProcess.getInputStream(),
                    StandardCharsets.UTF_8));
            while ((readLine = reader.readLine()) != null) {
                // if boot process is success the process gives 1 as output
                if (readLine.contains("1")) {
                    sysBootComplete = true;
                }
            }
            System.out.print(".");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
                // ignored
            } finally {
                reader.close();
            }
        } while (!sysBootComplete);
        System.out.println();
    }

    /**
     * This method gets the Android SDK location if available and sets the SDK path else downloads the SDK.
     */
    private void setAndroidSDK() {
        sdkLocationFile = new File("sdkLocation");

        if (!(sdkLocationFile.exists() && !sdkLocationFile.isDirectory())) {
            Scanner read = new Scanner(System.in, "UTF-8");
            System.out.println("Do you have an Android SDK installed on your computer (y/N)?: ");
            String response = read.next().toLowerCase();
            if (response.matches("y")) {
                setSDKPath();
            } else {
                getAndroidSDK();
            }
        }
        // writes the Android SDK location to sdkLocationFile file
        Scanner scanner = null;
        try {
            scanner = new Scanner(sdkLocationFile, "UTF-8");
            androidSdkHome = scanner.useDelimiter("\\Z").next();
        } catch (FileNotFoundException ignored) {
            System.out.println("sdkLocation file not found");
            // already written to androidSdkHome
        } finally {
            if (scanner != null) {
                scanner.close();
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
        System.exit(0);
    }

    /**
     * This method check for the android agent in the specified AVD and installs it if not available.
     *
     * @return package name and act name.
     * @throws IOException process throws  if an I/O error occurs.
     */
    private String[] checkForAgent() throws IOException {
        String apkFileLocation = workingDirectory + File.separator + "resources" + File.separator + "android-agent.apk";
        String aaptLocation = androidSdkHome + File.separator + "build-tools" + File.separator + "25.0.2"
                + File.separator + "aapt";

        if (osSuffix.equals(Constants.WINDOWS_OS)) {
            aaptLocation += Constants.WINDOWS_EXTENSION_EXE;
        }
        setExecutePermission(aaptLocation);

        //process to get the name of package and launchable-activity available in android agent apk file
        ProcessBuilder badgingApkFileProcessBuilder = new ProcessBuilder(aaptLocation, "d", "badging",
                apkFileLocation);
        Process badgingApkFileProcess = badgingApkFileProcessBuilder.start();

        String pkg = null;
        String activity = null;
        Boolean hasAgent = false;
        String readLine;
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(badgingApkFileProcess.getInputStream(),
                    StandardCharsets.UTF_8));


            while ((readLine = reader.readLine()) != null) {
                if (readLine.contains("package")) {
                    pkg = readLine.substring(readLine.indexOf(Constants.NAME) + 6).substring(0,
                            readLine.substring(readLine.indexOf(Constants.NAME)
                                    + 6).indexOf("'"));
                }
                if (readLine.contains("launchable-activity")) {
                    activity = readLine.substring(readLine.indexOf(Constants.NAME) + 6).substring(0,
                            readLine.substring(readLine.indexOf(Constants.NAME)
                                    + 6).indexOf("'"));
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        ProcessBuilder listPackages = new ProcessBuilder(adbLocation, "shell", "pm", "list", "packages");
        Process listPackagesProcess = listPackages.start();
        try {
            listPackagesProcess.waitFor();
        } catch (InterruptedException e) {
            System.out.println("Unable to read available packages in the current AVD");
        }

        try {
            reader = new BufferedReader(new InputStreamReader(listPackagesProcess.getInputStream(),
                    StandardCharsets.UTF_8));

            while ((readLine = reader.readLine()) != null) {
                if (readLine.contains("package:" + pkg)) {
                    hasAgent = true;
                }
            }
        } finally {
            reader.close();
        }


        if (!hasAgent) {
            installAgent();
        }
        return new String[]{pkg, activity};
    }

    /**
     * This method installs the Android Agent ( WSO2 iot agent ).
     *
     * @throws IOException process start throws  if an I/O error occurs.
     */
    private void installAgent() throws IOException {
        String androidAgentLocation = workingDirectory + File.separator + "resources" + File.separator
                + "android-agent.apk";

        System.out.println("Installing agent ...");
        ProcessBuilder installAgentProcessBuilder = new ProcessBuilder(adbLocation, "install",
                androidAgentLocation);
        Process installAgentProcess = installAgentProcessBuilder.start();

        try {
            installAgentProcess.waitFor();
        } catch (InterruptedException e) {
            System.out.println("WSO2 Agent installation failed");
            installAgent();
        }
    }

    /**
     * This method starts the package (wso2.iot.agent).
     *
     * @param agents package name and launchable activity name.
     * @throws IOException process throws  if an I/O error occurs.
     */
    private void startPackage(String[] agents) throws IOException {
        String pkg = agents[0];
        String activity = agents[1];

        ProcessBuilder pkgStartProcessBuilder = new ProcessBuilder(adbLocation, "shell", "am", "start",
                "-n", pkg + "/" + activity);
        Process pkgStartProcess = pkgStartProcessBuilder.start();

        try {
            pkgStartProcess.waitFor();
        } catch (InterruptedException e) {
            handleException("Package start process interuptted", e);
        }
    }

    /**
     * This method checks for the availability of Android Platform in SDK and if not available downloads it.
     */
    private void checkForPlatform() {
        File platform = new File(androidSdkHome + File.separator + "platforms" + File.separator + "android-23");

        if (!platform.isDirectory()) {
            getTools(System.getProperty(Constants.PLATFORM_URL), "_Android-platforms.zip");
            //noinspection ResultOfMethodCallIgnored
            new File(androidSdkHome + File.separator + "platforms").mkdir();
            //noinspection ResultOfMethodCallIgnored
            new File(androidSdkHome + File.separator + "android-6.0").renameTo(new File(androidSdkHome
                    + File.separator + "platforms" + File.separator + "android-23"));
        }
    }

    /**
     * This method checks for the system images in the Android SDK and downloads if not available.
     */
    private void checkForSystemImages() {
        File systemImages = new File(androidSdkHome + File.separator + "system-images"
                + File.separator + "android-23" + File.separator + "default");

        if (!systemImages.isDirectory()) {
            getTools(System.getProperty(Constants.SYSTEM_IMAGE_URL), "_sys-images.zip");
            //noinspection ResultOfMethodCallIgnored
            new File(androidSdkHome + File.separator + "system-images" + File.separator
                    + "android-23" + File.separator + "default").mkdirs();
            //noinspection ResultOfMethodCallIgnored
            new File(androidSdkHome + File.separator + "x86").renameTo(new File(androidSdkHome
                    + File.separator + "system-images" + File.separator + "android-23" + File.separator
                    + "default" + File.separator + "x86"));
        }
    }

    /**
     * This method install Hardware_Accelerated Execution_Manager in mac and windows os.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void installHAXM() {
        String haxmLocation = androidSdkHome + File.separator + "extras" + File.separator + "intel"
                + File.separator + "Hardware_Accelerated_Execution_Manager";

        if (!new File(haxmLocation).isDirectory()) {
            System.out.println("Downloading intel HAXM...");

            new File(haxmLocation).mkdirs();
            String folderName = "_haxm.zip";

            downloadArtifacts(System.getProperty(Constants.HAXM_URL), haxmLocation + File.separator
                    + folderName);
            System.out.println("Configuring HAXM...");
            extractFolder(haxmLocation + File.separator + folderName);

            String haxmInstaller = haxmLocation + File.separator + "silent_install";

            if (osSuffix.equals(Constants.WINDOWS_OS)) {
                haxmInstaller += Constants.WINDOWS_EXTENSION_BAT;
            } else {
                haxmInstaller += ".sh";
            }
            setExecutePermission(haxmInstaller);

            ProcessBuilder processBuilder = new ProcessBuilder(haxmInstaller, "-m", "2048", "-log",
                    workingDirectory + File.separator + "haxmSilentRun.log");
            processBuilder.directory(new File(haxmLocation));
            processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process process = null;
            try {
                process = processBuilder.start();
            } catch (IOException e) {
                handleException("HAXM installation failed", e);
            }
            try {
                if (process != null) {
                    process.waitFor();
                }
            } catch (InterruptedException e) {
                handleException("HAXM installation failed", e);
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
                qemuSystemFileLocation += "darwin" + "-x86_64" + File.separator + "qemu-system-i386";
                break;
            case Constants.WINDOWS_OS:
                qemuSystemFileLocation += osSuffix + "-x86_64" + File.separator + "qemu-system-i386.exe";
                break;
            default:
                qemuSystemFileLocation += osSuffix + "-x86_64" + File.separator + "qemu-system-i386";
        }
        setExecutePermission(qemuSystemFileLocation);
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.execute(new TryItEmulator(deviceId, emulatorLocation));
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
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                handleException("Virtual device not loaded properly, process interuptted", e);
            }
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
        if (!new File(fileName).canExecute()) {
            if (!new File(fileName).setExecutable(true)) {
                System.out.println("Set the Execute permission of : " + fileName + " to continue");
                System.exit(0);      // if can't execute, unable to proceed
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

            //noinspection ResultOfMethodCallIgnored
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
