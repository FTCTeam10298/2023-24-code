package us.brainstormz.localizer

import FieldRelativePointInSpace
import android.util.Size
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import us.brainstormz.localizer.aprilTagLocalization.AprilTagFieldConfigurations
import us.brainstormz.localizer.aprilTagLocalization.AprilTagLocalizationFunctions
import us.brainstormz.localizer.aprilTagLocalization.AprilTagPipelineForEachCamera
import us.brainstormz.localizer.aprilTagLocalization.ReusableAprilTagFieldLocalizer
import us.brainstormz.robotTwo.RobotTwoHardware

class AprilTagLocalizerRepackaged(
        private val telemetry: Telemetry,
        private val aprilTagPipeline: AprilTagPipelineForEachCamera
): Localizer {

    private val aprilTagLocalizationMath = AprilTagLocalizationFunctions(
            cameraXOffset= 0.0,
            cameraYOffset= RobotTwoHardware.cameraBackwardOffsetFromRobotCenterInches,
            telemetry = telemetry
    )
    private val currentFieldConfiguration = AprilTagFieldConfigurations.fieldConfigurationNoOffsets
//    AprilTagFieldConfigurations.fieldConfigurationNoOffsets
    private val aprilTagLocalizer = ReusableAprilTagFieldLocalizer(
            aprilTagLocalization = aprilTagLocalizationMath,
            averageErrorRedSide = currentFieldConfiguration.RedAllianceOffsets,
            averageErrorBlueSide =  currentFieldConfiguration.BlueAllianceOffsets
    )

    fun FieldRelativePointInSpace.toPositionAndRotation() = PositionAndRotation(
            x = this.xInches,
            y = this.yInches,
            r = this.headingDegrees
    )

    private var currentPositionAndRotation: PositionAndRotation? = null
    override fun currentPositionAndRotation(): PositionAndRotation = currentPositionAndRotation ?: PositionAndRotation()
    override fun recalculatePositionAndRotation() {
        val aprilTagDetections = aprilTagPipeline.detections()

        val aprilTagsAreDetected = aprilTagDetections.isNotEmpty()
        currentPositionAndRotation = if (aprilTagsAreDetected) {

            val closestAprilTag: AprilTagDetection = aprilTagLocalizationMath.findClosestAprilTagToBot(
                    allAprilTags = aprilTagDetections
            )

            val theTargetAprilTagPositionInfo = aprilTagLocalizer.returnAprilTagInFieldCentricCoords(
                    aprilTag = closestAprilTag
            )

            theTargetAprilTagPositionInfo.FieldRelativePointInSpace.toPositionAndRotation()
        } else {
            currentPositionAndRotation
        }
    }

    override fun setPositionAndRotation(newPosition: PositionAndRotation) {
        val message = "This doesn't mean anything to april tags. They're absolute."
        telemetry.addLine(message)
        println(message)
    }
}

@TeleOp
class TestAprilTagLocalizerRepackaged: OpMode() {
    private val aprilTagPipeline = AprilTagPipelineForEachCamera("Webcam 1", Size(640, 480))
    private val localizerRepackaged = AprilTagLocalizerRepackaged(telemetry, aprilTagPipeline)

    private val hardware: RobotTwoHardware = RobotTwoHardware(telemetry= telemetry, opmode = this)

    override fun init() {
        hardware.init(hardwareMap)
    }

    override fun start() {
        aprilTagPipeline.init(viewContainerId = null, hardwareMap = hardwareMap)
        aprilTagPipeline.resumeStreaming()
    }

    override fun loop() {
        localizerRepackaged.recalculatePositionAndRotation()
        val currentPosition = localizerRepackaged.currentPositionAndRotation()
        telemetry.addLine("currentPosition: $currentPosition")
        telemetry.update()
    }

    override fun stop() {
        aprilTagPipeline.close()
    }
}