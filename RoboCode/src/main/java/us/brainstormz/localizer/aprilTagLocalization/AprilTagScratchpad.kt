//package us.brainstormz.localizer.aprilTagLocalization
//
//import kotlin.math.cos
//import kotlin.math.sin
//
//class AprilTagScratchpad {
//
//    fun main () {
//        val aprilTagLocalization = AprilTagLocalizationFunctions(
//                cameraXOffset=0.00,
//                cameraYOffset=0.00 //it's right on center! Yay!
//        )
//
//
//        val targetAprilTagID: Int = 2
//        val inputCamRelative = CameraRelativePointInSpace(xInches=10.0, yInches=10.0, yawDegrees= 10.0)
//
//        val expectedOutputTagRelative = TagRelativePointInSpace(xInches=11.585, yInches=8.112, headingDegrees= 10.0)//heading degrees = yaw
//
//        val actualOutputTagRelative = returnCamCentricCoordsInTagCentricCoordsPartDeux(inputCamRelative)
//
//        val inputTagRelative = actualOutputTagRelative
//
//        val expectedOutputFieldRelative = FieldRelativePointInSpace(xInches=46.995, yInches=52.138, headingDegrees = 10.0)
//
//        val actualOutputFieldRelative = aprilTagLocalization.getCameraPositionOnField(targetAprilTagID, inputTagRelative,
//                allianceSideFound = ReusableAprilTagFieldLocalizer.AllianceSide.Red)
//
//
//        println("Our camera-relative input was $inputCamRelative" )
//        println("Our expected tag-relative output was $expectedOutputTagRelative")
//        println("...but we got this: $actualOutputTagRelative")
//
//        println("So we took that tag-relative position, $inputTagRelative.")
//        println("We expected to see a position of $expectedOutputFieldRelative")
//        println("Instead, we calculated that our camera is at $actualOutputFieldRelative.")
//
//    }
//
//    data class CameraRelativePointInSpace(val xInches: Double, val yInches: Double, val yawDegrees: Double)
//    data class TagRelativePointInSpace(val xInches: Double, val yInches: Double, val headingDegrees: Double)
//
//    data class FieldRelativePointInSpace(val xInches: Double, val yInches: Double, val headingDegrees: Double)
//
//
//
//    fun returnCamCentricCoordsInTagCentricCoordsPartDeux(anyOldTag: CameraRelativePointInSpace): TagRelativePointInSpace {
//        //go look in the FTC documentation, you absolutely need to understand the FTC AprilTag Coordinate System
//
//        val yawRadians = Math.toRadians(anyOldTag.yawDegrees)
//
//        val aSectionOfXInches = anyOldTag.xInches/ cos(yawRadians)
//        val yOutsideOfSquareInches = aSectionOfXInches * sin(yawRadians)
//
//        //another angle in the triangle.
//        val qDegrees: Double = 180 - 90 - anyOldTag.yawDegrees
//
//        val qRadians = Math.toRadians(qDegrees)
//
//        val yInsideOfSquareInches = anyOldTag.yInches - yOutsideOfSquareInches
//
//        val otherPartOfXInches = yInsideOfSquareInches * cos(qRadians)
//        val yRelativeToTagInches = yInsideOfSquareInches * sin(qRadians)
//        val xRelativeToTagInches = aSectionOfXInches + otherPartOfXInches
//
//        return TagRelativePointInSpace(xInches=xRelativeToTagInches, yInches=yRelativeToTagInches, headingDegrees=anyOldTag.yawDegrees)
//
//    }
//}