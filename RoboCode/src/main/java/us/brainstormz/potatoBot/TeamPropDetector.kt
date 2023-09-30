package us.brainstormz.potatoBot

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import us.brainstormz.examples.ExampleHardware
import us.brainstormz.telemetryWizard.TelemetryConsole
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.telemetryWizard.GlobalConsole.console
//
class TeamPropDetector(val telemetry: Telemetry) {
    enum class PropPosition {
        Left, Center, Right
    }

    private val blue = Scalar(0.0, 0.0, 255.0)
    private val red = Scalar(225.0, 0.0, 0.0)
    private val black = Scalar(255.0, 0.0, 255.0)
    private val transparent = Scalar(0.0, 255.0, 0.0)


    private val tseThreshold = 135

    private val orangePlaces = listOf(
            Rect(Point(100.0, 240.0), Point(0.0, 100.0)),
            Rect(Point(210.0, 100.0), Point(110.0, 240.0)),
            Rect(Point(220.0, 100.0), Point(300.0, 240.0)) )

    private val regions = listOf(
            PropPosition.Left to orangePlaces[0],
            PropPosition.Center to orangePlaces[1],
            PropPosition.Right to orangePlaces[2],
    )

    private val colors = listOf(
            PropPosition.Left to blue,
            PropPosition.Center to black,
            PropPosition.Right to red
    )

    @Volatile // Volatile since accessed by OpMode thread w/o synchronization
    var position = PropPosition.Left

//    fun init(frame: Mat): Mat {
////        val cbFrame = inputToCb(frame)
//
//        return frame
//    }

    private lateinit var submats: List<Pair<PropPosition, Mat>>
    private lateinit var cbFrame: Mat

    private var yCrCb = Mat()
    private var cb = Mat()

    fun processFrame(frame: Mat): Mat {

        fun colorInRect(rect: Mat): Int {
            return Core.mean(rect).`val`[0].toInt()
        }

        //
//    /**
//     * This function takes the RGB frame, converts to YCrCb,
//     * and extracts the Cb channel to the cb variable
//     */

        fun inputToCb(input: Mat?): Mat {
            Imgproc.cvtColor(input, yCrCb, Imgproc.COLOR_RGB2YCrCb)
            Core.extractChannel(yCrCb, cb, 1)
            return cb
        }

        cbFrame = inputToCb(frame)

        submats = regions.map {
            it.first to cbFrame.submat(it.second)
        }

        var result = PropPosition.Right
        var prevColor = 0
        submats.forEach {
            val color = colorInRect(it.second)
            if (color > prevColor) {
                prevColor = color
                result = it.first
            }
        }

        position = result

        colors.forEach {
            val rect = regions.toMap()[it.first]
            Imgproc.rectangle(frame, rect, it.second, 2)
        }
//
        telemetry.addLine("Position: $position")
        telemetry.addLine("Highest Color: $prevColor")
        telemetry.update()

        return frame
    }
//



}


@Autonomous
class ThuUnderstudyTest/** Change Depending on robot */: LinearOpMode() {

    val opencv = OpenCvAbstraction(this)
    val tseDetector = TeamPropDetector(telemetry)

    /** Change Depending on robot */

    override fun runOpMode() {
        telemetry.addLine("he forgot to set gravity")
        telemetry.update()
//        /** INIT PHASE */
        opencv.init(hardwareMap)
        opencv.internalCamera = false
        opencv.cameraName = "Webcam 1" //DEFINE THIS IN HW CONFIG ON HUB!!
//        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_LEFT
        hardwareMap.allDeviceMappings.forEach { m ->
            println("HW: ${m.deviceTypeClass} ${m.entrySet().map{it.key}.joinToString(",")}")
        }
        opencv.onNewFrame(tseDetector::processFrame)

        waitForStart()
        /** AUTONOMOUS  PHASE */
    }

}