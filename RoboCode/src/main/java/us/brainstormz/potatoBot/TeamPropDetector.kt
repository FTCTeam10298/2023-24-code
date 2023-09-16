package us.brainstormz.potatoBot

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import us.brainstormz.examples.ExampleHardware
import us.brainstormz.telemetryWizard.TelemetryConsole
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.telemetryWizard.GlobalConsole.console

class TeamPropDetector() {
    enum class TSEPosition {
        One, Two, Three
    }

    private val blue = Scalar(0.0, 0.0, 255.0)
    private val red = Scalar(225.0, 0.0, 0.0)
    private val black = Scalar(255.0, 0.0, 255.0)
    private val transparent = Scalar(0.0, 255.0, 0.0)


    private val tseThreshold = 135

    private val orangePlaces = listOf(
        Rect(Point(100.0, 240.0), Point(0.0, 100.0)),
        Rect(Point(210.0, 100.0), Point(110.0, 240.0)),
        Rect(Point(220.0, 100.0), Point(300.0, 240.0)))

    private val regions = listOf(
        TSEPosition.One to orangePlaces[0],
        TSEPosition.Two to orangePlaces[1],
        TSEPosition.Three to orangePlaces[2],
    )

    private val colors = listOf(
        TSEPosition.One to blue,
        TSEPosition.Two to black,
        TSEPosition.Three to red
    )

    @Volatile // Volatile since accessed by OpMode thread w/o synchronization
    var position = TSEPosition.One

//    fun init(frame: Mat): Mat {
////        val cbFrame = inputToCb(frame)
//
//        return frame
//    }

    private lateinit var submats: List<Pair<TSEPosition, Mat>>
    private lateinit var cbFrame: Mat

    fun processFrame(frame: Mat): Mat {

        cbFrame = inputToCb(frame)

        submats = regions.map {
            it.first to cbFrame.submat(it.second)
        }

        var result = TSEPosition.Three
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

        console.display(8, "Position: $position")
        console.display(9, "Highest Color: $prevColor")

        return frame
    }

    private fun colorInRect(rect: Mat): Int {
        return Core.mean(rect).`val`[0].toInt()
    }

    /**
     * This function takes the RGB frame, converts to YCrCb,
     * and extracts the Cb channel to the cb variable
     */
    private var yCrCb = Mat()
    private var cb = Mat()
    private fun inputToCb(input: Mat?): Mat {
        Imgproc.cvtColor(input, yCrCb, Imgproc.COLOR_RGB2YCrCb)
        Core.extractChannel(yCrCb, cb, 1)
        return cb
    }
}

@Autonomous
class ThuUnderstudyTest/** Change Depending on robot */: LinearOpMode() {

    val opencv = OpenCvAbstraction(this)
    val tseDetector = TeamPropDetector()

    /** Change Depending on robot */

    override fun runOpMode() {
        println("he forgot to set gravity")
//        /** INIT PHASE */
        opencv.init(hardwareMap)
        opencv.internalCamera = false
        opencv.cameraName = "Webcam 1" //DEFINE THIS IN HW CONFIG ON HUB!!
        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_LEFT
        hardwareMap.allDeviceMappings.forEach { m ->
            println("HW: ${m.deviceTypeClass} ${m.entrySet().map{it.key}.joinToString(",")}")
        }
//        opencv.onNewFrame(tseDetector::processFrame)

        waitForStart()
        /** AUTONOMOUS  PHASE */
    }

}