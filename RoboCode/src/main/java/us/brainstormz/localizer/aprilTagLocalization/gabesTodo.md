## Todo from James (Lovingly, -ya boi)



(realistic error: sub 0.5)
determine the bounds of accurate angles
accept offset for x, y, and r
(test function)

MAGICAL MYSTICAL APRILTAG PUBLISHING TRACK
- James coords. - done I think
- Pick Best Apriltag Function - very done
- Test & Retest to Get To 1/10th inch, 1 degree max error
- Handle nulls ()

//Teague ask - function to find how many apriltags

//when angle between 0 and 1, subtract .4 or .5 from X

//subtract 180 from angle, get negative angle

//TESTING EXTREME TAG DETECTION CASES
- Decide position
- hardcode tag to look at
- test whether angle (yikes) and position (RELATIVE TO TAG) are accurate (error: angle: 1 degree, 1/10 in for position)

1. ~~Find which tag its reading from. (very quick)~~
2. ~~Finish "chooseBestAprilTag" function so it will filter through the list of april tag readings a return the best one~~
3. Test the chooseBestAprilTag by lining the bot up to one tag and making sure it chooses it as the best tag
4. Test whether the tag diff will still be small if the robot does a 360 then returns to the original position
5. Perform *Changing tag test*
6. Let me know your results
7. haha no more hw changes

*Changing tag test:*
1. cover all but one tag
2. line up robot to uncovered tag
3. turn on the test opmode
4. note the diff
5. change which tag is uncovered
6. note the diff
7. line up robot to new uncovered tag

5 in. from target up to 24 in. on Y axis, with camera within corner-to-corner of the Apriltags, with max angle = 0 Bearing, 30 degrees yaw.

Non-AprilTag Todo:

Set off some notification (light, vibration) when pixels are engaged. (heartbeat?) - DONE!

Rumble code: RobotTwoTeleOp.kt lines 42-45, 793-808, 953-955 - USED!

LIGHTING 

Only fill 1/2 or 1/4 of lights to accelerate play

Implement init — done!
data representation of pixels - done
Create actualizer - use 1 draw command, experimentation - done!
Implement flood pixels - Done!
Implement draw half - Done!
Connect to bot - Done!
Complete - Done!

(James wants draw half to take >= 5ms per loop - update in groups of pixels from the center & if color changed overwrite)
if called again, do another 5 on each side.



Objective: 5 ms per run (/loop)  show2Colors

Pt. 1—refactor existing light code to use my library - Done!
Pt. 2—rewrite lighting updater to be lazy—compare past and present state – return past - DONE!!!
Pt. 3—rewrite show 2Colors to update x lights at a time… find first wrong light, fill x lights on both sides with target colors (lazy rewriter will handle overwrites) 
Pt. 4—Test with just continuous rewrites and then do switchback—repeat
(james has a loop timing class!)

