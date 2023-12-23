package us.brainstormz.localizer

import org.firstinspires.ftc.vision.apriltag.AprilTagDetection

interface AprilTagAndOdometryLocalizer: Localizer {
    val aprilTagPositionFinder: AprilTagPositionFinder
    val odometryLocalizer: Localizer
}

interface AprilTagPositionFinder {
    fun recalculatePositionBasedOnDetection(detection: AprilTagDetector.RobotRelativeAprilTagDetection): PositionAndRotation
}

interface AprilTagDetector {
    enum class CamerasOnOurRobot {
        Front,
        Back
    }
    data class RobotRelativeAprilTagDetection (
        val detection: AprilTagDetection,
        val detectingCamera: CamerasOnOurRobot,
        val detectionConfidence: Int
    ) {
        val tagId = detection.id
        val tagPosition = detection.ftcPose
        val timeOfDetectionMilis: Long = detection.frameAcquisitionNanoTime / 1000000
    }
    fun isDetectionRelevant(detection: RobotRelativeAprilTagDetection): Boolean
    fun getNewAprilTagDetections(): List<RobotRelativeAprilTagDetection>
}