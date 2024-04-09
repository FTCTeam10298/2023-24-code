package us.brainstormz.robotTwo.onRobotTests

import android.util.Size
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.vision.VisionPortal
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import org.firstinspires.ftc.vision.apriltag.AprilTagPoseFtc
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.tuningAndCalibration.PidConfig
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.RobotTwoPropDetector
import us.brainstormz.robotTwo.subsystems.Drivetrain

fun formatFtcPose(ftcPose: AprilTagPoseFtc): String = """
   |AprilTagPoseFtc(
   |    x= ${ftcPose.x},
   |    y= ${ftcPose.y},
   |    z= ${ftcPose.z},
   |    bearing= ${ftcPose.bearing},
   |    range= ${ftcPose.range},
   |    elevation= ${ftcPose.elevation},
   |    yaw= ${ftcPose.yaw},
   |    pitch= ${ftcPose.pitch},
   |    roll= ${ftcPose.roll},)
""".trimMargin()

fun aprilTagDetectionToString(tag: AprilTagDetection): String = """
        AprilTagDetection(
        id= ${tag.id},
        ftcPose= ${formatFtcPose(tag.ftcPose)},
        frameAcquisitionNanoTime= ${tag.frameAcquisitionNanoTime})
    """.trimMargin()


open class AprilTagPipeline(val cameraName: String, val resolution: Size) {
    private var aprilTag: AprilTagProcessor? = null
    private var visionPortal: VisionPortal? = null

    fun init(viewContainerId:Int?, hardwareMap: HardwareMap) {
        aprilTag = AprilTagProcessor.Builder().build()

        aprilTag?.setDecimation(1f)

        val builder = VisionPortal.Builder()
                .enableLiveView(true)
                .setStreamFormat(VisionPortal.StreamFormat.YUY2)
        viewContainerId?.let(builder::setLiveViewContainerId)

        builder.setCameraResolution(resolution)
        builder.setCamera(hardwareMap.get(WebcamName::class.java, cameraName))
        builder.addProcessor(aprilTag)



        visionPortal = builder.build()
    }

    fun close() {
        visionPortal!!.close()
    }

    fun detections() = aprilTag?.detections ?: emptyList()
}

class AprilTagLineup {

    private fun getTagIdOffsetForAlliance(alliance: RobotTwoHardware.Alliance): Int {
        return when (alliance) {
            RobotTwoHardware.Alliance.Red -> 3
            RobotTwoHardware.Alliance.Blue -> 0
        }
    }

    private fun getTagIdForPropPosition(propPosition: RobotTwoPropDetector.PropPosition, offsetForAlliance: Int): Int {
        return offsetForAlliance + when (propPosition) {
            RobotTwoPropDetector.PropPosition.Left -> 1
            RobotTwoPropDetector.PropPosition.Center -> 2
            RobotTwoPropDetector.PropPosition.Right -> 3
        }
    }

    fun findDetection(detections: List<AprilTagDetection>, propPosition: RobotTwoPropDetector.PropPosition, alliance: RobotTwoHardware.Alliance): AprilTagDetection? {
        val id = getTagIdForPropPosition(propPosition, getTagIdOffsetForAlliance(alliance))
        return detections.firstOrNull{ it.id == id }
    }

    fun getTargetPositionToLineupWithTag(currentPosition: PositionAndRotation, previousPosition: PositionAndRotation, aprilTagReading: AprilTagDetection?, previousAprilTagReading: AprilTagDetection?): PositionAndRotation {
        return if (aprilTagReading != previousAprilTagReading && aprilTagReading != null) {
            previousPosition.copy(x = currentPosition.x - aprilTagReading.ftcPose.x)
        } else {
            previousPosition
        }
    }
}

data class LineUpToAprilTagConfig(
        val sidewaysPID: PidConfig,
)

class LineUpToAprilTagTest(private val hardware: RobotTwoHardware, private val telemetry: Telemetry, private val aprilTagPipeline: AprilTagPipeline) {

    private val aprilTagLineup: AprilTagLineup = AprilTagLineup()

