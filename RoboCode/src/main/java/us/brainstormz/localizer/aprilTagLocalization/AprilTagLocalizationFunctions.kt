package us.brainstormz.localizer.aprilTagLocalization

import FieldRelativePointInSpace
import TagRelativePointInSpace
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.localizer.PositionAndRotation
import kotlin.math.*

class AprilTagLocalizationFunctions(val cameraXOffset: Double, val cameraYOffset: Double, val telemetry: Telemetry = PrintlnTelemetry()) {

    fun calcRobotPositionFromCameraPosition(cameraPositionAndRotation: FieldRelativePointInSpace, cameraOffsetFromRobotCenterYInches: Double): FieldRelativePointInSpace {

        val angleForTrigRadians = Math.toRadians(cameraPositionAndRotation.headingDegrees - 90)
        telemetry.addLine("angleForTrigRadians: $angleForTrigRadians")

        val xOffset = cos(angleForTrigRadians) * cameraOffsetFromRobotCenterYInches
        val yOffset = sin(angleForTrigRadians) * cameraOffsetFromRobotCenterYInches

        telemetry.addLine("xOffset: $xOffset")
        telemetry.addLine("yOffset: $yOffset")

        val offsetPosition = FieldRelativePointInSpace(
                xInches = xOffset + cameraPositionAndRotation.xInches,
                yInches = yOffset + cameraPositionAndRotation.yInches,
                headingDegrees = cameraPositionAndRotation.headingDegrees
        )
        telemetry.addLine("offsetPosition: $offsetPosition")

        return offsetPosition
    }

    //distance of the camera from the bot's center so that we find position relative to bot center
    // not camera center.
    fun getCameraPositionOnField(aprilTagID: Int,
                                 aprilTagInTagCentricCoords: TagRelativePointInSpace,
                                 allianceSideFound: ReusableAprilTagFieldLocalizer.AllianceSide
    ): FieldRelativePointInSpace {


        val positionOfTagOnField = getAprilTagLocation(aprilTagID).posAndRot

        telemetry.addLine("aprilTagInTagCentricCoords: $aprilTagInTagCentricCoords")
        telemetry.addLine("positionOfTagOnField: $positionOfTagOnField")

        val cameraCenterPositionOnField = FieldRelativePointInSpace(
                xInches = positionOfTagOnField.x + aprilTagInTagCentricCoords.xInches,
                yInches = positionOfTagOnField.y - aprilTagInTagCentricCoords.yInches,
                headingDegrees = aprilTagInTagCentricCoords.headingDegrees //originally bearing, WATCH OUT
        )

        telemetry.addLine("cameraCenterPositionOnField: $cameraCenterPositionOnField")

        val tagRelativeToCameraOurCoordinateSystem = calcRobotPositionFromCameraPosition(
                cameraPositionAndRotation = cameraCenterPositionOnField,
                cameraOffsetFromRobotCenterYInches = cameraYOffset
        )
        telemetry.addLine("tagRelativeToCameraOurCoordinateSystem: $tagRelativeToCameraOurCoordinateSystem")

        //TODO: Convert to James's coordinate system.



        //I guess subtraction works? Some things should just be worked out with experimentation.
        //Has to be 12-14 inches from most on-center target for results accurate to +- one inch.

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

        return tagRelativeToCameraOurCoordinateSystem
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

    fun findClosestAprilTagToBot(allAprilTags: List<AprilTagDetection>): AprilTagDetection {
        val sortedByRange = allAprilTags.sortedBy {
            val rangeAbs = it.ftcPose.range.absoluteValue
//            println("yawAbs: $yawAbs, tag ID: ${it.id}")
            rangeAbs
        }
//        val topTwoRange = listOf(sortedByRange[0], sortedByRange[1])
//        val differenceInBetweenTopChoices = (topTwoRange[1].ftcPose.yaw - topTwoRange[0].ftcPose.yaw).absoluteValue
//        val areTheYawValuesSuperClose:Boolean = differenceInBetweenTopChoices < 0.5 //too large... 0.5 worked better
//        return if (areTheYawValuesSuperClose) {
//            val closestToCenter: AprilTagDetection = topTwoRange.minBy {
//                val bearing = it.ftcPose.bearing.absoluteValue
////                println("bearing: $bearing, tag ID: ${it.id}")
//                bearing
//            }
//            closestToCenter
//        }
//        else {

        return sortedByRange[0]
//        }
    }

    data class AprilTagPositionOnField(val posAndRot: PositionAndRotation)


    /**Robot location on the field, where the center of the field is (0, 0).
     * x and y are the 2d position. unit is Inches
     * r is the rotation. unit is Degrees*/
    data class RobotPositionOnField(val posAndRot: PositionAndRotation)
}
