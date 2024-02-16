package us.brainstormz.robotTwo

import org.opencv.core.*
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.opencv.imgproc.Imgproc

class RobotTwoPropDetector(private val telemetry: Telemetry, private val colorToDetect: PropColors) {
    enum class PropPosition {
        Left, Center, Right
    }

    enum class PropColors {
        Red, Blue
    }

    private val positionsMappedToRects = listOf(
        PropPosition.Left to Rect(Point(0.0, 170.0), Point(40.0, 230.0)),
        PropPosition.Center to Rect(Point(120.0, 150.0), Point(200.0, 200.0)),
        PropPosition.Right to Rect(Point(290.0, 170.0), Point(320.0, 230.0))
    )

    private val blue = Scalar(0.0, 0.0, 255.0)
    private val green = Scalar(0.0, 255.0, 0.0)
    private val red = Scalar(255.0, 0.0, 0.0)
    private val white = Scalar(255.0, 255.0, 255.0)
    private val black = Scalar(0.0, 0.0, 0.0)
    private val colorToDetectAsScalar = mapOf(PropColors.Blue to blue, PropColors.Red to red).toMap()[colorToDetect]

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
    private var regions: Map<PropPosition, Mat> = emptyMap()

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
        regions = positionsMappedToRects.associate { (position, rect) ->
            position to mat.submat(rect)
        }

        val colorIntensities = regions.map { (position, rect) ->
            position to Core.sumElems(rect).`val`[0]
        }.toMap()

        regions.forEach {(position, rect) ->
            rect.release()
        }

        propPosition = colorIntensities.entries.maxBy { (position, value) ->
            telemetry.addLine("color Intensity $position = $value")
            value
        }.key

        positionsMappedToRects.forEach {(position, rect) ->
            val borderColor = if (position == propPosition) colorToDetectAsScalar else white
            Imgproc.rectangle(frame, rect, borderColor, 3)
        }

        val sizePerTinyBox = 10.0
        Colors.entries.forEachIndexed { i, it ->
            val tinyBox = Rect(Point(i*sizePerTinyBox, 0.0), Point((i*sizePerTinyBox)+sizePerTinyBox, 10.0))
            Imgproc.rectangle(frame, tinyBox, it.scalar, 2)
        }

        telemetry.update()

        return frame
    }
}