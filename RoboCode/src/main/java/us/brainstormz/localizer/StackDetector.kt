package us.brainstormz.localizer

import locationTracking.PosAndRot
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

data class StackDetection(
    val xPosition:Double,
    val whenDetected:Long
)

class StackDetectorVars(
    var targetHue:StackDetector.TargetHue,
    var displayMode:StackDetector.Mode
)

class StackDetector(private val vars:StackDetectorVars, private val telemetry: Telemetry){

    enum class Mode {
        FRAME,
        MASK
    }

    enum class TargetHue {
        RED,
        BLUE,
        absRED
    }

    class NamedVar(val name: String, var value: Double)


    class ColorRange(val L_H: NamedVar, val L_S: NamedVar, val L_V: NamedVar, val U_H: NamedVar, val U_S: NamedVar, val U_V: NamedVar)

    private val redSolarizedColor = ColorRange(
            L_H = NamedVar("Low Hue", 120.0),
            L_S = NamedVar("Low Saturation", 150.0),
            L_V = NamedVar("Low Vanity/Variance/VolumentricVibacity", 0.0),
            U_H = NamedVar("Uppper Hue", 130.0),
            U_S = NamedVar("Upper Saturation", 255.0),
            U_V = NamedVar("Upper Vanity/Variance/VolumentricVibracity", 200.0)
    )

    private val redAbsoluteColor = ColorRange(
            L_S = NamedVar("Low Saturation", 150.0),
            L_H = NamedVar("Low Hue", 120.0),
            L_V = NamedVar("Low Vanity/Variance/VolumentricVibacity", 0.0),
            U_H = NamedVar("Uppper Hue", 255.0),
            U_S = NamedVar("Upper Saturation", 255.0),
            U_V = NamedVar("Upper Vanity/Variance/VolumentricVibracity", 255.0)
    )

    val redColor = ColorRange(
            L_S = NamedVar("Low Saturation", 70.0),
            L_H = NamedVar("Low Hue", 115.0),
            L_V = NamedVar("Low Vanity/Variance/VolumentricVibacity", 90.0),
            U_H = NamedVar("Uppper Hue", 150.0),
            U_S = NamedVar("Upper Saturation", 255.0),
            U_V = NamedVar("Upper Vanity/Variance/VolumentricVibracity", 255.0))

    //trained for yellow
    val blueColor = ColorRange(
            L_S = NamedVar("Low Saturation", 20.0),
            L_H = NamedVar("Low Hue", 0.0),
            L_V = NamedVar("Low Vanity/Variance/VolumentricVibacity", 55.0),
            U_H = NamedVar("Upper Hue", 35.0),
            U_S = NamedVar("Upper Saturation", 255.0),
            U_V = NamedVar("Upper Vanity/Variance/VolumentricVibracity", 255.0))

    val goalColor: ColorRange = when(vars.targetHue){
        TargetHue.RED -> redColor
        TargetHue.BLUE -> blueColor
        TargetHue.absRED -> redAbsoluteColor
    }

//    var x = 0.0
//    var y = 0.0

    private val hsv = Mat()
    private val maskA = Mat()
    private val maskB = Mat()
    private val kernel = Mat(5, 5, CvType.CV_8U)

    private var mostRecentDetection:StackDetection? = null

    fun detectedPosition(staleThresholdAgeMillis:Long):Double? {
        return mostRecentDetection?.let{detection ->
            val t = System.currentTimeMillis() - staleThresholdAgeMillis
            if(detection.whenDetected >= t) detection.xPosition else null
        }
    }

    fun convert(matOfPoint2f: MatOfPoint2f): MatOfPoint {
        val foo = MatOfPoint()
        matOfPoint2f.convertTo(foo, CvType.CV_32S)
        return foo
    }

    private fun heightOfLineOnScreen(line:Pair<Point, Point>):Double {
        return line.second.y - line.first.y

    }

