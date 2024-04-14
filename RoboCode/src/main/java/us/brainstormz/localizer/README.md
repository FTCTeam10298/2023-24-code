Welcome to the Team 10298 Brain Stormz April Tag Localization System!

This code has multiple parts, but here's how you use it: 

1) CalibrateAprilTag finds the robot position on 4 points.
TAKE A PHOTO OF THAT DATA.
2) Find AprilTag offsets using the Excel spreadsheet FindAprilTagCalibration.xlsx
3) Create a FieldConfiguration with your values in AprilTagFieldConfigurations
4) Write it into TestAprilTagCalibration to test your configuration and see if it works.

Then you can use it in an opmode!
TestAprilTagCalibration is also a clear example of how to implement it... well, kinda.