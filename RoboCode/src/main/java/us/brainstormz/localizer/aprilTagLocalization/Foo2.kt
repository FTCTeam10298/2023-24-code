package us.brainstormz.localizer.aprilTagLocalization

import org.firstinspires.ftc.vision.apriltag.AprilTagDetection

//find the average error and just subtract that much.
data class AprilTuningOffsets(
        var xInches:Double,
        var yInches:Double,
        var rDegrees:Double,
)

data class AprilLocalizationResult(
    var x:Double,
    var y:Double,
    var r:Double,
)

fun doIt(detectedTags:List<AprilTagDetection>, offets:AprilTuningOffsets):AprilLocalizationResult? {
    return null
}