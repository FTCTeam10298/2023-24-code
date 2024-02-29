## Todo from James (Lovingly, -ya boi)

1. ~~Find which tag its reading from. (very quick)~~
2. ~~Finish "chooseBestAprilTag" function so it will filter through the list of april tag readings a return the best one~~
3. Test the chooseBestAprilTag by lining the bot up to one tag and making sure it chooses it as the best tag
4. Test whether the tag diff will still be small if the robot does a 360 then returns to the original position
5. Perform *Changing tag test*
6. Let me know your results

*Changing tag test:*
1. cover all but one tag
2. line up robot to uncovered tag
3. turn on the test opmode
4. note the diff
5. change which tag is uncovered
6. note the diff
7. line up robot to new uncovered tag

Non-AprilTag Todo:

Set off some notification (light, vibration) when pixels are engaged. (heartbeat?) - DONE!

Rumble code: RobotTwoTeleOp.kt lines 42-45, 793-808, 953-955

LIGHTING 

Implement init — done!
data representation of pixels - done
Create actualizer - use 1 draw command, experimentation 
Implement flood pixels - Done!
Implement draw half - Done!
Connect to bot 
Complete 

(James wants draw half to take >= 5ms per loop - update in groups of pixels from the center & if color changed overwrite)
if called again, do another 5 on each side.

