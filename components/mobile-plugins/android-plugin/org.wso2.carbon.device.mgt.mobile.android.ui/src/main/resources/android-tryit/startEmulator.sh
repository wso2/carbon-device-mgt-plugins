#!/bin/bash
echo Welcome

OS_SUFFIX="linux"
if [[ "$OSTYPE" == "darwin"* ]]; then
    OS_SUFFIX="macosx"
fi

sdk_tools_url="https://dl.google.com/android/repository/tools_r25.2.5-$OS_SUFFIX.zip"
platform_tools_url="http://dl.google.com/android/repository/platform-tools_r25.0.3-$OS_SUFFIX.zip"
build_tools_url="https://dl.google.com/android/repository/build-tools_r25.0.2-$OS_SUFFIX.zip"
platform_url="https://dl.google.com/android/repository/platform-23_r03.zip"
sys_img_url="https://dl.google.com/android/repository/sys-img/android/x86-23_r09.zip"
haxm_url="https://dl.google.com/android/repository/extras/intel/haxm-macosx_r6_0_5.zip"

java\
 -Dsdk_tools_url=$sdk_tools_url\
 -Dplatform_tools_url=$platform_tools_url\
 -Dbuild_tools_url=$build_tools_url\
 -Dplatform_url=$platform_url\
 -Dsys_img_url=$sys_img_url\
 -Dhaxm_url=$haxm_url\
 -jar EmulatorJava.jar
echo
