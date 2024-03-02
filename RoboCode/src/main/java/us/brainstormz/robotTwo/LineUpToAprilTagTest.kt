package us.brainstormz.robotTwo

import android.util.Size
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.vision.VisionPortal
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import org.firstinspires.ftc.vision.apriltag.AprilTagPoseFtc
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor

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
""".trimIndent()

fun aprilTagDetectionToString(tag: AprilTagDetection): String = """
        AprilTagDetection(
        id= ${tag.id},
        ftcPose= ${formatFtcPose(tag.ftcPose)},
        frameAcquisitionNanoTime= ${tag.frameAcquisitionNanoTime})
    """.trimIndent()


class AprilTagPipeline(val cameraName: String, val resolution: Size) {
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
//    fun resumeStreaming() {
//        visionPortal!!.resumeStreaming()
//    }

}


class LineUpToAprilTagTest(private val hardware: RobotTwoHardware, private val telemetry: Telemetry, private val aprilTagPipeline: AprilTagPipeline) {

    private val detectionWeWantId = 6
    fun loop() {
        val currentDetections: List<AprilTagDetection> = aprilTagPipeline.detections()

        val detectionWeWant: AprilTagDetection? = currentDetections.firstOrNull {
            it.id == detectionWeWantId
        }

        telemetry.addLine("All detections: ${currentDetections.fold("") {acc, it -> "$acc \n${aprilTagDetectionToString(it)}"}}")
        telemetry.addLine("\n\nOur detection: ${detectionWeWant?.let { aprilTagDetectionToString(it) }}")
        telemetry.update()
    }
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
    }

    override fun loop() {
        program.loop()
    }

}