    private val drivetrain = Drivetrain(hardware, RRTwoWheelLocalizer(hardware, hardware.inchesPerTick), telemetry)
//
//    private lateinit var config: LineUpToAprilTagConfig
//    private lateinit var configServer: ConfigServer
//    fun init() {
//        config = LineUpToAprilTagConfig(
//                PidConfig(sidewaysPID)
//        )
//        configServer = ConfigServer(
//                port = 8083,
//                get = { jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(config) },
//                update = {
//                    config = jacksonObjectMapper().readValue(it)
//                    sidewaysPID = config.sidewaysPID.toPID()
//                },
//                getInfoToPrint = {"No Telemetry Passed"})
//    }

    var sidewaysPID = PID("sidewaysPID",
                            kp = 0.12,
                            ki = 1.0E-6,
                            kd = 1.0
    )

    var previousDetectionWeWant: AprilTagDetection? = null
    var previousTarget = Drivetrain.DrivetrainTarget(PositionAndRotation())
    private val detectionWeWantId = 5
    fun loop(gamepad1: Gamepad) {
        drivetrain.xTranslationPID = sidewaysPID
        val currentDetections: List<AprilTagDetection> = aprilTagPipeline.detections()

        val tag = aprilTagLineup.findDetection(currentDetections, RobotTwoPropDetector.PropPosition.Center, RobotTwoHardware.Alliance.Red)
        val previousTag = previousDetectionWeWant//aprilTagLineup.findDetection(listOf(previousDetectionWeWant?:currentDetections), RobotTwoPropDetector.PropPosition.Center, RobotTwoHardware.Alliance.Red)

        val currentPosition = drivetrain.getPosition()

        val targetPosition = aprilTagLineup.getTargetPositionToLineupWithTag(
                previousPosition = previousTarget.targetPosition,
                currentPosition = currentPosition,
                aprilTagReading = tag,
                previousAprilTagReading = previousTag
        )

        telemetry.addLine("\n\ntargetPosition: ${targetPosition}")
        telemetry.addLine("currentPosition: ${currentPosition}")
        val positionError = currentPosition - targetPosition
        telemetry.addLine("positionError: ${positionError}\n\n")


        val driverControlPower = Drivetrain.DrivetrainPower(
                x= gamepad1.left_stick_x.toDouble(),
                y= -gamepad1.left_stick_y.toDouble(),
                r= gamepad1.right_stick_x.toDouble())

        val drivetrainTarget = Drivetrain.DrivetrainTarget(
                targetPosition = targetPosition,
//                power = driverControlPower,
//                movementMode =
//                if ((driverControlPower.x+driverControlPower.y+driverControlPower.y) > 0.1) {
//                    DualMovementModeSubsystem.MovementMode.Power
//                } else {
//                    DualMovementModeSubsystem.MovementMode.Position
//                }
        )

        drivetrain.actuateDrivetrain(
                target = drivetrainTarget,
                actualPosition = currentPosition,
                previousTarget = previousTarget)

        telemetry.addLine("All detections: ${currentDetections.fold("") {acc, it -> "$acc \n${aprilTagDetectionToString(it)}"}}")
        telemetry.addLine("\n\nOur detection: ${tag?.let { aprilTagDetectionToString(it) }}")
        telemetry.update()
        previousTarget = drivetrainTarget
        previousDetectionWeWant = tag
    }


//    fun stop() {
//        configServer.stop()
//    }
}

@TeleOp
class LineUpToAprilTagTestOpMode: OpMode() {
    private val hardware = RobotTwoHardware(telemetry, this)
    private lateinit var program: LineUpToAprilTagTest

    override fun init() {
        hardware.init(hardwareMap)

        val aprilTagPipeline = AprilTagPipeline(hardware.backCameraName, Size(1920, 1080))
        aprilTagPipeline.init(null, hardwareMap)

        program = LineUpToAprilTagTest(hardware, telemetry, aprilTagPipeline)
//        program.init()
    }

    override fun loop() {
        program.loop(gamepad1)
    }

    override fun stop() {
//        program.stop()
    }
}