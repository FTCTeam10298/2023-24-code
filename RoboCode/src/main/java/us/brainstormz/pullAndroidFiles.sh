
adb pull /storage/emulated/0/Download/ /Users/jamespenrose/ftc/odomCalibrate

echo Pulled files
#mv /Users/jamespenrose/ftc/odomCalibrate/Download/ /Users/jamespenrose/ftc/odomCalibrate/

adb shell rm -r /storage/emulated/0/Download/
adb shell mkdir /storage/emulated/0/Download/

echo Wiped robot