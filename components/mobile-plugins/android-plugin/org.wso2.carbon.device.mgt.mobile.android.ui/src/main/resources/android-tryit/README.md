Prerequisites
===============
1. You should have curl in your computer.
2. Java 7 or higher.

Instructions
=================

1. Run 'start' script in your terminal.
2. If you already have android sdk in your computer, please provide location of the sdk. 
   Otherwise this tool will download and install minimum SDK components which needs to run the emulator. 
   This is a one time process.
3. This tool will ask to create AVD if you don't have any in your computer. 
   Otherwise you can select existing AVD to try out IoT Agent.


Troubleshooting
==================

1. If your exisitng SDK doen't work or giving any errors, delete 'sdklocation' file and try agin without selecting the existing SDK path.
2. If your emulator does not start correctly, please remove all files and directories in $HOME/.android/avd/ directory. Then try again with a new emulator.
