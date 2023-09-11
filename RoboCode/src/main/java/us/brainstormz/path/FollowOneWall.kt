package us.brainstormz.path

import us.brainstormz.localizer.PositionAndRotation

class FollowOneWall:Path{
    override fun length() = 144.0

    override fun positionAt(distance: Double) = PositionAndRotation(
        x= if(distance > length()) length() else distance/length(),
        y= 0.0,
        r= 0.0)
}

