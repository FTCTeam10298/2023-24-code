package us.brainstormz.localizer.aprilTagLocalization

import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import us.brainstormz.localizer.PositionAndRotation
import kotlin.math.abs

class AprilTagLocalizer {
    val aprilTagLocalizationFunctions = AprilTagLocalizationFunctions(0.0, 0.0, )

    //find the average error and just subtract that much.
    data class AprilTuningOffsets(
            var xInches: Double,
            var yInches: Double,
            var rDegrees: Double,
    )

    data class AprilLocalizationResult(
            var x: Double,
            var y: Double,
            var r: Double,
    )

//    fun calculateFieldPosition(detectedTags: List<AprilTagDetection>, offsets: AprilTuningOffsets): PositionAndRotation? {
//        aprilTagLocalization.findClosestAprilTagToBot(currentDetections)
//        return null
//    }


}