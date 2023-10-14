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

class TeamPropDetector(val telemetry: Telemetry, val propColor: TeamPropDetector.PropColors) {
    enum class PropPosition {
        Left, Center, Right
    }

    enum class PropColors {
        Red, Blue
    }

    var theColorWeAreLookingFor = PropColors.Red

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
    private lateinit var colFrame: Mat
    private lateinit var submatsBlue: List<Pair<PropPosition, Mat>>
    private lateinit var submatsRed: List<Pair<PropPosition, Mat>>

//    private var filtered = Mat()
    private var redFrame = Mat()
    private var blueFrame = Mat()
    private var intermediateHoldingFrame = Mat()
    fun processFrame(frame: Mat): Mat {

        fun colorInRect(rect: Mat): Int {
            return Core.mean(rect).`val`[0].toInt()
        }

        //
//    /**
//     * This function takes the RGB frame
//     * and then extracts some channel to some variable
//     */


        fun inputToColor(frame: Mat, colorToReturn: PropColors): Mat {
            //subtract red from blue in detection

            //coi explanation: input, output, color channel iso. in output: 0 -> R, 1 -> G?, 2 -> B
//            Imgproc.cvtColor(input, yCrCb, Imgproc.COLOR_RGB2YCrCb)
            var coi = when (colorToReturn) {
                PropColors.Red -> 0
                PropColors.Blue -> 3
            }
            Core.extractChannel(frame, intermediateHoldingFrame, coi)
            return intermediateHoldingFrame
        }

        blueFrame = inputToColor(frame, PropColors.Blue)
        redFrame = inputToColor(frame, PropColors.Red)

        submatsBlue = regions.map {
            it.first to intermediateHoldingFrame.submat(it.second)
        }
        submatsRed = regions.map {
            it.first to intermediateHoldingFrame.submat(it.second)
        }

        var result = PropPosition.Right
        var prevColor = 0

        submatsBlue.forEach {
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

        return blueFrame
    }
//



}


@Autonomous
class ThuUnderstudyTest/** Change Depending on robot */: LinearOpMode() {

    val opencv = OpenCvAbstraction(this)
    val tseDetector = TeamPropDetector(telemetry, TeamPropDetector.PropColors.Blue)

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