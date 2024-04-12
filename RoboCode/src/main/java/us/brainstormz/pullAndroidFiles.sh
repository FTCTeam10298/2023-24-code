#!/bin/bash

set -e

mkdir -p ~/Desktop/ftc/odomCalibrate/allData/

adb pull /storage/emulated/0/Download/ ~/Desktop/ftc/odomCalibrate
echo Pulled files

mv ~/Desktop/ftc/odomCalibrate/Download/* ~/Desktop/ftc/odomCalibrate/allData/
echo Moved files

adb shell rm -r /storage/emulated/0/Download/
adb shell mkdir /storage/emulated/0/Download/
echo Wiped robot