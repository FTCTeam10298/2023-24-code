package us.brainstormz.threeDay

import org.opencv.core.*
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.opencv.imgproc.Imgproc
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.openCvAbstraction.OpenCvAbstraction

enum class PropPosition {
    Left, Center, Right
}

enum class PropColors {
    Red, Blue
}

class PropDetector(private val telemetry: Telemetry, private val colorToDetect: PropColors) {

    private val rects = listOf(
        Rect(Point(0.0, 60.0), Point(40.0, 100.0)),
        Rect(Point(150.0, 60.0), Point(180.0, 100.0)),
        Rect(Point(290.0, 60.0), Point(320.0, 100.0))
    )

    private val positionsMappedToRects = listOf(
        PropPosition.Left to rects[0],
        PropPosition.Center to rects[1],
        PropPosition.Right to rects[2]
    )

    private val blue = Scalar(0.0, 0.0, 255.0)
    private val green = Scalar(0.0, 255.0, 0.0)
    private val red = Scalar(255.0, 0.0, 0.0)
    private val white = Scalar(255.0, 255.0, 255.0)
    private val black = Scalar(0.0, 0.0, 0.0)
    private val rectanglesMappedToBorderColors = listOf(
        positionsMappedToRects[0] to red,
        positionsMappedToRects[1] to green,
        positionsMappedToRects[2] to blue
    )

    private enum class Colors(val scalar: Scalar) {
        MinBlue(Scalar(100.0, 100.0, 100.0)),
        MaxBlue(Scalar(115.0, 255.0,255.0)),
        MinLowRed(Scalar(0.0,100.0, 100.0)),
        MaxLowRed(Scalar(25.0,255.0,255.0)),
        MinHighRed(Scalar(160.0, 100.0,100.0)),
        MaxHighRed(Scalar(255.0,255.0,255.0)),
    }

    @Volatile
    var propPosition = PropPosition.Left

    private var mat = Mat()
    private var redLowMat = Mat()
    private var redHighMat = Mat()
    private var regions: List<Mat> = listOf()

    fun processFrame(frame: Mat): Mat {
        Imgproc.cvtColor(frame, mat, Imgproc.COLOR_RGB2HSV)

        when (colorToDetect) {
            PropColors.Blue -> {
                Core.inRange(mat, Colors.MinBlue.scalar, Colors.MaxBlue.scalar, mat)
            }
            PropColors.Red -> {
                Core.inRange(mat, Colors.MinLowRed.scalar, Colors.MaxLowRed.scalar, redLowMat)
                Core.inRange(mat, Colors.MinHighRed.scalar, Colors.MaxHighRed.scalar, redHighMat)
                Core.bitwise_or(redLowMat, redHighMat, mat)
            }
        }
        regions = positionsMappedToRects.map { it ->
            mat.submat(it.second)
        }

        val values = regions.map { it ->
            Core.sumElems(it).`val`[0]
        }

        regions.forEach {
            it.release()
        }

        val leftValue = values[0]
        val centerValue = values[1]
        val rightValue = values[2]

        propPosition = if (leftValue >= rightValue && leftValue >= centerValue) {
            PropPosition.Left
        } else if (rightValue >= centerValue) {
            PropPosition.Right
        } else {
            PropPosition.Center
        }

        telemetry.addLine("propPosition: $propPosition")

        rectanglesMappedToBorderColors.forEach { it ->
            val borderColor = if (it.first.first == propPosition) white else it.second
            Imgproc.rectangle(frame, it.first.second, borderColor, 3)
        }

        val sizePerColor = 10.0
        Colors.values().forEachIndexed {i, it ->
            val rect = Rect(Point(i*sizePerColor, 0.0), Point((i*sizePerColor)+sizePerColor, 10.0))
            Imgproc.rectangle(frame, rect, it.scalar, 2)
        }

        telemetry.update()

        return frame
    }
}


@Autonomous
class JamesVisionTest/** Change Depending on robot */: LinearOpMode() {


    /** Change Depending on robot */
    override fun runOpMode() {
        val opencv = OpenCvAbstraction(this)
        val tseDetector = PropDetector(telemetry, PropColors.Blue)

        opencv.init(hardwareMap)
        opencv.internalCamera = false
        opencv.cameraName = "Webcam 1"
        opencv.cameraOrientation = OpenCvCameraRotation.UPSIDE_DOWN
        hardwareMap.allDeviceMappings.forEach { m ->
            println("HW: ${m.deviceTypeClass} ${m.entrySet().map{it.key}.joinToString(",")}")
        }
        opencv.onNewFrame(tseDetector::processFrame)

        waitForStart()
        /** AUTONOMOUS  PHASE */
    }

}