    fun processFrame(frame: Mat): Mat {
        val color = when (vars.targetHue) {
            TargetHue.RED -> redColor
            TargetHue.BLUE -> blueColor
            TargetHue.absRED -> redColor
        }

        // Pre-process the image to make contour detection easier
        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV)
        val lower = Scalar(color.L_H.value, color.L_S.value, color.L_V.value)
        val upper = Scalar(color.U_H.value, color.U_S.value, color.U_V.value)
        Core.inRange(hsv, lower, upper, maskA)
        Imgproc.erode(maskA, maskB, kernel)

        // Find the contours
        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(maskB, contours, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        val areaTheshold = 0
        val largeEnoughContours = contours.filter{ cnt: MatOfPoint ->
            val area = Imgproc.contourArea(cnt)

            area >= areaTheshold
        }

            // Dig through them
        val shapesByArea = largeEnoughContours.map{ cnt: MatOfPoint ->
            val area = Imgproc.contourArea(cnt)

            fun convert(src: MatOfPoint): MatOfPoint2f {
                val dst = MatOfPoint2f()
                src.convertTo(dst, CvType.CV_32F)
                return dst
            }

            val cnt2f = convert(cnt)
            val points = MatOfPoint2f()
            Imgproc.approxPolyDP(cnt2f, points, 0.02 * Imgproc.arcLength(cnt2f, true), true)


            val pointsList = points.toList()

            area to pointsList
        }

        val largestShape = shapesByArea.maxByOrNull { it.first }?.second

        val detectedStack = largestShape

        if(detectedStack!=null){
            drawLinesFromPoints(detectedStack, frame)

            val center = specialXAlgorithm(detectedStack)//centroid(detectedStack)

            val topY = -240.0
            val bottomY = 240.0

            val pointAtTheTop = Point(center.x, topY)
            val pointAtBottom = Point(center.x, bottomY)

            Imgproc.line(frame, center, pointAtTheTop, lower, 10)
            Imgproc.line(frame, center, pointAtBottom, upper, 10)
            Imgproc.putText(frame, "x: ${String.format("%.2f", center.x)}", center, Imgproc.FONT_HERSHEY_PLAIN, 0.8, Scalar(0.0, 0.0, 0.0))

            mostRecentDetection = StackDetection(
                    xPosition = center.x,
                    whenDetected = System.currentTimeMillis()
            )
        }

        return when (vars.displayMode) {
            Mode.FRAME -> frame
            Mode.MASK -> maskB
        }
    }

    private fun specialXAlgorithm(points: List<Point>): Point {
        val topThird = points.sortedBy { it.y }.take(points.size / 3)
//        telemetry.addLine("topThird top 2: ${topThird.take(2)}")
        val centerY = topThird.fold(0.0) {acc, point -> acc + point.y } / topThird.size
        val centerX = topThird.fold(0.0) {acc, point -> acc + point.x } / topThird.size
//        telemetry.addLine("centerX: $centerX")
        return Point(centerX, centerY)
    }

    private fun centroid(points: List<Point>): Point {
        val result = points.fold(Point()){ acc, it ->
            Point(acc.x + it.x, acc.y + it.y)
        }
        result.x /= points.size
        result.y /= points.size

        return result
    }

    private fun areRoughlyInSameXPlane(a: Point, b: Point): Boolean {
        val fudge = 42
        return abs(b.x - a.x) <= fudge
    }


    fun drawLinesFromPoints(points: List<Point>, toFrame:Mat) {
        Imgproc.drawContours(
                toFrame,
                listOf(convert(MatOfPoint2f(*(points.toTypedArray())))),
                0,
                Scalar(0.0, 255.0, 0.0),
                2)
    }

    fun drawLines(points: MatOfPoint2f, toFrame:Mat) {

        Imgproc.drawContours(
                toFrame,
                listOf(convert(points)),
                0,
                Scalar(0.0, 255.0, 0.0),
                2)
    }

}