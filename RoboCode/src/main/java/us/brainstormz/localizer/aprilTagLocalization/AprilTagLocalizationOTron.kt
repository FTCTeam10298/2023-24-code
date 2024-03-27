package us.brainstormz.localizer.aprilTagLocalization

import FieldRelativePointInSpace
import TagRelativePointInSpace
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase
import us.brainstormz.localizer.PositionAndRotation
import kotlin.math.atan
import kotlin.math.*

class AprilTagLocalizationOTron(val cameraXOffset: Double, val cameraYOffset: Double) {
    //distance of the camera from the bot's center so that we find position relative to bot center
    // not camera center.
    fun getCameraPositionOnField(aprilTagID: Int, aprilTagInTagCentricCoords: TagRelativePointInSpace): FieldRelativePointInSpace {

        /** adds relative position to position of tags on field, because both are calculated with
         * the same origin (point with an x and y of 0), because then we reflect how much more/less the bot
         * position is than the apriltag position.
         */

        //This code is meant to be used 12-14 in. from the AprilTag, and will return coords.
        //based on the closest AprilTag, with an accuracy of +-1/2 inch.

        val angle = 0

        val tagRelativeToCamera = aprilTagInTagCentricCoords
        val tagRelativeToCameraOurCoordinateSystem = PositionAndRotation(
                x= tagRelativeToCamera.xInches + cameraXOffset,
                y= -tagRelativeToCamera.yInches + cameraYOffset,
                r= tagRelativeToCamera.headingDegrees //originally bearing, WATCH OUT
        )


        val tagRelativeToField = getAprilTagLocation(aprilTagID).posAndRot

        //I guess subtraction works? Some things should just be worked out with experimentation.
        //Has to be 12-14 inches from most on-center target for results accurate to +- one inch.
        val  robotRelativeToFieldX = tagRelativeToField.x + tagRelativeToCameraOurCoordinateSystem.x
        val robotRelativeToFieldY = tagRelativeToField.y + tagRelativeToCameraOurCoordinateSystem.y

        val robotRelativeToFieldRotation = tagRelativeToCameraOurCoordinateSystem.r

        //TODO: TRIGONOMETRYYYY IDK if this is complete :&
//        //We're trying to find the angle between the AprilTag Y Axis and the bot. Draw this out:
//        //There are two relevant angles: bearing, angle between us and the AprilTag, and
//        //the angle between that bearing. If we find all of these, we can finish the triangle with
//        //180 - those two angles, which means the angle between this angle and
//
//        //what is the point on AprilTagY that is at RobotX? A point that is (RobotX, AprilTagY).
//        //James wants the Y Axis
//
//        val distanceBetweenAprilTagAndBot = sqrt(tagRelativeToCameraOurCoordinateSystem.x.pow(2) +
//                tagRelativeToCameraOurCoordinateSystem.y.pow(2))
//
////        robotRelativeToFieldY = (sqrt(2.0)/2)
////        robotRelativeToFieldX = (sqrt(2.0)/2)
//
//        val intersectionBetweenBotAndAprilTagYAxis = PositionAndRotation(
//                x= robotRelativeToFieldX,
//                y= tagRelativeToField.y,
//                r= 0.0 //there really isn't one.
//        )
//        //So, what length of AprilTagY is that? That's just the x of that point we just made.
//
//        var distanceBetweenBotAndAprilTagXAxis = tagRelativeToField.x - robotRelativeToFieldX
//        println("Robot X Distance: $distanceBetweenBotAndAprilTagXAxis")
//
//        var distanceBetweenBotAndAprilTagYAxis = tagRelativeToField.y - robotRelativeToFieldY
//
//        val tangentOfAngleBetweenCenterOfAprilTagAndBot =
//                distanceBetweenBotAndAprilTagYAxis/distanceBetweenBotAndAprilTagXAxis
//
//        val angleBetweenCenterOfAprilTagAndRobot = Math.toDegrees(atan(tangentOfAngleBetweenCenterOfAprilTagAndBot))
////        println("Robot-AprilTag Angle Found: $angleBetweenCenterOfAprilTagAndRobot")
//        val angleBetweenAprilTagYAxisAndRobot = 90 - angleBetweenCenterOfAprilTagAndRobot
////        println("Robot-AprilTag Axis Angle Found: $angleBetweenAprilTagYAxisAndRobot")
//        //arctangent takes us home to the first angle! Yay!
////        val angleBetween

        return FieldRelativePointInSpace(robotRelativeToFieldX, robotRelativeToFieldY, tagRelativeToCameraOurCoordinateSystem.r)
        //yaw is equivalent to bearing
    }

    fun getAprilTagLocation(tagId: Int): AprilTagPositionOnField {
        val library = AprilTagGameDatabase.getCenterStageTagLibrary()
        val sdkPositionOnField = library.lookupTag(tagId).fieldPosition
        val tagRelativeToFieldOurCoordinateSystem = PositionAndRotation(
                x= sdkPositionOnField.get(1).toDouble(),
                y= sdkPositionOnField.get(0).toDouble(),
                r= 0.0
        )
        return AprilTagPositionOnField(tagRelativeToFieldOurCoordinateSystem)
    }

    data class AprilTagPositionOnField(val posAndRot: PositionAndRotation)


    /**Robot location on the field, where the center of the field is (0, 0).
     * x and y are the 2d position. unit is Inches
     * r is the rotation. unit is Degrees*/
    data class RobotPositionOnField(val posAndRot: PositionAndRotation)
}
