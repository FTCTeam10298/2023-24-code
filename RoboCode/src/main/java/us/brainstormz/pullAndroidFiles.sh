#!/bin/bash

set -x
TIMESTAMP=`date +"%Y-%m-%d.%H%M%S"`
LABEL="$1"

if [ -z "$LABEL" ]
then
    echo "NO LABEL"
else
    echo "Using label '$LABEL'"
    LABEL="-$LABEL"
fi

DEST=~/Desktop/ftc/odomCalibrate/$TIMESTAMP$LABEL
mkdir -p $DEST/allData/
file $DEST/allData/

adb pull /storage/emulated/0/Download/ $DEST
echo Pulled files

file $DEST/allData/

mv $DEST/Download/* $DEST/allData/
echo Moved files


file $DEST/allData/

adb shell rm -r /storage/emulated/0/Download/
adb shell mkdir /storage/emulated/0/Download/
echo Wiped robot