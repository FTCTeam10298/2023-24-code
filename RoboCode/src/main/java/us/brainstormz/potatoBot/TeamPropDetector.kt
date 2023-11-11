package us.brainstormz.potatoBot

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.openCvAbstraction.OpenCvAbstraction

class TeamPropDetector(val telemetry: Telemetry, val propColor: TeamPropDetector.PropColors) {
    public enum class PropPosition {
        Left, Center, Right
    }

    enum class PropColors {
        Red, Blue
    }

    private val blue = Scalar(0.0, 0.0, 255.0)
    private val red = Scalar(225.0, 0.0, 0.0)
    private val black = Scalar(255.0, 0.0, 255.0)
    private val transparent = Scalar(0.0, 255.0, 0.0)

    private val tseThreshold = 135

    private val boxLocations = listOf(
        Rect(Point(0.0, 60.0), Point(40.0, 100.0)),
        Rect(Point(150.0, 60.0), Point(180.0, 100.0)),
        Rect(Point(290.0, 60.0), Point(320.0, 100.0)) )

    private val regions = listOf(
            PropPosition.Left to boxLocations[0],
            PropPosition.Center to boxLocations[1],
            PropPosition.Right to boxLocations[2],
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

        Imgproc.cvtColor(frame, intermediateHoldingFrame, Imgproc.COLOR_RGB2HSV)

        var DETECT_RED: Boolean = true;
        var MINIMUM_VALUES: Double = 100.0;
        var MAXIMUM_VALUES: Double = 255.0;
        var MINIMUM_BLUE_HUE: Double = 100.0;
        var MAXIMUM_BLUE_HUE: Double = 115.0;
        var MINIMUM_RED_LOW_HUE: Double = 0.0;
        var MAXIMUM_RED_LOW_HUE: Double = 25.0;
        var MINIMUM_RED_HIGH_HUE: Double = 160.0;
        var MAXIMUM_RED_HIGH_HUE: Double = 255.0;

        var MINIMUM_BLUE: Scalar = Scalar(MINIMUM_BLUE_HUE,MINIMUM_VALUES, MINIMUM_VALUES)
        var MAXIMUM_BLUE: Scalar = Scalar(MAXIMUM_BLUE_HUE, MAXIMUM_VALUES, MAXIMUM_VALUES)
        var MINIMUM_RED_LOW: Scalar = Scalar(MINIMUM_RED_LOW_HUE, MINIMUM_VALUES, MINIMUM_VALUES)
        var MAXIMUM_RED_LOW: Scalar = Scalar(MAXIMUM_RED_LOW_HUE, MAXIMUM_VALUES, MAXIMUM_VALUES)
        var MINIMUM_RED_HIGH: Scalar = Scalar(MINIMUM_RED_HIGH_HUE, MINIMUM_VALUES, MINIMUM_VALUES)
        var MAXIMUM_RED_HIGH: Scalar = Scalar(MAXIMUM_RED_HIGH_HUE, MAXIMUM_VALUES, MAXIMUM_VALUES)

        fun colorInRect(rect: Mat): Int {
            return Core.mean(rect).`val`[0].toInt()
        }

            if (propColor != PropColors.Red) {
                //Blue value
                Core.inRange(intermediateHoldingFrame, MINIMUM_BLUE, MAXIMUM_BLUE, intermediateHoldingFrame);
            } else {
                //Red value
                var mat1: Mat = intermediateHoldingFrame.clone()
                var mat2: Mat = intermediateHoldingFrame.clone()
                Core.inRange(mat1, MINIMUM_RED_LOW, MAXIMUM_RED_LOW, mat1)
                Core.inRange(mat2, MINIMUM_RED_HIGH, MAXIMUM_RED_HIGH, mat2)
                Core.bitwise_or(mat1, mat2, intermediateHoldingFrame)
            }
//
        submatsBlue = regions.map {
            it.first to intermediateHoldingFrame.submat(it.second)
        }
        submatsRed = regions.map {
            it.first to intermediateHoldingFrame.submat(it.second)
        }

        var prevColor = 0

        val bothSubmats = submatsBlue.mapIndexed{ i, it ->
            it to submatsRed[i]
        }

        var result = PropPosition.Right

        var indexVar = -1
        bothSubmats.forEach {
            indexVar ++

            val blueRect = it.first
            val blueColor = colorInRect(blueRect.second)
            telemetry.addLine("blueColor: $blueColor, ${it.first.first}")


            val redRect = it.second
            val redColor = colorInRect(redRect.second)
            telemetry.addLine("redColor: $redColor, ${it.first.first}")
        }


        when (propColor) {
            PropColors.Blue -> {
                submatsBlue.forEach {
                    val color = colorInRect(it.second)
                    if (color > prevColor) {
                        prevColor = color
                        result = it.first
                    }
                }
            }
            PropColors.Red -> {
                submatsBlue.forEach {
                    val color = colorInRect(it.second)
                    if (color > prevColor) {
                        prevColor = color
                        result = it.first
                    }
                }
            }
        }

/*
     allthecolorsLOL = submatsRed.map {
        it.first to submats
     }

     submatsBlue.forEach {
            val color = colorInRect(it.second)
    }
      submatsRed.forEach {
            val color = colorInRect(it.second)
    }
 */

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
    val tseDetector = TeamPropDetector(telemetry, TeamPropDetector.PropColors.Blue)

    /** Change Depending on robot */

    override fun runOpMode() {
        telemetry.addLine("he forgot to set gravity")
        telemetry.update()
//        /** INIT PHASE */
        opencv.init(hardwareMap)
        opencv.internalCamera = false
        opencv.cameraName = "Webcam 1" //DEFINE THIS IN HW CONFIG ON HUB!!
        opencv.cameraOrientation = OpenCvCameraRotation.UPSIDE_DOWN
        hardwareMap.allDeviceMappings.forEach { m ->
            println("HW: ${m.deviceTypeClass} ${m.entrySet().map{it.key}.joinToString(",")}")
        }
        opencv.onNewFrame(tseDetector::processFrame)

        waitForStart()
        /** AUTONOMOUS  PHASE */
    }

}